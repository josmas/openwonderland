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

package org.jdesktop.wonderland.client.jme.affordances;

import com.jme.math.Vector3f;
import com.jme.scene.Line;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.ControlCube;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;

/**
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ResizeAffordance extends Affordance {
    private Node rootNode;
    
    private ResizeAffordance(Cell cell) {
        super("Resize", cell);
        
        rootNode = new Node();
        
        ControlCube cc = new ControlCube(new Vector3f(), 1, 1, 1);
        Vector3f[][] edges = cc.getEdges();
        Vector3f[] vertices = cc.getVertices();
                    
        RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(rootNode);
        this.addComponent(RenderComponent.class, rc);
        for (int i = 0; i < edges.length; i++) {
            Line l = new Line("Line " + i, edges[i], null, null, null);
            rootNode.attachChild(l);
        }

        for (int i = 0; i < vertices.length; i++) {
            Box b = new Box("Box " + i, vertices[i], 0.1f, 0.1f, 0.1f);
            b.setRandomColors();
            rootNode.attachChild(b);
        }
    }
    
    protected Node getRootNode() {
        return rootNode;
    }
    
    public static ResizeAffordance addToCell(Cell cell) {
        ResizeAffordance resizeEntity = new ResizeAffordance(cell);
        CellRendererJME r = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
        BasicRenderer.entityAddChild(r.getEntity(), resizeEntity);
        ClientContextJME.getWorldManager().addToUpdateList(resizeEntity.getRootNode());
        return resizeEntity;
    }
}
