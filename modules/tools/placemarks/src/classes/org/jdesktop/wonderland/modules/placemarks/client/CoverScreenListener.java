/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.placemarks.client;

import com.jme.math.Vector3f;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.MainFrame;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.placemarks.api.common.Placemark;
import org.jdesktop.wonderland.modules.placemarks.common.CoverScreenData;

/**
 *
 * @author Abhishek Upadhyay
 */
public class CoverScreenListener implements CellStatusChangeListener {

    private static final Logger LOGGER
            = Logger.getLogger(CoverScreenListener.class.getName());

    private int flg = 1;
    private Collection<Cell> rootCells = new ArrayList<Cell>();
    private CellStatusChangeListener csl;
    private ScheduledExecutorService exec = null;
    private ScheduledExecutorService exec_cellStatus = null;
    private JDialog dialog = null;
    private Vector3f goalPos = new Vector3f();
    int lock = 0;

    public CoverScreenListener(final Placemark placemark, final ServerSessionManager sessionManager,
            Vector3f goalPosP, String from, CoverScreenData csd) {
        if (goalPosP == null) {
            if (System.getProperty("x") == null || System.getProperty("y") == null
                    || System.getProperty("z") == null) {
                goalPosP = new Vector3f(0, 0, 0);
            } else {
                goalPosP = new Vector3f(Float.parseFloat(System.getProperty("x")), Float.parseFloat(System.getProperty("y")), Float.parseFloat(System.getProperty("z")));
            }
        }
        this.goalPos = goalPosP;

        Vector3f fromLoc = null;
        try {
            fromLoc = ClientContextJME.getViewManager()
                    .getPrimaryViewCell().getWorldTransform().getTranslation(null);
        } catch (Exception e) {
        }
        Vector3f toLoc = goalPos;
        if (fromLoc != null && goalPos != null && Math.abs((fromLoc.x) - (toLoc.x)) <= 150
                && Math.abs((fromLoc.y) - (toLoc.y)) <= 150
                && Math.abs((fromLoc.z) - (toLoc.z)) <= 150) {
            //Cover Screen not needed
        } else {
            //attach cover screen
            final MainFrame mainFrame = JmeClientMain.getFrame();
            final Canvas canvas = mainFrame.getCanvas();
            final CoverScreen coverScreenPanel = new CoverScreen(csd, canvas.getSize());

            try {

                //show cover screen in JDialog over the canvas panel
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        dialog = new JDialog(mainFrame.getFrame(), "Child", false);

                        coverScreenPanel.setPreferredSize(canvas.getSize());
                        dialog.setSize(canvas.getSize().width, canvas.getSize().height);
                        dialog.setLocation(canvas.getLocationOnScreen().x, canvas.getLocationOnScreen().y);

                        mainFrame.getFrame().addComponentListener(new ComponentAdapter() {
                            private void doChange() {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        if (dialog != null && dialog.isVisible()) {
                                            Component cs = dialog.getComponent(0);
                                            Canvas canvas = mainFrame.getCanvas();
                                            cs.setPreferredSize(canvas.getSize());
                                            dialog.setSize(canvas.getSize().width, canvas.getSize().height);
                                            dialog.setLocation(canvas.getLocationOnScreen().x, canvas.getLocationOnScreen().y);
                                            dialog.pack();
                                            dialog.setVisible(true);
                                        }
                                    }
                                });
                            }

                            public void componentResized(ComponentEvent e) {
                                doChange();
                            }

                            public void componentMoved(ComponentEvent e) {
                                doChange();
                            }
                        });
                        dialog.getRootPane().setOpaque(false);
                        dialog.add(coverScreenPanel);

                        dialog.setUndecorated(true);
                        dialog.pack();
                        dialog.setVisible(true);
                    }
                });

                //Listener for the close icon
                coverScreenPanel.getCloseComponent().addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (exec_cellStatus != null) {
                            exec_cellStatus.shutdown();
                            exec_cellStatus = null;
                        }
                        if (exec != null) {
                            exec.shutdown();
                            exec = null;
                        }
                        if (dialog != null) {
                            dialog.dispose();
                        }
                    }
                });
                ClientContextJME.getCellManager().addCellStatusChangeListener(this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private synchronized boolean checkIfAllCellsLoaded(Collection<Cell> rootCells, int level, boolean log) {

        Iterator<Cell> itr = rootCells.iterator();
        while (itr.hasNext()) {
            Cell c = itr.next();
            if (log) {
                for (int i = 0; i < level; i++) {
                    System.out.print("   ");
                }
                System.out.println(" " + c.getName() + " - " + c.getStatus());
            }
            List<Cell> childs = c.getChildren();
            if (!c.getStatus().equals(CellStatus.VISIBLE)) {
                return false;
            }
            if (childs.size() != 0) {
                if (!checkIfAllCellsLoaded(childs, level + 1, log)) {
                    return false;
                }
            }
        }
        return true;
    }

    public synchronized void cellStatusChanged(final Cell cell, CellStatus status) {
        try {
            if (status.equals(CellStatus.VISIBLE)
                    && (Math.abs(goalPos.x - cell.getWorldTransform().getTranslation(null).x) <= 150
                    && Math.abs(goalPos.y - cell.getWorldTransform().getTranslation(null).y) <= 150
                    && Math.abs(goalPos.z - cell.getWorldTransform().getTranslation(null).z) <= 150)) {

                if (flg == 1) {
                    csl = this;
                    exec_cellStatus = Executors.newSingleThreadScheduledExecutor();
                    exec_cellStatus.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public synchronized void run() {
                            try {
                                rootCells = cell.getCellCache().getRootCells();
                                if (rootCells.size() == 1) {

                                    exec_cellStatus.awaitTermination(5, TimeUnit.SECONDS);

                                }
                                if (checkIfAllCellsLoaded(rootCells, 0, false)) {
                                    exec_cellStatus.awaitTermination(3, TimeUnit.SECONDS);
                                    if (checkIfAllCellsLoaded(rootCells, 0, false)) {
                                        if (dialog != null) {
                                            checkIfAllCellsLoaded(rootCells, 0, true);
                                            dialog.dispose();
                                        }
                                        exec_cellStatus.shutdown();
                                        exec_cellStatus = null;
                                        ClientContextJME.getCellManager().removeCellStatusChangeListener(csl);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 0, 3, TimeUnit.SECONDS);
                    flg++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
