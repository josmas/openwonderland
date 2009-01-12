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
package org.jdesktop.wonderland.modules.jmecolladaloader.client.jme.cellrenderer;

import org.jdesktop.wonderland.client.jme.cellrenderer.*;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.image.Texture;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.SharedMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.state.LightState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.jme.ClientContextJME;

/**
 *
 * @deprecated 
 * @author paulby
 */
public class RoomTestRenderer extends BasicRenderer {
    
    public RoomTestRenderer(Cell cell) {
        super(cell);
    }
    
    protected Node createSceneGraph(Entity entity) {
        ColorRGBA color = new ColorRGBA();

        Vector3f translation = cell.getLocalTransform().getTranslation(null);
        
        color.r = 0.0f; color.g = 0.0f; color.b = 1.0f; color.a = 1.0f;
        return createRoom(cell.getCellID().toString(), translation.x, translation.y, translation.z, color);        
    }

    public Node createRoom(String name, float xoff, float yoff, float zoff, 
            ColorRGBA color) {
        
        float radius=16;
        
        BoundingVolume bv = cell.getLocalBounds();
        if (bv instanceof BoundingBox) {
            radius = ((BoundingBox)bv).xExtent;
        } else if (bv instanceof BoundingSphere) {
            radius = ((BoundingSphere)bv).getRadius();
        }
        
        // Copied from the JME tutorial
        Box forceFieldX = new Box("forceFieldX", new Vector3f(-radius, -3f, -0.1f), new Vector3f(radius, 3f, 0.1f));
        forceFieldX.setModelBound(new BoundingBox());
        forceFieldX.updateModelBound();
        //We are going to share these boxes as well
        SharedMesh forceFieldX1 = new SharedMesh("forceFieldX1",forceFieldX);
        forceFieldX1.setLocalTranslation(new Vector3f(radius,0,0));
        SharedMesh forceFieldX2 = new SharedMesh("forceFieldX2",forceFieldX);
        forceFieldX2.setLocalTranslation(new Vector3f(radius,0,radius*2));

        //The other box for the Z axis
        Box forceFieldZ = new Box("forceFieldZ", new Vector3f(-0.1f, -3f, -radius), new Vector3f(0.1f, 3f, radius));
        forceFieldZ.setModelBound(new BoundingBox());
        forceFieldZ.updateModelBound();
        //and again we will share it
        SharedMesh forceFieldZ1 = new SharedMesh("forceFieldZ1",forceFieldZ);
        forceFieldZ1.setLocalTranslation(new Vector3f(0,0,radius));
        SharedMesh forceFieldZ2 = new SharedMesh("forceFieldZ2",forceFieldZ);
        forceFieldZ2.setLocalTranslation(new Vector3f(radius*2,0,radius));

        //add all the force fields to a single node
        Node forceFieldNode = new Node(name);
        forceFieldNode.attachChild(forceFieldX1);
        forceFieldNode.attachChild(forceFieldX2);
        forceFieldNode.attachChild(forceFieldZ1);
        forceFieldNode.attachChild(forceFieldZ2);
        forceFieldNode.setLocalTranslation(xoff, zoff, zoff);

        //load a texture for the force field elements
        TextureState ts = (TextureState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_TEXTURE);
        
        Texture t = TextureManager.loadTexture(RoomTestRenderer.class.getClassLoader()
                          .getResource("org/jdesktop/wonderland/client/resources/jme/dirt.jpg"),
                          Texture.MinificationFilter.BilinearNoMipMaps, Texture.MagnificationFilter.Bilinear);

        t.setWrap(Texture.WrapMode.MirroredRepeat);
        t.setTranslation(new Vector3f());
        ts.setTexture(t);

        forceFieldNode.setRenderState(ts);     
        
//        AlphaState as1 = JmeClientMain.getWorldManager().createRendererState(RenderState.;
//        as1.setBlendEnabled(true);
//        as1.setSrcFunction(AlphaState.SB_SRC_ALPHA);
//        as1.setDstFunction(AlphaState.DB_ONE);
//        as1.setTestEnabled(true);
//        as1.setTestFunction(AlphaState.TF_GREATER);
//        as1.setEnabled(true);
//
//        forceFieldNode.setRenderState(as1); 
        
//        BlendState as = (BlendState) JmeClientMain.getWorldManager().createRendererState(RenderState.RS_BLEND);
//        as.setEnabled(true);
//        as.setBlendEnabled(true);
//        as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
//        as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
//        forceFieldNode.setRenderState(as);
        
        forceFieldNode.setName("Cell_"+cell.getCellID()+":"+cell.getName());

        forceFieldNode.setModelBound(new BoundingSphere());
        forceFieldNode.updateModelBound();

        return forceFieldNode;
    }
}
