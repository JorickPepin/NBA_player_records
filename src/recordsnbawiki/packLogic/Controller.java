package recordsnbawiki.packLogic;

import java.util.ArrayList;
import recordsnbawiki.packVue.Observer;

/**
 *
 * @author Jorick
 */
public class Controller implements Observable {

    private ArrayList<Observer> observateurs;
    
    public Controller() {
        this.observateurs = new ArrayList<>();
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
