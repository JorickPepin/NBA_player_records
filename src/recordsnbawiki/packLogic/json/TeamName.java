package recordsnbawiki.packLogic.json;

/**
 * 
 * @author Jorick
 */
public class TeamName {
    private String shortName;
    private String longName;

    public TeamName(String shortName, String longName) {
        this.shortName = shortName;
        this.longName = longName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }
}
