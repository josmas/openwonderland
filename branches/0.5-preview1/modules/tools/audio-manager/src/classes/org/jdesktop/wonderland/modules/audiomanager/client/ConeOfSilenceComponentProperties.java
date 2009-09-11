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
package org.jdesktop.wonderland.modules.audiomanager.client;

import java.util.ResourceBundle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.audiomanager.common.ConeOfSilenceComponentServerState;
import org.jdesktop.wonderland.modules.audiomanager.common.VolumeUtil;

/**
 * Properties panel for the cone of silence component.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
@PropertiesFactory(ConeOfSilenceComponentServerState.class)
public class ConeOfSilenceComponentProperties extends javax.swing.JPanel
        implements PropertiesFactorySPI {

    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle");
    private CellPropertiesEditor editor = null;
    private String originalName = null;
    private boolean originalUseCellBounds = true;
    private double originalFullVolumeRadius = 0.0;
    private int originalOutsideAudioVolume = 0;
    private SpinnerNumberModel fullVolumeRadiusModel = null;

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

        // Listen for changes to the text field and spinner
        nameTextField.getDocument().addDocumentListener(
                new NameTextFieldListener());

	useCellBoundsRadioButton.addChangeListener(new UseCellBoundsChangeListener());
        fullVolumeRadiusModel.addChangeListener(new RadiusChangeListener());
    }

    /**
     * @{inheritDoc}
     */
    public String getDisplayName() {
        return BUNDLE.getString("Cone_of_Silence");
    }

    /**
     * @{inheritDoc}
     */
    public JPanel getPropertiesJPanel() {
        return this;
    }

    /**
     * @{inheritDoc}
     */
    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }

    /**
     * @{inheritDoc}
     */
    public void open() {
        CellServerState cellServerState = editor.getCellServerState();
        ConeOfSilenceComponentServerState state =
                (ConeOfSilenceComponentServerState) cellServerState.getComponentServerState(
                ConeOfSilenceComponentServerState.class);

        if (state == null) {
            return;
        }

        originalName = state.getName();
	originalUseCellBounds = state.getUseCellBounds();
        originalFullVolumeRadius = state.getFullVolumeRadius();
        originalOutsideAudioVolume = VolumeUtil.getClientVolume(
                state.getOutsideAudioVolume());

        nameTextField.setText(originalName);
        outsideAudioVolumeSlider.setValue(originalOutsideAudioVolume);
        fullVolumeRadiusSpinner.setValue(originalFullVolumeRadius);
    }

    /**
     * @{inheritDoc}
     */
    public void close() {
        // Do nothing
    }

    /**
     * @{inheritDoc}
     */
    public void apply() {
        // Figure out whether there already exists a server state for the
        // component. If it does not exist, then return, but we could always
        // create a new one really.
        CellServerState cellServerState = editor.getCellServerState();
        ConeOfSilenceComponentServerState state =
                (ConeOfSilenceComponentServerState) cellServerState.getComponentServerState(
                ConeOfSilenceComponentServerState.class);
        if (state == null) {
            return;
        }

        state.setName(nameTextField.getText());
	state.setUseCellBounds(useCellBoundsRadioButton.isSelected());
        state.setFullVolumeRadius((Double) fullVolumeRadiusModel.getValue());
        state.setOutsideAudioVolume(VolumeUtil.getServerVolume(
                outsideAudioVolumeSlider.getValue()));
        editor.addToUpdateList(state);
    }

    /**
     * @{inheritDoc}
     */
    public void restore() {
        // Reset the original values to the GUI
        nameTextField.setText(originalName);
	useCellBoundsRadioButton.setSelected(originalUseCellBounds);
        outsideAudioVolumeSlider.setValue(originalOutsideAudioVolume);
        fullVolumeRadiusSpinner.setValue(originalFullVolumeRadius);
    }

    /**
     * Inner class to listen for changes to the use cell bounds radio button 
     * and fire off dirty or clean indications to the cell properties editor
     */
    class UseCellBoundsChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
		boolean useCellBounds = useCellBoundsRadioButton.isSelected();

                editor.setPanelDirty(ConeOfSilenceComponentProperties.class,
                    useCellBounds != originalUseCellBounds);
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class RadiusChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                Double radius = (Double) fullVolumeRadiusModel.getValue();
                editor.setPanelDirty(ConeOfSilenceComponentProperties.class,
                        radius != originalFullVolumeRadius);
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
            if (editor != null) {
                editor.setPanelDirty(ConeOfSilenceComponentProperties.class,
                        !nameTextField.getText().equals(originalName));
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        fullVolumeRadiusSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        outsideAudioVolumeSlider = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        useCellBoundsRadioButton = new javax.swing.JRadioButton();
        specifyRadiusRadioButton = new javax.swing.JRadioButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("ConeOfSilenceComponentProperties.jLabel1.text")); // NOI18N

        fullVolumeRadiusSpinner.setEnabled(false);

        jLabel3.setText(bundle.getString("ConeOfSilenceComponentProperties.jLabel3.text")); // NOI18N

        outsideAudioVolumeSlider.setMajorTickSpacing(1);
        outsideAudioVolumeSlider.setMaximum(10);
        outsideAudioVolumeSlider.setPaintLabels(true);
        outsideAudioVolumeSlider.setPaintTicks(true);
        outsideAudioVolumeSlider.setSnapToTicks(true);
        outsideAudioVolumeSlider.setValue(0);
        outsideAudioVolumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                outsideAudioVolumeSliderStateChanged(evt);
            }
        });

        jLabel4.setText(bundle.getString("ConeOfSilenceComponentProperties.jLabel4.text")); // NOI18N

        buttonGroup1.add(useCellBoundsRadioButton);
        useCellBoundsRadioButton.setSelected(true);
        useCellBoundsRadioButton.setText(bundle.getString("ConeOfSilenceComponentProperties.useCellBoundsRadioButton.text")); // NOI18N
        useCellBoundsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useCellBoundsRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(specifyRadiusRadioButton);
        specifyRadiusRadioButton.setText(bundle.getString("ConeOfSilenceComponentProperties.specifyRadiusRadioButton.text")); // NOI18N
        specifyRadiusRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specifyRadiusRadioButtonActionPerformed(evt);
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
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel1)
                            .add(jLabel4))
                        .add(51, 51, 51)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                            .add(useCellBoundsRadioButton)
                            .add(layout.createSequentialGroup()
                                .add(specifyRadiusRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(fullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jLabel3)
                        .add(18, 18, 18)
                        .add(outsideAudioVolumeSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .add(21, 21, 21)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(useCellBoundsRadioButton))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(specifyRadiusRadioButton)
                    .add(fullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(18, 18, 18)
                        .add(outsideAudioVolumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(34, 34, 34)
                        .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void outsideAudioVolumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_outsideAudioVolumeSliderStateChanged
        if (editor != null) {
            int outsideAudioVolume = outsideAudioVolumeSlider.getValue();
            editor.setPanelDirty(ConeOfSilenceComponentProperties.class,
                    originalOutsideAudioVolume != outsideAudioVolume);
        }
    }//GEN-LAST:event_outsideAudioVolumeSliderStateChanged

    private void useCellBoundsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useCellBoundsRadioButtonActionPerformed
	fullVolumeRadiusSpinner.setEnabled(false);
    }//GEN-LAST:event_useCellBoundsRadioButtonActionPerformed

    private void specifyRadiusRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specifyRadiusRadioButtonActionPerformed
	fullVolumeRadiusSpinner.setEnabled(true);
    }//GEN-LAST:event_specifyRadiusRadioButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JSpinner fullVolumeRadiusSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JSlider outsideAudioVolumeSlider;
    private javax.swing.JRadioButton specifyRadiusRadioButton;
    private javax.swing.JRadioButton useCellBoundsRadioButton;
    // End of variables declaration//GEN-END:variables
}
