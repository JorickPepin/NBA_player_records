package recordsnbawiki.packLogic;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import recordsnbawiki.packVue.Observer;
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
     * Méthode permettant d'obtenir le fichier texte contenant les records 
     * mis en forme dans le dossier "fichiers" à partir de l'identifiant REALGM du joueur
     * 
     * @param identifiantRealGM = l'identifiant RealGM du joueur
     * @param identifiantESPN = l'identifiant ESPN du joueur
     */
    public void obtenirFichierTexte(int identifiantRealGM, int identifiantESPN) {

        // récupération du contenu brut du code source de RealGM
        String contenuRecords;        
        try {
            contenuRecords = dataManagement.recuperationContenuRealGM(identifiantRealGM);

            // récupération du titre de la page
            String titre = dataManagement.recuperationTitre(identifiantRealGM);

            // traitement du contenu pour obtenir la liste des records
            ArrayList<Record> listeRecords = dataManagement.traitementContenu(contenuRecords);

            // ajout des informations des records au template
            contenuRecords = dataManagement.preparationContenuRecords(listeRecords);

            // récupération du contenu du code source d'ESPN
            String[] valeursDD2_TD3 = dataManagement.recuperationContenuESPN(identifiantESPN);

            // ajout des informations des double-doubles et triple-doubles au template
            String contenuDD2_TD3 = dataManagement.preparationContenuDD2_TD3(valeursDD2_TD3);

            // le contenu final correspond au contenu sur les DD2 et TD3 ajouté à celui sur les records
            String contenuFinal = contenuRecords + contenuDD2_TD3;
            
            dataManagement.setFinalContent(contenuFinal);            
            
        } catch (RealGMException ex) {
            notifyObservateurs("errorRealGM");
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
