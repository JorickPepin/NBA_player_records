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

    @Override
    public String toString() {      
        if (("").equals(date))
            return valeur + " || colspan=\"2\"| " + adversaire;
        else
            return valeur + " || " + adversaire + " || " + date;
    }  
}
