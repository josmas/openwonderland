/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.hudx;

import javax.swing.JPanel;
import org.jdesktop.wonderland.client.hudx.annotations.View;
import org.jdesktop.wonderland.client.cell.ModelCell;
import org.jdesktop.wonderland.client.hud.CompassLayout;
import org.jdesktop.wonderland.client.hudx.SPI.ViewSPI;
import org.jdesktop.wonderland.client.hudx.annotations.ControlledView;
import org.jdesktop.wonderland.client.hudx.SimpleView;
/**
 *
 * @author Ryan
 */
@View(view=SimpleView.class)
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
        if(property.equals("ok-action")) {
            handleOKAction();
        } else if(property.equals("cancel-action")) {
            handleCancelAction();
    }
    }

    @Override
    protected boolean openOnStartup() {
        return false;
    }

    private void handleOKAction() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleCancelAction() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
}
