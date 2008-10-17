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
package org.jdesktop.wonderland.modules.appbase.client.swing;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.JComponent;
import javax.swing.Popup;
import com.sun.embeddedswing.EmbeddedToolkit;
import com.sun.embeddedswing.EmbeddedPeer;
import org.jdesktop.wonderland.modules.appbase.client.DrawingSurface;
import java.awt.Dimension;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.jme.ClientContextJME;

/**
 * The main interface to Embedded Swing. This class provides access to the three basic capabilities
 * of Embedded Swing.
 * <br><br>
 * 1. Component embedding for the purpose of drawing.
 * <br><br>
 * 2. Mouse event handling.
 * <br><br>
 * 3. Popup window creation.
 */

class WindowSwingEmbeddedToolkit 
    extends EmbeddedToolkit<WindowSwingEmbeddedToolkit.WindowSwingEmbeddedPeer> 
{
    private static final WindowSwingEmbeddedToolkit embeddedToolkit = new WindowSwingEmbeddedToolkit();

    public static WindowSwingEmbeddedToolkit getWindowSwingEmbeddedToolkit() {
        return embeddedToolkit;
    }
    
    @Override
    protected WindowSwingEmbeddedPeer createEmbeddedPeer(JComponent parent, Component embedded, Object... args) {
        return new WindowSwingEmbeddedPeer(parent, embedded, this);
    }
    
    @Override
    protected CoordinateHandler createCoordinateHandler(JComponent parent, Point2D point, MouseEvent mouseEvent) {
	return null;
	/*
	System.err.println("Enter WSET.createCoordinateHandler, mouseEvent = " + mouseEvent);

	FoundationWinSys fws = FoundationWinSys.getFoundationWinSys();
	if (fws == null) {
	    return null;
	}
	System.err.println("1");

	final Canvas3D canvas = fws.getCanvas(0);
	if (canvas == null) {
	    return null;
	}
	System.err.println("2");

	PickEngineAWT pickEngine = WinSysAWT.getPickEngineAWT();
	if (pickEngine == null) {
	    return null;
	}
	System.err.println("3");

	int x = (int) point.getX();
	int y = (int) point.getY();
	PickInfo[] pickInfos = pickEngine.performPick(canvas, x, y);
	//System.err.println("WSET: pickInfos = " + pickInfos);
	if (pickInfos == null || pickInfos[0] == null) {
	    System.err.println("WSET: pick missed");
	    System.err.println("************* Direct inject pick miss");
	    pickEngine.enqueue(mouseEvent);
	    pickEngine.addPickInfos(null);
	    
	    return null;
	}
	//System.err.println("WSET: pick hit, pickInfos = " + pickInfos);
	System.err.println("4");

	Node pickedNode = pickInfos[0].getNode();
	System.err.println("pickedNode = " + pickedNode);
	Node parentNode = pickedNode.getParent();
	int i = 0;
	while (parentNode != null && 
	       parentNode.getClass() != org.jdesktop.lg3d.wonderland.appshare.AppWindowImage.TexturedPanelObject.class) {
	    //System.err.println((i++) + ": parent = " + parentNode);
	    parentNode = parentNode.getParent();
	}
	System.err.println("5");
	if (parentNode == null) {
	    // We didn't hit a WindowSwing, but pass the pick info along to PickEngineAWT.
	    System.err.println("Missed window");
	    System.err.println("************* Direct inject pick hit");
	    System.err.println("mouseEvent = " + mouseEvent);
	    pickEngine.enqueue(mouseEvent);
	    pickEngine.addPickInfos(pickInfos);
	    return null;
	}
	System.err.println("6");
	
	final AppWindowImage.TexturedPanelObject tpo = (AppWindowImage.TexturedPanelObject) parentNode;
	final WindowSwing windowSwing = (WindowSwing) tpo.getAuxObject();
	if (windowSwing == null) {
	    System.err.println("Missed swing window");
	    System.err.println("************* Direct inject pick hit to app win");
	    System.err.println("mouseEvent = " + mouseEvent);
	    pickEngine.enqueue(mouseEvent);
	    pickEngine.addPickInfos(pickInfos);
	    return null;
	}

	final Point3d intersectionPointLocal = pickInfos[0].getClosestIntersectionPoint();
	
	// TODO: debug
	final Component source = (Component) mouseEvent.getSource();
	//System.err.println("Original source = " + source);
	final Point sourcePoint = mouseEvent.getPoint();
	Component root = SwingUtilities.getRoot(source);
	//System.err.println("Root = " + root);
	if (source != root) {
	    System.err.println("Source doesn't equal root!");
	    mouseEvent.setSource(root);
	    Point rootPoint = 
		SwingUtilities.convertPoint(source, sourcePoint, root);
	    mouseEvent.translatePoint(rootPoint.x - sourcePoint.x, 
				      rootPoint.y - sourcePoint.y);
	}

        final EmbeddedPeer targetEmbeddedPeer = windowSwing.getEmbeddedPeer();
        CoordinateHandler coordinateHandler = new CoordinateHandler() {
	    @Override
            public EmbeddedPeer getEmbeddedPeer() {
                return targetEmbeddedPeer;
            }
            @Override
            public Point2D transform(Point2D src, Point2D dst) {
		Point pt = windowSwing.calcPositionInComponent(new Point3f(intersectionPointLocal));
		//System.err.println("pt = " + pt);

		if (dst == null) {
		    dst = new Point2D.Double();
		}
		
		// TODO: for now
		dst.setLocation(new Point2D.Double((double)pt.x, (double)pt.y));
		//System.err.println("dst = " + dst);

		return dst;
            }
        };
        return coordinateHandler;
	*/
    }

    @Override
    // Note: peer should be the owning WindowSwing.embeddedPeer	
    public Popup getPopup(EmbeddedPeer peer, Component contents, int x, int y) {

	// TODO: for now
	return null;

	/* Old: sort of worked, but not very well
	//final WindowSwingPopup wsp =  new WindowSwingPopup(peer, x, y, contents.getWidth(), contents.getHeight());
	final WindowSwingPopup wsp =  new WindowSwingPopup(peer, x, y, 50, 50);
	wsp.setComponent(contents);
	*/

	/* Old: Original Igor code
        WindowSwingEmbeddedPeer embeddedPeer = (WindowSwingEmbeddedPeer) peer;
        SGGroup topSGGroup = null;
        AffineTransform accTransform = new AffineTransform();
        Point offset = new Point(x, y);
        //find topmost embeddedPeer and accumulated transform
        while (embeddedPeer != null) {
            accTransform.preConcatenate(AffineTransform.getTranslateInstance(
                    offset.getX(), offset.getY()));
            SGLeaf leaf = embeddedPeer.getSGComponent();
            accTransform.preConcatenate(
                    leaf.getCumulativeTransform());
            JSGPanel jsgPanel = leaf.getPanel();
            System.out.println(jsgPanel);
            topSGGroup = jsgPanel.getSceneGroup();
            //check if it is an embedded JSGPanel
            WindowSwingEmbeddedPeer parentPeer = WindowSwingEmbeddedToolkit.getWindowSwingEmbeddedToolkit().getEmbeddedPeer(jsgPanel);
            if (parentPeer != null) {
                offset = SwingUtilities.convertPoint(
                        embeddedPeer.getEmbeddedComponent(),
                        0, 0,
                        parentPeer.getEmbeddedComponent());
            }
            embeddedPeer = parentPeer;
        }
        final SGComponent sgComponent = new SGComponent();
        sgComponent.setComponent(contents);
        final SGTransform.Affine sgTransform = 
            SGTransform.createAffine(accTransform, sgComponent);
        sgTransform.setVisible(false);
        topSGGroup.add(sgTransform);
	*/

	/*
        return new Popup() {
            @Override
            public void show() {
		wsp.setShowing(true);
            } 
            @Override
            public void hide() {
		wsp.setShowing(false);
            }
        };
	*/
    }
    
    static class WindowSwingEmbeddedPeer extends EmbeddedPeer {

	private static PainterThread painterThread;

	WindowSwingEmbeddedToolkit toolkit;

        private WindowSwing windowSwing = null;

        protected WindowSwingEmbeddedPeer(JComponent parent, Component embedded, WindowSwingEmbeddedToolkit toolkit) {
            super(parent, embedded);
	    this.toolkit = toolkit;

	    painterThread = new PainterThread();
	    painterThread.start();
        }

	void repaint () {
	    // TODO: for now
	    repaint(0, 0, 0, 0);
	}

        @Override
	public void repaint(int x, int y, int width, int height) {

            if (windowSwing == null) {
                return;
            }

	    paintOnWindow(windowSwing);

  	    /* TODO: if I do this it needs to be in the painter thread
            Component embedded = getEmbeddedComponent();
            int compX0 = embedded.getX();
            int compY0 = embedded.getY();
            int compX1 = compX0 + embedded.getWidth();
            int compY1 = compY0 + embedded.getHeight();
            int x0 = Math.max(x, compX0);
            int y0 = Math.max(y, compY0);
            int x1 = Math.min(x + width, compX1);
            int y1 = Math.min(y + height, compY1);
            
            if (x0 == compX0 && y0 == compY0
                    && x1 == compX1 && y1 == compY1) {
		// TODO: windowSwing
                sgComponent.repaint(false);
            } else if (x1 > x0 && y1 > y0){
		// TODO: windowSwing
                sgComponent.repaint(
                        new Rectangle2D.Float(x0, y0, x1 - x0, y1 - y0));
            }
	    */
        }

        void setWindowSwing(WindowSwing windowSwing) {
            this.windowSwing = windowSwing;
        }

        WindowSwing getWindowSwing () {
            return windowSwing;
        }

	protected EmbeddedToolkit<?> getEmbeddedToolkit () {
	    return toolkit;
	}
    
        @Override
        protected void sizeChanged(Dimension oldSize, Dimension newSize) {
	    /* TODO
            if (getSGComponent() != null) {
                getSGComponent().repaint(true);
            }
	    */
        }

	private void paintOnWindow (WindowSwing window) {
	    painterThread.enqueuePaint(this, window);
	}

	private static class PainterThread extends Thread {

	    private static final Logger logger = Logger.getLogger(PainterThread.class.getName());

	    private static class PaintRequest {
		private WindowSwingEmbeddedPeer embeddedPeer;
		private WindowSwing window;
		private PaintRequest (WindowSwingEmbeddedPeer embeddedPeer, WindowSwing window) {
		    this.embeddedPeer = embeddedPeer;
		    this.window = window;
		}
	    }

	    private LinkedBlockingQueue<PaintRequest> queue = new LinkedBlockingQueue<PaintRequest>();

	    private PainterThread () {
		super("WindowSwingEmbeddedPeer-PainterThread");
	    }

	    private void enqueuePaint (WindowSwingEmbeddedPeer embeddedPeer, WindowSwing window) {
		queue.add(new PaintRequest(embeddedPeer, window));
	    }

	    public void run () {
		while (true) {
		    try {
			PaintRequest request = null;
			request = queue.take();
			DrawingSurface drawingSurface = request.window.getSurface();
			Graphics2D gDst = drawingSurface.getGraphics();
			request.embeddedPeer.paint(gDst);
		    } catch (Exception ex) {
			ex.printStackTrace();
			logger.warning("Exception caught in WindowSwingEmbeddedToolkit Painter Thread.");
		    }
		}
	    }
	}
    }
}
