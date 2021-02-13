package recordsnbawiki.packLogic.wikidata;

import java.util.List;

/**
 *
 * @author Jorick
 */
public class WikidataResult {
    
    private int success;
    private final List<WikidataItem> search;
    
    public WikidataResult(List<WikidataItem> search, int success) {
        this.search = search;
        this.success = success;
    }
    
    public List<WikidataItem> getSearch() {
        return this.search;
    }

    /**
     * 
     * @return 1 if the request is a success, 0 otherwise
     */
    public int isSuccess() {
        return success;
    }
}
