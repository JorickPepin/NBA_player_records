package recordsnbawiki.packLogic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import recordsnbawiki.utils.RealGMException;

/**
 *
 * @author Jorick
 */
public class DataManagement {
    
    private String finalContent;
    
    public DataManagement() {
        
    }
    
    /**
     * Méthode permettant de récupérer le titre de la page sans balise HTML
     *
     * @param identifiant = l'identifiant REALGM du joueur
     * @return le titre de la page sans balise HTML
     */
    public String recuperationTitre(int identifiant) {

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
     * @throws RealGMException
     */
    public String recuperationContenuRealGM(int identifiant) throws RealGMException {

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
            
            // à améliorer : gérer le cas où un record est manquant et le remplacer par - || - || -
            if (i < 32) {
                throw new RealGMException();
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
    public ArrayList<Record> traitementContenu(String contenu) {
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
                  
                    // si le 4e mot n'est pas "on", cela signifie que l'équipe est composée de 2 mots (Trail Blazers)
                    if (!"on".equals(ligneSeparee[3])) {
                        if (ligne.contains("@")) {
                            adversaire = "@ " + transformerAdversaire(ligneSeparee[2] + " " + ligneSeparee[3]);
                        } else {
                            adversaire = transformerAdversaire(ligneSeparee[2] + " " + ligneSeparee[3]);
                        }

                    } else { // sinon le nom est juste composé du 3e mot (Mavericks, Clippers, ...)
                        if (ligne.contains("@")) {
                            adversaire = "@ " + transformerAdversaire(ligneSeparee[2]);
                        } else {
                            adversaire = transformerAdversaire(ligneSeparee[2]);
                        }
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
    /*private void ecritureDansFichier(String contenu, String nom) {

        byte data[] = contenu.getBytes();

        // on crée ou écrit dans le fichier correspondant au nom du joueur
        String file = "fichiers/" + nom + ".txt";
        
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(data, 0, data.length);
            
            System.out.println("Fichier créé avec succès.");
        } catch (IOException e) {
            System.err.println("Erreur : " + e);
        }
    }
*/
    
    /**
     * Méthode permettant de récupérer le nom et le prénom du joueur 
     * à partir du titre de la page
     *
     * @param titre = titre de la page
     * @return prénom_nom du joueur
     */
    private String recuperationNomJoueur(String titre) {
        
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
    public String preparationContenuRecords(ArrayList<Record> listeRecords) {
            
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
        
        ArrayList<String> elementsPresentsSR = new ArrayList<>(); // saison régulière
        ArrayList<String> elementsPresentsPL = new ArrayList<>(); // playoffs
        
        // pour chaque ligne du texte
        for (int i = 0; i < lignes.length; ++i) {
            String ligne = lignes[i];
               
            if (!ligne.startsWith("|-")) {
                // séparation de la ligne mot par mot
                String[] ligneSeparee = ligne.split(" ");
                
                // le record attendu à cette ligne est représenté par un nombre
                // et correspond au dernier mot de la ligne
                String index = ligneSeparee[ligneSeparee.length - 1];

                // on récupère l'adversaire et la date pour tester s'ils sont déjà présents dans le tableau
                try {
                    String adversaire = listeRecords.get(Integer.parseInt(index.trim())).getAdversaireSansArobase();
                    String date = listeRecords.get(Integer.parseInt(index.trim())).getDate();

                    if (!adversaire.contains("fois") && !adversaire.startsWith("-")) {

                        if (Integer.parseInt(index.trim()) < 16) { // < 16 : records en saison régulière

                            if (!elementsPresentsSR.contains(adversaire)) { // si c'est la première fois que cet adversaire apparait
                                elementsPresentsSR.add(adversaire); // ajout de l'adveraire aux éléments déjà présents

                                // on transforme l'écriture de l'adversaire pour qu'il apparaisse avec un lien interne [[nom_adversaire]]
                                listeRecords.get(Integer.parseInt(index.trim())).setAdversaire(creerAdversaireAvecLienInterne(listeRecords, index, adversaire));
                            }

                            if (!elementsPresentsSR.contains(date)) { // si c'est la première fois que cette date apparait
                                elementsPresentsSR.add(date); // ajout de la date aux éléments déjà présents

                                listeRecords.get(Integer.parseInt(index.trim())).setDate("{{date|" + listeRecords.get(Integer.parseInt(index.trim())).getDate() + "|en basket-ball}}");

                            } else { // date déjà présente
                                listeRecords.get(Integer.parseInt(index.trim())).setDate("{{date-|" + listeRecords.get(Integer.parseInt(index.trim())).getDate() + "}}");
                            }

                        } else { // >= 16 : records en playoffs

                            if (!elementsPresentsPL.contains(adversaire)) {
                                elementsPresentsPL.add(adversaire);

                                listeRecords.get(Integer.parseInt(index.trim())).setAdversaire(creerAdversaireAvecLienInterne(listeRecords, index, adversaire));
                            }

                            if (!elementsPresentsPL.contains(date)) {
                                elementsPresentsPL.add(date);

                                listeRecords.get(Integer.parseInt(index.trim())).setDate("{{date|" + listeRecords.get(Integer.parseInt(index.trim())).getDate() + "|en basket-ball}}");

                            } else {
                                listeRecords.get(Integer.parseInt(index.trim())).setDate("{{date-|" + listeRecords.get(Integer.parseInt(index.trim())).getDate() + "}}");
                            }
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    System.err.println("Le contenu de RealGM n'est pas conforme, le fichier n'a pas pu être créé.");
                    System.exit(0);
                }
                
                // on remplace l'index du record par celui-ci
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
    private String transformerDate(String dateBrute) {
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
    private String transformerAdversaire(String adversaire) {
        
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

    /**
     * Méthode permettant de transformer l'écriture d'un adversaire en brut 
     * en écriture avec un lien interne 
     * Cas général : (@) [[nom_adversaire]]
     * 
     * @param listeRecords
     * @param index
     * @param adversaire
     * @return (@) [[nom_adversaire]] ou (@) [[Hornets de Charlotte (NBA)|Hornets de Charlotte]]
     */
    private String creerAdversaireAvecLienInterne(ArrayList<Record> listeRecords, String index, String adversaire) {

        if (listeRecords.get(Integer.parseInt(index.trim())).getAdversaire().contains("@")) { // si c'est à l'extérieur, on met l'@

            if (adversaire.equals("Hornets de Charlotte")) { // cas particulier
                return "@ [[Hornets de Charlotte (NBA)|Hornets de Charlotte]]";
            } else {
                return "@ [[" + listeRecords.get(Integer.parseInt(index.trim())).getAdversaireSansArobase() + "]]";
            }

        } else {

            if (adversaire.equals("Hornets de Charlotte")) { // cas particulier
                return "[[Hornets de Charlotte (NBA)|Hornets de Charlotte]]";
            } else {
                return "[[" + listeRecords.get(Integer.parseInt(index.trim())).getAdversaireSansArobase() + "]]";
            }
        }
    }

    /**
     * Récupération du nombre de double-double et triple-double en saison
     * régulière et en playoffs sur espn.com
     * 
     * @param identifiant l'identifiant ESPN du joueur
     * @return 
     */
    public String[] recuperationContenuESPN(int identifiant) {

        String DD2_SR = "0";
        String TD3_SR = "0";
        
        // récupération du nombre de double-double et triple-double en saison régulière
        try {
            Document document = Jsoup.connect("https://www.espn.com/nba/player/stats/_/id/" + identifiant).get();
            
            // le nombre de double-double en saison régulière correspond au 36e élément <span class="fw-bold">
            DD2_SR = document.select("span.fw-bold").get(36).text();
            // le nombre de triple-double en saison régulière correspond au 37e élément <span class="fw-bold">
            TD3_SR = document.select("span.fw-bold").get(37).text();
            
        } catch (IOException e) {
            System.err.println("Erreur : " + e);
        }

        String DD2_PL = "0";
        String TD3_PL = "0";
        
        // récupération du nombre de double-double et triple-double en playoffs
        try {
            Document document = Jsoup.connect("https://www.espn.com/nba/player/stats/_/id/" + identifiant + "/type/nba/seasontype/3").get();
            
            // si le joueur n'a jamais disputé les playoffs, la page contient
            // un message "No available information." accessible depuis la classe
            // ".NoDataAvailable__Msg__Content"
            // si cette classe n'existe pas, les données sont accessibles
            if (document.select(".NoDataAvailable__Msg__Content").isEmpty()) {
                
                // le nombre de double-double en playoffs correspond au 36e élément <span class="fw-bold">
                DD2_PL = document.select("span.fw-bold").get(36).text();
                // le nombre de triple-double en playoffs correspond au 37e élément <span class="fw-bold">
                TD3_PL = document.select("span.fw-bold").get(37).text();
            }
            
        } catch (IOException e) {
            System.err.println("Erreur : " + e);
        }
              
        return new String[]{DD2_SR, TD3_SR, DD2_PL, TD3_PL};
    }
    
    /**
     * Mise en forme des valeurs récupérées sur ESPN
     * 
     * @param valeurs
     * @return le texte de fin de page avec les stats DD2, TD3 et la date de màj
     */
    public String preparationContenuDD2_TD3(String[] valeurs) {
        
        int nbDD2_SR;
        int nbTD3_SR;
        int nbDD2_PL;
        int nbTD3_PL;

        String ligne_DD2 = "* [[Double-double]] : ";
        String ligne_TD3 = "* [[Triple-double]] : ";

        try {
            nbDD2_SR = Integer.valueOf(valeurs[0]);
            nbTD3_SR = Integer.valueOf(valeurs[1]);

            nbDD2_PL = Integer.valueOf(valeurs[2]);
            nbTD3_PL = Integer.valueOf(valeurs[3]);

            int nbTotalDD2 = nbDD2_SR + nbDD2_PL;
            int nbTotalTD3 = nbTD3_SR + nbTD3_PL;

            ligne_DD2 += nbTotalDD2;
            ligne_TD3 += nbTotalTD3;

            if (nbDD2_PL > 0) { // s'il y a des DD2 en playoffs
                ligne_DD2 += " (dont " + nbDD2_PL + " en playoffs)";
            }

            if (nbTD3_PL > 0) { // s'il y a des TD3 en playoffs
                ligne_TD3 += " (dont " + nbTD3_PL + " en playoffs)";
            }
        } catch (NumberFormatException e) {
            System.err.println("Le contenu d'ESPN n'est pas conforme, le fichier n'a pas pu être créé.");
            System.exit(0);
        }

        return "|}\n" + ligne_DD2 + "\n" + ligne_TD3 + recuperationDateDuJour();
    }
    
    /**
     * Méthode retournant la date du jour au bon format
     * 
     * @return la date du jour
     */
    private String recuperationDateDuJour() {
        
        Date date = new Date();
        
        SimpleDateFormat formatter = new SimpleDateFormat("d MMMM yyyy");
        
        return "\n''Dernière mise à jour : {{date-|" + formatter.format(date) + "}}''";
    }

    public String getFinalContent() {
        return finalContent;
    }

    public void setFinalContent(String finalContent) {
        this.finalContent = finalContent;
    }
}
