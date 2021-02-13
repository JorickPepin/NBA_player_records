package recordsnbawiki.packLogic.wikidata;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import recordsnbawiki.utils.WikidataException;

/**
 *
 * @author Jorick
 */
public class Wikidata {
    
    public void generateContent(String userInput) throws WikidataException {
        HttpURLConnection con = connection(userInput);
        List<WikidataItem> results = retrieveResults(con);
        List<WikidataItem> players = sortResults(results);
    }
    
    /**
     * Se connecter à l'api wikidata
     * @param user_input - la recherche de l'utilisateur
     * @return la connexion
     * @throws WikidataException 
     */
    private HttpURLConnection connection(String userInput) throws WikidataException {
        
        try {             
            String query = "https://www.wikidata.org/w/api.php"
                    + "?action=wbsearchentities"
                    + "&format=json"
                    + "&search=" + userInput.replace(" ", "%20")
                    + "&language=fr"
                    + "&limit=50"
                    + "&uselang=fr";
            
            URL url = new URL(query);
   
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            
            return con;
            
        } catch (IOException e) {
            System.err.println(e);
            throw new WikidataException();
        }
    }

    /**
     * Récupère les résultats de la recherche à partir de la connexion
     * @param connection
     * @return une liste contenant les résultats de la recherche
     * @throws WikidataException 
     */
    private List<WikidataItem> retrieveResults(HttpURLConnection connection) throws WikidataException {

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            
            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                response.append(currentLine);
            }

            in.close();
            
            Gson gson = new Gson();
            WikidataResult wikidataResult = gson.fromJson(response.toString(), WikidataResult.class);
            List<WikidataItem> results = wikidataResult.getSearch();
            
            return results;
            
        } catch (JsonSyntaxException | IOException e) {
            System.err.println(e);
            throw new WikidataException();
        }
    }
    
    /**
     * Trie les résultats pour ne garder que les joueurs de basket-ball
     * @param items - les résultats de la requête
     * @return la liste des items représentant des joueurs de basket
     */
    private List<WikidataItem> sortResults(List<WikidataItem> items) {
        String[] BASKETBALL_MATCHES = {"basket-ball", "basketball", "basketteur", "basket", "baloncestista", "cestista", "basketballspieler"};
        
        List<WikidataItem> players = new ArrayList<>();
        
        for (WikidataItem item : items) {
            
            if (item.getDescription() != null) {
                if (Arrays.stream(BASKETBALL_MATCHES).anyMatch(item.getDescription().toLowerCase()::contains)) {
                    players.add(item);
                }
            }
        }
       
        return players;
    }
}
