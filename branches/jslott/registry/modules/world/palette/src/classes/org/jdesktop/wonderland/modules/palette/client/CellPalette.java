/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package org.jdesktop.wonderland.modules.palette.client;

import com.jme.math.Vector3f;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import org.jdesktop.wonderland.client.cell.CellEditChannelConnection;
import org.jdesktop.wonderland.client.cell.registry.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup.Origin;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

/**
 *
 * @author  jordanslott
 */
public class CellPalette extends javax.swing.JFrame {

    /** Creates new form CellPalette */
    public CellPalette() {
        initComponents();
        
        CellRegistry registry = CellRegistry.getCellRegistry();
        Set<CellFactory> cellFactories = registry.getAllCellFactories();
        String cellNames[] = new String[cellFactories.size()];
        Iterator<CellFactory> it = cellFactories.iterator();
        int i = 0;
        while (it.hasNext() == true) {
            CellFactory cellFactory = it.next();
            try {
                cellNames[i] = cellFactory.getCellPaletteInfo().getDisplayName();
            } catch (java.lang.Exception excp) {
                cellNames[i] = "Not Here Yet";
            }
        }
        cellList.setListData(cellNames);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cellScrollPane = new javax.swing.JScrollPane();
        cellList = new javax.swing.JList();
        createButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Cell Palette");
        setName("cellFrame"); // NOI18N

        cellScrollPane.setViewportView(cellList);

        createButton.setText("Create");
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(cellScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(createButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(cellScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 226, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(createButton))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void createActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createActionPerformed

    // From the selected value, find the proper means to create the object
    String cellDisplayName = (String) cellList.getSelectedValue();
    CellFactory factory = getCellFactory(cellDisplayName);
    BasicCellSetup setup = factory.getDefaultCellSetup();
    Vector3f origin = new Vector3f(new Random().nextInt(10) - 5,
            new Random().nextInt(5), new Random().nextInt(10) - 5);
    setup.setOrigin(new Origin(origin));
    
    // Send the message to the server
    WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
    CellEditChannelConnection connection = (CellEditChannelConnection)session.getConnection(CellEditConnectionType.CLIENT_TYPE);
    CellCreateMessage msg = new CellCreateMessage(null, setup);
    connection.send(msg);
}//GEN-LAST:event_createActionPerformed

    /**
     * Returns the cell factory given its display name
     */
    private CellFactory getCellFactory(String name) {
        CellRegistry registry = CellRegistry.getCellRegistry();
        Set<CellFactory> cellFactories = registry.getAllCellFactories();
        Iterator<CellFactory> it = cellFactories.iterator();
        int i = 0;
        while (it.hasNext() == true) {
            CellFactory cellFactory = it.next();
            try {
                String cellName = cellFactory.getCellPaletteInfo().getDisplayName();
                if (cellName.equals(name) == true) {
                    return cellFactory;
                }
            } catch (java.lang.Exception excp) {
            }
        }
        return null;
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CellPalette().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList cellList;
    private javax.swing.JScrollPane cellScrollPane;
    private javax.swing.JButton createButton;
    // End of variables declaration//GEN-END:variables

}
