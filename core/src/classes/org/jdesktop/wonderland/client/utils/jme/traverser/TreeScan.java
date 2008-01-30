/*
 * $RCSfile: TreeScan.java,v $
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 * $Revision: 1.3 $
 * $Date: 2007/02/09 17:17:03 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.client.utils.jme.traverser;

import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import com.jme.scene.Spatial;
import com.jme.scene.SwitchNode;
import java.util.HashSet;

public class TreeScan extends Object {
    
    private static HashSet visitedSharedGroups = null;
    
    
    /** Traverse the SceneGraph starting at node treeRoot. Every time a node of
     * class nodeClass is found call processNode method in processor.
     * @param treeRoot The root of the SceneGraph to search
     * @param nodeClass The class of the node(s) to search for
     * @param processor The class containing the processNode method which will be
     * called every time the correct nodeClass is found in the Scene Graph.
     * @param onlyEnabledSwitchChildren when true only recurse into Switch
     * children which are enabled
     * @param sharedGroupsOnce when true only process SharedGroups once,
     * regardless how many Links refer to them
     * @throws CapabilityNotSetException If the node is live or compiled and the scene graph
     * contains groups without ALLOW_CHILDREN_READ capability
     */
    public static void findNode(SceneElement treeRoot,Class nodeClass,ProcessNodeInterface processor,boolean onlyEnabledSwitchChildren,boolean sharedGroupsOnce) throws javax.media.j3d.CapabilityNotSetException {
        
        Class[] nodeClasses = new Class[]{ nodeClass };
        
        findNode( treeRoot, nodeClasses, processor,
                onlyEnabledSwitchChildren,
                sharedGroupsOnce );
        
    }
    
    /**
     * Traverse the graph visiting all nodes that subclass SceneElement and all switch
     * and shared groups
     * @param treeRoot
     * @param processor
     */
    public static void findNode(SceneElement treeRoot, ProcessNodeInterface processor) {
        findNode(treeRoot, new Class[]{SceneElement.class}, processor, false, false);
    }
    
    /** Traverse the SceneGraph starting at node treeRoot. Every time a node of
     * class nodeClass is found call processNode method in processor.
     * @param treeRoot The root of the SceneGraph to search
     * @param nodeClasses The list of classes of the node(s) to search for
     * @param processor The class containing the processNode method which will be
     * called every time the correct nodeClass is found in the Scene Graph.
     * @param onlyEnabledSwitchChildren when true only recurse into Switch
     * children which are enabled
     * @param sharedGroupsOnce when true only process SharedGroups once,
     * regardless how many Links refer to them
     * @throws CapabilityNotSetException If the node is live or compiled and the scene graph
     * contains groups without ALLOW_CHILDREN_READ capability
     */
    public static void findNode( SceneElement treeRoot,
            Class[] nodeClasses,
            ProcessNodeInterface processor,
            boolean onlyEnabledSwitchChildren,
            boolean sharedGroupsOnce ) throws
            javax.media.j3d.CapabilityNotSetException {
        if (sharedGroupsOnce)
            if (visitedSharedGroups==null)
                visitedSharedGroups = new HashSet();
        
        actualFindNode( treeRoot, nodeClasses, processor,
                onlyEnabledSwitchChildren,
                sharedGroupsOnce );
        
        if (sharedGroupsOnce)
            visitedSharedGroups.clear();
    }
    
    /**
     * Conveniance method to return a Class given the full Class name
     * without throwing ClassNotFoundException
     *
     * If the class is not available an error message is displayed and a
     * runtime exception thrown
     */
    public static Class getClass( String str ) {
        try {
            return Class.forName( str );
        } catch( ClassNotFoundException e ) {
            e.printStackTrace();
            throw new RuntimeException( "BAD CLASS "+str );
        }
    }
    
    private static void actualFindNode( SceneElement treeRoot,
            Class[] nodeClasses,
            ProcessNodeInterface processor,
            boolean onlyEnabledSwitchChildren,
            boolean sharedGroupsOnce ) throws
            javax.media.j3d.CapabilityNotSetException {
        boolean doChildren = true;
        
        if (treeRoot == null)
            return;
        
        //System.out.print( treeRoot.getClass().getName()+"  ");
        //System.out.print( nodeClasses[0].getName()+"  ");
        //System.out.println( nodeClasses[0].isAssignableFrom( treeRoot.getClass() ));
        for(int i=0; i<nodeClasses.length; i++)
            if (nodeClasses[i].isAssignableFrom( treeRoot.getClass() )) {
                doChildren = processor.processNode( treeRoot );
                i = nodeClasses.length;
            }
        
        if (!doChildren)
            return;
        
        if (onlyEnabledSwitchChildren && treeRoot instanceof SwitchNode ) {
            throw new RuntimeException("Not Implemented");
//            int whichChild = ((Switch)treeRoot).getWhichChild();
//            
//            if (whichChild==Switch.CHILD_ALL) {
//                Enumeration e = ((Group)treeRoot).getAllChildren();
//                while( e.hasMoreElements() )
//                    actualFindNode( (Node)e.nextElement(), nodeClasses, processor,
//                            onlyEnabledSwitchChildren, sharedGroupsOnce  );
//            } else if (whichChild==Switch.CHILD_MASK) {
//                BitSet set = ((Switch)treeRoot).getChildMask();
//                for(int s=0; s<set.length(); s++) {
//                    if (set.get(s))
//                        actualFindNode( ((Switch)treeRoot).getChild(s), nodeClasses,
//                                processor, onlyEnabledSwitchChildren, sharedGroupsOnce );
//                }
//            } else if (whichChild==Switch.CHILD_NONE) {
//                // DO nothing
//            } else
//                actualFindNode( ((Switch)treeRoot).currentChild(), nodeClasses,
//                        processor, onlyEnabledSwitchChildren, sharedGroupsOnce );
        } else if (treeRoot instanceof Node) {
            for(Spatial sp : ((Node)treeRoot).getChildren()) {
                actualFindNode( sp, nodeClasses, processor,
                        onlyEnabledSwitchChildren, sharedGroupsOnce  );
            }
        }
    }

}
