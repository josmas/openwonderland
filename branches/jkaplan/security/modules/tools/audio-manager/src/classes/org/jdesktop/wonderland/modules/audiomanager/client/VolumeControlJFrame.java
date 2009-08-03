/*
 * VolumeControlJFrame.java
 *
 * Created on February 5, 2009, 10:33 AM
 */

package org.jdesktop.wonderland.modules.audiomanager.client;

/**
 *
 * @author  jp
 */
public class VolumeControlJFrame extends javax.swing.JFrame {

    private VolumeChangeListener listener;

    /** Creates new form VolumeControlJFrame */
    public VolumeControlJFrame() {
        initComponents();
    }

    public VolumeControlJFrame(VolumeChangeListener listener, String title) {
        initComponents();

	setTitle(title);
	this.listener = listener;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        volumeControlSlider = new javax.swing.JSlider();

        setTitle("Volume Control");

        volumeControlSlider.setMajorTickSpacing(1);
        volumeControlSlider.setMaximum(10);
        volumeControlSlider.setMinorTickSpacing(1);
        volumeControlSlider.setPaintLabels(true);
        volumeControlSlider.setPaintTicks(true);
        volumeControlSlider.setSnapToTicks(true);
        volumeControlSlider.setValue(5);
        volumeControlSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                volumeControlSliderStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(47, 47, 47)
                .add(volumeControlSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 263, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(64, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(volumeControlSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void volumeControlSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeControlSliderStateChanged
	javax.swing.JSlider source = (javax.swing.JSlider) evt.getSource();

	listener.volumeChanged(source.getValue() / 5.0);
}//GEN-LAST:event_volumeControlSliderStateChanged

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VolumeControlJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider volumeControlSlider;
    // End of variables declaration//GEN-END:variables

}