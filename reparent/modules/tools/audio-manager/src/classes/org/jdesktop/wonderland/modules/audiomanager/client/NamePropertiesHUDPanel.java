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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
 */
public class NamePropertiesHUDPanel extends javax.swing.JPanel {

    private PresenceInfo presenceInfo;
    private PropertyChangeSupport listeners;

    private enum NameTagAttribute {

        HIDE,
        SMALL_FONT,
        REGULAR_FONT,
        LARGE_FONT
    };
    private NameTagAttribute originalMyNameTagAttribute = NameTagAttribute.REGULAR_FONT;
    private NameTagAttribute myNameTagAttribute = NameTagAttribute.REGULAR_FONT;
    private NameTagAttribute originalOtherNameTagAttributes = NameTagAttribute.REGULAR_FONT;
    private NameTagAttribute otherNameTagAttributes = NameTagAttribute.REGULAR_FONT;

    public NamePropertiesHUDPanel() {
        initComponents();
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
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
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
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
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
                            presenceInfo.userID.getUsername(), presenceInfo.usernameAlias);
                    break;

                case SMALL_FONT:
                    NameTagNode.setMyNameTag(EventType.SMALL_FONT,
                            presenceInfo.userID.getUsername(), presenceInfo.usernameAlias);
                    break;

                case REGULAR_FONT:
                    NameTagNode.setMyNameTag(EventType.REGULAR_FONT,
                            presenceInfo.userID.getUsername(), presenceInfo.usernameAlias);
                    break;

                case LARGE_FONT:
                    NameTagNode.setMyNameTag(EventType.LARGE_FONT,
                            presenceInfo.userID.getUsername(), presenceInfo.usernameAlias);
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
                        presenceInfo.userID.getUsername(), presenceInfo.usernameAlias);
                OrbCell.makeOrbsVisible(false);
                break;

            case SMALL_FONT:
                NameTagNode.setOtherNameTags(EventType.SMALL_FONT,
                        presenceInfo.userID.getUsername(), presenceInfo.usernameAlias);
                OrbCell.makeOrbsVisible(true);
                break;

            case REGULAR_FONT:
                NameTagNode.setOtherNameTags(EventType.REGULAR_FONT,
                        presenceInfo.userID.getUsername(), presenceInfo.usernameAlias);
                OrbCell.makeOrbsVisible(true);
                break;

            case LARGE_FONT:
                NameTagNode.setOtherNameTags(EventType.LARGE_FONT,
                        presenceInfo.userID.getUsername(), presenceInfo.usernameAlias);
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
        myNameFontSizeComboBox = new javax.swing.JComboBox();
        showOtherNamesCheckBox = new javax.swing.JCheckBox();
        otherFontSizeLabel = new javax.swing.JLabel();
        othersNameFontSizeComboBox = new javax.swing.JComboBox();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        avatarNamesLabel.setText("Avatar Names");

        showMyNameCheckBox.setSelected(true);
        showMyNameCheckBox.setText("Show my name");
        showMyNameCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showMyNameCheckBoxItemStateChanged(evt);
            }
        });

        myFontSizeLabel.setText("Font size:");

        myNameFontSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Small", "Regular", "Large" }));
        myNameFontSizeComboBox.setSelectedIndex(1);
        myNameFontSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myNameFontSizeComboBoxActionPerformed(evt);
            }
        });

        showOtherNamesCheckBox.setSelected(true);
        showOtherNamesCheckBox.setText("Show other's names");
        showOtherNamesCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showOtherNamesCheckBoxItemStateChanged(evt);
            }
        });

        otherFontSizeLabel.setText("Font size:");

        othersNameFontSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Small", "Regular", "Large" }));
        othersNameFontSizeComboBox.setSelectedIndex(1);
        othersNameFontSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                othersNameFontSizeComboBoxActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(showMyNameCheckBox)
                            .add(showOtherNamesCheckBox)))
                    .add(layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(avatarNamesLabel))
                    .add(layout.createSequentialGroup()
                        .add(37, 37, 37)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(cancelButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(okButton)
                                .add(24, 24, 24))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, myFontSizeLabel)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, otherFontSizeLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(othersNameFontSizeComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(myNameFontSizeComboBox, 0, 114, Short.MAX_VALUE))))))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(avatarNamesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showMyNameCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myFontSizeLabel)
                    .add(myNameFontSizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showOtherNamesCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(otherFontSizeLabel)
                    .add(othersNameFontSizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(okButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        listeners.firePropertyChange("ok", new String(""), null);
}//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        listeners.firePropertyChange("cancel", new String(""), null);
}//GEN-LAST:event_cancelButtonActionPerformed

    private void myNameFontSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myNameFontSizeComboBoxActionPerformed
        JComboBox cb = (JComboBox) evt.getSource();

        updateMyNameTag(true);
}//GEN-LAST:event_myNameFontSizeComboBoxActionPerformed

    private void othersNameFontSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_othersNameFontSizeComboBoxActionPerformed
        JComboBox cb = (JComboBox) evt.getSource();

        updateOthersNameTag(true);
    }//GEN-LAST:event_othersNameFontSizeComboBoxActionPerformed

    private void updateMyNameTag(boolean showingName) {
        if (showingName) {
            switch (myNameFontSizeComboBox.getSelectedIndex()) {
                case 0:
                    myNameTagAttribute = NameTagAttribute.SMALL_FONT;
                    break;
                case 1:
                    myNameTagAttribute = NameTagAttribute.REGULAR_FONT;
                    break;
                case 2:
                    myNameTagAttribute = NameTagAttribute.LARGE_FONT;
                    break;
                default:
                    break;
            }
            myNameFontSizeComboBox.setEnabled(true);
        } else {
            myNameTagAttribute = NameTagAttribute.HIDE;
            myNameFontSizeComboBox.setEnabled(false);
        }
        applyChanges();
    }

    private void showMyNameCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showMyNameCheckBoxItemStateChanged
        JCheckBox cb = (JCheckBox) evt.getSource();

        updateMyNameTag(cb.isSelected());
    }//GEN-LAST:event_showMyNameCheckBoxItemStateChanged

    private void updateOthersNameTag(boolean showingName) {
        if (showingName) {
            switch (othersNameFontSizeComboBox.getSelectedIndex()) {
                case 0:
                    otherNameTagAttributes = NameTagAttribute.SMALL_FONT;
                    break;
                case 1:
                    otherNameTagAttributes = NameTagAttribute.REGULAR_FONT;
                    break;
                case 2:
                    otherNameTagAttributes = NameTagAttribute.LARGE_FONT;
                    break;
                default:
                    break;
            }
            othersNameFontSizeComboBox.setEnabled(true);
        } else {
            otherNameTagAttributes = NameTagAttribute.HIDE;
            othersNameFontSizeComboBox.setEnabled(false);
        }
        applyChanges();
    }

    private void showOtherNamesCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showOtherNamesCheckBoxItemStateChanged
        JCheckBox cb = (JCheckBox) evt.getSource();

        updateOthersNameTag(cb.isSelected());
    }//GEN-LAST:event_showOtherNamesCheckBoxItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel avatarNamesLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel myFontSizeLabel;
    private javax.swing.JComboBox myNameFontSizeComboBox;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel otherFontSizeLabel;
    private javax.swing.JComboBox othersNameFontSizeComboBox;
    private javax.swing.JCheckBox showMyNameCheckBox;
    private javax.swing.JCheckBox showOtherNamesCheckBox;
    // End of variables declaration//GEN-END:variables
}
