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
package org.jdesktop.wonderland.client.jme;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Manage render modules
 * 
 * @author paulby
 */
public class ModuleManager {
    private HashMap<RenderModule, ModuleState> modules = new HashMap();
    private WonderlandJmeClient client;
    private JMenu moduleMenu=null;
    
    public ModuleManager(WonderlandJmeClient client) {
        this.client = client;
    }
    
    public synchronized void addModule(RenderModule module, boolean active) {
        ModuleState ms = new ModuleState(module, active);
        modules.put(module, ms);
        
        if (active) {
            client.addModule(module);
        } 
        
        if (moduleMenu!=null) {
            moduleMenu.add(ms.getMenuItem());            
        }
    }
    
    synchronized void setState(RenderModule module, boolean active) {
        ModuleState currentState = modules.get(module);
        
        if (active) {
            client.addModule(module);
        } else {
            client.removeModule(module);
        }
        currentState.active = active;
    }
    
    public synchronized JMenu getModuleMenu() {
        if (moduleMenu==null)
            createModuleMenu();
        return moduleMenu;
    }
    
    private void createModuleMenu() {
        moduleMenu = new JMenu("Modules");
        
        for(ModuleState ms : modules.values())
            moduleMenu.add(ms.getMenuItem());
    }
    
    class ModuleState {
        private RenderModule module;
        private boolean active;
        private JMenuItem menuItem=null;
        
        public ModuleState(RenderModule module, boolean active) {
            this.module = module;
            this.active = active;
        }
        
        public JMenuItem getMenuItem() {
            if (menuItem==null) {
                String name = module.getClass().getName();
                menuItem = new JCheckBoxMenuItem(name.substring(name.lastIndexOf('.')+1), active);
                menuItem.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        active = menuItem.isSelected();
                        setState(module, active);
                    }
                });
            }
            
            return menuItem;
        }
    }
}
