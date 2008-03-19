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
package org.jdesktop.wonderland.client.jme.graphviewer;

import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import javax.swing.JPanel;

/**
 * Interface for graph viewer implementations
 * 
 * @author paulby
 */
public interface GraphViewer {

    /**
     * Add a new graph root to the set of graphs being displayed
     * @param root
     */
    public void addGraphRoot(Node root);
    
    /**
     * Remove the graph root
     * @param root
     */
    public void removeGraphRoot(Node root);
    
    /**
     * Add the child graph to the parent
     * @param parent
     * @param child
     */
    public void childAdded(Node parent, SceneElement child);
    
    /**
     * Get the panel that contains the graph
     * @return
     */
    public JPanel getGraphPanel();
    
    /**
     * Set the node viewer. When nodes are selected in the graph the
     * node viewer will be notified to display the details.
     * @param nodeViewer
     */
    public void setNodeViewer(NodeViewer nodeViewer);
}
