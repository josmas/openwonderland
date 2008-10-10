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
package org.jdesktop.wonderland.client.jme.artimport;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.CellManager;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
import org.jdesktop.wonderland.client.cell.RootCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 *
 * @author  paulby
 */
public class CellViewerFrame extends javax.swing.JFrame {

    private ArrayList<Cell> rootCells = new ArrayList();
    private DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Root");
    private HashMap<Cell, DefaultMutableTreeNode> nodes = new HashMap();
    
    private boolean active = false;

    private CellViewerEventListener cellViewerListener;
    
    private static final Logger logger = Logger.getLogger(CellViewerFrame.class.getName());
    
    /** Creates new form CellViewerFrame */
    public CellViewerFrame(WonderlandSession session) {
        initComponents();
        cellViewerListener = new CellViewerEventListener();

        CellManager.getCellManager().addCellStatusChangeListener(new CellStatusChangeListener() {

            public void cellStatusChanged(Cell cell, CellStatus status) {
                DefaultMutableTreeNode node = nodes.get(cell);
                
                switch(status) {
                    case DISK :
                        if (node!=null)
                            ((DefaultTreeModel)cellTree.getModel()).removeNodeFromParent(node);
                        break;
                    case BOUNDS :
                        if (node==null) {
                            node = createJTreeNode(cell);
                        }
                        break;
                }
            }
            
        });
        
        refreshCells(session);
        ((DefaultTreeModel)cellTree.getModel()).setRoot(treeRoot);
        
        jmeTree.setCellRenderer(new JmeTreeCellRenderer());
        jmeTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                Object selectedNode = jmeTree.getLastSelectedPathComponent();
                System.out.println("Selected "+selectedNode);
            }
            
        });
        
        cellTree.setCellRenderer(new WonderlandCellRenderer());
        cellTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) cellTree.getLastSelectedPathComponent();
                System.out.println("Selected "+selectedNode);

                if (selectedNode.getUserObject() instanceof Cell) {
                    Cell cell = (Cell) selectedNode.getUserObject();
                    System.out.println("Cell "+cell.getName());

                    CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
                    if (renderer==null)
                        return;

                    showJMEGraph(((RenderComponent)renderer.getEntity().getComponent(RenderComponent.class)).getSceneRoot());
                } else if (selectedNode.getUserObject() instanceof Entity) {
                    // TOOD 
                }
            }
            
        });
    }
    
    
    /**
     * Show the JME scene graph for this node, find the 
     * @param node
     */
    private void showJMEGraph(Node node) {
        Node root = node;
        while(root.getParent()!=null) {
//            System.out.println("Finding root "+root);
            root = root.getParent();
        }
            
        jmeTree.setModel(new JmeTreeModel(root));
    }
    
    /**
     * Get the  cells from the cache and update the UI
     */
    private void refreshCells(WonderlandSession session) {
        CellCache cache = ClientContext.getCellCache(session);
        
        for(Cell rootCell : cache.getRootCells()) {
            rootCells.add(rootCell);
        }
        
        populateJTree();
    }
    
    private void populateJTree() {
        for(Cell rootCell : rootCells) {            
            treeRoot.add(createJTreeNode(rootCell));
        }
    }
    
    private DefaultMutableTreeNode createJTreeNode(Cell cell) {
        DefaultMutableTreeNode parentNode = nodes.get(cell.getParent());
        if (parentNode==null && !(cell instanceof RootCell)) {
            logger.severe("******* Null parent "+cell.getParent());
            return null;
        } 
        
        
        DefaultMutableTreeNode ret = new DefaultMutableTreeNode(cell);
        nodes.put(cell, ret);
        if (cell instanceof RootCell)
            parentNode = treeRoot;
        ((DefaultTreeModel)cellTree.getModel()).insertNodeInto(ret, parentNode, parentNode.getChildCount());

        CellRenderer cr = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        if (cr!=null && cr instanceof CellRendererJME) {
            CellRendererJME crj = (CellRendererJME)cr;
            Entity e = crj.getEntity();
            DefaultMutableTreeNode entityNode = new DefaultMutableTreeNode(e);
            ((DefaultTreeModel)cellTree.getModel()).insertNodeInto(entityNode, parentNode, parentNode.getChildCount());
            // TODO find children entity, but don't traverse into child cells entities
        }
        
        List<Cell> children = cell.getChildren();
        for(Cell child : children)
            ret.add(createJTreeNode(child));
        
        
        return ret;
    }
    
    private void populateCellPanelInfo(Cell cell) {
        if (cell==null) {
            cellClassNameTF.setText(null);
            cellNameTF.setText(null);
            DefaultListModel listModel = (DefaultListModel) cellComponentList.getModel();
            listModel.clear();
        } else {
            cellClassNameTF.setText(cell.getClass().getName());
            cellNameTF.setText(cell.getName());
            DefaultListModel listModel = (DefaultListModel) cellComponentList.getModel();
            listModel.clear();
            for(CellComponent c : cell.getComponents()) {
                listModel.addElement(c.getClass().getName());
            }
        }
    }
    
    private void setViewerActive(boolean active) {
        System.out.println("Viewer Active "+active);

        if (this.active = active)
            return;
        
        if (active) {
            ClientContext.getInputManager().addGlobalEventListener(cellViewerListener);            
        } else {
            ClientContext.getInputManager().removeGlobalEventListener(cellViewerListener);
        }
        
        this.active = active;
    }

    class WonderlandCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                                               Object value,
                                               boolean selected,
                                               boolean expanded,
                                               boolean leaf,
                                               int row,
                                               boolean hasFocus) {
            super.getTreeCellRendererComponent(
                        tree, value, selected,
                        expanded, leaf, row,
                        hasFocus);
            
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;

            if (treeNode.getUserObject() instanceof Cell) {
                Cell cell = (Cell) treeNode.getUserObject();
                String name = cell.getName();
                if (name==null)
                    name="";

                setText("C "+getTrimmedClassname(cell)+":"+name);
            } else if (treeNode.getUserObject() instanceof Entity) {
                Entity entity = (Entity)treeNode.getUserObject();
                String name = entity.getName();
                if (name==null)
                    name="";
                setText("E "+getTrimmedClassname(entity)+":"+name);
            }
            return this;
        }       

        /**
         * Return the classname of the object, trimming off the package name
         * @param o
         * @return
         */
        private String getTrimmedClassname(Object o) {
            String str = o.getClass().getName();

            return str.substring(str.lastIndexOf('.')+1);
        }
    }
    
    class CellViewerEventListener extends EventClassFocusListener {
        @Override
        public Class[] eventClassesToConsume () {
            return new Class[] { KeyEvent3D.class, MouseEvent3D.class };
        }

        @Override
        public void commitEvent (Event event) {
            System.out.println("evt " +event);
            
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

        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        cellTree = new javax.swing.JTree();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        cellInfoPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        cellComponentList = new javax.swing.JList();
        cellClassNameTF = new javax.swing.JTextField();
        cellNameTF = new javax.swing.JTextField();
        jmeGraphPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jmeTree = new javax.swing.JTree();
        entityGraphPanel = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setTitle("Cell Viewer");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowIconified(java.awt.event.WindowEvent evt) {
                formWindowIconified(evt);
            }
            public void windowDeiconified(java.awt.event.WindowEvent evt) {
                formWindowDeiconified(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(300);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jLabel4.setText("Cells");
        jPanel2.add(jLabel4, java.awt.BorderLayout.NORTH);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        cellTree.setAutoscrolls(true);
        cellTree.setDragEnabled(true);
        cellTree.setRootVisible(false);
        cellTree.setScrollsOnExpand(true);
        cellTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                cellTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(cellTree);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel3, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel2);

        jLabel1.setText("Cell Class :");

        jLabel2.setText("Cell Name :");

        jLabel3.setText("Cell Components :");

        cellComponentList.setModel(new DefaultListModel());
        jScrollPane2.setViewportView(cellComponentList);

        cellClassNameTF.setText("jTextField1");

        cellNameTF.setText("jTextField1");

        org.jdesktop.layout.GroupLayout cellInfoPanelLayout = new org.jdesktop.layout.GroupLayout(cellInfoPanel);
        cellInfoPanel.setLayout(cellInfoPanelLayout);
        cellInfoPanelLayout.setHorizontalGroup(
            cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(cellInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cellNameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                    .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(cellClassNameTF)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        cellInfoPanelLayout.setVerticalGroup(
            cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(cellInfoPanelLayout.createSequentialGroup()
                .add(23, 23, 23)
                .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(cellClassNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(cellNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cellInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 98, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addContainerGap(210, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Cell Info", cellInfoPanel);

        jmeGraphPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane3.setViewportView(jmeTree);

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
        );

        jmeGraphPanel.add(jPanel4, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("JME Graph", jmeGraphPanel);

        org.jdesktop.layout.GroupLayout entityGraphPanelLayout = new org.jdesktop.layout.GroupLayout(entityGraphPanel);
        entityGraphPanel.setLayout(entityGraphPanelLayout);
        entityGraphPanelLayout.setHorizontalGroup(
            entityGraphPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 425, Short.MAX_VALUE)
        );
        entityGraphPanelLayout.setVerticalGroup(
            entityGraphPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 403, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Entity Graph", entityGraphPanel);

        jSplitPane1.setRightComponent(jTabbedPane1);

        jPanel1.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void cellTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_cellTreeValueChanged
    // Tree selection
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                       cellTree.getLastSelectedPathComponent();

    if (node == null) {
        //Nothing is selected.	
        return;
    }

    Cell cell = (Cell) node.getUserObject();
    populateCellPanelInfo(cell);

}//GEN-LAST:event_cellTreeValueChanged

private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
    setViewerActive(false);
}//GEN-LAST:event_formWindowClosed

private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
    setViewerActive(true);
}//GEN-LAST:event_formWindowOpened

private void formWindowIconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowIconified
    setViewerActive(false);
}//GEN-LAST:event_formWindowIconified

private void formWindowDeiconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowDeiconified
    setViewerActive(true);
}//GEN-LAST:event_formWindowDeiconified

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField cellClassNameTF;
    private javax.swing.JList cellComponentList;
    private javax.swing.JPanel cellInfoPanel;
    private javax.swing.JTextField cellNameTF;
    private javax.swing.JTree cellTree;
    private javax.swing.JPanel entityGraphPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel jmeGraphPanel;
    private javax.swing.JTree jmeTree;
    // End of variables declaration//GEN-END:variables

}
