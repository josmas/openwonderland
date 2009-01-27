/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.audiomanager.client;

import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.CellComponentProperties;
import org.jdesktop.wonderland.client.cell.properties.spi.CellComponentPropertiesSPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.audiomanager.common.ConeOfSilenceComponentServerState;

/**
 * Properties panel for the cone of silence component.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@CellComponentProperties
public class ConeOfSilenceComponentProperties extends javax.swing.JPanel implements CellComponentPropertiesSPI {
    private CellPropertiesEditor editor = null;
    private String originalName = null;
    private float originalFullVolumeRadius = 0.0f;
    private SpinnerNumberModel fullVolumeRadiusModel = null;

    /** Creates new form ConeOfSilenceComponentProperties */
    public ConeOfSilenceComponentProperties() {
        initComponents();

        // Set the maximum and minimum values for the volume radius spinner
        Float value = new Float(1);
        Float min = new Float(0);
        Float max = new Float(100);
        Float step = new Float(1);
        fullVolumeRadiusModel = new SpinnerNumberModel(value, min, max, step);
        fullVolumeRadiusSpinner.setModel(fullVolumeRadiusModel);

        // Listen for changes to the text field and spinner
        nameTextField.getDocument().addDocumentListener(new NameTextFieldListener());
        fullVolumeRadiusModel.addChangeListener(new RadiusChangeListener());

    }

    /**
     * @{inheritDoc}
     */
    public Class getServerCellComponentClass() {
        return ConeOfSilenceComponentServerState.class;
    }

    /**
     * @{inheritDoc}
     */
    public String getDisplayName() {
        return "Cone Of Silence";
    }

    /**
     * @{inheritDoc}
     */
    public JPanel getPropertiesJPanel(CellPropertiesEditor editor) {
        this.editor = editor;
        return this;
    }

    /**
     * @{inheritDoc}
     */
    public <T extends CellServerState> void updateGUI(T cellServerState) {
        CellComponentServerState state = cellServerState.getComponentServerState(ConeOfSilenceComponentServerState.class);
        if (state != null) {
            originalName = ((ConeOfSilenceComponentServerState)state).getName();
            originalFullVolumeRadius = ((ConeOfSilenceComponentServerState)state).getFullVolumeRadius();
            nameTextField.setText(originalName);
            fullVolumeRadiusSpinner.setValue(originalFullVolumeRadius);
            return;
        }
    }

    /**
     * @{inheritDoc}
     */
    public <T extends CellServerState> void getCellServerState(T cellServerState) {
        // Figure out whether there already exists a server state for the
        // component.
        CellComponentServerState state = cellServerState.getComponentServerState(ConeOfSilenceComponentServerState.class);
        if (state == null) {
            state = new ConeOfSilenceComponentServerState();
        }
        ((ConeOfSilenceComponentServerState)state).setName(nameTextField.getText());
        Float value = (Float)fullVolumeRadiusModel.getValue();
        ((ConeOfSilenceComponentServerState)state).setFullVolumeRadius(value);
        cellServerState.addComponentServerState(state);
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor
     */
    class RadiusChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            Float radius = (Float)fullVolumeRadiusModel.getValue();
            if (editor != null && radius != originalFullVolumeRadius) {
                editor.setPanelDirty(ConeOfSilenceComponentProperties.class, true);
            }
            else if (editor != null) {
                editor.setPanelDirty(ConeOfSilenceComponentProperties.class, false);
            }

        }
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor.
     */
    class NameTextFieldListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            checkDirty();
        }

        public void removeUpdate(DocumentEvent e) {
            checkDirty();
        }

        public void changedUpdate(DocumentEvent e) {
            checkDirty();
        }

        private void checkDirty() {
            String name = nameTextField.getText();
            if (editor != null && name.equals(originalName) == false) {
                editor.setPanelDirty(ConeOfSilenceComponentProperties.class, true);
            }
            else if (editor != null) {
                editor.setPanelDirty(ConeOfSilenceComponentProperties.class, false);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        fullVolumeRadiusSpinner = new javax.swing.JSpinner();

        jLabel1.setText("Name:");

        jLabel2.setText("Full Volume Radius:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .add(99, 99, 99)
                        .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .add(18, 18, 18)
                        .add(fullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(fullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(278, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner fullVolumeRadiusSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField nameTextField;
    // End of variables declaration//GEN-END:variables
}
