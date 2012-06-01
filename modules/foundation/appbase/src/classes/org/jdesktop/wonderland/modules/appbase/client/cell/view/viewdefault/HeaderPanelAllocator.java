/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */

/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.appbase.client.cell.view.viewdefault;

import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.SwingUtilities;

/**
 * This class reduces the number of times that a new Swing header needs
 * to be created. This greatly minimizes the potential for some deadlocks
 * which still exist in the code. 
 * <br><br>
 * TODO: need to clean this up by single threading the app base.
 *
 * @author deronj
 */
class HeaderPanelAllocator implements Runnable {

    private static final int SIZE = 10;

    private Thread thread;
    private LinkedBlockingQueue<HeaderPanel> headers = new LinkedBlockingQueue<HeaderPanel>();

    HeaderPanelAllocator () {
        initialize();

        thread = new Thread(this, "HeaderPanel Allocator Thread");
        thread.start();
    }

    private void initialize () {
        for (int i = 0; i < SIZE; i++) {
            HeaderPanel header = createHeader();
            try {
                headers.put(header);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // Note: this can still block, but the preallocated headers should make this less likely.
    HeaderPanel allocate () {
        if (headers.size() > 0) {
            try {
                return headers.take();
            } catch (InterruptedException ex) {
            }
        }

        // Note: this may block
        return createHeader();
    }

    void deallocate (HeaderPanel header) {
        if (headers.size() >= SIZE) return;
        try {
            headers.put(header);
        } catch (InterruptedException ex) {
        }
    }

    private HeaderPanel createHeader () {
        final HeaderHolder holder = new HeaderHolder();
        
        try {
            SwingUtilities.invokeAndWait(new Runnable () {
                public void run () {
                    synchronized (holder) {
                        holder.panel = new HeaderPanel();
                    }
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        synchronized (holder) {
            return holder.panel;
        }
    }

    public void run () {
        // This thread runs forever
        while (true) {
            if (headers.size() < SIZE) {
                HeaderPanel header = createHeader();
                try {
                    headers.put(header);
                } catch (InterruptedException ex) {
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }
    
    private static class HeaderHolder {
        public HeaderPanel panel;
    }
}
