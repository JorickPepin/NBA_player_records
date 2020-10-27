package recordsnbawiki.packLogic;

import java.util.ArrayList;
import recordsnbawiki.packVue.Observateur;

/**
 *
 * @author Jorick
 */
public class Controller implements Observable {

    private ArrayList<Observateur> observateurs = new ArrayList<>();
    
    @Override
    public void notifyObservateurs(String code) {
        for (Observateur obs : observateurs) {
            obs.update(code);
        }
    }

    @Override
    public void addObservateur(Observateur obs) {
        observateurs.add(obs);
    }
    
}
