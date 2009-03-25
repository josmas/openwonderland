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
package org.jdesktop.wonderland.modules.microphone.client.cell;

import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;

import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.modules.jmecolladaloader.client.jme.cellrenderer.JmeColladaRenderer;

/**
 * @author jkaplan
 */
public class MicrophoneCellRenderer extends JmeColladaRenderer {
    
    public MicrophoneCellRenderer(Cell cell) {
        super(cell);
    }

    @Override
    protected Node createSceneGraph(Entity entity) {

	System.out.println("MICROPHONE:  createSceneGraph");

        try {
            Node ret = loadColladaAsset(cell.getCellID().toString(), getAssetURL("wla://microphone/models/Microphone.dae"));
            //Matrix3f rot = new Matrix3f();
            //rot.fromAngleAxis((float) -Math.PI/2, new Vector3f(1,0,0));
            //ret.setLocalRotation(rot);
            return ret;
        } catch (MalformedURLException ex) {
            Logger.getLogger(MicrophoneCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
