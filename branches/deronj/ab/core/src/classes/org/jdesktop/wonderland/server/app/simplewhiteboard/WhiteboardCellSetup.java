/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package org.jdesktop.wonderland.server.app.simplewhiteboard;

import com.jme.math.Vector2f;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;

/**
 * The WFS setup class for WhiteboardCellMO.
 * 
 * @author deronj
 */
@XmlRootElement(name="simplewhiteboard-cell")
public class WhiteboardCellSetup extends BasicCellSetup implements Serializable {
    
    /** The user's preferred width of the whiteboard window. */
    @XmlElement(name="preferredWidth")
    public int preferredWidth = 1024;
    
    /** The user's preferred height of the whiteboard window. */
    @XmlElement(name="preferredHeight")
    public int preferredHeight = 768;
    
    /** The pixel scale of the whiteboard window. */
    @XmlElement(name="pixelScale")
	public Vector2f pixelScale = new Vector2f(0.01f, 0.01f);
    
    /** Default constructor */
    public WhiteboardCellSetup() {}
    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.server.app.simplewhiteboard.WhiteboardCellMO";
    }

    @XmlTransient public int getPreferredWidth () {
        return preferredWidth;
    }
    
    public void setPreferredWidth (int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }
    
    @XmlTransient public int getPreferredHeight () {
        return preferredHeight;
    }
    
    public void setPreferredHeight (int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }
    
    @XmlTransient public Vector2f getPixelScale () {
        return pixelScale;
    }
    
    public void setPixelScale (Vector2f pixelScale) {
        this.pixelScale = pixelScale;
    }
    
    /**
     * Returns a string representation of this class.
     *
     * @return The setup information as a string.
     */
    @Override
    public String toString() {
        return super.toString() + " [WhiteboardCellSetup]: " +
	    "preferredWidth=" + preferredWidth + "," +
	    "preferredHeight=" + preferredHeight + "," +
	    "pixelScale=" + pixelScale;
    }
}
