
/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.aviatrix3d.examples.multipass;

import org.j3d.maths.vector.Vector3d;
import org.j3d.maths.vector.Vector4d;

/**
 * A triangle object containing index of the vertices
 * and a plane equation of this triangle.
 * 
 * @author Sang Park
 * @version $Revision: 1.1 $
 */
public class Triangle {

	/** Three triangle indexes */
	public int[] vertIndex = new int[3];
	
	/** Vector representing plane equation of this triangle */
	public Vector4d plane = new Vector4d();
	
	/**
	 * Constructs instance of this triangle
	 * from another triangle object.
	 * 
	 * @param tri Instance of a triangle object
	 */
	public Triangle(Triangle tri) {
		
		plane.x = tri.plane.x;
		plane.y = tri.plane.y;
		plane.z = tri.plane.z;
		plane.w = tri.plane.w;
		
		vertIndex[0] = tri.vertIndex[0];
		vertIndex[1] = tri.vertIndex[1];
		vertIndex[2] = tri.vertIndex[2];
	}
	
	/**
	 * Construts instance of this triangle
	 * 
	 * @param idx1 First index of triangle
	 * @param idx2 Second index of triangle
	 * @param idx3 Third index of triangle
	 * @param coords Lists of vertices
	 */
	public Triangle(int idx1, int idx2, int idx3, float[] coords) {

		Vector3d vert1 = new Vector3d();
		Vector3d vert2 = new Vector3d();
        Vector3d vert3 = new Vector3d();

        vert1.set(coords[idx1 + 0], coords[idx1 + 1], coords[idx1 + 2]);
        vert2.set(coords[idx2 + 0], coords[idx2 + 1], coords[idx2 + 2]);
        vert3.set(coords[idx3 + 0], coords[idx3 + 1], coords[idx3 + 2]);
		
		vert2.sub(vert2, vert1);
		vert3.sub(vert3, vert1);
		
		Vector3d cross = new Vector3d();
		cross.cross(vert2, vert3);
		
		plane.x = cross.x;
		plane.y = cross.y;
		plane.z = cross.z;
		plane.w = -cross.dot(vert1);
		
		vertIndex[0] = idx1;
		vertIndex[1] = idx2;
		vertIndex[2] = idx3;
	}
}
