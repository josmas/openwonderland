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
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.jdesktop.wonderland.modules.Module;
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
    /* The definition of important file locations within the module directory */
    public static final String ADD_DIR         = "add/";
    public static final String PENDING_DIR     = "pending/";
    public static final String INSTALLED_DIR   = "installed/";
    public static final String REMOVE_DIR      = "remove/";
    public static final String UNINSTALL_DIR   = "uninstall/";
    
    /* The base module directory */
    private File root = null;
    
    /* File roots forstatic each of the important module directories */
    private File addDirectory = null;
    private File pendingDirectory = null;
    private File installedDirectory = null;
    private File removeDirectory = null;
    private File uninstallDirectory = null;
    
    /*
     * Hash maps of modules that are installed, pending to be installed, and
     * pending to be removed.
     */
    private HashMap<String, AddedModule>  addModules = null;
    private HashMap<String, PendingModule> pendingModules = null;
    private HashMap<String, InstalledModule> installedModules = null;
    private HashMap<String, RemovedModule> removeModules = null;
    private HashMap<String, UninstalledModule> uninstallModules = null;

    
    /* The logger for the module manager */
    private static final Logger logger = Logger.getLogger(ModuleManager.class.getName());

    
    /** Constructor */
    private ModuleManager() {
        /* Initialize some member variables and the like */
        this.addModules = new HashMap<String, AddedModule>();
        this.pendingModules = new HashMap<String, PendingModule>();
        this.installedModules = new HashMap<String, InstalledModule>();
        this.removeModules = new HashMap<String, RemovedModule>();
        this.uninstallModules = new HashMap<String, UninstalledModule>();
        
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
                
        /* Create the file roots for each important directory */
        try {
            this.addDirectory = this.getModuleRoot(ModuleManager.ADD_DIR);
            this.pendingDirectory = this.getModuleRoot(ModuleManager.PENDING_DIR);
            this.installedDirectory = this.getModuleRoot(ModuleManager.INSTALLED_DIR);
            this.removeDirectory = this.getModuleRoot(ModuleManager.REMOVE_DIR);
            this.uninstallDirectory = this.getModuleRoot(ModuleManager.UNINSTALL_DIR);
        } catch (java.io.IOException excp) {
            logger.severe("ModuleManager: unable to create module subdir: " + excp.toString());
            System.exit(1);
        }
        
        /* Go ahead and read all of the modules */
        this.readAddModules();
        this.readPendingModules();
        this.readInstalledModules();
        this.readRemoveModules();
        this.readUninstalledModules();
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
     * TBD.
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
        }
        
        /*
         * If we have reached here, it means that we can safely install the
         * module. Move the module archive file from the add/ directory to the
         * pending/ directory
         */
        String name = module.getFile().getName();
        File pendingFile = new File(this.pendingDirectory, name);
        try {
            FileUtils.moveFile(module.getFile(), pendingFile);
        } catch (java.io.IOException excp) {
            /* Log an error and return false to indicate step failed */
            logger.warning("ModuleManager: Unable to move " +
                    module.getFile().getAbsolutePath() + " to " +
                    pendingFile.getAbsolutePath());
            return false;
        }
        
        /* Remove from the list modules to add, add to pending list */
        PendingModule pm = PendingModule.getPendingModule(pendingFile);
        this.addModules.remove(module.getModuleInfo().getName());
        this.pendingModules.put(module.getModuleInfo().getName(), pm);
        
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
    public boolean install(PendingModule pending) {
        /*
         * Expand the contents of the module to the installed/ directory. First
         * create a directory holding the module (but check first if it already
         * exists and log a warning message).
         */
        String moduleName = pending.getModuleInfo().getName();
        File installed = new File(this.root, ModuleManager.INSTALLED_DIR + moduleName);
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
        
        /* Next, expand the contents of the module into this directory */
        try {
            pending.expand(installed);
        } catch (java.io.IOException excp) {
            logger.severe("ModuleManager: Unable to write to install: " + installed.toString());
            return false;
        }
        
        /* Remove the pending file and update the lists */
        if (pending.getFile().delete() == false) {
            logger.warning("ModuleManager: Unable to remove from pending: " + installed.toString());
        }
        
        InstalledModule im = InstalledModule.getInstalledModule(installed);
        this.pendingModules.remove(moduleName);
        this.installedModules.put(moduleName, im);
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
     * @param module The module to remove (prepare for uninstall)
     * @return True upon success, false upon error.
     */
    public boolean remove(RemovedModule module) {
        /*
         * Check to see that the module can be safely removed by making sure that
         * no other installed module (or one waiting to be installed) depends
         * upon it.
         */
        ModuleRequiredCheck check = new ModuleRequiredCheck(module);
        if (check.checkRequired() == true) {
            logger.warning("ModuleManager: module still required: " +
                    module.getModuleInfo().getName());
        }
        
        /*
         * If we have reached here, it means that we can safely uninstall the
         * module. Move the module.xml file from the remove/ directory to the
         * uninstall/ directory
         */
        String name = module.getModuleXML().getName();
        File uninstall = new File(this.uninstallDirectory, name);
        try {
            FileUtils.moveFile(module.getModuleXML(), uninstall);
        } catch (java.io.IOException excp) {
            /* Log an error and return false to indicate step failed */
            logger.warning("ModuleManager: Unable to move " +
                    module.getModuleXML().getAbsolutePath() + " to " +
                    uninstall.getAbsolutePath());
            return false;
        }
        
        // add to the list of uninstalled modules XXXX
        UninstalledModule um = new UninstalledModule(uninstall, module.getModuleInfo());
        this.removeModules.remove(name);
        this.uninstallModules.put(name, um);
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
     * @param module The module to uninstall entirely
     * @return True upon success, false upon failure
     */
    public boolean uninstall(UninstalledModule module) {
        /*
         * See if the directory exists, if so, remove it.
         */
        String moduleName = module.getModuleInfo().getName();
        File installed = new File(this.installedDirectory, moduleName);
        if (installed.exists() == true) {
            /*
             * Try to delete the existing directory. If we cannot, then we log
             * an error and leave the module.xml in uninstalled/ just in case
             * a future attempt will clear things up.
             */
            try {
                FileUtils.deleteDirectory(installed);
            } catch (java.io.IOException excp) {
                /* If we cannot delete the existing directory, log a warning */
                logger.warning("ModuleManager: Unable to remove installed dir: " + installed.toString());
                return false;
            }
        }
        
        /*
         * Remove the file in the uninstalled/ directory.
         */
        try {
            return module.getModuleXML().delete();
        } catch (java.lang.SecurityException excp) {
            logger.warning("ModuleManager: Unable to remove module.xml to uinstalled: " + module.getModuleXML().getAbsolutePath());
        }
        
        /* Remove from the list of uninstalled modules */
        this.uninstallModules.remove(moduleName);
        return true;
    }
    
    /**
     * Returns the added module given its unique name, null if the module
     * does not exist.
     *
     * @param moduleName The unique module name
     * @return The added module
     */
    public AddedModule getAddModule(String uniqueName) {
        return this.addModules.get(uniqueName);
    }
    
    /**
     * Returns the pending module given its unique name, null if the module
     * does not exist.
     *
     * @param moduleName The unique module name
     * @return The pending module
     */
    public PendingModule getPendingModule(String uniqueName) {
        return this.pendingModules.get(uniqueName);
    }
    
    /**
     * Returns the installed module given its unique name, null if the module
     * does not exist.
     *
     * @param moduleName The unique module name
     * @return The installed modules
     */
    public InstalledModule getInstalledModule(String uniqueName) {
        return this.installedModules.get(uniqueName);
    }
    
    /**
     * Returns an array of strings of the names of all installed modules.
     * Returns an empty array if no installed modules exists.
     * 
     * @param An array of installed module names
     */
    public String[] getInstalledModules() {
        return this.installedModules.keySet().toArray(new String[] {});
    }
    
    /**
     * Returns the removed module given its unique name, null if the module
     * does not exist.
     *
     * @param moduleName The unique module name
     * @return The removed modules
     */
    public RemovedModule getRemoveModule(String uniqueName) {
        return this.removeModules.get(uniqueName);
    }
        
    /**
     * Returns the uninstalled module given its unique name, null if the module
     * does not exist.
     *
     * @param moduleName The unique module name
     * @return The uninstalled modules
     */
    public UninstalledModule getUninstalledModule(String uniqueName) {
        return this.uninstallModules.get(uniqueName);
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
        if (this.getUninstalledModule(uniqueName) != null) {
            return null;
        }
        
        /* If installed, then return it */
        if ((module = this.getInstalledModule(uniqueName)) != null) {
            return module;
        }
        
        /* If about to be installed, then return true */
        if ((module = this.getPendingModule(uniqueName)) != null) {
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
        Collection<InstalledModule> installed = this.installedModules.values();
        Iterator<InstalledModule> iterator = installed.iterator();
        while (iterator.hasNext() == true) {
            InstalledModule im = iterator.next();
            
            /* Do not look at uninstalled modules */
            if (this.getUninstalledModule(im.getModuleInfo().getName()) == null) {
                if (im.getModuleRequires().isRequired(uniqueName) == true) {
                    return im;
                }
            }
        }
        
        /* Loop through all of the pending modules */
        Collection<PendingModule> pending = this.pendingModules.values();
        Iterator<PendingModule> iterator2 = pending.iterator();
        while (iterator2.hasNext() == true) {
            PendingModule pm = iterator2.next();
            if (pm.getModuleRequires().isRequired(uniqueName) == true) {
                return pm;
            }
        }
        
        return null;
    }
    
    /**
     * TBD
     */
    private void readAddModules() {
        /*
         * List all of the directories underneath this base level directory and
         * try to open it as a module. If successful, add it to the hash of
         * the unique module name and its Module entry.
         */
        File[] files = this.addDirectory.listFiles();
        for (File entry : files) {
            /* Check if it is a directory, if so, then skip */
            if (entry.isDirectory() == true) {
                continue;
            }
            
            /* If its suffix is not .jar or .zip then skip */
            String name = entry.getName();
            if (name.endsWith(".zip") == false && name.endsWith(".jar") == false) {
                continue;
            }
            
            /*
             * Try to open it as a module. If unable, then just skip
             */
            AddedModule module = AddedModule.getAddModule(entry);
            if (module != null) {
                this.addModules.put(module.getModuleInfo().getName(), module);
            }
        }
    }
    
    /**
     * TBD
     */
    private void readPendingModules() {
        /*
         * List all of the directories underneath this base level directory and
         * try to open it as a module. If successful, add it to the hash of
         * the unique module name and its Module entry.
         */
        File[] files = this.pendingDirectory.listFiles();
        for (File entry : files) {
            /* Check if it is a directory, if so, then skip */
            if (entry.isDirectory() == true) {
                continue;
            }
            
            /* If its suffix is not .jar or .zip then skip */
            String name = entry.getName();
            if (name.endsWith(".zip") == false && name.endsWith(".jar") == false) {
                continue;
            }
            
            /*
             * Try to open it as a module. If unable, then just skip
             */
            PendingModule module = PendingModule.getPendingModule(entry);
            if (module != null) {
                this.pendingModules.put(module.getModuleInfo().getName(), module);
            }
        }
    } 
    
    /**
     * TBD
     */
    private void readInstalledModules() {
        /*
         * List all of the directories underneath this base level directory and
         * try to open it as a module. If successful, add it to the hash of
         * the unique module name and its Module entry.
         */
        File[] files = this.installedDirectory.listFiles();
        for (File entry : files) {
            /* Check if it is a directory, if not, then skip */
            if (entry.isDirectory() == false) {
                continue;
            }
            
            /* Try to open it as a module */
            InstalledModule module = InstalledModule.getInstalledModule(entry);
            if (module != null) {
                this.installedModules.put(module.getModuleInfo().getName(), module);
            }
        }
    }
    
    /**
     * TBD
     */
    private void readRemoveModules() {
        /*
         * List all of the files underneath this base level directory, each
         * should be a version of the module.xml file. Ignore anything but
         * .xml files.
         */
        File[] files = this.removeDirectory.listFiles();
        for (File entry : files) {
            /* Check if it is a directory or does not end in .xml */
            if (entry.isDirectory() == true || entry.getName().endsWith(".xml") == false) {
                continue;
            }

            /*
             * Try to parse the file as an ModuleInfo class. If we canont, then
             * we just skip it. Otherwise, create a new RemoveModule class, with
             * just the ModuleInfo set.
             */
            try {
                ModuleInfo info = ModuleInfo.decode(new FileReader(entry));
                RemovedModule rm = new RemovedModule(entry, info);
                this.removeModules.put(info.getName(), rm);
            } catch (java.io.FileNotFoundException excp) {
                /* Log an error and simply continue to the next entry */
                logger.warning("ModuleManager: invalid xml file in remove: " + entry.getAbsolutePath());
            } catch (javax.xml.bind.JAXBException excp) {
                /* Log an error and simply continue to the next entry */
                logger.warning("ModuleManager: invalid xml file in remove: " + entry.getAbsolutePath());
            }
        }
    }
    
    /**
     * TBD
     */
    private void readUninstalledModules() {
        /*
         * List all of the files underneath this base level directory, each
         * should be a version of the module.xml file. Ignore anything but
         * .xml files.
         */
        File[] files = this.uninstallDirectory.listFiles();
        for (File entry : files) {
            /* Check if it is a directory or does not end in .xml */
            if (entry.isDirectory() == true || entry.getName().endsWith(".xml") == false) {
                continue;
            }

            /*
             * Try to parse the file as an ModuleInfo class. If we canont, then
             * we just skip it. Otherwise, create a new RemoveModule class, with
             * just the ModuleInfo set.
             */
            try {
                ModuleInfo info = ModuleInfo.decode(new FileReader(entry));
                UninstalledModule um = new UninstalledModule(entry, info);
                this.uninstallModules.put(info.getName(), um);
            } catch (java.io.FileNotFoundException excp) {
                /* Log an error and simply continue to the next entry */
                logger.warning("ModuleManager: invalid xml file in remove: " + entry.getAbsolutePath());
            } catch (javax.xml.bind.JAXBException excp) {
                /* Log an error and simply continue to the next entry */
                logger.warning("ModuleManager: invalid xml file in remove: " + entry.getAbsolutePath());
            }
        }
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
    private File getModuleRoot(String subdir) throws IOException {
        try {
            File file = new File(this.root, subdir);
            if (file.exists() == false) {
                file.mkdir();
            }
            return file;
        } catch (java.lang.SecurityException excp) {
            logger.severe("ModuleManager: Unable to create module subdir: " + subdir);
            throw new IOException(excp.toString());
        }
    }
    
    public static void main(String args[]) {
        ModuleManager mm = ModuleManager.getModuleManager();
    }
}
