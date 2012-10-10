/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.hudx;

import org.jdesktop.wonderland.client.hudx.annotations.View;
import org.jdesktop.wonderland.client.hud.CompassLayout;

/**
 *
 * @author Ryan
 */
@View(view = SimpleView.class)
public class SimpleController extends BaseController {

//    @ControlledView
//    protected ViewSPI view;
    @Override
    protected void initialize() {
        view.addObserver(this);

        this.hudComponent = hud().createComponent(view.getJPanel());
        hudComponent.setDecoratable(true);
        hudComponent.setName("Simple");
        hudComponent.setPreferredLocation(CompassLayout.Layout.CENTER);


        hud().addComponent(hudComponent);
    }

    @Override
    protected void cleanup() {
    }

    public void eventObserved(String property, Object value) {
        if (property.equals("ok-action")) {
            handleOKAction();
        } else if (property.equals("cancel-action")) {
            handleCancelAction();
        } else if(property.equals("name-action")) {
            handleNameChangedAction();
        } else if(property.equals("password-action")) {
            handlePasswordChangedAction();
        }
    }

    private void handleOKAction() {
        //delegate form execution here.
        close();
    }

    private void handleCancelAction() {
        close();
    }
    
    private void handleNameChangedAction() {
        //add business logic here
    }
    
    private void handlePasswordChangedAction() {
        //add business logic here
    }
}
