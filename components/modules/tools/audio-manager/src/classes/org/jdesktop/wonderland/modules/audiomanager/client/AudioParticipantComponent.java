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
package org.jdesktop.wonderland.modules.audiomanager.client;

import java.util.logging.Logger;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 * A component that provides audio participant control
 * 
 * @author jprovino
 */
@ExperimentalAPI
public class AudioParticipantComponent extends CellComponent implements VolumeChangeListener {
    
    private static Logger logger = Logger.getLogger(AudioParticipantComponent.class.getName());

    public AudioParticipantComponent(Cell cell) {
        super(cell);
    }
    
    @Override
    public void setStatus(CellStatus status) {
	switch(status) {
        case DISK:
            break;

	case BOUNDS:
	    break;

        }
    }
    
    public void volumeChanged(double volume) {
	System.out.println("Volume is " + volume);
    }

}
