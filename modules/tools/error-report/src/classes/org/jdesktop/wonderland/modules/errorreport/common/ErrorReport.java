/**
 * Open Wonderland
 *
 * Copyright (c) 2011 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.errorreport.common;

import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Log of an error report
 */
@XmlRootElement(name="error-report")
public class ErrorReport {
    public static final String DIR_NAME = "errorReports";
    
    private String id;
    private String creator;
    private Date timeStamp;
    private String content;
    private String comments;
    
    public ErrorReport() {
        this (null, null, null, null);
    }
    
    public ErrorReport(String creator, Date timeStamp, String content,
                       String comments) {
        this (null, creator, timeStamp, content, comments);
    }
    
    public ErrorReport(String id, String creator, Date timeStamp,
                       String content, String comments) 
    {
        this.id = id;
        this.creator = creator;
        this.timeStamp = timeStamp;
        this.content = content;
        this.comments = comments;
    }
    
    @XmlElement
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    @XmlElement
    public String getCreator() {
        return creator;
    }
    
    public void setCreator(String creator) {
        this.creator = creator;
    }
    
    @XmlElement
    public Date getTimeStamp() {
        return timeStamp;
    }
    
    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    @XmlElement
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    @XmlElement
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
}
