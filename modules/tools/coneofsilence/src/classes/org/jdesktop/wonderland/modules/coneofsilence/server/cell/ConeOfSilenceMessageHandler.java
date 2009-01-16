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
package org.jdesktop.wonderland.modules.coneofsilence.server.cell;

import com.sun.sgs.app.ManagedReference;

import com.sun.sgs.app.AppContext;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableMessage;

import org.jdesktop.wonderland.server.WonderlandContext;

import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;

import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

import java.io.Serializable;

import java.util.logging.Logger;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.common.cell.MultipleParentException;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

import org.jdesktop.wonderland.server.UserManager;

import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellMOFactory;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;

import org.jdesktop.wonderland.server.comms.WonderlandClientID;

import org.jdesktop.wonderland.modules.coneofsilence.common.messages.ConeOfSilenceEnterCellMessage;

import org.jdesktop.wonderland.modules.coneofsilence.server.cell.ConeOfSilenceCellMO;

import com.jme.bounding.BoundingVolume;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;

/**
 * A server cell that provides conference coneofsilence functionality
 * @author jprovino
 */
public class ConeOfSilenceMessageHandler implements Serializable, ComponentMessageReceiver {

    private static final Logger logger =
        Logger.getLogger(ConeOfSilenceMessageHandler.class.getName());
     
    private ManagedReference<ConeOfSilenceCellMO> coneOfSilenceCellMORef;

    private ManagedReference<ChannelComponentMO> channelComponentRef = null;

    private String name;

    private MyProximityListener proximityListener;

    public ConeOfSilenceMessageHandler(ConeOfSilenceCellMO coneOfSilenceCellMO, String name) {
	this.name = name;

	coneOfSilenceCellMO.addComponent(new MovableComponentMO(coneOfSilenceCellMO));

	coneOfSilenceCellMORef = AppContext.getDataManager().createReference(
	        (ConeOfSilenceCellMO) CellManagerMO.getCell(coneOfSilenceCellMO.getCellID()));

        ChannelComponentMO channelComponentMO = (ChannelComponentMO) 
	    coneOfSilenceCellMO.getComponent(ChannelComponentMO.class);

        if (channelComponentMO == null) {
            throw new IllegalStateException("Cell does not have a ChannelComponent");
	}

        channelComponentMO.addMessageReceiver(ConeOfSilenceEnterCellMessage.class, this);
        channelComponentMO.addMessageReceiver(MovableMessage.class, this);

        channelComponentRef = AppContext.getDataManager().createReference(channelComponentMO);

        ProximityComponentMO prox = new ProximityComponentMO(coneOfSilenceCellMO);
        BoundingVolume[] bounds = new BoundingVolume[1];

	bounds[0] = coneOfSilenceCellMO.getLocalBounds();

        proximityListener = new MyProximityListener(name);

        prox.addProximityListener(proximityListener, bounds);
        coneOfSilenceCellMO.addComponent(prox);
    }

    public void done() {
	channelComponentRef.get().removeMessageReceiver(ConeOfSilenceEnterCellMessage.class);
    }

    public void messageReceived(final WonderlandClientSender sender, 
	    final WonderlandClientID clientID, final CellMessage message) {

	if (message instanceof ConeOfSilenceEnterCellMessage) {
	    ConeOfSilenceEnterCellMessage msg = (ConeOfSilenceEnterCellMessage) message;

	    if (true) {
		System.out.println("Ignoring cone message().  Entered =" + msg.getEntered());
		return;
	    }

	    logger.fine("Got message " + msg);

	    if (msg.getEntered()) {
	        proximityListener.cellEntered(msg.getSoftphoneCallID());
	    } else {
	        proximityListener.cellExited(msg.getSoftphoneCallID());
	    }
	} else if (message instanceof MovableMessage) {
	    MovableMessage movableMessage = (MovableMessage) message;

	    Vector3f translation = movableMessage.getTranslation();

	    Quaternion rotation = movableMessage.getRotation();

	    System.out.println("Got movable message:  action "
		+ movableMessage.getActionType() 
		+ " translation " + translation + " rotation " + rotation);
	} else {
	    logger.warning("Unknown message " + message);
	}
    }

}
