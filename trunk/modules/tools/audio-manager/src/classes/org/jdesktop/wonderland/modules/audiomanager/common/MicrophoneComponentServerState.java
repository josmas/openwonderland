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
package org.jdesktop.wonderland.modules.audiomanager.common;

import com.jme.math.Vector3f;
import java.io.Serializable;
import java.util.ResourceBundle;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.utils.jaxb.Vector3fAdapter;

/**
 * The MicrophoneComponentServerState class is the cell that renders a microphone cell in
 * world.
 * 
 * @author jprovino
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
@XmlRootElement(name = "microphone-component")
@ServerState
public class MicrophoneComponentServerState extends CellComponentServerState {

    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/audiomanager/common/Bundle");
    @XmlElement(name = "name")
    private String name = BUNDLE.getString("Microphone");
    @XmlElement(name = "volume")
    private double volume = 1;
    @XmlElement(name = "fullVolumeArea")
    private FullVolumeArea fullVolumeArea = new FullVolumeArea();
    @XmlElement(name = "showBounds")
    private boolean showBounds = false;
    @XmlElement(name = "activeArea")
    private ActiveArea activeArea = new ActiveArea();
    @XmlElement(name = "showActiveArea")
    private boolean showActiveArea = false;

    public enum MicrophoneBoundsType {
	CELL_BOUNDS,
	BOX,
	SPHERE
    }

    /** Default constructor */
    public MicrophoneComponentServerState() {
    }

    public MicrophoneComponentServerState(String name, double volume, FullVolumeArea fullVolumeArea,
	    ActiveArea activeArea) {

        this.name = name;
	this.volume = volume;
	this.fullVolumeArea = fullVolumeArea;
	this.activeArea = activeArea;
    }

    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.audiomanager." + "server.MicrophoneComponentMO";
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public String getName() {
        return name;
    }

    public void setVolume(double volume) {
	this.volume = volume;
    }

    @XmlTransient
    public double getVolume() {
	return volume;
    }

    public void setFullVolumeArea(FullVolumeArea fullVolumeArea) {
	this.fullVolumeArea = fullVolumeArea;
    }

    @XmlTransient
    public FullVolumeArea getFullVolumeArea() {
	return fullVolumeArea;
    }

    public void setShowBounds(boolean showBounds) {
	this.showBounds = showBounds;
    }

    @XmlTransient
    public boolean getShowBounds() {
	return showBounds;
    }

    public void setActiveArea(ActiveArea activeArea) {
	this.activeArea = activeArea;
    }

    @XmlTransient
    public ActiveArea getActiveArea() {
	return activeArea;
    }

    public void setShowActiveArea(boolean showActiveArea) {
	this.showActiveArea = showActiveArea;
    }

    @XmlTransient
    public boolean getShowActiveArea() {
	return showActiveArea;
    }

    @XmlRootElement(name = "microphone-component-fullVolumeArea")
    @ServerState
    public static class FullVolumeArea implements Serializable {

	@XmlElement(name="boundsType") public MicrophoneBoundsType boundsType = 
	    MicrophoneBoundsType.CELL_BOUNDS;
	@XmlElement(name="bounds") 
        @XmlJavaTypeAdapter(Vector3fAdapter.class)
	public Vector3f bounds = new Vector3f();

        /** Default constructor */
        public FullVolumeArea() {
            this(MicrophoneBoundsType.CELL_BOUNDS, new Vector3f());
        }

        public FullVolumeArea(double fullVolumeRadius) {
            this(MicrophoneBoundsType.SPHERE, new Vector3f((float) fullVolumeRadius, 0f, 0f));
        }

        public FullVolumeArea(Vector3f bounds) {
            this(MicrophoneBoundsType.BOX, bounds);
        }

	private FullVolumeArea(MicrophoneBoundsType boundsType, Vector3f bounds) {
	    this.boundsType = boundsType;
	    this.bounds = bounds;
	}

	public String toString() {
	    return "MicrophoneBoundsType " + boundsType + " bounds " + bounds;
	}

    }

    @XmlRootElement(name = "microphone-component-activeArea")
    @ServerState
    public static class ActiveArea implements Serializable {

	@XmlElement(name="activeAreaOrigin")
        @XmlJavaTypeAdapter(Vector3fAdapter.class)
        public Vector3f activeAreaOrigin = new Vector3f();
	@XmlElement(name="activeAreaBoundsType") 
	public MicrophoneBoundsType activeAreaBoundsType = MicrophoneBoundsType.BOX;
	@XmlElement(name="activeAreaBounds") 
        @XmlJavaTypeAdapter(Vector3fAdapter.class)
	public Vector3f activeAreaBounds = new Vector3f();

        /** Default constructor */
        public ActiveArea() {
            this(new Vector3f());
        }

        public ActiveArea (Vector3f origin) {
	    this(origin, MicrophoneBoundsType.CELL_BOUNDS, new Vector3f());
	}

        public ActiveArea (Vector3f origin, double fullVolumeRadius) {
            this(origin, MicrophoneBoundsType.SPHERE, new Vector3f((float) fullVolumeRadius, 0f, 0f));
	}

        public ActiveArea(Vector3f origin, Vector3f bounds) {
            this(origin, MicrophoneBoundsType.BOX, bounds);
        }

	private ActiveArea (Vector3f activeAreaOrigin, MicrophoneBoundsType activeAreaBoundsType, 
		Vector3f activeAreaBounds) {

	    this.activeAreaOrigin = activeAreaOrigin;
	    this.activeAreaBoundsType = activeAreaBoundsType;
	    this.activeAreaBounds = activeAreaBounds;
	}

	public String toString() {
	    return "Origin " + activeAreaOrigin + " Bounds Type " + activeAreaBoundsType 
		+ " bounds " + activeAreaBounds;
        }

    } 

}
