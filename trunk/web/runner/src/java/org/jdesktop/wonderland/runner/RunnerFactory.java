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
package org.jdesktop.wonderland.runner;

import java.util.Properties;

/**
 * Create a new runner with the given type using reflection.
 * @author jkaplan
 */
public class RunnerFactory {
    /**
     * Create a new runner of the given type
     * @param className the runner type, a fully-qualified class name
     * @param props the properties to configure the runner with
     * @throws RunnerException if there is an error creating the runner
     */
    public static Runner create(String className, Properties props) 
            throws RunnerException 
    {
        try {
            Class<Runner> clazz = (Class<Runner>) Class.forName(className);
            Runner r = clazz.newInstance();
            r.configure(props);
            
            return r;
        } catch (ClassNotFoundException cnfe) {
            throw new RunnerCreationException(cnfe);
        } catch (InstantiationException ie) {
            throw new RunnerCreationException(ie);
        } catch (IllegalAccessException iae) {
            throw new RunnerCreationException(iae);
        }
    }
}
