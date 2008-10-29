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

package org.jdesktop.wonderland.web.help.deployer;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.help.HelpInfo;
import org.jdesktop.wonderland.modules.Module;
import org.jdesktop.wonderland.modules.ModulePart;
import org.jdesktop.wonderland.modules.spi.ModuleDeployerSPI;
import org.jdesktop.wonderland.web.help.HelpContent;
import org.jdesktop.wonderland.web.help.HelpLayout;

/**
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class HelpDeployer implements ModuleDeployerSPI {

    /* Holds map of deployed help module name  and root Files for assets */
    private static Map<String, File> helpMap = new HashMap();
    
    /* A map of help category names and a list of help entries */
    private static Map<String, List<DeployedHelpContent>> contentMap = new HashMap();
    
    /* The layout of the help system, to be populated by entries */
    private static HelpLayout helpLayout = null;
    
    /* The error logger */
    private static Logger logger = Logger.getLogger(HelpDeployer.class.getName());
    
    /**
     * Container for the HelpMenuContent for a content item and the name of the
     * module from which it came. This wrapper is necessary to remove it upon
     * undeploy
     */
    private static class DeployedHelpContent {
        public String moduleName = null;
        public HelpContent.HelpMenuContent content = null;
        
        /** Constructor, takes the args */
        public DeployedHelpContent(String moduleName, HelpContent.HelpMenuContent content) {
            this.moduleName = moduleName;
            this.content = content;
        }
    }
    
    /** Default constructor */
    public HelpDeployer() {
    }

    /**
     * Returns (a copy of) a map of module assets to their File roots.
     */
    public static Map<String, File> getFileMap() {
        return new HashMap(helpMap);
    }
    
    public String getName() {
        return "Help Deployer";
    }

    public String[] getTypes() {
        return new String[] { "help" };
    }

    public boolean isDeployable(String type, Module module, ModulePart part) {
        /* Help is always deployable */
        return true;
    }

    public boolean isUndeployable(String type, Module module, ModulePart part) {
        /* Help is always undeployable */
        return true;
    }

    public void deploy(String type, Module module, ModulePart part) {
        /*
         * Check to see if the module has a layout.xml file and parse it and
         * use it. 
         */
        File layoutFile = new File(part.getFile(), "layout.xml");
        if (layoutFile.exists() == true && layoutFile.canRead() == true) {
            try {
                helpLayout = HelpLayout.decode(new FileReader(layoutFile));
            } catch (Exception excp) {
                logger.log(Level.WARNING, "[HELP] Cannot parse layout.xml", excp);
            }
        }
        
        /*
         * Check to see if the module has a content.xml file and parse it and
         * put each category into the map.
         */
        File contentFile = new File(part.getFile(), "content.xml");
        if (contentFile.exists() == true && contentFile.canRead() == true) {
            try {
                HelpContent content = HelpContent.decode(new FileReader(contentFile));
                HelpContent.HelpMenuContent entries[] = content.getHelpContent();
                if (entries != null) {
                    for (HelpContent.HelpMenuContent entry : entries) {
                        this.addContent(module.getName(), entry);
                    }
                }
            } catch (Exception excp) {
                logger.log(Level.WARNING, "[HELP] Cannot parse content.xml", excp);
            }
        }
        
        /* Add the File to the map so we can find the html files */
        helpMap.put(module.getName(), part.getFile());
    }

    public void undeploy(String type, Module module, ModulePart part) {
        /* Find all of the content entries in the map and remove */
        Iterator<String> it = contentMap.keySet().iterator();
        while (it.hasNext() == true) {
            this.removeContent(module.getName(), it.next());
        }
        
        /* Remove from the file map */
        helpMap.remove(module.getName());
    }
    
    /**
     * Adds a content entry to the proper place in the map
     */
    private void addContent(String moduleName, HelpContent.HelpMenuContent content) {
        List<DeployedHelpContent> list = contentMap.get(content.category);
        if (list == null) {
            list = new LinkedList<DeployedHelpContent>();
            contentMap.put(content.category, list);
        }
        list.add(new DeployedHelpContent(moduleName, content));
    }
    
    /**
     * Removes all entries belonging to a category given the module name
     */
    private void removeContent(String moduleName, String categoryName) {
        List<DeployedHelpContent> list = contentMap.get(categoryName);
        if (list == null) {
            return;
        }
        ListIterator<DeployedHelpContent> it = list.listIterator();
        while (it.hasNext() == true) {
            DeployedHelpContent h = it.next();
            if (h.moduleName.equals(moduleName) == true) {
                it.remove();
            }
        }
    }
    
    /**
     * Builds a HelpInfo class based upon the layout and content information
     * loaded. This substitutes category entries into their place in the menu
     * layout tree.
     */
    public static HelpInfo buildHelpInfo() {
        HelpInfo info = new HelpInfo();
        List<HelpInfo.HelpMenuEntry> entryList = new LinkedList();
        
        /* If the help layout does not exist, just return an empty object */
        if (helpLayout == null) {
            return info;
        }
        
        /* Loop through the layout recursively and populate with content */
        if (helpLayout.getHelpEntries() != null) {
            for (HelpInfo.HelpMenuEntry entry : helpLayout.getHelpEntries()) {
                HelpInfo.HelpMenuEntry[] children = HelpDeployer.buildHelpEntries(entry);
                for (HelpInfo.HelpMenuEntry child : children) {
                    entryList.add(child);
                }
            }
        }
        
        /* Populate the elements of the HelpInfo object and return */
        info.setHelpEntries(entryList.toArray(new HelpInfo.HelpMenuEntry[] {}));
        return info;
    }
    
    /**
     * Takes a HelpInfo.HelpMenuEntry item and recursively descents and fills
     * in the content based upon the layout and categories defined and the
     * content for those categories. Returns an array of HelpMenuEntry[] items
     * that are inserted into a place in the tree.
     */
    private static HelpInfo.HelpMenuEntry[] buildHelpEntries(HelpInfo.HelpMenuEntry entry) {
        if (entry instanceof HelpInfo.HelpMenuFolder) {
            HelpInfo.HelpMenuFolder helpFolder = (HelpInfo.HelpMenuFolder)entry;
            List<HelpInfo.HelpMenuEntry> childList = new LinkedList();
            
            /* Create a new folder, return as an array and recurse */
            if (helpFolder.entries != null) {
                for (HelpInfo.HelpMenuEntry folderEntry : helpFolder.entries) {
                    HelpInfo.HelpMenuEntry[] children = HelpDeployer.buildHelpEntries(folderEntry);
                    for (HelpInfo.HelpMenuEntry child : children) {
                        childList.add(child);
                    }
                }
            }
            HelpInfo.HelpMenuEntry[] childArray = childList.toArray(new HelpInfo.HelpMenuEntry[] {});
            String folderName = helpFolder.name;
            HelpInfo.HelpMenuFolder folder = new HelpInfo.HelpMenuFolder(folderName, childArray);
            
            return new HelpInfo.HelpMenuEntry[] { folder };
        }
        else if (entry instanceof HelpInfo.HelpMenuItem) {
            /* Create a new item, return as an array */
            HelpInfo.HelpMenuItem helpItem = (HelpInfo.HelpMenuItem)entry;
            String itemName = helpItem.name;
            String itemURI = helpItem.helpURI;
            HelpInfo.HelpMenuItem item = new HelpInfo.HelpMenuItem(itemName, itemURI);
            return new HelpInfo.HelpMenuEntry[] { item };
        }
        else if (entry instanceof HelpInfo.HelpMenuCategory) {
            /* Fill in the entries with the category return as array */
            HelpInfo.HelpMenuCategory helpCategory = (HelpInfo.HelpMenuCategory)entry;
            String categoryName = helpCategory.name;
            return HelpDeployer.getEntriesByCategory(categoryName);
        }
        else if (entry instanceof HelpInfo.HelpMenuSeparator) {
            /* Create a new separator, return as an array */
            HelpInfo.HelpMenuSeparator separator = new HelpInfo.HelpMenuSeparator();
            return new HelpInfo.HelpMenuEntry[] { separator };
        }
        return new HelpInfo.HelpMenuEntry[] {} ;
    }
    
    /**
     * Given a category name, generate an array of HelpMenuEntry objects for
     * all deployed content. This simply converts from the contentMap into
     * an array
     */
    private static HelpInfo.HelpMenuEntry[] getEntriesByCategory(String category) {
        List<HelpInfo.HelpMenuEntry> list = new LinkedList();
        
        /* Fetch the list of deployed contents for the category */
        List<DeployedHelpContent> deployedHelpContents = contentMap.get(category);
        if (deployedHelpContents == null) {
            return list.toArray(new HelpInfo.HelpMenuEntry[] {});
        }
        
        /* Loop through each category entry we find, add to master list */
        ListIterator<DeployedHelpContent> it = deployedHelpContents.listIterator();
        while (it.hasNext() == true) {
            HelpContent.HelpMenuContent entry = it.next().content;
            if (entry.entries != null) {
                for (HelpInfo.HelpMenuEntry content : entry.entries) {
                    list.add(content);
                }
            }
        }
        return list.toArray(new HelpInfo.HelpMenuEntry[] {});
    }
}
