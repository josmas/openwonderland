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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModuleChecksums;
import org.jdesktop.wonderland.modules.ModuleInfo;
import org.jdesktop.wonderland.modules.ModuleRequires;

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
    
    /* The fine name corresponding to the collection of modules to actually removeModuleFromInstall */
    private static final String UNINSTALL_XML = "uninstall.xml";
    
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

    /* The base module directory, and for the add/, pending/, and installed/ */
    private File root = null;
    private File addFile = null;
    private File pendingFile = null;
    private File installedFile = null;
    
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
                ModuleManagerUtils.makeDirectory(this.root);
            }
        } catch (java.io.IOException excp) {
            logger.severe("[MODULES] Failed to create root " + this.root.getAbsolutePath());
            logger.severe("[MODULES] " + excp.toString());
            System.exit(1);
        }
        
        /* Attempt to create the add/, pending/, and installed/ directories */
        this.addFile = new File(this.root, State.ADD.dir());
        this.pendingFile = new File(this.root, State.PENDING.dir());
        this.installedFile = new File(this.root, State.INSTALLED.dir());
        try {
            ModuleManagerUtils.makeDirectory(this.addFile);
            ModuleManagerUtils.makeDirectory(this.pendingFile);
            ModuleManagerUtils.makeDirectory(this.installedFile);
        } catch (java.io.IOException excp) {
            logger.severe("[MODULES] Failed to create directory " + excp.toString());
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
     * Returns a File object that represents the base directory in the module
     * system.
     * 
     * @param state The State we want the directory for
     * @return The File object for modules stored
     */
    public File getModuleStateDirectory(State state) {
        switch (state) {
            case ADD:       return this.addFile;
            case PENDING:   return this.pendingFile;
            case INSTALLED: return this.installedFile;
            default:        return null;
        }
    }
    
    /**
     * Returns an array of ModuleInfo objects of modules to removeModuleFromInstall. If no
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
     * Adds a module information to the list of modules to removeModuleFromInstall. If the
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
        List<ModuleInfo> infoCollection = new LinkedList<ModuleInfo>();
        for (ModuleInfo info : list.getModuleInfos()) {
            infoCollection.add(info);
        }
                
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
     * Removes a module information to the list of modules to removeModuleFromInstall. If the
     * module does not exist, this method throws IllegalArgumentException. Upon
     * other error, this method throws
     * IOException
     * 
     * @param moduleInfo The new module info to remove from the list of modules to removeModuleFromInstall
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
        List<ModuleInfo> infoCollection = new LinkedList<ModuleInfo>();
        for (ModuleInfo info : list.getModuleInfos()) {
            infoCollection.add(info);
        }
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
     * Attempts to add a collection of modules. Returns a new collection of all
     * of the module names that were successfully added and now pending for
     * installation (during the next restart). If delete is true, removes the
     * original modules files.
     * 
     * @param addedModules A collection of modules to add
     * @param delete True to delete the modules for successfully added modules
     * @return The names of the successfully added modules
     */
    public Collection<String> addAll(Collection<AddedModule> addedModules, boolean delete) {
        /* Returns a collection of module names added */
        Collection<String> added = new LinkedList<String>();
        
        /*
         * Check to see that the module can be safely added by making sure that
         * first its dependencies are met.
         */
        Collection<AddedModule> check = new LinkedList(addedModules);
        Collection<AddedModule> checked = this.checkDependencies(check);
        
        /*
         * Iterate through the list of modules whose requirements have been met
         * and attempt to prepare all of them for installation. If successful,
         * then add to the list of modules that we return.
         */
        Iterator<AddedModule> it = checked.iterator();
        while (it.hasNext() == true) {
            AddedModule module = it.next();
            String moduleName = module.getName();
            
            if (this.addModuleToPending(module, delete) == false) {
                logger.warning("[MODULES] ADDALL Failed to add " + moduleName);
                continue;
            }
            added.add(moduleName);
        }
        return added;
    }

    
    /**
     * Installs all of the pending modules.
     */
    public void installAll() {
        Iterator<String> it = this.getModules(State.PENDING).iterator();
        while (it.hasNext() == true) {
            PendingModule pm = (PendingModule)this.getModule(it.next(), State.PENDING);
            if (this.install(pm) == false) {
                logger.warning("[MODULES] INSTALL ALL Failed on " + pm.getName());
            }
        }
    }
 
    /**
     * Uninstalls all of the modules waiting to be uninstalled.
     */
    public void uninstallAll() {
        ModuleInfo[] uninstallInfos = this.getUninstalledModuleInfos();
        for (ModuleInfo info : uninstallInfos) {
            if (this.removeModuleFromInstall(info) == false) {
                logger.warning("[MODULES] UNINSTALL ALL Failed on " + info.toString());
            }
        }
    }

    /**
     * Attempts to remove a collection of modules. Returns a new collection of
     * all of the module names that were successfully removed and now pending
     * for un installation (during the next restart). Note that a "removed"
     * module is still present in the system until the next restart, during
     * which it is "uninstalled".
     * 
     * @param removedModules A collection of modules to remove
     * @return The names of the successfully removed modules
     */
    public Collection<String> removeAll(Collection<ModuleInfo> removedModules) {
        /* Returns a collection of module names removed */
        Collection<String> removed = new LinkedList<String>();
        
        /*
         * Make a copy of the list of removed modules so that we do not modify
         * the argument. We first need to make sure that these modules are
         * actually installed. If not, log a message and remove it from the
         * list.
         */
        Collection<ModuleInfo> check = new LinkedList<ModuleInfo>();
        Iterator<ModuleInfo> it = removedModules.iterator();
        while (it.hasNext() == true) {
            ModuleInfo moduleInfo = it.next();
            Module module = this.getModule(moduleInfo.getName(), State.INSTALLED);
            if (module != null && module.getModuleInfo().equals(moduleInfo) == true) {
                check.add(moduleInfo);
            }
        }
                
        /*
         * Check to see that the module can be safely removed by making sure that
         * first it is no longer required
         */
        Collection<ModuleInfo> checked = this.checkRequired(check);
        
        /*
         * Iterate through the list of modules that are no longer required and
         * attempt to prepare all of them for removal. If successful, then add
         * to the list of modules that we return.
         */
        Iterator<ModuleInfo> it1 = checked.iterator();
        while (it1.hasNext() == true) {
            ModuleInfo moduleInfo = it1.next();
            
            /*
             * If we have reached here, it means that we can safely removeModuleFromInstall the
             * module. Add the module info to the removeModuleFromInstall.xml file.
             */
            try {
                this.addUninstalledModuleInfo(moduleInfo);
            } catch (java.lang.Exception excp) {
                logger.warning("[MODULES] REMOVE unable to add to the list of modules to uninstall " + moduleInfo.toString());
                logger.warning("[MODULES] REMOVE will leave in the remove.xml file");
                logger.warning("[MODULES] REMOVE " + excp.toString());
                continue;
            }
            removed.add(moduleInfo.getName());
        }
        return removed;
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

        /*
         * Loop through each file and check that it is potentially valid.
         * If so, add its name to the list of module names
         */
        File[] files = this.getModuleStateDirectory(state).listFiles();
        for (File file : files) {
            if (this.isValidModule(file, state) == true) {
                list.addLast(this.getModuleName(file, state));
            }
        }
        return list;
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
        /* First check to see if the module exsits */
        File dir = this.getModuleStateDirectory(state);
        if (this.isModuleExists(uniqueName, state) == false) {
            return null;
        }

        try {
            /* Otherwise fetch the module */
            switch (state) {
                case ADD:       return new AddedModule(dir, uniqueName);
                case PENDING:   return new PendingModule(dir, uniqueName);
                case INSTALLED: return new InstalledModule(dir, uniqueName);
            }
        } catch (java.io.IOException excp) {
            logger.info("[MODULES] FACTORY Cannot create module: " + uniqueName);
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
     * Returns true if the module, given by its unique name and state exists.
     * 
     * @param uniqueName The unique name of the module
     * @param state The module state (ADD, PENDING, INSTALLED)
     * @return True if the module exists, false if not
     */
    private boolean isModuleExists(String uniqueName, State state) {
        File dir = this.getModuleStateDirectory(state);
        switch (state) {
            case ADD:       return AddedModule.isExists(dir, uniqueName);
            case PENDING:   return PendingModule.isExists(dir, uniqueName);
            case INSTALLED: return InstalledModule.isExists(dir, uniqueName);
        }
        return false;
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
    
    /**
     * Returns a set of module infos that are "present" in the system. A module
     * is "present" if it is either installed or pending to be installed and it
     * is not waiting to be uninstalled. The keys in the map returned are the
     * module information objects and the values are their states.
     */
    private Map<ModuleInfo, State> getPresentModuleInfos() {
        Map<ModuleInfo, State> present = new HashMap<ModuleInfo, State>();
        
        /* Fetch the list of pending module infos and add */
        Collection<String> pending = this.getModules(State.PENDING);
        Iterator<String> it = pending.iterator();
        while (it.hasNext() == true) {
            Module module = this.getModule(it.next(), State.PENDING);
            present.put(module.getModuleInfo(), State.PENDING);
        }
        
        /* Fetch the list of installed module infos and add */
        Collection<String> installed = this.getModules(State.INSTALLED);
        Iterator<String> it2 = installed.iterator();
        while (it2.hasNext() == true) {
            Module module = this.getModule(it2.next(), State.INSTALLED);
            present.put(module.getModuleInfo(), State.INSTALLED);
        }
        
        /* Remove the modules waiting to be uninstalled */
        ModuleInfo[] uninstalled = this.getUninstalledModuleInfos();
        for (ModuleInfo info : uninstalled) {
            present.remove(info.getName());
        }
        return present;
    }
        
    /**
     * Takes an added modules and prepares it for installation. This method
     * assumes that its dependencies have already been checked.
     * 
     * @param module The module to add
     * @param delete True to delete original added modules
     * @return True upon success, false upon failure
     */
    private boolean addModuleToPending(AddedModule module, boolean delete) {
        String moduleName = module.getName();

        /*
         * Expand the contents of the module to the installed/ directory. First
         * create a directory holding the module (but check first if it already
         * exists and log a warning message).
         */
        File pending = new File(this.getModuleStateDirectory(State.PENDING), moduleName);
        if (ModuleManagerUtils.makeCleanDirectory(pending) == false) {
            logger.warning("[MODULES] ADD TO PENDING Failed to make directory " + pending.getAbsolutePath());
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
        if (delete == true) {
            if (module.delete() == false) {
                logger.warning("ModuleManager: Unable to remove from added: " + module.getName());
                return false;
            }
        }
        return true;
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
    private boolean install(PendingModule pending) {
        /*
         * Copy the contents of the module to the installed/ directory. First
         * create a directory holding the module (but check first if it already
         * exists and log a warning message).
         */
        String moduleName = pending.getModuleInfo().getName();
        File installed = new File(this.getModuleStateDirectory(State.INSTALLED), moduleName);
        if (ModuleManagerUtils.makeCleanDirectory(installed) == false) {
            logger.warning("[MODULES] INSTALL Failed to make directory " + installed.getAbsolutePath());
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
     * @param moduleInfo The module to removeModuleFromInstall entirely
     * @return True upon success, false upon failure
     */
    private boolean removeModuleFromInstall(ModuleInfo moduleInfo) {
        /*
         * Try to delete the existing directory. If we cannot, then we log
         * an error and leave the module.xml in uninstalled/ just in case
         * a future attempt will clear things up.
         */
        File installed = new File(this.getModuleStateDirectory(State.INSTALLED), moduleInfo.getName());
        try {
            FileUtils.deleteDirectory(installed);
        } catch (java.io.IOException excp) {
            /* If we cannot delete the existing directory, log a warning */
            logger.warning("[MODULE] UNINSTALL Unable to remove directory " + installed.toString());
            return false;
        }

        /*
         * Remove from the list of modules to removeModuleFromInstall
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
     * Checks whether a collection of modules asked to be removed are still
     * required and returns a collection of modules that are no longer required.
     * This method iteratres until it can find no more modules that are no
     * longer requires.
     * 
     * @param removedModules A collection of module infos to check
     * @return A collection of modules that are no longer required
     */
    private Collection<ModuleInfo> checkRequired(Collection<ModuleInfo> removedModules) {
        Collection<ModuleInfo> satisfied = new LinkedList<ModuleInfo>();
        
        /*
         * Create a list of ModuleRequireCheck classes for each of the modules
         * we wish to check for removal
         */
        HashMap<String, ModuleRequiredCheck> required = new HashMap();
        Iterator<ModuleInfo> it = removedModules.iterator();
        while (it.hasNext() == true) {
            ModuleInfo info = it.next();
            required.put(info.getName(), new ModuleRequiredCheck(info));
        }
        
        /*
         * Fetch a list of pending and installed (minus uninstalled) modules.
         * Loop through each and add as requirements to the modules if they
         * are being asked to be removed.
         */
        Map<ModuleInfo, State> present = this.getPresentModuleInfos();
        Iterator<ModuleInfo> it2 = present.keySet().iterator();
        while (it2.hasNext() == true) {
            /*
             * Fetch the list of modules that this module requires
             */
            ModuleInfo info = it2.next();
            String moduleName = info.getName();
            State state = present.get(info);
            Module module = this.getModule(moduleName, state);
            ModuleRequires requirements = module.getModuleRequires();
            
            /*
             * Loop through each of the requirements of the module and add it
             * to the ModuleRequiredCheck, if it exists. (If it does exist, it
             * means we are checking to see if the module is still required and
             * we want to flag it with this module).
             */
            for (ModuleInfo infoRequires : requirements.getRequires()) {
                ModuleRequiredCheck check = required.get(infoRequires.getName());
                if (check != null) {
                    check.addRequiresModuleInfo(info);
                }
            }
        }
        
        /*
         * Next we need to loop through and see which modules are no longer
         * required. When a module is no longer required, we should add it to
         * the list of satisfied modules and also remove it from all of the
         * other module requirement checks. We continue checking until we can
         * find no more additional modules that are no longer requires.
         */
        boolean found = true;
        while (found == true) {
            found = false;
            Iterator<Map.Entry<String, ModuleRequiredCheck>> it4 = required.entrySet().iterator();
            while (it4.hasNext() == true) {
                Map.Entry<String, ModuleRequiredCheck> entry = it4.next();
                
                /*
                 * If the module is no longer required, then...
                 */
                if (entry.getValue().isRequired() == false) {
                    /* Add it to the 'satified list' */
                    ModuleInfo moduleInfo = entry.getValue().getCheckedModuleInfo();
                    satisfied.add(moduleInfo);
                    
                    /* Remove it from the dependency list using the iterator */
                    it4.remove();
                    
                    /*
                     * Iterator over the remaining required check objects. This
                     * part assumes the following works properly in Java: nested
                     * iterations where we just removed an entry from the original
                     * list using the Iterator.remove() method
                     */
                    Iterator<Map.Entry<String, ModuleRequiredCheck>> it5 = required.entrySet().iterator();
                    while (it5.hasNext() == true) {
                        Map.Entry<String, ModuleRequiredCheck> check = it5.next();
                        check.getValue().checkRequired(moduleInfo);
                    }
                    
                    /* Indicate we have found more satified modules */
                    found = true;
                }
            }
            
            /* If there are no more modules left, then we are done */
            if (required.isEmpty() == true) {
                break;
            }
        }       
        return satisfied;
    }
    
    /**
     * Checks the dependencies for a collection of added modules and returns a
     * collection of added modules whose requirements have been met. This method
     * iterates until it can satify the requirements of added modules no longer. 
     *
     * @param addedModules A collection of added modules to check dependencies
     * @return A collection of added moduels with satified dependencies
     */
    private Collection<AddedModule> checkDependencies(Collection<AddedModule> addedModules) {
        Collection<AddedModule> satisfied = new LinkedList<AddedModule>();
        
        /*
         * Create a list of ModuleDependencyCheck classes for each of the 
         * modules we wish to add.
         */
        HashMap<AddedModule, ModuleDependencyCheck> dependencies = new HashMap();
        Iterator<AddedModule> it = addedModules.iterator();
        while (it.hasNext() == true) {
            AddedModule module = it.next();
            dependencies.put(module, new ModuleDependencyCheck(module));
        }
        
        /*
         * Fetch a list of pending, installed, and uninstalled modules. Loop
         * through each and see if any of the modules depends upon the present
         * module. If so, mark the dependency as met.
         */
        Map<ModuleInfo, State> present = this.getPresentModuleInfos();
        Iterator<ModuleInfo> it2 = present.keySet().iterator();
        while (it2.hasNext() == true) {
            ModuleInfo potentialDependency = it2.next();
            Iterator<Map.Entry<AddedModule, ModuleDependencyCheck>> it3 = dependencies.entrySet().iterator();
            while (it3.hasNext() == true) {
                ModuleDependencyCheck check = it3.next().getValue();
                check.checkDependency(potentialDependency);
            }
        }
        
        /*
         * Next we need to loop through and see which modules have had their
         * requirements met. When a module has all of its requirements met, then
         * we should add it to the list of satified modules and also remove it
         * from all of the other module dependency checks. We continue checking
         * until we can find no more additional modules requirements met.
         */
        boolean found = true;
        while (found == true) {
            found = false;
            Iterator<Map.Entry<AddedModule, ModuleDependencyCheck>> it4 = dependencies.entrySet().iterator();
            while (it4.hasNext() == true) {
                Map.Entry<AddedModule, ModuleDependencyCheck> entry = it4.next();
                
                /*
                 * If the module has all of its requirements met and it is not
                 * already in the list of modules who have had their requirements
                 * met (meaning, this is the first time we see it has had its
                 * requirements met), then...
                 */
                if (entry.getValue().isDependenciesMet() == true && satisfied.contains(entry.getKey()) == false) {
                    /* Add it to the 'satified list' */
                    satisfied.add(entry.getKey());
                    
                    /* Remove it from the dependency list using the iterator */
                    it4.remove();
                    
                    /*
                     * Iterator over the remaining dependency check objects. This
                     * part assumes the following works properly in Java: nested
                     * iterations where we just removed an entry from the original
                     * list using the Iterator.remove() method
                     */
                    ModuleInfo moduleInfo = entry.getKey().getModuleInfo();
                    Iterator<Map.Entry<AddedModule, ModuleDependencyCheck>> it5 = dependencies.entrySet().iterator();
                    while (it5.hasNext() == true) {
                        Map.Entry<AddedModule, ModuleDependencyCheck> check = it5.next();
                        check.getValue().checkDependency(moduleInfo);
                    }
                    
                    /* Indicate we have found more satified modules */
                    found = true;
                }
            }
            
            /* If there are no more modules left, then we are done */
            if (dependencies.isEmpty() == true) {
                break;
            }
        }
        
        return satisfied;
    }
    
    public static void main(String args[]) {
        System.setProperty("wonderland.webserver.modules.root", "/Users/jordanslott/wonderland/trunk/web/examples/modules");
        ModuleManager mm = ModuleManager.getModuleManager();
        
        /* Write out the installed modules */
//        StringBuilder sb = new StringBuilder("Installed Modules\n");
//        Iterator<String> it = mm.getModules(State.INSTALLED).iterator();
//        while (it.hasNext() == true) {
//            Module module = mm.getModule(it.next(), State.INSTALLED);
//            sb.append(module.toString());
//        }
//        logger.info(sb.toString());
        
//        Collection<String> added = mm.getModules(State.ADD);
//        System.out.println(added);
//        AddedModule am1 = (AddedModule)mm.getModule("medical", State.ADD);
//        AddedModule am2 = (AddedModule)mm.getModule("demo", State.ADD);
//        Collection<AddedModule> list = new LinkedList();
//        list.add(am1);
//        list.add(am2);
//        mm.addAll(list, true);
        
//        mm.installAll();
//        
//        ModuleInfo info1 = new ModuleInfo("demo", 1, 0);
//        ModuleInfo info2 = new ModuleInfo("medical", 1, 0);
//        Collection<ModuleInfo> list = new LinkedList();
//        list.add(info1);
//        list.add(info2);
//        mm.removeAll(list);
                mm.uninstallAll();

        mm.uninstallAll();
    }
}
