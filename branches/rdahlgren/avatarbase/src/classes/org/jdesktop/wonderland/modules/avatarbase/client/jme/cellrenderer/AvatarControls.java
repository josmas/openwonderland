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
package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;

import org.jdesktop.wonderland.client.jme.*;
import imi.scene.JScene;
import imi.utils.input.InputScheme;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.DefaultScheme;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Logger;
import javolution.util.FastList;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.jme.input.InputEvent3D;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;

/**
 * An AvatarControl using the control scheme from the avatars project
 *
 * @author paulby
 */
public class AvatarControls extends ViewControls implements JSceneEventProcessor {
    private static final Logger logger =
            Logger.getLogger(AvatarControls.class.getName());

    private JScene      m_jscene = null;
    private InputScheme m_scheme = new DefaultScheme();
    private FastList<InputScheme> m_schemeList = new FastList<InputScheme>();
    private final LinkedList<Event> events = new LinkedList();

    private HashSet<Integer> pressedKeys = new HashSet();

    private final AvatarEventListener eventListener = new AvatarEventListener();

    private Cell viewCell = null;

    private boolean enable = false;

    public AvatarControls() {
    }

    @Override
    public void compute(ProcessorArmingCollection arg0) {
            LinkedList eventAwt = new LinkedList();

            for (Event evt : events) {
                if (evt instanceof KeyEvent3D) {
                    // Strip out KEY_PRESSED caused by auto repeat and ignore KEY_TYPED
                    KeyEvent ke = (KeyEvent) ((KeyEvent3D)evt).getAwtEvent();
//                    if (ke.getID()==KeyEvent.KEY_PRESSED) {
//                        if (!pressedKeys.contains(ke.getKeyCode())) {
//                            pressedKeys.add(ke.getKeyCode());
                            processKeyEvent(ke);
                            eventAwt.add(ke);
//                        }
//                    } else if (ke.getID()==KeyEvent.KEY_RELEASED) {
//                        pressedKeys.remove(ke.getKeyCode());
//                        processKeyEvent(ke);
//                        eventAwt.add(ke);
//                    }
                } else {
                    eventAwt.add(((InputEvent3D)evt).getAwtEvent());
                }
            }

//            System.err.println("Sending events "+m_scheme+" "+eventAwt.size());
            m_scheme.processEvents(eventAwt.toArray());
            events.clear();
    }

    @Override
    public void setEnabled(boolean enable) {
        if (this.enable==enable)
            return;

        super.setEnabled(enable);

        logger.fine("[AvatarControls] " + this + " enabled: " + enable);

        if (enable) {
            ClientContext.getInputManager().addGlobalEventListener(eventListener);
            // register the avatar controls with the world manager
            ClientContextJME.getWorldManager().addUserData(JSceneEventProcessor.class, this);
            ((AvatarCell)viewCell).setSelectedForInput(enable);
        } else {
            ((AvatarCell)viewCell).setSelectedForInput(enable);
            ClientContext.getInputManager().removeGlobalEventListener(eventListener);
            ClientContextJME.getWorldManager().removeUserData(JSceneEventProcessor.class);
        }

        this.enable = enable;
    }

    /**
     * Process KEY_PRESSED and KEY_RELEASED.
     * Note KEY_TYPED events are not passed to this method
     * @param ke
     */
    private void processKeyEvent(KeyEvent ke) {
//        int index = 0;
//
//        if (ke.getID() == KeyEvent.KEY_PRESSED)
//        {
//            // Smooth normals toggle
//            if (ke.getKeyCode() == KeyEvent.VK_ADD)
//            {
//                index = m_schemeList.indexOf(m_scheme);
//                index++;
//                if (index > m_schemeList.size()-1)
//                    m_scheme = m_schemeList.get(0);
//                else
//                    m_scheme = m_schemeList.get(index);
//
//                m_scheme.setJScene(m_jscene);
//            }
//
//            // Toggle PRenderer mesh display
//            if (ke.getKeyCode() == KeyEvent.VK_SUBTRACT)
//            {
//                index = m_schemeList.indexOf(m_scheme);
//                index--;
//                if (index < 0)
//                    m_scheme = m_schemeList.get(m_schemeList.size()-1);
//                else
//                    m_scheme = m_schemeList.get(index);
//
//                m_scheme.setJScene(m_jscene);
//            }
//        }
    }
    
    @Override
    public void commit(ProcessorArmingCollection arg0) {
        
    }

    @Override
    public void initialize() {
        // Chain with the eventListener so we exectue after, but in the same frame
        eventListener.addToChain(this);
    }

    public void clearSchemes()
    {
        logger.fine("[AvatarControls] clear schemes on " + this);

        m_scheme = m_schemeList.get(0);
        m_schemeList.clear();
        m_schemeList.add(m_scheme);
    }
    
    public InputScheme setDefault(InputScheme defaultScheme) 
    {
        logger.fine("[AvatarControls] set scheme " + defaultScheme +
                    " on " + this.hashCode());
        
        m_scheme = defaultScheme;
        m_schemeList.clear();
        m_schemeList.add(m_scheme);
        return m_scheme;
    }
    
    public void addScheme(InputScheme scheme)
    {
        logger.fine("[AvatarControls] add scheme " + scheme + " to " + this);

        m_schemeList.add(scheme);
    }

    public JScene getJScene() 
    {
        return m_jscene;
    }

    public void setJScene(JScene jscene) 
    {
        logger.fine("[AvatarControls] set scene to " + jscene + " on " + this);

        m_jscene = jscene;
        m_scheme.setJScene(jscene);
    }
    
    public InputScheme getInputScheme()
    {
        return m_scheme;
    }

    public void attach(Cell cell) {
        this.viewCell = cell;
    }
    
    class AvatarEventListener extends EventClassFocusListener {
        @Override
        public Class[] eventClassesToConsume () {
            return new Class[] { KeyEvent3D.class, MouseEvent3D.class };
        }

        @Override
        public void computeEvent (Event event) {
//            System.out.println("evt " +event);
            // Access to events does not need to be synchronised as the commit
            // is guaranteed to happen after this computeEvent becuase the processors
            // are chained
            events.add(event);
        }
        
    }
}
