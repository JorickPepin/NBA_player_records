package recordsnbawiki.packLogic;

import java.util.ArrayList;
import recordsnbawiki.packVue.Observer;
import recordsnbawiki.utils.ESPNException;
import recordsnbawiki.utils.RealGMException;

/**
 *
 * @author Jorick
 */
public class Controller implements Observable {

    /**
     * Observer list
     */
    private ArrayList<Observer> observateurs;

    /**
     * Model
     */
    private DataManagement dataManagement;

    public Controller(DataManagement dataManagement) {
        this.observateurs = new ArrayList<>();
        this.dataManagement = dataManagement;
    }

    /**
     * Generates content from the player's RealGM and ESPN identifier
     *
     * @param RealGM_id - the player's RealGM identifier
     * @param ESPN_id - the player's ESPN identifier
     * @param header - true if the header is needed, false otherwise
     * @throws RealGMException
     * @throws ESPNException
     */
    public void generateContent(int RealGM_id, int ESPN_id, boolean header) throws ESPNException, RealGMException {

        try {

            String RealGM_content = dataManagement.getRealGMContent(RealGM_id);
            String ESPN_content = dataManagement.getESPNContent(ESPN_id);
            String final_content;

            if (header) { // true signifie que l'en-tête doit être ajoutée
                String contenuEnTete = dataManagement.getHeader();
                final_content = contenuEnTete + RealGM_content + ESPN_content;
            } else {
                final_content = RealGM_content + ESPN_content;
            }

            dataManagement.setFinalContent(final_content);

        } catch (RealGMException e) {
            if ("ID issue".equals(e.getMessage())) {
                notifyObservateurs("errorNoPlayerRealGM");
            } else if ("never played in NBA".equals(e.getMessage())) {
                notifyObservateurs("errorNeverPlayedInNBARealGM");
            } else {
                notifyObservateurs("errorRealGM");
            }
        } catch (ESPNException e) {
            if ("ID issue".equals(e.getMessage())) {
                notifyObservateurs("errorNoPlayerESPN");
            } else {
                notifyObservateurs("errorESPN");
            }
        }
    }

    @Override
    public void notifyObservateurs(String code) {
        for (Observer obs : observateurs) {
            obs.update(code);
        }
    }

    @Override
    public void addObservateur(Observer obs) {
        observateurs.add(obs);
    }
}
