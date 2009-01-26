/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.common.cell.state;

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.AssetURIAdapter;

/**
 * The CellServerState class is the base class for all classes that represent
 * the setup information for specific cell types. This class must be overridden
 * by the cell-specific setup class.
 * <p>
 * In additional to the setup information defined here, the cell-specific setup
 * class may also add additional parameters and require one or more component
 * setup classes. It must define the getServerClassName() method to return the
 * fully-qualified name of the server-side cell class.
 * <p>
 * The subclass of this class must be annotated with @XmlRootElement that
 * defines the root element for all documents of that cell type. It must be
 * unique for all cell types.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class CellServerState implements Serializable {

    /* The name of the cell */
    @XmlElement(name="name")
    private String name = null;

    /* Arbitrary collection of key-value meta data */
    @XmlElements({ 
        @XmlElement(name="metadata")
    })
    public MetaDataHashMap metadata = new MetaDataHashMap();
    
    @XmlElementRefs({
        @XmlElementRef()
    })
    public CellComponentServerState components[] = new CellComponentServerState[0];
    
    /*
     * The internal representation of the metadata as a hashed map. The HashMap
     * class is not supported by JAXB so we must convert it to a list for
     * serialization
     */
    @XmlTransient
    public HashMap<String, String> internalMetaData = new HashMap<String, String>();

    /**
     * A wrapper class for hashmaps, because JAXB does not correctly support
     * the HashMap class.
     */
    public static class MetaDataHashMap implements Serializable {
        /* A list of entries */
        @XmlElements( {
            @XmlElement(name="entry")
        })
        public List<HashMapEntry> entries = new ArrayList<HashMapEntry>();

        /** Default constructor */
        public MetaDataHashMap() {
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
    public CellServerState() {
    }
    
    /**
     * Returns the fully-qualified class name for the server-side cell class
     * to instantiate.
     * 
     * @return The FQCN of the server-side cell class
     */
    public abstract String getServerClassName();
    
    /**
     * Returns the cell's collection of component setup information
     * 
     * @return The cell's collection of component setup information
     */
    @XmlTransient public CellComponentServerState[] getCellComponentServerStates() {
        return this.components;
    }
    
    /**
     * Sets the cell's collection of component setup information. If null, then
     * this property will not be written out to the file.
     * 
     * @param bounds The new cell bounds
     */
    public void setCellComponentServerStates(CellComponentServerState[] components) {
        this.components = components;
    }
    
    /**
     * Returns the cell metadata.
     * 
     * @return The cell metadata
     */
    @XmlTransient public HashMap<String, String> getMetaData() {
        return this.internalMetaData;
    }
    
    /**
     * Sets the cell's metadata. If null, then this property will not be
     * written out to the file.
     * 
     * @param metadata The new cell metadata
     */
    public void setMetaData(HashMap<String, String> metadata) {
        this.internalMetaData = metadata;
    }

    @XmlTransient public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
   
    /**
     * Takes the input reader of the XML data and instantiates an instance of
     * the CellServerState class
     * <p>
     * @param r The input reader of the XML data
     * @throw ClassCastException If the input data does not map to CellServerState
     * @throw JAXBException Upon error reading the XML data
     */
    public static CellServerState decode(Reader r) throws JAXBException {
        return decode(r, null, null);
    }

    /**
     * Takes the input reader of the XML file and instantiates an instance of
     * the CellServerState class. Also takes the class loader and server name
     * associated with the context.
     * <p>
     * @param r The input data of the version XML data
     * @param cl The class loader
     * @param server The name of the server
     * @throw ClassCastException If the input data does not map to CellServerState
     * @throw JAXBException Upon error reading the XML data
     */
    public static CellServerState decode(Reader r, ClassLoader cl, String server) throws JAXBException {
        /*
         * De-serialize from XML. We set up an adapter to handle XML elements
         * of type AssetURI. This will properly decode them and also fill in
         * the name of the server context.
         */
        Unmarshaller u = CellServerStateFactory.getUnmarshaller(cl);
        if (server != null) {
            AssetURIAdapter adapter = new AssetURIAdapter(server);
            u.setAdapter(adapter);
        }
        CellServerState setup = (CellServerState)u.unmarshal(r);
        
        /* Convert metadata to internal representation */
        if (setup.metadata != null) {
            ListIterator<HashMapEntry> iterator = setup.metadata.entries.listIterator();
            setup.internalMetaData = new HashMap<String, String>();
            while (iterator.hasNext() == true) {
                HashMapEntry entry = iterator.next();
                setup.internalMetaData.put(entry.key, entry.value);
            }
        }
        else {
            setup.internalMetaData = null;
        }
        return setup;
    }
    
    /**
     * Writes the CellServerState class to an output writer.
     * <p>
     * @param w The output write to write to
     * @throw JAXBException Upon error writing the XML data
     */
    public void encode(Writer w) throws JAXBException {
        encode(w, null);
    }
    
    /**
     * Writes the CellServerState class to an output writer. Also takes the
     * class loader context.
     * <p>
     * @param w The output write to write to
     * @param cl The class loader
     * @throw JAXBException Upon error writing the XML data
     */   
    public void encode(Writer w, ClassLoader cl) throws JAXBException {
        /* Convert internal metadata hash to one suitable for serialization */
        if (this.internalMetaData != null) {
            this.metadata = new MetaDataHashMap();
            for (Map.Entry<String, String> e : this.internalMetaData.entrySet()) {
                HashMapEntry entry = new HashMapEntry();
                entry.key = e.getKey();
                entry.value = e.getValue();
                this.metadata.entries.add(entry);
            }
        }
        else {
            this.metadata = null;
        }

        /* Write out as XML */
        Marshaller m = CellServerStateFactory.getMarshaller(cl);
        m.marshal(this, w);
    }
    
    /**
     * Returns a string representation of this class
     *
     * @return The setup information as a string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[CellServerState] ");
        for (CellComponentServerState state : this.getCellComponentServerStates()) {
            sb.append(state.toString());
        }
        return sb.toString();
    }
}
