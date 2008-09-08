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


/**
 * A module's repository information, as represented by this class, represents
 * the collection of URLs where assets may be found over the Internet.
 * <p>
 * This class stores the name of the master repository where the artwork
 * can be downloaded and also a list of mirror repositories. Both the master
 * and mirror repositories are optional. If no master or mirror is specified,
 * then it is assumed the artwork is made available by the Wonderland server
 * in which the module is installed (if use_server is not false).
 * <p>
 * This class deserializes information distributed from the module service.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="module-repository")
public class RepositoryList implements Serializable {
    
    /* An array of module resources, as relative paths within the module */
    @XmlElements({
        @XmlElement(name="resource")
    })
    private String[] resources = null;
    
    /* The hostname of the master asset server for this repository */
    @XmlElement(name="master") private String master = null;
  
    /* An array of hostnames that serve as mirrors for serving the assets */
    @XmlElements({
        @XmlElement(name = "mirror")
    })
    private String[] mirrors   = null;
    
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleRepositorys */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(RepositoryList.class);
            RepositoryList.unmarshaller = jc.createUnmarshaller();
            RepositoryList.marshaller = jc.createMarshaller();
            RepositoryList.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
    
    /** Default constructor */
    public RepositoryList() {}
    
    /** Constructor that takes an existing ModuleRepository and makes a copy */
    public RepositoryList(RepositoryList repository) {
        this.master = (repository.getMaster() != null) ? new String(repository.getMaster()) : null;
        this.mirrors = (repository.getMirrors() != null) ? new String[repository.getMirrors().length] : null;
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
    @XmlTransient public String getMaster() { return this.master; }
    public void setMaster(String master) { this.master = master; }
    @XmlTransient public String[] getMirrors() { return this.mirrors; }
    public void setMirrors(String[] mirrors) { this.mirrors = mirrors; }
    
        /**
     * Returns the version as a string: <major>.<minor>
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Module Repository:\n");
        str.append("Master:\n  " + this.getMaster() + "\n");
        str.append("Mirrors:\n");
        if (this.mirrors != null) {
            for (String mirror : mirrors) {
                str.append("  " + mirror + "\n");
            }
        }
        if (this.resources != null) {
            str.append("Resources:\n");
            for (String resource : resources) {
                str.append("  " + resource + "\n");
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
    public static RepositoryList decode(Reader r) throws JAXBException {
        return (RepositoryList)RepositoryList.unmarshaller.unmarshal(r);        
    }
    
    /**
     * Writes the ModuleRepository class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        RepositoryList.marshaller.marshal(this, w);
    }

    /**
     * Writes the ModuleRepository class to an output stream.
     * <p>
     * @param os The output stream to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(OutputStream os) throws JAXBException {
        RepositoryList.marshaller.marshal(this, os);
    }
}
