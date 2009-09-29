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
public class SharedString implements SharedData {
    private static final long serialVersionUID = 1L;
    private String value;

    /**
     * No arg constructor needed by jaxb.  Use valueOf() when constructing
     * this class directly.
     */
    public SharedString() {
    }

    /**
     * Use valueOf() instead of this
     * @param value the value
     */
    private SharedString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getValue();
    }

    public static SharedString valueOf(String value) {
        return new SharedString(value);
    }
}
