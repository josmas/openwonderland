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
    @XmlElement(name = "listenArea")
    private ListenArea listenArea = new ListenArea();
    @XmlElement(name = "showBounds")
    private boolean showBounds = false;
    @XmlElement(name = "talkArea")
    private TalkArea talkArea = new TalkArea();
    @XmlElement(name = "showTalkArea")
    private boolean showTalkArea = false;

    public enum MicrophoneBoundsType {
	CELL_BOUNDS,
	BOX,
	SPHERE
    }

    /** Default constructor */
    public MicrophoneComponentServerState() {
    }

    public MicrophoneComponentServerState(String name, double volume, ListenArea listenArea,
	    TalkArea talkArea) {

        this.name = name;
	this.volume = volume;
	this.listenArea = listenArea;
	this.talkArea = talkArea;
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

    public void setListenArea(ListenArea listenArea) {
	this.listenArea = listenArea;
    }

    @XmlTransient
    public ListenArea getListenArea() {
	return listenArea;
    }

    public void setShowBounds(boolean showBounds) {
	this.showBounds = showBounds;
    }

    @XmlTransient
    public boolean getShowBounds() {
	return showBounds;
    }

    public void setTalkArea(TalkArea talkArea) {
	this.talkArea = talkArea;
    }

    @XmlTransient
    public TalkArea getTalkArea() {
	return talkArea;
    }

    public void setShowTalkArea(boolean showTalkArea) {
	this.showTalkArea = showTalkArea;
    }

    @XmlTransient
    public boolean getShowTalkArea() {
	return showTalkArea;
    }

    @XmlRootElement(name = "microphone-component-listenArea")
    @ServerState
    public static class ListenArea implements Serializable {

	@XmlElement(name="listenAreaOrigin")
        @XmlJavaTypeAdapter(Vector3fAdapter.class)
        public Vector3f listenAreaOrigin = new Vector3f();
	@XmlElement(name="boundsType") public MicrophoneBoundsType boundsType = 
	    MicrophoneBoundsType.CELL_BOUNDS;
	@XmlElement(name="bounds") 
        @XmlJavaTypeAdapter(Vector3fAdapter.class)
	public Vector3f bounds = new Vector3f();

        /** Default constructor */
        public ListenArea() {
	}

        public ListenArea(Vector3f origin) {
            this(origin, MicrophoneBoundsType.CELL_BOUNDS, origin);
        }

        public ListenArea(Vector3f origin, double listenRadius) {
            this(origin, MicrophoneBoundsType.SPHERE, new Vector3f((float) listenRadius, 0f, 0f));
        }

        public ListenArea(Vector3f origin, Vector3f bounds) {
            this(origin, MicrophoneBoundsType.BOX, bounds);
        }

	private ListenArea(Vector3f origin, MicrophoneBoundsType boundsType, Vector3f bounds) {
	    this.listenAreaOrigin = origin;
	    this.boundsType = boundsType;
	    this.bounds = bounds;
	}

	public String toString() {
	    return "Origin " + listenAreaOrigin + " MicrophoneBoundsType " + boundsType + " bounds " + bounds;
	}

    }

    @XmlRootElement(name = "microphone-component-talkArea")
    @ServerState
    public static class TalkArea implements Serializable {

	@XmlElement(name="talkAreaOrigin")
        @XmlJavaTypeAdapter(Vector3fAdapter.class)
        public Vector3f talkAreaOrigin = new Vector3f();
	@XmlElement(name="talkAreaBoundsType") 
	public MicrophoneBoundsType talkAreaBoundsType = MicrophoneBoundsType.BOX;
	@XmlElement(name="talkAreaBounds") 
        @XmlJavaTypeAdapter(Vector3fAdapter.class)
	public Vector3f talkAreaBounds = new Vector3f();

        /** Default constructor */
        public TalkArea() {
            this(new Vector3f());
        }

        public TalkArea (Vector3f origin) {
	    this(origin, MicrophoneBoundsType.CELL_BOUNDS, new Vector3f());
	}

        public TalkArea (Vector3f origin, double listenRadius) {
            this(origin, MicrophoneBoundsType.SPHERE, new Vector3f((float) listenRadius, 0f, 0f));
	}

        public TalkArea(Vector3f origin, Vector3f bounds) {
            this(origin, MicrophoneBoundsType.BOX, bounds);
        }

	private TalkArea (Vector3f talkAreaOrigin, MicrophoneBoundsType talkAreaBoundsType, 
		Vector3f talkAreaBounds) {

	    this.talkAreaOrigin = talkAreaOrigin;
	    this.talkAreaBoundsType = talkAreaBoundsType;
	    this.talkAreaBounds = talkAreaBounds;
	}

	public String toString() {
	    return "Origin " + talkAreaOrigin + " Bounds Type " + talkAreaBoundsType 
		+ " bounds " + talkAreaBounds;
        }

    } 

}
