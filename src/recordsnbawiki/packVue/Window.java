package recordsnbawiki.packVue;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import static org.jsoup.internal.StringUtil.isNumeric;
import recordsnbawiki.packLogic.Controller;
import recordsnbawiki.packLogic.wikidata.WikidataItem;
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
            case "wikidataIssue":
                label_alert.setText("Un problème est survenu avec Wikidata.");
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
        //textField_RealGM.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        //textField_ESPN.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        textArea_content.setText(""); // clear the textArea 
    }

    private void resetTextFields() {
        //textField_RealGM.setText(""); 
        //textField_ESPN.setText(""); 
    }
    
    /** 
     * Disable or able all components
     *
     * @param value - true to disable, false otherwise
     */
    private void disableComponents(boolean value) {
        //button_submit.setEnabled(!value);
        button_copy.setEnabled(!value);
       // textField_RealGM.setEnabled(!value);
        //textField_ESPN.setEnabled(!value);
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
/*
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
        }).start();*/
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
/*
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
        }*/
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        button_copy = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(50, 0), new java.awt.Dimension(50, 0), new java.awt.Dimension(50, 32767));
        textArea_content = new javax.swing.JTextArea();
        checkBox_header = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        list_players = new javax.swing.JList<>();
        field_player_name = new javax.swing.JTextField();
        label_player_name = new javax.swing.JLabel();
        label_alert = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Records joueur NBA");
        setPreferredSize(new java.awt.Dimension(500, 600));
        setResizable(false);
        setSize(new java.awt.Dimension(500, 600));

        button_copy.setText("Copier");
        button_copy.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button_copy.setFocusPainted(false);

        textArea_content.setEditable(false);
        textArea_content.setColumns(20);
        textArea_content.setRows(5);
        textArea_content.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        textArea_content.setPreferredSize(new java.awt.Dimension(400, 94));

        checkBox_header.setSelected(true);
        checkBox_header.setText("En-tête");
        checkBox_header.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        checkBox_header.setFocusPainted(false);
        checkBox_header.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        jScrollPane1.setViewportView(list_players);

        field_player_name.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        label_player_name.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        label_player_name.setText("Nom du joueur : ");

        label_alert.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(filler2, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(checkBox_header)
                    .addComponent(textArea_content, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(button_copy, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(label_player_name, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(field_player_name, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(label_alert, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(filler2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(field_player_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label_player_name))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label_alert)
                .addGap(10, 10, 10)
                .addComponent(checkBox_header)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textArea_content, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_copy)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addListeners() {
        field_player_name.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { action(); }
            @Override public void removeUpdate(DocumentEvent e) { action(); }
            @Override public void changedUpdate(DocumentEvent e) { action(); }
             
            public void action() {

                String text = field_player_name.getText();

                if (text.length() >= 3) { // the text field contains three or more caracters
                    new Thread(() -> {
                        controller.retrievePlayers(text);
                        addPlayers(controller.getPlayers());
                    
                    }).start();
                } 
                
                if (text.length() == 0) { // clear the list
                    DefaultListModel listModel = (DefaultListModel) list_players.getModel();
                    listModel.removeAllElements();
                }
            }
        });

        list_players.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list_players = (JList) evt.getSource();
                
                if (evt.getClickCount() == 2) { // double-click
                    int index = list_players.locationToIndex(evt.getPoint());
                    System.out.println(controller.getPlayers().get(index).getId());
                } 
            } 
        });
    }
    
    private void addPlayers(List<WikidataItem> players) {
        DefaultListModel<String> model = new DefaultListModel<>();
        
        for (WikidataItem player : players) {
            model.addElement(player.toString());
        }
        
        list_players.setModel(model);
    }
    /*
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
    }*/
    
    /**
     * Allows to stop the execution when an exception is thrown
     */
    private boolean stop = false;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_copy;
    private javax.swing.JCheckBox checkBox_header;
    private javax.swing.JTextField field_player_name;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel label_alert;
    private javax.swing.JLabel label_player_name;
    private javax.swing.JList<String> list_players;
    private javax.swing.JTextArea textArea_content;
    // End of variables declaration//GEN-END:variables
}
