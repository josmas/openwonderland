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
package org.jdesktop.wonderland.modules.portal.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.Component;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.placemarks.api.client.PlacemarkRegistry;
import org.jdesktop.wonderland.modules.placemarks.api.client.PlacemarkRegistry.PlacemarkType;
import org.jdesktop.wonderland.modules.placemarks.api.client.PlacemarkRegistryFactory;
import org.jdesktop.wonderland.modules.placemarks.api.common.Placemark;
import org.jdesktop.wonderland.modules.portal.common.PortalComponentServerState;

/**
 * A property sheet for the Portal component, allowing users to enter the
 * destination URL, location, and look direction.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
@PropertiesFactory(PortalComponentServerState.class)
public class PortalComponentProperties extends JPanel
        implements PropertiesFactorySPI {

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/portal/client/resources/Bundle");
    private static final Logger LOGGER =
            Logger.getLogger(PortalComponentProperties.class.getName());
    
    // The main editor object for the Cell Editor
    private CellPropertiesEditor editor = null;

    // The original values for all of the fields. We use the convention that
    // if empty, an empty string ("") is used, rather than null.
    private String origServerURL = "";
    private String origX = "";
    private String origY = "";
    private String origZ = "";
    private String origAngle = "";

    /** Creates new form PortalComponentProperties */
    public PortalComponentProperties() {
        // Initialize the GUI
        initComponents();

        // Listen for changes to the text fields
        TextFieldListener listener = new TextFieldListener();
        urlTF.getDocument().addDocumentListener(listener);
        locX.getDocument().addDocumentListener(listener);
        locY.getDocument().addDocumentListener(listener);
        locZ.getDocument().addDocumentListener(listener);
        angleTF.getDocument().addDocumentListener(listener);

        // set renderer for placemarks
        placemarkCB.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Placemark placemark = (Placemark) value;
                String name = placemark == null ? "" : placemark.getName();
                return super.getListCellRendererComponent(list, name,
                        index, isSelected, cellHasFocus);
            }
        });
    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return BUNDLE.getString("Portal");
    }

    /**
     * @inheritDoc()
     */
    public JPanel getPropertiesJPanel() {
        return this;
    }

    /**
     * @inheritDoc()
     */
    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }

    /**
     * @inheritDoc()
     */
    public void open() {
        // Fetch the current state from the Cell. If none exist, then just
        // return.
        CellServerState cellServerState = editor.getCellServerState();
        PortalComponentServerState state = (PortalComponentServerState)
                cellServerState.getComponentServerState(
                PortalComponentServerState.class);
        if (state == null) {
            return;
        }

        // Otherwise, update the values of the text fields and store away the
        // original values. We use the convention that an empty entry is
        // represented by an empty string ("") rather than null.

        // Fetch the destination URL from the server state. If the original
        // state is null, then convert it into an empty string and update the
        // text field.
        origServerURL = state.getServerURL();
        if (origServerURL == null) {
            origServerURL = "";
        }
        urlTF.setText(origServerURL);

        // Fetch the destination location from the server state. If the value
        // is null, then set the original values and text fields to empty
        // strings.
        Vector3f origin = state.getLocation();
        if (origin != null) {
            origX = String.valueOf(origin.x);
            origY = String.valueOf(origin.y);
            origZ = String.valueOf(origin.z);
        } else {
            origX = "";
            origY = "";
            origZ = "";
        }
        locX.setText(origX);
        locY.setText(origY);
        locZ.setText(origZ);

        // Fetc the destination look direction from the server state. If the
        // value is null, then set the original value and text field to an
        // empty string.
        Quaternion lookAt = state.getLook();
        if (lookAt != null) {
            double angle = Math.toDegrees(lookAt.toAngleAxis(new Vector3f()));
            origAngle = String.valueOf(angle);
        } else {
            origAngle = "";
        }
        angleTF.setText(origAngle);
    }

    /**
     * @inheritDoc()
     */
    public void close() {
        // Do nothing
    }

    /**
     * @inheritDoc()
     */
    public void apply() {
        // Figure out whether there already exists a server state for the
        // component. If not, then create one.
        CellServerState cellServerState = editor.getCellServerState();
        PortalComponentServerState state = 
                (PortalComponentServerState) cellServerState.getComponentServerState(
                PortalComponentServerState.class);
        if (state == null) {
            state = new PortalComponentServerState();
        }

        // Set the values in the server state from the text fields. If the text
        // fields are empty, they will return an empty string (""), this is
        // converted to null to set in the server state.

        // Fetch the destination URL from the text field, and convert an empty
        // string into a null.
        String serverURL = urlTF.getText().trim();
        if (serverURL.length() == 0) {
            serverURL = null;
        }
        state.setServerURL(serverURL);

        // Fetch the destination location from the text fields, and convert an
        // empty string into 0.0 indivudually.
        String xstr = locX.getText().trim();
        if (xstr.length() == 0) {
            xstr = "0.0";
        }
        
        String ystr = locY.getText().trim();
        if (ystr.length() == 0) {
            ystr = "0.0";
        }

        String zstr = locZ.getText().trim();
        if (zstr.length() == 0) {
            zstr = "0.0";
        }

        // Set the location on the server state
        Vector3f location = new Vector3f();
        location.x = Float.parseFloat(xstr);
        location.y = Float.parseFloat(ystr);
        location.z = Float.parseFloat(zstr);
        state.setLocation(location);

        // Set the destination look direction from the text field. If the text
        // field is empty, then set the server state as a zero rotation.
        Quaternion look = new Quaternion();
        String anglestr = angleTF.getText().trim();
        if (anglestr.length() != 0) {
            Vector3f axis = new Vector3f(0.0f, 1.0f, 0.0f);
            double angle = Math.toRadians(Float.parseFloat(anglestr));
            look.fromAngleAxis((float) angle, axis);
        }
        state.setLook(look);
        editor.addToUpdateList(state);
    }

    /**
     * @inheritDoc()
     */
    public void restore() {
        // Restore from the originally stored values.
        urlTF.setText(origServerURL);
        locX.setText(origX);
        locY.setText(origY);
        locZ.setText(origZ);
        angleTF.setText(origAngle);
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor.
     */
    class TextFieldListener implements DocumentListener {

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
            if (editor == null) {
                return;
            }

            boolean clean = urlTF.getText().equals(origServerURL);
            clean &= locX.getText().trim().equals(origX);
            clean &= locY.getText().trim().equals(origY);
            clean &= locZ.getText().trim().equals(origZ);
            clean &= angleTF.getText().trim().equals(origAngle);

            editor.setPanelDirty(PortalComponentProperties.class, !clean);
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

        placemarkDialog = new javax.swing.JDialog();
        jLabel6 = new javax.swing.JLabel();
        placemarkCB = new javax.swing.JComboBox();
        placemarkCancelB = new javax.swing.JButton();
        placemarkSetB = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        urlTF = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        locX = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        locY = new javax.swing.JTextField();
        locZ = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        angleTF = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        fromPlacemarkB = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/portal/client/resources/Bundle"); // NOI18N
        jLabel6.setText(bundle.getString("PortalComponentProperties.jLabel6.text")); // NOI18N

        placemarkCancelB.setText(bundle.getString("PortalComponentProperties.placemarkCancelB.text")); // NOI18N
        placemarkCancelB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                placemarkCancelBActionPerformed(evt);
            }
        });

        placemarkSetB.setText(bundle.getString("PortalComponentProperties.placemarkSetB.text")); // NOI18N
        placemarkSetB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                placemarkSetBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout placemarkDialogLayout = new org.jdesktop.layout.GroupLayout(placemarkDialog.getContentPane());
        placemarkDialog.getContentPane().setLayout(placemarkDialogLayout);
        placemarkDialogLayout.setHorizontalGroup(
            placemarkDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(placemarkDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(placemarkDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(placemarkDialogLayout.createSequentialGroup()
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(placemarkCB, 0, 162, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, placemarkDialogLayout.createSequentialGroup()
                        .add(placemarkSetB)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(placemarkCancelB)))
                .addContainerGap())
        );
        placemarkDialogLayout.setVerticalGroup(
            placemarkDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(placemarkDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(placemarkDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(placemarkCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(placemarkDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(placemarkCancelB)
                    .add(placemarkSetB))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setText(bundle.getString("PortalComponentProperties.jLabel1.text")); // NOI18N

        jLabel2.setText(bundle.getString("PortalComponentProperties.jLabel2.text")); // NOI18N

        jLabel3.setText(bundle.getString("PortalComponentProperties.jLabel3.text")); // NOI18N

        jLabel4.setText(bundle.getString("PortalComponentProperties.jLabel4.text")); // NOI18N

        jLabel5.setText(bundle.getString("PortalComponentProperties.jLabel5.text")); // NOI18N

        jLabel10.setText(bundle.getString("PortalComponentProperties.jLabel10.text")); // NOI18N

        fromPlacemarkB.setText(bundle.getString("PortalComponentProperties.fromPlacemarkB.text")); // NOI18N
        fromPlacemarkB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromPlacemarkBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jLabel1)
                        .add(jLabel2)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel5)))
                    .add(jLabel10))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(urlTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, locX)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, locY)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, angleTF)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, locZ, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE))
                    .add(fromPlacemarkB))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(urlTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(fromPlacemarkB))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(locX, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(locY, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(locZ, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(angleTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void placemarkSetBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_placemarkSetBActionPerformed
        placemarkDialog.setVisible(false);

        // get the currently selected placemark
        Placemark pm = (Placemark) placemarkCB.getSelectedItem();

        if (pm == null) {
            LOGGER.warning("null placemark selected!");
            return;
        }

        // set values
        urlTF.setText(pm.getUrl());
        locX.setText(String.valueOf(pm.getX()));
        locY.setText(String.valueOf(pm.getY()));
        locZ.setText(String.valueOf(pm.getZ()));

        // convert angle properly
        angleTF.setText(String.valueOf(Math.toDegrees(pm.getAngle())));
    }//GEN-LAST:event_placemarkSetBActionPerformed

    private void placemarkCancelBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_placemarkCancelBActionPerformed
        placemarkDialog.setVisible(false);
    }//GEN-LAST:event_placemarkCancelBActionPerformed

    private void fromPlacemarkBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromPlacemarkBActionPerformed
        // display the list of placemarks
        PlacemarkRegistry reg = PlacemarkRegistryFactory.getInstance();
        Vector<Placemark> allPlacemarks = new Vector<Placemark>();
        allPlacemarks.addAll(reg.getAllPlacemarks(PlacemarkType.SYSTEM));
        allPlacemarks.addAll(reg.getAllPlacemarks(PlacemarkType.USER));

        placemarkCB.setModel(new DefaultComboBoxModel(allPlacemarks));
        placemarkDialog.pack();
        placemarkDialog.setLocationRelativeTo(this);
        placemarkDialog.setVisible(true);
    }//GEN-LAST:event_fromPlacemarkBActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField angleTF;
    private javax.swing.JButton fromPlacemarkB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTextField locX;
    private javax.swing.JTextField locY;
    private javax.swing.JTextField locZ;
    private javax.swing.JComboBox placemarkCB;
    private javax.swing.JButton placemarkCancelB;
    private javax.swing.JDialog placemarkDialog;
    private javax.swing.JButton placemarkSetB;
    private javax.swing.JTextField urlTF;
    // End of variables declaration//GEN-END:variables
}
