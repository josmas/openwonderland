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
package org.jdesktop.wonderland.runner.ant;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author jkaplan
 */
public class RunnerTask extends Task {
    private URL serverUrl;
    private String action = "restart";
    private boolean wait = true;
    private String service = "all";
    
    private Set<Service> services = new HashSet<Service>();
    
    // url for restarting the server
    private static final String SERVICES_URL =
            "wonderland-web-runner/services/runner";
 
    public void setServerUrl(URL serverUrl) {
        this.serverUrl = serverUrl;
    }
 
    public void setAction(String action) {
        this.action = action;
    }
    
    public void setWait(boolean wait) {
        this.wait = wait;
    }
    
    public void setService(String service) {
        this.service = service;
    }
    
    public void add(Service service) {
        if (service.name == null || service.name.trim().length() == 0) {
            throw new BuildException("service requires a name");
        }
        services.add(service);
    }
    
    @Override
    public void execute() throws BuildException {
        if (serverUrl == null) {
            throw new BuildException("serverUrl required");
        }
        
        try {
            if (!services.isEmpty()) {
                for (Service s : services) {
                    execute(s.name, action);
                }
            } else {
                execute(service, action);
            }
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        }
    } 
    
    protected void execute(String service, String action)
        throws IOException
    {
        URL restartUrl = new URL(serverUrl, SERVICES_URL + "/" +
                                 service + "/" + action);
        HttpURLConnection uc = (HttpURLConnection) restartUrl.openConnection();
        uc.connect();
                
        int response = uc.getResponseCode();
        if (response != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error " + response + " restarting server: " + 
                                  uc.getResponseMessage());
        }
    }
    
    public static class Service {
        private String name;
    
        public void setName(String name) {
            this.name = name;
        }
    }
}
