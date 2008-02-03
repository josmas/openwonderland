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
package org.jdesktop.wonderland.server.cell;

/**
 * Cells that implement this interface are notified of
 * cache usage and in turn notify the cache of any 
 * changes to the cell.
 * 
 * @author paulby
 */
public interface CacheHelperInterface {

    /**
     * Notify cell it is being used by a cache
     * @param cache
     */
    public void addCache(CacheHelperListener cache);
    
    /**
     * Notify the cell it is no longer being used by the specified cache
     * @param cache
     */
    public void removeCache(CacheHelperListener cache);
}
