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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import org.jdesktop.wonderland.server.cell.setup.BasicCellSetup.HashMapEntry;

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
public class ModuleChecksums {
    /* The SHA-1 checksum algorithm */
    public final static String SHA1_CHECKSUM_ALGORITHM = "SHA-1";
    
    /* A list of checksum entries */
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
            JAXBContext jc = JAXBContext.newInstance(ModuleChecksums.class);
            ModuleChecksums.unmarshaller = jc.createUnmarshaller();
            ModuleChecksums.marshaller = jc.createMarshaller();
            ModuleChecksums.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
  
    public static class Checksum {
        /* The hex-encoded checksum string */
        @XmlElement(name = "checksum-hex-encoded")
        public String checksum = null;

        /* The time the resource was last modified on disk, millisecs since epoch */
        @XmlElement(name = "last-modified")
        public long lastModified = 0;

        /* The relative name of the resource in the repository */
        @XmlElement(name = "resource-path")
        public String pathName = null;
        
        /** Default constructor */
        public Checksum() {
        }
        
        /**
         * Converts the checksum given as an array of bytes into a hex-encoded
         * string.
         * 
         * @param bytes The checksum as an array of bytes
         * @return The checksum as a hex-encoded string
         */
        public static String toHexString(byte bytes[]) {
            StringBuffer ret = new StringBuffer();
            for (int i = 0; i < bytes.length; ++i) {
                ret.append(Integer.toHexString(0x0100 + (bytes[i] & 0x00FF)).substring(1));
            }
            return ret.toString();
        }
    }
    
    /** Default constructor */
    public ModuleChecksums() {
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
    public static ModuleChecksums decode(Reader r) throws JAXBException {
        ModuleChecksums rc = (ModuleChecksums)ModuleChecksums.unmarshaller.unmarshal(r); 
        
        /* Convert metadata to internal representation */
        if (rc.checksums != null) {
            Iterator<Checksum> iterator = rc.checksums.iterator();
            rc.internalChecksums = new HashMap<String, Checksum>();
            while (iterator.hasNext() == true) {
                Checksum c = iterator.next();
                rc.internalChecksums.put(c.pathName, c);
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
        ModuleChecksums.marshaller.marshal(this, w);
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
        ModuleChecksums.marshaller.marshal(this, os);
    }
    
    /**
     * Creates an returns a new instance of the RepositoryCheckums object given
     * a root directory to search, the name of the checksum algorithm, and an
     * array of regular expression to match against file names to include and
     * to exclude.
     * <p>
     * If the list of regular expressions to include is either null or an empty
     * string, all files will be included. If the list of regular expressions
     * to exclude is either null or an empty string, no files are excluded.
     * 
     * @param root The root directory to search
     * @param algorithm The checksum algorithm
     * @param includes An array of regular expressions of files to include
     * @throws NoSuchAlgorithmException If the given checksum algorithm is invalid
     * @throws PatternSynaxException If either includes or excludes is invalid
     */
    public static ModuleChecksums generate(File root, String algorithm,
            String[] includes, String excludes[]) throws NoSuchAlgorithmException {
        
        /* Try creating hte message digest, throws NoSuchAlgorithmException */
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        
        /* Recursively generate checksums, then convert list to an array */
        HashMap<String, Checksum> list = ModuleChecksums.generateChecksumForDirectory(
                root, root, digest, includes, excludes);
        ModuleChecksums rc = new ModuleChecksums();
        rc.setChecksums(list);
        return rc;
    }
    
    /**
     * Recursively generates checksums for the given directory. Returns a hash
     * map of all checksum objects beneath the given root directory.
     */
    private static HashMap<String, Checksum> generateChecksumForDirectory(File root,
            File dir, MessageDigest digest, String[] includes, String[] excludes) {
 
        /* Put all of the checksums we generate in this linked list */
        HashMap<String, Checksum> list = new HashMap<String, Checksum>();

        /*
         * Loop through all of the files. If it is a directory, then recursively
         * descend into subdirectories. Before computing the checksum make sure
         * the file name satisfies the includes and excludes list.
         */
        File[] files = dir.listFiles();
        for (File file : files) {
            /* If a directory, then recursively descend and append */
            if (file.isDirectory() == true && file.isHidden() == false) {
                HashMap<String, Checksum> rList = ModuleChecksums.generateChecksumForDirectory(
                        root, file, digest, includes, excludes);
                list.putAll(rList);
            }
            else if (file.isFile() == true && file.isHidden() == false) {
                /*
                 * If a normal, non-hidden file, then check whether the name
                 * is included or excluded.
                 */
                if (ModuleChecksums.isAcceptable(file.getName(), includes, excludes) == true) {
                    try {
                        byte[] buf = new byte[1024 * 1024];
                        int bytesRead = 0;
                        FileInputStream fis = new FileInputStream(file);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        InputStream in = new DigestInputStream(bis, digest);

                        /* Read in the entire file */
                        do {
                            bytesRead = in.read(buf);
                        } while (bytesRead != -1);
                        in.close();

                        /* Compute the checksum with the digest */
                        byte[] byteChecksum = digest.digest();
                        digest.reset();

                        /*
                         * The relative path name is the absolute path name of the
                         * file, stripping off the absolute path name of the root
                         */
                        String name = file.getAbsolutePath().substring((int) (root.getAbsolutePath().length() + 1));

                        /* Create a new checksum object and add to the list */
                        Checksum c = new Checksum();
                        c.lastModified = file.lastModified();
                        c.pathName = name;
                        c.checksum = Checksum.toHexString(byteChecksum);
                        list.put(name, c);
                    } catch (java.io.IOException excp) {
                        // ignore for now
                    }
                }
            }
        }
        return list;
    }
    
    /**
     * Given a file name and an array of (possible null) string regular
     * expressions of files patterns to include and exclude, return true if
     * the given file name is acceptable.
     */
    public static boolean isAcceptable(String name, String[] includes, String excludes[]) {
        /* Track whether the file name is acceptable */
        boolean acceptable = true;
        
        /*
         * Check whether the file name is expicitly included. Only do this if
         * includes is either not null and not an empty string. We need to
         * only match one includes to be included.
         */
        if (includes != null && includes.length > 0) {
            acceptable = false;
            for (String include : includes) {
                if (name.matches(include) == true) {
                    acceptable = true;
                    break;
                }
            }
        }
        
        /* If we did match any includes return false */
        if (acceptable == false) {
            return false;
        }
        
        /*
         * Check whether the file name is explicitly excluded. Only do this if
         * excludes is either not null and not an empty string. We need to
         * match only one excludes to be excluded.
         */
        if (excludes != null && excludes.length > 0) {
            for (String exclude: excludes) {
                if (name.matches(exclude) == true) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Generates the checksum file for a given directory (given as the first
     * argument) and writes it out to a file (given as the second argument)
     */
    public static void main(String args[]) throws NoSuchAlgorithmException, JAXBException, IOException {
//        /* Sanity check on the arguments given */
//        if (args.length < 2) {
//            System.out.println("usage: java ModuleChecksums <dir> <checksum file>");
//            System.exit(0);
//        }
//     
//        /* Fetch the arguments */
//        File root = new File(args[0]);
//        File file = new File(args[1]);

        File root = new File("/Users/jordanslott/wonderland/trunk/web/examples/modules/installed/mpk20/art");
        File file = new File("checksums.xml");
        /* Generate the checksums */
        String includes[] = new String[0];
        String excludes[] = new String[0];
        
        ModuleChecksums checksums = ModuleChecksums.generate(root, SHA1_CHECKSUM_ALGORITHM, includes, excludes);
        checksums.encode(new FileWriter(file));
    }
}
