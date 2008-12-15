/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
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

    private JScene      m_jscene = null;
    private InputScheme m_scheme = new DefaultScheme();
    private FastList<InputScheme> m_schemeList = new FastList<InputScheme>();
    private final LinkedList<Event> events = new LinkedList();

    private HashSet<Integer> pressedKeys = new HashSet();
    
    
    public AvatarControls() {
        AvatarEventListener listener = new AvatarEventListener();
        ClientContext.getInputManager().addGlobalEventListener(listener);
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
            
            m_scheme.processEvents(eventAwt.toArray());
            events.clear();
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
        setArmingCondition(new NewFrameCondition(this));

        // Run in the renderer to guarantee that the avatar move and camera
        // are updated together
        setRunInRenderer(true);
    }

    public void clearSchemes()
    {
        m_scheme = m_schemeList.get(0);
        m_schemeList.clear();
        m_schemeList.add(m_scheme);
    }
    
    public InputScheme setDefault(InputScheme defaultScheme) 
    {
        m_scheme = defaultScheme;
        m_schemeList.clear();
        m_schemeList.add(m_scheme);
        return m_scheme;
    }
    
    public void addScheme(InputScheme scheme)
    {
        m_schemeList.add(scheme);
    }

    public JScene getJScene() 
    {
        return m_jscene;
    }

    public void setJScene(JScene jscene) 
    {
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
        }
        
    }

    public interface AvatarInputSelector {
        public void selectForInput();
    }
}
