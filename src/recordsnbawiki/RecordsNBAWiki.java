package recordsnbawiki;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
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
        recuperationContenu(24275);
    }

    /**
     * Méthode permettant de récupérer le code source de la page
     * @param identifiant = l'identifiant REALGM du joueur
     */
    private static void recuperationContenu(int identifiant) {

        try {
            // url de la page du joueur
            URL url = new URL("https://basketball.realgm.com/player/LeBron-James/Bests/" + identifiant);

            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder contenuBrut = new StringBuilder(); // SB contenant le contenu

            // booleen permettant de garder que le contenu désiré
            boolean contenuValide = false;
            
            String titre = "";
            String ligne; 
            while ((ligne = br.readLine()) != null) {

                // récupération du titre de la page pour avoir le nom du joueur
                if (ligne.startsWith("<title>")) {
                    titre = ligne;
                }
                
                // on détermine des "balises" pour récupérer que le contenu utile
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
            
            // récupération du contenu du SB (le code HTML utile) en String pour le traiter avec jsoup
            String html = contenuBrut.toString();       
            Document document = Jsoup.parse(html);
                        
            // récupération du nombre d'éléments <td> présents
            int size = document.select("td").size();
                           
            StringBuilder contenuTraite = new StringBuilder(); // SB contenant le contenu sans balise HTML (uniquement le texte)
            
            int i = 0;
            while (i < size) {
                // récupération des éléments <td>
                Element link = document.select("td").get(i);
              
                // ajout de l'élément <td> sans balise au SB
                contenuTraite.append(link.text()).append("\n");

                i++;
            }
            
            // une fois notre contenu récupéré, on l'inscrit dans le fichier au nom du joueur
            ecritureDansFichier(contenuTraite.toString(), recuperationNomJoueur(titre));
            
        } catch (MalformedURLException e) {
            System.err.println("Erreur : " + e);
        } catch (IOException e) {
            System.err.println("Erreur : " + e);
        }
    }

    /**
     * Méthode permettant d'écrire le contenu dans un fichier .txt
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
     * Méthode permettant de récupérer le nom et le prénom du joueur à partir
     * de la ligne <title> du code source
     * @param titre = la ligne <title> du code source
     * @return prénom_nom du joueur
     */
    private static String recuperationNomJoueur(String titre) {
        
        // récupération du titre sans la balise <title>
        Document docTitre = Jsoup.parse(titre);
        Element linkTitre = docTitre.select("title").first();
        
        String texte = linkTitre.text();
        
        String prenom = texte.split(" ")[0];
        String nom = texte.split(" ")[1];
            
        return prenom + "_" + nom;
    }
}
