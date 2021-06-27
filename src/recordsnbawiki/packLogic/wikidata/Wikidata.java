package recordsnbawiki.packLogic.wikidata;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
 * Contient les échanges avec l'API Wikidata
 * 
 * @author Jorick
 */
public class Wikidata {
    
    /**
     * Génère et retourne les joueurs de basket trouvés avec la recherche de l'utilisateur
     * @param userInput - la recherche de l'utilisateur
     * @return une liste de joueurs
     * @throws WikidataException 
     */
    public List<WikidataItem> generatePlayers(String userInput) throws WikidataException {
        HttpURLConnection con = executeItemsQuery(userInput);
        List<WikidataItem> results = retrieveItems(con);
        List<WikidataItem> players = sortItems(results);
        
        return players;
    }
    
    /**
     * Se connecte à l'api wikidata
     * @param user_input - la recherche de l'utilisateur
     * @return la connexion
     * @throws WikidataException 
     */
    private HttpURLConnection executeItemsQuery(String userInput) throws WikidataException {
        
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
            throw new WikidataException();
        }
    }

    /**
     * Récupère les résultats de la recherche à partir de la connexion
     * @param connection
     * @return une liste contenant les résultats de la recherche
     * @throws WikidataException 
     */
    private List<WikidataItem> retrieveItems(HttpURLConnection connection) throws WikidataException {

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            
            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                response.append(currentLine);
            }

            in.close();
            
            Gson gson = new Gson();
            WikidataItemResults wikidataResult = gson.fromJson(response.toString(), WikidataItemResults.class);
            List<WikidataItem> results = wikidataResult.getSearch();
            
            return results;
            
        } catch (JsonSyntaxException | IOException e) {
            throw new WikidataException();
        }
    }
    
    /**
     * Trie les résultats pour ne garder que les joueurs de basket-ball
     * @param items - les résultats de la requête
     * @return la liste des items représentant des joueurs de basket
     */
    private List<WikidataItem> sortItems(List<WikidataItem> items) {
        String[] BASKETBALL_MATCHES = {"basket-ball", "basketball", "basketteur",
            "basket", "baloncestista", "cestista", "basketballspieler"};
        
        List<WikidataItem> players = new ArrayList<>();
        
        for (WikidataItem item : items) {
            
            if (item.getDescription() != null) {
                if (Arrays.stream(BASKETBALL_MATCHES).anyMatch(item.getDescription()
                        .toLowerCase()::contains)) {
                    players.add(item);
                }
            }
        }
       
        return players;
    }
    
    public String retrieveRealGMId(String playerId) throws WikidataException {
        
        String REALGM_PROPERTY = "P3957";
        
        try {
            // connection
            String query = "https://www.wikidata.org/w/api.php"
                    + "?action=wbgetclaims"
                    + "&format=json"
                    + "&entity=" + playerId 
                    + "&property=" + REALGM_PROPERTY;
            
            URL url = new URL(query);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            
            // response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            
            StringBuilder response = new StringBuilder();
            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                response.append(currentLine);
            }
            in.close();
            
            // content parsing
            JsonElement root = JsonParser.parseString(response.toString());
            String realgmId = root.getAsJsonObject().getAsJsonObject("claims")
                    .getAsJsonArray(REALGM_PROPERTY).get(0).getAsJsonObject()
                    .getAsJsonObject("mainsnak").getAsJsonObject("datavalue")
                    .getAsJsonPrimitive("value").getAsString();
            
            return realgmId;
            
        } catch (IOException e) {
            throw new WikidataException();
        } catch (NullPointerException e) {
            System.err.println("ce joueur n'a pas d'id espn");
            throw new WikidataException();
        }
    }
    
    public String retrieveESPNId(String playerId) throws WikidataException {
        
        String ESPN_PROPERTY = "P3685";
        
        try {
            // connection
            String query = "https://www.wikidata.org/w/api.php"
                    + "?action=wbgetclaims"
                    + "&format=json"
                    + "&entity=" + playerId 
                    + "&property=" + ESPN_PROPERTY;
            
            URL url = new URL(query);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            
            // response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            
            StringBuilder response = new StringBuilder();
            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                response.append(currentLine);
            }
            in.close();
            
            // content parsing
            JsonElement root = JsonParser.parseString(response.toString());
            String espnId = root.getAsJsonObject().getAsJsonObject("claims")
                    .getAsJsonArray(ESPN_PROPERTY).get(0).getAsJsonObject()
                    .getAsJsonObject("mainsnak").getAsJsonObject("datavalue")
                    .getAsJsonPrimitive("value").getAsString();
            
            return espnId;
            
        } catch (IOException e) {
            throw new WikidataException();
        } catch (NullPointerException e) {
            System.err.println("ce joueur n'a pas d'id espn");
            throw new WikidataException();
        }
    }
    
}