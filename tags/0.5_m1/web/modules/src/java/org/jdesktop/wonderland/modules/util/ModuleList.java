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

package org.jdesktop.wonderland.modules.util;

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
 * TBD
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="wl-module-list")
public class ModuleList {
    /* An array of cell children names */
    @XmlElements({
        @XmlElement(name="module")
    })
    private String[] modules = null;
    
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleInfos */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(ModuleList.class);
            ModuleList.unmarshaller = jc.createUnmarshaller();
            ModuleList.marshaller = jc.createMarshaller();
            ModuleList.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
        
    /** Default constructor */
    public ModuleList() {
    }
    
    /** Constructor, takes the names of the modules */
    public ModuleList(String[] modules) {
        this.modules = modules;
    }

    
    /**
     * Returns the array of module names.
     * 
     * @return An array of module names
     */
    @XmlTransient public String[] getModules() {
        return this.modules;
    }
    
    /**
     * Takes the input stream of the XML and instantiates an instance of
     * the WFSCellList class
     * <p>
     * @param is The input stream of the XML representation
     * @throw ClassCastException If the input file does not map to WFSCellList
     * @throw JAXBException Upon error reading the XML stream
     */
    public static ModuleList decode(InputStream is) throws JAXBException {
        ModuleList children = (ModuleList)ModuleList.unmarshaller.unmarshal(is);
        return children;
    }
    
    /**
     * Writes the ModuleInfo class to an output stream.
     * <p>
     * @param w The output write to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        ModuleList.marshaller.marshal(this, w);
    }
}
