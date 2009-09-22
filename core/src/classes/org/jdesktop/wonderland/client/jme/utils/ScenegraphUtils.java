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

package org.jdesktop.wonderland.client.jme.utils;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * Various utilties for the JME scene graph
 *
 * @author paulby
 */
public class ScenegraphUtils {


    /**
     * Traverse the scene graph from rootNode and return the first
     * node with the given name. Returns null if no node is found
     *
     * @param rootNode the root of the graph
     * @param name the name to search for
     * @return the named node, or null
     */
    public static Spatial findNamedNode(Node rootNode, String name) {
        FindNamedNodeListener listener = new FindNamedNodeListener(name);

        TreeScan.findNode(rootNode, listener);

        return listener.getResult();
    }

    /**
     * Scan the graph and add all named nodes (non null names only) to the nodeMap.
     * If there are duplicate names a logger warning will be printed and previous
     * name/node will be overwritten
     *
     * @param rootNode root of graph
     * @param nodeMap the HashMap to which nodes are added
     */
    public static void getNamedNodes(Node rootNode, final HashMap<String, Spatial> nodeMap) {
        TreeScan.findNode(rootNode, new ProcessNodeInterface() {

            public boolean processNode(Spatial node) {
                if (node.getName()!=null) {
                    Spatial old = nodeMap.put(node.getName(), node);
                    if (old!=null)
                        Logger.getLogger(ScenegraphUtils.class.getName()).warning("Duplicate node name in scene "+node.getName());
                }
                return true;
            }
        });
    }

    /**
     * Given the world coordinates of a child compute the childs localTransform 
     * asssuming it will become a child of the parent with the supplied world transform
     * 
     * @param parent
     * @param childWorldTransform
     * @return
     */
    public static CellTransform computeChildTransform(CellTransform parentWorld, CellTransform childWorldTransform) {

        CellTransform pInv = parentWorld.clone(null).invert();

        CellTransform newChildLocal = new CellTransform();
        newChildLocal.mul(pInv, childWorldTransform);

        return newChildLocal;
    }

    /**
     * Given a list of cell transforms, multiply them together and return the
     * 'world' transform of the leaf transform. This mimics the transform calculation in the scene
     * graph, where the first element of the array is the graph root and the last
     * element is the leaf.
     * @param graphLocal list of cell transforms
     * @return the 'world' transform of the leaf
     */
//    public static CellTransform computeGraph(ArrayList<CellTransform> graphLocal) {
//        CellTransform result = new CellTransform();
//        for(int i=0; i<graphLocal.size(); i++) {
//            result.mul(graphLocal.get(i));
//        }
//        return result;
//    }

    static class FindNamedNodeListener implements ProcessNodeInterface {

        private String nodeName;
        private Spatial result=null;

        public FindNamedNodeListener(String name) {
            nodeName = name;
        }

        public boolean processNode(Spatial node) {
            if (node.getName().equals(nodeName)) {
                result = node;
                return false;
            }
            return true;
        }

        public Spatial getResult() {
            return result;
        }
    }

}
