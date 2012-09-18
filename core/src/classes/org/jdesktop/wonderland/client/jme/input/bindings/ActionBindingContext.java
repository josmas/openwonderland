package org.jdesktop.wonderland.client.jme.input.bindings;

import java.util.*;
import org.jdesktop.wonderland.client.jme.input.bindings.controllers.AbstractBaseController;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.ActionSPI;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.Controller;
import org.jdesktop.wonderland.client.jme.input.bindings.spi.ControllerSPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;

/**
 *
 * @author Ryan
 */
public class ActionBindingContext {

    private Map<Class, ControllerSPI> controllers;
    private Dispatcher dispatcher = null;
    private boolean initialized = false;
    
    private ActionRegistry registry = null;

    /**
     * Make constructor default so only our factory can create it.
     */
    ActionBindingContext() {
        controllers = Collections.synchronizedMap(new HashMap<Class, ControllerSPI>());
        registry = new ActionRegistry();
        
        initialize();
        
    }

    /**
     * Initializes the entire binding system: * Scans classloader for
     * ControllerSPI * Adds any ControllerSPIs to a hashmap based on class *
     * installs the global listener to listen for events of the type of the keys
     * in the keyset of the hashmap.
     */
    public void initialize() {

        if (!initialized) { //only initialize once.
            
            //scan for controller classes
            Iterator<AbstractBaseController> cs = scanForControllerClasses();

            //add controllers to a map
            addControllersToMap(cs);

            //install dispatcher
            installDispatcher();

            initialized = true;
        }
    }

    private Iterator<AbstractBaseController> scanForControllerClasses() {
        ScannedClassLoader loader = LoginManager.getPrimary().getClassloader();
        
        return loader.getInstances(Controller.class, AbstractBaseController.class);
    }

    private void addControllersToMap(Iterator<AbstractBaseController> controllerIter) {
        while (controllerIter.hasNext()) {
            AbstractBaseController controller = controllerIter.next();
            controller.setContext(this);
            controller.initialize(this);
            registerController(controller);
        }
    }

    private void installDispatcher() {
        dispatcher = new Dispatcher(controllers);
    }

    public void registerController(ControllerSPI controller) {
        synchronized (controllers) {
            controllers.put(controller.consumesEventClass(), controller);
        }
    }

    public Map<Class, ControllerSPI> getControllers() {
        return controllers;
    }

    public Set<Couple> getActionsFromControllers() {
        synchronized (controllers) {
            Set<Couple> bindings = new HashSet<Couple>();
            for (ControllerSPI controller : controllers.values()) {
                bindings.addAll(controller.getActions());
            }

            return bindings;
        }
    }

    public Binder getBinder() {
        return new Binder(this, controllers);
   }
    
    public ActionRegistry getRegistry() {
        return this.registry;
    }


    public ActionSPI getActionByName(String actionName) {
        return registry.getActionByName(actionName);
    }
}
