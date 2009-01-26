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

import com.jme.scene.Spatial;
import org.jdesktop.wonderland.client.jme.cellrenderer.*;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.model.collada.ColladaImporter;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.modules.jmecolladaloader.client.cell.JmeColladaCell;
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
        return loadColladaAsset(cell.getCellID().toString());        
    }

    /**
     * Load a collada model from a local file, used to import art during
     * world building
     *
     * @deprecated this is just a test method, TODO remove
     */
//    public Node loadCollada(String name, float xoff, float yoff, float zoff, LightState ls) {
//        MaterialState matState = null;
//
//        Node ret;
//
//        try {
////            InputStream input = this.getClass().getClassLoader().getResourceAsStream("org/jdesktop/wonderland/client/resources/jme/duck_triangulate.dae");
//            InputStream input = this.getClass().getClassLoader().getResourceAsStream("org/jdesktop/wonderland/client/resources/jme/sphere2.dae");
////            System.out.println("Resource stream "+input);
//            ColladaImporter.load(input, "Test");
//            ret = ColladaImporter.getModel();
//            ColladaImporter.cleanUp();
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, "Error loading Collada file", e);
//            ret = new Node();
//        }
//
//
//        ret.setModelBound(new BoundingBox());
//        ret.updateModelBound();
////        System.out.println("Triangles "+ret.getTriangleCount());
//
//        ret.setLocalTranslation(xoff, yoff, zoff);
//
//        ret.setName("Cell_"+cell.getCellID()+":"+cell.getName());
//
//        ret.setModelBound(new BoundingSphere());
//        ret.updateModelBound();
//
//        return ret;
//    }
    
    /**
     * Loads a collada cell from the asset managergiven an asset URL
     *
     * @param name the name to put in the returned node
     */
    public Node loadColladaAsset(String name) {        
        Node node = new Node();
        Node model=null;
        
        try {
            URL url = getAssetURL(((JmeColladaCell)cell).getModelURI());
            logger.warning("URL: " + url);
            InputStream input = url.openStream();
//            System.out.println("Resource stream "+input);

            ResourceLocatorTool.addResourceLocator(
                    ResourceLocatorTool.TYPE_TEXTURE,
                    new AssetResourceLocator(url));

            ColladaImporter.load(input, "Test");
            model = ColladaImporter.getModel();
            ColladaImporter.cleanUp();
            
            // Adjust model origin wrt to cell
            if (((JmeColladaCell)cell).getGeometryTranslation()!=null)
                model.setLocalTranslation(((JmeColladaCell)cell).getGeometryTranslation());
            if (((JmeColladaCell)cell).getGeometryRotation()!=null)
                model.setLocalRotation(((JmeColladaCell)cell).getGeometryRotation());
            node.attachChild(model);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading Collada file "+((JmeColladaCell)cell).getModelURI(), e);
        }
        
        
        node.setName(name);

        // Make sure all the geometry has model bounds
        TreeScan.findNode(node, Geometry.class, new ProcessNodeInterface() {

            public boolean processNode(Spatial node) {
                Geometry g = (Geometry)node;
                if (g.getModelBound()==null) {
                    g.setModelBound(new BoundingBox());
                    g.updateModelBound();
                }

                return true;
            }

        }, false, true);

        return node;
    }
}
