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
import javax.swing.JSpinner.DefaultEditor;
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
        myNameFontSizeSpinner.setValue("Regular");
        otherNamesFontSizeSpinner.setValue("Regular");
        ((DefaultEditor) myNameFontSizeSpinner.getEditor()).getTextField().setEditable(false);
        ((DefaultEditor) otherNamesFontSizeSpinner.getEditor()).getTextField().setEditable(false);
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
        showOtherNamesCheckBox = new javax.swing.JCheckBox();
        otherFontSizeLabel = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        myNameFontSizeSpinner = new javax.swing.JSpinner();
        otherNamesFontSizeSpinner = new javax.swing.JSpinner();

        avatarNamesLabel.setText("Avatar Names");

        showMyNameCheckBox.setSelected(true);
        showMyNameCheckBox.setText("Show my name");
        showMyNameCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showMyNameCheckBoxItemStateChanged(evt);
            }
        });

        myFontSizeLabel.setText("Font size:");

        showOtherNamesCheckBox.setSelected(true);
        showOtherNamesCheckBox.setText("Show other's names");
        showOtherNamesCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showOtherNamesCheckBoxItemStateChanged(evt);
            }
        });

        otherFontSizeLabel.setText("Font size:");

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

        myNameFontSizeSpinner.setModel(new javax.swing.SpinnerListModel(new String[] {"Small", "Regular", "Large"}));
        myNameFontSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                myNameFontSizeSpinnerStateChanged(evt);
            }
        });

        otherNamesFontSizeSpinner.setModel(new javax.swing.SpinnerListModel(new String[] {"Small", "Regular", "Large"}));
        otherNamesFontSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                otherNamesFontSizeSpinnerStateChanged(evt);
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
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, myFontSizeLabel)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, otherFontSizeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(myNameFontSizeSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .add(otherNamesFontSizeSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                        .add(24, 24, 24)))
                .add(14, 14, 14))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(55, Short.MAX_VALUE)
                .add(cancelButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(okButton)
                .add(53, 53, 53))
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

    private void updateMyNameTag(boolean showingName) {
        if (showingName) {
            if (myNameFontSizeSpinner.getValue().equals("Small")) {
                myNameTagAttribute = NameTagAttribute.SMALL_FONT;
            } else if (myNameFontSizeSpinner.getValue().equals("Regular")) {
                myNameTagAttribute = NameTagAttribute.REGULAR_FONT;
            } else if (myNameFontSizeSpinner.getValue().equals("Large")) {
                myNameTagAttribute = NameTagAttribute.LARGE_FONT;
            }

            myNameFontSizeSpinner.setEnabled(true);
        } else {
            myNameTagAttribute = NameTagAttribute.HIDE;
            myNameFontSizeSpinner.setEnabled(false);
        }
        applyChanges();
    }

    private void showMyNameCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showMyNameCheckBoxItemStateChanged
        JCheckBox cb = (JCheckBox) evt.getSource();

        updateMyNameTag(cb.isSelected());
    }//GEN-LAST:event_showMyNameCheckBoxItemStateChanged

    private void updateOthersNameTag(boolean showingName) {
        if (showingName) {
            if (otherNamesFontSizeSpinner.getValue().equals("Small")) {
                otherNameTagAttributes = NameTagAttribute.SMALL_FONT;
            } else if (otherNamesFontSizeSpinner.getValue().equals("Regular")) {
                otherNameTagAttributes = NameTagAttribute.REGULAR_FONT;
            } else if (otherNamesFontSizeSpinner.getValue().equals("Large")) {
                otherNameTagAttributes = NameTagAttribute.LARGE_FONT;
            }

            otherNamesFontSizeSpinner.setEnabled(true);
        } else {
            otherNameTagAttributes = NameTagAttribute.HIDE;
            otherNamesFontSizeSpinner.setEnabled(false);
        }
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
