/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */

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
package org.jdesktop.wonderland.modules.userlist.client.views;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeListener;
import org.jdesktop.wonderland.modules.orb.client.cell.OrbCell;


/**
 * A panel for selecting display properties for avatar name tags.
 *
 * @author jp
 * @author nsimpson
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 * @author JagWire
 */
public class NamePropertiesPanel extends javax.swing.JPanel
    implements NamePropertiesView {

    private static final Logger LOGGER =  Logger.getLogger(NamePropertiesPanel.class.getName());

    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/userlist/client/resources/Bundle");
    private final static String SMALL_STRING = BUNDLE.getString("Small");
    private final static String REGULAR_STRING = BUNDLE.getString("Regular");
    private final static String LARGE_STRING = BUNDLE.getString("Large");

    public static enum NameTagAttribute {
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

    public NamePropertiesPanel() {
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
    
      public void makeOrbsVisible(boolean visible) {
        OrbCell.makeOrbsVisible(visible);
    }

    public NameTagAttribute getMyNameTagAttribute() {
        return myNameTagAttribute;
    }

    public NameTagAttribute getMyOriginalNameTagAttribute() {
        return originalMyNameTagAttribute;
    }

    public NameTagAttribute getOthersNameTagAttribute() {
        return otherNameTagAttributes;
    }

    public NameTagAttribute getOthersOriginalNameTagAttribute() {
        return originalOtherNameTagAttributes;
    }

    public void setMyOriginalNameTagAttribute(NameTagAttribute nta) {
        originalMyNameTagAttribute = nta;
    }

    public void setOthersOriginalNameTagAttributes(NameTagAttribute nta) {
       originalOtherNameTagAttributes = nta;
    }
    
    
 
    public void addOKButtonActionListener(ActionListener listener) {
        okButton.addActionListener(listener);
    }

    public void addCancelButtonActionListener(ActionListener listener) {
        cancelButton.addActionListener(listener);
    }

    public void addShowMyNameItemListener(ItemListener listener) {
        showMyNameCheckBox.addItemListener(listener);
    }

    public void addShowOthersNamesItemListener(ItemListener listener) {
        showOtherNamesCheckBox.addItemListener(listener);
    }

    public void addMyFontSizeChangeListener(ChangeListener listener) {
        myNameFontSizeSpinner.addChangeListener(listener);
    }

    public void addOthersFontSizeChangeListener(ChangeListener listener) {
        otherNamesFontSizeSpinner.addChangeListener(listener);
    }

    public JCheckBox getShowMyNameCheckbox() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return showMyNameCheckBox;
    }

    public JCheckBox getShowOthersNamesCheckbox() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return showOtherNamesCheckBox;
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
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/userlist/client/resources/Bundle"); // NOI18N
        avatarNamesLabel.setText(bundle.getString("ChangeNameHUDPanel.avatarNamesLabel.text")); // NOI18N

        showMyNameCheckBox.setSelected(true);
        showMyNameCheckBox.setText(bundle.getString("NamePropertiesHUDPanel.showMyNameCheckBox.text")); // NOI18N

        myFontSizeLabel.setText(bundle.getString("NamePropertiesHUDPanel.myFontSizeLabel.text")); // NOI18N

        showOtherNamesCheckBox.setSelected(true);
        showOtherNamesCheckBox.setText(bundle.getString("NamePropertiesHUDPanel.showOtherNamesCheckBox.text")); // NOI18N

        otherFontSizeLabel.setText(bundle.getString("NamePropertiesHUDPanel.otherFontSizeLabel.text")); // NOI18N

        cancelButton.setText(bundle.getString("NamePropertiesHUDPanel.cancelButton.text")); // NOI18N

        okButton.setText(bundle.getString("NamePropertiesHUDPanel.okButton.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    public void updateMyNameTag(boolean showingName) {
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
//        applyChanges();
    }

    public void updateOthersNameTag(boolean showingName) {
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
//        applyChanges();
    }

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
