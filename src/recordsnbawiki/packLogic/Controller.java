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

    private ArrayList<Observer> observateurs;
    
    private DataManagement dataManagement;
    
    public Controller(DataManagement dataManagement) {
        this.observateurs = new ArrayList<>();
        this.dataManagement = dataManagement;
    }     
 
    /**
     * Génère le fichier texte contenant les records à partir de l'identifiant 
     * RealGM et ESPN du joueur
     * 
     * @param identifiantRealGM - l'identifiant RealGM du joueur
     * @param identifiantESPN - l'identifiant ESPN du joueur
     * @throws RealGMException
     * @throws ESPNException
     */
    public void generateContent(int identifiantRealGM, int identifiantESPN) throws ESPNException, RealGMException {
   
        try {
            String contenuFinalRealGM = dataManagement.getRealGMContent(identifiantRealGM);
            String contenuFinalESPN = dataManagement.getESPNContent(identifiantESPN);

            String contenuFinal = contenuFinalRealGM + contenuFinalESPN;
            dataManagement.setFinalContent(contenuFinal);
            
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

    public void setDataManagement(DataManagement dataManagement) {
        this.dataManagement = dataManagement;
    }
}
