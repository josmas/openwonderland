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

package org.jdesktop.wonderland.checksum;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The RepositoryChecksums class represents a collection of checkums for a given
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
public class RepositoryChecksums {
    /* The SHA-1 checksum algorithm */
    public final static String SHA1_CHECKSUM_ALGORITHM = "SHA-1";
    
    /* An array of individual checksum objects */
    @XmlElements({
        @XmlElement(name="checksum")
    })
    public Checksum[] checksums = null;
    
    /** Default constructor */
    public RepositoryChecksums() {
    }
    
    /**
     * Sets the array of individual checksums.
     * 
     * @param checksums An array of Checksum objects
     */
    public void setChecksums(Checksum[] checksums) {
        this.checksums = checksums;
    }
    
    /**
     * Returns the array of individual checksums.
     * 
     * @return An array of Checksum objects
     */
    public Checksum[] getChecksums() {
        return this.checksums;
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
    public static RepositoryChecksums generate(File root, String algorithm,
            String[] includes, String excludes[]) throws NoSuchAlgorithmException {
        
        /* Try creating hte message digest, throws NoSuchAlgorithmException */
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        
        /* Recursively generate checksums, then convert list to an array */
        LinkedList<Checksum> list = RepositoryChecksums.generateChecksumForDirectory(
                root, root, digest, includes, excludes);
        RepositoryChecksums rc = new RepositoryChecksums();
        rc.setChecksums(list.toArray(new Checksum[] {}));
        return rc;
    }
    
    /**
     * Recursively generates checksums for the given directory. Returns a linked
     * list of all checksum objects beneath the given root directory.
     */
    private static LinkedList<Checksum> generateChecksumForDirectory(File root,
            File dir, MessageDigest digest, String[] includes, String[] excludes) {
 
        /* Put all of the checksums we generate in this linked list */
        LinkedList<Checksum> list = new LinkedList<Checksum>();

        /*
         * Loop through all of the files. If it is a directory, then recursively
         * descend into subdirectories. Before computing the checksum make sure
         * the file name satisfies the includes and excludes list.
         */
        File[] files = dir.listFiles();
        for (File file : files) {
            /* If a directory, then recursively descend and append */
            if (file.isDirectory() == true) {
                LinkedList<Checksum> rList = RepositoryChecksums.generateChecksumForDirectory(
                        root, file, digest, includes, excludes);
                list.addAll(rList);
            }
            else if (file.isFile() == true && file.isHidden() == false) {
                /*
                 * If a normal, non-hidden file, then check whether the name
                 * is included or excluded.
                 */
                if (RepositoryChecksums.isAcceptable(file.getName(), includes, excludes) == true) {
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
                        String name = file.getAbsolutePath().substring((int) (root.length() + 1));

                        /* Create a new checksum object and add to the list */
                        Checksum c = new Checksum();
                        c.setLastModified(file.lastModified());
                        c.setPathName(name);
                        c.setChecksum(Checksum.toHexString(byteChecksum));
                        list.add(c);
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
}
