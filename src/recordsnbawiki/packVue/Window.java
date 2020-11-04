package recordsnbawiki.packVue;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.border.Border;
import static org.jsoup.internal.StringUtil.isNumeric;
import recordsnbawiki.packLogic.Controller;
import recordsnbawiki.packLogic.DataManagement;

/**
 *
 * @author Jorick
 */
public class Window extends JFrame implements Observer {

    /**
     * Controller
     */
    private Controller controller;

    /**
     * Model
     */
    private DataManagement dataManagement;

    /**
     * Creates new form Window
     */
    public Window() {
        initComponents();

        this.dataManagement = new DataManagement();
        this.controller = new Controller(dataManagement);

        this.controller.addObservateur(this);

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    @Override
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
            case "loading":
                label_alert.setText("Chargement ...");
                label_alert.setForeground(Color.BLACK);
                break;
            case "copyContent":
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
        }
    }

    /**
     * Clean the textArea and init fields border
     */
    private void resetComponents() {
        textField_RealGM.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        textField_ESPN.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        textArea_content.setText(""); // clear the textArea
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
        controller.notifyObservateurs("loading");

        disableComponents(true); // on désactive les composants pendant le chargement
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        new Thread(new Runnable() {
            public void run() {
                try {

                    controller.generateContent(RealGM_id, ESPN_id, checkBox_header.isSelected());

                    if (!stop) {
                        textArea_content.setText(dataManagement.getFinalContent());
                        controller.notifyObservateurs("copyContent");
                    }

                    disableComponents(false);
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    stop = false;

                } catch (Exception e) {
                }
            }
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

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
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
        resetComponents();

        String RealGM_id = "";
        String ESPN_id = "";

        boolean entriesAreValid = true;

        Border redBorder = BorderFactory.createLineBorder(Color.RED, 1);

        if (textField_RealGM.getText().length() > 0) {
            RealGM_id = textField_RealGM.getText();
        } else { // le field est vide
            textField_RealGM.setBorder(redBorder);
            entriesAreValid = false;
        }

        if (textField_ESPN.getText().length() > 0) {
            ESPN_id = textField_ESPN.getText();
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
                addContentToTextArea(Integer.parseInt(RealGM_id), Integer.parseInt(ESPN_id));
            } else {
                controller.notifyObservateurs("invalidEntry");
            }
        } else {
            controller.notifyObservateurs("fieldsNotFilled");
        }

    }//GEN-LAST:event_button_submitActionPerformed

    private void button_copyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_copyActionPerformed
        controller.notifyObservateurs("copyContent");
    }//GEN-LAST:event_button_copyActionPerformed

    /**
     * Allows to stop the execution when an exception is thrown
     */
    private boolean stop = false;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_copy;
    private javax.swing.JButton button_submit;
    private javax.swing.JCheckBox checkBox_header;
    private javax.swing.JPopupMenu jPopupMenu1;
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
