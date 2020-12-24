package recordsnbawiki.packVue;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import static org.jsoup.internal.StringUtil.isNumeric;
import recordsnbawiki.packLogic.Controller;
import recordsnbawiki.utils.ESPNException;
import recordsnbawiki.utils.RealGMException;

/**
 *
 * @author Jorick
 */
public class Window extends JFrame {

    /** Controller */
    private Controller controller;

    /** 
     * Creates new form Window
     * 
     * @param controller 
     */
    public Window(Controller controller) {
        this.controller = controller;
        
        initComponents();
        addListeners();
        
        this.setIcon();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
        ToolTipManager.sharedInstance().setEnabled(false);
    }
    
    public void update(String code) {

        switch (code) {
            case "fieldsNotFilled":
                label_alert.setText("Tous les champs ne sont pas remplis.");
                label_alert.setForeground(Color.RED);
                break;
            case "invalidEntry":
                label_alert.setText("Les identifiants doivent être des entiers.");
                label_alert.setForeground(Color.RED);
                break;
            case "out of bounds":
                label_alert.setText("Identifiants hors limites.");
                label_alert.setForeground(Color.RED);
                break;
            case "loading":
                label_alert.setText("Chargement ...");
                label_alert.setForeground(Color.BLACK);
                break;
            case "submission":
                submission();
                break;
            case "copy":
                addContentToClipboard();
                String text = "Contenu copié dans le presse-papier.";
                label_alert.setText(text);
                label_alert.setForeground(Color.BLACK);

                Timer t = new Timer(3000, (ActionEvent e) -> {
                    if (text.equals(label_alert.getText())) {
                        label_alert.setText(null);
                    }
                });
                t.setRepeats(false);
                t.start();
                break;
            case "errorRealGM":
                label_alert.setText("Le contenu de RealGM n'a pas pu être récupéré.");
                label_alert.setForeground(Color.RED);
                stop = true;
                break;
            case "errorESPN":
                label_alert.setText("Le contenu d'ESPN n'a pas pu être récupéré.");
                label_alert.setForeground(Color.RED);
                stop = true;
                break;
            case "errorNoPlayerRealGM":
                label_alert.setText("L'ID RealGM ne correspond à aucun joueur.");
                label_alert.setForeground(Color.RED);
                stop = true;
                break;
            case "errorNoPlayerESPN":
                label_alert.setText("L'ID ESPN ne correspond à aucun joueur.");
                label_alert.setForeground(Color.RED);
                stop = true;
                break;
            case "errorNeverPlayedInNBARealGM":
                label_alert.setText("Le joueur n'a jamais joué en NBA.");
                label_alert.setForeground(Color.RED);
                stop = true;
                break;
            case "names incompatibility":
                displayIncompatibilityMessage();
                break;
            case "warningESPN":
                displayESPNWarningMessage();
                break;
            case "teams.jsonIssue":
                label_alert.setText("Le fichier teams.json est introuvable.");
                label_alert.setForeground(Color.RED);
                stop = true;
                break;
            case "stats.jsonIssue":
                label_alert.setText("Le fichier stats.json est introuvable.");
                label_alert.setForeground(Color.RED);
                stop = true;
                break;
            case "header_playoffs.txtIssue":
                label_alert.setText("Le fichier header_playoffs.txt est introuvable.");
                label_alert.setForeground(Color.RED);
                stop = true;
                break;
            case "header_noplayoffs.txtIssue":
                label_alert.setText("Le fichier header_noplayoffs.txt est introuvable.");
                label_alert.setForeground(Color.RED);
                stop = true;
                break;
            case "fileIssue":
                label_alert.setText("Une ressource n'a pas pu être récupérée.");
                label_alert.setForeground(Color.RED);
                stop = true;
                break;
        }
    }

    /**
     * Display a warning message when the two recovered names are not identical
     */
    private void displayIncompatibilityMessage() {
        String message = "Il se peut que les identifiants n'appartiennent pas au même joueur.\n\n"
                + "Joueur récupéré sur RealGM : "
                + controller.getRealGMPlayerName()
                + "\nJoueur récupéré sur ESPN : "
                + controller.getESPNPlayerName()
                + "\n\nAssurez-vous qu'il s'agit du même joueur avant de publier le contenu sur Wikipédia.";

        JOptionPane.showMessageDialog(this, message, "Erreur potentielle", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Display a warning message when the player played before the 1993-1994 season
     * (ESPN does not display DD2 and TD3 until this season)
     */
    private void displayESPNWarningMessage() {
        
        String message = "ESPN comptabilise les double-doubles et les triple-doubles depuis la saison 1993-1994.\n"
                + "Or, ce joueur a pris part à des matchs avant cette saison-là.\n\n"
                + "<html>En cliquant sur <span style='color:green'>OK</span>, le contenu d'ESPN sera enlevé pour éviter de rentrer des informations erronées.</html>\n"
                + "<html>Vous pouvez également le <span style='color:red'>conserver</span> (déconseillé).</html>";
        
        String[] options = {"Conserver", "OK"};
        
        int answer = JOptionPane.showOptionDialog(this, message, "Avertissement contenu ESPN", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
        
        if (answer == 1 || answer == -1) { // the user clicks on OK or closes the window
            controller.removeESPNContent();
        }
    }
    
    private void setIcon() {
        BufferedImage image;
        
        try {
            image = ImageIO.read(getClass().getClassLoader().getResource("images/basketball.png"));
            
        } catch (IOException | IllegalArgumentException e) {
            image = (BufferedImage) this.getIconImage();
        }
        
        this.setIconImage(image);
    }
    
    /**
     * Clean the textArea and init fields border
     */
    private void resetComponents() {
        textField_RealGM.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        textField_ESPN.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        textArea_content.setText(""); // clear the textArea 
    }

    private void resetTextFields() {
        textField_RealGM.setText(""); 
        textField_ESPN.setText(""); 
    }
    
    /** 
     * Disable or able all components
     *
     * @param value - true to disable, false otherwise
     */
    private void disableComponents(boolean value) {
        button_submit.setEnabled(!value);
        button_copy.setEnabled(!value);
        textField_RealGM.setEnabled(!value);
        textField_ESPN.setEnabled(!value);
        textArea_content.setEnabled(!value);
        checkBox_header.setEnabled(!value);
    }

    /**
     * Add the content to the textArea
     *
     * @param RealGM_id
     * @param ESPN_id
     */
    private void addContentToTextArea(int RealGM_id, int ESPN_id) {
        this.update("loading");

        disableComponents(true); // on désactive les composants pendant le chargement
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        new Thread(() -> {
            try {
                
                controller.generateContent(RealGM_id, ESPN_id, checkBox_header.isSelected());
                
                if (!stop) {
                    textArea_content.setText(controller.getContent());
                    this.update("copy");
                    resetTextFields();
                }
                
                disableComponents(false);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                stop = false;
                
            } catch (ESPNException | RealGMException e) {}
        }).start();
    }

    /**
     * Add the content to the clipboard
     */
    private void addContentToClipboard() {
        StringSelection stringSelection = new StringSelection(textArea_content.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    /**
     * Is launched when the user presses the button
     */
    private void submission() {
        resetComponents();

        String RealGM_id = "";
        String ESPN_id = "";

        boolean entriesAreValid = true;

        Border redBorder = BorderFactory.createLineBorder(Color.RED, 1);

        if (textField_RealGM.getText().length() > 0) {
            RealGM_id = textField_RealGM.getText().replace(" ", ""); // get text without space
        } else { // le field est vide
            textField_RealGM.setBorder(redBorder);
            entriesAreValid = false;
        }

        if (textField_ESPN.getText().length() > 0) {
            ESPN_id = textField_ESPN.getText().replace(" ", "");
        } else {
            textField_ESPN.setBorder(redBorder);
            entriesAreValid = false;
        }

        if (entriesAreValid) { // si les deux fields sont complétés
            if (!isNumeric(RealGM_id)) { // le contenu du field n'est pas un entier
                textField_RealGM.setBorder(redBorder);
            }
            if (!isNumeric(ESPN_id)) {
                textField_ESPN.setBorder(redBorder);
            }

            if (isNumeric(RealGM_id) && isNumeric(ESPN_id)) { // les deux entrées sont des int, on lance le chargement et l'affichage du contenu
                
                try {
                    addContentToTextArea(Integer.parseInt(RealGM_id), Integer.parseInt(ESPN_id));  
                } catch (NumberFormatException e) {
                    textField_RealGM.setBorder(redBorder);
                    textField_ESPN.setBorder(redBorder);
                    this.update("out of bounds");
                }        
                
            } else {
                this.update("invalidEntry");
            }
        } else {
            this.update("fieldsNotFilled");
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel1 = new java.awt.Panel();
        textField_RealGM = new javax.swing.JTextField();
        textField_ESPN = new javax.swing.JTextField();
        label_RealGM = new javax.swing.JLabel();
        label_ESPN = new javax.swing.JLabel();
        button_submit = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        textArea_content = new javax.swing.JTextArea();
        label_alert = new javax.swing.JLabel();
        button_copy = new javax.swing.JButton();
        checkBox_header = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Records joueur NBA");
        setResizable(false);

        panel1.setPreferredSize(new java.awt.Dimension(300, 122));

        textField_RealGM.setToolTipText("");
        textField_RealGM.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        textField_RealGM.setPreferredSize(new java.awt.Dimension(250, 20));

        textField_ESPN.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        textField_ESPN.setPreferredSize(new java.awt.Dimension(250, 20));

        label_RealGM.setText("Identifiant RealGM :");

        label_ESPN.setText("Identifiant ESPN :");

        button_submit.setText("Valider");
        button_submit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button_submit.setFocusPainted(false);
        button_submit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_submitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(button_submit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(label_RealGM)
                            .addComponent(label_ESPN))
                        .addGap(18, 18, 18)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textField_RealGM, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textField_ESPN, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(63, 63, 63))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textField_RealGM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label_RealGM))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(label_ESPN)
                    .addComponent(textField_ESPN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(button_submit)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        textArea_content.setEditable(false);
        textArea_content.setColumns(20);
        textArea_content.setRows(5);
        jScrollPane1.setViewportView(textArea_content);

        label_alert.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_alert.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        button_copy.setText("Copier");
        button_copy.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button_copy.setFocusPainted(false);
        button_copy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_copyActionPerformed(evt);
            }
        });

        checkBox_header.setSelected(true);
        checkBox_header.setText("En-tête");
        checkBox_header.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        checkBox_header.setFocusPainted(false);
        checkBox_header.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(button_copy)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(50, 50, 50)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(100, 100, 100)
                            .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(46, 46, 46)
                            .addComponent(checkBox_header)
                            .addGap(12, 12, 12)
                            .addComponent(label_alert, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(50, 50, 50))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label_alert, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBox_header))
                .addGap(2, 2, 2)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 385, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(button_copy)
                .addGap(13, 13, 13))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void button_submitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_submitActionPerformed
        this.update("submission");
    }//GEN-LAST:event_button_submitActionPerformed

    private void button_copyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_copyActionPerformed
        this.update("copy");
    }//GEN-LAST:event_button_copyActionPerformed

    private void addListeners() {
        button_submit.addKeyListener(new Listener());
        textField_RealGM.addKeyListener(new Listener());
        textField_ESPN.addKeyListener(new Listener());
    }
    
    private class Listener implements KeyListener {

        @Override
        public void keyPressed(KeyEvent e) {
            // user presses enter key
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                update("submission");
            }
        }
        
        @Override public void keyTyped(KeyEvent e) {}
        @Override public void keyReleased(KeyEvent e) {}
    }
    
    /**
     * Allows to stop the execution when an exception is thrown
     */
    private boolean stop = false;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_copy;
    private javax.swing.JButton button_submit;
    private javax.swing.JCheckBox checkBox_header;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel label_ESPN;
    private javax.swing.JLabel label_RealGM;
    private javax.swing.JLabel label_alert;
    private java.awt.Panel panel1;
    private javax.swing.JTextArea textArea_content;
    private javax.swing.JTextField textField_ESPN;
    private javax.swing.JTextField textField_RealGM;
    // End of variables declaration//GEN-END:variables
}
