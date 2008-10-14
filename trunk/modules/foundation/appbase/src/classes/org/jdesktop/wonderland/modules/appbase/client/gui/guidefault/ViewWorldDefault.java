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
package org.jdesktop.wonderland.modules.appbase.client.gui.guidefault;

import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.util.TextureManager;
import java.awt.Button;
import java.awt.Toolkit;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.modules.appbase.client.Window2DView;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.AppCell;
import org.jdesktop.wonderland.modules.appbase.client.Window2DViewWorld;
import org.jdesktop.wonderland.client.jme.utils.graphics.TexturedQuad;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import com.jme.scene.state.TextureState;
import java.nio.FloatBuffer;
import com.jme.util.geom.BufferUtils;
import com.jme.scene.TexCoords;

/**
 * A view onto a window which exists in the 3D world.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public class ViewWorldDefault extends Window2DView implements Window2DViewWorld {

    private static final Logger logger = Logger.getLogger(ViewWorldDefault.class.getName());

    /** The vector offset from the center of the cell to the center of the window */
    protected Vector3f position;

    /** The width of the view (excluding the frame) */
    protected float width;

    /** The height of the view (excluding the frame) */
    protected float height;

    /** The textured 3D object in which displays the window contents */
    protected ViewGeometryObject geometryObj;

    /** The view's geometry is connected to its cell */
    protected boolean connectedToCell;
    
    /** 
     * The root of the view subgraph. This contains all geometry and is 
     * connected to the local scene graph of the cell.
     */
    protected Node baseNode;

    /** The control arbitrator of the window */
    protected ControlArb controlArb;

    /** The frame of the view's window (if it is top-level) */
    protected FrameWorldDefault frame;
    
    /** The visibility of the view itself */
    private boolean viewVisible;

    /** 
     * The combined window/view visibility. The view is actually visible
     * only if both are true.
     */
    private boolean visible;

    /** Whether the view's window is top-level */
    private boolean topLevel;

    /** A temporary used by deliverEvent(MouseEvent3D) */
    // TODO private Point3f tmpP3f = new Point3f();

    /** A dummy AWT component used by deliverEvent(Window2D,MouseEvent3D) */
    private Button dummyButton = new Button();

    /*
    ** TODO: WORKAROUND FOR A WONDERLAND PICKER PROBLEM:
    ** 
    ** We cannot rely on the x and y values in the intersection info coming from LG
    ** for mouse release events.
    ** The problem is both the same for the X11 and AWT pickers. The LG pickers are 
    ** currently defined to set the node info of all events interior to and terminating
    ** a grab to the node info of the button press which started the grab. Not only is
    ** the destination node set (as is proper) but also the x and y intersection info
    ** (which is dubious and, I believe, improper). Note that there is a hack in both 
    ** pickers to work around this problem for drag events, which was all LG cared about
    ** at the time. I don't want to perturb the semantics of the LG pickers at this time,
    ** but in the future the intersection info must be dealt with correctly for all
    ** events interior to and terminating a grab. Note that this problem doesn't happen
    ** with button presses, because these start grabs.
    **
    ** For now I'm simply going to treat the intersection info in button release events
    ** as garbage and supply the proper values by tracking the pointer position myself.
    */
    private boolean pointerMoveSeen = false;
    private int pointerLastX;
    private int pointerLastY;

    /**
     * Create a new instance of ViewWorldDefault.
     *
     * @param window The window displayed by the view.
     */
    public ViewWorldDefault (Window2D window) {
	super(window, "World");
	// TODO gui = new Gui2DInterior(this);

	controlArb = window.getApp().getControlArb();
    }

    /**
     * Clean up resources.
     */
    public void cleanup () {
	super.cleanup();
	setVisible(false);
	if (geometryObj != null) {
	    geometryObj.cleanup();
	    geometryObj = null;
	}
	if (controlArb != null) {
	    controlArb = null;
	}
	if (frame != null) {
	    frame.cleanup();
	    frame = null;
        }
	if (baseNode != null) {
	    AppCell cell = getCell();
	    if (cell != null) {
		cell.detachView(this, RendererType.RENDERER_JME);
	    }
	    baseNode = null;
	}
	if (gui != null) {
	    // TODO: gui.cleanup();
	    gui = null;
	}
    }

    /**
     * {@inheritDoc}
     */
    public void setVisible (boolean visible) {
	viewVisible = visible;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getVisible () {
	return viewVisible;
    }

    /**
     * Returns whether the window is actually visible, that is,
     * the view visibility combined with the window visibility.
     */
    boolean getActuallyVisible () {
	return visible;
    }

    /**
     * {@inheritDoc}
     */
    public void setTopLevel (boolean topLevel) {
	if (this.topLevel == topLevel) return;
	this.topLevel = topLevel;
    }
	
    /**
     * {@inheritDoc}
     */
    public boolean getTopLevel () {
	return topLevel;
    }

    /**
     * Sets the position of the view. Don't forget to also call update(CHANGED_POSITION) afterward.
     *
     * @param position The new position of the window (in cell local coordinates).
     */
    public void setPosition (Vector3f position) { 
	this.position = position;
    }

    /**
     * Returns the position of the window.
     */
    public Vector3f getPosition () { 
	return position;
    }

    /**
     * {@inheritDoc}
     */
    public void setSize (float width, float height) {
	this.width = width;
	this.height = height;
    }

    /**
     * {@inheritDoc}
     */
    public float getWidth () { 
	return width; 
    }

    /**
     * {@inheritDoc}
     */
    public float getHeight () { 
	return height; 
    }

    /**
     * Returns the base node of the view. This is the root node of the view's scene graph.
     */
    public Node getBaseNode () {
	if (baseNode == null) {
	    update(0);
	}
	return baseNode;
    }

    /**
     * Returns the frame of view. 
     */
    FrameWorldDefault getFrame () {
	if (topLevel && frame == null) {
	    update(CHANGED_TOP_LEVEL);
	}
	return frame;
    }

    /**
     * {@inheritDoc}"
     */
    @Override
    public void update (int changeMask) {

	// The first time we are updated we need to create top part 
	// of the view's scene graph (but it's not yet attached to its cell)
	if (baseNode == null) {
	    baseNode = new Node(); 

	    // There must be a node of this type as an ancestor of the textured panel
	    // in order to receive events
	    // TODO gui.initEventHandling(baseNode);
    	}
	
	// It's necessary to do these in the following order

	if ((changeMask & CHANGED_VISIBILITY) != 0) {
	    updateVisibility();

	    // When the view has been made visible for the first time
	    // we need to create the geometry
	    if (visible && geometryObj == null) {
		changeMask |= CHANGED_SIZE;
	    }
	}
	if ((changeMask & CHANGED_SIZE) != 0) {
	    try {
                updateGeometrySize();
                updateTexture();
            } catch (InstantiationException ex) {
                logger.warning("Instantiation exception while updating view size");
		ex.printStackTrace();
                return;
            }
	}
	if ((changeMask & CHANGED_STACK) != 0) {
	    updateStack();
	}
	if ((changeMask & (CHANGED_TRANSFORM | CHANGED_STACK)) != 0) {
	    updateTransform();
	}

	if ((changeMask & CHANGED_TOP_LEVEL) != 0) {
	    if (topLevel) {
		if (frame == null) {
		    frame = (FrameWorldDefault) window.getApp().getAppType().getGuiFactory().createFrame(this);
		}
	    } else {
		if (frame != null) {
		    frame.disconnect();
		    frame.cleanup();
		    frame = null;
		}
	    }
	}

	if ((changeMask & CHANGED_TITLE) != 0) {
	    frame.setTitle(((Window2D)window).getTitle());
	}
    }

    /** Update the view's geometry (for a size change) */
    protected void updateGeometrySize () throws InstantiationException {
	if (!visible) return;

        Window2D window2D = (Window2D) window;
        Vector2f pixelScale = window2D.getPixelScale();
	width = pixelScale.x * (float)window2D.getWidth();
	height = pixelScale.y * (float)window2D.getHeight();

	if (geometryObj == null) {

	    // Geometry first time creation

	    setGeometry(new ViewGeometryObjectDefault(this));

	} else {

	    // Geometry resize
	    geometryObj.updateSize();
	}

	if (frame != null) {
	    frame.update();
	}
    }

    /** The window's texture may have changed */
    protected void updateTexture () {
	if (geometryObj != null) {
	    geometryObj.updateTexture();
	}
    }

    /** Update the view's transform */
    protected void updateTransform () {
	// TODO: currently identity
    }

    /**
     * Returns the texture width.
     */
    public int getTextureWidth () {
	if (geometryObj != null) {
	    return geometryObj.getTextureWidth();
	} else {
	    return 0;
	}
    }

    /**
     * Returns the texture height.
     */
    public int getTextureHeight () {
	if (geometryObj != null) {
	    return geometryObj.getTextureHeight();
	} else {
	    return 0;
	}
    }

    /** Update the view's stacking relationships */
    protected void updateStack () {
	// TODO
    }

    /** Update the view's visibility */
    void updateVisibility() {
	AppCell cell = getCell();
	if (cell == null) return;
	baseNode.setName("ViewWorldDefault Node for cell " + getCell().getCellID().toString());

	visible = viewVisible && window.isVisible();
	
	if (visible && !connectedToCell) {
	    cell.attachView(this, RendererType.RENDERER_JME);
	    connectedToCell = true;
	} else if (!visible && connectedToCell) {
	    cell.detachView(this, RendererType.RENDERER_JME);
	    connectedToCell = false;
	}
    }

    /** Get the cell of the view */
    protected AppCell getCell () {
	return window.getCell();
    }
  
    /**
     * If you want to customize the geometry of a view implement
     * this interface and pass an instance to view.setGeometry.
     */
    public abstract static class ViewGeometryObject extends Node {

	/** The view for which the geometry object was created. */
	protected ViewWorldDefault view;

	/** 
	 * Create a new instance of ViewGeometryObject. Attach your geometry as 
	 * children of this Node.
	 *
	 * @param view The view object the geometry is to display.
	 */
       	public ViewGeometryObject (ViewWorldDefault view) {
	    super("ViewGeometryObject");
	    this.view = view;
	}

      	/** 
	 * Clean up resources. Any resources you allocate for the 
	 * object should be detached in here. 
	 *
	 * NOTE: It is normal for one ViewObjectGeometry to get stuck in 
	 * the Java heap (because it is reference by Wonderland picker last 
	 * picked node). So make sure there aren't large objects, such as a 
	 * texture, attached to it.
	 * TODO: goes away in Rel 0.5?
	 *
	 * NOTE: for cleanliness, call super.cleanup() in your constructor.
	 * (This is optional).
	 */
	public void cleanup () {
	    view = null;
	}

	/**
	 * The size of the view (and corresponding window texture) has
	 * changed. Make the corresponding change to your geometry in
	 * this method.
	 */
	public abstract void updateSize ();

	/** 
	 * The view's texture may have changed. You should get the view's
	 * texture and make your geometry display it.
	 */
	public abstract void updateTexture ();

	/**
	 * Transforms the given 3D point in world space into the window 
	 * coordinate system assuming that the shape is a rectangle
	 * the same size as the view. If this assumption does not hold
	 * for your subclass you must override this method.
	 *
	 * @param p3f A 3D world point.
	 * @return The 2D position of the world point in the window
	 * or null if the point is outside the window's geometry.
	 */
	/* TODO
	protected Point convertPoint3DTo2D (Point3f p3f) {
	    // First calculate the actual coordinates of the corners of
	    // the panel in world coords.
        
	    Transform3D t3d = new Transform3D();
	    getLocalToVworld(t3d);
        
	    float widthWorld = view.getWidthWorld();
	    float heightWorld = view.getHeightWorld();
	    Point3f topLeft = new Point3f( -widthWorld/2f, heightWorld/2f, 0f);
	    Point3f topRight = new Point3f( widthWorld/2f, heightWorld/2f, 0f);
	    Point3f bottomLeft = new Point3f( -widthWorld/2f, -heightWorld/2f, 0f);
	    Point3f bottomRight = new Point3f( widthWorld/2f, -heightWorld/2f, 0f);
        
	    t3d.transform(topLeft);
	    t3d.transform(topRight);
	    t3d.transform(bottomLeft);
	    t3d.transform(bottomRight);
        
	    // Now calculate the x and y coords relative to the panel
        
	    float y = Math3D.pointLineDistance(topLeft,topRight,p3f);
	    float y1 = Math3D.pointLineDistance(bottomLeft, bottomRight, p3f);
	    float x = Math3D.pointLineDistance(topLeft,bottomLeft,p3f);
	    float x1 = Math3D.pointLineDistance(topRight,bottomRight,p3f);

	    if (y > heightWorld || y1 > heightWorld || x > widthWorld || x1 > widthWorld) {
		return null;
	    }
        
	    Window2D window2D = (Window2D) view.getWindow();
	    return new Point((int)((x / widthWorld) * window2D.getWidth()),
			     (int)((y / heightWorld) * window2D.getHeight()));
	}
        */

	/**
	 * Returns the texture width.
	 */
	public abstract int getTextureWidth ();

	/**
	 * Returns the texture height.
	 */
	public abstract int getTextureHeight ();

	/**
	 * Returns the texture state.
	 */
	public abstract TextureState getTextureState ();
    }

    /** 
     * The geometry node used by the view.
     */
    protected static class ViewGeometryObjectDefault extends ViewGeometryObject {
               
	/** The actual geometry */
        private TexturedQuad quad;

	/** 
	 * Create a new instance of textured geometry. The dimensions
	 * are derived from the view dimensions.
	 *
	 * @param view The view object the geometry is to display.
	 */
        private ViewGeometryObjectDefault (ViewWorldDefault view) {
	    super(view);

	    quad = new TexturedQuad(null, "ViewWorldDefault-TexturedQuad");
	    attachChild(quad);

	    updateSize();

	    /* TODO: debug 
	    quad.printRenderState();
	    quad.printGeometry();
            */
        }

	/** 
	 * {@inheritDoc}
	 */
	public void cleanup () {
	    super.cleanup();
	    if (quad != null) {
		detachChild(quad);
		quad = null;
	    }
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateSize () {
	    float width = view.getWidth();
	    float height = view.getHeight();
	    quad.initialize(width, height);
	    updateTexture();
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateTexture () {
	    Window2D window2D = (Window2D) view.getWindow();
	    Texture texture = window2D.getTexture();

	    /* For debug
	    java.awt.Image bi = Toolkit.getDefaultToolkit().getImage("/home/dj/wl/images/Monkey.png");
	    Image image = TextureManager.loadImage(bi, false);
	    Texture texture = new Texture2D();
	    texture.setImage(image);
	    texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
	    texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
	    texture.setApply(Texture.ApplyMode.Replace);
	    */

	    if (texture == null) {
		// Texture hasn't been created yet. (This method will
		// be called again when it is created).
		return;
	    }

	    quad.setTexture(texture);

	    float winWidth = (float) window2D.getWidth();
	    float winHeight = (float) window2D.getHeight();

	    Image image = texture.getImage();
            float texCoordW = winWidth / image.getWidth();
            float texCoordH = winHeight / image.getHeight();
            
            //logger.warning("TexCoords "+texCoordW+", "+texCoordH);

	    FloatBuffer tbuf = BufferUtils.createVector2Buffer(4);
	    quad.setTextureCoords(new TexCoords(tbuf));
	    tbuf.put(0f).put(0);
	    tbuf.put(0f).put(texCoordH);
	    tbuf.put(texCoordW).put(texCoordH);
	    tbuf.put(texCoordW).put(0);
        } 

	/**
	 * Returns the texture width.
	 */
	public int getTextureWidth () {
	    return quad.getTexture().getImage().getWidth();
	}

	/**
	 * Returns the texture height.
	 */
	public int getTextureHeight () {
	    return quad.getTexture().getImage().getHeight();
	}

	/**
	 * Returns the texture state.
	 */
	public TextureState getTextureState () {
	    return quad.getTextureState();
	}
    }

    /**
     * Replace this view's geometry with different geometry.
     * The updateSize method of the geometry object will be called 
     * to provide geometry object with the view's current size.
     *
     * @param newGeometryObj The new geometry object for this window.
     */
    public void setGeometry (ViewGeometryObject newGeometryObj) {

	// Detach any previous geometry object
	if (geometryObj != null) {
	    geometryObj.cleanup();
	    baseNode.detachChild(geometryObj);
	}

	geometryObj = newGeometryObj;
	geometryObj.updateSize();
	geometryObj.updateTexture();

	baseNode.attachChild(newGeometryObj);
    }

    /**
     * {@inheritDoc}
     */
    /* TODO
    public void deliverEvent (Window2D window, MouseEvent3D me3d) {

	// No special processing is needed for wheel events. Just
	// send the 2D wheel event which is contained in the 3D event.
	if (me3d instanceof MouseWheelEvent3D) {
	    controlArb.deliverEvent(window, me3d.getMouseEvent());
	    return;
	}

	// Can't convert if there is no geometry
	if (geometryObj == null) return;

	// Convert mouse event intersection point to 2D
	Point point = geometryObj.convertPoint3DTo2D(me3d.getIntersection(tmpP3f));
	if (point == null) {
            // Event was outside our panel so do nothing
            // This can happen for drag events
            // TODO fix so that drags continue in the plane of the panel
	    return;
	}

	// Construct a corresponding 2D event
	MouseEvent me = me3d.getMouseEvent();
	int id = me.getID();
	long when = me.getWhen();
	int modifiers = me.getModifiers();
	int button = me.getButton();

	// TODO: WORKAROUND FOR A WONDERLAND PICKER PROBLEM:
	// See comment for pointerMoveSeen above
	if (id == MouseEvent.MOUSE_RELEASED && pointerMoveSeen) {
	    point.x = pointerLastX;
	    point.y = pointerLastY;
	}

	me = new MouseEvent(dummyButton, id, when, modifiers, point.x, point.y, 
			    0, false, button);

	// Send event to the window's control arbiter
	controlArb.deliverEvent(window, me);

	// TODO: WORKAROUND FOR A WONDERLAND PICKER PROBLEM:
	// See comment for pointerMoveSeen above
	if (id == MouseEvent.MOUSE_MOVED || id == MouseEvent.MOUSE_DRAGGED) {
	    pointerMoveSeen = true;
	    pointerLastX = point.x;
	    pointerLastY = point.y;
	}
    }
    */

    public void forceTextureIdAssignment () {
	if (geometryObj == null) {
	    logger.warning("Trying to force texture id assignment while view geometry is null");
	    return;
	}
	TextureState ts = geometryObj.getTextureState();
	if (ts == null) {
	    logger.warning("Trying to force texture id assignment while view texture state is null");
	    return;
	}
	// The JME magic - must be called from within the render loop
	ts.load();
	
	// Verify
	Texture texture = ((Window2D)window).getTexture();
	int texid = texture.getTextureId();
	logger.warning("ViewWorldDefault: allocated texture id " + texid);
	if (texid == 0) {
	    logger.severe("Texture Id is still 0!!!");
	}
    }
}

