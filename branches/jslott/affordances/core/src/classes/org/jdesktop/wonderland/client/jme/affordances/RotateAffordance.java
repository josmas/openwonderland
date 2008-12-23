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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Tube;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class RotateAffordance extends Affordance {
    private Node rootNode;

    private static ZBufferState zbuf = null;

    
    static {
        zbuf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
    }
    
    private RotateAffordance(Cell cell) {
        super("Rotate", cell);
        
        rootNode = new Node();
        
        RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(rootNode);
        this.addComponent(RenderComponent.class, rc);
        
        /* Tube in the x-y plane, color red */
        Entity e1 = new Entity("Tube X-Y");
        Tube t1 = new Tube("Tube X-Y", 3f, 2.8f, 0.1f, 50, 50);
        Node n1 = new Node();
        t1.setSolidColor(ColorRGBA.red);
        MaterialState matState1 = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState1.setDiffuse(ColorRGBA.red);
        n1.setRenderState(matState1);
        n1.setRenderState(zbuf);
        Quaternion q = new Quaternion().fromAngleAxis(1.5707f, new Vector3f(1, 0, 0));
        n1.setLocalRotation(q);
        n1.attachChild(t1);
        RenderComponent rc1 = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(n1);
        e1.addComponent(RenderComponent.class, rc1);
        BasicRenderer.entityAddChild(this, e1);

        /* Tube in the x-z plane, color green */
        Entity e2 = new Entity("Tube X-Z");
        Tube t2 = new Tube("Tube X-Z", 3f, 2.8f, 0.1f, 50, 50);
        Node n2 = new Node();
        t2.setSolidColor(ColorRGBA.green);        
        MaterialState matState2 = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState2.setDiffuse(ColorRGBA.green);
        n2.setRenderState(matState2);
        n2.setRenderState(zbuf);
        n2.attachChild(t2);
        RenderComponent rc2 = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(n2);
        e2.addComponent(RenderComponent.class, rc2);
        BasicRenderer.entityAddChild(this, e2);
        
        /* Tube in the y-z plane, color blue */
        Entity e3 = new Entity("Tube Y-Z");
        Tube t3 = new Tube("Tube Y-Z", 3f, 2.8f, 0.1f, 50, 50);
        Node n3 = new Node();
        t2.setSolidColor(ColorRGBA.blue);
        MaterialState matState3 = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState3.setDiffuse(ColorRGBA.blue);
        n3.setRenderState(matState3);
        n3.setRenderState(zbuf);
        Quaternion q3 = new Quaternion().fromAngleAxis(1.5707f, new Vector3f(0, 0, 1));
        n3.setLocalRotation(q3);
        n3.attachChild(t3);
        RenderComponent rc3 = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(n3);
        e3.addComponent(RenderComponent.class, rc3);
        BasicRenderer.entityAddChild(this, e3);        
    }
    
    protected Node getRootNode() {
        return rootNode;
    }
    
    public static RotateAffordance addToCell(Cell cell) {
        RotateAffordance rotateAffordance = new RotateAffordance(cell);
        CellRendererJME r = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
        BasicRenderer.entityAddChild(r.getEntity(), rotateAffordance);
        ClientContextJME.getWorldManager().addToUpdateList(rotateAffordance.getRootNode());
        return rotateAffordance;
    }
}
