package recordsnbawiki.packLogic;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import recordsnbawiki.packLogic.json.JsonManagement;
import recordsnbawiki.utils.RealGMException;

/**
 *
 * @author Jorick
 */
public class RealGM {
    
    /** Titre de la page web RealGM (permet d'avoir le nom du joueur) */
    private String titre;
    
    /** URL de la page web RealGM */
    private String url;
    
    /**
     * Permet de savoir si le joueur a au moins un record en playoffs
     * afin de ne pas afficher la partie droite du tableau s'il n'en a pas 
     */
    private boolean aDesRecordsEnPlayoffs;
    
    /** Permet d'utiliser les données récupérées dans les fichiers json */
    private JsonManagement json;
    
    public String genererContenu(String id) throws RealGMException {
        this.url = "https://basketball.realgm.com/player/wd/Bests/" + id + "/NBA";
        this.aDesRecordsEnPlayoffs = false;
        
        String[] contenuBrut = recuperationContenu();
        List<ArrayList<Record>> listesRecords = traitementContenu(contenuBrut);
        String contenuFinal = miseEnFormeContenu(listesRecords);
        
        return contenuFinal;
    }
    
    /**
     * Récupère le contenu des deux tableaux contenant les records depuis le
     * code source de la page du joueur
     *
     * @param identifiant - l'identifiant RealGM du joueur
     * @return un tableau contenant deux String, une pour les records en saison
     * régulière (SR) et une pour ceux en playoffs (PL)
     * @throws RealGMException
     */
    private String[] recuperationContenu() throws RealGMException {
        String recordSR = "";
        String recordPL = "";

        try {
            Document document = Jsoup.connect(url).get();

            // récupération du titre de la page
            titre = document.title();

            // la classe "basketball stack force-table" est présente 2 fois dans le code source
            // première fois pour les records en saison régulière
            // deuxième fois pour les records en playoffs
            recordSR = document.getElementsByClass("basketball stack force-table").first().text();
            recordPL = document.getElementsByClass("basketball stack force-table").last().text();

            // l'identifiant correspond à un joueur mais celui-ci n'a jamais joué en NBA
            if (recordSR.isEmpty()) {
                throw new RealGMException("never played in NBA");
            }

        } catch (NullPointerException e) {
            throw new RealGMException("ID issue"); // l'identifiant ne correspond à aucun joueur
        } catch (IOException e) {
            throw new RealGMException();
        }

        return new String[]{recordSR, recordPL};
    }
    
    /**
     * Crée les records à partir du contenu brut de RealGM
     *
     * @param contenu - le tableau contenant les deux String (records en saison
     * régulière et en playoffs)
     * @return un tableau contenant deux ArrayList, une avec les records en SR
     * et une avec ceux en PL
     * @throws ParseException
     */
    private List<ArrayList<Record>> traitementContenu(String[] contenu) throws RealGMException {
        // la dernière info sur un record est sa date, on split donc 
        // les chaînes récupérées par les dates pour avoir les records séparement
        // on split également par "-" car c'est la seule info que contient la ligne si le record
        // n'a jamais été effectué par le joueur (ex: tirs à 3pts pour les pivots)
        String[] recordsSR = contenu[0].split("(?<=\\d{2}/\\d{2}/\\d{2}) |\\ - ");
        String[] recordsPL = contenu[1].split("(?<=\\d{2}/\\d{2}/\\d{2}) |\\ - ");

        // enlève le dernier record (FIC) qui n'est pas affiché sur Wiki
        recordsSR = Arrays.copyOf(recordsSR, recordsSR.length - 1);
        recordsPL = Arrays.copyOf(recordsPL, recordsPL.length - 1);

        ArrayList<Record> listeRecordsSR = new ArrayList<>();
        ArrayList<Record> listeRecordsPL = new ArrayList<>();
       
        int i;
        
        // initialisation des listes avec 15 records vides (15 = nb de records au total)
        for (i = 0; i < 15; ++i) { listeRecordsSR.add(new Record()); listeRecordsPL.add(new Record()); } 
      
        i = 0;
        for (String s : recordsSR) {
            if (s.matches("^.*\\d$")) { // si la ligne se termine par un chiffre
                // cela signifie qu'il y a une date de record et que le joueur l'a effectué
                // on récupère donc les différentes informations pour créer le record
                listeRecordsSR.set(i, new Record(getNomRecord(s), getValeurRecord(s), getAdversaireRecord(s), getDateRecord(s)));
            } else { // sinon on crée un record vide
                listeRecordsSR.set(i, new Record(traduireNom(s), "-", "-", "-"));
            }
            i++;
        }
      
        i = 0;
        for (String s : recordsPL) {
            if (s.matches("^.*\\d$")) {
                listeRecordsPL.set(i, new Record(getNomRecord(s), getValeurRecord(s), getAdversaireRecord(s), getDateRecord(s)));
                aDesRecordsEnPlayoffs = true; // le joueur a au moins un record en playoffs
            } else {
                listeRecordsPL.set(i, new Record(traduireNom(s), "-", "-", "-"));
            }
            i++;
        }

        // il arrive qu'un record soit absent pour un joueur (ex: ligne 'paniers à 3pts réussis' pour un pivot)
        // si c'est le cas, on ajoute un record 'vide' à la place où il devrait être
        
        ArrayList<String> nomsFR = new ArrayList<>(); // liste contenant les statistiques en français
        json.getStatNames_EN_to_FR().forEach((k, v) -> {
            nomsFR.add(v);
        });
        
        for (i = 0; i < nomsFR.size(); ++i) {
            if (!nomsFR.get(i).equals(listeRecordsSR.get(i).getNom())) { // si le record n'est pas celui qui est attendu à cette place
                listeRecordsSR.add(i, new Record(nomsFR.get(i), "-", "-", "-")); // on ajoute un record vide à sa place et le décale
            }
            if (!nomsFR.get(i).equals(listeRecordsPL.get(i).getNom())) {
                listeRecordsPL.add(i, new Record(nomsFR.get(i), "-", "-", "-"));
            }
        }
        
        List<ArrayList<Record>> liste = new ArrayList<>();
        liste.add(listeRecordsSR);
        liste.add(listeRecordsPL);
        
        return liste;
    }
    
    /**
     * Crée le contenu final en mettant en forme les records
     *
     * @param listes - le tableau contenant deux ArrayList, une avec les records
     * en SR et une avec ceux en PL
     * @return une chaîne de caractères correspondant au code Wiki des records
     */
    private String miseEnFormeContenu(List<ArrayList<Record>> listes) {
        ArrayList<Record> listeRecordsSR = listes.get(0);
        ArrayList<Record> listeRecordsPL = listes.get(1);

        String contenuRealGM = "";

        // contiennent les mêmes records mais dans l'ordre d'affichage sur Wiki
        ArrayList<Record> listeRecordsSRTriees = new ArrayList<>();
        ArrayList<Record> listeRecordsPLTriees = new ArrayList<>();

        // l'ordre d'affichage des records n'est pas le même que l'ordre dans lequel on les récupère
        // ex : Minutes jouées est récupéré en premire mais doit être affiché en dernier 
        int[] ordreApparation = new int[]{1, 8, 9, 10, 11, 12, 13, 6, 7, 2, 3, 4, 5, 14, 0};
        for (int i : ordreApparation) {
            listeRecordsSRTriees.add(listeRecordsSR.get(i));
            listeRecordsPLTriees.add(listeRecordsPL.get(i));
        }

        // on applique les liens internes et les modèles aux records
        formatageRecords(listeRecordsSRTriees);
        formatageRecords(listeRecordsPLTriees);

        for (int i = 0; i < listeRecordsSRTriees.size(); ++i) {

            contenuRealGM += "| " + listeRecordsSRTriees.get(i).getNom() + " || " + listeRecordsSRTriees.get(i).toString();
            if (aDesRecordsEnPlayoffs) { // le joueur a au moins un record en playoffs donc on les affiche
                contenuRealGM += "| " + listeRecordsPLTriees.get(i).toString();
            }

            if (i == listeRecordsSRTriees.size() - 1) { // si on est au dernier record, on ferme le modèle avec l'accolade
                contenuRealGM += "|}\n";
            } else {
                contenuRealGM += "|-\n";
            }
        }

        return contenuRealGM;
    }
    
    /**
     * Applique les liens internes et les modèles sur les records
     *
     * @param listeRecords - une liste contenant les records
     */
    private void formatageRecords(ArrayList<Record> listeRecords) {
        ArrayList<String> adversairesPresents = new ArrayList<>();
        ArrayList<String> datesPresentes = new ArrayList<>();

        listeRecords.forEach(r -> {
            String adversaire = r.getAdversaireSansArobase();
            String date = r.getDate();
            
            // si le record a été effectué plusieurs fois ou jamais, il n'a ni adversaire ni date
            if (!adversaire.contains("fois") && !adversaire.startsWith("-")) {

                if (!adversairesPresents.contains(adversaire)) { // si c'est la première apparation de l'adversaire
                    adversairesPresents.add(adversaire); // on l'ajoute à la liste de ceux présents

                    r.setAdversaire(formaterAdversaire(r)); // on lui ajoute le lien interne
                }

                if (!datesPresentes.contains(date)) { // si c'est la première apparation de la date
                    datesPresentes.add(date); // on l'ajoute à la liste de celles présentes

                    r.setDate("{{date|" + date + "|en basket-ball}}"); // application du modèle
                } else {
                    r.setDate("{{date-|" + date + "}}"); // application du modèle pour une date déjà présente
                }
            }
        });
    }

    /**
     * Retourne le nom du record
     *
     * @param s - la ligne contenant nom + valeur + adversaire + date
     * @return le nom du record en français
     */
    private String getNomRecord(String s) {
        // pour récupérer le nom, on split la ligne par la première valeur numérique
        // car le nom est à gauche de la valeur du record      
        Matcher matcher = Pattern.compile("\\d+").matcher(s);

        matcher.find(1); // find(1) pour éviter le 3 de 3 Pointers Made et Attempts

        String nom = s.substring(0, s.indexOf(" " + matcher.group()));

        // on traduit le nom avant de le retourner
        return traduireNom(nom);
    }

    /**
     * Retourne la valeur du record
     *
     * @param s - la ligne contenant nom + valeur + adversaire + date
     * @return la valeur du record
     */
    private String getValeurRecord(String s) {
        // la valeur correspond à la première valeur numérique
        Matcher matcher = Pattern.compile("\\d+").matcher(s);

        matcher.find(1); // find(1) pour éviter le 3 de 3 Pointers Made et Attempts

        return matcher.group();
    }

    /**
     * Retourne la date du record
     *
     * @param s - la ligne contenant nom + valeur + adversaire + date
     * @return la date du record en français
     */
    private String getDateRecord(String s) throws RealGMException {
        String[] ligneSeparee = s.split(" ");

        // si la ligne contient "times", le record a été fait plusieurs fois
        // et la date est laissée vide
        if (!s.contains("times")) {
            // la date correspond au dernier mot de chaque ligne
            String date = ligneSeparee[ligneSeparee.length - 1];

            // on formate la date avant de la retourner
            return transformerDate(date);
        } else {
            return "";
        }
    }

    /**
     * Retourne l'adversaire du record ou le nombre de fois qu'il a été fait
     * s'il a eu lieu plusieurs fois
     *
     * @param s - la ligne contenant nom + valeur + adversaire + date
     * @return l'adversaire du record en français
     */
    private String getAdversaireRecord(String s) throws RealGMException {
        // suppression du nom du record dans la lignes
        Matcher matcher = Pattern.compile("\\d+").matcher(s);
        matcher.find(1); // find(1) pour éviter le 3 de 3 Pointers Made et Attempts   
        String ligne = s.substring(matcher.start());

        String[] ligneSeparee = ligne.split(" ");

        String adversaire;

        // si la ligne contient "times", cela signifie que le record 
        // a été effectué plusieurs fois. Cependant, on ne peut récupérer
        // les infos que sur la dernière fois qu'il a été fait.
        // On se contente donc de dire qu'il a été fait plusieurs fois
        if (!ligne.contains("times")) {
            // s'il n'y a pas "times", l'adversaire correspond au 3e mot
            // s'il y a un '@', on le rajoute devant l'adversaire (match à l'extérieur)

            // si le 4e mot n'est pas "on", cela signifie que l'équipe est composée de 2 mots (Trail Blazers)
            if (!"on".equals(ligneSeparee[3])) {
                if (ligne.contains("@")) {
                    adversaire = "@ " + traduireAdversaire(ligneSeparee[2] + " " + ligneSeparee[3], getDateRecord(s));
                } else {
                    adversaire = traduireAdversaire(ligneSeparee[2] + " " + ligneSeparee[3], getDateRecord(s));
                }

            } else { // sinon le nom est juste composé du 3e mot (Mavericks, Clippers, ...)
                if (ligne.contains("@")) {
                    adversaire = "@ " + traduireAdversaire(ligneSeparee[2], getDateRecord(s));
                } else {
                    adversaire = traduireAdversaire(ligneSeparee[2], getDateRecord(s));
                }
            }
        } else {

            // s'il y a "times", le nombre de fois que le record a été 
            // fait correspond au 2e mot auquel on rajoute "fois" et 
            // la date est laisée vide
            adversaire = ligneSeparee[1] + " fois";
        }

        return adversaire;
    }

    /**
     * Transforme une date au format "03/01/19" en "1 mars 2019"
     *
     * @param dateBrute = la date au format "MM/dd/yy"
     * @return la date en français
     */
    private String transformerDate(String dateBrute) throws RealGMException {
        String dateEnFrancais = "";

        try {
            // définition du format que l'on souhaite pour la date
            SimpleDateFormat formater = new SimpleDateFormat("d MMMM yyyy");

            // création de la date en objet Date depuis le String
            Date date = new SimpleDateFormat("MM/dd/yy").parse(dateBrute);

            // formatage de la date
            dateEnFrancais = formater.format(date);
        } catch (ParseException e) {
            throw new RealGMException();
        }

        return dateEnFrancais;
    }

    /**
     * Retourne le nom de l'adversaire dans sa version longue en français
     * (Amélioration possible avec HashMap)
     *
     * @param adversaire
     * @return le nom de l'adversaire en français
     */
    private String traduireAdversaire(String adversaire, String date) throws RealGMException {
        
        adversaire = json.getTeamNames_short_to_long().get(adversaire);
 
        Date dateRecord;
        try {   
            dateRecord = new SimpleDateFormat("d MMMM yyyy").parse(date);
        } catch (ParseException e) {
            throw new RealGMException();
        }

        // liste des équipes ayant changé de nom après 1990
        ArrayList<String> franchisesAControler = new ArrayList<>(Arrays.asList(
                "Hornets de Charlotte", "Nets de Brooklyn", "Grizzlies de Memphis",
                "Pelicans de La Nouvelle-Orléans", "Wizards de Washington"));
 
        // si l'adversaire fait partie de la liste, on vérifie si le record
        // a été fait lorsque l'équipe avec un autre nom grâce à la date du record
        // et change le nom si besoin
        if (franchisesAControler.contains(adversaire)) {
            adversaire = renommageFranchise(adversaire, dateRecord);
        }
        
        return adversaire;
    }

    /**
     * Renomme une franchise en fonction de la date du record si besoin
     * (prend en compte les changements de nom après 1990)
     * @param franchise - le nom de la franchise à potentiellement changer
     * @param dateRecord - la date à laquelle le record a été effectué
     * @return le nom de la franchise selon la date du record
     */
    private String renommageFranchise(String franchise, Date dateRecord) {

        switch (franchise) {
            case "Hornets de Charlotte":
                try {
                    Date dateDebutBobcats = new SimpleDateFormat("d MMMM yyyy").parse("2 novembre 2004");
                    Date dateFinBobcats = new SimpleDateFormat("d MMMM yyyy").parse("28 octobre 2014");

                    if (dateRecord.before(dateFinBobcats) && dateRecord.after(dateDebutBobcats)) {
                        franchise = "Bobcats de Charlotte";
                    }

                } catch (ParseException e) {}
                break;
            case "Nets de Brooklyn":
                try {
                    Date dateDebutNetsDeBrooklyn = new SimpleDateFormat("d MMMM yyyy").parse("30 octobre 2012");

                    if (dateRecord.before(dateDebutNetsDeBrooklyn)) {
                        franchise = "Nets du New Jersey";
                    }

                } catch (ParseException e) {}
                break;
            case "Grizzlies de Memphis":
                try {
                    Date dateDebutGrizzliesDeMemphis = new SimpleDateFormat("d MMMM yyyy").parse("30 octobre 2001");

                    if (dateRecord.before(dateDebutGrizzliesDeMemphis)) {
                        franchise = "Grizzlies de Vancouver";
                    }

                } catch (ParseException e) {}
                break;
            case "Pelicans de La Nouvelle-Orléans":
                try {
                    Date dateDebutHornetsOklahoma = new SimpleDateFormat("d MMMM yyyy").parse("1 novembre 2005");
                    Date dateFinHornetsOklahoma = new SimpleDateFormat("d MMMM yyyy").parse("30 octobre 2007");
                    Date dateDebutPelicans = new SimpleDateFormat("d MMMM yyyy").parse("29 octobre 2013");
                    
                    if (dateRecord.before(dateDebutHornetsOklahoma)) {
                        franchise = "Hornets de La Nouvelle-Orléans";
                    } else if (dateRecord.before(dateFinHornetsOklahoma)) {
                        franchise = "Hornets de La Nouvelle-Orléans/Oklahoma City";
                    } else if (dateRecord.before(dateDebutPelicans)) {
                        franchise = "Hornets de La Nouvelle-Orléans";
                    }
                    
                } catch (ParseException e) {}
                break;
            case "Wizards de Washington":
                try {
                    Date dateDebutWizards = new SimpleDateFormat("d MMMM yyyy").parse("31 octobre 1997");

                    if (dateRecord.before(dateDebutWizards)) {
                        franchise = "Bullets de Washington";
                    }

                } catch (ParseException e) {}
                break;
        }
        
        return franchise;
    }

    /**
     * Traduit le nom d'un record
     *
     * @param nom - le nom du record en anglais
     * @return le nom en français
     */
    private String traduireNom(String nom) {
        return json.getStatNames_EN_to_FR().get(nom);
    }

    /**
     * Applique le lien interne sur l'adversaire
     *
     * @param r - le record à formater
     * @return l'adversaire formaté
     */
    private String formaterAdversaire(Record r) {

        String res = "[[" + r.getAdversaireSansArobase() + "]]"; // cas général

        // le lien interne pour les Hornets est particulier (gestion de l'homonymie)
        if ("Hornets de Charlotte".equals(r.getAdversaireSansArobase())) {
            res = "[[Hornets de Charlotte (NBA)|Hornets de Charlotte]]";
        }

        if (r.getAdversaire().charAt(0) == '@') {
            return "@ " + res;
        } else {
            return res;
        }
    }
    
    public void setJson(JsonManagement json) {
        this.json = json;
    }

    public boolean getADesRecordsEnPlayoffs() {
        return aDesRecordsEnPlayoffs;
    }    

    public String getTitre() {
        return titre;
    }

    public String getUrl() {
        return url;
    }
    
    public String getNomJoueur() {
        // le nom du joueur correspond à la partie gauche avant "Career"
        return titre.substring(0, titre.indexOf(" Career"));
    }
}
