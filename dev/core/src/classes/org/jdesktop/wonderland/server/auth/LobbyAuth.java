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
package org.jdesktop.wonderland.server.auth;

import com.sun.sgs.auth.Identity;
import com.sun.sgs.auth.IdentityAuthenticator;
import com.sun.sgs.auth.IdentityCredentials;
import com.sun.sgs.impl.auth.NamePasswordCredentials;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.logging.Logger;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * An authenticator that uses the Wonderland Lobby web application to get a 
 * person's information.  This authenticator relies on the following properties 
 * being set in the Darkstar configuration file:
 * <ul>
 * <li><code>org.jdesktop.lg3d.wonderland.darkstar.server.auth.lobby.baseurl</code>
 *     The lobby URL to connect to, for example https://localhost:8080/WonderlandLobby
 * </li>
 * </ul>
 * @author jkaplan
 */
public class LobbyAuth implements IdentityAuthenticator {
    // logger
    private static final Logger logger =
            Logger.getLogger(LobbyAuth.class.getName());
   
    // properties to use
    private static final String PROP_BASE = 
            LobbyAuth.class.getPackage().getName() + ".lobby.";
    private static final String PROP_URL = PROP_BASE + "baseurl";
    
    // properties values
    private URL baseURL;
    
    public LobbyAuth(Properties prop) {
        // required properties
        String baseURLStr = prop.getProperty(PROP_URL);
        if (baseURLStr == null) {
            throw new RuntimeException(PROP_URL + " required");
        }
        
        try {
            baseURL = new URL(baseURLStr);
        } catch (MalformedURLException mue) {
            throw new RuntimeException("Invalid base url: " + baseURLStr, mue);
        }
        
        logger.info("Loading Lobby authenticator");
    }

    public String[] getSupportedCredentialTypes() {
        return new String[] { "NameAndPasswordCredentials" };
    }
    
    public Identity authenticateIdentity(IdentityCredentials credentials) 
        throws LoginException 
    {
        if (!(credentials instanceof NamePasswordCredentials)) {
            throw new CredentialException("Wrong credential class: " +
                    credentials.getClass().getName());
        }
        
        NamePasswordCredentials npc = (NamePasswordCredentials) credentials;
        
        // make sure the name is specified and the password is not zero-length
        if (npc.getName() == null || npc.getPassword().length == 0) {
            throw new CredentialException("Invalid username or password.");
        }
         
        try {
            URL u = new URL(baseURL, "authenticate");
            HttpURLConnection huc = (HttpURLConnection) u.openConnection(); 
        
            huc.setRequestMethod("POST");
            huc.setDoOutput(true);
            huc.connect();
            
            // write the post contents
            String username = URLEncoder.encode(npc.getName(), "UTF-8");
            String password = URLEncoder.encode(String.valueOf(npc.getPassword()), 
                                                "UTF-8");
            PrintWriter out = new PrintWriter(huc.getOutputStream());
            out.print("username=" + username + "&");
            out.print("password=" + password);
            out.close();
            
            // create a DOM parse
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            
            // parse the identity from the response
            WonderlandIdentity id = getIdentity(db.parse(huc.getInputStream()));
            return id;
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to connect to " + baseURL,
                                       ioe);
        } catch (SAXException se) {
            throw new RuntimeException("Unable to parse result", se);
        } catch (ParserConfigurationException pce) {
            throw new RuntimeException("Error setting up parser", pce);
        }
    }
    
    /**
     * Get an identity from a DocumentBuilder -- this method parses
     * the DOM tree to find information about the person.  It returns
     * an identity if there is one contained in the given tree,
     * or returns null if there is no identity
     * @param doc the document to parse
     * @return the user contained in the given document, or null
     * if no user is contained in the document
     */
    public WonderlandIdentity getIdentity(Document doc)
        throws LoginException
    {
        System.out.println("Got document: " + doc.getChildNodes().getLength());
        
        String res = doc.getFirstChild().getNodeName();
        if (res.equalsIgnoreCase("error")) {
            Node error = doc.getFirstChild();
            String reason = error.getFirstChild().getNodeValue();
            
            logger.info("Login error: " + reason);
            throw new CredentialException(reason);
        }
        
        Element ok = (Element) doc.getFirstChild();
        Element userinfo = getChild(ok, "userinfo");
        
        String username = getChildValue(userinfo, "username");
        String fullname = getChildValue(userinfo, "fullname");
        String email = getChildValue(userinfo, "email");
        
        return new WonderlandIdentity(username, fullname, email);
    }
    
    /**
     * Find the first child of a given element with the given name
     * @param parent the parent element
     * @param name the child tag name to find
     * @return the first child with the given name, or null if no child
     * has that name
     */
    private Element getChild(Element parent, String name) {
        NodeList nl = parent.getElementsByTagName(name);
        if (nl.getLength() == 0) {
            return null;
        }
        
        return (Element) nl.item(0);
    }
    
    /**
     * Get the value of a child node
     * @param parent the parent element
     * @param name the name of the child
     * @return the value of the child node's first child, or null
     * if no such node exists
     */
    private String getChildValue(Element parent, String name) {
        Element e = getChild(parent, name);
        if (e == null || e.getFirstChild() == null) {
            return null;
        }
        
        return e.getFirstChild().getNodeValue();
    }
}
