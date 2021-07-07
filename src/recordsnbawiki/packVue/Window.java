package recordsnbawiki.packVue;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
    private final Controller controller;

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
        
        label_alert.requestFocusInWindow();
        ToolTipManager.sharedInstance().setEnabled(false);
    }
    
    public void update(String code) {

        switch (code) {
            case "loading":
                info("Chargement ...");
                break;
            case "copy":
                addContentToClipboard();

                String text = "Contenu copié dans le presse-papier.";
                info(text);

                Timer t = new Timer(3000, (ActionEvent e) -> {
                    if (text.equals(label_alert.getText())) {
                        label_alert.setText(null);
                    }
                });
                t.setRepeats(false);
                t.start();
                break;
            case "errorRealGM":
                error("Le contenu de RealGM n'a pas pu être récupéré.");
                break;
            case "errorESPN":
                error("Le contenu d'ESPN n'a pas pu être récupéré.");
                break;
            case "errorNoPlayerRealGM":
                error("L'ID RealGM ne correspond à aucun joueur.");
                break;
            case "errorNoPlayerESPN":
                error("L'ID ESPN ne correspond à aucun joueur.");
                break;
            case "errorNeverPlayedInNBARealGM":
                error("Le joueur n'a jamais joué en NBA.");
                break;
            case "names incompatibility":
                displayIncompatibilityMessage();
                break;
            case "warningESPN":
                displayESPNWarningMessage();
                break;
            case "teams.jsonIssue":
                error("Le fichier teams.json est introuvable.");
                break;
            case "stats.jsonIssue":
                error("Le fichier stats.json est introuvable.");
                break;
            case "header_playoffs.txtIssue":
                error("Le fichier header_playoffs.txt est introuvable.");
                break;
            case "header_noplayoffs.txtIssue":
                error("Le fichier header_noplayoffs.txt est introuvable.");
                break;
            case "fileIssue":
                error("Une ressource n'a pas pu être récupérée.");
                break;
            case "wikidataIssue":
                error("Un problème est survenu avec Wikidata.");
                break;
            case "errorNoESPNId":
                error("Ce joueur n'a pas d'identifiant ESPN sur Wikidata.");
                break;
            case "errorNoRealGMId":
                error("Ce joueur n'a pas d'identifiant RealGM sur Wikidata.");
                break;
        }
    };

    private void info(String message) {
        label_alert.setText(message);
        label_alert.setForeground(Color.BLACK);
    }

    private void error(String message) {
        label_alert.setText(message);
        label_alert.setForeground(Color.RED);
        stop = true;
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

    private void clearTextArea() {
        textArea_content.setText("");
    }

    private void clearList() {
        DefaultListModel listmodel = new DefaultListModel();
        list_players.setModel(listmodel);
    }

    private void initTextField() {
        field_player_name.setText("Recherchez un joueur");
        field_player_name.setForeground(Color.LIGHT_GRAY);
    }

    /** 
     * Disable or able all components
     *
     * @param value - true to disable, false otherwise
     */
    private void disableComponents(boolean value) {
        button_copy.setEnabled(!value);
        textArea_content.setEnabled(!value);
        checkBox_header.setEnabled(!value);
        field_player_name.setEnabled(!value);
        list_players.setEnabled(!value);
    }

    /**
     * Add the content to the textArea
     *
     * @param player
     */
    private void addContentToTextArea(WikidataItem player) {
        this.update("loading");

        disableComponents(true); // on désactive les composants pendant le chargement
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        new Thread(() -> {
            try {
                
                controller.generateContent(player, checkBox_header.isSelected());
                
                if (!stop) {
                    textArea_content.setText(controller.getContent());
                    this.update("copy");
                    initTextField();
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
    private void submission(WikidataItem player) {
        clearTextArea();

        addContentToTextArea(player);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        field_player_name = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        list_players = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        textArea_content = new javax.swing.JTextArea();
        button_copy = new javax.swing.JButton();
        checkBox_header = new javax.swing.JCheckBox();
        label_alert = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Records joueur NBA");
        setLocation(new java.awt.Point(500, 600));
        setResizable(false);
        setSize(new java.awt.Dimension(500, 600));

        field_player_name.setFont(new java.awt.Font("Ubuntu", 0, 16)); // NOI18N
        field_player_name.setForeground(java.awt.Color.lightGray);
        field_player_name.setText("Recherchez un joueur");
        field_player_name.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.lightGray));
        field_player_name.setBorder(BorderFactory.createCompoundBorder(
            field_player_name.getBorder(),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    field_player_name.addFocusListener(new java.awt.event.FocusAdapter() {
        public void focusGained(java.awt.event.FocusEvent evt) {
            field_player_nameFocusGained(evt);
        }
        public void focusLost(java.awt.event.FocusEvent evt) {
            field_player_nameFocusLost(evt);
        }
    });

    list_players.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.lightGray));
    list_players.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    jScrollPane1.setViewportView(list_players);

    textArea_content.setEditable(false);
    textArea_content.setColumns(20);
    textArea_content.setRows(5);
    textArea_content.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.lightGray));
    jScrollPane2.setViewportView(textArea_content);

    button_copy.setText("Copier");

    checkBox_header.setSelected(true);
    checkBox_header.setText("En-tête");
    checkBox_header.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    checkBox_header.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            checkBox_headerActionPerformed(evt);
        }
    });

    label_alert.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    label_alert.setText(" ");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGap(50, 50, 50)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(button_copy)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(checkBox_header)
                    .addComponent(field_player_name)
                    .addComponent(jScrollPane2)
                    .addComponent(label_alert, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(50, 50, 50))
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGap(20, 20, 20)
            .addComponent(field_player_name, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(label_alert, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(checkBox_header)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(button_copy)
            .addContainerGap(40, Short.MAX_VALUE))
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void field_player_nameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_field_player_nameFocusLost
        if (field_player_name.getText().equals("")) {
            initTextField();            
        }
    }//GEN-LAST:event_field_player_nameFocusLost

    private void field_player_nameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_field_player_nameFocusGained
        if (field_player_name.getText().equals("Recherchez un joueur")) {
            field_player_name.setText("");
            field_player_name.setForeground(Color.BLACK);
        }
    }//GEN-LAST:event_field_player_nameFocusGained

    private void checkBox_headerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBox_headerActionPerformed
        if (!textArea_content.getText().isEmpty()) {
            JCheckBox cbLog = (JCheckBox) evt.getSource();
            if (cbLog.isSelected()) {
                controller.addHeader();
            } else {
                controller.removeHeader();
            }

            textArea_content.setText(controller.getContent());
            textArea_content.setCaretPosition(0);
            this.update("copy");
        }
    }//GEN-LAST:event_checkBox_headerActionPerformed
    
    private Thread searchThread;

    private void addListeners() {

        field_player_name.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { action(); }
            @Override public void removeUpdate(DocumentEvent e) { action(); }
            @Override public void changedUpdate(DocumentEvent e) { action(); }
             
            public void action() {

                String text = field_player_name.getText();

                if (text.length() >= 3 && !text.equals("Recherchez un joueur")) { // the text field contains three or more caracters, we load the players

                    if (searchThread != null) {
                        searchThread.interrupt();
                    }

                    searchThread = new Thread(() -> {
                        list_players.setEnabled(false);

                        controller.retrievePlayers(text);
                        addPlayers(controller.getPlayers());

                        list_players.setEnabled(true);
                    });

                    searchThread.start();
                }

                if (text.length() == 0) { // clear the list
                    clearList();
                }
            }
        });
                
        list_players.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list_players = (JList) evt.getSource();
                
                if (evt.getClickCount() == 2) { // double-click
                    int index = list_players.locationToIndex(evt.getPoint());
                    submission(controller.getPlayers().get(index));
                } 
            } 
        });
        
        list_players.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent evt) {
                
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) { // user presses enter key
                    int index = list_players.getSelectedIndex();
                    
                    if (index >= 0) { // to avoid -1 index
                        submission(controller.getPlayers().get(index));
                    }
                }
            }

            @Override public void keyTyped(KeyEvent evt) {}
            @Override public void keyReleased(KeyEvent evt) {}
        });
        
        button_copy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                update("copy");
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
    
    /**
     * Allows to stop the execution when an exception is thrown
     */
    private boolean stop = false;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_copy;
    private javax.swing.JCheckBox checkBox_header;
    private javax.swing.JTextField field_player_name;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel label_alert;
    private javax.swing.JList<String> list_players;
    private javax.swing.JTextArea textArea_content;
    // End of variables declaration//GEN-END:variables
}
