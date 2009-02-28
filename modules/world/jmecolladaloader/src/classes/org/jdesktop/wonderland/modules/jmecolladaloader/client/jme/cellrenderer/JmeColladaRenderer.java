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
import java.net.MalformedURLException;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.jme.cellrenderer.*;
import com.jme.bounding.BoundingBox;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.util.export.SavableString;
import com.jme.util.geom.TangentBinormalGenerator;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.model.collada.ColladaImporter;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.shader.DiffuseMap;
import org.jdesktop.mtgame.shader.DiffuseNormalMap;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.modules.jmecolladaloader.client.cell.JmeColladaCell;

/**
 * A cell renderer that uses the JME Collada loader
 * 
 * @author paulby
 */
public class JmeColladaRenderer extends BasicRenderer {

    private Node model;

    public JmeColladaRenderer(Cell cell) {
        super(cell);
    }
    
    @Override
    protected Node createSceneGraph(Entity entity) {
        try {
            // We need to handle null model uri's better!
            Node ret = new Node();
            if (((JmeColladaCell)cell).getModelURI() != null) {
                ret = loadColladaAsset(cell.getCellID().toString(), getAssetURL(((JmeColladaCell) cell).getModelURI()));
            }
            else {
                model = new Node();
                ret.attachChild(model);
            }

            // Adjust model origin wrt to cell
            if (((JmeColladaCell)cell).getGeometryTranslation()!=null)
                model.setLocalTranslation(((JmeColladaCell)cell).getGeometryTranslation());
            if (((JmeColladaCell)cell).getGeometryRotation()!=null)
                model.setLocalRotation(((JmeColladaCell)cell).getGeometryRotation());
            if (((JmeColladaCell)cell).getGeometryScale()!=null)
                model.setLocalScale(((JmeColladaCell)cell).getGeometryScale());
            return ret;
        } catch (MalformedURLException ex) {
            Logger.getLogger(JmeColladaRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Loads a collada cell from the asset managergiven an asset URL
     *
     * @param name the name to put in the returned node
     */
    public Node loadColladaAsset(String name, URL url) {
        Node node = new Node();
        
        try {
            InputStream input;
            
            if (url.getFile().endsWith(".gz")) {
                input = new GZIPInputStream(url.openStream());
            } else {
                input = url.openStream();
            }

            ResourceLocatorTool.addResourceLocator(
                    ResourceLocatorTool.TYPE_TEXTURE,
                    new AssetResourceLocator(url));

            model = loadModel(input, name);
            
            node.attachChild(model);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading Collada file "+((JmeColladaCell)cell).getModelURI(), e);
        }
        
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

    public static Node loadModel(InputStream in, String name) {
        Node ret;
        ColladaImporter.load(in, name);
        ret = ColladaImporter.getModel();
        parseModel(0, ret, true);

        ColladaImporter.cleanUp();
        return ret;
    }

    static void parseModel(int level, Spatial model, boolean normalMap) {
        if (model instanceof Node) {
            Node n = (Node)model;
            for (int i=0; i<n.getQuantity(); i++) {
                parseModel(level+1, n.getChild(i), normalMap);
            }
        } else if (model instanceof Geometry) {
            Geometry geo = (Geometry)model;

            SavableString str = (SavableString)geo.getUserData("MTGameShaderFlag");
            if (geo instanceof TriMesh && str!=null && str.getValue() != null) {
                //System.out.println("Generating Tangents: " + geo);
                TangentBinormalGenerator.generate((TriMesh)geo);
                //System.out.println("Vertex Buffer: " + geo.getVertexBuffer());
                //System.out.println("Normal Buffer: " + geo.getNormalBuffer());
                //System.out.println("Color Buffer: " + geo.getColorBuffer());
                //System.out.println("TC 0 Buffer: " + geo.getTextureCoords(0));
                //System.out.println("TC 1 Buffer: " + geo.getTextureCoords(1));
                //System.out.println("Tangent Buffer: " + geo.getTangentBuffer());
                //System.out.println("Binormal Buffer: " + geo.getBinormalBuffer());
                assignShader(geo, str.getValue(), normalMap);
            }
        }
    }

    static void assignShader(Geometry geo, String shaderFlag, boolean normalMap) {
        if (shaderFlag.equals("MTGAMEDiffuseNormalMap")) {
            if (normalMap) {
                DiffuseNormalMap shader = new DiffuseNormalMap(ClientContextJME.getWorldManager());
                shader.applyToGeometry(geo);
            } else {
                DiffuseMap shader = new DiffuseMap(ClientContextJME.getWorldManager());
                shader.applyToGeometry(geo);
            }
//            System.out.println("Assigning Shader: " + shaderFlag);
        }
    }

}
