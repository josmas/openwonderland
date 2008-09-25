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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
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
 * The ModuleIdentity class represents the basic information about a module: its
 * unique name and its major.minor version.
 * <p>
 * This class serializes to an XML format distributed by the module service
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="wonderland-module")
public class ModuleIdentity implements Serializable {
    /* Flag indicating that a version component is unset */
    public static final int VERSION_UNSET = -1;
    
    /* The unique module name */
    @XmlElement(name="name", required=true)
    private String name = null;
    
    /* The version numbers */
    @XmlElement(name="version")
    private Version version = new ModuleIdentity.Version();
    
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleInfos */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(ModuleIdentity.class);
            ModuleIdentity.unmarshaller = jc.createUnmarshaller();
            ModuleIdentity.marshaller = jc.createMarshaller();
            ModuleIdentity.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
    
    /**
     * The Version static inner class simply stores the major and minor version
     * numbers
     */
    public static class Version {
        /* The major and minor version numbers */
        @XmlElement(name="major")
        public int major = ModuleIdentity.VERSION_UNSET;
        
        @XmlElement(name="minor")
        public int minor = ModuleIdentity.VERSION_UNSET;
        
        /** Default constructor */
        public Version() {}
    }
    
    /** Default constructor */
    public ModuleIdentity() {}
    
    /** Constructor which takes major/minor version number */
    public ModuleIdentity(String name, int major, int minor) {
        /* Populate the basic elements of the module info */
        this.name          = name;
        this.version.major = major;
        this.version.minor = minor;
    }
    
    /* Java Bean Setter/Getter methods */
    @XmlTransient public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    @XmlTransient public int getMajor() { return this.version.major; }
    public void setMajor(int major) { this.version.major = major; }
    @XmlTransient public int getMinor() { return this.version.minor; }
    public void setMinor(int minor) { this.version.minor = minor; }
    
    /**
     * Returns the version as a string: <major>.<minor>
     */
    @Override
    public String toString() {
        return "Module Info: " + this.getName() + "(v" +
                Integer.toString(this.getMajor()) + "." +
                Integer.toString(this.getMinor()) + ")\n";
    }
    
    /**
     * Takes the input reader of the XML file and instantiates an instance of
     * the ModuleInfo class
     * <p>
     * @param r The input reader of the version XML file
     * @throw ClassCastException If the input file does not map to ModuleInfo
     * @throw JAXBException Upon error reading the XML file
     */
    public static ModuleIdentity decode(Reader r) throws JAXBException {
        return (ModuleIdentity)ModuleIdentity.unmarshaller.unmarshal(r);        
    }
    
    /**
     * Writes the ModuleInfo class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        ModuleIdentity.marshaller.marshal(this, w);
    }

    /**
     * Writes the ModuleInfo class to an output stream.
     * <p>
     * @param os The output stream to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(OutputStream os) throws JAXBException {
        ModuleIdentity.marshaller.marshal(this, os);
    }
}
