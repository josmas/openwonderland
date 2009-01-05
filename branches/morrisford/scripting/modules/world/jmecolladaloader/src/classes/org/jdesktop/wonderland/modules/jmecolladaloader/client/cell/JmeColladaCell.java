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
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.jmecolladaloader.client.cell;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.EntityComponent;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.modules.jmecolladaloader.client.jme.cellrenderer.JmeColladaRenderer;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.config.JmeColladaCellConfig;

/**
 * Client side cell for rendering JME content
 * 
 * @author paulby
 */
public class JmeColladaCell extends Cell {
    
    /* The URI of the model asset */
    private String modelURI = null;
    private Vector3f geometryTranslation;
    private Quaternion geometryRotation;
    private String scriptURL;
    private String scriptClump;
    private String scriptExt;
    private String scriptType;
    private ScriptingComponent scriptComp;
    private Node node = null;
    private CellRenderer renderer = null;
    private CellID cellID;
    
    public JmeColladaCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        this.cellID = cellID;
    }
    
    // Make this entity pickable by adding a collision component to it
    protected static void makeEntityPickable (Entity entity, Node node) 
        {
        JMECollisionSystem collisionSystem = (JMECollisionSystem)ClientContextJME.getWorldManager().getCollisionManager().
            loadCollisionSystem(JMECollisionSystem.class);

        CollisionComponent cc = collisionSystem.createCollisionComponent(node);
        entity.addComponent(CollisionComponent.class, cc);
        }
    
    /**
     * Called when the cell is initially created and any time there is a 
     * major configuration change. The cell will already be attached to it's parent
     * before the initial call of this method
     * 
     * @param config the cell config object
     */
    @Override
    public void configure(CellConfig config) {
        super.configure(config);
        JmeColladaCellConfig colladaConfig = (org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.config.JmeColladaCellConfig)config;
        this.modelURI = colladaConfig.getModelURI();
        this.geometryRotation = colladaConfig.getGeometryRotation();
        this.geometryTranslation = colladaConfig.getGeometryTranslation();
        this.scriptURL = colladaConfig.getScriptURL();
        this.scriptExt = colladaConfig.getScriptExt();
        this.scriptClump = colladaConfig.getScriptClump();
        this.scriptType = colladaConfig.getScriptType();
        addComponent(new ScriptingComponent(this));
        scriptComp = getComponent(ScriptingComponent.class);

        logger.warning("[CELL] COLLADA CELL " + this.modelURI);
    }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) 
        {
//        CellRenderer ret = null;
        switch(rendererType) 
            {
            case RENDERER_2D :
                // No 2D Renderer yet
                break;
            case RENDERER_JME :
                node = new Node();
                this.renderer = new JmeColladaRenderer(this, node);
                break;                
            }
        
        MouseEventListener myListener = new MouseEventListener();
        myListener.addToEntity(((CellRendererJME)this.renderer).getEntity());
        System.out.println("In createCellRenderer in JmeColladaCell - cell = " + cellID.toString() + 
                " Clump = " + scriptClump + "   entity = " + ((CellRendererJME)this.renderer).getEntity());

        Entity entity = ((CellRendererJME)this.renderer).getEntity();
        System.err.println("Entity = " + entity);

        EntityComponent cc = entity.getComponent(CollisionComponent.class);
        System.err.println("cc = " + cc);

        if (cc == null) 
            {
            makeEntityPickable(entity, node);
            }
        return this.renderer;
    }
    
    /**
     * Returns the URI of the model asset.
     * 
     * TODO shouldn't this be a URL instead of a String ?
     * 
     * @return The asset URI
     */
    public String getModelURI() {
        return this.modelURI;
    }

    public Vector3f getGeometryTranslation() {
        return geometryTranslation;
    }

    public Quaternion getGeometryRotation() {
        return geometryRotation;
    }
    
    public String getScriptURL()
    {
        return scriptURL;
    }
    
    public String getScriptClump()
    {
        return scriptClump;
    }
    
    public String getScriptExt()
    {
        return scriptExt;
    }
    
    public String getScriptType()
    {
        return scriptType;
    }
    
    class MouseEventListener extends EventClassListener 
        {
        @Override
        public Class[] eventClassesToConsume() 
            {
            return new Class[]{MouseButtonEvent3D.class};
            }

        @Override
        public void computeEvent(Event event)
        {
            System.out.println("In computeEvent in JmeColladaCell");
        }
        // Note: we don't override computeEvent because we don't do any computation in this listener.

        @Override
        public void commitEvent(Event event) 
            {
            System.out.println("In commitEvent in JmeColladaCell");
            MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
            if (mbe.isClicked() == false) 
                {
                return;
                }
//            renderer.updateShape();
            System.out.println("Before executeScript - this = " + this);
            scriptComp.executeScript("mouse", node, scriptClump, scriptExt, scriptType, scriptURL);
           }
        }

}

