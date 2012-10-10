/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.hudx;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.hudx.SPI.ViewSPI;
import org.jdesktop.wonderland.client.hudx.annotations.View;
import org.jdesktop.wonderland.client.utils.Observer;

/**
 *
 * @author Ryan
 */
public abstract class BaseController implements Observer {
    
    protected HUDComponent hudComponent;
    protected ViewSPI view;
    
    public BaseController() {
        
        initializeViewField();
        
        initialize();
    }
    
    public HUD hud() {
        return HUDManagerFactory.getHUDManager().getHUD("main");
    }
    
    public void open() {
        SwingUtilities.invokeLater(new Runnable() { 
            public void run() {
                hudComponent.setVisible(true);
            }
        });
    }
    
    public void close() {
        SwingUtilities.invokeLater(new Runnable() { 
            public void run() {
                hudComponent.setVisible(false);
            }
        });
    }
    
    
    /**
     * Find first annotated field with annotation @ControlledView. Set our view
     * variable to that field;
     */
    protected void initializeViewField() {
        Class self = this.getClass();
        
        View annotation = (View) self.getAnnotation(View.class);
        if(annotation != null) {
            try {
                
                //instantiate the view
                view = (ViewSPI) annotation.view().getConstructor().newInstance();
                
                //register this controller as an observer to the view
                view.addObserver(this);
                
            } catch (InstantiationException ex) {
                Logger.getLogger(BaseController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(BaseController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(BaseController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(BaseController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(BaseController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } else {
            throw new RuntimeException("CONTROLLER CLASS MUST BE ANNOTATED WITH @VIEW!");
        }
        
    }
            
    
    
    protected abstract void initialize();
    
    protected abstract void cleanup();
    
}
