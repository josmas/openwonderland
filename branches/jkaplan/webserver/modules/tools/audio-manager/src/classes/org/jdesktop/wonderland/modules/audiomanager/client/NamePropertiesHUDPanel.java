/*
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

/*
 * NamePropertiesHUDPanel.java
 *
 * Created on Jun 16, 2009, 3:30:01 PM
 */
package org.jdesktop.wonderland.modules.audiomanager.client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SpinnerListModel;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarNameEvent;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode.EventType;
import org.jdesktop.wonderland.modules.orb.client.cell.OrbCell;

/**
 * A panel for selecting display properties for avatar name tags.
 *
 * @author jp
 * @author nsimpson
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
public class NamePropertiesHUDPanel extends javax.swing.JPanel {

    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle");
    private final static String SMALL_STRING = BUNDLE.getString("Small");
    private final static String REGULAR_STRING = BUNDLE.getString("Regular");
    private final static String LARGE_STRING = BUNDLE.getString("Large");
    private PresenceInfo presenceInfo;
    private PropertyChangeSupport listeners;

    private enum NameTagAttribute {

        HIDE,
        SMALL_FONT,
        REGULAR_FONT,
        LARGE_FONT
    };
    private NameTagAttribute originalMyNameTagAttribute =
            NameTagAttribute.REGULAR_FONT;
    private NameTagAttribute myNameTagAttribute =
            NameTagAttribute.REGULAR_FONT;
    private NameTagAttribute originalOtherNameTagAttributes =
            NameTagAttribute.REGULAR_FONT;
    private NameTagAttribute otherNameTagAttributes =
            NameTagAttribute.REGULAR_FONT;

    public NamePropertiesHUDPanel() {
        initComponents();
        String[] spinnerValues = new String[]{
            SMALL_STRING, REGULAR_STRING, LARGE_STRING};
        myNameFontSizeSpinner.setModel(new SpinnerListModel(spinnerValues));
        myNameFontSizeSpinner.setValue(REGULAR_STRING);
        otherNamesFontSizeSpinner.setModel(new SpinnerListModel(spinnerValues));
        otherNamesFontSizeSpinner.setValue(REGULAR_STRING);
        ((DefaultEditor) myNameFontSizeSpinner.getEditor()).getTextField().
                setEditable(false);
        ((DefaultEditor) otherNamesFontSizeSpinner.getEditor()).getTextField().
                setEditable(false);
    }

    public NamePropertiesHUDPanel(PresenceInfo presenceInfo) {
        this();
        this.presenceInfo = presenceInfo;
    }

    /**
     * Adds a bound property listener to the dialog
     * @param listener a listener for dialog events
     */
    @Override
    public synchronized void addPropertyChangeListener(
            PropertyChangeListener listener) {
        if (listeners == null) {
            listeners = new PropertyChangeSupport(this);
        }
        listeners.addPropertyChangeListener(listener);
    }

    /**
     * Removes a bound property listener from the dialog
     * @param listener the listener to remove
     */
    @Override
    public synchronized void removePropertyChangeListener(
            PropertyChangeListener listener) {
        if (listeners != null) {
            listeners.removePropertyChangeListener(listener);
        }
    }

    private void applyChanges() {
        AvatarNameEvent avatarNameEvent;

        if (myNameTagAttribute != originalMyNameTagAttribute) {
            originalMyNameTagAttribute = myNameTagAttribute;

            switch (myNameTagAttribute) {
                case HIDE:
                    NameTagNode.setMyNameTag(EventType.HIDE,
                            presenceInfo.userID.getUsername(),
                            presenceInfo.usernameAlias);
                    break;

                case SMALL_FONT:
                    NameTagNode.setMyNameTag(EventType.SMALL_FONT,
                            presenceInfo.userID.getUsername(),
                            presenceInfo.usernameAlias);
                    break;

                case REGULAR_FONT:
                    NameTagNode.setMyNameTag(EventType.REGULAR_FONT,
                            presenceInfo.userID.getUsername(),
                            presenceInfo.usernameAlias);
                    break;

                case LARGE_FONT:
                    NameTagNode.setMyNameTag(EventType.LARGE_FONT,
                            presenceInfo.userID.getUsername(),
                            presenceInfo.usernameAlias);
                    break;
            }
        }

        if (otherNameTagAttributes == originalOtherNameTagAttributes) {
            return;
        }

        originalOtherNameTagAttributes = otherNameTagAttributes;

        switch (otherNameTagAttributes) {
            case HIDE:
                NameTagNode.setOtherNameTags(EventType.HIDE,
                        presenceInfo.userID.getUsername(),
                        presenceInfo.usernameAlias);
                OrbCell.makeOrbsVisible(false);
                break;

            case SMALL_FONT:
                NameTagNode.setOtherNameTags(EventType.SMALL_FONT,
                        presenceInfo.userID.getUsername(),
                        presenceInfo.usernameAlias);
                OrbCell.makeOrbsVisible(true);
                break;

            case REGULAR_FONT:
                NameTagNode.setOtherNameTags(EventType.REGULAR_FONT,
                        presenceInfo.userID.getUsername(),
                        presenceInfo.usernameAlias);
                OrbCell.makeOrbsVisible(true);
                break;

            case LARGE_FONT:
                NameTagNode.setOtherNameTags(EventType.LARGE_FONT,
                        presenceInfo.userID.getUsername(),
                        presenceInfo.usernameAlias);
                OrbCell.makeOrbsVisible(true);
                break;
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

        avatarNamesLabel = new javax.swing.JLabel();
        showMyNameCheckBox = new javax.swing.JCheckBox();
        myFontSizeLabel = new javax.swing.JLabel();
        showOtherNamesCheckBox = new javax.swing.JCheckBox();
        otherFontSizeLabel = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        myNameFontSizeSpinner = new javax.swing.JSpinner();
        otherNamesFontSizeSpinner = new javax.swing.JSpinner();

        avatarNamesLabel.setFont(avatarNamesLabel.getFont().deriveFont(avatarNamesLabel.getFont().getStyle() | java.awt.Font.BOLD));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle"); // NOI18N
        avatarNamesLabel.setText(bundle.getString("NamePropertiesHUDPanel.avatarNamesLabel.text")); // NOI18N

        showMyNameCheckBox.setSelected(true);
        showMyNameCheckBox.setText(bundle.getString("NamePropertiesHUDPanel.showMyNameCheckBox.text")); // NOI18N
        showMyNameCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showMyNameCheckBoxItemStateChanged(evt);
            }
        });

        myFontSizeLabel.setText(bundle.getString("NamePropertiesHUDPanel.myFontSizeLabel.text")); // NOI18N

        showOtherNamesCheckBox.setSelected(true);
        showOtherNamesCheckBox.setText(bundle.getString("NamePropertiesHUDPanel.showOtherNamesCheckBox.text")); // NOI18N
        showOtherNamesCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showOtherNamesCheckBoxItemStateChanged(evt);
            }
        });

        otherFontSizeLabel.setText(bundle.getString("NamePropertiesHUDPanel.otherFontSizeLabel.text")); // NOI18N

        cancelButton.setText(bundle.getString("NamePropertiesHUDPanel.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText(bundle.getString("NamePropertiesHUDPanel.okButton.text")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        myNameFontSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                myNameFontSizeSpinnerStateChanged(evt);
            }
        });

        otherNamesFontSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                otherNamesFontSizeSpinnerStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(showMyNameCheckBox)
                            .add(showOtherNamesCheckBox)))
                    .add(avatarNamesLabel)
                    .add(layout.createSequentialGroup()
                        .add(31, 31, 31)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, myFontSizeLabel)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, otherFontSizeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(cancelButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(okButton))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, otherNamesFontSizeSpinner)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, myNameFontSizeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 122, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(avatarNamesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showMyNameCheckBox)
                .add(2, 2, 2)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(myNameFontSizeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myFontSizeLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showOtherNamesCheckBox)
                .add(2, 2, 2)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(otherFontSizeLabel)
                    .add(otherNamesFontSizeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton)
                    .add(cancelButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        listeners.firePropertyChange("ok", new String(""), null);
}//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        listeners.firePropertyChange("cancel", new String(""), null);
}//GEN-LAST:event_cancelButtonActionPerformed

    private void updateMyNameTag(boolean showingName) {
        if (showingName) {
            Object value = myNameFontSizeSpinner.getValue();
            if (value.equals(SMALL_STRING)) {
                myNameTagAttribute = NameTagAttribute.SMALL_FONT;
            } else if (value.equals(REGULAR_STRING)) {
                myNameTagAttribute = NameTagAttribute.REGULAR_FONT;
            } else if (value.equals(LARGE_STRING)) {
                myNameTagAttribute = NameTagAttribute.LARGE_FONT;
            }
        } else {
            myNameTagAttribute = NameTagAttribute.HIDE;
        }
        myNameFontSizeSpinner.setEnabled(showingName);
        applyChanges();
    }

    private void showMyNameCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showMyNameCheckBoxItemStateChanged
        JCheckBox cb = (JCheckBox) evt.getSource();

        updateMyNameTag(cb.isSelected());
    }//GEN-LAST:event_showMyNameCheckBoxItemStateChanged

    private void updateOthersNameTag(boolean showingName) {
        if (showingName) {
            Object value = otherNamesFontSizeSpinner.getValue();
            if (value.equals(SMALL_STRING)) {
                otherNameTagAttributes = NameTagAttribute.SMALL_FONT;
            } else if (value.equals(REGULAR_STRING)) {
                otherNameTagAttributes = NameTagAttribute.REGULAR_FONT;
            } else if (value.equals(LARGE_STRING)) {
                otherNameTagAttributes = NameTagAttribute.LARGE_FONT;
            }
        } else {
            otherNameTagAttributes = NameTagAttribute.HIDE;
        }
        otherNamesFontSizeSpinner.setEnabled(showingName);
        applyChanges();
    }

    private void showOtherNamesCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showOtherNamesCheckBoxItemStateChanged
        JCheckBox cb = (JCheckBox) evt.getSource();

        updateOthersNameTag(cb.isSelected());
    }//GEN-LAST:event_showOtherNamesCheckBoxItemStateChanged

    private void myNameFontSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_myNameFontSizeSpinnerStateChanged
        updateMyNameTag(showMyNameCheckBox.isSelected());
    }//GEN-LAST:event_myNameFontSizeSpinnerStateChanged

    private void otherNamesFontSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_otherNamesFontSizeSpinnerStateChanged
        updateOthersNameTag(showOtherNamesCheckBox.isSelected());
}//GEN-LAST:event_otherNamesFontSizeSpinnerStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel avatarNamesLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel myFontSizeLabel;
    private javax.swing.JSpinner myNameFontSizeSpinner;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel otherFontSizeLabel;
    private javax.swing.JSpinner otherNamesFontSizeSpinner;
    private javax.swing.JCheckBox showMyNameCheckBox;
    private javax.swing.JCheckBox showOtherNamesCheckBox;
    // End of variables declaration//GEN-END:variables
}
