/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import java.util.*;
import com.jme.app.SimpleGame;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.*;
import com.jme.scene.shape.Box;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import jmetest.renderer.*;

import imi.loaders.rtg.*;
import imi.animations.articulatedanimation.*;
import imi.animations.articulatedanimation.instance.*;
import imi.actors.*;

import imi.loaders.AssetManager;


    
    

/**
 * <code>TestBox</code>
 * @author Mark Powell
 * @version $Id: TestBox.java,v 1.7 2007/05/04 10:02:02 rherlitz Exp $
 */
public class Main_Chris extends SimpleGame
{
    static public SimpleGame ms_pInstance = null;
  
    ArrayList m_Animations = new ArrayList();

  
  
    static public SimpleGame getInstance()
    {
        return(ms_pInstance);
    }
    
    /**
      * Entry point for the test,
      * @param args
      */
    public static void main(String[] args)
    {
        Main_Chris app = new Main_Chris();
        app.setDialogBehaviour(FIRSTRUN_OR_NOCONFIGFILE_SHOW_PROPS_DIALOG);
        app.start();
    }
  
    public Main_Chris()
    {
        ms_pInstance = this;

    }
  
  
    public boolean loadAnimationFile(String animationFilename)
    {
        RtgLoader loader = new RtgLoader();
        boolean bResult;
        ArticulatedAnimation pAnimation;

        try
        {
            bResult = loader.load(animationFilename);

            pAnimation = loader.getArticulatedAnimation();

            m_Animations.add(pAnimation);

            return(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return(false);
    }

    protected Actor createAnimationInstance(int AnimationIndex, float fx, float fy, float fz)
    {
        Actor pAnimationActor = new Actor();
        ArticulatedAnimation pAnimation = (ArticulatedAnimation)m_Animations.get(AnimationIndex);

        pAnimationActor.setArticulatedAnimation(pAnimation);
    
        pAnimationActor.setLocalTranslation(fx, fy, fz);

        rootNode.attachChild(pAnimationActor);
        
        return(pAnimationActor);
    }

    protected void simpleInitGame()
    {
        //  Initialize the AssetManager.
        AssetManager.init(display);
            

        display.setTitle("Repeating Texture");
        lightState.setEnabled(false);
    
        buildTestScene(rootNode);

/*
        Box floor = new Box("Floor", new Vector3f(), 100, 1, 100); 
        floor.setModelBound(new BoundingBox()); 
        floor.updateModelBound(); 
        floor.getLocalTranslation().y = -20; 
        TextureState ts = display.getRenderer().createTextureState();
        Texture t0 = TextureManager.loadTexture(TestEnvMap.class.getClassLoader().getResource("jmetest/data/images/Monkey.jpg"),
                                                Texture.MM_LINEAR_LINEAR,
                                                Texture.FM_LINEAR);
        t0.setWrap(Texture.WM_WRAP_S_WRAP_T);
        ts.setTexture(t0);
        floor.setRenderState(ts); 
        floor.getBatch(0).scaleTextureCoordinates(0, 5);


        rootNode.attachChild(floor); 
*/

        loadAnimationFile("assets/models/people/ChrisM_Listen.rtg");
        loadAnimationFile("assets/models/people/Bob_Chatter.rtg.gz");
        loadAnimationFile("assets/models/people/Bob_Listen.rtg.gz");
        loadAnimationFile("assets/models/people/ChrisM_Listen.rtg");
        loadAnimationFile("assets/models/people/DaveD_Chatter.rtg.gz");
        loadAnimationFile("assets/models/people/Derek_Chatter.rtg.gz");
        loadAnimationFile("assets/models/people/Derek_Listen.rtg.gz");
        loadAnimationFile("assets/models/people/Female_Chatter.rtg.gz");
        loadAnimationFile("assets/models/people/Female_Conf1.rtg.gz");
        loadAnimationFile("assets/models/people/Female_Idle.rtg.gz");
        loadAnimationFile("assets/models/people/Female_Rig.rtg.gz");
        loadAnimationFile("assets/models/people/Female_Walk.rtg");
        loadAnimationFile("assets/models/people/Greg_Idle.rtg.gz");
        loadAnimationFile("assets/models/people/JeffK_Chatter.rtg.gz");
        loadAnimationFile("assets/models/people/Jenn_Chatter.rtg.gz");
        loadAnimationFile("assets/models/people/Jenn_Listen.rtg.gz");
        loadAnimationFile("assets/models/people/Jen_Chatter.rtg.gz");
        loadAnimationFile("assets/models/people/JF_Chatter.rtg.gz");
        loadAnimationFile("assets/models/people/Joe_Chatter.rtg.gz");
        loadAnimationFile("assets/models/people/Male_Conf_1.rtg.gz");
        loadAnimationFile("assets/models/people/Male_Walk.rtg.gz");
        loadAnimationFile("assets/models/people/Nancy_Chatter.rtg.gz");
        loadAnimationFile("assets/models/people/Nicole_Chatter.rtg.gz");
        loadAnimationFile("assets/models/people/Onlooker1_Idle.rtg.gz");
        loadAnimationFile("assets/models/people/PaulL_Chatter.rtg.gz");
        loadAnimationFile("assets/models/people/SimN_Chatter.rtg.gz");


//        createAnimationInstance(0, -20.0f, 0.0f, 0.0f);
//        createAnimationInstance(1, 0.0f, 0.0f, 0.0f);
//        createAnimationInstance(0, 20.0f, 0.0f, 0.0f);

        int a;
        float fx = -20.0f;
        for (a=0; a<m_Animations.size(); a++)
        {
            createAnimationInstance(a, fx, -13.0f, 0.0f);

            fx += 3.0f;
        }
        
        
/*
        int a;
        Actor pAnimationActor;
        float fx = -20.0f;
    
        for (a=0; a<10; a++)
        {
            //  Create an Actor that contains an Animation.
            pAnimationActor = new Actor();
            pAnimationActor.setArticulatedAnimation(m_pAnimation);
    
            pAnimationActor.setLocalTranslation(fx, 0.0f, 0.0f);
        
            fx += 5.0f;

            rootNode.attachChild(pAnimationActor);
        }
*/


        AxisActor pAxisActor = new AxisActor();
        
        pAxisActor.initialize();
        
        rootNode.attachChild(pAxisActor);
    }


    void buildTestScene(Node pRootNode)
    {
        Node pBoxNode = new Node();
        Box  pFloor = null;
        Node pRedNode = new Node();
        Box  pRedBox = null;
             

        pBoxNode.getLocalTranslation().y = -15;
        pFloor = new Box("Floor", new Vector3f(), 100, 1, 100); 
        pFloor.setModelBound(new BoundingBox()); 
        pFloor.updateModelBound(); 
        //pFloor.getLocalTranslation().y = -20; 
        TextureState ts = display.getRenderer().createTextureState();
        //Base texture, not environmental map.
        Texture t0 = TextureManager.loadTexture("assets/textures/largecheckerboard.png",
                                                Texture.MM_LINEAR_LINEAR,
                                                Texture.FM_LINEAR);
        t0.setWrap(Texture.WM_WRAP_S_WRAP_T);
        ts.setTexture(t0);
        pFloor.setRenderState(ts); 
        pFloor.getBatch(0).scaleTextureCoordinates(0, 10);
    
        pBoxNode.attachChild(pFloor);
    
        
        
        String curDir = System.getProperty("user.dir");

        
/*
        pRedNode.getLocalTranslation().x = 10.0f;
        
        pRedBox = new Box("Box", new Vector3f(), 5, 5, 5); 
        pRedBox.setModelBound(new BoundingBox()); 
        pRedBox.updateModelBound(); 
        //pFloor.getLocalTranslation().y = -20; 
        TextureState redts = display.getRenderer().createTextureState();
        //Base texture, not environmental map.
        Texture redt0 = TextureManager.loadTexture("assets/textures/red.png",
                                                   Texture.MM_LINEAR_LINEAR,
                                                   Texture.FM_LINEAR);
        redt0.setWrap(Texture.WM_WRAP_S_WRAP_T);
        redts.setTexture(redt0);
        pRedBox.setRenderState(redts); 
        pRedBox.getBatch(0).scaleTextureCoordinates(0, 5);
    
        pRedNode.attachChild(pRedBox);

        pBoxNode.attachChild(pRedNode);
*/

        pRootNode.attachChild(pBoxNode);
    }

}




