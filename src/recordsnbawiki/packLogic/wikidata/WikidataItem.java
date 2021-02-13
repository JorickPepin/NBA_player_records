package recordsnbawiki.packLogic.wikidata;

/**
 * 
 * @author Jorick
 */
public class WikidataItem {
    
    private String id;
  
    private String label;
    
    private String description;

    public WikidataItem(String id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
    
    @Override
    public String toString() {
        return "WikidataPlayer{" + "id=" + id + ", label=" + label + ", description=" + description + '}';
    }
}
