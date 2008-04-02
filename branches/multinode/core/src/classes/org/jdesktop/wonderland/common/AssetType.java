/**
 * Project Wonderland
 *
 * $RCSfile: AssetType.java,v $
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
 * $Date: 2007/05/04 23:11:34 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.common;

import org.jdesktop.wonderland.ExperimentalAPI;

/**
 *
 * Asset types
 *
 * @author paulby
 */
@ExperimentalAPI
public enum AssetType {
    
    // IMPORTANT names must be <=10 characters
    
    IMAGE,
    MODEL,
    FILE,
    OTHER           // For user defined assets
    
}
