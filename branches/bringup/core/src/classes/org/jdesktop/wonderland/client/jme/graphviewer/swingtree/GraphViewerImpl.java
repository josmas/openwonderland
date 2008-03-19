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
package org.jdesktop.wonderland.client.jme.graphviewer.swingtree;

import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import javax.swing.JPanel;
import org.jdesktop.wonderland.client.jme.graphviewer.GraphViewer;
import org.jdesktop.wonderland.client.jme.graphviewer.NodeViewer;

/**
 *
 * @author paulby
 */
public class GraphViewerImpl implements GraphViewer {

    private JmeTreeModel model;
    private TreeGraphPanel graphPanel=null;
    
    public GraphViewerImpl() {
        model = new JmeTreeModel();
    }
    
    public void addGraphRoot(Node root) {
        if (root==null)
            return;
        
        ((JmeTreeModel.RootNode)model.getRoot()).addChild(root);
    }
    
    public void removeGraphRoot(Node root) {
        if (root==null)
            return;
        ((JmeTreeModel.RootNode)model.getRoot()).removeChild(root);
    }

    public void childAdded(Node parent, SceneElement child) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public JPanel getGraphPanel() {
        if (graphPanel==null)
            graphPanel = new TreeGraphPanel(model);
        return graphPanel;
    }

    public void setNodeViewer(NodeViewer nodeViewer) {
        graphPanel.setNodeViewer(nodeViewer);
    }

}
