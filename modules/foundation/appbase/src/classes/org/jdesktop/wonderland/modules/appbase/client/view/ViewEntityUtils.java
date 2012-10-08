/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.appbase.client.view;

/**
 *
 * @author Ryan
 */
public class ViewEntityUtils {

    protected static final int CHANGED_TYPE = 0x0001;
    protected static final int CHANGED_PARENT = 0x0002;
    protected static final int CHANGED_VISIBLE = 0x0004;
    protected static final int CHANGED_DECORATED = 0x0008;
    protected static final int CHANGED_GEOMETRY = 0x0010;
    protected static final int CHANGED_SIZE_APP = 0x0020;
    protected static final int CHANGED_PIXEL_SCALE = 0x0040;
    protected static final int CHANGED_OFFSET = 0x0080;
    protected static final int CHANGED_USER_TRANSFORM = 0x0100;
    protected static final int CHANGED_TITLE = 0x0200;
    protected static final int CHANGED_STACK = 0x0400;
    protected static final int CHANGED_ORTHO = 0x0800;
    protected static final int CHANGED_LOCATION_ORTHO = 0x1000;
    protected static final int CHANGED_TEX_COORDS = 0x2000;
    protected static final int CHANGED_USER_RESIZABLE = 0x4000;

    protected static enum AttachState {

        DETACHED,
        ATTACHED_TO_ENTITY,
        ATTACHED_TO_WORLD
    };

    protected static enum FrameChange {
        ATTACH_FRAME,
        DETACH_FRAME,
        REATTACH_FRAME,
        UPDATE_TITLE,
        UPDATE_USER_RESIZABLE
    };
}
