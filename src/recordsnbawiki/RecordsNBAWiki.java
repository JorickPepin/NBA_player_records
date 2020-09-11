package recordsnbawiki;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            URL url = new URL("https://basketball.realgm.com/player/LeBron-James/Bests/" + identifiant);

            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;

            StringBuilder sb = new StringBuilder();

            boolean contenuValide = false;
            
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
                }       
            }

            System.out.println(sb);

        } catch (MalformedURLException e) {
            System.out.println(e);
        } catch (IOException ex) {
            Logger.getLogger(RecordsNBAWiki.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
