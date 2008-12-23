/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.client.cell;

import com.jme.math.Vector3f;

/**
 *
 * @author jordanslott
 */
public class ControlCube {

    private Vector3f[] vertices = new Vector3f[8];
    private Vector3f[][] edges = new Vector3f[12][2];
    
    public ControlCube(Vector3f center, float xExtent, float yExtent, float zExtent) {
        computeVertices(center, xExtent, yExtent, zExtent);
        computeEdges();
    }
    
    public Vector3f[] getVertices() {
        return vertices;
    }
    
    public Vector3f[][] getEdges() {
        return edges;
    }
    
    /**
     * Computes the vertices given the center, and extents in each axis.
     */
    private void computeVertices(Vector3f center, float xExtent, float yExtent, float zExtent) {
        vertices[0] = new Vector3f(center.x - xExtent, center.y - yExtent, center.z + zExtent);
        vertices[1] = new Vector3f(center.x - xExtent, center.y + yExtent, center.z + zExtent);
        vertices[2] = new Vector3f(center.x + xExtent, center.y + yExtent, center.z + zExtent);
        vertices[3] = new Vector3f(center.x + xExtent, center.y - yExtent, center.z + zExtent);
        vertices[4] = new Vector3f(center.x - xExtent, center.y - yExtent, center.z - zExtent);
        vertices[5] = new Vector3f(center.x - xExtent, center.y + yExtent, center.z - zExtent);
        vertices[6] = new Vector3f(center.x + xExtent, center.y + yExtent, center.z - zExtent);
        vertices[7] = new Vector3f(center.x + xExtent, center.y - yExtent, center.z - zExtent);
    }
    
    /**
     * Computes the edges once the vertices have been computed
     */
    private void computeEdges() {
        edges[0][0] = vertices[0]; edges[0][1] = vertices[1];
        edges[1][0] = vertices[1]; edges[1][1] = vertices[2];
        edges[2][0] = vertices[2]; edges[2][1] = vertices[3];
        edges[3][0] = vertices[3]; edges[3][1] = vertices[0];
        edges[4][0] = vertices[4]; edges[4][1] = vertices[5];
        edges[5][0] = vertices[5]; edges[5][1] = vertices[6];
        edges[6][0] = vertices[6]; edges[6][1] = vertices[7];
        edges[7][0] = vertices[7]; edges[7][1] = vertices[4];
        edges[8][0] = vertices[0]; edges[8][1] = vertices[4];
        edges[9][0] = vertices[1]; edges[9][1] = vertices[5];
        edges[10][0] = vertices[2]; edges[10][1] = vertices[6];
        edges[11][0] = vertices[3]; edges[11][1] = vertices[7];
    }
}
