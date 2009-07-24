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
package org.jdesktop.wonderland.modules.placemarks.client;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.jdesktop.wonderland.modules.placemarks.client.PlacemarkRegistry.PlacemarkListener;
import org.jdesktop.wonderland.modules.placemarks.client.PlacemarkRegistry.PlacemarkType;
import org.jdesktop.wonderland.modules.placemarks.common.Placemark;
import org.jdesktop.wonderland.modules.placemarks.common.PlacemarkList;

/**
 * A JFrame to allow editing of the list of X Apps registered to appear in the
 * Cell Palettes.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class EditPlacemarksJFrame extends javax.swing.JFrame {

    private Logger logger = Logger.getLogger(EditPlacemarksJFrame.class.getName());
    private PlacemarkTableModel placemarksTableModel = null;
    private JTable placemarksTable = null;

    /** Creates new form EditPlacemarksJFrame */
    public EditPlacemarksJFrame() {
        initComponents();

        // Create the user table to display the user Placemarks
        PlacemarkList placemarkList = PlacemarkUtils.getUserPlacemarkList();
        placemarksTableModel = new PlacemarkTableModel(placemarkList.getPlacemarksAsList());
        placemarksTable = new JTable(placemarksTableModel);
        placemarksTable.setColumnSelectionAllowed(false);
        placemarksTable.setRowSelectionAllowed(true);
        placemarksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userScrollPane.setViewportView(placemarksTable);

        // Listen for changes to the select on the user table and enable/
        // disable the Edit/Remove buttons as a result.
        ListSelectionListener userListener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                boolean isRowSelected = placemarksTable.getSelectedRow() != -1;
                editButton.setEnabled(isRowSelected);
                removeButton.setEnabled(isRowSelected);
            }
        };
        placemarksTable.getSelectionModel().addListSelectionListener(userListener);

        // Upon a double-click, activated the Edit... button
        placemarksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editButton.doClick();
                }
            }

        });

        // Listen for changes in the list of registered Placemarks in the
        // system and update the table model accordingly
        PlacemarkRegistry registry = PlacemarkRegistry.getPlacemarkRegistry();
        registry.addPlacemarkRegistryListener(new PlacemarkListener() {
            public void placemarkAdded(Placemark placemark, PlacemarkType type) {
                if (type == PlacemarkType.USER) {
                    placemarksTableModel.addToPlacemarkList(placemark);
                }
            }

            public void placemarkRemoved(Placemark placemark, PlacemarkType type) {
                if (type == PlacemarkType.USER) {
                    placemarksTableModel.removeFromPlacemarkList(placemark);
                }
            }
        });
    }

    /**
     * A table model that displays a list of user-specific placemarks.
     */
    private class PlacemarkTableModel extends AbstractTableModel {

        private List<Placemark> placemarkList = null;

        /** Constructor, takes the list of registry items to display */
        public PlacemarkTableModel(List<Placemark> items) {
            placemarkList = items;
        }

        /**
         * @inheritDoc()
         */
        public int getRowCount() {
            return placemarkList.size();
        }

        /**
         * @inheritDoc()
         */
        public int getColumnCount() {
            return 4;
        }

        /**
         * @inheritDoc()
         */
        @Override
        public String getColumnName(int column) {
           switch (column) {
               case 0:
                   return "Name";
               case 1:
                   return "Server URL";
               case 2:
                   return "Location";
               case 3:
                   return "Look Angle";
               default:
                   return "";
           }
        }

        /**
         * @inheritDoc()
         */
        public Object getValueAt(int rowIndex, int columnIndex) {
            Placemark item = placemarkList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item.getName();
                case 1:
                    return item.getUrl();
                case 2:
                    return "(" + item.getX() + ", " + item.getY() + ", " + item.getZ() + ")";
                case 3:
                    return item.getAngle() + " degrees";
                default:
                    return "";
            }
        }

        /**
         * Returns the Nth placemark in the list.
         *
         * @param n The index into the list
         * @return Returns the nth placemark
         */
        public Placemark getPlacemark(int n) {
            return placemarkList.get(n);
        }

        /**
         * Resets the list of Placemarks and tells the table to update itself
         */
        public void setPlacemarkList(List<Placemark> list) {
            placemarkList = list;
            fireTableDataChanged();
        }

        /**
         * Add a placemark to the list
         */
        public void addToPlacemarkList(Placemark placemark) {
            placemarkList.add(placemark);
            fireTableDataChanged();
        }

        /**
         * Remove a placemark from the list
         */
        public void removeFromPlacemarkList(Placemark placemark) {
            placemarkList.remove(placemark);
            fireTableDataChanged();
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
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        userMainPanel = new javax.swing.JPanel();
        userScrollPane = new javax.swing.JScrollPane();
        userButtonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setTitle("Manage Placemarks");
        getContentPane().setLayout(new java.awt.GridLayout(1, 1));

        mainPanel.setLayout(new java.awt.GridLayout(1, 0));

        userMainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        userMainPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        userMainPanel.add(userScrollPane, gridBagConstraints);

        userButtonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        addButton.setText("Add...");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        userButtonPanel.add(addButton);

        editButton.setText("Edit...");
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        userButtonPanel.add(editButton);

        removeButton.setText("Remove");
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        userButtonPanel.add(removeButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        userMainPanel.add(userButtonPanel, gridBagConstraints);

        jLabel1.setText("(Placemarks are not fully functional yet)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        userMainPanel.add(jLabel1, gridBagConstraints);

        mainPanel.add(userMainPanel);

        getContentPane().add(mainPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // Fetch the list of known USER Placemark names
        PlacemarkRegistry registry = PlacemarkRegistry.getPlacemarkRegistry();
        Set<Placemark> placemarkSet = registry.getAllPlacemarks(PlacemarkType.USER);

        // When the Add... button is pressed popup a dialog asking for all of
        // the information. Add it to the repository and registry upon OK.
        AddEditPlacemarkJDialog dialog = new AddEditPlacemarkJDialog(this, true, placemarkSet);
        dialog.setTitle("Add Placemark");
        dialog.setLocationRelativeTo(this);
        dialog.pack();
        dialog.setVisible(true);

        if (dialog.getReturnStatus() == AddEditPlacemarkJDialog.RET_OK) {
            String name = dialog.getPlacemarkName();
            String url = dialog.getServerURL();
            float x = dialog.getLocationX();
            float y = dialog.getLocationY();
            float z = dialog.getLocationZ();
            float angle = dialog.getLookAtAngle();

            Placemark placemark = new Placemark(name, url, x, y, z, angle);
            try {
                PlacemarkUtils.addUserPlacemark(placemark);
            } catch (Exception excp) {
                logger.log(Level.WARNING, "Unable to add " + name + " to " +
                        " user's placemarks", excp);
                return;
            }

            // Tell the client-side registry of placemarks that a new one has
            // been added
            registry.registerPlacemark(placemark, PlacemarkType.USER);
        }
}//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // When the Remove..... button is pressed find out the name of the
        // Placemark being removed and remove it from the placemark list and
        // refresh the table.
        int row = placemarksTable.getSelectedRow();
        if (row == -1) {
            return;
        }
        String name = (String)placemarksTableModel.getValueAt(row, 0);
        Placemark placemark = placemarksTableModel.getPlacemark(row);

        try {
            PlacemarkUtils.removeUserPlacemark(name);
        } catch (Exception excp) {
            logger.log(Level.WARNING, "Unable to remove " + name + " from " +
                    " user's placemarks", excp);
            return;
        }

        // Tell the client-side registry of placemarks that a new one has
        // been added
        PlacemarkRegistry registry = PlacemarkRegistry.getPlacemarkRegistry();
        registry.unregisterPlacemark(placemark, PlacemarkType.USER);
}//GEN-LAST:event_removeButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        // Fetch the list of known USER Placemark names
        PlacemarkRegistry registry = PlacemarkRegistry.getPlacemarkRegistry();
        Set<Placemark> placemarkSet = registry.getAllPlacemarks(PlacemarkType.USER);

        // When the Edit..... button is pressed find the Placemark selected
        // and display a dialog with the values filled in.
        int row = placemarksTable.getSelectedRow();
        if (row == -1) {
            return;
        }
        Placemark placemark = placemarksTableModel.getPlacemark(row);

        // Display a dialog with the values in the Placemark. And if we wish
        // to update the values, then re-add the placemark. (Re-adding the
        // placemark should have the effect of updating its values.
        AddEditPlacemarkJDialog dialog = new AddEditPlacemarkJDialog(this, true, placemark, placemarkSet);
        dialog.setTitle("Edit Placemark");
        dialog.setLocationRelativeTo(this);
        dialog.pack();
        dialog.setVisible(true);

        if (dialog.getReturnStatus() == AddEditPlacemarkJDialog.RET_OK) {
            // First remove the old placemark.
            String oldName = placemark.getName();
            try {
                PlacemarkUtils.removeUserPlacemark(oldName);
            } catch (Exception excp) {
                logger.log(Level.WARNING, "Unable to remove " + oldName + " from " +
                        " user's placemarks", excp);
                return;
            }

            // Tell the client-side registry of placemarks that a new one has
            // been added
            registry.unregisterPlacemark(placemark, PlacemarkType.USER);

            // Create a new placemark with the new information.
            String name = dialog.getPlacemarkName();
            String url = dialog.getServerURL();
            float x = dialog.getLocationX();
            float y = dialog.getLocationY();
            float z = dialog.getLocationZ();
            float angle = dialog.getLookAtAngle();
            Placemark newPlacemark = new Placemark(name, url, x, y, z, angle);

            try {
                PlacemarkUtils.addUserPlacemark(newPlacemark);
            } catch (Exception excp) {
                logger.log(Level.WARNING, "Unable to add " + name + " to " +
                        " user's placemarks", excp);
                return;
            }

            // Tell the client-side registry of placemarks that a new one has
            // been added
            registry.registerPlacemark(newPlacemark, PlacemarkType.USER);
        }
    }//GEN-LAST:event_editButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JPanel userButtonPanel;
    private javax.swing.JPanel userMainPanel;
    private javax.swing.JScrollPane userScrollPane;
    // End of variables declaration//GEN-END:variables
}
