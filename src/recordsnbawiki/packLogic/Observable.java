package recordsnbawiki.packLogic;

import recordsnbawiki.packVue.Observateur;

/**
 *
 * @author Jorick
 */
public interface Observable {
    public void addObservateur(Observateur obs);
    public void notifyObservateurs(String code);
}
