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

package org.jdesktop.wonderland.modules.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.spi.Service;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.zip.ZipOutputStream;
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
    private String moduleDescription;
    
    private List<Requires> requires = new ArrayList<Requires>();
    private List<Plugin> plugins = new ArrayList<Plugin>();
   
    private File buildDir;
    private boolean overwrite = false;
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }
    
    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }
    
    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }
    
    public void setModuleDescription(String moduleDescription) {
        this.moduleDescription = moduleDescription;
    }
    
    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
    }
    
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
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
        // make sure there are no obvious errors before we write anything
        validate();
        
        // remember the context classloader
        ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        
        // now write the relevant xml files, by creating temp files
        // and adding those temp files to parent .jar
        try {
            
            // workaround for JAXB issue.  The JAXB ContextFinder uses the
            // context classloader to load the correct JAXBContext instance.
            // Make sure the context classloader is the one that loaded
            // this task (which has the JAXB classpath).  Otherwsise,
            // the default ant classloader will be used, which doesn't
            // have the JAXB classes.  Also make sure to set the
            // context classloader back after this try block.            
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            
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
        } finally {
            // reset the classloader
            Thread.currentThread().setContextClassLoader(contextCL);
        }
        
        // TODO calculate checksums?        
        super.execute();
    }
    
    private void writeModuleInfo() throws IOException, JAXBException {
        ModuleInfo mi = new ModuleInfo(name, majorVersion, minorVersion, moduleDescription);
        
        File moduleInfoFile;
        if (buildDir == null) {
            moduleInfoFile = File.createTempFile("moduleInfo", "xml");
            moduleInfoFile.deleteOnExit();
        } else {
            moduleInfoFile = new File(buildDir, "moduleInfo.xml");
        }
        
        if (overwrite || !compareModuleInfo(mi, moduleInfoFile)) {
            log("Rewriting moduleInfo file", Project.MSG_VERBOSE);
            mi.encode(new FileWriter(moduleInfoFile));
        }
        
        ZipFileSet zfs = new ZipFileSet();
        zfs.setFile(moduleInfoFile);
        zfs.setFullpath(Module.MODULE_INFO);
        
        super.addFileset(zfs);
    }
    
    /**
     * Return if the given new ModuleInfo object is the same as the 
     * module info contained in the file oldMIFile.  Returns false if
     * the old file doesn't exist.  Note this relies on more than just the 
     * equals() method of the ModuleInfo object -- it also compares the 
     * description.
     * 
     * @param newMI the new module info object
     * @param oldMIFile the file containing the old module info object
     * @return true if the files are the same, or false if they are different
     * @throws IOException if there is a problem reading the file
     */
    private boolean compareModuleInfo(ModuleInfo newMI, File oldMIFile) 
        throws IOException
    {
        log("Comparing module info " + oldMIFile.getCanonicalPath() + 
            " exists: " + oldMIFile.exists(), Project.MSG_VERBOSE);
        
        if (!oldMIFile.exists()) {
            return false;
        }
        
        try {
            ModuleInfo oldMI = ModuleInfo.decode(new FileReader(oldMIFile));
            
            log("New desc:|" + newMI.getDescription() + "|Old desc:|" + oldMI.getDescription() + "|", Project.MSG_VERBOSE);
            
            // ModuleInfo.equals() doesn't check the description field, 
            // but we want to re-write the file if the description has
            // changed.
            boolean descChanged = (newMI.getDescription() == null) ?
                (oldMI.getDescription() != null) :
                (!newMI.getDescription().equals(oldMI.getDescription()));
            
            log("ModuleInfo: descChanged: " + descChanged + " " +
                "equals: " + newMI.equals(oldMI), Project.MSG_VERBOSE);
            
            return (!descChanged && newMI.equals(oldMI));
        } catch (JAXBException je) {
            // problem reading file
        }
        
        return false;
    }
    
    private void writeRequires() throws IOException, JAXBException {
        Set<ModuleInfo> mis = new HashSet<ModuleInfo>();
        for (Requires r : requires) {
            mis.add(new ModuleInfo(r.name, r.majorVersion, r.minorVersion));
        }
        
        ModuleRequires mr = new ModuleRequires(mis.toArray(new ModuleInfo[0]));
        
        File moduleRequiresFile;
        if (buildDir == null) {
            moduleRequiresFile = File.createTempFile("moduleRequires", ".xml");
            moduleRequiresFile.deleteOnExit();
        } else {
            moduleRequiresFile = new File(buildDir, "moduleRequires.xml");
        }
        
        if (overwrite || !compareModuleRequires(mr, moduleRequiresFile)) {
            log("Rewriting moduleRequires file", Project.MSG_VERBOSE);
            mr.encode(new FileOutputStream(moduleRequiresFile));
        }
        
        ZipFileSet zfs = new ZipFileSet();
        zfs.setFile(moduleRequiresFile);
        zfs.setFullpath(Module.MODULE_REQUIRES);
        
        super.addFileset(zfs);
    }
    
    /**
     * Return if the given new ModuleRequires object is the same as the 
     * module requires contained in the file oldMRFile.  Returns false if
     * the old file doesn't exist. This task compares the ModuleInfo[] objects
     * in each requires file.
     * 
     * @param newMR the new module requires object
     * @param oldMRFile the file containing the old module requires object
     * @return true if the files are the same, or false if they are different
     * @throws IOException if there is a problem reading the file
     */
    private boolean compareModuleRequires(ModuleRequires newMR, File oldMRFile) 
        throws IOException
    {
        if (!oldMRFile.exists()) {
            return false;
        }
        
        try {
            ModuleRequires oldMR = ModuleRequires.decode(new FileReader(oldMRFile));
           
            return Arrays.deepEquals(newMR.getRequires(), oldMR.getRequires());
        } catch (JAXBException je) {
            // problem reading file
        }
        
        return false;
    }
    
    private void writePlugin(Plugin p) throws IOException {
        if (p.clientJar != null) {
            writePluginJar(p.name, p.clientJar, "client");
        }
        if (p.commonJar != null) {
            writePluginJar(p.name, p.commonJar, "common");
        }
        if (p.serverJar != null) {
            writePluginJar(p.name, p.serverJar, "server");
        }
        
        for (ExtraJar e : p.extraClientJars) {
            writePluginExtraJar(p.name, e, "client");
        }
        
        for (ExtraJar e : p.extraCommonJars) {
            writePluginExtraJar(p.name, e, "common");
        }
        
        for (ExtraJar e : p.extraServerJars) {
            writePluginExtraJar(p.name, e, "server");
        }
    }
    
    private void writePluginJar(String pluginName, PluginJar pluginJar, 
                                String pluginDir)
        throws IOException
    {
        String pluginJarName = pluginName + "-" + pluginDir + ".jar";
        
        File pluginFile;
        if (buildDir == null) {
            pluginFile = File.createTempFile("plugin", ".jar");
            pluginFile.delete();
            pluginFile.deleteOnExit();
        } else {
            File pluginBuildDir = new File(buildDir, pluginName);
            File pluginInstDir = new File(pluginBuildDir, pluginDir);
            pluginInstDir.mkdirs();
            
            pluginFile = new File(pluginInstDir, pluginJarName);
        }
        
        pluginJar.setInternalDestFile(pluginFile);
        pluginJar.execute();
            
        ZipFileSet zfs = new ZipFileSet();
        zfs.setFile(pluginFile);
        zfs.setFullpath(Module.MODULE_PLUGINS + "/" + pluginName + "/" + 
                        pluginDir + "/" + pluginJarName);
        
        super.addFileset(zfs);
    }
    
    private void writePluginExtraJar(String pluginName, ExtraJar e, 
                                     String pluginDir)
        throws IOException
    {
        ZipFileSet zfs = new ZipFileSet();
        zfs.setFile(e.jarFile);
        zfs.setFullpath(Module.MODULE_PLUGINS + "/" + pluginName + "/" + 
                        pluginDir + "/" + e.jarFile.getName());
        
        super.addFileset(zfs);
    }
    
    /**
     * Once this task is completely assembled, this method can be used
     * to check for any errors.  If it returns normally, there are no
     * errors.
     * @throws BuildException if there are errors with this tasks
     */
    private void validate() throws BuildException {
        // make sure we have a name and version
        if (name == null) {
            throw new BuildException("Name is required.");
        }
        
        if (majorVersion == ModuleInfo.VERSION_UNSET) {
            throw new BuildException("Major version is required.");
        }
        
        // force the minor version to be 0 if it is unset
        if (minorVersion == ModuleInfo.VERSION_UNSET) {
            minorVersion = 0;
        }
        
        // check any included requirements
        for (Requires r : requires) {
            r.validate();
        }
        
        // check any included plugins
        for (Plugin p : plugins) {
            p.validate();
        }
    }
    
    public static class Requires {
        private String name;
        private int majorVersion = ModuleInfo.VERSION_UNSET;
        private int minorVersion = ModuleInfo.VERSION_UNSET;
        
        public void setName(String name) {
            this.name = name;
        }
    
        public void setVersion(int majorVersion) {
            this.majorVersion = majorVersion;
        }
    
        public void setMinorVersion(int minorVersion) {
            this.minorVersion = minorVersion;
        }
        
        private void validate() throws BuildException {
            if (name == null) {
                throw new BuildException("Requires without name.");
            }
            
            if (majorVersion == ModuleInfo.VERSION_UNSET) {
                throw new BuildException("Requires without major version.");
            }
        }
    }
    
    public static class Plugin {
        private String name;
        
        private ClientJar clientJar;
        private PluginJar commonJar;
        private ServerJar serverJar;
        
        private List<ExtraJar> extraClientJars = new ArrayList<ExtraJar>();
        private List<ExtraJar> extraCommonJars = new ArrayList<ExtraJar>();
        private List<ExtraJar> extraServerJars = new ArrayList<ExtraJar>();
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void addClient(ClientJar clientJar) {
            if (this.clientJar != null) {
                throw new BuildException("Only one <client> allowed.");
            }
            
            this.clientJar = clientJar;
        }
        
        public void addCommon(PluginJar commonJar) {
            if (this.commonJar != null) {
                throw new BuildException("Only one <common> allowed.");
            }
            
            this.commonJar = commonJar;
        }
        
        public void addServer(ServerJar serverJar) {
            if (this.serverJar != null) {
                throw new BuildException("Only one <server> allowed.");
            }
            
            this.serverJar = serverJar;
        }
        
        public void addClientJar(ExtraJar jar) {
            extraClientJars.add(jar);
        }
        
        public void addCommonJar(ExtraJar jar) {
            extraCommonJars.add(jar);
        }
        
        public void addServerJar(ExtraJar jar) {
            extraServerJars.add(jar);
        }
        
        private void validate() throws BuildException {
            if (name == null) {
                throw new BuildException("Plugin without name.");
            }
        }
    }
    
    public static class ExtraJar {
        private File jarFile;
        
        public void setJarFile(File jarFile) {
            this.jarFile = jarFile;
        }
    }
    
    public static class PluginJar extends Jar {
        private List<Service> services = new ArrayList<Service>();
        
        @Override
        public void setDestFile(File file) {
            throw new BuildException("Cannot change destination file of " +
                                     " ModuleJar.");
        }
        
        void setInternalDestFile(File file) {
            super.setDestFile(file);
        }
        
        /**
         * A nested SPI service element.  Workaround for ant 1.7 issue
         * writing services to the wrong directory.
         * @param service the nested element.
         */
        @Override
        public void addConfiguredService(Service service) {
            // Check if the service is configured correctly
            service.check();
            services.add(service);
        }

        /**
         * Initialize the zip output stream.
         * @param zOut the zip output stream
         * @throws IOException on I/O errors
         * @throws BuildException on other errors
         */
        @Override
        protected void initZipOutputStream(ZipOutputStream zOut)
                throws IOException, BuildException 
        {
            super.initZipOutputStream(zOut);
            
            if (!skipWriting) {
                writeServices(zOut);
            }
        }

        
        /**
         * Write SPI Information to JAR. Workaround for ant 1.7 issue
         * writing service to the wrong directory.
         */
        private void writeServices(ZipOutputStream zOut) throws IOException {
            Iterator serviceIterator;
            Service service;

            serviceIterator = services.iterator();
            while (serviceIterator.hasNext()) {
                service = (Service) serviceIterator.next();
                //stolen from writeManifest
                super.zipFile(service.getAsStream(), zOut,
                        "META-INF/services/" + service.getType(),
                        System.currentTimeMillis(), null,
                        ZipFileSet.DEFAULT_FILE_MODE);
            }
        }
    }
    
    public static class ServerJar extends PluginJar {
        public void addConfiguredServerPlugin(ServerPlugin serverPlugin) {
            addConfiguredService(serverPlugin);
        }
        
        public void addConfiguredCellSetup(CellSetup cellSetup) {
            addConfiguredService(cellSetup);
        }
    }
    
    public static class ServerPlugin extends Service {
        public ServerPlugin() {
            setType("org.jdesktop.wonderland.server.ServerPlugin");
        }
    }
    
    public static class CellSetup extends Service {
        public CellSetup() {
            setType("org.jdesktop.wonderland.common.cell.setup.spi.CellSetupSPI");
        }
    }
    
    public static class ClientJar extends PluginJar {
        public void addConfiguredClientPlugin(ClientPlugin clientPlugin) {
            addConfiguredService(clientPlugin);
        }
    }
    
    public static class ClientPlugin extends Service {
        public ClientPlugin() {
            setType("org.jdesktop.wonderland.client.ClientPlugin");
        }
    }
}
