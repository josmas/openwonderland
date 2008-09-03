/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.webserver.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 *
 * @author jkaplan
 */
public class ListFilesTask extends Task {
    private String jar;
    private String dir;
    private String output;
    
    @Override
    public void execute() throws BuildException {
        File jarFile = new File(jar);
        if (!jarFile.exists()) {
            throw new BuildException("No such jar file: " + jar);
        }
        
        if (dir == null) {
            dir = "/";
        }
        
        if (output == null) {
            output = "listfile.out";
        }
        
        // open the jar file and output file
        try {
            JarInputStream in = new JarInputStream(new FileInputStream(jarFile));
            PrintWriter out = new PrintWriter(new FileWriter(output));
            
            Pattern p = Pattern.compile("^" + dir + "/(.+)");
            
            JarEntry je;
            while ((je = in.getNextJarEntry()) != null) {
                Matcher m = p.matcher(je.getName());
                if (m.matches() && m.groupCount() == 1) {
                    out.println("/" + m.group());
                }
            }
            
            out.close();
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        }
        
    }
    
    public void setJar(String jar) {
        this.jar = jar;
    }
    
    public void setDir(String dir) {
        this.dir = dir;
    }
    
    public void setOutput(String output) {
        this.output = output;
    }
    
}
