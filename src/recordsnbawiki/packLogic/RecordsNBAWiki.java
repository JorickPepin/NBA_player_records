package recordsnbawiki.packLogic;

import recordsnbawiki.packLogic.wikidata.Wikidata;
import recordsnbawiki.utils.WikidataException;

/**
 *
 * @author Jorick
 */
public class RecordsNBAWiki {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws WikidataException {
         
        //Controller controller = new Controller();
        
        Wikidata wd = new Wikidata();
        wd.generateContent("Mike James");
    }

}
