package recordsnbawiki;

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

    public String getValeur() {
        return valeur;
    }

    public String getAdversaire() {
        return adversaire;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return valeur + " contre " + adversaire + " le " + date;
    }  
}
