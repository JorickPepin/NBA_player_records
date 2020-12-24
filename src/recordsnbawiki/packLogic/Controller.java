package recordsnbawiki.packLogic;

import recordsnbawiki.packLogic.json.JsonManagement;
import java.io.FileNotFoundException;
import java.io.IOException;
import recordsnbawiki.packVue.Window;
import recordsnbawiki.utils.ESPNException;
import recordsnbawiki.utils.RealGMException;

/**
 *
 * @author Jorick
 */
public class Controller {
 
    private Window view;
    
    private RealGM realGM;
    private ESPN espn;
    
    /*** Store the final content */
    private String content;
    
    public Controller() {
        this.view = new Window(this);
        
        this.realGM = new RealGM();
        this.espn = new ESPN();          
    }

    /**
     * Generates content from the player's RealGM and ESPN identifier
     *
     * @param RealGM_id - the player's RealGM identifier
     * @param ESPN_id - the player's ESPN identifier
     * @param header_required - true if the header is needed, false otherwise
     * @throws RealGMException
     * @throws ESPNException
     */
    public void generateContent(int RealGM_id, int ESPN_id, boolean header_required) throws ESPNException, RealGMException {
        
        try {
            
            realGM.setJson(new JsonManagement());
            
            String RealGM_content = realGM.genererContenu(RealGM_id);
            String ESPN_content = espn.genererContenu(ESPN_id);
            
            String final_content;
            
            if (header_required) {
                Header header = new Header(realGM, espn);
                final_content = header.getContenu() + RealGM_content + ESPN_content;
            } else {
                final_content = RealGM_content + ESPN_content;
            }
            
            this.content = final_content;
            
            if (!namesAreCompatible()) { // display warning message if the two names are not identical
                view.update("names incompatibility");
            }
            
            if (espn.necessiteWarning()) {
                view.update("warningESPN");
            }
            
        } catch (RealGMException e) {
            if (null == e.getMessage()) {
                view.update("errorRealGM");
            } else switch (e.getMessage()) {
                case "ID issue":
                    view.update("errorNoPlayerRealGM");
                    break;
                case "never played in NBA":
                    view.update("errorNeverPlayedInNBARealGM");
                    break;
                default:
                    view.update("errorRealGM");
                    break;
            }
        } catch (ESPNException e) {
            if (null == e.getMessage()) {
                view.update("errorESPN");
            } else switch (e.getMessage()) {
                case "ID issue":
                    view.update("errorNoPlayerESPN");
                    break;
                default:
                    view.update("errorESPN");
                    break;
            }
        } catch (FileNotFoundException e) {
            if (null == e.getMessage()) {
                view.update("fileIssue");
            } else switch (e.getMessage()) {
                case "teams.json":
                    view.update("teams.jsonIssue");
                    break;
                case "stats.json":
                    view.update("stats.jsonIssue");
                    break;
                case "header_playoffs.txt":
                    view.update("header_playoffs.txtIssue");
                    break;
                case "header_noplayoffs.txt":
                    view.update("header_noplayoffs.txtIssue");
                    break;
            }
        } catch (IOException e) {
            view.update("fileIssue");
        }
    }

    /**
     * Tests if the two recovered names are identical
     * @return true if the two names are identical, false otherwise
     */
    private boolean namesAreCompatible() {
     
        String RealGM_name = realGM.getNomJoueur();
        String ESPN_name = espn.getNomJoueur();
        
        String RealGM_name_formatted = "";
        String ESPN_name_formatted = "";
        
        for (char c : RealGM_name.toCharArray()) {
            if (Character.isLetter(c) || c == ' ') { // keep only letters and spaces to test without punctuation
                RealGM_name_formatted += c;
            }
        }
        
        for (char c : ESPN_name.toCharArray()) {
            if (Character.isLetter(c) || c == ' ') {
                ESPN_name_formatted += c;
            }
        }
          
        return RealGM_name_formatted.equalsIgnoreCase(ESPN_name_formatted);
    }

    /**
     * Remove ESPN content and update date in the final content
     */
    public void removeESPNContent() {
        
        int truncateIndex = content.length();

        for (int i = 0; i < 3; i++) {
            truncateIndex = content.lastIndexOf('\n', truncateIndex - 1);
        }

        content = content.substring(0, truncateIndex); // remove the 3 last lines
        
        content = content.replaceAll("\\{\\{,\\}\\}<ref>.*<\\/ref>", ""); // remove the ESPN ref
    }
    
    public String getRealGMPlayerName() {
        return realGM.getNomJoueur();
    }
    
    public String getESPNPlayerName() {
        return espn.getNomJoueur();
    }
    
    public String getContent() {
        return content;
    }
}
