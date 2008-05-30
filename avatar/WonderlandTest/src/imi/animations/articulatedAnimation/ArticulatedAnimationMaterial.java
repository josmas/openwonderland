/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.animations.articulatedanimation;

import java.net.URL;
import com.jme.math.*;
import com.jme.image.*;
import com.jme.util.*;
import com.jme.renderer.*;

import test.TriMeshTest;




public class ArticulatedAnimationMaterial
{
    private String m_Name = "";
    private ColorRGBA m_Ambient = new ColorRGBA();
    private ColorRGBA m_Diffuse = new ColorRGBA();
    private ColorRGBA m_Specular = new ColorRGBA();
    private ColorRGBA m_Emission = new ColorRGBA();
    private float m_fShininess = 0.0f;
    private float m_fTransparency = 0.0f;

    private String m_TextureFilename = "";

    private Texture m_Texture = null;



    //  Constructor.
	public ArticulatedAnimationMaterial()
    {
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

    
    
    //  Gets the AmbientColor.
    public ColorRGBA getAmbient()
    {
        return(m_Ambient);
    }
    //  Sets the AmbientColor.
    public void setAmbient(ColorRGBA pAmbient)
    {
        m_Ambient = pAmbient;
    }

    
    
    //  Gets the DiffuseColor.
    public ColorRGBA getDiffuse()
    {
        return(m_Diffuse);
    }
    //  Sets the DiffuseColor.
    public void setDiffuse(ColorRGBA pDiffuse)
    {
        m_Diffuse = pDiffuse;
    }

    

    //  Gets the SpecularColor.
    public ColorRGBA getSpecular()
    {
        return(m_Specular);
    }
    //  Sets the SpecularColor.
    public void setSpecular(ColorRGBA pSpecular)
    {
        m_Specular = pSpecular;
    }    
    

    
    //  Gets the EmissionColor.
    public ColorRGBA getEmission()
    {
        return(m_Emission);
    }
    //  Sets the EmissionColor.
    public void setEmission(ColorRGBA pEmission)
    {
        m_Emission = pEmission;
    }
    

    
    //  Gets the Shininess.
    public float getShininess()
    {
        return(m_fShininess);
    }
    //  Sets the Shininess.
    public void setShininess(float fShininess)
    {
        m_fShininess = fShininess;
    }
    


    //  Gets the Transparency.
    public float getTransparency()
    {
        return(m_fTransparency);
    }
    //  Sets the Transparency.
    public void setTransparency(float fTransparency)
    {
        m_fTransparency = fTransparency;
    }

    
    
    //  Gets the TextureFilename.
    public String getTextureFilename()
    {
        return(m_TextureFilename);
    }
    //  Sets the TextureFilename.
    public void setTextureFilename(String textureFilename)
    {
        m_TextureFilename = textureFilename;
    }
    

    
    //  Gets the Texture.
    public Texture getTexture()
    {
        return(m_Texture);
    }
 
    //  Loads the Texture.
    public boolean loadTexture()
    {
        if (m_TextureFilename.length() > 0)
        {
            URL textureURL = TriMeshTest.class.getClassLoader().getResource(m_TextureFilename);

            m_Texture = TextureManager.loadTexture(textureURL, Texture.MM_LINEAR, Texture.FM_LINEAR);
            if (m_Texture != null)
                return(true);
        }

        return(false);
    }
}
