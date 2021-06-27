package recordsnbawiki.packLogic.wikidata;

import java.util.List;

/**
 * Matches the results retrieved from the query to find the
 * list of items from a name
 * 
 * @author Jorick
 */
class WikidataItemResults {
    
    private int success;
    private final List<WikidataItem> search;
    
    public WikidataItemResults(List<WikidataItem> search, int success) {
        this.search = search;
        this.success = success;
    }
    
    public List<WikidataItem> getSearch() {
        return this.search;
    }

    /**
     * @return 1 if the request is a success, 0 otherwise
     */
    public int isSuccess() {
        return success;
    }
}

/**
 * Matches the results retrieved from the query to find
 * a claim of an item
 * 
 * @author Jorick
 */
class WikidataClaimResults {
    
    private String id;
    
    public WikidataClaimResults(String test) {
        this.id = test;
    }

    public String getTest() {
        return id;
    }
    
    
}