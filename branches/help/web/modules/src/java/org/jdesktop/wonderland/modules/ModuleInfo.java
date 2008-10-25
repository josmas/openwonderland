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

package org.jdesktop.wonderland.modules;

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
 * The ModuleInfo class represents the basic information about a module: its
 * unique name and its major.minor version.
 * <p>
 * This class follows the Java Bean pattern (default constructor, setter/getter
 * methods) so that it may be serialised to/from disk. The static decode() and
 * encode methods take an instance of a ModuleVersion class and perform the
 * loading and saving from/to disk.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="wonderland-module")
public class ModuleInfo implements Serializable {
    /* Flag indicating that a version component is unset */
    public static final int VERSION_UNSET = -1;
    
    /* The unique module name */
    @XmlElement(name="name", required=true)
    private String name = null;
    
    /* The version numbers */
    @XmlElement(name="version")
    private Version version = new ModuleInfo.Version();
    
    /* A textual description of the module */
    @XmlElement(name="description")
    private String description = null;
    
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleInfos */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(ModuleInfo.class);
            ModuleInfo.unmarshaller = jc.createUnmarshaller();
            ModuleInfo.marshaller = jc.createMarshaller();
            ModuleInfo.marshaller.setProperty("jaxb.formatted.output", true);
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
        public int major = ModuleInfo.VERSION_UNSET;
        
        @XmlElement(name="minor")
        public int minor = ModuleInfo.VERSION_UNSET;
        
        /** Default constructor */
        public Version() {}

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Version other = (Version) obj;
            if (this.major != other.major) {
                return false;
            }
            if (this.minor != other.minor) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + this.major;
            hash = 71 * hash + this.minor;
            return hash;
        }
    }
    
    /** Default constructor */
    public ModuleInfo() {}
    
    /** Constructor which takes major/minor version number and description */
    public ModuleInfo(String name, int major, int minor, String description) {
        this(name, major, minor);
        this.description = description;
    }
    
    /** Constructor which takes major/minor version number */
    public ModuleInfo(String name, int major, int minor) {
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
    @XmlTransient public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }
    
    /**
     * Returns the version as a string: <major>.<minor>
     */
    @Override
    public String toString() {
        return this.getName() + "(v" +
                Integer.toString(this.getMajor()) + "." +
                Integer.toString(this.getMinor()) + ")";
    }
    
    /**
     * Takes the input reader of the XML file and instantiates an instance of
     * the ModuleInfo class
     * <p>
     * @param r The input reader of the version XML file
     * @throw ClassCastException If the input file does not map to ModuleInfo
     * @throw JAXBException Upon error reading the XML file
     */
    public static ModuleInfo decode(Reader r) throws JAXBException {
        return (ModuleInfo)ModuleInfo.unmarshaller.unmarshal(r);        
    }
    
    /**
     * Writes the ModuleInfo class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        ModuleInfo.marshaller.marshal(this, w);
    }

    /**
     * Writes the ModuleInfo class to an output stream.
     * <p>
     * @param os The output stream to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(OutputStream os) throws JAXBException {
        ModuleInfo.marshaller.marshal(this, os);
    }
    
    /**
     * Returns true if both the module name and version matches, false if not
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ModuleInfo)) {
            return false;
        }
        ModuleInfo info = (ModuleInfo)object;
        if (this.getName().equals(info.getName()) == false) {
            return false;
        }
        if (this.version.equals(info.version) == false) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 13 * hash + (this.version != null ? this.version.hashCode() : 0);
        return hash;
    }
    
    /**
     * Main method which writes a sample WFSVersion class to disk
     */
    public static void main(String args[]) {
        try {
            ModuleInfo info = new ModuleInfo();
            info.setName("MPK20 Demo");
            info.setMajor(5);
            info.setMinor(3);
            info.encode(new FileWriter(new File("/Users/jordanslott/module.xml")));
            
            info = ModuleInfo.decode(new FileReader(new File("/Users/jordanslott/module.xml")));
            System.out.println(info.getName());
            System.out.println(info.getMajor());
            System.out.println(info.getMinor());
        } catch (java.lang.Exception excp) {
            System.out.println(excp.toString());
        }
    }
}
