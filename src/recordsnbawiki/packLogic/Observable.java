package recordsnbawiki.packLogic;

import recordsnbawiki.packVue.Observer;

/**
 *
 * @author Jorick
 */
public interface Observable {
    public void addObservateur(Observer obs);
    public void notifyObservateurs(String code);
}
