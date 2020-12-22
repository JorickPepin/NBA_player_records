package recordsnbawiki.packLogic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Jorick
 */
public class Header {
    
    private String contenu;
    
    private RealGM realGM;
    
    private ESPN espn;
    
    public Header(RealGM realGM, ESPN espn) throws FileNotFoundException, IOException {
        this.realGM = realGM;
        this.espn = espn;
        
        contenu = getHeader();
    }
    
    /**
     * Crée et retourne l'en-tête comprend le titre de section, le modèle,
     * l'introduction et les références
     *
     * @return l'en-tête
     * @throws FileNotFoundException
     */
    private String getHeader() throws FileNotFoundException, IOException {
        
        BufferedReader br;
        
        if (realGM.getADesRecordsEnPlayoffs()) { // le header n'est pas le même si le joueur a des records en playoffs ou non
            try { 
                br = new BufferedReader (new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("data/header_playoffs.txt"), StandardCharsets.UTF_8));
            } catch (NullPointerException e) {
                throw new FileNotFoundException("header_playoffs.txt");
            }
        } else {
            try {
                br = new BufferedReader (new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("data/header_noplayoffs.txt"), StandardCharsets.UTF_8));
            } catch (NullPointerException e) {
                throw new FileNotFoundException("header_noplayoffs.txt");
            }
        }

        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
        }
        
        String template = sb.toString();

        br.close();

        // remplacement des titres dans les références
        template = template.replace("TITLE_REALGM", realGM.getNomJoueur() + " Career Bests");
        template = template.replace("TITLE_ESPN", espn.getNomJoueur() + " Stats");

        // remplacement des url dans les références
        template = template.replace("URL_REALGM", realGM.getUrl());
        template = template.replace("URL_ESPN", espn.getUrl());

        String nomDuJoueur = espn.getNomJoueur();

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
    
   
    public String getContenu() {
        return contenu;
    }
}
