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
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import recordsnbawiki.utils.ESPNException;
import recordsnbawiki.utils.RealGMException;

/**
 * Récupération et traitement du contenu
 *
 * @author Jorick
 */
public class DataManagement {

    private String contenuFinal;

    /**
     * Titre de la page web RealGM pour récupérer le nom du joueur
     */
    private String titreRealGM;

    /**
     * Titre de la page web ESPN pour récupérer le nom du joueur
     */
    private String titreESPN;

    /**
     * Contient l'url RealGM personnalisée selon le joueur
     */
    private String URL_RealGM;

    /**
     * Contient l'url ESPN personnalisée selon le joueur
     */
    private String URL_ESPN;

    /**
     * Permet de savoir si le joueur a au moins un record en playoffs
     * afin de ne pas les afficher s'il n'en a pas 
     */
    private boolean aDesRecordsEnPlayoffs;
    
    /**
     * Récupère le contenu des deux tableaux contenant les records depuis le
     * code source de la page du joueur
     *
     * @param identifiant - l'identifiant RealGM du joueur
     * @return un tableau contenant deux String, une pour les records en saison
     * régulière (SR) et une pour ceux en playoffs (PL)
     * @throws RealGMException
     */
    private String[] recuperationContenuRealGM(int identifiant) throws RealGMException {
        String recordSR = "";
        String recordPL = "";

        URL_RealGM = "https://basketball.realgm.com/player/wd/Bests/" + identifiant + "/NBA";

        try {
            Document document = Jsoup.connect(URL_RealGM).get();

            // récupération du titre de la page
            titreRealGM = document.title();

            // la classe "basketball stack force-table" est présente 2 fois dans le code source
            // première fois pour les records en saison régulière
            // deuxième fois pour les records en playoffs
            recordSR = document.getElementsByClass("basketball stack force-table").first().text();
            recordPL = document.getElementsByClass("basketball stack force-table").last().text();

            // l'identifiant correspond à un joueur mais celui-ci n'a jamais joué en NBA
            if (recordSR.isBlank()) {
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
    private ArrayList[] traitementContenuRealGM(String[] contenu) throws RealGMException {
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
        aDesRecordsEnPlayoffs = false;
        for (String s : recordsPL) {
            if (s.matches("^.*\\d$")) {
                listeRecordsPL.set(i, new Record(getNomRecord(s), getValeurRecord(s), getAdversaireRecord(s), getDateRecord(s)));
                aDesRecordsEnPlayoffs = true; // le joueur a au moins un record en playoffs
            } else {
                listeRecordsPL.set(i, new Record(traduireNom(s), "-", "-", "-"));
            }
            i++;
        }

        // il arrive qu'un record soit absent pour un joueur
        // si c'est le cas, on l'ajoute à la liste
        for (i = 0; i < nomsFR.length; ++i) {
            if (!nomsFR[i].equals(listeRecordsSR.get(i).getNom())) {
                listeRecordsSR.add(i, new Record(nomsFR[i], "-", "-", "-"));
            }
            if (!nomsFR[i].equals(listeRecordsPL.get(i).getNom())) {
                listeRecordsPL.add(i, new Record(nomsFR[i], "-", "-", "-"));
            }
        }

        return new ArrayList[]{listeRecordsSR, listeRecordsPL};
    }

    /**
     * Crée le contenu final en mettant en forme les records
     *
     * @param listes - le tableau contenant deux ArrayList, une avec les records
     * en SR et une avec ceux en PL
     * @return une chaîne de caractères correspondant au code Wiki des records
     */
    private String miseEnFormeContenuRealGM(ArrayList[] listes) {
        ArrayList<Record> listeRecordsSR = listes[0];
        ArrayList<Record> listeRecordsPL = listes[1];

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

        for (Record r : listeRecords) {

            String adversaire = r.getAdversaireSansArobase();
            String date = r.getDate();

            if (!adversaire.contains("fois") && !adversaire.startsWith("-")) { // le record a été effectué plusieurs fois ou jamais et n'a donc ni adversaire ni date

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
        }
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
        
        String[] nomsCourts = new String[]{"Nuggets", "Timberwolves", "Thunder",
            "Trail Blazers", "Jazz", "Warriors", "Clippers", "Lakers", "Suns",
            "Kings", "Mavericks", "Rockets", "Grizzlies", "Pelicans", "Spurs",
            "Celtics", "Nets", "Knicks", "Sixers", "Raptors", "Bulls", "Cavaliers",
            "Pistons", "Pacers", "Bucks", "Hawks", "Hornets", "Heat", "Magic", "Wizards", "SuperSonics", "Hornets (1988)"};

        String[] nomsLongs = new String[]{"Nuggets de Denver", "Timberwolves du Minnesota",
            "Thunder d'Oklahoma City", "Trail Blazers de Portland", "Jazz de l'Utah",
            "Warriors de Golden State", "Clippers de Los Angeles", "Lakers de Los Angeles",
            "Suns de Phoenix", "Kings de Sacramento", "Mavericks de Dallas", "Rockets de Houston",
            "Grizzlies de Memphis", "Pelicans de La Nouvelle-Orléans", "Spurs de San Antonio",
            "Celtics de Boston", "Nets de Brooklyn", "Knicks de New York", "76ers de Philadelphie",
            "Raptors de Toronto", "Bulls de Chicago", "Cavaliers de Cleveland", "Pistons de Détroit",
            "Pacers de l'Indiana", "Bucks de Milwaukee", "Hawks d'Atlanta", "Hornets de Charlotte",
            "Heat de Miami", "Magic d'Orlando", "Wizards de Washington", "SuperSonics de Seattle", "Hornets de Charlotte"};

        for (int i = 0; i < nomsCourts.length; ++i) {
            if (adversaire.equals(nomsCourts[i])) {
                adversaire = nomsLongs[i];
                break;
            }
        }
 
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

    private String[] nomsFR = new String[]{"Minutes jouées", "Points", "Rebonds totaux",
        "Passes décisives", "Interceptions", "Contres", "Rebonds offensifs",
        "Rebonds défensifs", "Paniers marqués", "Paniers tentés", "Paniers à 3 points réussis",
        "Paniers à 3 points tentés", "Lancers francs réussis", "Lancers francs tentés", "Balles perdues"};

    /**
     * Traduit le nom d'un record
     *
     * @param nom - le nom du record en anglais
     * @return le nom en français
     */
    private String traduireNom(String nom) {

        String[] nomsEN = new String[]{"Minutes Played", "Points", "Rebounds",
            "Assists", "Steals", "Blocks", "Offensive Rebounds", "Defensive Rebounds",
            "Field Goals Made", "Field Goal Attempts", "3 Pointers Made", "3 Point Attempts",
            "Free Throws Made", "Free Throw Attempts", "Turnovers"};
    
        for (int i = 0; i < nomsEN.length; ++i) {
            if (nom.equals(nomsEN[i])) {
                nom = nomsFR[i];
                break;
            }
        }

        return nom;
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

    /**
     * Récupération du nombre de double-double et triple-double en saison
     * régulière et en playoffs sur espn.com
     *
     * @param identifiant l'identifiant ESPN du joueur
     * @return
     * @throws recordsnbawiki.utils.NoPlayerESPNException
     * @throws java.io.IOException
     */
    private String[] recuperationContenuESPN(int identifiant) throws ESPNException {

        String DD2_SR = "0";
        String TD3_SR = "0";

        URL_ESPN = "https://www.espn.com/nba/player/stats/_/id/" + identifiant;

        // récupération du nombre de double-double et triple-double en saison régulière
        try {
            Document document = Jsoup.connect(URL_ESPN).get();

            // récupération du titre de la page
            titreESPN = document.title();

            // le nombre de double-double en saison régulière correspond au 36e élément <span class="fw-bold">
            DD2_SR = document.select("span.fw-bold").get(36).text();
            // le nombre de triple-double en saison régulière correspond au 37e élément <span class="fw-bold">
            TD3_SR = document.select("span.fw-bold").get(37).text();

        } catch (HttpStatusException e) {
            throw new ESPNException("ID issue");
        } catch (IOException e) {
            throw new ESPNException();
        }

        String DD2_PL = "0";
        String TD3_PL = "0";

        // récupération du nombre de double-double et triple-double en playoffs
        try {
            Document document = Jsoup.connect(URL_ESPN + "/type/nba/seasontype/3").get();

            // si le joueur n'a jamais disputé les playoffs, la page contient
            // un message "No available information." accessible depuis la classe
            // ".NoDataAvailable__Msg__Content"
            // si cette classe n'existe pas, les données sont accessibles
            if (document.select(".NoDataAvailable__Msg__Content").isEmpty()) {

                // le nombre de double-double en playoffs correspond au 36e élément <span class="fw-bold">
                DD2_PL = document.select("span.fw-bold").get(36).text();
                // le nombre de triple-double en playoffs correspond au 37e élément <span class="fw-bold">
                TD3_PL = document.select("span.fw-bold").get(37).text();
            }

        } catch (HttpStatusException e) {
            throw new ESPNException("ID issue");
        } catch (IOException e) {
            throw new ESPNException();
        }

        return new String[]{DD2_SR, TD3_SR, DD2_PL, TD3_PL};
    }

    /**
     * Mise en forme des valeurs récupérées sur ESPN
     *
     * @param valeurs
     * @return le texte de fin de page avec les stats DD2, TD3 et la date de màj
     */
    private String miseEnFormeContenuESPN(String[] valeurs) throws ESPNException {

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

    /**
     * Crée et retourne l'en-tête comprend le titre de section, le modèle,
     * l'introduction et les références
     *
     * @return l'en-tête
     */
    public String getHeader() {
        String template;
        if (aDesRecordsEnPlayoffs) {
            template = "== Records sur une rencontre en NBA ==\n"
                + "Les records personnels PLAYER_NAME en [[National Basketball Association|NBA]] sont les suivants<ref>{{Lien web |titre=TITLE_REALGM |url=URL_REALGM |langue=en |site=basketball.realgm.com}}</ref>{{,}}<ref>{{Lien web |titre=TITLE_ESPN |url=URL_ESPN |langue=en |site=espn.com}}</ref> :\n"
                + "\n"
                + "{| class=\"wikitable\" style=\"font-size: 95%; text-align:center;\"\n"
                + "|+  class=\"hidden\" | Records personnels PLAYER_NAME\n"
                + "|-\n"
                + "! scope=\"col\" rowspan=\"2\"| Type de statistique\n"
                + "! scope=\"col\" colspan=\"3\"| Saison régulière\n"
                + "! scope=\"col\" colspan=\"3\"| Playoffs\n"
                + "|-\n"
                + "! scope=\"col\"| Record\n"
                + "! scope=\"col\"| Adversaire\n"
                + "! scope=\"col\"| Date\n"
                + "! scope=\"col\"| Record\n"
                + "! scope=\"col\"| Adversaire\n"
                + "! scope=\"col\"| Date\n"
                + "|-\n";
        } else {
            template = "== Records sur une rencontre en NBA ==\n"
                + "Les records personnels PLAYER_NAME en [[National Basketball Association|NBA]] sont les suivants<ref>{{Lien web |titre=TITLE_REALGM |url=URL_REALGM |langue=en |site=basketball.realgm.com}}</ref>{{,}}<ref>{{Lien web |titre=TITLE_ESPN |url=URL_ESPN |langue=en |site=espn.com}}</ref> :\n"
                + "\n"
                + "{| class=\"wikitable\" style=\"font-size: 95%; text-align:center;\"\n"
                + "|+  class=\"hidden\" | Records personnels PLAYER_NAME\n"
                + "|-\n"
                + "! scope=\"col\" rowspan=\"2\"| Type de statistique\n"
                + "! scope=\"col\" colspan=\"3\"| Saison régulière\n"
                + "|-\n"
                + "! scope=\"col\"| Record\n"
                + "! scope=\"col\"| Adversaire\n"
                + "! scope=\"col\"| Date\n"
                + "|-\n";
        }
        
        // remplacement des titres dans les références
        template = template.replace("TITLE_REALGM", recuperationNomJoueurRealGM() + " Career Bests");
        template = template.replace("TITLE_ESPN", recuperationNomJoueurESPN() + " Stats");

        // remplacement des url dans les références
        template = template.replace("URL_REALGM", URL_RealGM);
        template = template.replace("URL_ESPN", URL_ESPN);

        String nomDuJoueur = recuperationNomJoueurESPN();

        if (necessiteElision(nomDuJoueur.charAt(0))) { // si le nom commence par une voyelle ou un h
            template = template.replace("PLAYER_NAME", "d'" + nomDuJoueur);
        } else {
            template = template.replace("PLAYER_NAME", "de " + nomDuJoueur);
        }

        return template;
    }

    /**
     * Permet de savoir si une élision est nécessaire devant un nom
     *
     * @param c - le premier caractère du nom
     * @return true si c'est une voyelle ou un h, false sinon
     */
    private boolean necessiteElision(char c) {
        return "AEIOUaeiouHh".indexOf(c) != -1;
    }

    /**
     * Retourne le nom et le prénom du joueur à partir du titre de la page
     * RealGM
     *
     * @return string contenant prénom et nom du joueur
     */
    public String recuperationNomJoueurRealGM() {
        // le nom du joueur correspond à la partie gauche avant "Career"
        return titreRealGM.substring(0, titreRealGM.indexOf(" Career"));
    }

    /**
     * Retourne le nom et le prénom du joueur à partir du titre de la page ESPN
     *
     * @return
     */
    public String recuperationNomJoueurESPN() {
        // le nom du joueur correspond à la partie gauche avant "Stats"
        return titreESPN.substring(0, titreESPN.indexOf(" Stats"));
    }

    /**
     * Retourne le contenu final contenant les records personnels du joueur
     *
     * @param identifiantRealGM - identifiant RealGM du joueur
     * @return une chaîne de caractères correspondant au contenu de RealGM mis
     * en forme
     * @throws RealGMException
     */
    public String getRealGMContent(int identifiantRealGM) throws RealGMException {
        String[] contenuBrutRealGM = recuperationContenuRealGM(identifiantRealGM);
        ArrayList[] listesRecords = traitementContenuRealGM(contenuBrutRealGM);
        String contenuFinalRealGM = miseEnFormeContenuRealGM(listesRecords);

        return contenuFinalRealGM;
    }

    /**
     * Retourne le contenu contenant le nombre de double-double et triple-double
     * du joueur
     *
     * @param identifiantESPN - identifiant ESPN du joueur
     * @return une chaîne de caractères correspondant au contenu d'ESPN mis en
     * forme + la date de màj
     * @throws ESPNException
     */
    public String getESPNContent(int identifiantESPN) throws ESPNException {
        String[] contenuBrutESPN = recuperationContenuESPN(identifiantESPN);
        String contenuFinalESPN = miseEnFormeContenuESPN(contenuBrutESPN);

        return contenuFinalESPN;
    }

    public String getFinalContent() {
        return contenuFinal;
    }

    public void setFinalContent(String finalContent) {
        this.contenuFinal = finalContent;
    }
}
