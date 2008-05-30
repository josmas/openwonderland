
/**
 * Testing a TriMesh - programatically build and display
 * 
 * @author Lou Hayt
 */

package test;

import imi.loaders.MeshBuffer;
import com.jme.app.SimpleGame;
import com.jme.scene.TriMesh;
import com.jme.math.Vector3f;
import com.jme.math.Vector2f;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.jme.util.geom.BufferUtils;
import jmetest.renderer.TestBoxColor;

public class TriMeshTest extends SimpleGame {

    public static void main(String[] args) {

        TriMeshTest app = new TriMeshTest(); // Create Object

        // Signal to show properties dialog
        app.setDialogBehaviour(SimpleGame.ALWAYS_SHOW_PROPS_DIALOG);
        app.start(); // Start the program
    }

    protected void simpleInitGame() {

        TriMesh m = new TriMesh("My Mesh");

        MeshBuffer mb = new MeshBuffer();
        int index1, index2, index3;

        //0 v 0 1 2 n 0 1 2 t 0 0 0 

        // first vertex
        index1 = mb.addPosition(new Vector3f(0.020623f, 0.015098f, -0.059735f));
        mb.addNormal(new Vector3f(-0.391551f, 0.247565f, -0.886228f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // secound vertex
        index2 = mb.addPosition(new Vector3f(-0.011066f, 0.007605f, -0.039921f));
        mb.addNormal(new Vector3f(-0.430489f, 0.506037f, -0.747400f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // third vertex
        index3 = mb.addPosition(new Vector3f(0.018833f, 0.034845f, -0.021558f));
        mb.addNormal(new Vector3f(-0.269007f, 0.948483f, -0.167374f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // first triangle
        mb.addTriangle(index1, index2, index3);


        ////////////   1 v 1 3 2 n 1 3 2 t 0 0 0 

        // first vertex
        index1 = mb.addPosition(new Vector3f(-0.011066f, 0.007605f, -0.039921f));
        mb.addNormal(new Vector3f(-0.430489f, 0.506037f, -0.747400f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // secound vertex
        index2 = mb.addPosition(new Vector3f(-0.024113f, 0.018773f, -0.025694f));
        mb.addNormal(new Vector3f(-0.316101f, 0.911975f, -0.261501f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // third vertex
        index3 = mb.addPosition(new Vector3f(0.018833f, 0.034845f, -0.021558f));
        mb.addNormal(new Vector3f(-0.269007f, 0.948483f, -0.167374f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // secound triangle
        mb.addTriangle(index1, index2, index3);

        //2 v 3 4 2 n 3 4 2 t 0 0 0 


        // first vertex
        index1 = mb.addPosition(new Vector3f(-0.024113f, 0.018773f, -0.025694f));
        mb.addNormal(new Vector3f(-0.316101f, 0.911975f, -0.261501f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // secound vertex
        index2 = mb.addPosition(new Vector3f(-0.030257f, 0.012426f, -0.011985f));
        mb.addNormal(new Vector3f(-0.464299f, 0.681883f, 0.565211f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // third vertex
        index3 = mb.addPosition(new Vector3f(0.018833f, 0.034845f, -0.021558f));
        mb.addNormal(new Vector3f(-0.269007f, 0.948483f, -0.167374f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // third triangle
        mb.addTriangle(index1, index2, index3);

//3 v 4 5 2 n 4 5 2 t 0 0 0 


        // first vertex
        index1 = mb.addPosition(new Vector3f(-0.030257f, 0.012426f, -0.011985f));
        mb.addNormal(new Vector3f(-0.464299f, 0.681883f, 0.565211f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // secound vertex
        index2 = mb.addPosition(new Vector3f(0.021407f, 0.019674f, 0.018777f));
        mb.addNormal(new Vector3f(-0.298481f, 0.569384f, 0.765970f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // third vertex
        index3 = mb.addPosition(new Vector3f(0.018833f, 0.034845f, -0.021558f));
        mb.addNormal(new Vector3f(-0.269007f, 0.948483f, -0.167374f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // triangle index 3
        mb.addTriangle(index1, index2, index3);

//4 v 6 5 7 n 6 5 7 t 0 0 0 


        // first vertex
        index1 = mb.addPosition(new Vector3f(0.025770f, -0.015243f, 0.020934f));
        mb.addNormal(new Vector3f(-0.154317f, -0.508830f, 0.846923f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // secound vertex
        index2 = mb.addPosition(new Vector3f(0.021407f, 0.019674f, 0.018777f));
        mb.addNormal(new Vector3f(-0.298481f, 0.569384f, 0.765970f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // third vertex
        index3 = mb.addPosition(new Vector3f(-0.028052f, -0.005245f, -0.010815f));
        mb.addNormal(new Vector3f(-0.484901f, -0.440214f, 0.755700f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // triangle index 4
        mb.addTriangle(index1, index2, index3);


//5 v 4 7 5 n 4 7 5 t 0 0 0 

        // first vertex
        index1 = mb.addPosition(new Vector3f(-0.030257f, 0.012426f, -0.011985f));
        mb.addNormal(new Vector3f(-0.464299f, 0.681883f, 0.565211f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // secound vertex
        index2 = mb.addPosition(new Vector3f(-0.028052f, -0.005245f, -0.010815f));
        mb.addNormal(new Vector3f(-0.484901f, -0.440214f, 0.755700f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // third vertex
        index3 = mb.addPosition(new Vector3f(0.021407f, 0.019674f, 0.018777f));
        mb.addNormal(new Vector3f(-0.298481f, 0.569384f, 0.765970f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // triangle index 5
        mb.addTriangle(index1, index2, index3);





//6 v 6 7 8 n 6 7 8 t 0 0 0 

        // first vertex
        index1 = mb.addPosition(new Vector3f(0.025770f, -0.015243f, 0.020934f));
        mb.addNormal(new Vector3f(-0.154317f, -0.508830f, 0.846923f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // secound vertex
        index2 = mb.addPosition(new Vector3f(-0.028052f, -0.005245f, -0.010815f));
        mb.addNormal(new Vector3f(-0.484901f, -0.440214f, 0.755700f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // third vertex
        index3 = mb.addPosition(new Vector3f(0.027560f, -0.034990f, -0.017243f));
        mb.addNormal(new Vector3f(-0.209172f, -0.977654f, -0.020987f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        // triangle index 6
        mb.addTriangle(index1, index2, index3);

        //7 v 7 9 8 n 7 9 8 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(-0.028052f, -0.005245f, -0.010815f));
        mb.addNormal(new Vector3f(-0.484901f, -0.440214f, 0.755700f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(-0.029844f, -0.015952f, -0.025316f));
        mb.addNormal(new Vector3f(-0.313104f, -0.949629f, -0.013028f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.027560f, -0.034990f, -0.017243f));
        mb.addNormal(new Vector3f(-0.209172f, -0.977654f, -0.020987f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);


//8 v 9 10 8 n 9 10 8 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(-0.029844f, -0.015952f, -0.025316f));
        mb.addNormal(new Vector3f(-0.313104f, -0.949629f, -0.013028f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(-0.008861f, -0.010066f, -0.038751f));
        mb.addNormal(new Vector3f(-0.445420f, -0.521429f, -0.727814f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.027560f, -0.034990f, -0.017243f));
        mb.addNormal(new Vector3f(-0.209172f, -0.977654f, -0.020987f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//9 v 10 11 8 n 10 11 8 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(-0.008861f, -0.010066f, -0.038751f));
        mb.addNormal(new Vector3f(-0.445420f, -0.521429f, -0.727814f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.024988f, -0.019819f, -0.057578f));
        mb.addNormal(new Vector3f(-0.338197f, -0.590572f, -0.732698f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.027560f, -0.034990f, -0.017243f));
        mb.addNormal(new Vector3f(-0.209172f, -0.977654f, -0.020987f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//10 v 11 10 0 n 11 10 0 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.024988f, -0.019819f, -0.057578f));
        mb.addNormal(new Vector3f(-0.338197f, -0.590572f, -0.732698f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(-0.008861f, -0.010066f, -0.038751f));
        mb.addNormal(new Vector3f(-0.445420f, -0.521429f, -0.727814f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.020623f, 0.015098f, -0.059735f));
        mb.addNormal(new Vector3f(-0.391551f, 0.247565f, -0.886228f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//11 v 10 1 0 n 10 1 0 t 0 0 0 


        index1 = mb.addPosition(new Vector3f(-0.008861f, -0.010066f, -0.038751f));
        mb.addNormal(new Vector3f(-0.445420f, -0.521429f, -0.727814f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(-0.011066f, 0.007605f, -0.039921f));
        mb.addNormal(new Vector3f(-0.430489f, 0.506037f, -0.747400f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.020623f, 0.015098f, -0.059735f));
        mb.addNormal(new Vector3f(-0.391551f, 0.247565f, -0.886228f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);



//12 v 12 13 2 n 12 13 2 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.111493f, 0.044170f, -0.031036f));
        mb.addNormal(new Vector3f(0.062805f, 0.998023f, -0.002451f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.106122f, 0.022365f, -0.076512f));
        mb.addNormal(new Vector3f(-0.051667f, 0.498219f, -0.865510f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.018833f, 0.034845f, -0.021558f));
        mb.addNormal(new Vector3f(-0.269007f, 0.948483f, -0.167374f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//13 v 0 2 13 n 0 2 13 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.020623f, 0.015098f, -0.059735f));
        mb.addNormal(new Vector3f(-0.391551f, 0.247565f, -0.886228f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.018833f, 0.034845f, -0.021558f));
        mb.addNormal(new Vector3f(-0.269007f, 0.948483f, -0.167374f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.106122f, 0.022365f, -0.076512f));
        mb.addNormal(new Vector3f(-0.051667f, 0.498219f, -0.865510f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);


//14 v 14 12 5 n 14 12 5 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.117252f, 0.027443f, 0.016907f));
        mb.addNormal(new Vector3f(-0.391551f, 0.247565f, -0.886228f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.111493f, 0.044170f, -0.031036f));
        mb.addNormal(new Vector3f(0.062805f, 0.998023f, -0.002451f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.021407f, 0.019674f, 0.018777f));
        mb.addNormal(new Vector3f(-0.298481f, 0.569384f, 0.765970f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//15 v 2 5 12 n 2 5 12 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.018833f, 0.034845f, -0.021558f));
        mb.addNormal(new Vector3f(-0.269007f, 0.948483f, -0.167374f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.021407f, 0.019674f, 0.018777f));
        mb.addNormal(new Vector3f(-0.298481f, 0.569384f, 0.765970f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.111493f, 0.044170f, -0.031036f));
        mb.addNormal(new Vector3f(0.062805f, 0.998023f, -0.002451f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//16 v 5 6 14 n 5 6 14 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.021407f, 0.019674f, 0.018777f));
        mb.addNormal(new Vector3f(-0.298481f, 0.569384f, 0.765970f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.025770f, -0.015243f, 0.020934f));
        mb.addNormal(new Vector3f(-0.154317f, -0.508830f, 0.846923f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.117252f, 0.027443f, 0.016907f));
        mb.addNormal(new Vector3f(-0.391551f, 0.247565f, -0.886228f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);


//17 v 6 15 14 n 6 15 14 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.025770f, -0.015243f, 0.020934f));
        mb.addNormal(new Vector3f(-0.154317f, -0.508830f, 0.846923f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.117640f, -0.011090f, 0.019374f));
        mb.addNormal(new Vector3f(0.110875f, -0.386956f, 0.915408f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.117252f, 0.027443f, 0.016907f));
        mb.addNormal(new Vector3f(-0.391551f, 0.247565f, -0.886228f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//18 v 15 6 16 n 15 6 16 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.117640f, -0.011090f, 0.019374f));
        mb.addNormal(new Vector3f(0.110875f, -0.386956f, 0.915408f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.025770f, -0.015243f, 0.020934f));
        mb.addNormal(new Vector3f(-0.154317f, -0.508830f, 0.846923f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.112269f, -0.032896f, -0.026101f));
        mb.addNormal(new Vector3f(0.043009f, -0.992576f, 0.113765f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//19 v 6 8 16 n 6 8 16 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.025770f, -0.015243f, 0.020934f));
        mb.addNormal(new Vector3f(-0.154317f, -0.508830f, 0.846923f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.027560f, -0.034990f, -0.017243f));
        mb.addNormal(new Vector3f(-0.209172f, -0.977654f, -0.020987f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.112269f, -0.032896f, -0.026101f));
        mb.addNormal(new Vector3f(0.043009f, -0.992576f, 0.113765f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//20 v 16 8 17 n 16 8 17 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.112269f, -0.032896f, -0.026101f));
        mb.addNormal(new Vector3f(0.043009f, -0.992576f, 0.113765f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.027560f, -0.034990f, -0.017243f));
        mb.addNormal(new Vector3f(-0.209172f, -0.977654f, -0.020987f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.106510f, -0.016168f, -0.074044f));
        mb.addNormal(new Vector3f(-0.072389f, -0.606597f, -0.791707f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//21 v 8 11 17 n 8 11 17 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.027560f, -0.034990f, -0.017243f));
        mb.addNormal(new Vector3f(-0.209172f, -0.977654f, -0.020987f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.024988f, -0.019819f, -0.057578f));
        mb.addNormal(new Vector3f(-0.338197f, -0.590572f, -0.732698f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.106510f, -0.016168f, -0.074044f));
        mb.addNormal(new Vector3f(-0.072389f, -0.606597f, -0.791707f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//22 v 13 17 0 n 13 17 0 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.106122f, 0.022365f, -0.076512f));
        mb.addNormal(new Vector3f(-0.051667f, 0.498219f, -0.865510f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.106510f, -0.016168f, -0.074044f));
        mb.addNormal(new Vector3f(-0.072389f, -0.606597f, -0.791707f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.020623f, 0.015098f, -0.059735f));
        mb.addNormal(new Vector3f(-0.391551f, 0.247565f, -0.886228f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//23 v 11 0 17 n 11 0 17 t 0 0 0 


        index1 = mb.addPosition(new Vector3f(0.024988f, -0.019819f, -0.057578f));
        mb.addNormal(new Vector3f(-0.338197f, -0.590572f, -0.732698f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.020623f, 0.015098f, -0.059735f));
        mb.addNormal(new Vector3f(-0.391551f, 0.247565f, -0.886228f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.106510f, -0.016168f, -0.074044f));
        mb.addNormal(new Vector3f(-0.072389f, -0.606597f, -0.791707f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);


//24 v 13 12 18 n 13 12 18 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.106122f, 0.022365f, -0.076512f));
        mb.addNormal(new Vector3f(-0.051667f, 0.498219f, -0.865510f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.111493f, 0.044170f, -0.031036f));
        mb.addNormal(new Vector3f(0.062805f, 0.998023f, -0.002451f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.229801f, 0.006792f, -0.069784f));
        mb.addNormal(new Vector3f(0.132156f, 0.678930f, -0.722211f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//25 v 12 19 18 n 12 19 18 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.111493f, 0.044170f, -0.031036f));
        mb.addNormal(new Vector3f(0.062805f, 0.998023f, -0.002451f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.235222f, 0.020244f, -0.037335f));
        mb.addNormal(new Vector3f(0.185228f, 0.979307f, -0.081535f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.229801f, 0.006792f, -0.069784f));
        mb.addNormal(new Vector3f(0.132156f, 0.678930f, -0.722211f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//26 v 14 20 12 n 14 20 12 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.117252f, 0.027443f, 0.016907f));
        mb.addNormal(new Vector3f(-0.391551f, 0.247565f, -0.886228f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.240877f, 0.010286f, -0.003421f));
        mb.addNormal(new Vector3f(0.217324f, 0.609523f, 0.762399f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.111493f, 0.044170f, -0.031036f));
        mb.addNormal(new Vector3f(0.062805f, 0.998023f, -0.002451f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//27 v 12 20 19 n 12 20 19 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.111493f, 0.044170f, -0.031036f));
        mb.addNormal(new Vector3f(0.062805f, 0.998023f, -0.002451f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.240877f, 0.010286f, -0.003421f));
        mb.addNormal(new Vector3f(0.217324f, 0.609523f, 0.762399f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.235222f, 0.020244f, -0.037335f));
        mb.addNormal(new Vector3f(0.185228f, 0.979307f, -0.081535f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//28 v 14 15 20 n 14 15 20 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.117252f, 0.027443f, 0.016907f));
        mb.addNormal(new Vector3f(-0.391551f, 0.247565f, -0.886228f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.117640f, -0.011090f, 0.019374f));
        mb.addNormal(new Vector3f(0.110875f, -0.386956f, 0.915408f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.240877f, 0.010286f, -0.003421f));
        mb.addNormal(new Vector3f(0.217324f, 0.609523f, 0.762399f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//29 v 15 21 20 n 15 21 20 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.117640f, -0.011090f, 0.019374f));
        mb.addNormal(new Vector3f(0.110875f, -0.386956f, 0.915408f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.241114f, -0.013123f, -0.001956f));
        mb.addNormal(new Vector3f(0.119591f, -0.698927f, 0.705123f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.240877f, 0.010286f, -0.003421f));
        mb.addNormal(new Vector3f(0.217324f, 0.609523f, 0.762399f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//30 v 15 16 21 n 15 16 21 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.117640f, -0.011090f, 0.019374f));
        mb.addNormal(new Vector3f(0.110875f, -0.386956f, 0.915408f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.112269f, -0.032896f, -0.026101f));
        mb.addNormal(new Vector3f(0.043009f, -0.992576f, 0.113765f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.241114f, -0.013123f, -0.001956f));
        mb.addNormal(new Vector3f(0.119591f, -0.698927f, 0.705123f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//31 v 16 22 21 n 16 22 21 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.112269f, -0.032896f, -0.026101f));
        mb.addNormal(new Vector3f(0.043009f, -0.992576f, 0.113765f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.235693f, -0.026574f, -0.034406f));
        mb.addNormal(new Vector3f(0.054137f, -0.997508f, 0.045246f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.241114f, -0.013123f, -0.001956f));
        mb.addNormal(new Vector3f(0.119591f, -0.698927f, 0.705123f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//32 v 17 23 16 n 17 23 16 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.106510f, -0.016168f, -0.074044f));
        mb.addNormal(new Vector3f(-0.072389f, -0.606597f, -0.791707f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.230036f, -0.016617f, -0.068320f));
        mb.addNormal(new Vector3f(0.040545f, -0.612698f, -0.789276f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.112269f, -0.032896f, -0.026101f));
        mb.addNormal(new Vector3f(0.043009f, -0.992576f, 0.113765f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//33 v 16 23 22 n 16 23 22 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.112269f, -0.032896f, -0.026101f));
        mb.addNormal(new Vector3f(0.043009f, -0.992576f, 0.113765f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.230036f, -0.016617f, -0.068320f));
        mb.addNormal(new Vector3f(0.040545f, -0.612698f, -0.789276f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.235693f, -0.026574f, -0.034406f));
        mb.addNormal(new Vector3f(0.054137f, -0.997508f, 0.045246f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//34 v 17 13 23 n 17 13 23 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.106510f, -0.016168f, -0.074044f));
        mb.addNormal(new Vector3f(-0.072389f, -0.606597f, -0.791707f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.106122f, 0.022365f, -0.076512f));
        mb.addNormal(new Vector3f(-0.051667f, 0.498219f, -0.865510f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.230036f, -0.016617f, -0.068320f));
        mb.addNormal(new Vector3f(0.040545f, -0.612698f, -0.789276f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

//35 v 13 18 23 n 13 18 23 t 0 0 0 

        index1 = mb.addPosition(new Vector3f(0.106122f, 0.022365f, -0.076512f));
        mb.addNormal(new Vector3f(-0.051667f, 0.498219f, -0.865510f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.229801f, 0.006792f, -0.069784f));
        mb.addNormal(new Vector3f(0.132156f, 0.678930f, -0.722211f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.230036f, -0.016617f, -0.068320f));
        mb.addNormal(new Vector3f(0.040545f, -0.612698f, -0.789276f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        mb.addTriangle(index1, index2, index3);

        ///////////////////////////////////////// Tex coord changes
        ///////////////////////////////////////// Tex coord changes
        ///////////////////////////////////////// Tex coord changes
        ///////////////////////////////////////// Tex coord changes

//36 v 18 19 23 n 24 25 26 t 0 1 2 

        index1 = mb.addPosition(new Vector3f(0.229801f, 0.006792f, -0.069784f));
        mb.addNormal(new Vector3f(   0.986356f, -0.000374f, -0.164625f));
        mb.addTexCoord(0, new Vector2f(0.102639f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(0.102639f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.235222f, 0.020244f, -0.037335f));
        mb.addNormal(new Vector3f(   0.986358f, -0.000379f, -0.164613f));
        mb.addTexCoord(0, new Vector2f(   0.602638f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(   0.602638f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.230036f, -0.016617f, -0.068320f));
        mb.addNormal(new Vector3f(   0.986357f, -0.000378f, -0.164620f));
        mb.addTexCoord(0, new Vector2f(   0.000000f, 0.305715f));
        mb.addTexCoord(1, new Vector2f(   0.000000f, 0.305715f));

        mb.addTriangle(index1, index2, index3);

//37 v 23 19 22 n 26 25 27 t 2 1 3 

        index1 = mb.addPosition(new Vector3f(0.230036f, -0.016617f, -0.068320f));
        mb.addNormal(new Vector3f(   0.986357f, -0.000378f, -0.164620f));
        mb.addTexCoord(0, new Vector2f(   0.000000f, 0.305715f));
        mb.addTexCoord(1, new Vector2f(   0.000000f, 0.305715f));

        index2 = mb.addPosition(new Vector3f(0.235222f, 0.020244f, -0.037335f));
        mb.addNormal(new Vector3f(   0.986358f, -0.000379f, -0.164613f));
        mb.addTexCoord(0, new Vector2f(   0.602638f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(   0.602638f, 0.000000f));

        index3 = mb.addPosition(new Vector3f(0.235693f, -0.026574f, -0.034406f));
        mb.addNormal(new Vector3f(   0.986357f, -0.000368f, -0.164620f));
        mb.addTexCoord(0, new Vector2f(   0.397362f, 0.611427f));
        mb.addTexCoord(1, new Vector2f(   0.397362f, 0.611427f));

        mb.addTriangle(index1, index2, index3);

//38 v 19 20 22 n 25 28 27 t 1 4 3 

        index1 = mb.addPosition(new Vector3f(0.235222f, 0.020244f, -0.037335f));
        mb.addNormal(new Vector3f(   0.986358f, -0.000379f, -0.164613f));
        mb.addTexCoord(0, new Vector2f(   0.602638f, 0.000000f));
        mb.addTexCoord(1, new Vector2f(   0.602638f, 0.000000f));

        index2 = mb.addPosition(new Vector3f(0.240877f, 0.010286f, -0.003421f));
        mb.addNormal(new Vector3f(   0.986357f, -0.000362f, -0.164622f));
        mb.addTexCoord(0, new Vector2f(   1.000000f, 0.305712f));
        mb.addTexCoord(1, new Vector2f(   1.000000f, 0.305712f));

        index3 = mb.addPosition(new Vector3f(0.235693f, -0.026574f, -0.034406f));
        mb.addNormal(new Vector3f(   0.986357f, -0.000368f, -0.164620f));
        mb.addTexCoord(0, new Vector2f(   0.397362f, 0.611427f));
        mb.addTexCoord(1, new Vector2f(   0.397362f, 0.611427f));

        mb.addTriangle(index1, index2, index3);

//39 v 20 21 22 n 28 29 27 t 4 5 3 

        index1 = mb.addPosition(new Vector3f(0.240877f, 0.010286f, -0.003421f));
        mb.addNormal(new Vector3f(   0.986357f, -0.000362f, -0.164622f));
        mb.addTexCoord(0, new Vector2f(   1.000000f, 0.305712f));
        mb.addTexCoord(1, new Vector2f(   1.000000f, 0.305712f));

        index2 = mb.addPosition(new Vector3f(0.241114f, -0.013123f, -0.001956f));
        mb.addNormal(new Vector3f(   0.986353f, -0.000343f, -0.164645f));
        mb.addTexCoord(0, new Vector2f(   0.897362f, 0.611424f));
        mb.addTexCoord(1, new Vector2f(   0.897362f, 0.611424f));

        index3 = mb.addPosition(new Vector3f(0.235693f, -0.026574f, -0.034406f));
        mb.addNormal(new Vector3f(   0.986357f, -0.000368f, -0.164620f));
        mb.addTexCoord(0, new Vector2f(   0.397362f, 0.611427f));
        mb.addTexCoord(1, new Vector2f(   0.397362f, 0.611427f));

        mb.addTriangle(index1, index2, index3);

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Give a name to the arrays
        Vector3f[] vertexes = mb.getPositions();
        Vector3f[] normals = mb.getNormals();
        Vector2f[] texCoords = mb.getTexCoords(); // equivilent to  mb.getTexCoords(0)
        int[] indexes = mb.getIndices();

        // Feed the information to the TriMesh
        m.reconstruct(BufferUtils.createFloatBuffer(vertexes), BufferUtils.createFloatBuffer(normals),
                null, BufferUtils.createFloatBuffer(texCoords),
                BufferUtils.createIntBuffer(indexes));
        // Feed multi-texture information to the TriMesh
        texCoords = mb.getTexCoords(1);
        m.setTextureBuffer(0, BufferUtils.createFloatBuffer(texCoords), 1);
        
        //m.copyTextureCoords(0, 0, 1); // this is not needed because we fed all texture cordinates earlier

        // Create a bounds
        m.setModelBound(new BoundingBox());
        m.updateModelBound();
        
//        // Point to the image
//         URL monkeyLoc=
//          TriMeshTest.class.getClassLoader().
//          getResource("jmetest/data/texture/grassb.png");
//         // Get my TextureState
//         TextureState ts=display.getRenderer().createTextureState();
//         // Get my Texture
//         Texture t=TextureManager.loadTexture(monkeyLoc,
//          Texture.MM_LINEAR,
//          Texture.FM_LINEAR);
//         // Set a wrap for my texture so it repeats
//         //t.setWrap(Texture.WM_WRAP_S_WRAP_T);
//         // Set the texture to the TextureState
//         ts.setTexture(t);
//         // Assign the TextureState to the square
//         m.setRenderState(ts);

        // Attach the mesh to my scene graph
        rootNode.attachChild(m);
        
        // Let us see the per vertex colors
        lightState.setEnabled(false);
        
        // Multi-texturing test
        TextureState ts=display.getRenderer().createTextureState();
        ts.setEnabled(true);
        Texture t1 = TextureManager.loadTexture(
        TestBoxColor.class.getClassLoader().getResource(
        "jmetest/data/images/Monkey.jpg"),
        Texture.MM_LINEAR,
        Texture.FM_LINEAR);
        ts.setTexture(t1, 0);

        Texture t2 = TextureManager.loadTexture(TestBoxColor.class.getClassLoader().
                                            getResource("jmetest/data/texture/dirt.jpg"),
                                            Texture.MM_LINEAR,
                                            Texture.FM_LINEAR);
        ts.setTexture(t2, 1);
        
        rootNode.setRenderState(ts);
        
    }
}
