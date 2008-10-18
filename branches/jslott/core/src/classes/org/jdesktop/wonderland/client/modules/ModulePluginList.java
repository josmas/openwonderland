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

package org.jdesktop.wonderland.client.modules;

import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
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
 * A list of module plugins, given by URIs. These URIs are of the format:
 * <p>
 * wlj://<module name>/<jar path>
 * <p>
 * where <module name> is the name of the module, and <jar path> is the path
 * of the jar within the module, e.g. "server/myplugin-server.jar".
 * <p>
 * This class deserializes information distributed from the module service.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="module-plugin-list")
public class ModulePluginList implements Serializable {
    
    /* An array of module plugin JAR URIs */
    @XmlElements({
        @XmlElement(name="jar-uri")
    })
    private String[] jarURIs = null;
    
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleRepositorys */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(ModulePluginList.class);
            ModulePluginList.unmarshaller = jc.createUnmarshaller();
            ModulePluginList.marshaller = jc.createMarshaller();
            ModulePluginList.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
    
    /** Default constructor */
    public ModulePluginList() {}
    
    /* Setters and getters */
    @XmlTransient public String[] getJarURIs() { return this.jarURIs; }
    public void setJarURIs(String[] jarURIs) { this.jarURIs = jarURIs; }
    
    /**
     * Returns the list of repositories encoded as a string
     */
    @Override
    public String toString() {
        return this.jarURIs.toString();
    }
     
    /**
     * Takes the input reader of the XML file and instantiates an instance of
     * the ModuleRepository class
     * <p>
     * @param r The input stream of the version XML file
     * @throw ClassCastException If the input file does not map to ModuleRepository
     * @throw JAXBException Upon error reading the XML file
     */
    public static ModulePluginList decode(Reader r) throws JAXBException {
        ModulePluginList list = (ModulePluginList)ModulePluginList.unmarshaller.unmarshal(r);
        return list;
    }
    
    /**
     * Writes the ModuleRepository class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        ModulePluginList.marshaller.marshal(this, w);
    }

    /**
     * Writes the ModuleRepository class to an output stream.
     * <p>
     * @param os The output stream to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(OutputStream os) throws JAXBException {
        ModulePluginList.marshaller.marshal(this, os);
    }
}
