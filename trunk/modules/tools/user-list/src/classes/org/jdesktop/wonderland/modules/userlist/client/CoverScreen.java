/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.userlist.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;

/**
 *
 * @author Abhishek Upadhyay
 */
public class CoverScreen extends javax.swing.JPanel {

    private CoverScreenData coverScreenData=null;
    private JLabel closeIcon = null;
    private CoverScreenMainPanel csp = null;
    private int i=0;
    private Image img = null;
    
    /**
     * Creates new form CoverScreen
     */
    public CoverScreen(CoverScreenData coverScreenData,Dimension dim) {
        this.coverScreenData = coverScreenData;
        initComponents();
        csp = new CoverScreenMainPanel();
        csp.addComponentListener(new ComponentAdapter() {

            public void componentResized(ComponentEvent e) {
                Insets insets = ((JPanel)e.getComponent()).getInsets();
                Dimension size = closeIcon.getPreferredSize();
                closeIcon.setBounds((int) (((JPanel)e.getComponent()).getPreferredSize().getWidth()-size.width) + insets.left, insets.top,
                size.width, size.height);
            }

        });
        csp.setLayout(null);
        closeIcon = new JLabel();
        csp.setPreferredSize(dim.getSize());
        closeIcon.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        closeIcon.setLocation(100, 100);
        
        closeIcon.setHorizontalAlignment(JLabel.RIGHT);
        closeIcon.setPreferredSize(new Dimension(10, 17));
        closeIcon.setText("X");
        closeIcon.setOpaque(true);
        if(coverScreenData!=null) {
            float[] comps = coverScreenData
                            .getBackgroundColor().getColorArray();
            csp.setBackground(new Color(comps[0],comps[1],comps[2]));
        }
        
        csp.add(closeIcon);
        Insets insets = csp.getInsets();
        Dimension size = closeIcon.getPreferredSize();
        closeIcon.setBounds((int) (csp.getPreferredSize().getWidth()-size.width) + insets.left, insets.top,
        size.width, size.height);
        jPanel1.add(csp, BorderLayout.CENTER);
        setPreferredSize(null);
        
        if(coverScreenData.getImageURL()!=null && 
                        (!coverScreenData.getImageURL().equals(""))) {
            try {
                img = csp.scaleImage(AssetUtils
                                .getAssetURL(coverScreenData.getImageURL()),(int)dim.getSize().getWidth()
                        ,(int)dim.getSize().getHeight());
            } catch (MalformedURLException ex) {
                Logger.getLogger(CoverScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

    public JLabel getCloseComponent() {
        return closeIcon;
    }

    private class CoverScreenMainPanel extends JPanel {
        ImageIcon ii=null;

        private Image scaleImage(URL imageURL,int width, int height) {
            try {
                Image scaledBimg = null;
                ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
                ContentRepository cr = registry.getRepository(LoginManager.getPrimary());
                String[] urls = imageURL.toString().split("/");
                String fname = urls[urls.length-1];
                String userName = urls[3];
                ContentCollection user = cr.getUserRoot(userName);
                
                ContentResource res = (ContentResource) user.getChild(fname);
                
                BufferedImage bimg = ImageIO.read(res.getInputStream());
                if(bimg.getWidth()>width || bimg.getHeight()>height) {
                    if(bimg.getWidth()>width && bimg.getHeight()>height) {
                        if(bimg.getWidth()-width>bimg.getHeight()-height) {
                            float nw = width;
                            float nh = (width*bimg.getHeight())/bimg.getWidth();
                            scaledBimg = bimg.getScaledInstance((int)nw, (int)nh,BufferedImage.SCALE_SMOOTH);
                            //scaledBimg.getGraphics().drawImage(bimg, 0, 0, null);
                        } else {
                            float nh = height;
                            float nw = (height*bimg.getWidth())/bimg.getHeight();
                            scaledBimg = bimg.getScaledInstance((int)nw, (int)nh,BufferedImage.SCALE_SMOOTH);
                            //scaledBimg.getGraphics().drawImage(bimg, 0, 0, null);
                        }
                    } else if(bimg.getWidth()>width) {
                        float nw = width;
                        float nh = (width*bimg.getHeight())/bimg.getWidth();
                        scaledBimg = bimg.getScaledInstance((int)nw, (int)nh,BufferedImage.SCALE_SMOOTH);
                    } else if(bimg.getHeight()>height) {
                        float nh = height;
                        float nw = (height*bimg.getWidth())/bimg.getHeight();
                        scaledBimg = bimg.getScaledInstance((int)nw, (int)nh,BufferedImage.SCALE_SMOOTH);
                    }
                } else {
                    scaledBimg = bimg;
                }
                return scaledBimg;
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(CoverScreen.class.getName()).log(Level.SEVERE, null, ex);
            } 
            return null;
        }
       
        @Override
        public void paintComponent(Graphics g) {
            
            super.paintComponent(g);
            if(coverScreenData!=null) {
                Graphics2D g2d = (Graphics2D) g;
                if(coverScreenData.getImageURL()!=null && 
                        (!coverScreenData.getImageURL().equals(""))) {
                    
                    try {
                        g2d.drawImage(img, (getWidth()/2)-(img.getWidth(null)/2)
                                , (getHeight()/2)-(img.getHeight(null)/2), this);
                    } catch (Exception ex) {
                        Logger.getLogger(CoverScreen.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } 
                Image img = new ImageIcon(getClass()
                            .getResource("/org/jdesktop/wonderland/modules/placemarks/client/resources/loading.gif")).getImage();
                    
                g2d.drawImage(img, (getWidth()/2)-(img.getWidth(null)/2), (getHeight()/2)-(img.getHeight(null)/2)
                        , this);
                float[] comps = coverScreenData
                .getTextColor().getColorArray();
                g2d.setColor(new Color(comps[0],comps[1],comps[2]));
                Font font = new Font("Arial", Font.BOLD,20);
                FontRenderContext frc1 = new FontRenderContext(null, true, true);
                Rectangle2D rec = font.getStringBounds(coverScreenData.getMessage(), frc1);
                int w = (int) (rec.getWidth());
                int h = (int) (rec.getHeight());
                g2d.setFont(font);
                g2d.drawString(coverScreenData.getMessage()
                    , (getWidth()/2)-(w/2), getHeight()-(h)+10  );
            } 
            Insets insets = getInsets();
            Dimension size = closeIcon.getPreferredSize();
            closeIcon.setBounds((int) (getWidth()-size.width) + insets.left, insets.top,
            size.width, size.height);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        setBackground(new java.awt.Color(0, 0, 0));
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.BorderLayout());
        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
