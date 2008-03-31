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

import com.jme.app.SimpleGame;
import com.jme.image.Texture;
import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import com.jme.scene.Text;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;

/**
 *
 * @author paulby
 */
public class HUDModule extends RenderModule {

    public static String fontLocation = "com/jme/app/defaultfont.tga";
    protected Node fpsNode;
    protected Text fps;
    
    public void init(RenderInfo info) {
      // -- FPS DISPLAY
      // First setup alpha state
        /** This allows correct blending of text and what is already rendered below it*/
      AlphaState as1 = info.getDisplay().getRenderer().createAlphaState();
      as1.setBlendEnabled(true);
      as1.setSrcFunction(AlphaState.SB_SRC_ALPHA);
      as1.setDstFunction(AlphaState.DB_ONE);
      as1.setTestEnabled(true);
      as1.setTestFunction(AlphaState.TF_GREATER);
      as1.setEnabled(true);
 
      // Now setup font texture
      TextureState font = info.getDisplay().getRenderer().createTextureState();
        /** The texture is loaded from fontLocation */
      font.setTexture(
          TextureManager.loadTexture(
          SimpleGame.class.getClassLoader().getResource(
          fontLocation),
          Texture.MM_LINEAR,
          Texture.FM_LINEAR));
      font.setEnabled(true);
 
      // Then our font Text object.
        /** This is what will actually have the text at the bottom. */
      fps = new Text("FPS label", "");
      fps.setCullMode(SceneElement.CULL_NEVER);
      fps.setTextureCombineMode(TextureState.REPLACE);
 
      // Finally, a stand alone node (not attached to root on purpose)
      fpsNode = new Node("FPS node");
      fpsNode.attachChild(fps);
      fpsNode.setRenderState(font);
      fpsNode.setRenderState(as1);
      fpsNode.setCullMode(SceneElement.CULL_NEVER);
      
      fpsNode.updateGeometricState(0.0f, true);
      fpsNode.updateRenderState();
    }

    public void update(RenderInfo info, float interpolation) {
      fps.print("FPS: " + (int) info.getTimer().getFrameRate() + " - " +
                info.getDisplay().getRenderer().getStatistics());
    }

    public void render(RenderInfo info, float interpolation) {
        info.getDisplay().getRenderer().draw(fpsNode);
    }

    @Override
    public void setActiveImpl(boolean active, RenderInfo info) {
        // nothing to do
    }

}
