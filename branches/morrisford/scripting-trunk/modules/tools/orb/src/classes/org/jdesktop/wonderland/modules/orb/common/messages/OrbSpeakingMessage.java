/**
 * Project Looking Glass
 * 
 * $RCSfile: OrbSpeakingMessage.java,v $
 * 
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 * 
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 * 
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 * 
 * $Revision: 1.8 $
 * $Date: 2008/06/12 18:48:17 $
 * $State: Exp $ 
 */

package org.jdesktop.wonderland.modules.orb.common.messages;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/*
 *
 * @author: jprovino
 */
public class OrbSpeakingMessage extends CellMessage {
    
    private boolean isSpeaking;

    public OrbSpeakingMessage(CellID cellID, boolean isSpeaking) {
	super(cellID);

	this.isSpeaking = isSpeaking;
    }

    public boolean isSpeaking() {
	return isSpeaking;
    }

}
