/*
 * AudioTreatmentComponentProperties.java
 *
 * Created on May 26, 2009, 2:55 PM
 */

package org.jdesktop.wonderland.modules.audiomanager.client;

import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentServerState;
import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentServerState.PlayWhen;

import javax.swing.JFileChooser;
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

    private String originalGroupId;
    private String originalTreatments;
    private double originalVolume;
    private PlayWhen originalPlayWhen;
    private double originalExtentRadius;
    private double originalFullVolumeAreaPercent;
    private boolean originalDistanceAttenuated;
    private int originalFalloff;

    private SpinnerNumberModel fullVolumeAreaPercentModel;
    private SpinnerNumberModel extentRadiusModel;
    private SpinnerNumberModel volumeModel;

    private double extentRadius = 0;

    private PlayWhen playWhen;
    
    private boolean distanceAttenuated;

    /** Creates new form AudioTreatmentComponentProperties */
    public AudioTreatmentComponentProperties() {
        initComponents();

        // Set the maximum and minimum values for the spinners

        Double value = new Double(25);
        Double min = new Double(0);
        Double max = new Double(100);
        Double step = new Double(1);
        fullVolumeAreaPercentModel = new SpinnerNumberModel(value, min, max, step);
        fullVolumeAreaPercentSpinner.setModel(fullVolumeAreaPercentModel);

        value = new Double(10);
        min = new Double(0);
        max = new Double(100);
        step = new Double(1);
        extentRadiusModel = new SpinnerNumberModel(value, min, max, step);
        extentRadiusSpinner.setModel(extentRadiusModel);

        value = new Double(1);
        min = new Double(0);
        max = new Double(20);
        step = new Double(1);
        volumeModel = new SpinnerNumberModel(value, min, max, step);
        volumeSpinner.setModel(volumeModel);

        // Listen for changes to the text fields and spinners
	audioGroupIdTextField.getDocument().addDocumentListener(new AudioGroupTextFieldListener());
	fileTextField.getDocument().addDocumentListener(new AudioTreatmentsTextFieldListener());
	urlTextField.getDocument().addDocumentListener(new AudioTreatmentsTextFieldListener());
        fullVolumeAreaPercentModel.addChangeListener(new FullVolumeAreaPercentChangeListener());
        extentRadiusModel.addChangeListener(new ExtentRadiusChangeListener());
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

	    originalTreatments = "";

	    for (int i = 0; i < treatmentList.length; i++) {
		originalTreatments += treatmentList[i] + " ";		
	    }

	    originalTreatments = originalTreatments.trim();
	    fileTextField.setText(originalTreatments);
	    
	    originalVolume = state.getVolume();
	    volumeSpinner.setValue(originalVolume);

	    originalPlayWhen = state.getPlayWhen();
	    playWhen = originalPlayWhen;
	    
	    switch (originalPlayWhen) { 
	    case ALWAYS:
		alwaysRadioButton.setSelected(true);
		break;

	    case FIRST_IN_RANGE:
		proximityRadioButton.setSelected(true);	
		break;

	    case MANUAL:
		manualRadioButton.setSelected(true);
		break;
	    }

	    originalExtentRadius = state.getExtent();
	    extentRadius = originalExtentRadius;
            extentRadiusSpinner.setValue(originalExtentRadius);

	    if (extentRadius != 0) {
	        extentRadiusRadioButton.setSelected(true);
                extentRadiusSpinner.setEnabled(true);
		extentRadiusSpinner.setValue((Double) extentRadius);
	    } else {
		entireCellRadioButton.setSelected(true);
                extentRadiusSpinner.setEnabled(false);
	    }

	    originalFullVolumeAreaPercent = state.getFullVolumeAreaPercent();
            fullVolumeAreaPercentSpinner.setValue(originalFullVolumeAreaPercent);

	    originalDistanceAttenuated = state.getDistanceAttenuated();
	    distanceAttenuated = originalDistanceAttenuated;
	    distanceAttenuatedRadioButton.setSelected(originalDistanceAttenuated);

	    originalFalloff = (int) state.getFalloff();
	    falloffSlider.setValue(originalFalloff);

	    if (originalDistanceAttenuated == true) {
		falloffSlider.setEnabled(true);
	    } else {
		falloffSlider.setEnabled(false);
	    }
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
	
	String treatments = fileTextField.getText();

	treatments = treatments.replaceAll(",", " ");
	treatments = treatments.replaceAll("  ", " ");

	String urls = urlTextField.getText();

	urls = urls.replaceAll(",", " ");
	urls = urls.replaceAll("  ", " ");

	if (urls.length() > 0) {
	    if (treatments.length() > 0) {
		treatments += " " + urls.split(" ");
	    } else {
		treatments = urls;
	    }
	}

	state.setTreatments(treatments.split(" "));

        state.setVolume((Double) volumeModel.getValue());

	state.setPlayWhen(playWhen);

	state.setExtent((Double) extentRadiusModel.getValue());

        state.setFullVolumeAreaPercent((Double) fullVolumeAreaPercentModel.getValue());

	state.setDistanceAttenuated(distanceAttenuated);

	state.setFalloff(falloffSlider.getValue());

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
            String treatments = fileTextField.getText();

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

    class FullVolumeAreaPercentChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            Double fullVolumeAreaPercent = (Double) fullVolumeAreaPercentModel.getValue();
            if (editor != null) { 
		if (fullVolumeAreaPercent != originalFullVolumeAreaPercent) {
                    editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
		} else {
                    editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
		}
            }
        }
    }

    class ExtentRadiusChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            Double extentRadius = (Double) extentRadiusModel.getValue();
            if (editor != null) { 
		if (extentRadius != originalExtentRadius) {
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        audioGroupIdTextField = new javax.swing.JTextField();
        volumeSpinner = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();
        urlTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        alwaysRadioButton = new javax.swing.JRadioButton();
        proximityRadioButton = new javax.swing.JRadioButton();
        manualRadioButton = new javax.swing.JRadioButton();
        jLabel11 = new javax.swing.JLabel();
        entireCellRadioButton = new javax.swing.JRadioButton();
        extentRadiusRadioButton = new javax.swing.JRadioButton();
        extentRadiusSpinner = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        fullVolumeAreaPercentSpinner = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        ambientRadioButton = new javax.swing.JRadioButton();
        distanceAttenuatedRadioButton = new javax.swing.JRadioButton();
        falloffSlider = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        jLabel1.setText("Play:");

        jLabel2.setText("Volume:");

        jLabel7.setText("Audio Group Id:");

        volumeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                volumeSpinnerStateChanged(evt);
            }
        });

        jLabel8.setText("File:");

        jLabel9.setText("URL:");

        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        jLabel10.setText("Play when:");

        buttonGroup1.add(alwaysRadioButton);
        alwaysRadioButton.setText("Always");
        alwaysRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alwaysRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(proximityRadioButton);
        proximityRadioButton.setText("First avatar is in range");
        proximityRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proximityRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(manualRadioButton);
        manualRadioButton.setText("Manually started from the context menu");
        manualRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualRadioButtonActionPerformed(evt);
            }
        });

        jLabel11.setText("Extent:");

        buttonGroup2.add(entireCellRadioButton);
        entireCellRadioButton.setText("Entire cell");
        entireCellRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entireCellRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(extentRadiusRadioButton);
        extentRadiusRadioButton.setText("Circular area with radius");
        extentRadiusRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extentRadiusRadioButtonActionPerformed(evt);
            }
        });

        extentRadiusSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                extentRadiusSpinnerStateChanged(evt);
            }
        });

        jLabel12.setText("Full volume =");

        fullVolumeAreaPercentSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fullVolumeAreaPercentSpinnerStateChanged(evt);
            }
        });

        jLabel13.setText("% of area from center");

        jLabel14.setText("Audio characteristics:");

        buttonGroup3.add(ambientRadioButton);
        ambientRadioButton.setText("Ambient");
        ambientRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ambientRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup3.add(distanceAttenuatedRadioButton);
        distanceAttenuatedRadioButton.setText("Distance attenuated");
        distanceAttenuatedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distanceAttenuatedRadioButtonActionPerformed(evt);
            }
        });

        falloffSlider.setMajorTickSpacing(10);
        falloffSlider.setPaintTicks(true);
        falloffSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                falloffSliderStateChanged(evt);
            }
        });

        jLabel3.setText("Falloff:");

        jLabel4.setText("Slow");

        jLabel5.setText("Fast");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel10)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(entireCellRadioButton)
                            .add(layout.createSequentialGroup()
                                .add(extentRadiusRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(extentRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(manualRadioButton)
                            .add(proximityRadioButton)
                            .add(alwaysRadioButton))
                        .add(70, 70, 70))
                    .add(layout.createSequentialGroup()
                        .add(jLabel11)
                        .addContainerGap(392, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel12)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fullVolumeAreaPercentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel13)
                        .addContainerGap(110, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel14)
                            .add(layout.createSequentialGroup()
                                .add(55, 55, 55)
                                .add(jLabel3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 14, Short.MAX_VALUE)
                                .add(jLabel4)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(falloffSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(4, 4, 4)
                                .add(jLabel5))
                            .add(layout.createSequentialGroup()
                                .add(ambientRadioButton)
                                .add(18, 18, 18)
                                .add(distanceAttenuatedRadioButton)))
                        .addContainerGap(35, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel9)
                                    .add(jLabel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(volumeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(267, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(audioGroupIdTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                                    .add(urlTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                                    .add(layout.createSequentialGroup()
                                        .add(fileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)))
                                .add(12, 12, 12)
                                .add(browseButton)
                                .add(49, 49, 49))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(audioGroupIdTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jLabel8)
                    .add(browseButton)
                    .add(fileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(volumeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(alwaysRadioButton)
                    .add(jLabel10))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(proximityRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(manualRadioButton)
                .add(12, 12, 12)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(entireCellRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(extentRadiusRadioButton)
                    .add(extentRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(fullVolumeAreaPercentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel13))
                .add(26, 26, 26)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel14)
                    .add(ambientRadioButton)
                    .add(distanceAttenuatedRadioButton))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel3)
                        .add(jLabel4))
                    .add(falloffSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addContainerGap(32, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void extentRadiusRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extentRadiusRadioButtonActionPerformed
    extentRadiusSpinner.setEnabled(true);

    extentRadius = (Double) extentRadiusModel.getValue();

    if (editor == null) { 
	return;
    }

    if (extentRadius != originalExtentRadius) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
    } else {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
    }
}//GEN-LAST:event_extentRadiusRadioButtonActionPerformed

private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
    JFileChooser chooser = new JFileChooser(fileTextField.getText());

     int returnVal = chooser.showOpenDialog(this);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
	fileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
    }
}//GEN-LAST:event_browseButtonActionPerformed

private void volumeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeSpinnerStateChanged
    if (editor == null) { 
	return;
    }

    if (((Double) volumeModel.getValue()) != originalVolume) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
    } else {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
    }
}//GEN-LAST:event_volumeSpinnerStateChanged

private void alwaysRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alwaysRadioButtonActionPerformed
    playWhen = PlayWhen.ALWAYS;

    if (editor == null) { 
	return;
    }

    if (playWhen != originalPlayWhen) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
    } else {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
    }
}//GEN-LAST:event_alwaysRadioButtonActionPerformed

private void proximityRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proximityRadioButtonActionPerformed
    playWhen = PlayWhen.FIRST_IN_RANGE;

    if (editor == null) { 
	return;
    }

    if (playWhen != originalPlayWhen) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
    } else {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
    }
}//GEN-LAST:event_proximityRadioButtonActionPerformed

private void manualRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualRadioButtonActionPerformed
    playWhen = PlayWhen.MANUAL;

    if (editor == null) { 
	return;
    }

    if (playWhen != originalPlayWhen) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
    } else {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
    }
}//GEN-LAST:event_manualRadioButtonActionPerformed

private void entireCellRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entireCellRadioButtonActionPerformed
    extentRadiusSpinner.setEnabled(false);
    extentRadius = 0;

    if (editor == null) {
        return;
    }

    if (extentRadius != originalExtentRadius) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
    } else {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
    }
}//GEN-LAST:event_entireCellRadioButtonActionPerformed

private void extentRadiusSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_extentRadiusSpinnerStateChanged
    extentRadius = (Double) extentRadiusModel.getValue();

    if (editor == null) {
        return;
    }

    if (extentRadius != originalExtentRadius) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
    } else {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
    }
}//GEN-LAST:event_extentRadiusSpinnerStateChanged

private void fullVolumeAreaPercentSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fullVolumeAreaPercentSpinnerStateChanged
    if (editor == null) {
        return;
    }

    if (((Double) fullVolumeAreaPercentModel.getValue()) != originalFullVolumeAreaPercent) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
    } else {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
    }
}//GEN-LAST:event_fullVolumeAreaPercentSpinnerStateChanged

private void falloffSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_falloffSliderStateChanged
    if (editor == null) {
        return;
    }

    if (falloffSlider.getValue() != originalFalloff) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
    } else {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
    }
}//GEN-LAST:event_falloffSliderStateChanged

private void distanceAttenuatedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_distanceAttenuatedRadioButtonActionPerformed
    falloffSlider.setEnabled(true);

    distanceAttenuated = true;

    if (editor == null) {
        return;
    }

    if (distanceAttenuated != originalDistanceAttenuated) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
    } else {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
    }
}//GEN-LAST:event_distanceAttenuatedRadioButtonActionPerformed

private void ambientRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ambientRadioButtonActionPerformed
    falloffSlider.setEnabled(false);

    distanceAttenuated = false;

    if (editor == null) {
        return;
    }

    if (distanceAttenuated != originalDistanceAttenuated) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, true);
    } else {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class, false);
    }
}//GEN-LAST:event_ambientRadioButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton alwaysRadioButton;
    private javax.swing.JRadioButton ambientRadioButton;
    private javax.swing.JTextField audioGroupIdTextField;
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JRadioButton distanceAttenuatedRadioButton;
    private javax.swing.JRadioButton entireCellRadioButton;
    private javax.swing.JRadioButton extentRadiusRadioButton;
    private javax.swing.JSpinner extentRadiusSpinner;
    private javax.swing.JSlider falloffSlider;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JSpinner fullVolumeAreaPercentSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JRadioButton manualRadioButton;
    private javax.swing.JRadioButton proximityRadioButton;
    private javax.swing.JTextField urlTextField;
    private javax.swing.JSpinner volumeSpinner;
    // End of variables declaration//GEN-END:variables

}
