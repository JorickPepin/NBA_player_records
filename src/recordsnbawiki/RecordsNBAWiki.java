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
            StringBuilder sb = new StringBuilder(); // SB contenant le contenu

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
                    sb.append(ligne);
                    sb.append(System.lineSeparator());
                }     
            }
            
            // récupération du contenu du SB (le code HTML utile) pour le transtyper en String
            String html = sb.toString();
            
            Document document = Jsoup.parse(html);
                        
            // récupération du nombre d'éléments <td> (ceux qui nous intéressent)
            int size = document.select("td").size();
                    
            int i = 0;
            while (i < size) {
                // récupération des éléments <td>
                Element link = document.select("td").get(i);
                String texteAEcrire = link.text() + "\n";
                ecritureDansFichier(texteAEcrire, recuperationNomJoueur(titre));
                i++;
            }
            
        } catch (MalformedURLException e) {
            System.err.println("Erreur : " + e);
        } catch (IOException e) {
            System.err.println("Erreur : " + e);
        }
    }

    /**
     * Méthode permettant d'écrire le contenu dans un fichier .txt
     * @param contenu
     */
    private static void ecritureDansFichier(String contenu, String nom) {

        byte data[] = contenu.getBytes();
        
        // on crée ou écrit dans le fichier correspondant au nom du joueur
        Path fichier = Paths.get("fichiers/" + nom + ".txt");
      
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(fichier, CREATE, APPEND))) {
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
