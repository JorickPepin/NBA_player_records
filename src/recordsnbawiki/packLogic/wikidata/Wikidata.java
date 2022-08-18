package recordsnbawiki.packLogic.wikidata;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import recordsnbawiki.utils.WikidataException;

/**
 * Contient les échanges avec l'API Wikidata
 * 
 * @author Jorick
 */
public class Wikidata {

    /**
     * Génère et retourne les joueurs de basket trouvés avec la recherche de
     * l'utilisateur
     * 
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
     * 
     * @param user_input - la recherche de l'utilisateur
     * @return la connexion
     * @throws WikidataException
     */
    private HttpURLConnection executeItemsQuery(String userInput) throws WikidataException {

        try {
            String query = "https://www.wikidata.org/w/api.php"
                    + "?action=wbsearchentities"
                    + "&format=json"
                    + "&search=" + URLEncoder.encode(userInput, "UTF-8")
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
     * 
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
     * 
     * @param items - les résultats de la requête
     * @return la liste des items représentant des joueurs de basket
     */
    private List<WikidataItem> sortItems(List<WikidataItem> items) {
        String[] BASKETBALL_MATCHES = { "basket-ball", "basketball", "basketteur",
                "basket", "baloncestista", "cestista", "basketballspieler" };

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

    /**
     * Charge l'élément Wikidata du joueur à partir de son identifiant unique afin
     * de récupérer les claims RealGM et ESPN ainsi que le nom du joueur sur la
     * Wikipédia FR.
     *
     * @param playerId
     * @return
     * @throws WikidataException
     */
    public Map<String, String> retrieveEntity(String playerId) throws WikidataException {
        String REALGM_PROPERTY = "P3957";
        String ESPN_PROPERTY = "P3685";

        try {
            String query = "https://www.wikidata.org/w/api.php"
                    + "?action=wbgetentities"
                    + "&format=json"
                    + "&ids=" + playerId;

            URL url = new URL(query);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            StringBuilder response = new StringBuilder();
            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                response.append(currentLine);
            }
            in.close();

            JsonElement root = JsonParser.parseString(response.toString());
            JsonObject entity = root.getAsJsonObject().get("entities").getAsJsonObject().get(playerId)
                    .getAsJsonObject();

            JsonObject claims = entity.get("claims").getAsJsonObject();

            if (!claims.has(REALGM_PROPERTY)) {
                throw new WikidataException("no RealGM ID");
            }

            if (!claims.has(ESPN_PROPERTY)) {
                throw new WikidataException("no ESPN ID");
            }

            String realgmId = claims.get(REALGM_PROPERTY).getAsJsonArray().get(0).getAsJsonObject()
                    .getAsJsonObject("mainsnak").getAsJsonObject("datavalue")
                    .getAsJsonPrimitive("value").getAsString();

            String espnId = claims.get(ESPN_PROPERTY).getAsJsonArray().get(0).getAsJsonObject()
                    .getAsJsonObject("mainsnak").getAsJsonObject("datavalue")
                    .getAsJsonPrimitive("value").getAsString();

            String name = entity.getAsJsonObject("sitelinks").getAsJsonObject("frwiki").get("title").getAsString();

            Map<String, String> data = new HashMap<String, String>();

            data.put("realgmId", realgmId);
            data.put("espnId", espnId);
            data.put("name", name.split(" \\(")[0]); // Franz Wagner (basket-ball) -> Franz Wagner

            return data;

        } catch (IOException e) {
            throw new WikidataException();
        }
    }
}
