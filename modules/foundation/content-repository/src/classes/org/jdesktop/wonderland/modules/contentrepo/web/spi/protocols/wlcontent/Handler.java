/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.contentrepo.web.spi.protocols.wlcontent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.ProtocolUtils;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.WebContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.WebContentRepositoryRegistry;

/**
 * Point wlcontent:// URLs at the local content repository.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class Handler extends URLStreamHandler {
    private static final Logger LOGGER =
            Logger.getLogger(Handler.class.getName());
    
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        ServletContext context = ProtocolUtils.getContext();
        if (context == null) {
            throw new IOException("No current context opening " + 
                                  url.toExternalForm());
        }
                
        try {
            WebContentRepository repo = 
                    WebContentRepositoryRegistry.getInstance().getRepository(context);
            ContentCollection root = repo.getRoot();
            ContentResource obj = (ContentResource) root.getChild(url.getHost() + "/" + url.getPath());
            
            return new WlContentURLConnection(url, obj);
            
        } catch (ContentRepositoryException ce) {
            throw new IOException(ce);
        }
    }
    
    class WlContentURLConnection extends URLConnection {
        private final ContentResource resource;
        
        WlContentURLConnection(URL url, ContentResource resource) {
            super(url);
            
            this.resource = resource;
        }
        
        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            try {
                return resource.getInputStream();
            } catch (ContentRepositoryException ex) {
                throw new IOException(ex);
            }
        }
    }
}
