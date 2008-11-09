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
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The ChecksumList class represents a collection of checkums for a given
 * repository in one file. It can be generated automatically by the static
 * generate() utility method.
 * <p>
 * This class uses JAXB to encode/decode the class to/from XML, either on disk
 * or over the network
 * 
 * @author paulby
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="repository-checksums")
public class ChecksumList {
    /* The SHA-1 checksum algorithm */
    public final static String SHA1_CHECKSUM_ALGORITHM = "SHA-1";
    
    /* A hashtable to resource path name-checksum entries */
    @XmlElements({ 
        @XmlElement(name="checksum")
    })
    public LinkedList<Checksum> checksums = new LinkedList<Checksum>();

    /*
     * The internal representation of the checksums as a hashed map. The HashMap
     * class is not supported by JAXB so we must convert it to a list for
     * serialization
     */
    @XmlTransient
    public HashMap<String, Checksum> internalChecksums = new HashMap<String, Checksum>();
    
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleInfos */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(ChecksumList.class);
            ChecksumList.unmarshaller = jc.createUnmarshaller();
            ChecksumList.marshaller = jc.createMarshaller();
            ChecksumList.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
        
    /** Default constructor */
    public ChecksumList() {
    }
    
    /**
     * Sets the array of individual checksums.
     * 
     * @param checksums An hash map of Checksum objects
     */
    public void setChecksums(HashMap<String, Checksum> checksums) {
        this.internalChecksums = checksums;
    }
    
    /**
     * Returns the array of individual checksums.
     * 
     * @return An array of Checksum objects
     */
    @XmlTransient
    public HashMap<String, Checksum> getChecksums() {
        return this.internalChecksums;
    }
    
    /**
     * Takes the input reader of the XML file and instantiates an instance of
     * the ChecksumList class
     * <p>
     * @param r The input reader of the version XML file
     * @throw ClassCastException If the input file does not map to ChecksumList
     * @throw JAXBException Upon error reading the XML file
     */
    public static ChecksumList decode(Reader r) throws JAXBException {
        ChecksumList rc = (ChecksumList)ChecksumList.unmarshaller.unmarshal(r); 
        
//        System.out.println("Checksumlist: rc=" + rc);
        /* Convert metadata to internal representation */
        if (rc.checksums != null) {
            Iterator<Checksum> iterator = rc.checksums.iterator();
            rc.internalChecksums = new HashMap<String, Checksum>();
            while (iterator.hasNext() == true) {
                Checksum c = iterator.next();
                rc.internalChecksums.put(c.getPathName(), c);
//                System.out.println("checksum: " + c.getPathName());
            }
        }
        else {
            rc.internalChecksums = null;
        }
        return rc;
    }
    
    /**
     * Writes the ChecksumList class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        /* Convert internal checksum hash to one suitable for serialization */
        if (this.internalChecksums != null) {
            this.checksums = new LinkedList<Checksum>();
            for (Map.Entry<String, Checksum> e : this.internalChecksums.entrySet()) {
                this.checksums.add(e.getValue());
            }
        }
        else {
            this.checksums = null;
        }
        ChecksumList.marshaller.marshal(this, w);
    }

    /**
     * Writes the ChecksumList class to an output stream.
     * <p>
     * @param os The output stream to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(OutputStream os) throws JAXBException {
        /* Convert internal checksum hash to one suitable for serialization */
        if (this.internalChecksums != null) {
            this.checksums = new LinkedList<Checksum>();
            for (Map.Entry<String, Checksum> e : this.internalChecksums.entrySet()) {
                this.checksums.add(e.getValue());
            }
        }
        else {
            this.checksums = null;
        }
        ChecksumList.marshaller.marshal(this, os);
    }
}
