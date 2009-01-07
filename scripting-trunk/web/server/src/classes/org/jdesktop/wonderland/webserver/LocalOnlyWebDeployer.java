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
package org.jdesktop.wonderland.webserver;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.io.WebDeploymentDescriptorFile;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import org.glassfish.embed.impl.EmbeddedWebDeployer;

/**
 * Workaround for WebDeployer to disable resolving external DTDs.  This
 * makes allows the server to work when not connected to the internet.
 * @author jkaplan
 */
public class LocalOnlyWebDeployer extends EmbeddedWebDeployer {
    private static final Logger logger =
            Logger.getLogger(LocalOnlyWebDeployer.class.getName());
    
    private WebBundleDescriptor defaultWebXMLWbd;
    
    /**
     * XXX THIS CODE IS COPIED FROM WebDeployer.java IN THE GLASSFISH SOURCE.
     *     IT NEEDS TO BE KEPT UP-TO-DATE WITH ANY CHANGES IN GLASSFISH.
     *     WE SHOULD SUGGEST A WORKAROUND IN THEIR SOURCE SO WE CAN DO
     *     THIS CLEANLY. XXX
     */
    @Override
    public WebBundleDescriptor getDefaultWebXMLBundleDescriptor() {
        initDefaultWebXMLBundleDescriptor();

        // when default-web.xml exists, add the default bundle descriptor
        // as the base web bundle descriptor
        WebBundleDescriptor defaultWebBundleDesc =
            new WebBundleDescriptor();
        if (defaultWebXMLWbd != null) {
            defaultWebBundleDesc.addWebBundleDescriptor(defaultWebXMLWbd);
        }
        return defaultWebBundleDesc;
    }


    /**
     * initialize the default WebBundleDescriptor from
     * default-web.xml
     */
    private synchronized void initDefaultWebXMLBundleDescriptor() {

        if (defaultWebXMLWbd != null) {
            return;
        }

        InputStream fis = null;

        try {
            // parse default-web.xml contents
            URL defaultWebXml = getDefaultWebXML();
            if (defaultWebXml!=null)  {
                fis = defaultWebXml.openStream();
                WebDeploymentDescriptorFile wddf = new LocalOnlyWebDDFile();
                wddf.setXMLValidation(false);
                defaultWebXMLWbd = wddf.read(fis);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error reading default web.xml", e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                     }
            } catch (IOException ioe) {
                // do nothing
            }
        }
    }

    class LocalOnlyWebDDFile extends WebDeploymentDescriptorFile {
        public SAXParser getSAXParser(boolean validating) {
            SAXParser sp = super.getSAXParser(validating);
            if (validating) {
                return sp;
            } else {
                logger.info("Turning off external DTD validation");
                return LocalOnlyWebArchivist.turnOffValidation(sp);
            }
        }
    }
}
