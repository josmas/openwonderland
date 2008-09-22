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

package org.jdesktop.wonderland.modules.service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModuleChecksums;
import org.jdesktop.wonderland.modules.ModuleInfo;

/**
 * The ModuleManager class manages the modules on the Wonderland server. It
 * enumerates the collection of installed modules on the system. During each
 * restart (or when a management 'reload' message is received), it scans the
 * module installation directory for modules to be installed or removed and
 * updates the system.
 * <p>
 * TBD - Description of module state in order here
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleManager {
    /* The file name corresponding to the collection of modules to prepare to remove */
    private static final String REMOVE_XML = "remove.xml";
    
    /* The fine name corresponding to the collection of modules to actually uninstall */
    private static final String UNINSTALL_XML = "uninstall.xm";
    
    /* An enumeration of module states, with their correlated directories */
    public enum State {
        ADD       ("add/"),
        PENDING   ("pending/"),
        INSTALLED ("installed/");


        /* The directory in which the modules are found for the state */
        private String directory = null;
        
        /** Constructor, takes the directory as an argument */
        State(String directory) {
            this.directory = directory;
        }
        
        /**
         * Returns the directory associated with the state
         */
        public String dir() {
            return this.directory;
        }
    }

    /* The base module directory */
    private File root = null;
    
    /* The logger for the module manager */
    private static final Logger logger = Logger.getLogger(ModuleManager.class.getName());
    
    /** Constructor */
    private ModuleManager() {
        /* Find the base modules/ directory in which all modules exist */
        String baseDir = ModuleManager.getModuleDirectory();
        if (baseDir == null) {
            logger.warning("ModuleManager: no wonderland.webserver.modules.dir");
        }
        logger.info("wonderland.webserver.modules.dir=" + baseDir);
        
        /* Set the base directory for the module system, create it if necessary */
        try {
            this.root = new File(baseDir);
            if (this.root.exists() == false) {
                this.root.mkdirs();
            }
        } catch (java.lang.SecurityException excp) {
            logger.severe("ModuleManager: unable to create module root: " + this.root.getAbsolutePath());
            System.exit(1);
        }
    }
    
    /**
     * Singleton to hold instance of ModuleManager. This holder class is loaded
     * on the first execution of ModuleManager.getModuleManager().
     */
    private static class ModuleManagerHolder {
        private final static ModuleManager moduleManager = new ModuleManager();
    }
    
    /**
     * Returns a single instance of this class
     * <p>
     * @return Single instance of this class.
     */
    public static final ModuleManager getModuleManager() {
        return ModuleManagerHolder.moduleManager;
    }
    
    /**
     * Returns the error logger associated with this class.
     * 
     * @return The error logger
     */
    public static Logger getLogger() {
        return ModuleManager.logger;
    }

    /**
     * Returns an array of ModuleInfo objects of modules to prepare to remove.
     * If no such modules exist, the method returns an empty array
     * 
     * @return An array of modules to prepare to remove
     */
    public ModuleInfo[] getRemovedModuleInfos() {
        File file = new File(this.root, ModuleManager.REMOVE_XML);
        try {
            ModuleInfoList list = ModuleInfoList.decode(new FileReader(file));
            return list.getModuleInfos();
        } catch (java.lang.Exception excp) {
            logger.warning("[MODULES] GET REMOVED Failed to read " + file.getAbsolutePath());
            logger.warning("[MODULES] GET REMOVED " + excp.toString());
            return new ModuleInfo[] {};
        }
    }
    
    /**
     * Adds a module information to the list of modules to prepare to be removed.
     * If the module already exists, this method throws IllegalArgumentException.
     * Upon other error, this method throws IOException
     * 
     * @param moduleInfo The new module info to add the list of modules to be removed
     * @throw IllegalArgumentException If the module already exists
     * @throw IOException Upon general I/O error
     */
    public void addRemoveModuleInfo(ModuleInfo moduleInfo) throws IllegalArgumentException, IOException {
        /*
         * We first need to de-serialized the object from disk. If it is not
         * there, then create a new one.
         */
        ModuleInfoList list = null;
        File file = new File(this.root, ModuleManager.REMOVE_XML);
        try {
            list = ModuleInfoList.decode(new FileReader(file));
        } catch (java.lang.Exception excp) {
            logger.info("[MODULES] ADD REMOVED unable to read remove.xml file, not necessarily a big deal.");
            logger.info("[MODULES] ADD REMOVED will just create a new one.");
            logger.info("[MODULES] ADD REMOVED " + excp.toString());
            list = new ModuleInfoList();
        }
        
        /*
         * Fetch the current list of modules, check if ours exists already. We
         * check that the module name and version matches.
         */
        List<ModuleInfo> infoCollection = Arrays.asList(list.getModuleInfos());
        if (infoCollection.contains(moduleInfo) == true) {
            logger.info("[MODULES] ADD REMOVED remove.xml already has the entry " + moduleInfo.toString());
            return;
        }
            
        infoCollection.add(moduleInfo);
        list.setModuleInfos(infoCollection.toArray(new ModuleInfo[] {}));
        
        /* Write the list back out to the file */
        try {
            list.encode(new FileWriter(file));
        } catch (java.lang.Exception excp) {
            logger.warning("[MODULES] ADD REMOVED unable to save remove.xml file");
            logger.warning("[MODULES] " + excp.toString());
        }
    }
 
    /**
     * Removes a module information to the list of modules to prepare to be
     * removed. If the module does not exist, this method throws
     * IllegalArgumentException. Upon other error, this method throws
     * IOException
     * 
     * @param moduleInfo The new module info to remove from the list of modules to be removed
     * @throw IllegalArgumentException If the module already exists
     * @throw IOException Upon general I/O error
     */
    public void removeRemoveModuleInfo(ModuleInfo moduleInfo) throws IllegalArgumentException, IOException {
        /*
         * We first need to de-serialized the object from disk. If it is not
         * there, then create a new one.
         */
        ModuleInfoList list = null;
        File file = new File(this.root, ModuleManager.REMOVE_XML);
        try {
            list = ModuleInfoList.decode(new FileReader(file));
        } catch (java.lang.Exception excp) {
            logger.warning("[MODULES] ADD REMOVED unable to read remove.xml file. This should exist.");
            logger.warning("[MODULES] ADD REMOVED " + excp.toString());
            list = new ModuleInfoList();
        }
        
        /*
         * Fetch the current list of modules, check if ours exists already. We
         * check that the module name and version matches.
         */
        List<ModuleInfo> infoCollection = Arrays.asList(list.getModuleInfos());
        if (infoCollection.contains(moduleInfo) == false) {
            logger.warning("[MODULES] ADD REMOVED remove.xml does not have the entry " + moduleInfo.toString());
            return;
        }
            
        infoCollection.remove(moduleInfo);
        list.setModuleInfos(infoCollection.toArray(new ModuleInfo[] {}));
        
        /* Write the list back out to the file */
        try {
            list.encode(new FileWriter(file));
        } catch (java.lang.Exception excp) {
            logger.warning("[MODULES] ADD REMOVED unable to save remove.xml file");
            logger.warning("[MODULES] " + excp.toString());
        }
    }
    
    /**
     * Returns an array of ModuleInfo objects of modules to uninstall. If no
     * such modules exist, the method returns an empty array
     * 
     * @return An array of modules to prepare to remove
     */
    public ModuleInfo[] getUninstalledModuleInfos() {
        File file = new File(this.root, ModuleManager.UNINSTALL_XML);
        try {
            ModuleInfoList list = ModuleInfoList.decode(new FileReader(file));
            return list.getModuleInfos();
        } catch (java.lang.Exception excp) {
            logger.warning("[MODULES] GET UNINSTALL Failed to read " + file.getAbsolutePath());
            logger.warning("[MODULES] GET UNINSTALL " + excp.toString());
            return new ModuleInfo[] {};
        }
    }
    
    /**
     * Adds a module information to the list of modules to uninstall. If the
     * module already exists, this method throws IllegalArgumentException. Upon
     * other error, this method throws IOException
     * 
     * @param moduleInfo The new module info to add the list of modules to be removed
     * @throw IllegalArgumentException If the module already exists
     * @throw IOException Upon general I/O error
     */
    public void addUninstalledModuleInfo(ModuleInfo moduleInfo) throws IllegalArgumentException, IOException {
        /*
         * We first need to de-serialized the object from disk. If it is not
         * there, then create a new one.
         */
        ModuleInfoList list = null;
        File file = new File(this.root, ModuleManager.UNINSTALL_XML);
        try {
            list = ModuleInfoList.decode(new FileReader(file));
        } catch (java.lang.Exception excp) {
            logger.info("[MODULES] ADD UNINSTALL unable to read remove.xml file, not necessarily a big deal.");
            logger.info("[MODULES] ADD UNINSTALL will just create a new one.");
            logger.info("[MODULES] ADD UNINSTALL " + excp.toString());
            list = new ModuleInfoList();
        }
        
        /*
         * Fetch the current list of modules, check if ours exists already. We
         * check that the module name and version matches.
         */
        List<ModuleInfo> infoCollection = Arrays.asList(list.getModuleInfos());
        if (infoCollection.contains(moduleInfo) == true) {
            logger.info("[MODULES] ADD UNINSTALL uninstall.xml already has the entry " + moduleInfo.toString());
            return;
        }
            
        infoCollection.add(moduleInfo);
        list.setModuleInfos(infoCollection.toArray(new ModuleInfo[] {}));
        
        /* Write the list back out to the file */
        try {
            list.encode(new FileWriter(file));
        } catch (java.lang.Exception excp) {
            logger.warning("[MODULES] ADD UNINSTALL unable to save uninstall.xml file");
            logger.warning("[MODULES] " + excp.toString());
        }
    }
 
    /**
     * Removes a module information to the list of modules to uninstall. If the
     * module does not exist, this method throws IllegalArgumentException. Upon
     * other error, this method throws
     * IOException
     * 
     * @param moduleInfo The new module info to remove from the list of modules to uninstall
     * @throw IllegalArgumentException If the module already exists
     * @throw IOException Upon general I/O error
     */
    public void removeUninstalledModuleInfo(ModuleInfo moduleInfo) throws IllegalArgumentException, IOException {
        /*
         * We first need to de-serialized the object from disk. If it is not
         * there, then create a new one.
         */
        ModuleInfoList list = null;
        File file = new File(this.root, ModuleManager.UNINSTALL_XML);
        try {
            list = ModuleInfoList.decode(new FileReader(file));
        } catch (java.lang.Exception excp) {
            logger.warning("[MODULES] ADD UNINSTALL unable to read remove.xml file. This should exist.");
            logger.warning("[MODULES] ADD UNINSTALL " + excp.toString());
            list = new ModuleInfoList();
        }
        
        /*
         * Fetch the current list of modules, check if ours exists already. We
         * check that the module name and version matches.
         */
        List<ModuleInfo> infoCollection = Arrays.asList(list.getModuleInfos());
        if (infoCollection.contains(moduleInfo) == false) {
            logger.warning("[MODULES] ADD UNINSTALL uninstall.xml does not have the entry " + moduleInfo.toString());
            return;
        }
            
        infoCollection.remove(moduleInfo);
        list.setModuleInfos(infoCollection.toArray(new ModuleInfo[] {}));
        
        /* Write the list back out to the file */
        try {
            list.encode(new FileWriter(file));
        } catch (java.lang.Exception excp) {
            logger.warning("[MODULES] ADD UNINSTALL unable to save uninstall.xml file");
            logger.warning("[MODULES] " + excp.toString());
        }
    }
    
    /**
     * Adds a module to be installed. The added module is simply a properly
     * formatted jar file. This step makes sure that the prerequisites have
     * been met and generates a checksum file for the artwork resources in the
     * module. Returns true upon success, false upon failure.
     * 
     * @param module The module to add
     * @return True upon success, false upon failure
     */
    public boolean add(AddedModule module) {
        /*
         * Check to see that the module can be safely added by making sure that
         * first its dependencies are met.
         */
        ModuleDependencyCheck check = new ModuleDependencyCheck(module);
        if (check.checkDependencies() == false) {
            logger.warning("ModuleManager: dependencies not satisfied for: " +
                    module.getModuleInfo().getName());
            return false;
        }
        
        /*
         * Expand the contents of the module to the installed/ directory. First
         * create a directory holding the module (but check first if it already
         * exists and log a warning message).
         */
        String moduleName = module.getName();
        File pending = null;
        try {
            pending = new File(this.getModuleRoot(State.PENDING), moduleName);
        } catch (java.io.IOException excp) {
            logger.severe("ModuleManager: unable to create pending dir for module: " + moduleName);
            return false;
        }
        
        if (pending.exists() == true) {
            /* Log an error, and try to delete the existing directory */
            logger.warning("ModuleManager: Pending module already exists in " + pending.toString());
            try {
                FileUtils.deleteDirectory(pending);
            } catch (java.io.IOException excp) {
                /* If we cannot delete the existing directory, this is fatal */
                logger.severe("ModuleManager: Unable to remove existing dir: " + pending.toString());
                return false;
            }
        }
        
        /* Now go ahead and recreate the directory */
        try {
            pending.mkdir();
        } catch (java.lang.SecurityException excp) {
            logger.severe("ModuleManager: Unable to create install: " + pending.toString());
            return false;
        }
        
        /* Next, expand the contents of the module into this directory */
        try {
            module.expand(pending);
        } catch (java.io.IOException excp) {
            logger.severe("ModuleManager: Unable to write to install: " + pending.toString());
            logger.severe("ModuleManager: " + excp.toString());
            return false;
        }
        
        /* Compile the artwork checksums, overwrite any existing file */
        try {
            File artRoot = new File(pending, Module.MODULE_ART);
            File chkFile = new File(pending, Module.MODULE_CHECKSUMS);
            ModuleChecksums cks = ModuleChecksums.generate(artRoot, ModuleChecksums.SHA1_CHECKSUM_ALGORITHM, new String[0], new String[0]);
            cks.encode(new FileWriter(chkFile));
        } catch (java.lang.Exception excp) {
            logger.severe("ModuleManager: Failed to create checksums: " + excp.toString());
            return false;
        }

        /* Remove the added file */
        if (module.delete() == false) {
            logger.warning("ModuleManager: Unable to remove from added: " + module.getName());
            return false;
        }
        return true;
    }
    
    /**
     * Installs all of the pending modules.
     */
    public void installAll() {
        Iterator<String> it = this.getModules(State.PENDING).iterator();
        while (it.hasNext() == true) {
            PendingModule pm = (PendingModule)this.getModule(it.next(), State.PENDING);
            if (this.install(pm) == false) {
                logger.warning("[MODULES] Failed to install pending module " + pm.getName());
            }
        }
    }
 
    /**
     * Uninstalls all of the modules waiting to be uninstalled.
     */
    public void uninstallAll() {
        ModuleInfo[] uninstallInfos = this.getUninstalledModuleInfos();
        for (ModuleInfo info : uninstallInfos) {
            this.uninstall(info);
        }
    }
    
    /**
     * Installs a pending module. This method copies the necessary files into
     * location. At this point, this method assumes that the pending module
     * has been properly prepared and all configuration of the system has
     * been done.
     * <p>
     * This method returns true upon success and false upon failure, however,
     * failure should only occur very rarely. It should only occur for things
     * like inability to write to disk or out of disk space.
     *
     * @param pending The module pending installation
     * @return True upon success, false upon failure.
     */
    public boolean install(PendingModule pending) {
        /*
         * Copy the contents of the module to the installed/ directory. First
         * create a directory holding the module (but check first if it already
         * exists and log a warning message).
         */
        String moduleName = pending.getModuleInfo().getName();
        File installed = null;
        try {
            installed = new File(this.getModuleRoot(State.INSTALLED), moduleName);
        } catch (java.io.IOException excp) {
            logger.warning("ModuleManager: Unable to create install file: " + moduleName);
            return false;
        }
        
        if (installed.exists() == true) {
            /* Log an error, and try to delete the existing directory */
            logger.warning("ModuleManager: Pending module already exists in " + installed.toString());
            try {
                FileUtils.deleteDirectory(installed);
            } catch (java.io.IOException excp) {
                /* If we cannot delete the existing directory, this is fatal */
                logger.severe("ModuleManager: Unable to remove existing dir: " + installed.toString());
                return false;
            }
        }
        
        /* Now go ahead and recreate the directory */
        try {
            installed.mkdir();
        } catch (java.lang.SecurityException excp) {
            logger.severe("ModuleManager: Unable to create install: " + installed.toString());
            return false;
        }
        
        /* Next, copy the contents of the module into this directory */
        try {
            File pendingFile = pending.getRoot();
            FileUtils.copyDirectory(pendingFile, installed);
        } catch (java.io.IOException excp) {
            logger.severe("ModuleManager: Unable to write to install: " + installed.toString());
            return false;
        }
        
        /* Remove the pending module */
        if (pending.delete() == false) {
            logger.warning("ModuleManager: Unable to remove from pending: " + installed.toString());
            return false;
        }
        return true;
    }
  
    /**
     * Prepares a module for uninstall by "removing" it. This step verifies that
     * no module relies upon it and can be safely uninstalled. When verification
     * is complete, move the module.xml file to the uninstall/ directory.
     * <p>
     * This method returns true upon success, false upon failure. Reasons for
     * failure include: some installed module depends upon this module, so this
     * module cannot be uninstalled.
     * 
     * @param moduleInfo The module to remove (prepare for uninstall)
     * @return True upon success, false upon error.
     */
    public boolean remove(ModuleInfo moduleInfo) {
        /*
         * Fetch the module that is installed. If not installed, then there is
         * no reason to remove it.
         */
        InstalledModule im = (InstalledModule)this.getModule(moduleInfo.getName(), State.INSTALLED);
        if (im == null) {
            logger.warning("[MODULES] REMOVE Installed module does not exist " + moduleInfo.getName());
            return false;
        }
        
        /*
         * Check to see that the module can be safely removed by making sure that
         * no other installed module (or one waiting to be installed) depends
         * upon it.
         */
        ModuleRequiredCheck check = new ModuleRequiredCheck(im.getName());
        if (check.checkRequired() == true) {
            logger.warning("[MODULES] REMOVE module still required: " + im.getName());
            return false;
        }
        
        /*
         * If we have reached here, it means that we can safely uninstall the
         * module. Add the module info to the uninstall.xml file.
         */
        try {
            this.addUninstalledModuleInfo(moduleInfo);
        } catch (java.lang.Exception excp) {
            logger.warning("[MODULES] REMOVE unable to add to the list of modules to uninstall " + moduleInfo.toString());
            logger.warning("[MODULES] REMOVE will leave in the remove.xml file");
            logger.warning("[MODULES] REMOVE " + excp.toString());
            return false;
        }

        /*
         * Remove from the list of modules to prepare to uninstall
         */
         try {
            this.removeRemoveModuleInfo(moduleInfo);
        } catch (java.lang.Exception excp) {
            logger.warning("[MODULES] REMOVE unable to add to the list of modules to uninstall " + moduleInfo.toString());
            logger.warning("[MODULES] REMOVE will leave in the remove.xml file");
            logger.warning("[MODULES] REMOVE " + excp.toString());
            return false;
        }        
        return true;
    }

    
    /**
     * Uninstalls a removed that has been uninstalled. At this point, all of
     * the necessary configuration has happened to the system and the files just
     * need to be removed from the installed/ directory. This method removes the
     * moduleInfo file from the uinstall.xml directory when complete.
     * <p>
     * This method returns true upon success and false upon failure, however,
     * failure should only occur very rarely. It should only occur for things
     * like inability to write to disk or if the module within the installed/
     * directory does not exist. Neither of these should happen under normal
     * circumstances
     *
     * @param moduleInfo The module to uninstall entirely
     * @return True upon success, false upon failure
     */
    public boolean uninstall(ModuleInfo moduleInfo) {
        /*
         * Try to delete the existing directory. If we cannot, then we log
         * an error and leave the module.xml in uninstalled/ just in case
         * a future attempt will clear things up.
         */
        File installed = null;
        try {
            installed = new File(this.getModuleRoot(State.INSTALLED), moduleInfo.getName());
            FileUtils.deleteDirectory(installed);
        } catch (java.io.IOException excp) {
            /* If we cannot delete the existing directory, log a warning */
            logger.warning("[MODULE] UNINSTALL Unable to remove directory " + installed.toString());
            return false;
        }

        /*
         * Remove from the list of modules to uninstall
         */
         try {
            this.removeUninstalledModuleInfo(moduleInfo);
        } catch (java.lang.Exception excp) {
            logger.warning("[MODULES] UNINSTALL unable to remove from the list of modules to uninstall " + moduleInfo.toString());
            logger.warning("[MODULES] UNINSTALL " + excp.toString());
            return false;
        }        
        return true;
    }
    
    /**
     * Returns a collection of module names for the given state. If there are no
     * modules present in the given state, then this method returns an empty
     * connection.
     * 
     * @param state The module state (ADD, PENDING, INSTALLED, REMOVE)
     * @return An collection of unique module names in the given state
     */
    public Collection<String> getModules(State state) {
        LinkedList<String> list = new LinkedList<String>();
        try {
            /*
             * Loop through each file and check that it is potentially valid.
             * If so, add its name to the list of module names
             */
            File[] files = this.getModuleRoot(state).listFiles();
            for (File file : files) {
                if (this.isValidModule(file, state) == true) {
                    list.addLast(this.getModuleName(file, state));
                }
            }
            return list;
        } catch (java.io.IOException excp) {
            logger.warning("ModuleManager: Unable to access module directory: " + excp.toString());
            return list;
        }
    }
    
    /**
     * Returns a module given its unique name and state. If the module name
     * does not exist, this method returns null.
     * 
     * @param uniqueName The name of the module
     * @param state The module state (ADD, PENDING, INSTALLED, REMOVE)
     * @return The module
     */
    public Module getModule(String uniqueName, State state) {
        return this.moduleFactory(uniqueName, state);
    }

    /**
     * Returns a module given its name if its either installed (and not about
     * to be removed) or waiting to be installed. That is, returns true if the
     * module is either in the INSTALLED state (and not UNINSTALL state) or
     * the PENDING state. This method is used to verify a module's dependencies
     * are met before installation.
     * 
     * @return The module information is the module is "present", null if not.
     */
    public ModuleInfo isModulePresent(String uniqueName) {
        Module module = null;
        
        /* If waiting to be removed, then return null */
        ModuleInfo[] removedModuleInfos = this.getRemovedModuleInfos();
        for (ModuleInfo info : removedModuleInfos) {
            if (uniqueName.compareTo(info.getName()) == 0) {
                return info;
            }
        }
        
        /* If installed, then return it */
        if ((module = this.getModule(uniqueName, State.INSTALLED)) != null) {
            return module.getModuleInfo();
        }
        
        /* If about to be installed, then return true */
        if ((module = this.getModule(uniqueName, State.PENDING)) != null) {
            return module.getModuleInfo();
        }
        
        /* Otherwise, it is not present */
        return null;
    }
    
    /**
     * Returns whether a given module (by its unique name) is required by
     * another module that is either installed (and not about to be removed)
     * or waiting to be installed. Returns true if it is required by one of
     * these modules, false if not.
     * 
     * @return Returns the module that requires the given module, or null
     */
    public Module isModuleRequired(String uniqueName) {
        /* Loop through all installed (but not uninstalled) modules */
        ModuleInfo[] uninstallModuleInfos = this.getUninstalledModuleInfos();
        Iterator<String> it = this.getModules(State.INSTALLED).iterator();
        while (it.hasNext() == true) {
            String installedName = it.next();
            InstalledModule im = (InstalledModule)this.getModule(uniqueName, State.INSTALLED);
            
            /* Do not look at uninstalled modules */
            for (ModuleInfo info : uninstallModuleInfos) {
                if (im.getModuleRequires().isRequired(info.getName()) == true) {
                    return im;
                }
            }
        }
        
        /* Loop through all of the pending modules */
        Iterator<String> it2 = this.getModules(State.PENDING).iterator();
        while (it2.hasNext() == true) {
            PendingModule pm = (PendingModule)this.getModule(it2.next(), State.PENDING);
            if (pm.getModuleRequires().isRequired(uniqueName) == true) {
                return pm;
            }
        }
        
        return null;
    }

    /**
     * Returns the module installation directory: the wonderland.module.dir
     * property.
     */
    private static String getModuleDirectory() {
        return System.getProperty("wonderland.webserver.modules.root");
    }
    
    /**
     * Returns a File object that represents the base directory in the module
     * system. Creates the directory if it does not exist. Throws IOException
     * if it cannot.
     */
    public File getModuleRoot(State state) throws IOException {
        try {
            File file = new File(this.root, state.dir());
            if (file.exists() == false) {
                file.mkdir();
            }
            return file;
        } catch (java.lang.SecurityException excp) {
            logger.severe("ModuleManager: Unable to create module subdir: " + state.dir());
            throw new IOException(excp.toString());
        }
    }
    
    /**
     * Returns true if an entry in the module directory is a potentially valid
     * module, false if not. An entry is potentially valid if is follows the
     * proper naming conventions, but does not necessarily have to be a well-
     * formed module.
     * 
     * @param file The file in the module directory
     * @param state The module state (ADD, PENDING, INSTALLED)
     * @return True if the entry is a potentially valid module, false if not.
     */
    private boolean isValidModule(File file, State state) {
        switch (state) {
            case ADD:       return AddedModule.isValidFile(file);
            case PENDING:   return PendingModule.isValidFile(file);
            case INSTALLED: return InstalledModule.isValidFile(file);
        }
        return false;
    }

    /**
     * Returns an instance of a module given its file root and state.
     * 
     * @param name The unique name of the module
     * @param state The state of the module (ADD, PENDING, INSTALLED)
     * @return The Module object.
     */
    private Module moduleFactory(String name, State state) {
        try {
            switch (state) {
                case ADD:       return new AddedModule(this.getModuleRoot(state), name);
                case PENDING:   return new PendingModule(this.getModuleRoot(state), name);
                case INSTALLED: return new InstalledModule(this.getModuleRoot(state), name);
            }
        } catch (java.io.IOException excp) {
            logger.warning("ModuleManager: cannot create module: " + excp.toString());
        }
        return null;
    }
    
    /**
     * Returns the name of the module given its file root
     * 
     * @param file The file pointin to the module directory or jar file
     * @param state The state of the module (ADD, PENDING, INSTALLED, REMOVE)
     * @return The name of the module
     */
    private String getModuleName(File file, State state) {
        switch (state) {
            case ADD:       return AddedModule.getModuleName(file);
            case PENDING:   return PendingModule.getModuleName(file);
            case INSTALLED: return InstalledModule.getModuleName(file);
        }
        return null;
    }
    
    public static void main(String args[]) {
        System.setProperty("wonderland.webserver.modules.root", "/Users/jordanslott/wonderland/trunk/web/examples/modules");
        ModuleManager mm = ModuleManager.getModuleManager();
        
        /* Write out the installed modules */
        StringBuilder sb = new StringBuilder("Installed Modules\n");
        Iterator<String> it = mm.getModules(State.INSTALLED).iterator();
        while (it.hasNext() == true) {
            Module module = mm.getModule(it.next(), State.INSTALLED);
            sb.append(module.toString());
        }
        logger.info(sb.toString());
        
        Collection<String> added = mm.getModules(State.ADD);
        System.out.println(added);
        AddedModule am = (AddedModule)mm.getModule("mpk20", State.ADD);
        mm.add(am);
        
        PendingModule pm = (PendingModule)mm.getModule("mpk20", State.PENDING);
        mm.install(pm);
        
    }
}
