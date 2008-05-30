/*
 *      Creates several Actors with a few shared meshes and move them individually 
 */

package test;

import com.jme.app.SimpleGame;
import com.jme.math.Vector3f;
import com.jme.scene.SharedMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import imi.actors.Actor;
import imi.utils.MsgBox;

/**
 *
 * @author Lou Hayt
 */
public class ActorTest extends SimpleGame 
{  
    public static void main(String[] args) 
    {
        ActorTest app = new ActorTest(); // Create Object
    
        // Signal to show properties dialog
            app.setDialogBehaviour(SimpleGame.ALWAYS_SHOW_PROPS_DIALOG);
            app.start(); // Start the program 
    }

    protected void simpleInitGame() 
    {
        display.setTitle("jME - IMI - Actor Test");
        
        Actor dude;      
        SharedMesh mesh;
	Sphere sphere = new Sphere("sphere", 20, 20, 2.0f);
        Box box = new Box("box", new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f));
        
        //  Spheres
        
        dude = new Actor("dude1", sphere);
        mesh = dude.getSharedMesh();
        mesh.setLocalTranslation(0.0f, 0.0f, 0.0f);
        rootNode.attachChild(dude);
        
        dude = new Actor("dude2", sphere);
        mesh = dude.getSharedMesh();
        mesh.setLocalTranslation(0.0f, 5.0f, 0.0f);
        rootNode.attachChild(dude);
        
        dude = new Actor("dude3", sphere);
        mesh = dude.getSharedMesh();
        mesh.setLocalTranslation(5.0f, 0.0f, 0.0f);
        rootNode.attachChild(dude);
        
        dude = new Actor("dude4", sphere);
        mesh = dude.getSharedMesh();
        mesh.setLocalTranslation(0.0f, 0.0f, 5.0f);
        rootNode.attachChild(dude);
        
        //  Boxes
        
        dude = new Actor("dude5", box);
        mesh = dude.getSharedMesh();
        mesh.setLocalTranslation(10.0f, 10.0f, 10.0f);
        rootNode.attachChild(dude);
        
        dude = new Actor("dude6", box);
        mesh = dude.getSharedMesh();
        mesh.setLocalTranslation(-10.0f, -10.0f, -10.0f);
        rootNode.attachChild(dude);
       
        
        
//        // Message box test -   several messages
//        MsgBox error = new MsgBox();
//        error.addMessage("message1");
//        error.addMessage("message2");
//        error.show();
//        // Message box test -    single quick  message
//        error = new MsgBox("single line error");
        
        
    }
    
}
