/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.jme.input.bindings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.ActionSPI;

/**
 *
 * @author Ryan
 */
public class ActionRegistry {

    private Map<String, ActionSPI> actions = new LinkedHashMap<String, ActionSPI>();

    public ActionSPI getActionByName(String name) {
        return actions.get(name);
    }

    public Set<String> getActionNames() {
        return actions.keySet();
    }

    /*
     * For KeyStroke bindings....
     */
    public void registerAction(ActionSPI action, String name) {
        synchronized (actions) {
            actions.put(name, action);
        }
    }

    /**
     * For mouse button bindings
     *
     * @param awo
     * @param name
     */
    public void registerAction(ActionWrapperObject awo, String name) {
    }

    public void unregisterAction(String name) {
        synchronized (actions) {
            actions.remove(name);
        }
    }
}
