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
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.client.cell;

import com.jme.bounding.BoundingBox;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * A Component that provides scripting interface 
 * 
 * @author morrisford
 */
@ExperimentalAPI
public class ScriptingComponent extends CellComponent 
    {
    private Node localNode;
    private String scriptClump;
    private String scriptURL;
    private String scriptExt;
    private String scriptType;
    public String stateString[] = {null,null,null,null,null,null,null,null,null,null};
    public int stateInt[] = {0,0,0,0,0,0,0,0,0,0};
    public boolean stateBoolean[] = {false,false,false,false,false,false,false,false,false,false};
    public float stateFloat[] = {0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f};
    Map<String, CompiledScript> scriptMap = new HashMap<String, CompiledScript>();
//    myThread mth = new myThread();
    private ArrayList aniList;
    private int aniFrame = 0;
    private int aniLast = 0;
    private CellTransform atRest;
    private int animation = 0;
    public String testName = "morrisford";
    public int testInt = 99;
    
    public ScriptingComponent(Cell cell) 
	{
        super(cell);
        }
    
    public void testMethod(String ibid)
    {
        System.out.println("ibid = " + ibid);
    }
    
    public void setStateString(String value, int which)
        {
        stateString[which] = value;
        }
    
    public String getStateString(int which)
        {
        return stateString[which];
        }
    
    public void setStateInt(int value, int which)
        {
        stateInt[which] = value;
        }
    
    public int getStateInt(int which)
        {
        return stateInt[which];
        }
    
    public void setStateFloat(float value, int which)
        {
        stateFloat[which] = value;
        }
    
    public float getStateFloat(int which)
        {
        return stateFloat[which];
        }
    
    public void setStateBoolean(boolean value, int which)
        {
        stateBoolean[which] = value;
        }
    
    public boolean getStateBoolean(int which)
        {
        return stateBoolean[which];
        }
    
    
    public String getName()
    {
        return testName;
    }
    
    public void putName(String theName)
    {
    testName = theName;    
    }
    
    public void executeScript(String scriptName, Node node, String Clump, String Ext, String Type, String URL)
       {
       System.out.println("Start of executeScript - this = " + this);
       localNode = node;
       scriptClump = Clump;
       scriptExt = Ext;
       scriptType = Type;
       scriptURL = URL;
       
       stateString[0] = "Morris - state string 0";
       
// This line is required to start up scripting
       try
           {
           String thePath = buildScriptPath(scriptName);
           System.out.println("ScriptingCell *** scriptPath = " + thePath);
           
           ScriptEngineManager engineManager = new ScriptEngineManager();
           ScriptEngine jsEngine = engineManager.getEngineByName(scriptType);  
           Bindings bindings = jsEngine.createBindings();
           
// This line passes 'this' instance over to the script
//           bindings.put("CommThread", mth);
           bindings.put("MyClass", this);
           bindings.put("stateString", stateString);
           bindings.put("stateInt", stateInt);
           bindings.put("stateBoolean", stateBoolean);
           bindings.put("stateFloat", stateFloat);
           bindings.put("name", testName);
           bindings.put("testInt", testInt);
           
           if(jsEngine instanceof Compilable)
               {
               CompiledScript  theScript = scriptMap.get(scriptName);
               if(theScript == null)
                   {
                   Compilable compilingEngine = (Compilable)jsEngine;
                   URL myURL = new URL(thePath);
                   BufferedReader in = new BufferedReader(new InputStreamReader(myURL.openStream()));
                   theScript = compilingEngine.compile(in);
                   scriptMap.put(scriptName, theScript);
                   }
               else
                   {
                   System.out.println("Script " + scriptName + " was already compiled");
                   }
               theScript.eval(bindings);
               }
           else
               {
               System.out.println("Not compilable - " + scriptName);
               URL myURL = new URL(thePath);
               BufferedReader in = new BufferedReader(new InputStreamReader(myURL.openStream()));
               jsEngine.eval(in, bindings);
               }
           System.out.println("stateString[0] = " + stateString[0] + " name = " + testName + " test int = " + testInt);
           }
       catch(ScriptException ex)
           {
           System.out.println("Script exception " + ex);
           ex.printStackTrace();
           }
       catch(Exception e)
           {
           System.out.println("General exception in script stuff" + e);
           e.printStackTrace();
           }
       }
    
    private String buildScriptPath(String theScript)
        {
        System.out.println("cell name = " + scriptClump);                        
        String thePath = scriptURL + "/scripts/" + scriptClump + "/" + theScript + "." + scriptExt;
        return thePath;
        }
    
    public void getInitialPosition()
        {
        Vector3f v3f = localNode.getLocalTranslation();
        stateFloat[0] = v3f.x;
        stateFloat[1] = v3f.y;
        stateFloat[2] = v3f.z;
        }

    public void setTranslation(float x, float y, float z)
        {
//        System.out.println("In setTranslation - node = " + node);
        Vector3f v3f = localNode.getLocalTranslation();
//        System.out.println("ScriptingCell *** Start of setTranslation - Original translation = " + v3f);
        Vector3f v3fn = new Vector3f(x, y, z);
//        System.out.println("ScriptingCell *** in setTranslation - New translation = " + v3fn);
        localNode.setLocalTranslation(v3fn);
        localNode.setModelBound(new BoundingBox());
        localNode.updateModelBound();
        ClientContextJME.getWorldManager().addToUpdateList(localNode);
        }

    public void setRotation(float x, float y, float z, float w)
        {
        Quaternion orig = localNode.getLocalRotation();
  //      System.out.println("ScriptingCell *** In setRotation - Original rotation = " + orig);
        Quaternion roll = new Quaternion();
        roll.fromAngleAxis( w , new Vector3f(x, y, z) );
//        System.out.println("ScriptingCell *** In setRotation - New rotation = " + roll);
        localNode.setLocalRotation(roll);
        localNode.setModelBound(new BoundingBox());
        localNode.updateModelBound();
        ClientContextJME.getWorldManager().addToUpdateList(localNode);
        }
 
    public void setScale(float x, float y, float z)
        {
        Vector3f orig = localNode.getLocalScale();
//        System.out.println("ScriptingCell *** In setScale - Original scale = " + orig);
        Vector3f scale = new Vector3f(x, y, z);
//        System.out.println("ScriptingCell *** In setScale - New scale = " + scale);
        localNode.setLocalScale(scale);
        localNode.setModelBound(new BoundingBox());
        localNode.updateModelBound();
        ClientContextJME.getWorldManager().addToUpdateList(localNode);
        }
 
    public void moveObject(float x, float y, float z)
        {
        Vector3f v3f = localNode.getLocalTranslation();
//        System.out.println("ScriptingCell *** In moveObject - Original translation = " + v3f);
        float X = v3f.x;
        float Y = v3f.y;
        float Z = v3f.z;
        Vector3f v3fn = new Vector3f(X + x, Y + y, Z + z);
//        System.out.println("ScriptingCell *** In moveObject - Original translation = " + v3fn);
        localNode.setLocalTranslation(v3fn);
        localNode.setModelBound(new BoundingBox());
        localNode.updateModelBound();
        ClientContextJME.getWorldManager().addToUpdateList(localNode);
        }

    public void rotateObject(float x, float y, float z, float w)
        {
        Quaternion orig = localNode.getLocalRotation();
//        System.out.println("ScriptingCell *** In rotateObject - Original rotation = " + orig);

        Quaternion roll = new Quaternion();
        roll.fromAngleAxis( w , new Vector3f(x, y, z) );
//        System.out.println("ScriptingCell *** In rotateObject - Change rotation = " + roll);
        Quaternion sum = orig.add(roll);
//        System.out.println("ScriptingCell *** In rotateObject - Sum rotation = " + sum);
        localNode.setLocalRotation(sum);
        localNode.setModelBound(new BoundingBox());
        localNode.updateModelBound();
        ClientContextJME.getWorldManager().addToUpdateList(localNode);
        }
 
    public void scaleObject(float x, float y, float z)
        {
        Vector3f orig = localNode.getLocalScale();
//        System.out.println("ScriptingCell *** In setScale - Original scale = " + orig);
        Vector3f scale = new Vector3f(x, y, z);
//        System.out.println("ScriptingCell *** In setScale - Change scale = " + scale);
        Vector3f sum = orig.add(scale);
//        System.out.println("ScriptingCell *** In setScale - Final scale = " + sum);
        localNode.setLocalScale(sum);
        localNode.setModelBound(new BoundingBox());
        localNode.updateModelBound();
        ClientContextJME.getWorldManager().addToUpdateList(localNode);
        }

    public void mySleep(int milliseconds)
        {
        try
            {
            Thread.sleep(milliseconds);
            }
        catch(Exception e)
            {
            System.out.println("Sleep exception");
            }
       }
    public ArrayList buildAnimation(String animationName) 
        {
        String line;
        aniList = new ArrayList();
        String thePath = buildScriptPath(animationName);
        System.out.println("Load animation -> " + thePath);
        try
            {
            URL myURL = new URL(thePath);
            BufferedReader in = new BufferedReader(new InputStreamReader(myURL.openStream()));
            while((line = in.readLine()) != null)
                {
                aniLast++;
                String[] result = line.split(",");
                Animation ani = new Animation();
                ani.xLoc = new Float(result[0]).floatValue();
                ani.yLoc = new Float(result[1]).floatValue();
                ani.zLoc = new Float(result[2]).floatValue();
                ani.xAxis = new Float(result[3]).floatValue();
                ani.yAxis = new Float(result[4]).floatValue();
                ani.zAxis = new Float(result[5]).floatValue();
                ani.rot = new Float(result[6]).floatValue();
                
                ani.rest = new String(result[7]);
                aniList.add(ani);
                }
            }
        catch(Exception e)
            {
            System.out.println("Exception reading " + thePath);
            e.printStackTrace();
            }
        aniFrame = 0;
        return aniList;
        }
    class expired extends TimerTask
        {
        public void run()
            {
            System.out.println("Expired");
            if(aniFrame < aniLast || animation == 0)
                executeScript("timer", localNode, scriptClump, scriptExt, scriptType, scriptURL);
            }
        }
    
    public void startTimer(int timeValue, int Animation)
        {
        animation = Animation;
        System.out.println("Start timer");
        Timer timer = new Timer();
        timer.schedule(new expired(), timeValue);
        }

    class Animation
        {
        public  float   xLoc;
        public  float   yLoc;
        public  float   zLoc;
        public  float   rot;
        public  float   xAxis;
        public  float   yAxis;
        public  float   zAxis;
        public  String  rest;
        }

    public int playAnimationFrame()
        {
        System.out.println("Print frame " + aniFrame + " of " + aniLast);
        doTransform(((Animation)aniList.get(aniFrame)).xLoc,
                ((Animation)aniList.get(aniFrame)).yLoc,
                ((Animation)aniList.get(aniFrame)).zLoc,
                ((Animation)aniList.get(aniFrame)).xAxis,
                ((Animation)aniList.get(aniFrame)).yAxis,
                ((Animation)aniList.get(aniFrame)).zAxis,
                ((Animation)aniList.get(aniFrame)).rot);

        if(((Animation)aniList.get(aniFrame)).rest.equals("r"))
            {
            restoreRest();
            }
        else if(((Animation)aniList.get(aniFrame)).rest.equals("n"))
            {
// Send the current transform off to the server to let other clients know that the script wants them to know where we are
            doNotify();
            }
        aniFrame++;
        if(aniFrame >= aniLast)
            {
            aniList.clear();
            aniFrame = 0;
            aniLast = 0;
// Notify other clients at the end of an animation
//            doNotify();
            return 0;
            }
        else
            {
            return 1;
            }
        }

    public void doTransform(double x, double y, double z, double rot, double xAxis, double yAxis, double zAxis)
        {
        System.out.println("The parms " + x + "," + y + "," + z + "," + rot + "," + xAxis + "," + yAxis + "," + zAxis);
        setTranslation((float)x, (float)y, (float)z);
        setRotation((float)xAxis, (float)yAxis, (float)zAxis, (float)rot);
        }
    
    public void doNotify()
        {
/*        CellTransform transform = this.getLocalTransform();
        SimpleTerrainCellMHFChangeMessage newMsg = new SimpleTerrainCellMHFChangeMessage(getCellID(), SimpleTerrainCellMHFChangeMessage.MESSAGE_CODE_3, SimpleTerrainCellMHFChangeMessage.MESSAGE_TRANSFORM);
        Matrix4d m4d = new Matrix4d();
        transform.get(m4d);
        newMsg.setTransformMatrix(m4d);
        ChannelController.getController().sendMessage(newMsg);
 */
        }

       public void restoreRest()
       {
/*       Quaternion rot = atRest.getRotation(null);
       Vector3f trans = atRest.getTranslation(null);
       Vector3f scale = atRest.getScaling(null);
       node.setLocalRotation(rot);
       node.setLocalTranslation(trans);
       node.setLocalScale(scale);
 */
       }
   

    }
