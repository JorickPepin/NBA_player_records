package recordsnbawiki.packLogic;

/**
 * Représente un record
 * 
 * @author Jorick
 */
public class Record {

    /**
     * Contient le nom du record (points marqués, balles perdues, etc.)
     */
    private String nom;

    /**
     * Contient la valeur du record (= nb de points marqués, nb de balles perdues, etc.)
     */
    private String valeur;

    /**
     * Contient l'équipe contre qui le record a été effectué ou le nombre de fois qu'il a
     * été effectué si effectué plusieurs fois
     */
    private String adversaire;

    /**
     * Contient la data à laquelle a été effectué le record ou est vide si celui-ci
     * a été effectué plusieurs fois
     */
    private String date;

    public Record() {
        nom = valeur = adversaire = date = "";
    }
    
    public Record(String nom, String valeur, String adversaire, String date) {
        this.nom = nom;
        this.valeur = valeur;
        this.adversaire = adversaire;
        this.date = date;
    }

    /**
     * Retourne l'adversaire sans l'arobase dans le cas d'un record à l'extérieur
     * @return 
     */
    public String getAdversaireSansArobase() {

        if (adversaire.charAt(0) == '@') {
            return adversaire.substring(2);
        }

        return adversaire;
    }

    public String getNom() {
        return nom;
    }

    public void setAdversaire(String adversaire) {
        this.adversaire = adversaire;
    }

    public String getAdversaire() {
        return adversaire;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        if (date.isEmpty()) { // record réalisé plusieurs fois par le joueur
            return valeur + " || colspan=\"2\"| " + adversaire + "\n";
        } else {
            return valeur + " || " + adversaire + " || " + date + "\n";
        }
    }
}
