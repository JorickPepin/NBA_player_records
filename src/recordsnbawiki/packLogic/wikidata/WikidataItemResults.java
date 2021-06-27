package recordsnbawiki.packLogic.wikidata;

import java.util.List;

/**
 * Matches the results retrieved from the query to find the
 * list of items from a name
 * 
 * @author Jorick
 */
class WikidataItemResults {
    
    private final List<WikidataItem> search;
    
    public WikidataItemResults(List<WikidataItem> search) {
        this.search = search;
    }
    
    public List<WikidataItem> getSearch() {
        return this.search;
    }

}
