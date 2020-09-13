package recordsnbawiki;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.CREATE;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Jorick
 */
public class RecordsNBAWiki {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        obtenirFichierTexte(32975);
    }

    /**
     * Méthode permettant d'obtenir le fichier texte contenant les records 
     * mis en forme dans le dossier "fichiers" à partir de l'identifiant REALGM du joueur
     * 
     * @param identifiant = l'identifiant REALGM du joueur
     */
    private static void obtenirFichierTexte(int identifiant) {
        
        // récupération du contenu brut du code source
        String contenu = recuperationContenu(identifiant); 
        
        // récupération du titre de la page
        String titre = recuperationTitre(identifiant);

        // traitement du contenu pour obtenir la liste des records
        ArrayList<Record> listeRecords = traitementContenu(contenu);
        
        // ajout des informations des records à notre template
        contenu = preparationContenuFinal(listeRecords);
        
        // écriture du contenu final dans le fichier au nom du joueur
        ecritureDansFichier(contenu, recuperationNomJoueur(titre));
    }

    /**
     * Méthode permettant de récupérer le titre de la page sans balise HTML
     *
     * @param identifiant = l'identifiant REALGM du joueur
     * @return le titre de la page sans balise HTML
     */
    private static String recuperationTitre(int identifiant) {

        String titre = null;

        try {
            // url de la page du joueur
            // obligé de mettre un nom de base (ici LJ) mais pas d'impact sur les infos récupérées
            URL url = new URL("https://basketball.realgm.com/player/LeBron-James/Bests/" + identifiant + "/NBA");

            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

            String ligne;
            while ((ligne = br.readLine()) != null && titre == null) {

                // récupération du titre de la page pour avoir le nom du joueur
                if (ligne.startsWith("<title>")) {
                    titre = ligne;
                }
            }

        } catch (MalformedURLException e) {
            System.err.println("Erreur : " + e);
        } catch (IOException e) {
            System.err.println("Erreur : " + e);
        }

        // récupération du titre sans la balise <title>
        Document docTitre = Jsoup.parse(titre);
        Element linkTitre = docTitre.select("title").first();
     
        return linkTitre.text();
    }

    /**
     * Méthode permettant de récupérer le contenu utile (les infos
     * sur les records) via le code source de la page
     *
     * @param identifiant = l'identifiant REALGM du joueur
     * @return le texte présent dans les balises <td> 
     */
    private static String recuperationContenu(int identifiant) {

        String contenu = "";

        try {
            // url de la page du joueur
            // obligé de mettre un nom de base (ici LJ) mais pas d'impact sur les infos récupérées
            URL url = new URL("https://basketball.realgm.com/player/LeBron-James/Bests/" + identifiant + "/NBA");

            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder contenuBrut = new StringBuilder();

            // booleen permettant de garder que le contenu désiré
            boolean contenuValide = false;

            String ligne;
            while ((ligne = br.readLine()) != null) {

                // on détermine des "balises" pour ne récupérer que le contenu utile
                if (ligne.equals("<h2>NBA Regular Season Career Highs</h2>")) {
                    contenuValide = true;
                } else if (ligne.startsWith("<p class=\"footnote\">")) {
                    contenuValide = false;
                }

                // si le contenu est compris entre les deux balises délimitées,
                // on l'ajoute au SB
                if (contenuValide) {
                    contenuBrut.append(ligne);
                    contenuBrut.append(System.lineSeparator());
                }
            }

            // récupération du contenu du SB (le code HTML utile) pour le traiter avec jsoup
            String html = contenuBrut.toString();
            Document document = Jsoup.parse(html);

            // récupération du nombre d'éléments <td> présents
            int size = document.select("td").size();

            StringBuilder contenuTraite = new StringBuilder(); // SB contenant le contenu sans balise HTML (càd le texte)

            int i = 0;
            while (i < size) {
                // récupération des éléments <td>
                Element link = document.select("td").get(i);

                // ajout de l'élément <td> sans balise HTML au SB
                contenuTraite.append(link.text()).append("\n");

                i++;
            }

            contenu = contenuTraite.toString();

        } catch (MalformedURLException e) {
            System.err.println("Erreur : " + e);
        } catch (IOException e) {
            System.err.println("Erreur : " + e);
        }

        return contenu;
    }

    /**
     * Méthode permettant de traiter le contenu pour obtenir chaque information 
     * sur le record séparément (valeur, adversaire, date)
     * 
     * @param contenu
     * @return une liste contenant tous les records
     */
    private static ArrayList<Record> traitementContenu(String contenu) {
        ArrayList<Record> listeRecords = new ArrayList();

        // on sépare notre texte pour le traiter ligne par ligne
        String[] lignes = contenu.split("\n");

        // pour chaque ligne du texte
        for (int i = 0; i < lignes.length; ++i) {
            String ligne = lignes[i];

            // si le record existe
            if (!ligne.startsWith("-")) {
                
                // on sépare les mots pour récupérer ceux qui nous intéressent
                String[] ligneSeparee = ligne.split(" ");

                // la date correspond au dernier mot de chaque ligne
                String date = ligneSeparee[ligneSeparee.length - 1];
                // on transforme la date dans le format souhaité
                String dateEnFrancais = transformerDate(date);

                // la valeur du record correspond au premier mot
                String valeur = ligneSeparee[0];

                String adversaire;

                // si la ligne contient "times", cela signifie que le record 
                // a été effectué plusieurs fois. Cependant, on ne peut récupérer
                // les infos que sur la dernière fois qu'il a été fait.
                // On se contente donc de dire qu'il a été fait plusieurs fois
                
                if (!ligne.contains("times")) {
                    // s'il n'y a pas "times", l'adversaire correspond au 3e mot
                    // s'il y a un '@', on le rajoute devant l'adversaire (match à l'extérieur)
                    if (ligne.contains("@")) {
                        adversaire = "@ " + transformerAdversaire(ligneSeparee[2]);
                    } else {
                       adversaire = transformerAdversaire(ligneSeparee[2]); 
                    }
                    
                    listeRecords.add(new Record(valeur, adversaire, dateEnFrancais));
                } else {
                    
                    // s'il y a "times", le nombre de fois que le record a été 
                    // fait correspond au 2e mot auquel on rajoute "fois" et 
                    // la date est laisée vide
                    adversaire = ligneSeparee[1] + " fois";

                    listeRecords.add(new Record(valeur, adversaire, ""));
                }
            } else {
                listeRecords.add(new Record("-", "-", "-"));
            }
        }

        return listeRecords;
    }

    /**
     * Méthode permettant d'écrire le contenu dans un fichier .txt 
     * au nom du joueur
     *
     * @param contenu
     * @param nom = le nom du joueur
     */
    private static void ecritureDansFichier(String contenu, String nom) {

        byte data[] = contenu.getBytes();

        // on crée ou écrit dans le fichier correspondant au nom du joueur
        Path fichier = Paths.get("fichiers/" + nom + ".txt");

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(fichier, CREATE))) {
            out.write(data, 0, data.length);
        } catch (IOException e) {
            System.err.println("Erreur : " + e);
        }
    }

    /**
     * Méthode permettant de récupérer le nom et le prénom du joueur 
     * à partir du titre de la page
     *
     * @param titre = titre de la page
     * @return prénom_nom du joueur
     */
    private static String recuperationNomJoueur(String titre) {
        
        String prenom = titre.split(" ")[0];
        String nom = titre.split(" ")[1];

        return prenom + "_" + nom;
    }
 
    /**
     * Méthode permettant d'inscrire les records dans le template
     * 
     * @param listeRecords
     * @return le contenu final
     */
    private static String preparationContenuFinal(ArrayList<Record> listeRecords) {
            
        String contenuTemplate = "";
        String contenuFinal = "";
        
        // récupération du contenu du template
        try {
            File fichier = new File("fichiers/template.txt");

            Scanner sc = new Scanner(fichier);
            StringBuilder sb = new StringBuilder();
            
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine());
                sb.append(System.lineSeparator());
            }
            
            contenuTemplate = sb.toString();
            
        } catch (IOException e) {
            System.err.println("Erreur : " + e);
        }
        
        // séparation du texte pour le traiter ligne par ligne
        String[] lignes = contenuTemplate.split("\n");
        
        // pour chaque ligne du texte
        for (int i = 0; i < lignes.length; ++i) {
            String ligne = lignes[i];
               
            if (!ligne.startsWith("|-")) {
                // séparation de la ligne mot par mot
                String[] ligneSeparee = ligne.split(" ");
                
                // le record souhaité (représenté par un nombre) correspond au dernier mot de la ligne
                String index = ligneSeparee[ligneSeparee.length - 1];

                // on remplace ce nombre par le record
                String nouvelleLigne = ligne.replace(index, listeRecords.get(Integer.parseInt(index.trim())).toString());
                
                lignes[i] = nouvelleLigne;
            }
            
            contenuFinal += lignes[i] + "\n";
        }
     
        return contenuFinal;
    }
        
    /**
     * Méthode permettant de transformer une date au format "03/01/19" en
     * "1 mars 2019"
     *
     * @param dateBrute = la date au format "MM/dd/yy"
     * @return la date en français
     */
    private static String transformerDate(String dateBrute) {
        String dateEnFrancais = "";

        try {
            // définition du format que l'on souhaite pour la date
            SimpleDateFormat formater = new SimpleDateFormat("d MMMM yyyy");

            // création de la date en objet Date depuis le String
            Date date = new SimpleDateFormat("MM/dd/yy").parse(dateBrute);

            // formatage de la date
            dateEnFrancais = formater.format(date);

        } catch (ParseException e) {
            System.err.println("Erreur : " + e);
        }

        return dateEnFrancais;
    }
    
    /**
     * Méthode permettant de récupérer le nom de l'adversaire dans sa version
     * longue en français
     * (Amélioration possible avec HashMap)
     * 
     * @param adversaire
     * @return le nom de l'adversaire en français
     */
    private static String transformerAdversaire(String adversaire) {
        
        String[] nomsCourts = new String[]{"Nuggets", "Timberwolves", "Thunder",
            "Trail Blazers", "Jazz", "Warriors", "Clippers", "Lakers", "Suns",
            "Kings", "Mavericks", "Rockets", "Grizzlies", "Pelicans", "Spurs",
            "Celtics", "Nets", "Knicks", "Sixers", "Raptors", "Bulls", "Cavaliers",
            "Pistons", "Pacers", "Bucks", "Hawks", "Hornets", "Heat", "Magic", "Wizards"};
        
        String[] nomsLongs = new String[]{"Nuggets de Denver", "Timberwolves du Minnesota",
            "Thunder d'Oklahoma City", "Trail Blazers de Portland", "Jazz de l'Utah",
            "Warriors de Golden State", "Clippers de Los Angeles", "Lakers de Los Angeles",
            "Suns de Phoenix", "Kings de Sacramento", "Mavericks de Dallas", "Rockets de Houston",
            "Grizzlies de Memphis", "Pelicans de La Nouvelle-Orléans", "Spurs de San Antonio",
            "Celtics de Boston", "Nets de Brooklyn", "Knicks de New York", "76ers de Philadelphie",
            "Raptors de Toronto", "Bulls de Chicago", "Cavaliers de Cleveland", "Pistons de Détroit",
            "Pacers de l'Indiana", "Bucks de Milwaukee", "Hawks d'Atlanta", "Hornets de Charlotte",
            "Heat de Miami", "Magic d'Orlando", "Wizards de Washington"};
        
        for (int i = 0; i < nomsCourts.length ; ++i) {
            if (adversaire.equals(nomsCourts[i])) {
                adversaire = nomsLongs[i];
            }
        }

        return adversaire;
    }
}
