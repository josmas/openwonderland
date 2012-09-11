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
package org.jdesktop.wonderland.modules.errorreport.web.resources;

import java.io.ByteArrayOutputStream;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.WebContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.web.spi.WebContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.errorreport.common.ErrorReport;

/**
 * Resource for storing error logs
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@Path("/errorReports")
public class ErrorReportResource {
    private static final Logger LOGGER = 
            Logger.getLogger(ErrorReportResource.class.getName());
    private static JAXBContext context;
    
    private final WebContentRepository repo;
    private ContentCollection dir;
    
    public ErrorReportResource(@Context ServletContext context) {
        WebContentRepositoryRegistry reg = WebContentRepositoryRegistry.getInstance();
        repo = reg.getRepository(context);
    
        // create directory if it doesn't exist
        try {
            dir = (ContentCollection)
                    repo.getRoot().getChild("groups/users/" + ErrorReport.DIR_NAME);
            if (dir == null) {
                throw new WebApplicationException(
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity("Report directory not found").build());
            }
        } catch (ContentRepositoryException ce) {
            throw new WebApplicationException(ce, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response list(@QueryParam("content") String content) {
        boolean includeContent = false;
        if (content != null) {
            includeContent = Boolean.parseBoolean(content);
        }
        
        try {
            List<ErrorReport> reports = new ArrayList<ErrorReport>();
            
            for (ContentNode node : dir.getChildren()) {
                if (node instanceof ContentResource) {
                    try {
                        ErrorReport report = read((ContentResource) node);
                        if (!includeContent) {
                            report.setContent(null);
                        }
                    
                        reports.add(report);
                    } catch (JAXBException je) {
                        LOGGER.log(Level.WARNING, "Error reading " + node.getName(),
                                   je);
                    }
                }
            }
            
            return Response.ok(new ErrorReportList(reports)).build();
        } catch (ContentRepositoryException ce) {
            throw new WebApplicationException(ce, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    @POST
    @Path("create")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response create(ErrorReport report) {
        try {
            String fileName;
            int index = 0;
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            do {
                fileName = report.getCreator() + "-" + df.format(report.getTimeStamp());
                fileName += (index == 0)?"":index;
            
                index++;
            } while (dir.getChild(fileName) != null);
            
            report.setId(fileName);
            write(report);
            
            return Response.ok(report).build();    
        } catch (ContentRepositoryException ce) {
            throw new WebApplicationException(ce, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (JAXBException je) {
            throw new WebApplicationException(je, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GET
    @Path("get/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response get(@PathParam("id") String id) {
        try {
            ContentNode node = dir.getChild(id);
            if (node == null || !(node instanceof ContentResource)) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            
            final ErrorReport log = read((ContentResource) node);
            if (log == null) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("No object with id " + id).build();
            }
            
            return Response.ok(log).build();    
        } catch (ContentRepositoryException ce) {
            throw new WebApplicationException(ce, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (JAXBException je) {
            throw new WebApplicationException(je, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GET
    @Path("download/{id}")
    @Produces({"text/plain"})
    public Response download(@PathParam("id") String id,
                             @QueryParam("attachment") String attachment) 
    {
        boolean asAttachment = true;
        if (attachment != null) {
            asAttachment = Boolean.parseBoolean("attachment");
        }
        
        try {
            ContentNode node = dir.getChild(id);
            if (node == null || !(node instanceof ContentResource)) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            
            final ErrorReport log = read((ContentResource) node);
            if (log == null) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("No object with id " + id).build();
            }
            
            ResponseBuilder out = Response.ok(new StreamingOutput() {
                public void write(OutputStream out) throws IOException, WebApplicationException {
                    PrintStream ps = new PrintStream(out);
                    
                    ps.println("User: " + log.getCreator());
                    ps.println("Submitted: " + 
                            SimpleDateFormat.getDateTimeInstance().format(log.getTimeStamp()));
                    ps.println("Comments:");
                    ps.println(log.getComments());
                    ps.println("---------- Error Report ----------");
                    ps.println(log.getContent());
                }
            });

            if (asAttachment) {
                out.header("Content-Disposition",
                           "attachment; filename=" + log.getId() + ".txt");
            }
             
            return out.build();        
        } catch (ContentRepositoryException ce) {
            throw new WebApplicationException(ce, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (JAXBException je) {
            throw new WebApplicationException(je, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GET
    @Path("delete/{id}")
    public Response download(@PathParam("id") String id) 
    {
        try {
            ContentNode node = dir.getChild(id);
            if (node == null || !(node instanceof ContentResource)) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            
            dir.removeChild(id);
            return Response.ok().build();
        } catch (ContentRepositoryException ce) {
            throw new WebApplicationException(ce, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    private ErrorReport read(ContentResource resource) 
            throws JAXBException, ContentRepositoryException 
    {
        Unmarshaller unmarshaller = getContext().createUnmarshaller();
        
        // reports may contain illegal characters. Add a filter to
        // ignore all these characters.
        Reader in = new EscapeBadCharsReader(new InputStreamReader(resource.getInputStream()));
        ErrorReport report = (ErrorReport) unmarshaller.unmarshal(in);
        report.setId(resource.getName());
        return report;
    }
    
    private void write(ErrorReport report) 
            throws JAXBException, ContentRepositoryException 
    {
        Marshaller marshaller = getContext().createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        ContentResource resource = (ContentResource) dir.getChild(report.getId());
        if (resource == null) {
            resource = (ContentResource) dir.createChild(report.getId(), 
                                                         ContentNode.Type.RESOURCE);
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(report, baos);
        resource.put(baos.toByteArray());
    }
    
    private synchronized static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(ErrorReport.class);
        }
        
        return context;
    }
    
    @XmlRootElement(name="client-test-log-list")
    public static class ErrorReportList {
        private final List<ErrorReport> logs = new ArrayList<ErrorReport>();
        
        public ErrorReportList() {
        }
        
        public ErrorReportList(List<ErrorReport> logs) {
            this.logs.addAll(logs);
        }
        
        @XmlElement
        public List<ErrorReport> getLogs() {
            return logs;
        }
    }
    
    static class EscapeBadCharsReader extends FilterReader {
        public EscapeBadCharsReader(Reader r) {
            super (r);
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            int res = super.read(cbuf, off, len);
            for (int i = off; i < len; i++) {
                int idx = off + i;
                cbuf[idx] = escape(cbuf[idx]);
            }
            
            return res;
        }
        
        private char escape(char c) {
            if((c == 0x9)
                || (c == 0xA) 
                || (c == 0xD) 
                || ((c >= 0x20) && (c <= 0xD7FF)) 
                || ((c >= 0xE000) && (c <= 0xFFFD)) 
                || ((c >= 0x10000) && (c <= 0x10FFFF)))
            {
                // valid
                return c;
            } else {
                return '.';
            }
        }
    }
}
