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
package org.jdesktop.wonderland.modules.audiomanager.server;

import java.util.logging.Logger;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;

import org.jdesktop.wonderland.common.cell.CellTransform;

import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;

import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.mpk20.voicelib.app.Player;

import com.jme.math.Vector3f;

/**
 *
 * @author jprovino
 */
public class AudioParticipantComponentMO extends CellComponentMO {

    private static final Logger logger =
            Logger.getLogger(AudioParticipantComponentMO.class.getName());

    private MyTransformChangeListener myTransformChangeListener;

    /**
     * Create a AudioParticipantComponent for the given cell. 
     * @param cell
     */
    public AudioParticipantComponentMO(CellMO cellMO) {
        super(cellMO);

	System.out.println("AudioParticipantComponentMO for " + cellMO.getName());
    }

    @Override
    public void setLive(boolean live) {
	if (live == false) {
	    if (myTransformChangeListener != null) {
	        cellRef.get().removeTransformChangeListener(myTransformChangeListener);
		myTransformChangeListener = null;
	    }
	    return;
	}

	myTransformChangeListener = new MyTransformChangeListener();

	cellRef.get().addTransformChangeListener(myTransformChangeListener);
    }

    protected String getClientClass() {
	return "org.jdesktop.wonderland.modules.audiomanager.client.AudioParticipantComponent";
    }

    /*
     * Let subclasses overrid this to be notified of the change.
     */
    protected void transformChanged(Vector3f location, double angle) {
	//System.out.println("Audio participant transformChanged() called");
    }

    static class MyTransformChangeListener implements TransformChangeListenerSrv {

        public void transformChanged(ManagedReference<CellMO> cellRef, 
	        final CellTransform localTransform, final CellTransform localToWorldTransform) {

	    String clientId = cellRef.get().getCellID().toString();

	    logger.fine("localTransform " + localTransform + " world " 
	        + localToWorldTransform);

	    float[] angles = new float[3];

	    localToWorldTransform.getRotation(null).toAngles(angles);

	    double angle = Math.toDegrees(angles[1]) % 360 + 90;

	    Vector3f location = localToWorldTransform.getTranslation(null);
	
	    Player player = AppContext.getManager(VoiceManager.class).getPlayer(clientId);

	    AudioParticipantComponentMO component = 
		cellRef.get().getComponent(AudioParticipantComponentMO.class);

	    if (component != null) {
		//System.out.println("Let subclasses know transform changed");
	        component.transformChanged(location, angle);   // let subclasses know
	    }

	    if (player == null) {
	        System.out.println("AudioParticipant:  got transformChanged, but can't find player for " + clientId);
		return;
	    }

	    //System.out.println(player + " x " + location.getX()
	    //	+ " y " + location.getY() + " z " + location.getZ()
	    //	+ " angle " + angle);

	    player.moved(location.getX(), location.getY(), location.getZ(), angle);
        }

    }

}
