package recordsnbawiki;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
        recuperationContenu(250);
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

            while ((line = br.readLine()) != null) {

                sb.append(line);
                sb.append(System.lineSeparator());

            }

            System.out.println(sb);

        } catch (MalformedURLException e) {
            System.out.println(e);
        } catch (IOException ex) {
            Logger.getLogger(RecordsNBAWiki.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
