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
package org.jdesktop.wonderland.client.jme.cellrenderer;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import com.jmex.model.collada.ColladaImporter;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.jme.ClientContextJME;

/**
 * A cell renderer that uses the JME Collada loader
 * 
 * @author paulby
 */
public class JmeColladaRenderer extends BasicRenderer {
    
    public JmeColladaRenderer(Cell cell) {
        super(cell);
    }
    
    protected Node createSceneGraph(Entity entity) {
        ColorRGBA color = new ColorRGBA();

        ZBufferState buf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setLocation(new Vector3f(100, 100, 100));
        light.setEnabled(true);
        LightState lightState = (LightState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_LIGHT);
        lightState.setEnabled(true);
        lightState.attach(light);

        Vector3f translation = cell.getLocalTransform().getTranslation(null);
        
        return loadCollada(cell.getCellID().toString(), translation.x, translation.y, translation.z, buf, lightState);        
    }

    public Node loadCollada(String name, float xoff, float yoff, float zoff, 
            ZBufferState buf, LightState ls) {
        MaterialState matState = null;
        
        Node ret;

        try {
//            InputStream input = this.getClass().getClassLoader().getResourceAsStream("org/jdesktop/wonderland/client/resources/jme/duck_triangulate.dae");
            InputStream input = this.getClass().getClassLoader().getResourceAsStream("org/jdesktop/wonderland/client/resources/jme/sphere2.dae");
            System.out.println("Resource stream "+input);
            ColladaImporter.load(input, "Test");
            ret = ColladaImporter.getModel();
            ColladaImporter.cleanUp();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading Collada file", e);
            ret = new Node();
        }
        
        
        ret.setModelBound(new BoundingBox());
        ret.updateModelBound();
        System.out.println("Triangles "+ret.getTriangleCount());

        matState = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_MATERIAL);
//        node.setRenderState(matState);
        ret.setRenderState(buf);
//        node.setRenderState(ls);
        ret.setLocalTranslation(xoff, yoff, zoff);
        
        ret.setName("Cell_"+cell.getCellID()+":"+cell.getName());

        return ret;
    }
    
}
