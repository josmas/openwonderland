

package org.jdesktop.wonderland.modules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The ModuleRequires class represents a dependency this module has on another
 * module. Dependencies are declared using the unique name of the module and an
 * optional major.minor version. The version may be unspecified, in which case
 * any version is acceptable.
 * <p>
 * This convenience method isAcceptable() takes an instance of a module and
 * returns true whether it satisfies the dependency specified by the instance
 * of the ModuleRequires class.
 * <p>
 * This class follows the Java Bean pattern (default constructor, setter/getter
 * methods) so that it may be serialised to/from disk. The static decode() and
 * encode methods take an instance of a ModuleRequires class and perform the
 * loading and saving from/to disk.
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="module-requires")
public class ModuleRequires {
    
    /* An array of modules that are dependencies */
    @XmlElements({
        @XmlElement(name="requires")
    })
    private ModuleInfo[] requires = new ModuleInfo[] {};
    
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleInfos */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(ModuleRequires.class);
            ModuleRequires.unmarshaller = jc.createUnmarshaller();
            ModuleRequires.marshaller = jc.createMarshaller();
            ModuleRequires.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
    
    /** Default constructor */
    public ModuleRequires() {
    }
    
    /** Constructor which takes an array of dependencies */
    public ModuleRequires(ModuleInfo[] requires) {
        this.requires = requires;
    }
    
    /* Java Bean Setter/Getter methods */
    @XmlTransient public ModuleInfo[] getRequires() { return this.requires; }
    public void setRequires(ModuleInfo[] requires) { this.requires = requires; }
    
    /**
     * Returns the list of module dependencies as a string: name vX.Y
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (ModuleInfo info : requires) {
            str.append("  " + info.toString() + " ");
        }
        return str.toString();
    }
    
    /**
     * Returns true if the given modules is required by this requirement set,
     * false if not. Only the unique name is checked.
     * 
     * @param uniqueName The module name to check if it is required
     * @return True if the given module is required, false if not
     */
    public boolean isRequired(String uniqueName) {
        for (ModuleInfo info : this.requires) {
            if (uniqueName.compareTo(info.getName()) == 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Takes the input stream of the XML file and instantiates an instance of
     * the ModuleRequires class
     * <p>
     * @param is The input stream of the version XML file
     * @throw ClassCastException If the input file does not map to ModuleRequires
     * @throw JAXBException Upon error reading the XML file
     */
    public static ModuleRequires decode(InputStream is) throws JAXBException {
        return (ModuleRequires)ModuleRequires.unmarshaller.unmarshal(is);        
    }

        /**
     * Takes the input reader of the XML file and instantiates an instance of
     * the ModuleRequires class
     * <p>
     * @param r The input reader of the requires XML file
     * @throw ClassCastException If the input file does not map to ModuleRequires
     * @throw JAXBException Upon error reading the XML file
     */
    public static ModuleRequires decode(Reader r) throws JAXBException {
        return (ModuleRequires)ModuleRequires.unmarshaller.unmarshal(r);        
    }
    
    /**
     * Writes the ModuleInfo class to an output stream.
     * <p>
     * @param os The output stream to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(OutputStream os) throws JAXBException {
        ModuleRequires.marshaller.marshal(this, os);
    }
    
    /**
     * Main method which writes a sample ModuleRequires class to disk
     */
    public static void main(String args[]) {
        try {
            ModuleRequires requires = new ModuleRequires();
            ModuleInfo info1 = new ModuleInfo("MPK20 Demo", 1, ModuleInfo.VERSION_UNSET);
            ModuleInfo info2 = new ModuleInfo("Basic Textures", ModuleInfo.VERSION_UNSET, ModuleInfo.VERSION_UNSET);
            requires.setRequires(new ModuleInfo[] { info1, info2 });
            requires.encode(new FileOutputStream(new File("/Users/jordanslott/module-wlm/requires.xml")));
        } catch (java.lang.Exception excp) {
            System.out.println(excp.toString());
        }
    }
}
