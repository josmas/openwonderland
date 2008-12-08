/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.webstart.ant;

// IMPORTANT! You need to compile this class against ant.jar.
// The easiest way to do this is to add ${ant.core.lib} to your project's classpath.
// For example, for a plain Java project with no other dependencies, set in project.properties:
// javac.classpath=${ant.core.lib}

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author jkaplan
 */
public class FileDateTask extends Task {
    private File file;
    private String prop;
    
    public void setFile(File file) {
        this.file = file;
    }
 
    public void setProp(String prop) {
        this.prop = prop;
    }
    
    @Override
    public void execute() throws BuildException {
        if (file == null) {
            throw new BuildException("File required");
        }
        if (prop == null) {
            throw new BuildException("Prop required");
        }
        
        getProject().setProperty(prop, String.valueOf(file.lastModified()));
    }
}
