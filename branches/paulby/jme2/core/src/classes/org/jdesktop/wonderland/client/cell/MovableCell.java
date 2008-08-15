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
package org.jdesktop.wonderland.client.cell;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Teapot;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import org.jdesktop.wonderland.common.cell.CellID;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RotationProcessor;
import org.jdesktop.mtgame.SceneComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.PrivateAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * A cell that can move
 * 
 * TODO DELETE ME
 * 
 * @author paulby
 * @deprecated
 */
class MovableCell extends Cell {
//    private CellChannelConnection cellChannelConnection;
    
    private static Logger logger = Logger.getLogger(MovableCell.class.getName());
//    private ArrayList<CellMoveListener> serverMoveListeners = null;
    
    public MovableCell(CellID cellID) {
        super(cellID);
        addComponent(new ChannelComponent(this));
        addComponent(new MovableComponent(this));
    }

}
