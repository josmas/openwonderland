/*
 * AudioTreatmentComponentProperties.java
 *
 * Created on May 26, 2009, 2:55 PM
 */

package org.jdesktop.wonderland.modules.audiomanager.client;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentServerState;

import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.wonderland.client.cell.properties.annotation.CellComponentProperties;

import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;

import org.jdesktop.wonderland.client.cell.properties.spi.CellComponentPropertiesSPI;

import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

/**
 *
 * @author  jp
 */
@CellComponentProperties
public class AudioTreatmentComponentProperties extends javax.swing.JPanel implements CellComponentPropertiesSPI {

    private CellPropertiesEditor editor;
    private SpinnerNumberModel fullVolumeRadiusModel;
    private SpinnerNumberModel zeroVolumeRadiusModel;
    private SpinnerNumberModel volumeModel;

    private String originalGroupId = "";
    private String originalTreatments = "";
    private double originalFullVolumeRadius = 1.0F;
    private double originalZeroVolumeRadius = 2.0F;
    private double originalVolume = 1.0F;

    /** Creates new form AudioTreatmentComponentProperties */
    public AudioTreatmentComponentProperties() {
        initComponents();

        // Set the maximum and minimum values for the full volume radius spinner
        Double value = new Double(1);
        Double min = new Double(0);
        Double max = new Double(100);
        Double step = new Double(1);
        fullVolumeRadiusModel = new SpinnerNumberModel(value, min, max, step);
        fullVolumeRadiusSpinner.setModel(fullVolumeRadiusModel);

        // Set the maximum and minimum values for the zero volume radius spinner
        value = new Double(1);
        min = new Double(0);
        max = new Double(100);
        step = new Double(1);
        zeroVolumeRadiusModel = new SpinnerNumberModel(value, min, max, step);
        zeroVolumeRadiusSpinner.setModel(zeroVolumeRadiusModel);

        // Set the maximum and minimum values for the volume
        value = new Double(1);
        min = new Double(0);
        max = new Double(20);
        step = new Double(1);
        volumeModel = new SpinnerNumberModel(value, min, max, step);
        volumeSpinner.setModel(volumeModel);

        // Listen for changes to the text fields and spinners
	audioGroupIdTextField.getDocument().addDocumentListener(new AudioGroupTextFieldListener());
	audioTreatmentsTextField.getDocument().addDocumentListener(new AudioTreatmentsTextFieldListener());
        fullVolumeRadiusModel.addChangeListener(new FullVolumeRadiusChangeListener());
        zeroVolumeRadiusModel.addChangeListener(new ZeroVolumeRadiusChangeListener());
        volumeModel.addChangeListener(new VolumeChangeListener());
    }

    /**
     * @{inheritDoc}
     */
    public Class getServerCellComponentClass() {
        return AudioTreatmentComponentServerState.class;
    }

    /**
     * @{inheritDoc}
     */
    public String getDisplayName() {
        return "Audio Treatment Component";
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
        AudioTreatmentComponentServerState state = (AudioTreatmentComponentServerState)
	    cellServerState.getComponentServerState(AudioTreatmentComponentServerState.class);

        if (state != null) {
            originalGroupId = state.getGroupId();
	    audioGroupIdTextField.setText(originalGroupId);
	    
            String[] treatmentList = state.getTreatments();

	    for (int i = 0; i < treatmentList.length; i++) {
		originalTreatments += treatmentList[i] + " ";		
	    }

	    originalTreatments = originalTreatments.trim();
	    audioTreatmentsTextField.setText(originalTreatments);
	    
	    originalFullVolumeRadius = state.getFullVolumeRadius();
            fullVolumeRadiusSpinner.setValue(originalFullVolumeRadius);

	    originalZeroVolumeRadius = state.getZeroVolumeRadius();
            zeroVolumeRadiusSpinner.setValue(originalZeroVolumeRadius);

	    originalVolume = state.getVolume();
	    volumeSpinner.setValue(originalVolume);
            return;
        }
    }

    /**
     * @{inheritDoc}
     */
    public <T extends CellServerState> void getCellServerState(T cellServerState) {
        // Figure out whether there already exists a server state for the
        // component.
        AudioTreatmentComponentServerState state = (AudioTreatmentComponentServerState)
	    cellServerState.getComponentServerState(AudioTreatmentComponentServerState.class);

        if (state == null) {
            state = new AudioTreatmentComponentServerState();
        }

	state.setGroupId(audioGroupIdTextField.getText());
	
	String treatments = audioTreatmentsTextField.getText();

	treatments = treatments.replaceAll(",", " ");
	treatments = treatments.replaceAll("  ", " ");

	//System.out.println("Treatments:  " + treatments + " size " + treatments.split(" ").length);

	state.setTreatments(treatments.split(" "));

        Double value = (Double) fullVolumeRadiusModel.getValue();
        state.setFullVolumeRadius(value);

        value = (Double) zeroVolumeRadiusModel.getValue();
        state.setZeroVolumeRadius(value);

        value = (Double) volumeModel.getValue();
        state.setVolume(value);

        cellServerState.addComponentServerState(state);
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor.
     */
    class AudioGroupTextFieldListener implements DocumentListener {
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
            String audioGroupId = audioGroupIdTextField.getText();

            if (editor != null) { 
		if (audioGroupId.equals(originalGroupId) == false) {
                    editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
                } else {
                    editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
		}
            }
        }
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor.
     */
    class AudioTreatmentsTextFieldListener implements DocumentListener {
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
            String treatments = audioTreatmentsTextField.getText();

	    //System.out.println("treatments " + treatments + " original " + originalTreatments);

            if (editor != null) { 
		if (treatments.equals(originalTreatments) == false) {
                    editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
                } else {
                    editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
		}
            }
        }
    }
    class FullVolumeRadiusChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            Double radius = (Double) fullVolumeRadiusModel.getValue();
            if (editor != null) { 
		if (radius != originalFullVolumeRadius) {
                    editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
		} else {
                    editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
		}
            }
        }
    }

    class ZeroVolumeRadiusChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            Double radius = (Double) zeroVolumeRadiusModel.getValue();
            if (editor != null) { 
		if (radius != originalZeroVolumeRadius) {
                    editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
		} else {
                    editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
		}
            }
        }
    }

    class VolumeChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            Double volume = (Double) volumeModel.getValue();
            if (editor != null) { 
		if (volume != originalVolume) {
                    editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
		} else {
                    editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
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
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        audioTreatmentsTextField = new javax.swing.JTextField();
        falloffComboBox = new javax.swing.JComboBox();
        proximityTriggersComboBox = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        audioGroupIdTextField = new javax.swing.JTextField();
        fullVolumeRadiusSpinner = new javax.swing.JSpinner();
        zeroVolumeRadiusSpinner = new javax.swing.JSpinner();
        volumeSpinner = new javax.swing.JSpinner();

        jLabel1.setText("Audio Treatments:");

        jLabel2.setText("Volume:");

        jLabel3.setText("Full Volume Radius:");

        jLabel4.setText("Falloff:");

        jLabel5.setText("Proximity Triggers:");

        falloffComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        proximityTriggersComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel6.setText("Zero Volume Radius:");

        jLabel7.setText("Audio Group Id:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 119, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(26, 26, 26))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                                .add(layout.createSequentialGroup()
                                    .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                            .add(layout.createSequentialGroup()
                                .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(volumeSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                            .add(audioTreatmentsTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, audioGroupIdTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, falloffComboBox, 0, 223, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, proximityTriggersComboBox, 0, 223, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, fullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE))
                        .add(20, 20, 20)))
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 145, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(zeroVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)))
                .add(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel7)
                    .add(audioGroupIdTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(audioTreatmentsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(15, 15, 15)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(fullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(zeroVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(falloffComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(proximityTriggersComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(volumeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .add(60, 60, 60))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField audioGroupIdTextField;
    private javax.swing.JTextField audioTreatmentsTextField;
    private javax.swing.JComboBox falloffComboBox;
    private javax.swing.JSpinner fullVolumeRadiusSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JComboBox proximityTriggersComboBox;
    private javax.swing.JSpinner volumeSpinner;
    private javax.swing.JSpinner zeroVolumeRadiusSpinner;
    // End of variables declaration//GEN-END:variables

}
