package recordsnbawiki.packLogic;

/**
 *
 * @author Jorick
 */
public class Record {
    
    private String nom;
    
    private String valeur;
    
    private String adversaire;
    
    private String date;
    
    public Record(String nom, String valeur, String adversaire, String date) {
        this.nom = nom;
        this.valeur = valeur;
        this.adversaire = adversaire;
        this.date = date;
    }
    
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
        if (("").equals(date)) {
            return valeur + " || colspan=\"2\"| " + adversaire + "\n";
        } else {
            return valeur + " || " + adversaire + " || " + date + "\n";
        }
    }
}
