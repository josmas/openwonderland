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
package org.jdesktop.wonderland.client.jme;

import imi.scene.JScene;
import imi.utils.input.InputScheme;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.DefaultScheme;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Logger;
import javolution.util.FastList;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.wonderland.client.ClientContext;
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
public class AvatarControls extends ProcessorComponent implements JSceneEventProcessor {
    private static final Logger logger =
            Logger.getLogger(AvatarControls.class.getName());

    private JScene      m_jscene = null;
    private InputScheme m_scheme = new DefaultScheme();
    private FastList<InputScheme> m_schemeList = new FastList<InputScheme>();
    private final LinkedList<Event> events = new LinkedList();

    private HashSet<Integer> pressedKeys = new HashSet();
    private long postId = ClientContextJME.getWorldManager().allocateEvent();

    private final AvatarEventListener eventListener = new AvatarEventListener();

    public AvatarControls() {
    }

    @Override
    public void compute(ProcessorArmingCollection arg0) {
        synchronized(events) {
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
    }

    @Override
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);

        logger.fine("[AvatarControls] " + this + " enabled: " + enable);

        if (enable) {
            ClientContext.getInputManager().addGlobalEventListener(eventListener);
            ClientContextJME.getWorldManager().addUserData(JSceneEventProcessor.class, this);
        } else {
            ClientContext.getInputManager().removeGlobalEventListener(eventListener);

            // todo -- remove user data
            ClientContextJME.getWorldManager();
        }
    }

    /**
     * Process KEY_PRESSED and KEY_RELEASED.
     * Note KEY_TYPED events are not passed to this method
     * @param ke
     */
    private void processKeyEvent(KeyEvent ke) {
        int index = 0;
        
        if (ke.getID() == KeyEvent.KEY_PRESSED) 
        {
            // Smooth normals toggle
            if (ke.getKeyCode() == KeyEvent.VK_ADD) 
            {
                index = m_schemeList.indexOf(m_scheme);
                index++;
                if (index > m_schemeList.size()-1)
                    m_scheme = m_schemeList.get(0);
                else
                    m_scheme = m_schemeList.get(index);
                
                m_scheme.setJScene(m_jscene);
            }
            
            // Toggle PRenderer mesh display
            if (ke.getKeyCode() == KeyEvent.VK_SUBTRACT)
            {
                index = m_schemeList.indexOf(m_scheme);
                index--;
                if (index < 0)
                    m_scheme = m_schemeList.get(m_schemeList.size()-1);
                else
                    m_scheme = m_schemeList.get(index);   
                
                m_scheme.setJScene(m_jscene);
            }
        }
    }
    
    @Override
    public void commit(ProcessorArmingCollection arg0) {
        
    }

    @Override
    public void initialize() {
        logger.fine("******** AvatarControls init");
//        setArmingCondition(new PostEventCondition(this, new long[]{postId}));
        setArmingCondition(new NewFrameCondition(this));

        // Run in the renderer to guarantee that the avatar move and camera
        // are updated together
        setRunInRenderer(true);
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
    
    class AvatarEventListener extends EventClassFocusListener {
        @Override
        public Class[] eventClassesToConsume () {
            return new Class[] { KeyEvent3D.class, MouseEvent3D.class };
        }

        @Override
        public void commitEvent (Event event) {
//            System.out.println("evt " +event);
            synchronized(events) {
                events.add(event);
            }
//            ClientContextJME.getWorldManager().postEvent(postId);
        }
        
    }

    public interface AvatarInputSelector {
        public void selectForInput(boolean selected);
    }

    public interface AvatarActionTrigger {
        public void trigger(int trigger, boolean pressed, String animationName);
    }
}
