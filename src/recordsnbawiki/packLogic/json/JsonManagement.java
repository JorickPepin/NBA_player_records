package recordsnbawiki.packLogic.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jorick
 */
public class JsonManagement {

    private final Gson gson;
    
    /** contains team names */
    private Map<String, String> teamNames_short_to_long;
    
    /** contains stat names */
    private Map<String, String> statNames_EN_to_FR;
    
    public JsonManagement() throws FileNotFoundException {
        gson = new Gson();
        
        teamNames_short_to_long = new HashMap<>();
        statNames_EN_to_FR = new LinkedHashMap<>(); // linkedhashmap to keep the same order as in the file
        
        retrieveTeamNames();
        retrieveStatNames();
    }
    
    /**
     * Retrieve the names of the teams from teams.json
     * 
     * @throws FileNotFoundException 
     */
    private void retrieveTeamNames() throws FileNotFoundException {
        try {
            JsonReader reader = new JsonReader(new FileReader("data/teams.json"));
        
            Type teamNameCollectionType = new TypeToken<List<TeamName>>(){}.getType();
            List<TeamName> teamNames = gson.fromJson(reader, teamNameCollectionType);

            teamNames.stream().forEach(n -> teamNames_short_to_long.put(n.getShortName(), n.getLongName())); 
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("teams.json");
        }
    }
    
    /**
     * Retrieve the names of the statistics from stats.json
     * 
     * @throws FileNotFoundException 
     */
    
    private void retrieveStatNames() throws FileNotFoundException {
        try {
            JsonReader reader = new JsonReader(new FileReader("data/stats.json"));
        
            Type statNameCollectionType = new TypeToken<List<StatName>>(){}.getType();
            List<StatName> statNames = gson.fromJson(reader, statNameCollectionType);

            statNames.stream().forEach(n -> statNames_EN_to_FR.put(n.getEn(), n.getFr())); 
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("stats.json");
        }
    }

    public Map<String, String> getTeamNames_short_to_long() {
        return teamNames_short_to_long;
    }

    public Map<String, String> getStatNames_EN_to_FR() {
        return statNames_EN_to_FR;
    }
}
