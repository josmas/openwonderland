
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
package org.jdesktop.wonderland.modules.jmecolladaloader.client.jme.cellrenderer;

import org.jdesktop.wonderland.client.jme.cellrenderer.*;
import com.jme.bounding.BoundingBox;
import com.jme.light.PointLight;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import com.jmex.model.collada.ColladaImporter;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.jmecolladaloader.client.cell.JmeColladaCell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.cell.CellTransform;

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
        
        return loadColladaAsset(cell.getCellID().toString(), buf);        
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
    
    /**
     * Loads a collada cell from the asset managergiven an asset URL
     */
    public Node loadColladaAsset(String name, ZBufferState buf) {        
        Node node = null;

        /* Fetch the basic info about the cell */
        CellTransform transform = cell.getLocalTransform();
        Vector3f translation = transform.getTranslation(null);
        Vector3f scaling = transform.getScaling(null);
        Quaternion rotation = transform.getRotation(null);
        
        try {
            URL url = new URL(((JmeColladaCell)cell).getModelURI());
            InputStream input = url.openStream();
            System.out.println("Resource stream "+input);
            ColladaImporter.load(input, "Test");
            node = ColladaImporter.getModel();
            ColladaImporter.cleanUp();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading Collada file", e);
            node = new Node();
        }
        
        /* Create the scene graph object and set its wireframe state */
        node.setModelBound(new BoundingBox());
        node.updateModelBound();
        node.setLocalTranslation(translation);
        node.setLocalScale(scaling);
        node.setLocalRotation(rotation);
        node.setRenderState(buf);
        node.setName(name);

        return node;
    }
}
