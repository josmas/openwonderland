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

package org.jdesktop.wonderland.wfs.util;

import java.io.InputStream;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The WFSRoots class simply represents an array of WFS root names. It is used
 * to serialize this list across a network in XML form or out to disk.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="wfs-roots")
public class WFSRoots {
    /* An array of WFS root names */
    @XmlElements({
        @XmlElement(name="root")
    })
    private String[] roots = null;

    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleInfos */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(WFSRoots.class);
            WFSRoots.unmarshaller = jc.createUnmarshaller();
            WFSRoots.marshaller = jc.createMarshaller();
            WFSRoots.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
    
    /** Default constructor */
    public WFSRoots() {
    }
    
    /** Constructor, takes the names of the roots, which may be null */
    public WFSRoots(String[] roots) {
        this.roots = roots;
    }
    
    /**
     * Returns the array of WFS root names, null if there are none.
     * 
     * @return An array of WFS root names
     */
    @XmlTransient public String[] getRoots() {
        return this.roots;
    }
    
    /**
     * Takes the input stream of the XML and instantiates an instance of
     * the WFSCellChildren class
     * <p>
     * @param is The input stream of the XML representation
     * @throw ClassCastException If the input file does not map to WFSCellChildren
     * @throw JAXBException Upon error reading the XML stream
     */
    public static WFSRoots decode(InputStream is) throws JAXBException {
        return (WFSRoots)WFSRoots.unmarshaller.unmarshal(is);        
    }
    
    /**
     * Writes the ModuleInfo class to an output stream.
     * <p>
     * @param w The output write to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        WFSRoots.marshaller.marshal(this, w);
    }
}
