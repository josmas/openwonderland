/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.defaultenvironment.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 *
 * @author Jagwire
 */
@ServerState
@XmlRootElement(name="shared-skybox")
public class SharedSkybox extends SharedData {
    private static final long serialVersionUID = 1L;
    
    @XmlElement(name="id")
    private String id = "";
    
    @XmlElement(name="description")
    private String description = "";
    
    @XmlElement(name="north")
    private String north = "";
    
    @XmlElement(name="south")
    private String south = "";
    
    @XmlElement(name="east")
    private String east = "";
    
    @XmlElement(name="west")
    private String west = "";
    
    @XmlElement(name="up")
    private String up = "";
    
    @XmlElement(name="down")
    private String down = "";
    
    public SharedSkybox() {
        
    }
    
    public SharedSkybox(String id,
                        String description,
                        String north,
                        String south,
                        String east,
                        String west,
                        String up,
                        String down) {
        this.id = id;
        this.description = description;
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
        this.up = up;
        this.down = down;
    }

    @XmlTransient
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @XmlTransient
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    
    
    @XmlTransient
    public String getDown() {
        return down;
    }

    public void setDown(String down) {
        this.down = down;
    }
    
    @XmlTransient
    public String getEast() {
        return east;
    }

    public void setEast(String east) {
        this.east = east;
    }

    @XmlTransient
    public String getNorth() {
        return north;
    }

    public void setNorth(String north) {
        this.north = north;
    }

    @XmlTransient
    public String getSouth() {
        return south;
    }
    
    public void setSouth(String south) {
        this.south = south;
    }

    @XmlTransient
    public String getUp() {
        return up;
    }

    public void setUp(String up) {
        this.up = up;
    }

    @XmlTransient
    public String getWest() {
        return west;
    }

    public void setWest(String west) {
        this.west = west;
    }
    
    
    public static SharedSkybox valueOf(String id,
                                       String description,
                                       String north,
                                       String south,
                                       String east,
                                       String west,
                                       String up,
                                       String down) {
        
        return new SharedSkybox(id, description, north, south, east, west, up, down);
    }
    
    
    
    
}
