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
package org.jdesktop.wonderland.wfs.loader;

import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * The CellMap class is a mapping between the canonical name of the cell
 * and some piece of information associated with that cell (e.g. the last
 * modification date or the cell object reference).
 * <p>
 * The implements of the list is via LinkedHashMap, so the entries are ordered.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellMap<T> extends LinkedHashMap<String, T> implements ManagedObject, Serializable {
    
    /** Default constructor */
    public CellMap() {
    }
}
