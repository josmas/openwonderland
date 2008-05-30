/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.loaders;


import java.util.*;
import com.jme.image.*;
import com.jme.util.*;
import com.jme.system.*;
import com.jme.scene.state.*;




class TextureEntry
{
    public Texture m_pTexture = null;
    public int m_ReferenceCount = 0;
}

public class AssetManager
{
    static private DisplaySystem m_Display = null;

    //  Contains all the Loaded Textures.
    static private ArrayList m_Textures = new ArrayList();

    
    static private String m_AssetPath = "assets";
    
    static private String m_TexturePath = "assets/textures";
    
    
    
    
    //  Constructor.
    private AssetManager()
    {
    }
    
    
    //  Initializes the AssetManager.
    static public void init(DisplaySystem display)
    {
        m_Display = display;
    }

    

    //  Creates a TextureState.
    static public TextureState createTextureState()
    {
        return(m_Display.getRenderer().createTextureState());
    }

    //  Creates a MaterialState.
    static public MaterialState createMaterialState()
    {
        return(m_Display.getRenderer().createMaterialState());
    }





    //  *********************************
    //  Texture management methods.
    //  *********************************

    //  Gets a Texture.
    //  If it's not already loaded, it is loaded and added to the internal list.
    static public Texture getTexture(String filename)
    {
        String currentDirectory = System.getProperty("user.dir");
        
//        System.out.println("Current Directory:  " + currentDirectory);
        
        
        TextureEntry pTextureEntry = findTexture(filename);
        if (pTextureEntry == null)
            pTextureEntry = loadTexture(filename);

        if (pTextureEntry != null)
        {
            pTextureEntry.m_ReferenceCount++;
            return(pTextureEntry.m_pTexture);
        }

        return(null);
    }

    //  Releases a Texture.
    //  If it's no longer referenced, it is deleted.
    static public boolean releaseTexture(Texture pTexture)
    {
        if (pTexture == null)
            return(false);

        TextureEntry pTextureEntry = findTexture(pTexture.getImageLocation());
        if (pTextureEntry != null)
        {
            pTextureEntry.m_ReferenceCount--;
            
            if (pTextureEntry.m_ReferenceCount <= 1)
            {
                m_Textures.remove(pTextureEntry);
                return(true);
            }
        }

        return(false);
    }

    //  Attemps to find the Texture with the filename.
    static private TextureEntry findTexture(String filename)
    {
        int a;
        TextureEntry pTextureEntry;
        
        for (a=0; a<m_Textures.size(); a++)
        {
            pTextureEntry = (TextureEntry)m_Textures.get(a);
            
            if (pTextureEntry.m_pTexture.getImageLocation().equals(filename))
                return(pTextureEntry);
        }

        return(null);
    }
    
    //  Loads a new Texture.
    static private TextureEntry loadTexture(String filename)
    {
        Texture pTexture;
        String textureFilename = m_TexturePath + "/" + filename;

//        System.out.println("TextureFilename:  " + textureFilename);

        pTexture = TextureManager.loadTexture(textureFilename, Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);   
        if (pTexture == null)
            return(null);

        pTexture.setWrap(Texture.WM_WRAP_S_WRAP_T);
        pTexture.setImageLocation(filename);

        TextureEntry pTextureEntry = new TextureEntry();
        pTextureEntry.m_pTexture = pTexture;
        pTextureEntry.m_ReferenceCount = 1;
        
        m_Textures.add(pTextureEntry);
        
        return(pTextureEntry);
    }


    
    
    
}

