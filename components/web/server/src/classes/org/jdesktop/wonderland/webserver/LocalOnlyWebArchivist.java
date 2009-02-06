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
package org.jdesktop.wonderland.webserver;

import com.sun.enterprise.deployment.archivist.WebArchivist;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.runtime.WebRuntimeDDFile;
import com.sun.enterprise.deployment.util.DOLUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;

/**
 * Workaround to prevent loading external DTDs during webapp deployment.
 * @author jkaplan
 */
public class LocalOnlyWebArchivist extends WebArchivist {

    private static final Logger logger =
            Logger.getLogger(LocalOnlyWebArchivist.class.getName());

    @Override
    public DeploymentDescriptorFile getConfigurationDDFile() {
        return new LocalOnlyDDFile();
    }

    /**
     * Turn off validation on the given parser.  Return the parser
     * on success, or null on failure
     * @param sp the parser to turn off validation on
     * @return the parser on success, or null on an error
     */
    public static SAXParser turnOffValidation(SAXParser sp) {
        try {
            sp.getXMLReader().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
                                         false);
            return sp;
        } catch (Exception e) {
            e.printStackTrace();
            DOLUtils.getDefaultLogger().log(Level.SEVERE,
                    "enterprise.deployment.backend.saxParserError",
                    new Object[]{e.getMessage()});
        }

        return null;
    }

    class LocalOnlyDDFile extends WebRuntimeDDFile {
        @Override
        public SAXParser getSAXParser(boolean validating) {
            SAXParser sp = super.getSAXParser(validating);
            if (validating) {
                return sp;
            } else {
                logger.info("Turning off external DTD loading");
                return turnOffValidation(sp);
            }
        }
    }
}
