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
import java.util.LinkedList;
import java.util.Set;

/**
 * The CellSet class is a list of canonical cell names. The implementation of
 * the list is via LinkedList, so the entries are ordered. This encapsulation
 * implements the Darkstar ManagedObject interface so that it can be managed
 * in its data amanger.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellSet extends LinkedList<String> implements ManagedObject, Serializable {
    
    /** Constructor takes a Set */
    public CellSet(Set<String> set) {
        super(set);
    }
}
