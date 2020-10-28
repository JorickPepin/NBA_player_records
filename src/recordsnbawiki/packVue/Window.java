package recordsnbawiki.packVue;

import java.awt.Container;
import javax.swing.JFrame;
import recordsnbawiki.packLogic.Controller;

/**
 *
 * @author Jorick
 */
public class Window extends JFrame implements Observer {

    private Controller controller;
    
    /**
     * Creates new form Window
     * @param controller
     */
    public Window(Controller controller) {
        initComponents();
        
        this.controller = controller;
    }
                      
    private void initComponents() {

        label_RealGM = new javax.swing.JLabel();
        label_ESPN = new javax.swing.JLabel();
        textField_RealGM = new javax.swing.JTextField();
        textField_ESPN = new javax.swing.JTextField();
        button_submit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(500, 600));
        setResizable(false);
        
        label_RealGM.setText("Identifiant RealGM :");
        label_ESPN.setText("Identifiant ESPN :");

        button_submit.setText("Valider");
        
        // ---- this part will be changed -----
        Container container = getContentPane();
        container.setLayout(null);
        
        label_RealGM.setBounds(158, 50, 120, 20);
        label_ESPN.setBounds(158, 80, 120, 20);
        
        textField_RealGM.setBounds(283, 50, 61, 20);
        textField_ESPN.setBounds(283, 80, 61, 20);
                
        container.add(label_RealGM);
        container.add(label_ESPN);
        container.add(textField_RealGM);
        container.add(textField_ESPN);
        // ------------------------------------
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }                       

    @Override
    public void update(String code) {
        
    }
                  
    private javax.swing.JButton button_submit;
    private javax.swing.JLabel label_RealGM;
    private javax.swing.JLabel label_ESPN;
    private javax.swing.JTextField textField_ESPN;
    private javax.swing.JTextField textField_RealGM;
}