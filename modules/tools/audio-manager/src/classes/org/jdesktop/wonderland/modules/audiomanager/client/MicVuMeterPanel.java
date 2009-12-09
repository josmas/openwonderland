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

import java.awt.Color;
import org.jdesktop.wonderland.modules.audiomanager.common.VolumeConverter;

import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jdesktop.wonderland.client.jme.VMeter;
import org.jdesktop.wonderland.client.softphone.MicrophoneInfoListener;
import org.jdesktop.wonderland.client.softphone.SoftphoneControl;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.client.softphone.SoftphoneListener;

/**
 * A microphone level control panel.
 *
 * @author jp
 * @author nsimpson
 */
public class MicVuMeterPanel extends javax.swing.JPanel implements
        SoftphoneListener, MicrophoneInfoListener, DisconnectListener {

    private static final Logger LOGGER =
            Logger.getLogger(MicVuMeterPanel.class.getName());
    private static final double DEFAULT_WARNING_LIMIT = 0.9d;
    private static final int VU_COUNT = 10;
    private AudioManagerClient client;
    private VMeter micMeter;
    private VMeter speakerMeter;
    private int count;
    private double volume;
    private VolumeConverter volumeConverter;
    private Color micPanelBackground;
    private Color speakerPanelBackground;
    private Color overLimitColor = Color.RED;
    private double micWarningLimit = DEFAULT_WARNING_LIMIT;
    private double speakerWarningLimit = DEFAULT_WARNING_LIMIT;
    private ImageIcon micMutedIcon;
    private ImageIcon micUnmutedIcon;
    private ImageIcon speakerMutedIcon;
    private ImageIcon speakerUnmutedIcon;

    public MicVuMeterPanel() {
        this(null);
    }

    public MicVuMeterPanel(AudioManagerClient client) {
        this.client = client;

        initComponents();
        micPanelBackground = micMeterPanel.getBackground();
        speakerPanelBackground = speakerMeterPanel.getBackground();
        micMutedIcon = new ImageIcon(getClass().getResource(
                "/org/jdesktop/wonderland/modules/audiomanager/client/" +
                "resources/UserListMicMuteOn24x24.png"));
        micUnmutedIcon = new ImageIcon(getClass().getResource(
                "/org/jdesktop/wonderland/modules/audiomanager/client/" +
                "resources/UserListMicMuteOff24x24.png"));
        speakerMutedIcon = new ImageIcon(getClass().getResource(
                "/org/jdesktop/wonderland/modules/audiomanager/client/" +
                "resources/UserListSpeakerMuteOn24x24.png"));
        speakerUnmutedIcon = new ImageIcon(getClass().getResource(
                "/org/jdesktop/wonderland/modules/audiomanager/client/" +
                "resources/UserListSpeakerMuteOff24x24.png"));

        volumeConverter = new VolumeConverter(micVolumeSlider.getMaximum());

        if (client != null) {
            client.addDisconnectListener(this);
        }

        // microphone volume meter
        micMeter = new VMeter("");
        micMeter.setBackground(Color.WHITE);
        micMeter.setForeground(Color.DARK_GRAY);
        micMeter.setPreferredSize(micMeterPanel.getPreferredSize());
        micMeter.setShowValue(false);
        micMeter.setShowTicks(false);
        micMeter.setMaxValue(1D);
        micMeter.setWarningValue(micWarningLimit);
        micMeter.setVisible(true);
        micMeterPanel.add(micMeter);

        // speaker volume meter
        speakerMeter = new VMeter("");
        speakerMeter.setBackground(Color.WHITE);
        speakerMeter.setForeground(Color.DARK_GRAY);
        speakerMeter.setPreferredSize(micMeterPanel.getPreferredSize());
        speakerMeter.setShowValue(false);
        speakerMeter.setShowTicks(false);
        speakerMeter.setMaxValue(1D);
        speakerMeter.setWarningValue(speakerWarningLimit);
        speakerMeter.setVisible(true);
        speakerMeterPanel.add(speakerMeter);

        startMicVuMeter(true);
        startSpeakerVuMeter(true);
    }

    public void disconnected() {
        startMicVuMeter(false);
        startSpeakerVuMeter(false);
    }

    public void startMicVuMeter(final boolean startVuMeter) {
        SoftphoneControl sc = SoftphoneControlImpl.getInstance();

        client.removeDisconnectListener(this);

        sc.removeSoftphoneListener(this);
        sc.removeMicrophoneInfoListener(this);

        if (startVuMeter) {
            client.addDisconnectListener(this);
            sc.addSoftphoneListener(this);
            sc.addMicrophoneInfoListener(this);

            try {
                sc.sendCommandToSoftphone("getMicrophoneVolume");
            } catch (IOException e) {
                LOGGER.log(Level.WARNING,
                        "Unable to get Microphone volume", e);
            }
        }

        sc.startVuMeter(startVuMeter);

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                setVisible(startVuMeter);
            }
        });
    }

    public void startSpeakerVuMeter(final boolean startVuMeter) {
        // TODO: set up monitor for speaker volume
        if (startVuMeter) {

        }
    }

    public void softphoneVisible(boolean isVisible) {
    }

    public void softphoneMuted(boolean muted) {
        if (SoftphoneControlImpl.getInstance().isMuted()) {
            micMuteButton.setIcon(micMutedIcon);
        } else {
            micMuteButton.setIcon(micUnmutedIcon);
        }
    }

    public void softphoneConnected(boolean connected) {
        SoftphoneControlImpl.getInstance().startVuMeter(connected);
    }

    public void softphoneExited() {
    }

    public void microphoneGainTooHigh() {
    }

    public void microphoneData(String data) {
        if (count == VU_COUNT) {
            count = 0;

            volume = Math.round(Math.sqrt(volume) * 100) / 100D;

            java.awt.EventQueue.invokeLater(new Runnable() {

                public void run() {
                    micMeter.setValue(volume);
                    if (volume > micWarningLimit) {
                        micMeterPanel.setBackground(overLimitColor);
                    } else {
                        micMeterPanel.setBackground(micPanelBackground);
                    }
                }
            });

            volume = 0;
        } else {
            double tmpVolume = Math.abs(Double.parseDouble(data));

            if (tmpVolume > volume) {
                volume = tmpVolume;
            }
        }

        count++;
    }

    public void microphoneVolume(String data) {
        micVolumeSlider.setValue(volumeConverter.getVolume((Float.parseFloat(data))));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        micVolumeSlider = new javax.swing.JSlider();
        micMeterPanel = new javax.swing.JPanel();
        micMuteButton = new javax.swing.JButton();
        speakerVolumeSlider = new javax.swing.JSlider();
        speakerMeterPanel = new javax.swing.JPanel();
        speakerMuteButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(95, 205));
        setLayout(null);

        micVolumeSlider.setMinorTickSpacing(10);
        micVolumeSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        micVolumeSlider.setPaintTicks(true);
        micVolumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                micVolumeSliderStateChanged(evt);
            }
        });
        add(micVolumeSlider);
        micVolumeSlider.setBounds(5, 14, 20, 155);

        micMeterPanel.setMinimumSize(new java.awt.Dimension(30, 160));
        micMeterPanel.setPreferredSize(new java.awt.Dimension(30, 160));
        add(micMeterPanel);
        micMeterPanel.setBounds(20, 10, 20, 160);

        micMuteButton.setFont(new java.awt.Font("Arial", 1, 8)); // NOI18N
        micMuteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/UserListMicMuteOff24x24.png"))); // NOI18N
        micMuteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                micMuteButtonActionPerformed(evt);
            }
        });
        add(micMuteButton);
        micMuteButton.setBounds(20, 175, 24, 24);

        speakerVolumeSlider.setMinorTickSpacing(10);
        speakerVolumeSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        speakerVolumeSlider.setPaintTicks(true);
        speakerVolumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                speakerVolumeSliderStateChanged(evt);
            }
        });
        add(speakerVolumeSlider);
        speakerVolumeSlider.setBounds(50, 14, 20, 155);

        speakerMeterPanel.setMinimumSize(new java.awt.Dimension(30, 160));
        speakerMeterPanel.setPreferredSize(new java.awt.Dimension(30, 160));
        add(speakerMeterPanel);
        speakerMeterPanel.setBounds(65, 10, 20, 160);

        speakerMuteButton.setFont(new java.awt.Font("Arial", 1, 8)); // NOI18N
        speakerMuteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/audiomanager/client/resources/UserListSpeakerMuteOff24x24.png"))); // NOI18N
        speakerMuteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speakerMuteButtonActionPerformed(evt);
            }
        });
        add(speakerMuteButton);
        speakerMuteButton.setBounds(65, 175, 24, 24);
    }// </editor-fold>//GEN-END:initComponents

    private void micVolumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_micVolumeSliderStateChanged
        SoftphoneControl sc = SoftphoneControlImpl.getInstance();

        float volume = volumeConverter.getVolume(micVolumeSlider.getValue());

        try {
            sc.sendCommandToSoftphone("microphoneVolume=" + volume);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING,
                    "Unable to send microphone volume command to softphone", e);
        }
    }//GEN-LAST:event_micVolumeSliderStateChanged

    private void speakerVolumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_speakerVolumeSliderStateChanged
        SoftphoneControl sc = SoftphoneControlImpl.getInstance();

        float volume = volumeConverter.getVolume(speakerVolumeSlider.getValue());
        // TODO: change speaker volume
    }//GEN-LAST:event_speakerVolumeSliderStateChanged

    private void micMuteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_micMuteButtonActionPerformed
        SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
        sc.mute(!sc.isMuted());
        // TODO: EXTRA CREDIT:
        // - set mic volume slider to zero when muted
        // - unmute when slider dragged
        // - restore slider position when unmuted
    }//GEN-LAST:event_micMuteButtonActionPerformed

    private void speakerMuteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speakerMuteButtonActionPerformed
        // TODO: handle muting of speaker
        // TODO: EXTRA CREDIT:
        // - set speaker volume slider to zero when muted
        // - unmute when slider dragged
        // - restore slider position when unmuted
    }//GEN-LAST:event_speakerMuteButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel micMeterPanel;
    private javax.swing.JButton micMuteButton;
    private javax.swing.JSlider micVolumeSlider;
    private javax.swing.JPanel speakerMeterPanel;
    private javax.swing.JButton speakerMuteButton;
    private javax.swing.JSlider speakerVolumeSlider;
    // End of variables declaration//GEN-END:variables
}
