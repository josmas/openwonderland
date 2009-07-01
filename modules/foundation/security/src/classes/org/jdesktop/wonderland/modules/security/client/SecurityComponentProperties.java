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

package org.jdesktop.wonderland.modules.security.client;

import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.security.common.ActionDTO;
import org.jdesktop.wonderland.modules.security.common.CellPermissions;
import org.jdesktop.wonderland.modules.security.common.Permission;
import org.jdesktop.wonderland.modules.security.common.Principal;
import org.jdesktop.wonderland.modules.security.common.Principal.Type;
import org.jdesktop.wonderland.modules.security.common.SecurityComponentServerState;

/**
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@PropertiesFactory(SecurityComponentServerState.class)
public class SecurityComponentProperties extends JPanel 
       implements PropertiesFactorySPI
{
    private CellPropertiesEditor editor = null;
    private PermTableModel perms = new PermTableModel();
    private EditPermsTableModel editPerms = null;

    // The original permissions before any editing took place
    private CellPermissions originalPermissions = null;

    /** Creates new form SecurityComponentProperties */
    public SecurityComponentProperties() {
        // Initialize the GUI
        initComponents();

        permsTable.setModel(perms);
        permsTable.getSelectionModel().addListSelectionListener(
                new RemoveButtonSelectionListener());
        permsTable.getSelectionModel().addListSelectionListener(
                new EditButtonSelectionListener());
        perms.addTableModelListener(new TableDirtyListener());

        editPermsTable.setDefaultRenderer(ActionDTO.class,
                                          new PermissionTableCellRenderer());
        editPermsTable.setDefaultRenderer(Permission.Access.class,
                                          new AccessTableCellRenderer());
        editPermsTable.setDefaultEditor(Permission.Access.class,
                                        new AccessTableCellEditor());

        addNameTF.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkButton();
            }

            public void removeUpdate(DocumentEvent e) {
                checkButton();
            }

            public void changedUpdate(DocumentEvent e) {
                checkButton();
            }

            private void checkButton() {
                String text = addNameTF.getText();
                addOKButton.setEnabled(text != null && text.length() > 0);
            }
        });
    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return "Security";
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
        CellServerState cellServerState = editor.getCellServerState();
        SecurityComponentServerState state =  (SecurityComponentServerState)
                cellServerState.getComponentServerState(SecurityComponentServerState.class);

        // set the lists up based on the model
        originalPermissions = state.getPermissions();
        perms.fromPermissions(originalPermissions);
        permsTable.repaint();
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
        // component.
        CellServerState cellServerState = editor.getCellServerState();
        SecurityComponentServerState state = (SecurityComponentServerState)
                cellServerState.getComponentServerState(SecurityComponentServerState.class);
        if (state == null) {
            state = new SecurityComponentServerState();
        }

        // Update the permissions state and add to the update list
        CellPermissions out = perms.toPermissions();
        state.setPermissions(out);
        editor.addToUpdateList(state);
    }

    /**
     * @inheritDoc()
     */
    public void restore() {
        // Restore the GUI to the original values
        perms.fromPermissions(originalPermissions);
        permsTable.repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addPrincipalDialog = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        addUserRB = new javax.swing.JRadioButton();
        addGroupRB = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        addNameTF = new javax.swing.JTextField();
        addCancelButton = new javax.swing.JButton();
        addOKButton = new javax.swing.JButton();
        addSearchButton = new javax.swing.JButton();
        addBG = new javax.swing.ButtonGroup();
        editPermsDialog = new javax.swing.JDialog();
        editPermsOKButton = new javax.swing.JButton();
        editPermsCancelButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        editPermsTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        permsTable = new javax.swing.JTable();
        editButton = new javax.swing.JButton();

        jLabel2.setText("Type:");

        addBG.add(addUserRB);
        addUserRB.setSelected(true);
        addUserRB.setText("User");

        addBG.add(addGroupRB);
        addGroupRB.setText("Group");

        jLabel3.setText("Name:");

        addCancelButton.setText("Cancel");
        addCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCancelButtonActionPerformed(evt);
            }
        });

        addOKButton.setText("OK");
        addOKButton.setEnabled(false);
        addOKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addOKButtonActionPerformed(evt);
            }
        });

        addSearchButton.setText("Search...");
        addSearchButton.setEnabled(false);

        org.jdesktop.layout.GroupLayout addPrincipalDialogLayout = new org.jdesktop.layout.GroupLayout(addPrincipalDialog.getContentPane());
        addPrincipalDialog.getContentPane().setLayout(addPrincipalDialogLayout);
        addPrincipalDialogLayout.setHorizontalGroup(
            addPrincipalDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(addPrincipalDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(addPrincipalDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addPrincipalDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(addPrincipalDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(addPrincipalDialogLayout.createSequentialGroup()
                                .add(6, 6, 6)
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(addUserRB)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(addGroupRB)
                                .add(178, 178, 178))
                            .add(addPrincipalDialogLayout.createSequentialGroup()
                                .add(jLabel3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(addNameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(addSearchButton)
                                .addContainerGap()))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, addPrincipalDialogLayout.createSequentialGroup()
                            .add(addCancelButton)
                            .addContainerGap()))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, addPrincipalDialogLayout.createSequentialGroup()
                        .add(addOKButton)
                        .add(99, 99, 99))))
        );
        addPrincipalDialogLayout.setVerticalGroup(
            addPrincipalDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(addPrincipalDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(addPrincipalDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(addNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(addSearchButton))
                .add(addPrincipalDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, addPrincipalDialogLayout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(addPrincipalDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel2)
                            .add(addUserRB)
                            .add(addGroupRB))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(addOKButton))
                    .add(addCancelButton))
                .addContainerGap())
        );

        editPermsOKButton.setText("OK");
        editPermsOKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editPermsOKButtonActionPerformed(evt);
            }
        });

        editPermsCancelButton.setText("Cancel");
        editPermsCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editPermsCancelButtonActionPerformed(evt);
            }
        });

        editPermsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Permission"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(editPermsTable);

        org.jdesktop.layout.GroupLayout editPermsDialogLayout = new org.jdesktop.layout.GroupLayout(editPermsDialog.getContentPane());
        editPermsDialog.getContentPane().setLayout(editPermsDialogLayout);
        editPermsDialogLayout.setHorizontalGroup(
            editPermsDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(editPermsDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(editPermsDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, editPermsDialogLayout.createSequentialGroup()
                        .add(editPermsCancelButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editPermsOKButton))
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE))
                .addContainerGap())
        );
        editPermsDialogLayout.setVerticalGroup(
            editPermsDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, editPermsDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(editPermsDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(editPermsOKButton)
                    .add(editPermsCancelButton))
                .addContainerGap())
        );

        jLabel1.setText("Permissions:");

        addButton.setText("Add...");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setText("Remove");
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        permsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Type", "Name", "Owner?", "Permissions"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        permsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(permsTable);

        editButton.setText("Edit...");
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(editButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(addButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(removeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editButton))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        addNameTF.setText("");
        addUserRB.setSelected(true);
        addPrincipalDialog.pack();
        addPrincipalDialog.setVisible(true);
}//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int curRow = permsTable.getSelectedRow();
        perms.removeRow(curRow);
}//GEN-LAST:event_removeButtonActionPerformed

    private void addCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCancelButtonActionPerformed
        addPrincipalDialog.setVisible(false);
    }//GEN-LAST:event_addCancelButtonActionPerformed

    private void addOKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addOKButtonActionPerformed
        addPrincipalDialog.setVisible(false);

        Type type;
        if (addUserRB.isSelected()) {
            type = Type.USER;
        } else {
            type = Type.GROUP;
        }

        perms.addRow(new Principal(addNameTF.getText(), type));
    }//GEN-LAST:event_addOKButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        int curRow = permsTable.getSelectedRow();

        Principal p = perms.getPrincipal(curRow);
        SortedSet<Permission> ps = perms.getPerms(curRow);
        Set<ActionDTO> aps = perms.getAllPerms();

        // clear existing rows
        if (editPerms != null) {
            editPerms.clear();
        }

        editPerms = new EditPermsTableModel(p, aps, ps);
        editPermsTable.setModel(editPerms);
        editPermsTable.clearSelection();

        editPermsDialog.pack();
        editPermsDialog.setVisible(true);
        editPermsTable.repaint();
    }//GEN-LAST:event_editButtonActionPerformed

    private void editPermsOKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editPermsOKButtonActionPerformed
        editPermsDialog.setVisible(false);

        perms.setPerms(editPerms.getPrincipal(), editPerms.toPermissions());
    }//GEN-LAST:event_editPermsOKButtonActionPerformed

    private void editPermsCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editPermsCancelButtonActionPerformed
        editPermsDialog.setVisible(false);
    }//GEN-LAST:event_editPermsCancelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup addBG;
    private javax.swing.JButton addButton;
    private javax.swing.JButton addCancelButton;
    private javax.swing.JRadioButton addGroupRB;
    private javax.swing.JTextField addNameTF;
    private javax.swing.JButton addOKButton;
    private javax.swing.JDialog addPrincipalDialog;
    private javax.swing.JButton addSearchButton;
    private javax.swing.JRadioButton addUserRB;
    private javax.swing.JButton editButton;
    private javax.swing.JButton editPermsCancelButton;
    private javax.swing.JDialog editPermsDialog;
    private javax.swing.JButton editPermsOKButton;
    private javax.swing.JTable editPermsTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable permsTable;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    class PrincipalCellRender extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, 
                            int index, boolean isSelected, boolean cellHasFocus) 
        {
            Principal p = (Principal) value;
            
            return super.getListCellRendererComponent(list, p.getId(), index, 
                                                      isSelected, cellHasFocus);
        }
    }
    
    class PermTableModel extends AbstractTableModel {
        private List<Principal> principals = new LinkedList<Principal>();
        private List<Boolean> owner = new LinkedList<Boolean>();
        private List<SortedSet<Permission>> perms = new LinkedList<SortedSet<Permission>>();
        private Set<ActionDTO> allPerms = new LinkedHashSet<ActionDTO>();

        public void addRow(Principal p) {
            principals.add(p);
            owner.add(false);
            perms.add(new TreeSet<Permission>());
            
            this.fireTableRowsInserted(principals.size() - 1,
                                       principals.size() - 1);
        }
        
        public void removeRow(int index) {
            principals.remove(index);
            owner.remove(index);
            perms.remove(index);
            
            this.fireTableRowsDeleted(index, index);
        }

        public Principal getPrincipal(int index) {
            return principals.get(index);
        }

        public boolean isOwner(int index) {
            return owner.get(index);
        }

        public Set<ActionDTO> getAllPerms() {
            return allPerms;
        }

        public SortedSet<Permission> getPerms(int index) {
            return perms.get(index);
        }

        public void setPerms(Principal p, SortedSet<Permission> ps) {
            int index = principals.indexOf(p);
            if (index == -1) {
                return;
            }

            perms.set(index, ps);
            fireTableCellUpdated(index, 3);
        }

        public CellPermissions toPermissions() {
            CellPermissions out = new CellPermissions();
            
            for (int i = 0; i < principals.size(); i++) {
                Principal p = principals.get(i);
                if (owner.get(i)) {
                    out.getOwners().add(p);
                } else {
                    out.getPermissions().addAll(perms.get(i));
                }
            }
            
            return out;
        }
        
        public void fromPermissions(CellPermissions in) {
            clear();
            
            for (Principal p : in.getOwners()) {
                principals.add(p);
                owner.add(true);
                perms.add(new TreeSet<Permission>());
            }
            
            Map<Principal, SortedSet<Permission>> pm =
                    new LinkedHashMap<Principal, SortedSet<Permission>>();
            for (Permission p : in.getPermissions()) {
                SortedSet<Permission> ps = pm.get(p.getPrincipal());
                if (ps == null) {
                    ps = new TreeSet<Permission>();
                    pm.put(p.getPrincipal(), ps);
                }
                ps.add(p);
            }
            
            for (Entry<Principal, SortedSet<Permission>> e : pm.entrySet()) {
                principals.add(e.getKey());
                owner.add(false);
                perms.add(e.getValue());
            }

            allPerms = in.getAllActions();

            fireTableRowsInserted(0, principals.size());
            fireTableDataChanged();
        }

        public void clear() {
            int size = principals.size();

            principals.clear();
            owner.clear();
            perms.clear();

            fireTableRowsDeleted(0, size);
            fireTableDataChanged();
        }

        public int getRowCount() {
            return principals.size();
        }

        public int getColumnCount() {
            return 4;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Principal p;
           
            switch (columnIndex) {
                case 0:
                    p = principals.get(rowIndex);
                    return p.getType().name();
                case 1:
                    p = principals.get(rowIndex);
                    return p.getId();
                case 2:
                    return owner.get(rowIndex);
                case 3:
                    return perms.get(rowIndex).size();
                default:
                    throw new IllegalStateException("Request for unknown " +
                                                    "column " + columnIndex);
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 2:
                    owner.set(rowIndex, (Boolean) aValue);
                    fireTableCellUpdated(rowIndex, columnIndex);
                    return;
                default:
                    throw new IllegalStateException("Column " + columnIndex +
                                                    " not editable."); 
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0:
                    return String.class;
                case 1:
                    return String.class;
                case 2:
                    return Boolean.class;
                case 3:
                    return String.class;
                default:
                    throw new IllegalStateException("Unknown column " + column);
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Type";
                case 1:
                    return "Name";
                case 2:
                    return "Owner?";
                case 3:
                    return "Permissions";
                default:
                    throw new IllegalStateException("Unknown column " + column);
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == 2);
        }
    }

    class EditPermsTableModel extends AbstractTableModel {
        private Principal principal;
        private List<Permission.Access> perms;
        private List<ActionDTO> allPerms;

        public EditPermsTableModel(Principal principal,
                                   Set<ActionDTO> allPerms,
                                   SortedSet<Permission> sortedPerms)
        {
            this.principal = principal;
            this.allPerms = new ArrayList<ActionDTO>(allPerms);

            // order the permissions list correctly
            this.perms = new ArrayList<Permission.Access>(allPerms.size());
            for (ActionDTO a : allPerms) {
                Permission p = new Permission(principal, a, null);
                
                SortedSet<Permission> tail = sortedPerms.tailSet(p);
                if (tail.isEmpty() || !tail.first().equals(p)) {
                    perms.add(null);
                } else {
                    perms.add(tail.first().getAccess());
                }
            }
        }

        public Principal getPrincipal() {
            return principal;
        }

        public SortedSet<Permission> toPermissions() {
            SortedSet<Permission> out = new TreeSet<Permission>();
            for (int i = 0; i < allPerms.size(); i++) {
                ActionDTO action = allPerms.get(i);
                Permission.Access access = perms.get(i);

                if (access != null) {
                    out.add(new Permission(principal, action, access));
                }
            }

            return out;
        }

        public void clear() {
            int size = perms.size();

            perms.clear();
            allPerms.clear();

            fireTableRowsDeleted(0, size);
            fireTableDataChanged();
        }

        public int getRowCount() {
            return allPerms.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return allPerms.get(rowIndex);
                case 1:
                    return perms.get(rowIndex);
                default:
                    throw new IllegalArgumentException("Unknown column " +
                                                       columnIndex);
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 1:
                    perms.set(rowIndex, (Permission.Access) aValue);
                    fireTableCellUpdated(rowIndex, columnIndex);
                    return;
                default:
                    throw new IllegalStateException("Column " + columnIndex +
                                                    " not editable.");
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Permission";
                case 1:
                    return "Value";
                default:
                    throw new IllegalStateException("Unknown column " + column);
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0:
                    return ActionDTO.class;
                case 1:
                    return Permission.Access.class;
                default:
                    throw new IllegalStateException("Unknown column " + column);
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == 1);
        }
    }

    class PermissionTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column)
        {
            ActionDTO a = (ActionDTO) value;
            String name = a.getAction().getDisplayName();
            if (name == null) {
                name = a.getAction().getName();
            }

            Component c = super.getTableCellRendererComponent(table, name, isSelected,
                                                              hasFocus, row, column);
            if (c instanceof JComponent) {
                ((JComponent) c).setToolTipText(a.getAction().getToolTip());
            }

            return c;
        }

    }

    class AccessTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column)
        {
            Permission.Access a = (Permission.Access) value;
            String name = "Unspecified";

            if (a != null) {
                switch (a) {
                    case GRANT:
                        name = "Granted";
                        break;
                    case DENY:
                        name = "Denied";
                        break;
                }
            }

            return super.getTableCellRendererComponent(table, name, isSelected,
                                                       hasFocus, row, column);
        }
    }

    static class AccessTableCellEditor extends DefaultCellEditor {
        public AccessTableCellEditor() {
            super (createComboBox());
        }

        private static JComboBox createComboBox() {
            JComboBox jcb = new JComboBox(new Object[] {
                null, Permission.Access.GRANT, Permission.Access.DENY });
            jcb.setRenderer(new AccessCBRenderer());
            return jcb;
        }
    }

    static class AccessCBRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus)
        {
            Permission.Access a = (Permission.Access) value;
            String name = "Unspecified";

            if (a != null) {
                switch (a) {
                    case GRANT:
                        name = "Granted";
                        break;
                    case DENY:
                        name = "Denied";
                        break;
                }
            }

            return super.getListCellRendererComponent(list, name, index,
                                                      isSelected, cellHasFocus);
        }
    }

    class RemoveButtonSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            boolean enabled = false;
            
            if (!e.getValueIsAdjusting()) {
                enabled = (permsTable.getSelectedRow() >= 0);
            }
            
            removeButton.setEnabled(enabled);
        }        
    }

    class EditButtonSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            boolean enabled = false;

            if (!e.getValueIsAdjusting()) {
                int row = permsTable.getSelectedRow();
                enabled = (row >= 0 && !perms.isOwner(row));
            }

            editButton.setEnabled(enabled);
        }
    }
    
    class TableDirtyListener implements TableModelListener {
        public void tableChanged(TableModelEvent tme) {
            editor.setPanelDirty(SecurityComponentProperties.class, true);
        }
    }
}
