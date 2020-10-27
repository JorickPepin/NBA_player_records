package recordsnbawiki.packLogic;

/**
 * Classe représentant un record
 * Un record est caractérisé par une valeur, un adversaire et une date
 * 
 * @author Jorick
 */
public class Record {

    private String valeur;
    private String adversaire;
    private String date;

    public Record(String valeur, String adversaire, String date) {
        this.valeur = valeur;
        this.adversaire = adversaire;
        this.date = date;
    }

    public String getAdversaireSansArobase() {
        
        String adversaireSansArobase = this.adversaire;
        
        if (adversaire.contains("@")) {
            String[] s = adversaire.split("@ ");
            
            adversaireSansArobase = "";
            for (int i = 1; i < s.length; ++i) {
                adversaireSansArobase += s[i];
            }
        }
        
        return adversaireSansArobase;
    }

    public String getAdversaire() {
        return adversaire;
    }
 
    public String getDate() {
        return date;
    }
 
    public void setAdversaire(String adversaire) {
        this.adversaire = adversaire;
    }

    public void setDate(String date) {
        this.date = date;
    }
      
    @Override
    public String toString() {      
        if (("").equals(date))
            return valeur + " || colspan=\"2\"| " + adversaire;
        else
            return valeur + " || " + adversaire + " || " + date;
    }  
}
