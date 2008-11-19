/**
 * Project Looking Glass
 * 
 * $RCSfile: ConeOfSilenceEnterCellMessage.java,v $
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
 * $Revision: 1.3 $
 * $Date: 2008/01/08 20:42:20 $
 * $State: Exp $ 
 */
package org.jdesktop.wonderland.modules.coneofsilence.common.messages;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author jprovino
 */
public class ConeOfSilenceEnterCellMessage extends CellMessage {

    private CellID avatarCellID;
    private boolean entered;

    public ConeOfSilenceEnterCellMessage(CellID coneCellID, CellID avatarCellID, boolean entered) {
	super(coneCellID);

	this.avatarCellID = avatarCellID;
	this.entered = entered;
    }
    
    public CellID getAvatarCellID() {
	return avatarCellID;
    }

    public boolean getEntered() {
	return entered;
    }

    public String toString() {
        return "ConeOfSilenceEnterCellMessage for CellID "
	    + getCellID() + " entered=" + entered;
    }

}
