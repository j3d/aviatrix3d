/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
// External imports
import java.util.HashMap;
import java.awt.Point;
import org.j3d.aviatrix3d.IndexedTriangleArray;
// Local imports
/**
 * Index triangle array that contains lists of silhouetted edges.
 * 
 * @author Sang Park
 * @version $Revision: 1.3 $
 */
public class SEdgeIndTriArray extends IndexedTriangleArray {
	/** Number of triangles */
	private int numTriangles;
	/** Silhouette Edges */
	private Edge[] silhouetteEdge;
	/** Silhouette Triangles */
	private Triangle[] silhouetteTri;
	/**
	 * Constructs instance of this class
	 */
	public SEdgeIndTriArray() {
		super();
	}
    //------------------------------------------------------------------------
    // Methods defined by IndexedTriangleArray
    //------------------------------------------------------------------------
	/**
	 * Set index
	 * 
	 * @param indexList Array of indices
	 * @param num Number of indexes
	 */
	public void setIndices(int[] indexList, int num) {
		super.setIndices(indexList, num);
		numTriangles = num / 3;
		setSilhouetteEdge(indexList, num);
	}
    //------------------------------------------------------------------------
    // Local methods
    //------------------------------------------------------------------------
	/**
	 * Create silhouette edges from the index list
	 * 
	 * @param indexList Index list array
	 * @param num Numbers of indexes
	 */
	protected void setSilhouetteEdge(int[] indexList, int num) {
		silhouetteTri = new Triangle[num / 3];
		int counter = 0;
		for(int i = 0; i < silhouetteTri.length; i++) {
			silhouetteTri[i] = new Triangle(indexList[counter + 0] * 3,
										    indexList[counter + 1] * 3,
										    indexList[counter + 2] * 3,
										    coordinates);
			counter += 3;
		}
		
		Edge[] tmpsilhouetteEdge = new Edge[silhouetteTri.length * 3];
		for(int i = 0; i < tmpsilhouetteEdge.length; i++) {
			tmpsilhouetteEdge[i] = new Edge();
		}
		HashMap<Point, Integer> edgeList = new HashMap<Point, Integer>();
		int silHouetteCounter = 0;
		for(int i = 0; i < silhouetteTri.length; i++) {
			int i1 = silhouetteTri[i].vertIndex[0];
			int i2 = silhouetteTri[i].vertIndex[1];
			int i3 = silhouetteTri[i].vertIndex[2];
			if(edgeList.containsKey(new Point(i1, i2)) &&
			   edgeList.containsKey(new Point(i2, i1))) {
				int edgeIndex = edgeList.get(new Point(i1, i2));
				tmpsilhouetteEdge[edgeIndex].triangleIndex[1] = i;
			} else {
				edgeList.put(new Point(i1, i2), silHouetteCounter);
				edgeList.put(new Point(i2, i1), silHouetteCounter);
				tmpsilhouetteEdge[silHouetteCounter].triangleIndex[0] = i;
				tmpsilhouetteEdge[silHouetteCounter].triangleIndex[1] = -1;
				tmpsilhouetteEdge[silHouetteCounter].vertexIndex[0] = i1;
				tmpsilhouetteEdge[silHouetteCounter].vertexIndex[1] = i2;
				silHouetteCounter++;
			}
			if(edgeList.containsKey(new Point(i2, i3)) &&
			   edgeList.containsKey(new Point(i3, i2))) {
				int edgeIndex = edgeList.get(new Point(i2, i3));
				tmpsilhouetteEdge[edgeIndex].triangleIndex[1] = i;
			} else {
				edgeList.put(new Point(i2, i3), silHouetteCounter);
				edgeList.put(new Point(i3, i2), silHouetteCounter);
				
				tmpsilhouetteEdge[silHouetteCounter].triangleIndex[0] = i;
				tmpsilhouetteEdge[silHouetteCounter].triangleIndex[1] = -1;
				tmpsilhouetteEdge[silHouetteCounter].vertexIndex[0] = i2;
				tmpsilhouetteEdge[silHouetteCounter].vertexIndex[1] = i3;
				silHouetteCounter++;
			}
			if(edgeList.containsKey(new Point(i3, i1)) &&
			   edgeList.containsKey(new Point(i1, i3))) {
				int edgeIndex = edgeList.get(new Point(i3, i1));
				tmpsilhouetteEdge[edgeIndex].triangleIndex[1] = i;
			} else {
				edgeList.put(new Point(i3, i1), silHouetteCounter);
				edgeList.put(new Point(i1, i3), silHouetteCounter);
				
				tmpsilhouetteEdge[silHouetteCounter].triangleIndex[0] = i;
				tmpsilhouetteEdge[silHouetteCounter].triangleIndex[1] = -1;
				tmpsilhouetteEdge[silHouetteCounter].vertexIndex[0] = i3;
				tmpsilhouetteEdge[silHouetteCounter].vertexIndex[1] = i1;
				silHouetteCounter++;
			}
		}
		silhouetteEdge = new Edge[silHouetteCounter];
		for(int j = 0; j < silHouetteCounter; j++) {
			silhouetteEdge[j] = new Edge(tmpsilhouetteEdge[j].vertexIndex[0],
									     tmpsilhouetteEdge[j].vertexIndex[1],
									     tmpsilhouetteEdge[j].triangleIndex[0],
									     tmpsilhouetteEdge[j].triangleIndex[1]);
		}
		tmpsilhouetteEdge = null;
		edgeList.clear();
		edgeList = null;
	}
	/**
	 * Reset the each triangles plane equations
	 */
	public void resetPlaneEquations() {
		
		for(int i = 0; i < silhouetteTri.length; i++) {
			silhouetteTri[i] = new Triangle(silhouetteTri[i].vertIndex[0],
											silhouetteTri[i].vertIndex[1],
											silhouetteTri[i].vertIndex[2],
										    coordinates);
		}
		
	}
	
	/**
	 * Retrieves silhouette edges
	 * 
	 * @return Arrays of silhouette edges.
	 */
	public final Edge[] getSilhouetteEdge() {
		return silhouetteEdge;
	}
	
	/**
	 * Retrieves silhouette triangles
	 * 
	 * @return Array of triangles
	 */
	public final Triangle[] getSilhouetteTriangle() {
		return silhouetteTri;
	}
	/**
	 * Retrieves numbers of edges
	 * 
	 * @return Numbers of edges
	 */
	public int getSilhouetteEdgeCount() {
		return silhouetteEdge.length;
	}
	/**
	 * Retrieves numbers of triangles
	 * 
	 * @return Numbers of triangles
	 */
	public int getSilhouetteTriCount() {
		return silhouetteTri.length;
	}
}