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
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentServerState;
import org.jdesktop.wonderland.modules.audiomanager.common.AudioTreatmentComponentServerState.PlayWhen;
import org.jdesktop.wonderland.modules.audiomanager.common.VolumeUtil;

/**
 *
 * @author  jp
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
@PropertiesFactory(AudioTreatmentComponentServerState.class)
public class AudioTreatmentComponentProperties extends javax.swing.JPanel
        implements PropertiesFactorySPI {

    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle");
    private CellPropertiesEditor editor;
    private String originalGroupId;
    private String originalFileTreatments;
    private String originalUrlTreatments;
    private int originalVolume;
    private PlayWhen originalPlayWhen;
    private boolean originalPlayOnce;
    private double originalExtentRadius;
    private double originalFullVolumeAreaPercent;
    private boolean originalDistanceAttenuated;
    private int originalFalloff;
    private SpinnerNumberModel fullVolumeAreaPercentModel;
    private SpinnerNumberModel extentRadiusModel;
    private double extentRadius = 0;
    private PlayWhen playWhen;
    private boolean playOnce;
    private boolean distanceAttenuated;

    /** Creates new form AudioTreatmentComponentProperties */
    public AudioTreatmentComponentProperties() {
        initComponents();

        String diagramFileName = BUNDLE.getString("AudioCapabilitiesDiagram");
        String resourceName = "/org/jdesktop/wonderland/modules/audiomanager/" +
                "client/resources/" + diagramFileName;
        Icon icon = new ImageIcon(getClass().getResource(resourceName));
        audioCapabilitiesLabel.setIcon(icon);

        // Set the maximum and minimum values for the spinners
        Double value = new Double(25);
        Double min = new Double(0);
        Double max = new Double(100);
        Double step = new Double(1);
        fullVolumeAreaPercentModel =
                new SpinnerNumberModel(value, min, max, step);
        fullVolumeAreaPercentSpinner.setModel(fullVolumeAreaPercentModel);

        value = new Double(10);
        min = new Double(0);
        max = new Double(100);
        step = new Double(1);
        extentRadiusModel = new SpinnerNumberModel(value, min, max, step);
        extentRadiusSpinner.setModel(extentRadiusModel);

        // Listen for changes to the text fields and spinners
        audioGroupIdTextField.getDocument().addDocumentListener(
                new AudioGroupTextFieldListener());
        fileTextField.getDocument().addDocumentListener(
                new AudioFileTreatmentsTextFieldListener());
        urlTextField.getDocument().addDocumentListener(
                new AudioUrlTreatmentsTextFieldListener());
        fullVolumeAreaPercentModel.addChangeListener(
                new FullVolumeAreaPercentChangeListener());
        extentRadiusModel.addChangeListener(
                new ExtentRadiusChangeListener());
    }

    /**
     * @{inheritDoc}
     */
    public String getDisplayName() {
        return BUNDLE.getString("Audio_Capabilities");
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
        CellServerState state = editor.getCellServerState();

        AudioTreatmentComponentServerState compState =
                (AudioTreatmentComponentServerState) state.getComponentServerState(
                AudioTreatmentComponentServerState.class);

        if (state == null) {
            return;
        }

        originalGroupId = compState.getGroupId();
        audioGroupIdTextField.setText(originalGroupId);

        String[] treatmentList = compState.getTreatments();

        originalFileTreatments = "";
        originalUrlTreatments = "";

        for (int i = 0; i < treatmentList.length; i++) {
            String treatment = treatmentList[i];

            if (treatment.indexOf("://") > 0) {
                if (treatment.indexOf("file://") >= 0) {
                    originalFileTreatments += treatment + " ";
                } else {
                    originalUrlTreatments += treatment + " ";
                }
            } else {
                originalFileTreatments += treatment + " ";
            }
        }

        originalFileTreatments = originalFileTreatments.trim();

        fileTextField.setText(originalFileTreatments);

        originalUrlTreatments = originalUrlTreatments.trim();

        urlTextField.setText(originalUrlTreatments);

        originalVolume = VolumeUtil.getClientVolume(compState.getVolume());
        volumeSlider.setValue(originalVolume);

        originalPlayWhen = compState.getPlayWhen();
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

        originalPlayOnce = compState.getPlayOnce();
        playOnce = originalPlayOnce;

        playOnceCheckBox.setSelected(playOnce);

        originalExtentRadius = compState.getExtent();
        extentRadius = originalExtentRadius;
        extentRadiusSpinner.setValue(originalExtentRadius);

        extentRadiusSpinner.setEnabled(true);
        extentRadiusSpinner.setValue((Double) extentRadius);

        originalFullVolumeAreaPercent = compState.getFullVolumeAreaPercent();
        fullVolumeAreaPercentSpinner.setValue(originalFullVolumeAreaPercent);

        originalDistanceAttenuated = compState.getDistanceAttenuated();
        distanceAttenuated = originalDistanceAttenuated;
        distanceAttenuatedRadioButton.setSelected(originalDistanceAttenuated);

        originalFalloff = (int) compState.getFalloff();
        falloffSlider.setValue(originalFalloff);

        if (originalDistanceAttenuated == true) {
            falloffSlider.setEnabled(true);
        } else {
            falloffSlider.setEnabled(false);
        }
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
        // component.
        CellServerState state = editor.getCellServerState();

        AudioTreatmentComponentServerState compState =
                (AudioTreatmentComponentServerState) state.getComponentServerState(
                AudioTreatmentComponentServerState.class);

        if (state == null) {
            return;
        }

        compState.setGroupId(audioGroupIdTextField.getText());

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

        // Update the component state, add to the list of updated states
        compState.setTreatments(treatments.split(" "));
        compState.setVolume(VolumeUtil.getServerVolume(volumeSlider.getValue()));
        compState.setPlayWhen(playWhen);
        compState.setPlayOnce(playOnce);
        compState.setExtent((Double) extentRadiusModel.getValue());
        compState.setFullVolumeAreaPercent(
                (Double) fullVolumeAreaPercentModel.getValue());
        compState.setDistanceAttenuated(distanceAttenuated);
        compState.setFalloff(falloffSlider.getValue());
        editor.addToUpdateList(compState);
    }

    /**
     * @{inheritDoc}
     */
    public void restore() {
        // Reset the GUI values to the original values
        audioGroupIdTextField.setText(originalGroupId);
        fileTextField.setText(originalFileTreatments);
        urlTextField.setText(originalUrlTreatments);
        volumeSlider.setValue(originalVolume);

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

        playOnceCheckBox.setSelected(originalPlayOnce);
        extentRadiusSpinner.setValue(originalExtentRadius);
        extentRadiusSpinner.setEnabled(true);
        fullVolumeAreaPercentSpinner.setValue(originalFullVolumeAreaPercent);
        distanceAttenuatedRadioButton.setSelected(originalDistanceAttenuated);
        falloffSlider.setValue(originalFalloff);
        falloffSlider.setEnabled(originalDistanceAttenuated);
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
                editor.setPanelDirty(AudioTreatmentComponentProperties.class,
                        !audioGroupId.equals(originalGroupId));
            }
        }
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor.
     */
    class AudioFileTreatmentsTextFieldListener implements DocumentListener {

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

            if (editor != null) {
                editor.setPanelDirty(AudioTreatmentComponentProperties.class,
                        !treatments.equals(originalFileTreatments));
            }
        }
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor.
     */
    class AudioUrlTreatmentsTextFieldListener implements DocumentListener {

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
            String treatments = urlTextField.getText();

            if (editor != null) {
                editor.setPanelDirty(AudioTreatmentComponentProperties.class,
                        !treatments.equals(originalUrlTreatments));
            }
        }
    }

    class FullVolumeAreaPercentChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            Double fullVolumeAreaPercent =
                    (Double) fullVolumeAreaPercentModel.getValue();
            if (editor != null) {
                editor.setPanelDirty(AudioTreatmentComponentProperties.class,
                        fullVolumeAreaPercent != originalFullVolumeAreaPercent);
            }
        }
    }

    class ExtentRadiusChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            Double extentRadius = (Double) extentRadiusModel.getValue();
            if (editor != null) {
                editor.setPanelDirty(AudioTreatmentComponentProperties.class,
                        extentRadius != originalExtentRadius);
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
        buttonGroup3 = new javax.swing.ButtonGroup();
        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        audioGroupIdTextField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();
        urlTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        alwaysRadioButton = new javax.swing.JRadioButton();
        proximityRadioButton = new javax.swing.JRadioButton();
        manualRadioButton = new javax.swing.JRadioButton();
        jLabel11 = new javax.swing.JLabel();
        extentRadiusSpinner = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        fullVolumeAreaPercentSpinner = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        ambientRadioButton = new javax.swing.JRadioButton();
        distanceAttenuatedRadioButton = new javax.swing.JRadioButton();
        falloffSlider = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        audioCapabilitiesLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        volumeSlider = new javax.swing.JSlider();
        playOnceCheckBox = new javax.swing.JCheckBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle"); // NOI18N
        jLabel5.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel5.text")); // NOI18N

        jLabel1.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel1.text")); // NOI18N

        jLabel2.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel2.text")); // NOI18N

        jLabel7.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel7.text")); // NOI18N

        jLabel8.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel8.text")); // NOI18N

        jLabel9.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel9.text")); // NOI18N

        browseButton.setText(bundle.getString("AudioTreatmentComponentProperties.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        jLabel10.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel10.text")); // NOI18N

        buttonGroup1.add(alwaysRadioButton);
        alwaysRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.alwaysRadioButton.text")); // NOI18N
        alwaysRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alwaysRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(proximityRadioButton);
        proximityRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.proximityRadioButton.text")); // NOI18N
        proximityRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proximityRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(manualRadioButton);
        manualRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.manualRadioButton.text")); // NOI18N
        manualRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualRadioButtonActionPerformed(evt);
            }
        });

        jLabel11.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel11.text")); // NOI18N

        extentRadiusSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                extentRadiusSpinnerStateChanged(evt);
            }
        });

        jLabel12.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel12.text")); // NOI18N

        fullVolumeAreaPercentSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fullVolumeAreaPercentSpinnerStateChanged(evt);
            }
        });

        jLabel13.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel13.text")); // NOI18N

        jLabel14.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel14.text")); // NOI18N

        buttonGroup3.add(ambientRadioButton);
        ambientRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.ambientRadioButton.text")); // NOI18N
        ambientRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ambientRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup3.add(distanceAttenuatedRadioButton);
        distanceAttenuatedRadioButton.setText(bundle.getString("AudioTreatmentComponentProperties.distanceAttenuatedRadioButton.text")); // NOI18N
        distanceAttenuatedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distanceAttenuatedRadioButtonActionPerformed(evt);
            }
        });

        falloffSlider.setMinorTickSpacing(10);
        falloffSlider.setPaintTicks(true);
        falloffSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                falloffSliderStateChanged(evt);
            }
        });

        jLabel3.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel3.text")); // NOI18N

        audioCapabilitiesLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/AudioCapabilitiesDiagram_en.png"))); // NOI18N

        jLabel6.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel6.text")); // NOI18N

        jLabel15.setText(bundle.getString("AudioTreatmentComponentProperties.jLabel15.text")); // NOI18N

        volumeSlider.setMajorTickSpacing(1);
        volumeSlider.setMaximum(10);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setValue(5);
        volumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                volumeSliderStateChanged(evt);
            }
        });

        playOnceCheckBox.setText(bundle.getString("AudioTreatmentComponentProperties.playOnceCheckBox.text")); // NOI18N
        playOnceCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playOnceCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(23, 23, 23)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel9)
                            .add(jLabel8)
                            .add(jLabel7)
                            .add(jLabel10)
                            .add(jLabel1)
                            .add(jLabel2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(proximityRadioButton)
                            .add(alwaysRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, urlTextField)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 255, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(manualRadioButton)
                            .add(layout.createSequentialGroup()
                                .add(29, 29, 29)
                                .add(playOnceCheckBox))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(audioCapabilitiesLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 167, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(layout.createSequentialGroup()
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, audioGroupIdTextField)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, fileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 251, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(browseButton)))))
                    .add(layout.createSequentialGroup()
                        .add(151, 151, 151)
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jLabel15)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(falloffSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 174, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel5))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel11)
                            .add(jLabel12)
                            .add(jLabel14))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(fullVolumeAreaPercentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel13))
                            .add(ambientRadioButton)
                            .add(distanceAttenuatedRadioButton)
                            .add(layout.createSequentialGroup()
                                .add(jLabel6)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(extentRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .add(21, 21, 21))
        );

        layout.linkSize(new java.awt.Component[] {audioGroupIdTextField, fileTextField, urlTextField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(audioGroupIdTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(fileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 36, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(alwaysRadioButton)
                    .add(jLabel10))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(proximityRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(manualRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(playOnceCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                            .add(jLabel6)
                            .add(extentRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel11))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                            .add(jLabel12)
                            .add(fullVolumeAreaPercentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel13))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(ambientRadioButton)
                            .add(jLabel14))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(distanceAttenuatedRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(20, 20, 20))
                    .add(layout.createSequentialGroup()
                        .add(audioCapabilitiesLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jLabel3)
                    .add(jLabel15)
                    .add(falloffSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
    JFileChooser chooser = new JFileChooser(fileTextField.getText());

    int returnVal = chooser.showOpenDialog(this);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
        fileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
    }
}//GEN-LAST:event_browseButtonActionPerformed

private void alwaysRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alwaysRadioButtonActionPerformed
    playWhen = PlayWhen.ALWAYS;

    if (editor != null) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class,
                playWhen != originalPlayWhen);
    }
}//GEN-LAST:event_alwaysRadioButtonActionPerformed

private void proximityRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proximityRadioButtonActionPerformed
    playWhen = PlayWhen.FIRST_IN_RANGE;

    if (editor != null) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class,
                playWhen != originalPlayWhen);
    }
}//GEN-LAST:event_proximityRadioButtonActionPerformed

private void manualRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualRadioButtonActionPerformed
    playWhen = PlayWhen.MANUAL;

    if (editor != null) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class,
                playWhen != originalPlayWhen);
    }
}//GEN-LAST:event_manualRadioButtonActionPerformed

private void extentRadiusSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_extentRadiusSpinnerStateChanged
    extentRadius = (Double) extentRadiusModel.getValue();

    if (editor != null) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class,
                extentRadius != originalExtentRadius);
    }
}//GEN-LAST:event_extentRadiusSpinnerStateChanged

private void fullVolumeAreaPercentSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fullVolumeAreaPercentSpinnerStateChanged
    if (editor != null) {
        double newValue = (Double) fullVolumeAreaPercentModel.getValue();
        editor.setPanelDirty(AudioTreatmentComponentProperties.class,
                newValue != originalFullVolumeAreaPercent);
    }
}//GEN-LAST:event_fullVolumeAreaPercentSpinnerStateChanged

private void falloffSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_falloffSliderStateChanged
    if (editor != null) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class,
                falloffSlider.getValue() != originalFalloff);
    }
}//GEN-LAST:event_falloffSliderStateChanged

private void distanceAttenuatedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_distanceAttenuatedRadioButtonActionPerformed
    falloffSlider.setEnabled(true);
    distanceAttenuated = true;

    if (editor != null) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class,
                distanceAttenuated != originalDistanceAttenuated);
    }
}//GEN-LAST:event_distanceAttenuatedRadioButtonActionPerformed

private void ambientRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ambientRadioButtonActionPerformed
    falloffSlider.setEnabled(false);
    distanceAttenuated = false;

    if (editor != null) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class,
                distanceAttenuated != originalDistanceAttenuated);
    }
}//GEN-LAST:event_ambientRadioButtonActionPerformed

private void volumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeSliderStateChanged
    if (editor != null) {
        editor.setPanelDirty(AudioTreatmentComponentProperties.class,
                volumeSlider.getValue() != originalVolume);
    }
}//GEN-LAST:event_volumeSliderStateChanged

private void playOnceCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playOnceCheckBoxActionPerformed
    playOnce = playOnceCheckBox.isSelected();
}//GEN-LAST:event_playOnceCheckBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton alwaysRadioButton;
    private javax.swing.JRadioButton ambientRadioButton;
    private javax.swing.JLabel audioCapabilitiesLabel;
    private javax.swing.JTextField audioGroupIdTextField;
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JRadioButton distanceAttenuatedRadioButton;
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
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JRadioButton manualRadioButton;
    private javax.swing.JCheckBox playOnceCheckBox;
    private javax.swing.JRadioButton proximityRadioButton;
    private javax.swing.JTextField urlTextField;
    private javax.swing.JSlider volumeSlider;
    // End of variables declaration//GEN-END:variables
}
