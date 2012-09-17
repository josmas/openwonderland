
package org.jdesktop.wonderland.client.SPI;

import java.awt.event.ActionListener;
import java.net.URL;

/**
 *
 * @author JagWire
 */
public interface QuickLaunchSPI extends ActionListener {
    
    /**
     * get URL pointing to an image to represent this action in a quicklaunch bar
     * 
     * @return 
     */
    public URL getImageURL();
    
    
}
