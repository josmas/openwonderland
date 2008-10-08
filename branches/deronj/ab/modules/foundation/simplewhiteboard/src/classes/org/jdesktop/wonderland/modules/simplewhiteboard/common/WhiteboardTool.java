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
package org.jdesktop.wonderland.modules.simplewhiteboard.common;

import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Whiteboard tool types
 *
 * @author nsimpson
 */

@ExperimentalAPI
public interface WhiteboardTool {
    public enum Tool {
        STROKE,
// not implemented yet
//        LINE,
//        RECTANGLE,
//        ELLIPSE
    };
    
    public static final Tool STROKE = Tool.STROKE;
//    public static final Tool LINE = Tool.LINE;    
//    public static final Tool RECTANGLE = Tool.RECTANGLE;
//    public static final Tool ELLIPSE = Tool.ELLIPSE;
}
