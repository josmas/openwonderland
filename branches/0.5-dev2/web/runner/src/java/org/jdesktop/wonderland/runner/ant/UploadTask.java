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

package org.jdesktop.wonderland.runner.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;
import org.jdesktop.wonderland.common.modules.MultiPartFormOutputStream;

/**
 * @author jkaplan
 */
public class UploadTask extends Task {
    private URL serverUrl;
    private List<FileSet> zips = new ArrayList<FileSet>();
   
    // url for restarting the server
    private static final String UPLOAD_URL =
            "wonderland-web-runner/upload";
 
    public void setServerUrl(URL serverUrl) {
        this.serverUrl = serverUrl;
    }
 
    public void addConfiguredFileSet(FileSet fileSet) {
        zips.add(fileSet);
    }
   
    @Override
    public void execute() throws BuildException {
        if (serverUrl == null) {
            throw new BuildException("serverUrl required");
        }
        
        // get the list fo files
        List<File> files = new ArrayList<File>();
        for (FileSet fs : zips) {
            Iterator<FileResource> rsrcs = (Iterator<FileResource>) fs.iterator();
            while (rsrcs.hasNext()) {
                files.add(rsrcs.next().getFile());
            }
        }

        // upload the files
        try {
            upload(files);
        } catch (IOException ioe) {
            throw new BuildException("Error uploading files", ioe);
        }
    }

    protected URL getUploadURL() throws MalformedURLException {
        return new URL(serverUrl, UPLOAD_URL);
    }

    protected void upload(List<File> files) throws IOException {
        // create the connection to write data to the form
        String boundary = MultiPartFormOutputStream.createBoundary();
        URLConnection uc = MultiPartFormOutputStream.createConnection(getUploadURL());
        if (!(uc instanceof HttpURLConnection)) {
            throw new IllegalStateException("Http URL required: " + getUploadURL());
        }
        HttpURLConnection conn = (HttpURLConnection) uc;
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Content-Type",
                MultiPartFormOutputStream.getContentType(boundary));
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Cache-Control", "no-cache");

        MultiPartFormOutputStream up =
                new MultiPartFormOutputStream(conn.getOutputStream(), boundary);

        // write files
        for (File file : files) {
            up.writeFile(file.getName(), "application/zip", file);
        }

        up.close();

        // read response
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            // print error in console
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                resp.append(line + "\n");
            }

            throw new IOException("Bad server response: " + responseCode +
                                  "\n" + resp.toString());
        }
    }
}
