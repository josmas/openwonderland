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
package org.jdesktop.wonderland.modules.viewproperties.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.ViewProperties;

/**
 * A dialog box to configure the view properties (e.g. field-of-view, front
 * and back clip).
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ViewPropertiesJDialog extends javax.swing.JDialog {

    private static Logger logger = Logger.getLogger(ViewPropertiesJDialog.class.getName());
    
    // The original values for the field-of-view, front/back clip to use upon
    // revert.
    private float originalFieldOfView = 0.0f;
    private float originalFrontClip = 0.0f;
    private float originalBackClip = 0.0f;

    // The view manager's properties
    private ViewProperties viewProperties = null;

    /** Creates new form ViewControls */
    public ViewPropertiesJDialog() {
        initComponents();
        setTitle("View Properties");

        // Fetch the view properties object
        ViewManager manager = ViewManager.getViewManager();
        viewProperties = manager.getViewProperties();

        // Listen for when the window is closing. We will pop up a confirm
        // dialog to see if the user wants to keep the current values.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isDirty() == true) {
                    int result = JOptionPane.showConfirmDialog(
                            ViewPropertiesJDialog.this,
                            "Do you wish to apply the properties before closing?",
                            "Apply values?", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        okButtonActionPerformed(null);
                    }
                    else {
                        // Reset the view to the original values and close the
                        // window. We do not dispose since we may want to use
                        // the dialog again.
                        viewProperties.setFieldOfView(originalFieldOfView);
                        viewProperties.setFrontClip(originalFrontClip);
                        viewProperties.setBackClip(originalBackClip);
                        setVisible(false);
                    }
                    return;
                }
                setVisible(false);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisible(boolean isVisible) {

        // If we are making the dialog visible, then store the original values
        // from the view manager's properties
        if (isVisible == true) {
            originalFieldOfView = viewProperties.getFieldOfView();
            originalFrontClip = viewProperties.getFrontClip();
            originalBackClip = viewProperties.getBackClip();
            updateGUI();
        }
        super.setVisible(isVisible);
    }

    /**
     * Updates the GUI with the original values.
     *
     * NOTE: This method assumes it is being called within the AWT Event Thread.
     */
    private void updateGUI() {
        fovSlider.setValue((int)originalFieldOfView);
        fovField.setText("" + (int)originalFieldOfView);
        frontClipSlider.setValue((int) originalFrontClip);
        frontClipField.setText("" + (int) originalFrontClip);
        rearClipSlider.setValue((int) originalBackClip);
        rearClipField.setText("" + (int)originalBackClip);
    }

    /**
     * Sets whether the Apply button is enabled depending upon whether changes
     * have been made to the GUI
     */
    private void setApplyEnabled() {
        okButton.setEnabled(isDirty());
    }

    /**
     * Returns true if changes have been made to the GUI different from the
     * original values.
     */
    private boolean isDirty() {
        if ((float)fovSlider.getValue() != originalFieldOfView) {
            return true;
        }

        if ((float)frontClipSlider.getValue() != originalFrontClip) {
            return true;
        }

        if ((float)rearClipSlider.getValue() != originalBackClip) {
            return true;
        }
        return false;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        fovLabel = new javax.swing.JLabel();
        fovSlider = new javax.swing.JSlider();
        fovField = new javax.swing.JTextField();
        rearClipField = new javax.swing.JTextField();
        frontClipLabel = new javax.swing.JLabel();
        rearClipSlider = new javax.swing.JSlider();
        frontClipSlider = new javax.swing.JSlider();
        rearClipLabel = new javax.swing.JLabel();
        frontClipField = new javax.swing.JTextField();

        okButton.setText("Apply");
        okButton.setEnabled(false);
        okButton.setSelected(true);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Close");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new java.awt.GridBagLayout());

        fovLabel.setText("Field Of View:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        mainPanel.add(fovLabel, gridBagConstraints);

        fovSlider.setMaximum(180);
        fovSlider.setMinimum(30);
        fovSlider.setValue(0);
        fovSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fovSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        mainPanel.add(fovSlider, gridBagConstraints);

        fovField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fovFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        mainPanel.add(fovField, gridBagConstraints);
        fovField.setText(String.valueOf(fovSlider.getValue()));

        rearClipField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rearClipFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        mainPanel.add(rearClipField, gridBagConstraints);
        rearClipField.setText(String.valueOf(rearClipSlider.getValue()));

        frontClipLabel.setText("Front Clip:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        mainPanel.add(frontClipLabel, gridBagConstraints);

        rearClipSlider.setMaximum(5000);
        rearClipSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rearClipSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        mainPanel.add(rearClipSlider, gridBagConstraints);

        frontClipSlider.setMaximum(1000);
        frontClipSlider.setValue(0);
        frontClipSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                frontClipSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        mainPanel.add(frontClipSlider, gridBagConstraints);

        rearClipLabel.setText("Back Clip:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        mainPanel.add(rearClipLabel, gridBagConstraints);

        frontClipField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frontClipFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        mainPanel.add(frontClipField, gridBagConstraints);
        frontClipField.setText(String.valueOf(frontClipSlider.getValue() / 1000d));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(345, Short.MAX_VALUE)
                .add(cancelButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(okButton)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(mainPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 121, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton)
                    .add(cancelButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fovSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fovSliderStateChanged
        // Update the value of the text field and also the view properties
        float fov = (float)fovSlider.getValue();
        fovField.setText("" + (int)fov);
        viewProperties.setFieldOfView(fov);
        setApplyEnabled();
    }//GEN-LAST:event_fovSliderStateChanged

    private void fovFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fovFieldActionPerformed
        fovSlider.setValue(Integer.parseInt(fovField.getText()));
        setApplyEnabled();
    }//GEN-LAST:event_fovFieldActionPerformed

    private void frontClipSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_frontClipSliderStateChanged
        // Update the value of the text field and also the view properties
        float clip = (float)frontClipSlider.getValue();
        frontClipField.setText("" + (int)clip);
        viewProperties.setFrontClip(clip);
        setApplyEnabled();
    }//GEN-LAST:event_frontClipSliderStateChanged

    private void rearClipSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rearClipSliderStateChanged
        // Update the value of the text field and also the view properties
        float clip = (float)rearClipSlider.getValue();
        rearClipField.setText("" + (int)clip);
        viewProperties.setBackClip(clip);
        setApplyEnabled();
    }//GEN-LAST:event_rearClipSliderStateChanged

    private void frontClipFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frontClipFieldActionPerformed
        frontClipSlider.setValue(Integer.parseInt(frontClipField.getText()));
        setApplyEnabled();
    }//GEN-LAST:event_frontClipFieldActionPerformed

    private void rearClipFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rearClipFieldActionPerformed
        rearClipSlider.setValue(Integer.parseInt(rearClipField.getText()));
        setApplyEnabled();
}//GEN-LAST:event_rearClipFieldActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        // Save the current properties to the user's local repository
        try {
            ViewPropertiesUtils.saveViewProperties(viewProperties);
        } catch (java.lang.Exception excp) {
            String msg = "Error writing properties to user's local repository.";
            String title = "Error Writing Properties";
            JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
            logger.log(Level.WARNING, "Error writing properties file", excp);
        }

        // Close the window, we do not dispose since we may want to use the
        // dialog again.
        setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed

        // Check to see if the values have changed from the original values. If
        // so then ask whether we want to apply the changes or not.
        if (isDirty() == true) {
            int result = JOptionPane.showConfirmDialog(
                    ViewPropertiesJDialog.this,
                    "Do you wish to apply the properties before closing?",
                    "Apply values?", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                okButtonActionPerformed(null);
            }
            else {
                // Reset the view to the original values and close the window.
                // We do not dispose since we may want to use the dialog again.
                viewProperties.setFieldOfView(originalFieldOfView);
                viewProperties.setFrontClip(originalFrontClip);
                viewProperties.setBackClip(originalBackClip);
                setVisible(false);
            }
            return;
        }
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField fovField;
    private javax.swing.JLabel fovLabel;
    private javax.swing.JSlider fovSlider;
    private javax.swing.JTextField frontClipField;
    private javax.swing.JLabel frontClipLabel;
    private javax.swing.JSlider frontClipSlider;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField rearClipField;
    private javax.swing.JLabel rearClipLabel;
    private javax.swing.JSlider rearClipSlider;
    // End of variables declaration//GEN-END:variables
}
