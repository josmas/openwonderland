/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ant;

// IMPORTANT! You need to compile this class against ant.jar.
// The easiest way to do this is to add ${ant.core.lib} to your project's classpath.
// For example, for a plain Java project with no other dependencies, set in project.properties:
// javac.classpath=${ant.core.lib}

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.ZipFileSet;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModuleInfo;
import org.jdesktop.wonderland.modules.ModuleRequires;
/**
 * @author jon
 */
public class ModuleTask extends Jar {
    
    // attributed
    private String name;
    private int majorVersion = ModuleInfo.VERSION_UNSET;
    private int minorVersion = ModuleInfo.VERSION_UNSET;
    private List<Requires> requires = new ArrayList<Requires>();
    private List<Plugin> plugins = new ArrayList<Plugin>();
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }
    
    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }
    
    public Requires createRequires() {
        Requires r = new Requires();
        requires.add(r);
        return r;
    }
    
    public void addArt(ZipFileSet art) {
        // set the art prefix and pass it up
        art.setPrefix("art/");
        super.addFileset(art);
    }
    
    public void addWFS(ZipFileSet wfs) {
        // set the wfs prefix and pass it up
        wfs.setPrefix("wfs/");
        super.addFileset(wfs);
    }
    
    public void addPlugin(Plugin p) {
        plugins.add(p);
    }
    
    @Override
    public void execute() throws BuildException {
        // first, check all the inputs to make sure they seem valid
        
        if (name == null) {
            throw new BuildException("Name is required.");
        }
        
        if (majorVersion == ModuleInfo.VERSION_UNSET) {
            throw new BuildException("Major version is required.");
        }
        
        for (Requires r : requires) {
            if (r.name == null) {
                throw new BuildException("Requires without name.");
            }
            
            if (r.majorVersion == ModuleInfo.VERSION_UNSET) {
                throw new BuildException("Requires without major version.");
            }
        }
        
        for (Plugin p : plugins) {
            if (p.name == null) {
                throw new BuildException("Plugin without name.");
            }
        }
        
        // now write the relevant xml files, by creating temp files
        // and adding those temp files to parent .jar
        try {
            // first write the module info
            writeModuleInfo();
            
            // next write required files
            writeRequires();
            
            // write plugins
            for (Plugin p : plugins) {
                writePlugin(p);
            }
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        } catch (JAXBException je) {
            throw new BuildException(je);
        }
        
        // TODO calculate checksums?        
        super.execute();
    }
    
    private void writeModuleInfo() throws IOException, JAXBException {
        ModuleInfo mi = new ModuleInfo(name, majorVersion, minorVersion);
        
        File moduleInfoFile = File.createTempFile("moduleInfo", "xml");
        moduleInfoFile.deleteOnExit();
        mi.encode(new FileWriter(moduleInfoFile));
            
        ZipFileSet zfs = new ZipFileSet();
        zfs.setFile(moduleInfoFile);
        zfs.setFullpath(Module.MODULE_INFO);
        
        super.addFileset(zfs);
    }
    
    private void writeRequires() throws IOException, JAXBException {
        Set<ModuleInfo> mis = new HashSet<ModuleInfo>();
        for (Requires r : requires) {
            mis.add(new ModuleInfo(r.name, r.majorVersion, r.minorVersion));
        }
        
        ModuleRequires mr = new ModuleRequires(mis.toArray(new ModuleInfo[0]));
        
        File moduleRequiresFile = File.createTempFile("moduleRequires", "xml");
        moduleRequiresFile.deleteOnExit();
        mr.encode(new FileOutputStream(moduleRequiresFile));
            
        ZipFileSet zfs = new ZipFileSet();
        zfs.setFile(moduleRequiresFile);
        zfs.setFullpath(Module.MODULE_REQUIRES);
        
        super.addFileset(zfs);
    }
    
    private void writePlugin(Plugin p) {
        if (p.clientJars != null) {
            p.clientJars.setPrefix(Module.MODULE_PLUGINS + "/" + p.name + "/");
            super.addFileset(p.clientJars);
        }
        
        if (p.commonJars != null) {
            p.commonJars.setPrefix(Module.MODULE_PLUGINS + "/" + p.name + "/");
            super.addFileset(p.commonJars);
        }
        
        if (p.serverJars != null) {
            p.serverJars.setPrefix(Module.MODULE_PLUGINS + "/" + p.name + "/");
            super.addFileset(p.serverJars);
        }
    }
    
    public static class Requires {
        private String name;
        private int majorVersion = ModuleInfo.VERSION_UNSET;
        private int minorVersion = ModuleInfo.VERSION_UNSET;
        
        public void setName(String name) {
            this.name = name;
        }
    
        public void setMajorVersion(int majorVersion) {
            this.majorVersion = majorVersion;
        }
    
        public void setMinorVersion(int minorVersion) {
            this.minorVersion = minorVersion;
        }
    }
    
    public static class Plugin {
        private String name;
        private ZipFileSet clientJars;
        private ZipFileSet commonJars;
        private ZipFileSet serverJars;
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void addClient(ZipFileSet clientJars) {
            this.clientJars = clientJars;
        }
        
        public void addCommon(ZipFileSet commonJars) {
            this.commonJars = commonJars;
        }
        
        public void addServer(ZipFileSet serverJars) {
            this.serverJars = serverJars;
        }
    }
}
