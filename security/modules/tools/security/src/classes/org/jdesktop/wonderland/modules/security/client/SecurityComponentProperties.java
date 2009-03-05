/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.security.client;

import java.awt.Component;
import java.util.Enumeration;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.CellComponentProperties;
import org.jdesktop.wonderland.client.cell.properties.spi.CellComponentPropertiesSPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.security.common.PermissionsSet;
import org.jdesktop.wonderland.modules.security.common.Principal;
import org.jdesktop.wonderland.modules.security.common.Principal.Type;
import org.jdesktop.wonderland.modules.security.common.SecurityComponentServerState;

/**
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@CellComponentProperties
public class SecurityComponentProperties extends JPanel 
        implements CellComponentPropertiesSPI
{
    private CellPropertiesEditor editor = null;
    private DefaultListModel owners;

    /** Creates new form SecurityComponentProperties */
    public SecurityComponentProperties() {
        // Initialize the GUI
        initComponents();

        ownersList.setCellRenderer(new PrincipalCellRender());
        ownersList.addListSelectionListener(
                            new PrincipalSelectionListener(ownersList,
                                                           ownersRemoveButton));

        addNameTF.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkButton();
            }

            public void removeUpdate(DocumentEvent e) {
                checkButton();
            }

            public void changedUpdate(DocumentEvent e) {
                checkButton();
            }

            private void checkButton() {
                String text = addNameTF.getText();
                addOKButton.setEnabled(text != null && text.length() > 0);
            }
        });
    }

    /**
     * @inheritDoc()
     */
    public Class getServerCellComponentClass() {
        return SecurityComponentServerState.class;
    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return "Cell Security Component";
    }

    /**
     * @inheritDoc()
     */
    public JPanel getPropertiesJPanel(CellPropertiesEditor editor) {
        this.editor = editor;
        return this;
    }

    /**
     * @inheritDoc()
     */
    public <T extends CellServerState> void updateGUI(T cellServerState) {
        SecurityComponentServerState state =  (SecurityComponentServerState)
                cellServerState.getComponentServerState(SecurityComponentServerState.class);

        // set the lists up based on the model
        owners = new DefaultListModel();
        for (Principal p : state.getPermissions().getOwners()) {
            owners.addElement(owners);
        }
        owners.addListDataListener(new ModelChangeListener());
        ownersList.setModel(owners);
    }

    /**
     * @inheritDoc()
     */
    public <T extends CellServerState> void getCellServerState(T cellServerState) {
        // Figure out whether there already exists a server state for the
        // component.
        SecurityComponentServerState state = (SecurityComponentServerState)
                cellServerState.getComponentServerState(SecurityComponentServerState.class);
        if (state == null) {
            state = new SecurityComponentServerState();
        }

        PermissionsSet perms = toPermissions();

        state.setPermissions(perms);
        cellServerState.addComponentServerState(state);
    }

    private PermissionsSet toPermissions() {
        PermissionsSet out = new PermissionsSet();

        for (Enumeration e = owners.elements(); e.hasMoreElements();) {
            Principal p = (Principal) e.nextElement();
            out.getOwners().add(p);
        }

        return out;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addPrincipalDialog = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        addUserRB = new javax.swing.JRadioButton();
        addGroupRB = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        addNameTF = new javax.swing.JTextField();
        addCancelButton = new javax.swing.JButton();
        addOKButton = new javax.swing.JButton();
        addSearchButton = new javax.swing.JButton();
        addBG = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ownersList = new javax.swing.JList();
        ownersAddButton = new javax.swing.JButton();
        ownersRemoveButton = new javax.swing.JButton();

        jLabel2.setText("Type:");

        addBG.add(addUserRB);
        addUserRB.setSelected(true);
        addUserRB.setText("User");

        addBG.add(addGroupRB);
        addGroupRB.setText("Group");

        jLabel3.setText("Name:");

        addCancelButton.setText("Cancel");
        addCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCancelButtonActionPerformed(evt);
            }
        });

        addOKButton.setText("OK");
        addOKButton.setEnabled(false);
        addOKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addOKButtonActionPerformed(evt);
            }
        });

        addSearchButton.setText("Search...");
        addSearchButton.setEnabled(false);

        org.jdesktop.layout.GroupLayout addPrincipalDialogLayout = new org.jdesktop.layout.GroupLayout(addPrincipalDialog.getContentPane());
        addPrincipalDialog.getContentPane().setLayout(addPrincipalDialogLayout);
        addPrincipalDialogLayout.setHorizontalGroup(
            addPrincipalDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(addPrincipalDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(addPrincipalDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addPrincipalDialogLayout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addUserRB)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addGroupRB)
                        .add(178, 178, 178))
                    .add(addPrincipalDialogLayout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addNameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addSearchButton)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, addPrincipalDialogLayout.createSequentialGroup()
                        .add(addOKButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addCancelButton))))
        );
        addPrincipalDialogLayout.setVerticalGroup(
            addPrincipalDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(addPrincipalDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(addPrincipalDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(addNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(addSearchButton))
                .add(6, 6, 6)
                .add(addPrincipalDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(addUserRB)
                    .add(addGroupRB))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(addPrincipalDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addCancelButton)
                    .add(addOKButton)))
        );

        jLabel1.setText("Owners:");

        ownersList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(ownersList);

        ownersAddButton.setText("Add...");
        ownersAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ownersAddButtonActionPerformed(evt);
            }
        });

        ownersRemoveButton.setText("Remove");
        ownersRemoveButton.setEnabled(false);
        ownersRemoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ownersRemoveButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 189, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(ownersRemoveButton)
                            .add(ownersAddButton)))
                    .add(jLabel1))
                .addContainerGap(192, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(ownersAddButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ownersRemoveButton))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(223, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ownersAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ownersAddButtonActionPerformed
        addNameTF.setText("");
        addUserRB.setSelected(true);
        addPrincipalDialog.setVisible(true);
    }//GEN-LAST:event_ownersAddButtonActionPerformed

    private void ownersRemoveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ownersRemoveButtonActionPerformed
        Principal p = (Principal) ownersList.getSelectedValue();
        if (p != null) {
            owners.removeElement(p);
        }
    }//GEN-LAST:event_ownersRemoveButtonActionPerformed

    private void addCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCancelButtonActionPerformed
        addPrincipalDialog.setVisible(false);
    }//GEN-LAST:event_addCancelButtonActionPerformed

    private void addOKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addOKButtonActionPerformed
        addPrincipalDialog.setVisible(false);

        Type type;
        if (addUserRB.isSelected()) {
            type = Type.USER;
        } else {
            type = Type.GROUP;
        }

        owners.addElement(new Principal(addNameTF.getText(), type));
    }//GEN-LAST:event_addOKButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup addBG;
    private javax.swing.JButton addCancelButton;
    private javax.swing.JRadioButton addGroupRB;
    private javax.swing.JTextField addNameTF;
    private javax.swing.JButton addOKButton;
    private javax.swing.JDialog addPrincipalDialog;
    private javax.swing.JButton addSearchButton;
    private javax.swing.JRadioButton addUserRB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton ownersAddButton;
    private javax.swing.JList ownersList;
    private javax.swing.JButton ownersRemoveButton;
    // End of variables declaration//GEN-END:variables

    class PrincipalCellRender extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, 
                            int index, boolean isSelected, boolean cellHasFocus) 
        {
            Principal p = (Principal) value;
            
            return super.getListCellRendererComponent(list, p.getId(), index, 
                                                      isSelected, cellHasFocus);
        }
    }
    
    class PrincipalSelectionListener implements ListSelectionListener {
        private JList list;
        private JButton removeButton;
        
        public PrincipalSelectionListener(JList list, JButton removeButton) {
            this.list = list;
            this.removeButton = removeButton;
        }
        
        public void valueChanged(ListSelectionEvent e) {
            boolean enabled = false;
            
            if (!e.getValueIsAdjusting()) {
                enabled = (list.getSelectedValue() != null);
            }
            
            removeButton.setEnabled(enabled);
        }        
    }
    
    class ModelChangeListener implements ListDataListener {
        public void intervalAdded(ListDataEvent e) {
            editor.setPanelDirty(SecurityComponentProperties.class, true);
        }

        public void intervalRemoved(ListDataEvent e) {
            editor.setPanelDirty(SecurityComponentProperties.class, true);        
        }

        public void contentsChanged(ListDataEvent e) {
            editor.setPanelDirty(SecurityComponentProperties.class, true);        
        }
    }
}
