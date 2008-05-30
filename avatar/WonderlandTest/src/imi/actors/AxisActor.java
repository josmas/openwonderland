/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.actors;


import com.jme.scene.*;
import com.jme.scene.shape.Box;
import com.jme.scene.state.TextureState;
import com.jme.math.Vector3f;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.util.*;

import imi.loaders.*;



public class AxisActor extends Node
{
    private Node m_pOriginNode;
    private Node m_pXTipNode;
    private Node m_pYTipNode;
    private Node m_pZTipNode;

    private float m_fAngle = 0.0f;
    private float m_fMinLength = 7.0f;
    private float m_fMaxLength = 20.0f;
    private float m_fLengthSpeed = 40.0f;
    private float m_fLength = m_fMinLength;
    private int m_Direction = 1;



    //  Constructor.
    public AxisActor()
    {
        
    }

    
    public void initialize()
    {
        buildAxisNodes(60.0f, 10.0f, 20.0f, 10.0f, 2.0f);     
    }
    
    
    
    public void updateGeometricState(float time, boolean initiator)
    {
        update(time);

        super.updateGeometricState(time, initiator);
    }
    
    //  Updates the AnimationInstance.
    public void update(float fDeltaTime)
    {
        m_fAngle += fDeltaTime * 60.0f;
        if (m_fAngle >= 360.0f)
            m_fAngle -= 360.0f;

        m_pOriginNode.getLocalRotation().fromAngles(0.0f, m_fAngle * 3.141592f / 180.0f, 0.0f);

        m_fLength += (fDeltaTime * (float)m_Direction) * m_fLengthSpeed;
        
        if (m_Direction > 0)
        {
            if (m_fLength >= m_fMaxLength)
            {
                m_fLength = m_fMaxLength - (m_fLength - m_fMaxLength);
                m_Direction = -1;
            }
        }
        else if (m_Direction < 0)
        {
            if (m_fLength <= m_fMinLength)
            {
                m_fLength = m_fMinLength + (m_fMinLength - m_fLength);
                m_Direction = 1;
            }
        }
        
        m_pXTipNode.getLocalTranslation().x = m_fLength; 
        m_pYTipNode.getLocalTranslation().y = m_fLength; 
        m_pZTipNode.getLocalTranslation().z = m_fLength; 
    }

    void buildAxisNodes(float fOriginX, float fOriginY, float fOriginZ, float fLength, float fBoxWidth)
    {
        m_pOriginNode = buildBoxNode(fOriginX, fOriginY, fOriginZ, fBoxWidth, "assets/textures/checkerboard.png");
        m_pXTipNode = buildBoxNode(fLength, 0.0f, 0.0f, fBoxWidth, "assets/textures/red.png"); 
        m_pYTipNode = buildBoxNode(0.0f, fLength, 0.0f, fBoxWidth, "assets/textures/green.png");
        m_pZTipNode = buildBoxNode(0.0f, 0.0f, fLength, fBoxWidth, "assets/textures/blue.png"); 

        m_pOriginNode.attachChild(m_pXTipNode);
        m_pOriginNode.attachChild(m_pYTipNode);
        m_pOriginNode.attachChild(m_pZTipNode);

        this.attachChild(m_pOriginNode);
    }

    
    public Node buildBoxNode(float fx, float fy, float fz, float fBoxWidth,
                             String textureFilename)
    {
        Node pBoxNode = new Node();
        Box  pBox = null;

        pBoxNode.setLocalTranslation(fx, fy, fz);

        pBox = new Box("Floor", new Vector3f(), fBoxWidth, fBoxWidth, fBoxWidth); 
        pBox.setModelBound(new BoundingBox()); 
        pBox.updateModelBound(); 

        //pFloor.getLocalTranslation().y = -20; 
        TextureState ts = AssetManager.createTextureState();
        //Base texture, not environmental map.
        Texture t0 = TextureManager.loadTexture(textureFilename,
                                                Texture.MM_LINEAR_LINEAR,
                                                Texture.FM_LINEAR);
        t0.setWrap(Texture.WM_WRAP_S_WRAP_T);
        ts.setTexture(t0);
        pBox.setRenderState(ts); 
    
        pBoxNode.attachChild(pBox);
        
        return(pBoxNode);
    }
    
    
}



