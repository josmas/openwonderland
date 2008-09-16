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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
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
    /* An enumeration of module states, with their correlated directories */
    public enum State {
        ADD       ("add/"),
        PENDING   ("pending/"),
        INSTALLED ("installed/"),
        REMOVE    ("remove/"),
        UNINSTALL ("uninstall/");

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
         * module. Move the module.xml file from the remove/ directory to the
         * uninstall/ directory
         */
        File file = null;
        try {
            file = new File(this.getModuleRoot(State.UNINSTALL), moduleInfo.getName() + ".xml");
            moduleInfo.encode(new FileWriter(file));
        } catch (java.lang.Exception excp) {
            logger.warning("[MODULES] REMOVE cannot move module.xml to " + file.getAbsolutePath());
            logger.warning("[MODULES] REMOVE " + excp.toString());
            return false;
        }
        
        /* Remove the module.xml file from the remove/ directory (if it exists) */
        try {
            File removeFile = new File(this.getModuleRoot(State.REMOVE), moduleInfo.getName() + ".xml");
            removeFile.delete();
        } catch (java.lang.Exception excp) {
            logger.warning("[MODULES] REMOVE cannot remove module.xml from remove/");
            logger.warning("[MODULES] REMOVE " + excp.toString());
            return false;
        }
        return true;
    }

    
    /**
     * Uninstalls a removed that has been uninstalled. At this point, all of
     * the necessary configuration has happened to the system and the files just
     * need to be removed from the installed/ directory. This method removes the
     * module.xml file from the uinstalled/ directory when complete.
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
         * Remove the file in the uninstalled/ directory.
         */
        File file = null;
        try {
            file = new File(this.getModuleRoot(State.UNINSTALL), moduleInfo.getName() + ".xml");
            file.delete();
        } catch (java.lang.Exception excp) {
            logger.warning("[MODULE] UNINSTALL Unable to remove " + file.getAbsolutePath());
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
     * @return True if the module is either installed or pending installation
     */
    public Module isModulePresent(String uniqueName) {
        Module module = null;
        
        /* If waiting to be removed, then return null */
        if (this.getModule(uniqueName, State.REMOVE) != null) {
            return null;
        }
        
        /* If installed, then return it */
        if ((module = this.getModule(uniqueName, State.INSTALLED)) != null) {
            return module;
        }
        
        /* If about to be installed, then return true */
        if ((module = this.getModule(uniqueName, State.PENDING)) != null) {
            return module;
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
        Iterator<String> it = this.getModules(State.INSTALLED).iterator();
        while (it.hasNext() == true) {
            String installedName = it.next();
            InstalledModule im = (InstalledModule)this.getModule(uniqueName, State.INSTALLED);
            
            /* Do not look at uninstalled modules */
            if (this.getModule(installedName, State.REMOVE) == null) {
                if (im.getModuleRequires().isRequired(uniqueName) == true) {
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
     * @param state The module state
     * @return True if the entry is a potentially valid module, false if not.
     */
    private boolean isValidModule(File file, State state) {
        switch (state) {
            case ADD:
                return AddedModule.isValidFile(file);
                
            case PENDING:
                return PendingModule.isValidFile(file);
                
            case INSTALLED:
                return InstalledModule.isValidFile(file);
                
            case REMOVE:
                return file.isFile() && file.getName().endsWith(".xml");
        }
        return false;
    }

    /**
     * Returns an instance of a module given its file root and state.
     * 
     * @param name The unique name of the module
     * @param state The state of the module (ADD, PENDING, INSTALLED, REMOVE)
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
