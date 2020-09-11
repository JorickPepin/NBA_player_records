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
import java.util.logging.Level;
import java.util.logging.Logger;
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
        recuperationContenu(54148);
    }

    /**
     * Méthode permettant de récupérer le code source de la page
     * 
     * @param identifiant = l'identifiant du joueur
     */
    private static void recuperationContenu(int identifiant) {

        try {
            // url de la page du joueur
            URL url = new URL("https://basketball.realgm.com/player/LeBron-James/Bests/" + identifiant);

            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder(); // SB contenant le contenu

            // booleen permettant de garder que le contenu désiré
            boolean contenuValide = false;
            
            String line; 
            while ((line = br.readLine()) != null) {

                // on détermine des "balises" pour récupérer que le contenu utile
                if (line.equals("<h2>NBA Regular Season Career Highs</h2>")) {
                    contenuValide = true;
                } else if (line.equals("<p class=\"footnote\">* NBA stats from 1946-1947</p><h2>NBA Career Comparison</h2>")) {
                    contenuValide = false;
                }
                
                if (contenuValide) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    //ecritureDansFichier(line + "\n");
                }     
                
            }

            // on récupère le contenu du SB (le code HTML utile) pour le transtyper en String
            String html = sb.toString();
            
            Document document = Jsoup.parse(html);
                        
            // on récupère le nombre d'éléments <td> (ceux qui nous intéressent)
            int size = document.select("td").size();
                    
            int i = 0;
            while (i < size) {
                // on affiche les éléments <td>
                Element link = document.select("td").get(i);
                System.out.println("Text: " + link.text());
                
                i++;
            }
            
        } catch (MalformedURLException e) {
            System.out.println(e);
        } catch (IOException ex) {
            Logger.getLogger(RecordsNBAWiki.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Méthode permettant d'écrire le contenu dans un fichier .txt
     * @param contenu = ce qu'on veut écrire
     */
    private static void ecritureDansFichier(String contenu) {

        byte data[] = contenu.getBytes();
        
        Path fichier = Paths.get("test.txt");
      
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(fichier, CREATE, APPEND))) {
            out.write(data, 0, data.length);
        } catch (IOException x) {
            System.err.println(x);
        }
    }
}
