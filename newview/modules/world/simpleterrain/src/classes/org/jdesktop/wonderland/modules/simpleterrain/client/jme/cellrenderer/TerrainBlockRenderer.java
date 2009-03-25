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
package org.jdesktop.wonderland.modules.simpleterrain.client.jme.cellrenderer;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jmex.terrain.TerrainBlock;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.cell.config.jme.MaterialJME;

/**
 * Render basic jme shapes
 *
 * @author paulby
 */
public class TerrainBlockRenderer extends BasicRenderer {

    public TerrainBlockRenderer(Cell cell) {
        super(cell);
    }
    
    @Override
    protected Node createSceneGraph(Entity entity) {
        Node ret = new Node();
        ret.setName(cell.getCellID().toString());
        int size = 20;
        Vector3f stepScale = new Vector3f(1f, 1f, 1f);
        float[] heightMap = new float[size*size];
        for(int i=0; i<heightMap.length; i++) {
            heightMap[i] = (float) Math.random();
        }
        Vector3f origin = new Vector3f();
        Geometry geom= new TerrainBlock("Test", size, stepScale, heightMap, origin);


        if (geom!=null) {

            geom.setModelBound(new BoundingBox());
            geom.updateModelBound();

            MaterialState matState = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_MATERIAL);
            MaterialJME matJME = new MaterialJME(ColorRGBA.blue, null, null, null, 0f);
            if (matJME!=null)
                matJME.apply(matState);
            geom.setRenderState(matState);
        }

        ret.attachChild(geom);

        // Set the transform
        applyTransform(ret, cell.getLocalTransform());
        
        return ret;
    }

}
