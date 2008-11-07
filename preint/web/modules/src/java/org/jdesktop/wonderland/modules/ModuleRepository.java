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
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;


/**
 * A module's repository information, as represented by this class, represents
 * the collection of art assets and where they can be found over the Internet.
 * <p>
 * A "repository" is a collection of art assets located somewhere on the
 * Internet and available to clients for download. Two fundamental kinds of
 * repositories exist: master and mirror. Master repositories represent the
 * primary copy of art assets; mirror repositories represent copies of the
 * art assets, available perhaps by a server that provides quicker downloads.
 * <p>
 * This class stores a list of "resources", each some piece of art generally.
 * This list is optional -- entries need not exist for resources that exist
 * themselves in the module. This list, therefore, provides a means to include
 * a resource in the module without including the actual artwork itself.
 * <p>
 * This class also stores the name of the master repository where the artwork
 * can be downloaded and also a list of mirror repositories. Both the master
 * and mirror repositories are optional. If no master or mirror is specified,
 * then it is assumed the artwork is made available by the Wonderland server
 * in which the module is installed.
 * <p>
 * If an entry contains the special string %WL_SERVER% then the hostname of
 * the machine on which the module is installed is inserted before send to
 * the client. This special tag can be use as either the master or any one of
 * the mirror repositories.
 * <p>
 * By default, the hostname of the machine on which the module is installed is
 * inserted as the final mirror, in case other repositories cannot be found. This
 * happens only if no other entry contains the %WL_SERVER% tag.
 * <p>
 * This class is annotation with JAXB XML elements and supports encoding and
 * decoding to/from XML via the encode() and decode() methods, respectively.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="module-repository")
public class ModuleRepository implements Serializable {
    /*
     * The special string that denotes the Wonderland server from which the
     * module was installed should be used.
     */
    public static final String WL_SERVER = "%WLSERVER%";
    
    /* An array of module resources, as relative paths within the module */
    @XmlElements({
        @XmlElement(name="resource")
    })
    private String[] resources = null;
    
    /* The hostname of the master asset server for this repository */
    @XmlElement(name="master") private Repository master = null;
  
    /* An array of hostnames that serve as mirrors for serving the assets */
    @XmlElements({
        @XmlElement(name = "mirror")
    })
    private Repository[] mirrors   = null;
    
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleRepositorys */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(ModuleRepository.class);
            ModuleRepository.unmarshaller = jc.createUnmarshaller();
            ModuleRepository.marshaller = jc.createMarshaller();
            ModuleRepository.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
    
    /**
     * The Repository static inner class simply stores the base URL of the
     * repository and whether it is located on the web server itself (if it is,
     * then there is no need to check the checksums). 
     */
    public static class Repository {
        /* The base URL */
        @XmlValue
        public String url = null;
        
        /* Whether it is the server */
        @XmlAttribute(name="isServer")
        public boolean isServer = false;
        
        /** Default constructor */
        public Repository() {}
        
        /** Constructor, takes an existing Repository as an argument */
        public Repository(Repository repository) {
            this.url = repository.url;
            this.isServer = repository.isServer;
        }
    }
    /** Default constructor */
    public ModuleRepository() {}
    
    /** Constructor that takes an existing ModuleRepository and makes a copy */
    public ModuleRepository(ModuleRepository repository) {
        this.master = (repository.getMaster() != null) ? new Repository(repository.getMaster()) : null;
        this.mirrors = (repository.getMirrors() != null) ? new Repository[repository.getMirrors().length] : null;
        if (this.mirrors != null) {
            for (int i = 0; i < this.mirrors.length; i++) {
                this.mirrors[i] = mirrors[i];
            }
        }
        this.resources = (repository.getResources() != null) ? new String[repository.getResources().length] : null;
        if (this.resources != null) {
            for (int i = 0; i < this.resources.length; i++) {
                this.resources[i] = resources[i];
            }
        }
    }
    
    /* Setters and getters */
    @XmlTransient public String[] getResources() { return this.resources; }
    public void setResources(String[] resources) { this.resources = resources; }
    @XmlTransient public Repository getMaster() { return this.master; }
    public void setMaster(Repository master) { this.master = master; }
    @XmlTransient public Repository[] getMirrors() { return this.mirrors; }
    public void setMirrors(Repository[] mirrors) { this.mirrors = mirrors; }
    
        /**
     * Returns the version as a string: <major>.<minor>
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        String masterName = (this.getMaster() != null) ? this.getMaster().url : (null);
        str.append("\t[master] " + masterName + "\n");
        str.append("\t[mirrors] ");
        if (this.mirrors != null) {
            for (Repository mirror : mirrors) {
                str.append(mirror.url + " ");
            }
        }
        return str.toString();
    }
     
    /**
     * Takes the input reader of the XML file and instantiates an instance of
     * the ModuleRepository class
     * <p>
     * @param r The input stream of the version XML file
     * @throw ClassCastException If the input file does not map to ModuleRepository
     * @throw JAXBException Upon error reading the XML file
     */
    public static ModuleRepository decode(Reader r) throws JAXBException {
        return (ModuleRepository)ModuleRepository.unmarshaller.unmarshal(r);        
    }
    
    /**
     * Writes the ModuleRepository class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        ModuleRepository.marshaller.marshal(this, w);
    }

    /**
     * Writes the ModuleRepository class to an output stream.
     * <p>
     * @param os The output stream to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(OutputStream os) throws JAXBException {
        ModuleRepository.marshaller.marshal(this, os);
    }
}
