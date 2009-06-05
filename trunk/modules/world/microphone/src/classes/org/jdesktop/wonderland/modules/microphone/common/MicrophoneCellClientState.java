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
package org.jdesktop.wonderland.modules.microphone.common;

import java.io.Serializable;

import org.jdesktop.wonderland.common.cell.state.CellClientState;

import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellServerState.ActiveArea;
import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellServerState.FullVolumeArea;

/**
 * The MicrophoneCellSetup class is the cell that renders a microphone cell in
 * world.
 * 
 * @author jkaplan
 */
public class MicrophoneCellClientState extends CellClientState {

    private String name;

    private double volume;

    private FullVolumeArea fullVolumeArea;

    private ActiveArea activeArea;
    
    /** Default constructor */
    public MicrophoneCellClientState() {
    }
    
    public MicrophoneCellClientState(String name, double volume, FullVolumeArea fullVolumeArea,
	    ActiveArea activeArea) {

	this.name = name;
	this.volume = volume;
	this.fullVolumeArea = fullVolumeArea;
	this.activeArea = activeArea;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setVolume(double volume) {
	this.volume = volume;
    }

    public double getVolume() {
	return volume;
    }

    public void setFullVolumeArea(FullVolumeArea fullVolumeArea) {
	this.fullVolumeArea = fullVolumeArea;
    }

    public FullVolumeArea getFullVolumeArea() {
        return fullVolumeArea;
    }

    public void setActiveArea(ActiveArea activeArea) {
	this.activeArea = activeArea;
    }

    public ActiveArea getActiveArea() {
        return activeArea;
    }

}
