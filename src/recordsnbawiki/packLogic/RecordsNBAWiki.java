package recordsnbawiki.packLogic;

import java.util.Scanner;
import recordsnbawiki.packVue.Window;

/**
 *
 * @author Jorick
 */
public class RecordsNBAWiki {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*
        Scanner sc = new Scanner(System.in);
        System.out.print("Identifiant RealGM du joueur : ");
        int idRealGM = sc.nextInt();
        System.out.print("Identifiant ESPN du joueur : ");
        int idESPN = sc.nextInt();
        obtenirFichierTexte(idRealGM, idESPN); */
        
        Controller controller = new Controller();
        Window window = new Window(controller);
    }

}
