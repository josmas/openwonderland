/**
 * Project Wonderland
 *
 * $RCSfile: EventProcessor.java,v $
 *
 * Copyright (c) 2004, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.16 $
 * $Date: 2007/05/23 18:19:30 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.client.event;

import java.util.logging.Logger;

/**
 * The base class for all LG events. This provides the basic information
 * required by all LG events.
 * @author  paulby
 * @version 
 */
public class WlEvent implements java.io.Serializable {
    protected static final Logger logger = Logger.getLogger("lg.event");
    
    /** the source of the event */
    private transient WlEventSource source;
    /** the clas of the source object */
    private Class sourceClass;
    
    
    /** Creates new LgEvent */
    public WlEvent() {
        source = null;
        sourceClass = null;
    }

    /**
     * Set the source object for this event
     * @param source the source object of this event
     */
    void setSource(WlEventSource source) {

        this.source = source;
        if (source == null) {
            sourceClass = null;
        } else {
            sourceClass = source.getClass();
        }
    }

    /**
     * Returns the source of the event. If you only need the class of the source
     * call getSourceClass
     * @return the source object for this event
     */
    public WlEventSource getSource() {
        return source;
    }
    
    /**
     * Get the class of the source object for this event
     * Getting the class from this method can be much
     * cheaper than calling getSource().getClass()
     * @return the class of the source object
     */
    public Class getSourceClass() {
       return sourceClass;
    }
    
}
