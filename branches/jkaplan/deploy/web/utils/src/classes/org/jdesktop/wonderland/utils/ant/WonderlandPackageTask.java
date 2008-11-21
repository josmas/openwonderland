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
package org.jdesktop.wonderland.utils.ant;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.apache.tools.ant.util.FileUtils;

/**
 * Package the main Wonderland.jar
 * @author jkaplan
 */
public class WonderlandPackageTask extends Jar {
    private final List<ZipFileSet> checksums = new ArrayList<ZipFileSet>();
    private File checksumDir;
    private String checksumAlgorithm = "SHA-1";

    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();

    public void addChecksumFileset(ZipFileSet fileSet) {
        checksums.add(fileSet);
        addZipfileset(fileSet);
    }

    public void setChecksumDir(File checksumDir) {
        this.checksumDir = checksumDir;
    }

    public void setChecksumAlgorithm(String checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }

    @Override
    public void execute() throws BuildException {
        try {
            if (checksumDir == null) {
                // initialize a temp directory if we don't have one
                checksumDir = File.createTempFile("wonderlandpkg", "files");
                checksumDir.delete();
                checksumDir.mkdir();
                checksumDir.deleteOnExit();
            } else {
                checksumDir.mkdirs();
            }

            Map<String, List<String>> fileLists =
                    new HashMap<String, List<String>>();

            // go through each fileset and generate checksums if necessary
            for (ZipFileSet files : checksums) {
                String prefix = files.getPrefix(getProject());
                File fileSetDir = new File(checksumDir, prefix);
                fileSetDir.mkdir();

                // keep track of file names
                List<String> fileNames = fileLists.get(prefix);
                if (fileNames == null) {
                    fileNames = new ArrayList<String>();
                    fileLists.put(prefix, fileNames);
                }

                Iterator<FileResource> i = (Iterator<FileResource>) files.iterator();
                while (i.hasNext()) {
                    FileResource fr = i.next();
                    File f = fr.getFile();

                    String writeName = fr.getName().replace(File.separatorChar, '-');
                    writeName += ".checksum";
                    File checksumFile = new File(fileSetDir, writeName);

                    if (!checksumFile.exists() || outOfDate(checksumFile, f)) {
                        generateChecksum(fr, checksumFile);
                    }

                    String checksum = readChecksum(checksumFile);

                    fileNames.add(fr.getName() + " " + checksum);
                }
            }

            // write file lists
            for (Map.Entry<String, List<String>> e : fileLists.entrySet()) {
                File fileSetDir = new File(checksumDir, e.getKey());

                 // create a file with a list of file names
                writeFileList(fileSetDir, e.getValue());
            }

            // add the checksums directory to the jar
            ZipFileSet zfs = new ZipFileSet();
            zfs.setDir(checksumDir);
            FilenameSelector fs = new FilenameSelector();
            fs.setName("**/files.list");
            zfs.add(fs);
            zfs.setPrefix("META-INF");
            addFileset(zfs);
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        }

        super.execute();
    }

    protected boolean outOfDate(File checksums, File orig) {
        return checksums.lastModified() < orig.lastModified();
    }

    protected void generateChecksum(FileResource fr, File checksumFile)
            throws IOException
    {
        log("Generating checksum for " + fr.getName(), Project.MSG_INFO);

        MessageDigest digest;

        try {
            digest = MessageDigest.getInstance(checksumAlgorithm);
        } catch (NoSuchAlgorithmException nsae) {
            IOException ioe = new IOException("No such algorithm " +
                                              checksumAlgorithm);
            ioe.initCause(nsae);
            throw ioe;
        }

        byte[] buf = new byte[1024 * 1024];
        int bytesRead = 0;
        BufferedInputStream bis = new BufferedInputStream(fr.getInputStream());
        InputStream in = new DigestInputStream(bis, digest);

        /* Read in the entire file */
        do {
            bytesRead = in.read(buf);
        } while (bytesRead != -1);
        in.close();

        /* Compute the checksum with the digest */
        byte[] byteChecksum = digest.digest();
        digest.reset();

        String csStr = toHexString(byteChecksum);
        PrintWriter pr = new PrintWriter(new FileWriter(checksumFile));
        pr.println(csStr);
        pr.close();
    }

    protected String readChecksum(File checksumFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(checksumFile));
        return br.readLine();
    }

    protected void writeFileList(File dir, List<String> fileNames)
        throws IOException
    {
        File listFile = new File(dir, "files.list");
        if (listFile.exists()) {
            List<String> existing = readFileList(listFile);
            if (existing.equals(fileNames)) {
                return;
            }
        }

        PrintWriter pr = new PrintWriter(new FileWriter(listFile));
        for (String fileName : fileNames) {
            pr.println(fileName);
        }
        pr.close();
    }

    protected List<String> readFileList(File listFile) {
        List<String> out = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(listFile));
            String line;
            while ((line = br.readLine()) != null) {
                out.add(line);
            }
            br.close();
        } catch (IOException ioe) {
            // ignore, just pretend the list was empty
            out = Collections.emptyList();
        }

        return out;
    }

    /**
     * Converts the checksum given as an array of bytes into a hex-encoded
     * string.
     *
     * @param bytes The checksum as an array of bytes
     * @return The checksum as a hex-encoded string
     */
    private static String toHexString(byte bytes[]) {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < bytes.length; ++i) {
            ret.append(Integer.toHexString(0x0100 + (bytes[i] & 0x00FF)).substring(1));
        }
        return ret.toString();
    }
}
