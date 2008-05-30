/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.animations.articulatedanimation;

import java.net.URL;
import com.jme.math.*;
import com.jme.image.*;
import com.jme.util.*;
import com.jme.scene.*;
import com.jme.util.geom.*;

import imi.loaders.*;
import com.jme.scene.state.*;
import com.jme.renderer.*;



public class ArticulatedAnimationGeometry
{
    private String m_Name = "";

    private ArticulatedAnimationMaterial m_AnimationMaterial = null;
    private TriMesh m_TriMesh = null;
    private MaterialState m_MaterialState = null;



    //  Constructor.
    public ArticulatedAnimationGeometry()
    {
    }
	public ArticulatedAnimationGeometry(String name)
    {
        m_Name = name;
    }

    
    
    //  Gets the name.
    public String getName()
    {
        return(m_Name);
    }
    
    //  Sets the name.
    public void setName(String name)
    {
        m_Name = name;
    }
    


    //  Gets the MaterialState.
    public MaterialState getMaterialState()
    {
        return(m_MaterialState);
    }



    //  Gets the AnimationMaterial.
    public ArticulatedAnimationMaterial getAnimationMaterial()
    {
        return(m_AnimationMaterial);
    }
    //  Sets the AnimationMaterial.
    public void setAnimationMaterial(ArticulatedAnimationMaterial pAnimationMaterial)
    {
        m_AnimationMaterial = pAnimationMaterial;
    }
    


    //  Gets the TriMesh.
    public TriMesh getTriMesh()
    {
        return(m_TriMesh);
    }
    //  Set the TriMesh.
    public void setTriMesh(TriMesh pTriMesh)
    {
        m_TriMesh = pTriMesh;

        if (m_TriMesh != null)
            SetupTriMeshRenderState();
    }
    
    //  Populates the TriMesh.
    public void populateTriMesh(MeshBuffer pMeshBuffer)
    {
        m_TriMesh = new TriMesh();
        
        
        //  Get the arrays of data.
        Vector3f[] pPositions = pMeshBuffer.getPositions();
        Vector3f[] pNormals = pMeshBuffer.getNormals();
        Vector2f[] pTexCoords = pMeshBuffer.getTexCoords();
        int[] pTriangleIndices = pMeshBuffer.getIndices();

        // Feed the information to the TriMesh
        m_TriMesh.reconstruct(BufferUtils.createFloatBuffer(pPositions),
                              BufferUtils.createFloatBuffer(pNormals),
                              null,
                              BufferUtils.createFloatBuffer(pTexCoords),
                              BufferUtils.createIntBuffer(pTriangleIndices));

        SetupTriMeshRenderState();
    }

    private void SetupTriMeshRenderState()
    {
        if (m_AnimationMaterial != null)
        {
            //  Get the filename of the Texture for the Material.
            String textureFilename = m_AnimationMaterial.getTextureFilename();
            if (textureFilename.length() > 0)
            {
                Texture pTexture = AssetManager.getTexture(textureFilename);
                if (pTexture == null)
                {
                    System.out.println("Error:  Unable to load texture '" + textureFilename + "'!");
                    return;
                }
                else
                {
                    //  Create a TextureState.
                    TextureState textureState = AssetManager.createTextureState();

                    textureState.setTexture(pTexture);

                    m_TriMesh.setRenderState(textureState);
                }
            }
            else
            {
                ColorRGBA AmbientColor = m_AnimationMaterial.getAmbient();
                ColorRGBA DiffuseColor = m_AnimationMaterial.getDiffuse();
                ColorRGBA SpecularColor = m_AnimationMaterial.getSpecular();
                ColorRGBA EmissiveColor = m_AnimationMaterial.getEmission();
                float fShininess = m_AnimationMaterial.getShininess();
                float fTransparency = m_AnimationMaterial.getTransparency();

                m_MaterialState = AssetManager.createMaterialState();

                m_MaterialState.setAmbient(AmbientColor);
                m_MaterialState.setDiffuse(DiffuseColor);
                m_MaterialState.setEmissive(EmissiveColor);
                m_MaterialState.setSpecular(SpecularColor);
                m_MaterialState.setShininess(fShininess);

                m_TriMesh.setRenderState(m_MaterialState);
            }
        }
    }
    
}





