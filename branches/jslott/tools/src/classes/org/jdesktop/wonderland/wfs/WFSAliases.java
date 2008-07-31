/**
 * Project Looking Glass
 *
 * $RCSfile: WFSAliases.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.2.8.2 $
 * $Date: 2008/04/08 10:44:30 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.wfs;

import java.io.File;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.cells.BasicCellSetup;

/**
 * The WFSAlises class represents a series of mappings between 'wfs' type URIs
 * and disk locations within a Wonderland File System (WFS). Currently this
 * mapping is stored as a hashmap of Strings, and there is no checking of
 * whether these Strings map to valid names.
 * <p>
 * This class follows the Java Bean pattern (default constructor, setter/getter
 * methods) so that it may be serialised to/from disk. The static decode() and
 * encode methods take an instance of a WFSAlises class and perform the loading
 * and saving from/to disk.
 *
 * @author jslott
 */
@XmlRootElement(name="wfs-aliases")
public class WFSAliases {
    /* Arbitrary collection of key-value aliases */
    @XmlElements({
        @XmlElement(name="alias")
    })
    public AliasesHashMap aliases = new AliasesHashMap();
    
    /*
     * The internal representation of the aliases as a hashed map. The HashMap
     * class is not supported by JAXB so we must convert it to a list for
     * serialization
     */
    @XmlTransient
    public HashMap<String, String> internalAliases = new HashMap<String, String>();
 
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all setup classes */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(WFSAliases.class.getName());
            WFSAliases.unmarshaller = jc.createUnmarshaller();
            WFSAliases.marshaller = jc.createMarshaller();
            WFSAliases.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
    
     /**
     * A wrapper class for hashmaps, because JAXB does not correctly support
     * the HashMap class.
     */
    public static class AliasesHashMap implements Serializable {
        /* A list of entries */
        @XmlElements( {
            @XmlElement(name="entry")
        })
        public List<HashMapEntry> entries = new ArrayList<HashMapEntry>();

        /** Default constructor */
        public AliasesHashMap() {
        }
    }
    
    /**
     * A wrapper class for hashmap entries, because JAXB does not correctly
     * support the HashMap class
     */
    public static class HashMapEntry implements Serializable {
        /* The key and values */
        @XmlAttribute public String key;
        @XmlAttribute public String value;

        /** Default constructor */
        public HashMapEntry() {
        }
    }
    
    /** Default constructor */
    public WFSAliases() {}

    /**
     * Returns the cell aliases.
     * 
     * @return The cell aliases
     */
    @XmlTransient public HashMap<String, String> getAliases() {
        return this.internalAliases;
    }
    
    /**
     * Sets the cell's aliases. If null, then this property will not be
     * written out to the file.
     * 
     * @param aliases The new cell aliases
     */
    public void setAliases(HashMap<String, String> aliases) {
        this.internalAliases = aliases;
    }

    /**
     * Returns the aliases as a string, where each line represents a single entry
     * of the form: <aliases>.<location>
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> i = this.internalAliases.entrySet().iterator();
        while (i.hasNext() == true) {
            Map.Entry<String, String> entry = i.next();
            sb.append(entry.toString() + "\n");
        }
        return sb.toString();
    }
    
    /**
     * Takes the input stream of the XML file and instantiates an instance of
     * the WFSAliases class
     * <p>
     * @param is The input stream of the version XML file
     * @throw ClassCastException If the input file does not map to WFSAliases
     * @throw JAXBException Upon error reading the XML file
     */
    public static WFSAliases decode(Reader r) throws JAXBException {
        /* Read from XML */
        WFSAliases setup = (WFSAliases)WFSAliases.unmarshaller.unmarshal(r);
        
        /* Convert Aliases to internal representation */
        if (setup.aliases != null) {
            ListIterator<HashMapEntry> iterator = setup.aliases.entries.listIterator();
            setup.internalAliases = new HashMap<String, String>();
            while (iterator.hasNext() == true) {
                HashMapEntry entry = iterator.next();
                setup.internalAliases.put(entry.key, entry.value);
            }
        }
        else {
            setup.internalAliases = null;
        }
        return setup;
    }
    
    /**
     * Writes the WFSAliases class to an output writer.
     * <p>
     * @param os The output stream to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        /* Convert internal Aliases hash to one suitable for serialization */
        if (this.internalAliases != null) {
            this.aliases = new AliasesHashMap();
            for (Map.Entry<String, String> e : this.internalAliases.entrySet()) {
                HashMapEntry entry = new HashMapEntry();
                entry.key = e.getKey();
                entry.value = e.getValue();
                this.aliases.entries.add(entry);
            }
        }
        else {
            this.aliases = null;
        }

        /* Write out as XML */
        WFSAliases.marshaller.marshal(this, w);
    }
    
    /**
     * Main method which writes a sample WFSAlises class to disk
     */
    public static void main(String args[]) {
        try {
            WFSAliases aliases = new WFSAliases();
            HashMap<String, String> a = new HashMap<String, String>();
            a.put("rooms/office", "cells/offce/room.xml");
            aliases.encode(new FileWriter(new File("aliases.xml")));
        } catch (java.lang.Exception excp) {
            System.out.println(excp.toString());
        }
    }
}