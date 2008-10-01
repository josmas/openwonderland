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

package org.jdesktop.wonderland.common.cell.setup;

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

/**
 * The BasicCellSetup class is the base class for all classes that represent
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
public abstract class BasicCellSetup implements Serializable {

    /* The (x, y, z) origin of the cell */
    @XmlElement(name="origin")
    public Origin origin = new Origin();
    
    /* The cell bounds */
    @XmlElement(name="bounds")
    public Bounds bounds = new Bounds();
    
    /* The (x, y, z) components of the scaling */
    @XmlElement(name="scale")
    public Scaling scaling = new Scaling();
    
    /* The rotation about an (x, y, z) axis and angle (radians) */
    @XmlElement(name="rotation")
    public Rotation rotation = new Rotation();
    
    /* Arbitrary collection of key-value meta data */
    @XmlElements({ 
        @XmlElement(name="metadata")
    })
    public MetaDataHashMap metadata = new MetaDataHashMap();
    
    @XmlElementRefs({
        @XmlElementRef()
    })
    public CellComponentSetup components[] = new CellComponentSetup[0];
    
    /*
     * The internal representation of the metadata as a hashed map. The HashMap
     * class is not supported by JAXB so we must convert it to a list for
     * serialization
     */
    @XmlTransient
    public HashMap<String, String> internalMetaData = new HashMap<String, String>();
    
    /**
     * The Origin static inner class simply stores (x, y, z) cell origin.
     */
    public static class Origin implements Serializable {
        /* The (x, y, z) origin components */
        @XmlElement(name="x") public double x = 0;        
        @XmlElement(name="y") public double y = 0;        
        @XmlElement(name="z") public double z = 0;
        
        /** Default constructor */
        public Origin() {
        }
    }
    
    /**
     * The Bounds static inner class stores the bounds type and bounds radius.
     */
    public static class Bounds implements Serializable {
        public enum BoundsType { SPHERE, BOX };
                
        /* The bounds type, either SPHERE or BOX */
        @XmlElement(name="type") public BoundsType type = BoundsType.SPHERE;

        /* The radius of the bounds */
        @XmlElement(name="radius") public double radius = 1.0;
        
        /** Default constructor */
        public Bounds() {
        }
    }
    
    /**
     * The Scaling static inner class stores the scaling for each of the
     * (x, y, z) components
     */
    public static class Scaling implements Serializable {
        /* The (x, y, z) scaling components */
        @XmlElement(name="x") public double x = 1;  
        @XmlElement(name="y") public double y = 1;
        @XmlElement(name="z") public double z = 1;
        
        /** Default constructor */
        public Scaling() {
        }
    }
    
    /**
     * The Rotation static inner class stores a rotation about an (x, y, z)
     * axis over an angle.
     */
    public static class Rotation implements Serializable {
        /* The (x, y, z) rotation axis components */
        @XmlElement(name="x") public double x = 0;        
        @XmlElement(name="y") public double y = 0;        
        @XmlElement(name="z") public double z = 0;
        
        /* The angle (radians) about which to rotate */
        @XmlElement(name="angle") public double angle = 0;
        
        /** Default constructor */
        public Rotation() {
        }
    }
    
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
    public BasicCellSetup() {
    }
    
    /**
     * Returns the fully-qualified class name for the server-side cell class
     * to instantiate.
     * 
     * @return The FQCN of the server-side cell class
     */
    public abstract String getServerClassName();

    /**
     * Returns the cell origin.
     * 
     * @return The cell origin
     */
    @XmlTransient public Origin getOrigin() {
        return this.origin;
    }
    
    /**
     * Sets the cell origin. If null, then this property will not be written
     * out to the file.
     * 
     * @param origin The new cell origin
     */
    public void setOrigin(Origin origin) {
        this.origin = origin;
    }
    
    /**
     * Returns the cell bounds.
     * 
     * @return The cell bounds
     */
    @XmlTransient public Bounds getBounds() {
        return this.bounds;
    }
    
    /**
     * Sets the cell bounds. If null, then this property will not be written
     * out to the file.
     * 
     * @param bounds The new cell bounds
     */
    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }
    
    /**
     * Returns the cell scaling.
     * 
     * @return The cell scaing
     */
    @XmlTransient public Scaling getScaling() {
        return this.scaling;
    }
    
    /**
     * Sets the cell scaling. If null, then this property will not be written
     * out to the file.
     * 
     * @param scaling The new cell scaling
     */
    public void setScaling(Scaling scaling) {
        this.scaling = scaling;
    }    
    
    /**
     * Returns the cell rotation.
     * 
     * @return The cell rotation
     */
    @XmlTransient public Rotation getRotation() {
        return this.rotation;
    }
    
    /**
     * Sets the cell rotation. If null, then this property will not be written
     * out to the file.
     * 
     * @param rotation The new cell rotation
     */
    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }
    
    /**
     * Returns the cell's collection of component setup information
     * 
     * @return The cell's collection of component setup information
     */
    @XmlTransient public CellComponentSetup[] getCellComponentSetups() {
        return this.components;
    }
    
    /**
     * Sets the cell's collection of component setup information. If null, then
     * this property will not be written out to the file.
     * 
     * @param bounds The new cell bounds
     */
    public void setCellComponentSetups(CellComponentSetup[] components) {
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
    
    /**
     * Takes the input stream of the XML file and instantiates an instance of
     * the BasicCellSetup class
     * <p>
     * @param is The input stream of the version XML file
     * @throw ClassCastException If the input file does not map to BasicCellSetup
     * @throw JAXBException Upon error reading the XML file
     */
    public static BasicCellSetup decode(Reader r) throws JAXBException {
        return decode(r, null);
    }

    public static BasicCellSetup decode(Reader r, ClassLoader classLoader) throws JAXBException {
        /* Read from XML */
        Unmarshaller u = CellSetupFactory.getUnmarshaller(classLoader);
        BasicCellSetup setup = (BasicCellSetup)u.unmarshal(r);
        
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
     * Writes the BasicCellSetup class to an output stream.
     * <p>
     * @param os The output stream to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        encode(w, null);
    }
    
    
    public void encode(Writer w, ClassLoader classLoader) throws JAXBException {
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
        Marshaller m = CellSetupFactory.getMarshaller(classLoader);
        m.marshal(this, w);
    }
    
    /**
     * Returns a string representation of this class
     *
     * @return The setup information as a string
     */
    @Override
    public String toString() {
        return "[BasicCellSetup] origin=(" + this.origin.x + "," + this.origin.y +
                "," + this.origin.z + ") rotation=(" + this.rotation.x + "," +
                this.rotation.y + "," + this.rotation.z + ") @ " + this.rotation.angle +
                " scaling=(" + this.scaling.x + "," + this.scaling.y + "," +
                this.scaling.z + ")";
    }
    
//    public static void main(String[] args) throws JAXBException, IOException {
//        StaticModelCellSetup setup = new StaticModelCellSetup();
//        //setup.setMetaData(null);
//        setup.encode(new FileWriter("setup.xml"));
//    }
}
