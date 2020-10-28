package recordsnbawiki.packVue;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.border.Border;
import recordsnbawiki.packLogic.Controller;
import recordsnbawiki.packLogic.DataManagement;

/**
 *
 * @author Jorick
 */
public class Window extends JFrame implements Observer {

    private Controller controller;
    
    private DataManagement dataManagement;
    
    /**
     * Creates new form Window
     * @param controller
     */
    public Window(Controller controller) {
        initComponents();
        
        this.controller = controller;
        
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    @Override
    public void update(String code) {
        
        switch (code) {
            case "entriesAreInvalid":
                label_alert.setText("Tous les champs ne sont pas remplis");
                label_alert.setForeground(Color.RED);
                label_alert.setHorizontalAlignment(JLabel.CENTER);
                break;
        }
    }

    private void initTextFields() {
        textField_RealGM.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        textField_ESPN.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panel1.setPreferredSize(new java.awt.Dimension(300, 122));

        textField_RealGM.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        textField_RealGM.setPreferredSize(new java.awt.Dimension(250, 20));

        textField_ESPN.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        textField_ESPN.setPreferredSize(new java.awt.Dimension(250, 20));

        label_RealGM.setText("Identifiant RealGM :");

        label_ESPN.setText("Identifiant ESPN :");

        button_submit.setText("Valider");
        button_submit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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
                .addGap(69, 69, 69))
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
                .addContainerGap(14, Short.MAX_VALUE))
        );

        textArea_content.setColumns(20);
        textArea_content.setRows(5);
        jScrollPane1.setViewportView(textArea_content);

        label_alert.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(100, 100, 100)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(label_alert, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(50, 50, 50))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(label_alert, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void button_submitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_submitActionPerformed
        initTextFields();
        
        String RealGM_id = "";
        String ESPN_id = "";
        
        boolean entriesAreValid = true;
        
        Border redBorder = BorderFactory.createLineBorder(Color.RED, 1);
        
        if (textField_RealGM.getText().length() > 0) {
            RealGM_id = textField_RealGM.getText();
        } else {
            textField_RealGM.setBorder(redBorder);
            entriesAreValid = false;
        }
       
        if (textField_ESPN.getText().length() > 0) {
            RealGM_id = textField_ESPN.getText();
        } else {
            textField_ESPN.setBorder(redBorder);
            entriesAreValid = false;
        }
        
        if (entriesAreValid) {
            this.dataManagement = new DataManagement(Integer.parseInt(RealGM_id), Integer.parseInt(ESPN_id));
        
            this.textArea_content.setText(dataManagement.getFinalContent());
        } else {
            update("entriesAreInvalid");
        }
        
    }//GEN-LAST:event_button_submitActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_submit;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel label_ESPN;
    private javax.swing.JLabel label_RealGM;
    private javax.swing.JLabel label_alert;
    private java.awt.Panel panel1;
    private javax.swing.JTextArea textArea_content;
    private javax.swing.JTextField textField_ESPN;
    private javax.swing.JTextField textField_RealGM;
    // End of variables declaration//GEN-END:variables
}
