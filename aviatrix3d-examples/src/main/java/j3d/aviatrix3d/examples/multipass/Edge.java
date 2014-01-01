
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

/**
 * A structure that contains pair of edges
 * 
 * @author Sang Park
 * @version $Revision: 1.1 $
 */
public class Edge {

	/** Vertex index */
	public int[] vertexIndex = new int[2];
	
	/** Triangle index */
	public int[] triangleIndex = new int[2];
	
	/**
	 * Constructs instance of silhouette edge
	 * 
	 * @param edge Silhouette edge instance to copy from
	 */
	public Edge(Edge edge) {
		
		this(edge.vertexIndex[0], edge.vertexIndex[1], edge.triangleIndex[0], edge.triangleIndex[1]);
	}

	/**
	 * Constructs instance of silhouette edge
	 * 
	 * @param vtxIdx1 Index of the coordinate 1
	 * @param vtxIdx2 Index of the coordinate 2
	 * @param triIdx1 Triangle index 1
	 * @param triIdx2 Triangle index 2
	 */
	public Edge(int vtxIdx1, int vtxIdx2, int triIdx1, int triIdx2) {
		
		vertexIndex[0] = vtxIdx1;
		vertexIndex[1] = vtxIdx2;
		
		triangleIndex[0] = triIdx1;
		triangleIndex[1] = triIdx2;
	}
	
	/**
	 * Create default instance of silhouette with all the index set to minus one
	 */
	public Edge() {
		
		vertexIndex[0] = -1;
		vertexIndex[1] = -1;
		
		triangleIndex[0] = -1;
		triangleIndex[1] = -1;
	}
}
