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

/**
 * Shared data representation of an integer
 * @author jkaplan
 */
public class SharedBoolean implements SharedData {
    private static final long serialVersionUID = 1L;

    public static final SharedBoolean TRUE = SharedBoolean.valueOf(true);
    public static final SharedBoolean FALSE = SharedBoolean.valueOf(false);

    private boolean value;

    /**
     * No arg constructor needed by jaxb.  Use valueOf() when constructing
     * this class directly.
     */
    public SharedBoolean() {
    }

    /**
     * Use valueOf() instead of this
     * @param value the value
     */
    private SharedBoolean(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }

    public static SharedBoolean valueOf(boolean value) {
        return new SharedBoolean(value);
    }

    public static SharedBoolean valueOf(String value) {
        return valueOf(Boolean.parseBoolean(value));
    }
}
