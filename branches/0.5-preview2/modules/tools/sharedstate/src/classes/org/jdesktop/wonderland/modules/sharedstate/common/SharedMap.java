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
package org.jdesktop.wonderland.modules.sharedstate.common;

import java.util.Map;

/**
 * A map of shared data.
 * @author jkaplan
 */
public interface SharedMap extends Map<String, SharedData> {
    /**
     * Get the name of this map. A map is unique withing a given
     * SharedStateComponent
     * @return the map's name
     */
    String getName();

    /**
     * Get shared data of the given type
     * @param key the key to get
     * @param type the type to get
     * @return the value associated with the given key, or null if no
     * value is associated with the given key
     */
    <T extends SharedData> T get(String key, Class<T> type);
}
