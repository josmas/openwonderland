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
    private double originalFullVolumeRadius = 0.0;
    private double originalOutsideAudioVolume = 0.0;
    private SpinnerNumberModel fullVolumeRadiusModel = null;
    private SpinnerNumberModel outsideAudioVolumeModel = null;

    /** Creates new form ConeOfSilenceComponentProperties */
    public ConeOfSilenceComponentProperties() {
        initComponents();

        // Set the maximum and minimum values for the volume radius spinner
        Double value = new Double(1);
        Double min = new Double(0);
        Double max = new Double(100);
        Double step = new Double(1);
        fullVolumeRadiusModel = new SpinnerNumberModel(value, min, max, step);
        fullVolumeRadiusSpinner.setModel(fullVolumeRadiusModel);

        // Set the maximum and minimum values for the outside volume spinner
        value = new Double(1);
        min = new Double(0);
        max = new Double(20);
        step = new Double(1);
        outsideAudioVolumeModel = new SpinnerNumberModel(value, min, max, step);
        outsideAudioVolumeSpinner.setModel(outsideAudioVolumeModel);

        // Listen for changes to the text field and spinner
        nameTextField.getDocument().addDocumentListener(new NameTextFieldListener());
        fullVolumeRadiusModel.addChangeListener(new RadiusChangeListener());
        outsideAudioVolumeModel.addChangeListener(new OutsideAudioVolumeChangeListener());
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
        ConeOfSilenceComponentServerState state = (ConeOfSilenceComponentServerState)
	    cellServerState.getComponentServerState(ConeOfSilenceComponentServerState.class);

        if (state != null) {
            originalName = state.getName();
            originalFullVolumeRadius = state.getFullVolumeRadius();
	    originalOutsideAudioVolume = state.getOutsideAudioVolume();
	    
            nameTextField.setText(originalName);
            fullVolumeRadiusSpinner.setValue(originalFullVolumeRadius);
            outsideAudioVolumeSpinner.setValue(originalOutsideAudioVolume);
            return;
        }
    }

    /**
     * @{inheritDoc}
     */
    public <T extends CellServerState> void getCellServerState(T cellServerState) {
        // Figure out whether there already exists a server state for the
        // component.
        ConeOfSilenceComponentServerState state = (ConeOfSilenceComponentServerState)
	    cellServerState.getComponentServerState(ConeOfSilenceComponentServerState.class);

        if (state == null) {
            state = new ConeOfSilenceComponentServerState();
        }
        state.setName(nameTextField.getText());
        state.setFullVolumeRadius((Double) fullVolumeRadiusModel.getValue());
        state.setOutsideAudioVolume((Double) outsideAudioVolumeModel.getValue());

        cellServerState.addComponentServerState(state);
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor
     */
    class RadiusChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            Double radius = (Double) fullVolumeRadiusModel.getValue();
            if (editor != null) { 
		if (radius != originalFullVolumeRadius) {
                    editor.setPanelDirty(ConeOfSilenceComponentProperties.class, true);
                } else {
                    editor.setPanelDirty(ConeOfSilenceComponentProperties.class, false);
		}
            }
        }
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor
     */
    class OutsideAudioVolumeChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            Double volume = (Double) outsideAudioVolumeModel.getValue();
            if (editor != null) { 
		if (volume != originalOutsideAudioVolume) {
                    editor.setPanelDirty(ConeOfSilenceComponentProperties.class, true);
                } else {
                    editor.setPanelDirty(ConeOfSilenceComponentProperties.class, false);
		}
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
            if (editor != null) { 
		if (name.equals(originalName) == false) {
                    editor.setPanelDirty(ConeOfSilenceComponentProperties.class, true);
                } else {
                    editor.setPanelDirty(ConeOfSilenceComponentProperties.class, false);
		}
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
        outsideAudioVolumeSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();

        jLabel1.setText("Name:");

        jLabel2.setText("Full Volume Radius:");

        jLabel3.setText("Outside Audio Volume:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(30, 30, 30)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel1)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 146, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, outsideAudioVolumeSpinner)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, fullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .add(27, 27, 27)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .add(27, 27, 27)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(outsideAudioVolumeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addContainerGap(202, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner fullVolumeRadiusSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JSpinner outsideAudioVolumeSpinner;
    // End of variables declaration//GEN-END:variables
}
