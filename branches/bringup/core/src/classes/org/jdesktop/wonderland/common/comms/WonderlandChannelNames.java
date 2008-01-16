/**
 * Project Wonderland
 *
 * $RCSfile:$
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
 * $Revision:$
 * $Date:$
 * $State:$
 */
package org.jdesktop.wonderland.common.comms;

import org.jdesktop.wonderland.InternalAPI;

/**
 * Well known channel prefixes. By convention, Wonderland channels are
 * named with well-known prefixes that indicate which client factory
 * will create a listener for the given channel.  This class contains
 * common prefixes used by the built-in Wonderland factories to
 * create listeners.
 * @author kaplanj
 */
@InternalAPI
public class WonderlandChannelNames {
    /** the prefix used by Wonderland internal channels */
    public static final String WONDERLAND_PREFIX = "Wonderland";

    /** 
     * The prefix used by cell channels.  Cell channels are identified
     * with this prefix followed by the cell id, for example the main
     * cell channel for cell 4 is named "Cell.4.Cell_Channel".
     */
    public static final String CELL_PREFIX = "Cell";
    
    /**
     * Prefix for avatar cell cache channel
     */
    public static final String AVATAR_CACHE_PREFIX = "AvatarCache";
}
