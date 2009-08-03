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

import org.jdesktop.wonderland.client.jme.graphviewer.defaultnodeviewer.NodeViewerImpl;
import org.jdesktop.wonderland.client.jme.graphviewer.swingtree.GraphViewerImpl;

/**
 *
 * @author paulby
 */
public class GraphViewerFactory {

    private static GraphViewer graphViewer=null;
    private static NodeViewer nodeViewer = null;
    
    public static GraphViewer getGraphViewer() {
        if (graphViewer==null) {
            graphViewer = new GraphViewerImpl();
        }
        return graphViewer;
    }
    
    public static NodeViewer getNodeViewer() {
        if (nodeViewer==null) {
            nodeViewer = new NodeViewerImpl();
            getGraphViewer().setNodeViewer(nodeViewer);
        }
        return nodeViewer;
    }
}