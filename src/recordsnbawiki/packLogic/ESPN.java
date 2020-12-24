package recordsnbawiki.packLogic;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import recordsnbawiki.utils.ESPNException;

/**
 *
 * @author Jorick
 */
public class ESPN {
    
    /** Titre de la page web ESPN (permet d'avoir le nom du joueur) */
    private String titre;
    
    /** URL de la page web ESPN */
    private String url;
    
    private boolean warning;
    
    /**
     * Génère et retourne le contenu des DD2 et TD3
     * 
     * @param id - l'identifiant ESPN du joueur
     * @return le contenu mis en forme
     * @throws ESPNException 
     */
    public String genererContenu(int id) throws ESPNException {
        this.url = "https://www.espn.com/nba/player/stats/_/id/" + id;
        this.warning = false;
        
        String[] contenuBrut = recuperationContenu();
        String contenuFinal = miseEnFormeContenu(contenuBrut);
        
        return contenuFinal;
    }
    
    /**
     * Récupération du nombre de double-double et triple-double en saison
     * régulière et en playoffs sur espn.com
     *
     * @param identifiant l'identifiant ESPN du joueur
     * @return
     * @throws recordsnbawiki.utils.NoPlayerESPNException
     * @throws java.io.IOException
     */
    private String[] recuperationContenu() throws ESPNException {

        String DD2_SR = "0";
        String TD3_SR = "0";

        // récupération du nombre de double-double et triple-double en saison régulière
        try {
            Document document = Jsoup.connect(url).get();
                    
            this.warning = necessiteWarning(document);
            
            // récupération du titre de la page
            titre = document.title();
            
            // le nombre de double-double en saison régulière correspond au 36e élément <span class="fw-bold">
            DD2_SR = document.select("span.fw-bold").get(36).text();
            // le nombre de triple-double en saison régulière correspond au 37e élément <span class="fw-bold">
            TD3_SR = document.select("span.fw-bold").get(37).text();

        } catch (HttpStatusException e) {
            throw new ESPNException("ID issue");
        } catch (IOException | IndexOutOfBoundsException e) {
            throw new ESPNException();
        }

        String DD2_PL = "0";
        String TD3_PL = "0";

        // récupération du nombre de double-double et triple-double en playoffs
        try {
            Document document = Jsoup.connect(url + "/type/nba/seasontype/3").get();

            // si le joueur n'a jamais disputé les playoffs, la page contient
            // un message "No available information." accessible depuis la classe
            // ".NoDataAvailable__Msg__Content"
            // si cette classe n'existe pas, les données sont accessibles
            if (document.select(".NoDataAvailable__Msg__Content").isEmpty()) {
                
                // le tableau contenant les DD2 et TD3 est présent
                if (document.text().contains("Postseason Misc Totals")) {
                    // le nombre de double-double en playoffs correspond au 36e élément <span class="fw-bold">
                    DD2_PL = document.select("span.fw-bold").get(36).text();
                    // le nombre de triple-double en playoffs correspond au 37e élément <span class="fw-bold">
                    TD3_PL = document.select("span.fw-bold").get(37).text();
                }
            }

        } catch (HttpStatusException e) {
            throw new ESPNException("ID issue");
        } catch (IOException | IndexOutOfBoundsException e) {
            throw new ESPNException();
        }

        return new String[]{DD2_SR, TD3_SR, DD2_PL, TD3_PL};
    }

    /**
     * Retourne true si le joueur a joué des matchs avant la saison 1993-1994
     * (ESPN n'affiche pas les DD2 et TD3 avant cette saison-là)
     * 
     * @return true si le joueur a joué des matchs avant 1993-1994, false sinon
     */
    private boolean necessiteWarning(Document document) {
        
        Pattern p = Pattern.compile("Regular Season Averages season Team ([^\\s]+)"); // récupère la première saison du joueur
        Matcher m = p.matcher(document.text());
        
        boolean res = false;
        
        if (m.find()) {
            String premiereSaison = m.group(1);
            if (!premiereSaison.contains("-")) { // il y a un tiret à partir de la saison 93-94
                res = true;
            }
        }

        return res;
    }
    
    /**
     * Mise en forme des valeurs récupérées sur ESPN
     *
     * @param valeurs
     * @return le texte de fin de page avec les stats DD2, TD3 et la date de màj
     */
    private String miseEnFormeContenu(String[] valeurs) throws ESPNException {

        int nbDD2_SR;
        int nbTD3_SR;
        int nbDD2_PL;
        int nbTD3_PL;

        String ligne_DD2 = "* [[Double-double]] : ";
        String ligne_TD3 = "* [[Triple-double]] : ";

        try {
            nbDD2_SR = Integer.valueOf(valeurs[0]);
            nbTD3_SR = Integer.valueOf(valeurs[1]);

            nbDD2_PL = Integer.valueOf(valeurs[2]);
            nbTD3_PL = Integer.valueOf(valeurs[3]);

            int nbTotalDD2 = nbDD2_SR + nbDD2_PL;
            int nbTotalTD3 = nbTD3_SR + nbTD3_PL;

            ligne_DD2 += nbTotalDD2;
            ligne_TD3 += nbTotalTD3;

            if (nbDD2_PL > 0) { // s'il y a des DD2 en playoffs
                ligne_DD2 += " (dont " + nbDD2_PL + " en playoffs)";
            }

            if (nbTD3_PL > 0) { // s'il y a des TD3 en playoffs
                ligne_TD3 += " (dont " + nbTD3_PL + " en playoffs)";
            }
        } catch (NumberFormatException e) {
            throw new ESPNException();
        }

        return ligne_DD2 + "\n" + ligne_TD3 + recuperationDateDuJour();
    }
    
    /**
     * Retourne la date du jour au bon format
     *
     * @return la date du jour
     */
    private String recuperationDateDuJour() {

        Date date = new Date();

        SimpleDateFormat formatter = new SimpleDateFormat("d MMMM yyyy");

        return "\n''Dernière mise à jour : {{date-|" + formatter.format(date) + "}}''";
    }

    public String getTitre() {
        return titre;
    }

    public String getUrl() {
        return url;
    }

    public boolean necessiteWarning() {
        return warning;
    }
    
    public String getNomJoueur() {
        // le nom du joueur correspond à la partie gauche avant "Stats"
        return titre.substring(0, titre.indexOf(" Stats"));
    }
}
