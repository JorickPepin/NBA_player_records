package recordsnbawiki.packLogic;

import recordsnbawiki.packLogic.json.JsonManagement;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import recordsnbawiki.packLogic.wikidata.Wikidata;
import recordsnbawiki.packLogic.wikidata.WikidataItem;
import recordsnbawiki.packVue.Window;
import recordsnbawiki.utils.ESPNException;
import recordsnbawiki.utils.RealGMException;
import recordsnbawiki.utils.WikidataException;

/**
 *
 * @author Jorick
 */
public class Controller {

    private Window view;

    private RealGM realGM;
    private ESPN espn;
    private Wikidata wikidata;
    private Header header;

    /** Store the final content */
    private String content;

    private List<WikidataItem> players;

    public Controller() {
        this.view = new Window(this);

        this.realGM = new RealGM();
        this.espn = new ESPN();
        this.wikidata = new Wikidata();
    }

    /**
     * Generates content from the player's RealGM and ESPN identifier
     *
     * @param player - the player chosen by the user
     * @param headerRequired - true if the header is needed, false otherwise
     * @throws RealGMException
     * @throws ESPNException
     */
    public void generateContent(WikidataItem player, boolean headerRequired) throws ESPNException, RealGMException {

        try {
            realGM.setJson(new JsonManagement());

            String realgmId = wikidata.retrieveRealGMId(player.getId());
            String espnId = wikidata.retrieveESPNId(player.getId());

            String realgmContent = realGM.genererContenu(realgmId);
            String espnContent = espn.genererContenu(espnId);

            header = new Header(realGM, espn, player.getLabel());

            String finalContent;

            if (headerRequired) {
                finalContent = header.getContenu() + realgmContent + espnContent;
            } else {
                finalContent = realgmContent + espnContent;
            }

            this.content = finalContent;

            if (!namesAreCompatible()) { // display warning message if the two names are not identical
                view.update("names incompatibility");
            }

            if (espn.necessiteWarning()) {
                view.update("warningESPN");
            }

        } catch (RealGMException e) {
            if (null == e.getMessage()) {
                view.update("errorRealGM");
            } else {
                switch (e.getMessage()) {
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
            }
        } catch (ESPNException e) {
            if (null == e.getMessage()) {
                view.update("errorESPN");
            } else {
                switch (e.getMessage()) {
                    case "ID issue":
                        view.update("errorNoPlayerESPN");
                        break;
                    default:
                        view.update("errorESPN");
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            if (null == e.getMessage()) {
                view.update("fileIssue");
            } else {
                switch (e.getMessage()) {
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
            }
        } catch (IOException e) {
            view.update("fileIssue");
        } catch (WikidataException e) {
            if (null == e.getMessage()) {
                view.update("errorWikidata");
            } else {
                switch (e.getMessage()) {
                    case "no ESPN ID":
                        view.update("errorNoESPNId");
                        break;
                    case "no RealGM ID":
                        view.update("errorNoRealGMId");
                        break;
                    default:
                        view.update("errorWikidata");
                        break;
                }
            }
        }
    }

    public void retrievePlayers(String userInput) {

        try {
            players = wikidata.generatePlayers(userInput);
        } catch (WikidataException ex) {
            view.update("wikidataIssue");
        }
    }

    /**
     * Tests if the two recovered names are identical
     *
     * @return true if the two names are identical, false otherwise
     */
    private boolean namesAreCompatible() {

        String realgmName = realGM.getNomJoueur();
        String espnName = espn.getNomJoueur();

        String realgmNameFormatted = "";
        String espnNameFormatted = "";

        for (char c : realgmName.toCharArray()) {
            if (Character.isLetter(c) || c == ' ') { // keep only letters and spaces to test without punctuation
                realgmNameFormatted += c;
            }
        }

        for (char c : espnName.toCharArray()) {
            if (Character.isLetter(c) || c == ' ') {
                espnNameFormatted += c;
            }
        }

        return realgmNameFormatted.equalsIgnoreCase(espnNameFormatted);
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

    public void removeHeader() {
        content = content.replace(header.getContenu(), "");
    }

    public void addHeader() {
        content = header.getContenu() + content;
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

    public List<WikidataItem> getPlayers() {
        return players;
    }
}
