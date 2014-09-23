/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.celleditor.client;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.scenemanager.event.EnterExitEvent;

/**
 *
 * manages the cursor for an object hover effect
 * 
 * @author Abhishek Upadhyay
 */
public class MouseCursorManager extends EventClassListener {

    private boolean enableGlobalCursor = false;
    private Image globalCursor = null;

    public MouseCursorManager() {
    }

    @Override
    public Class[] eventClassesToConsume() {
        return new Class[]{EnterExitEvent.class};
    }

    @Override
    public void commitEvent(Event event) {
        EnterExitEvent eee = (EnterExitEvent) event;
        Entity entity = eee.getPrimaryEntity();
        Cell cell = EnterExitEvent.getCellForEntity(entity);

        if (eee.isEnter()) {
            enter(cell);
        } else {
            exit(cell);
        }
    }

    private void enter(Cell cell) {
        if (enableGlobalCursor) {
            changeCursor(globalCursor);
        } else {
            if (cell != null) {
                NewInteractionComponent nic = cell.getComponent(NewInteractionComponent.class);
                if (nic != null && nic.isCursorEnable()) {
                    if (!nic.getCursorFilePath().equals("")) {
                        changeCursor(nic.getCursorFilePath());
                    }
                }
            }
        }
    }

    private void exit(Cell cell) {
        JmeClientMain.getFrame().getCanvas().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    private void changeCursor(String cursorFilePath) {
        Image img = null;
        Cursor cur = null;
        if (cursorFilePath.contains("wlcontent://")) {
            try {
                img = Toolkit.getDefaultToolkit().createImage(AssetUtils
                        .getAssetURL(cursorFilePath));
                cur = Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(0, 0), "new");
            } catch (MalformedURLException ex) {
                Logger.getLogger(MouseCursorManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            img = Toolkit.getDefaultToolkit().createImage(getClass()
                    .getResource(cursorFilePath));
            cur = Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(0, 0), "new");
        }
        final Cursor cursor = cur;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JmeClientMain.getFrame().getCanvas().setCursor(cursor);
            }
        });
    }

    private void changeCursor(Image image) {
        if (image != null) {
            final Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "new");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JmeClientMain.getFrame().getCanvas().setCursor(cursor);
                }
            });
        }
    }

    public boolean isEnableGlobalCursor() {
        return enableGlobalCursor;
    }

    public void enableGlobalCursor() {
        this.enableGlobalCursor = true;
    }

    public void disableGlobalCursor() {
        this.enableGlobalCursor = false;
    }

    public void setGlobalCursor(Image cursor) {
        this.globalCursor = cursor;
    }
}
