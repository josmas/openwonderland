/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.ClientContext;

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
//    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final int MIN_SIZE = 2;
    private static final int MAX_SIZE = 10;

    private final BlockingQueue<HeaderPanel> headers =
            new LinkedBlockingQueue<HeaderPanel>();

    // track whether there is currently a task allocating headers
    private boolean allocating = false;

    HeaderPanelAllocator () {
        scheduleTask();
    }

    // Note: this can still block, but the preallocated headers should make this less likely.
    HeaderPanel allocate() throws InterruptedException {
        // if the queue is getting empty, make sure to start refilling it
        if (headers.size() <= MIN_SIZE) {
            scheduleTask();
        }

        return headers.take();
    }

    void deallocate (HeaderPanel header) {
        if (headers.size() >= MAX_SIZE) return;
        headers.add(header);
    }

    private void scheduleTask() {
        if (!isAllocating()) {
            ClientContext.getGlobalExecutor().submit(this);
        }
    }

    private HeaderPanel createHeader () {
        final HeaderPanelHolder out = new HeaderPanelHolder();
        
        try {
            SwingUtilities.invokeAndWait(new Runnable () {
                public void run () {
                    out.result = new HeaderPanel();
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return out.result;
    }

    public void run () {
        setAllocating(true);

        try {
            while (headers.size() < MAX_SIZE) {
                headers.add(createHeader());
            }
        } finally {
            setAllocating(false);
        }
    }
    
    private synchronized boolean isAllocating() {
        return allocating;
    }
    
    private synchronized void setAllocating(boolean allocating) {
        this.allocating = allocating;
    }

    class HeaderPanelHolder {
        HeaderPanel result;
    }
}
