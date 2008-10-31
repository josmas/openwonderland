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

package org.jdesktop.wonderland.common.modules;

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The ModuleWFS class represents a single WFS stored within a module.
 * <p>
 * This class is annotation with JAXB XML elements and supports encoding and
 * decoding to/from XML via the encode() and decode() methods, respectively.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="wl-module-wfs")
public class ModuleWFS implements Serializable {
   
    /* The unique name of the wfs */
    @XmlElement(name="name", required=true)
    private String name = null;

    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleInfos */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(ModuleWFS.class);
            ModuleWFS.unmarshaller = jc.createUnmarshaller();
            ModuleWFS.marshaller = jc.createMarshaller();
            ModuleWFS.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }

    /** Default constructor */
    public ModuleWFS() {}
    
    /** Constructor which takes relative art resource path name */
    public ModuleWFS(String name) {
        this.name = name;
    }
    
    /* Java Bean Setter/Getter methods */
    @XmlTransient public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
 
    /**
     * Takes the input reader of the XML file and instantiates an instance of
     * the ModuleInfo class
     * <p>
     * @param r The input reader of the version XML file
     * @throw ClassCastException If the input file does not map to ModuleInfo
     * @throw JAXBException Upon error reading the XML file
     */
    public static ModuleWFS decode(Reader r) throws JAXBException {
        return (ModuleWFS)ModuleWFS.unmarshaller.unmarshal(r);
    }
    
    /**
     * Writes the ModuleInfo class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        ModuleWFS.marshaller.marshal(this, w);
    }
}
