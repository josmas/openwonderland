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
package org.jdesktop.wonderland.client.jme.graphviewer.defaultnodeviewer;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.SceneElement;
import javax.swing.JPanel;

/**
 * Superclass for all the info panels
 * 
 * @author paulby
 */
public abstract class JmeInfoPanel extends JPanel {

    abstract void setElement(SceneElement element);

    public static String toStringVector3f(Vector3f v3f) {
        return v3f.x+", "+v3f.y+", "+v3f.z;
    }
    
    public static String toStringQuaternion(Quaternion quat) {
        return quat.x+", "+quat.y+", "+quat.z+", "+quat.w;
    }
}
