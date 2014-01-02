/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005-
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom.hanim;

// External imports
import java.nio.FloatBuffer;
import java.util.ArrayList;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.renderer.aviatrix3d.nodes.BufferGeometry;

import org.j3d.geom.hanim.HAnimHumanoid;
import org.j3d.geom.hanim.HAnimObject;

/**
 * Common AV3D implementation of the Humanoid object that uses software
 * to implement the mesh skinning algorithm.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public abstract class SoftwareHumanoid extends AVHumanoid
    implements NodeUpdateListener
{
    /** Group for containing the skin Shape nodes */
    protected Group skinGroup;

    /** Geometry instances that implement BufferGeometry */
    protected ArrayList<Geometry> bufferGeometry;

    /** Geometry instances that implement VertexGeometry */
    protected ArrayList<Geometry> vertexGeometry;

    /** Collection of nodes that is the skin */
    protected Node[] skin;

    /** The number of valid items in the skin list */
    protected int numSkin;

    /**
     * An array for coordinates to be splatted to if the contained geometry
     * is an instance of VertexGeometry.
     */
    protected float[] coordsArray;

    /**
     * An array for coordinates to be splatted to if the contained geometry
     * is an instance of VertexGeometry.
     */
    protected float[] normalsArray;

    /**
     * An array for coordinates to be splatted to if the contained geometry
     * is an instance of VertexGeometry.
     */
    protected FloatBuffer coordsBuffer;

    /**
     * An array for coordinates to be splatted to if the contained geometry
     * is an instance of VertexGeometry.
     */
    protected FloatBuffer normalsBuffer;

    /**
     * A list of per-vertex flags to indicate which ones have changed since the
     * last update. This is so that we don't unneccesarily modify the
     * coordinates of the system that haven't changed due to the skeleton not
     * changing in that area. When we clear the output coordinate/normal array,
     * we only clear those that have marked their flag here.
     */
    protected boolean[] dirtyCoordinates;

    /**
     * Create a new, default instance of the site.
     */
    SoftwareHumanoid()
    {
        bufferGeometry = new ArrayList<Geometry>();
        vertexGeometry = new ArrayList<Geometry>();

        skinGroup = new Group();
        addNode(skinGroup);
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src)
    {
        if(src == skinGroup)
        {
            skinGroup.removeAllChildren();

            for(int i = 0; i < numSkin; i++)
                skinGroup.addChild(skin[i]);
        }
        else if(src instanceof BufferGeometry)
        {
            ((BufferGeometry)src).setVertices(BufferGeometry.COORDINATE_3,
                                              coordsBuffer);
        }
        else if(src instanceof VertexGeometry)
        {
            ((VertexGeometry)src).setVertices(BufferGeometry.COORDINATE_3,
                                              coordsArray);
        }
        else
            super.updateNodeBoundsChanges(src);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src)
    {
        if(src instanceof BufferGeometry)
        {
            ((BufferGeometry)src).setNormals(normalsBuffer);
        }
        else if(src instanceof VertexGeometry)
        {
            ((VertexGeometry)src).setNormals(normalsArray);
        }
        else
            super.updateNodeDataChanges(src);
    }


    //----------------------------------------------------------
    // Methods defined by HAnimHumanoid
    //----------------------------------------------------------

    /**
     * Set a new value for the skinCoord of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * a multiple of 3 units long.
     *
     * @param val The new skinCoord value to use
     * @param numElements The number of 3d-vectors in the array
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setSkinCoord(float[] val, int numElements)
    {
        super.setSkinCoord(val, numElements);

        if(coordsBuffer == null || coordsBuffer.capacity() < numElements * 3)
            coordsBuffer = createBuffer(numElements * 3);

        // Copy the skin to the output for the first frame.
        coordsBuffer.rewind();
        coordsBuffer.put(val, 0, numElements * 3);

        if(dirtyCoordinates == null || dirtyCoordinates.length < numElements)
        {
            dirtyCoordinates = new boolean[numElements];

            if(rootJoint != null)
                ((SoftwareJoint)rootJoint).setDirtyList(dirtyCoordinates);
        }

        // Every coordinate is dirty to start with
        for(int i = 0; i < numElements; i++)
            dirtyCoordinates[i] = true;
    }

    /**
     * Set a new value for the skinNormal of this joint. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * a multiple of 3 units long.
     *
     * @param val The new skinNormal value to use
     * @param numElements The number of 3d-vectors in the array
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    public void setSkinNormal(float[] val, int numElements)
    {
        super.setSkinNormal(val, numElements);

        if(normalsBuffer == null || normalsBuffer.capacity() < numElements * 3)
            normalsBuffer = createBuffer(numElements * 3);

        normalsBuffer.rewind();
        normalsBuffer.put(val, 0, numElements * 3);
    }

    /**
     * Replace the existing viewpoints with the new set of viewpoints.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     */
    public void setSkeleton(HAnimObject[] kids, int numValid)
    {
        super.setSkeleton(kids, numValid);

        if((rootJoint != null) && dirtyCoordinates != null)
            ((SoftwareJoint)rootJoint).setDirtyList(dirtyCoordinates);
    }

    /**
     * All the skeletal changes are in for this frame, so update the matrix
     * values now. If nothing has changed, don't bother doing any calculations
     * and return immediately.
     */
    public void updateSkeleton()
    {
        // keep the flag because the superclass is going to overwrite it.
        // Need to update the skeleton first.
        boolean geom_changed = skeletonChanged;

        super.updateSkeleton();

        int size = numSkinCoords / 3;
        for(int i = 0; i < size; i++)
            dirtyCoordinates[i] = false;

        if(geom_changed)
        {
            if(numSkinCoords != 0)
            {
                size = bufferGeometry.size();
                for(int i = 0; i < size; i++)
                {
                    Geometry geom = bufferGeometry.get(i);
                    geom.boundsChanged(this);
                }

                size = vertexGeometry.size();
                for(int i = 0; i < size; i++)
                {
                    Geometry geom = vertexGeometry.get(i);
                    geom.boundsChanged(this);
                }
            }

            if(numSkinNormals != 0)
            {
                size = bufferGeometry.size();
                for(int i = 0; i < size; i++)
                {
                    Geometry geom = bufferGeometry.get(i);
                    geom.dataChanged(this);
                }

                size = vertexGeometry.size();
                for(int i = 0; i < size; i++)
                {
                    Geometry geom = vertexGeometry.get(i);
                    geom.dataChanged(this);
                }
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by AVHumanoid
    //----------------------------------------------------------

    /**
     * Set the list of geometry that should be used by this humanoid. These are
     * not directly inserted into the scene graph, but are used for reference to
     * generate our own internal model. When skinned mesh animation takes place,
     * the coordinates are automatically fed from this humanoid to the appropriate
     * geometry. For best performance, the geometry should make use of
     * {@link org.j3d.renderer.aviatrix3d.nodes.BufferGeometry} class.
     *
     * @param skins List of representative shapes to use
     * @param numSkins The number of valid items to use from the skins array
     */
    public void setSkin(Node[] skins, int numSkins)
    {
        // Walk the nodes looking for geometry
        bufferGeometry.clear();
        vertexGeometry.clear();

        for(int i = 0; i < numSkins; i++)
        {
            if(skins[i] instanceof Shape3D)
            {
                Geometry geom = ((Shape3D)skins[i]).getGeometry();

                if(geom instanceof BufferGeometry)
                    bufferGeometry.add(geom);
                else if(geom instanceof VertexGeometry)
                    vertexGeometry.add(geom);
            }
            else if(skins[i] instanceof Group)
            {
                descendGroup((Group)skins[i]);
            }
        }

        if(skinGroup.isLive())
            skinGroup.boundsChanged(this);
        else
        {
            for(int i = 0; i < numSkins; i++)
                skinGroup.addChild(skins[i]);
        }
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Get the internal representation of the updated mesh skin coordinates.
     *
     * @return An object that is either a float[] or FloatBuffer, depending on
     *    the internal implementation used.
     */
    public abstract Object getUpdatedSkinCoords();

    /**
     * Get the internal representation of the updated mesh skin normals.
     *
     * @return An object that is either a float[] or FloatBuffer, depending on
     *    the internal implementation used.
     */
    public abstract Object getUpdatedSkinNormals();

    /**
     * Descend down through to the layers of hell, in search of the holy
     * Geometry instance.
     *
     * @param parent The gateway to hell
     */
    private void descendGroup(Group parent)
    {
        int num_kids = parent.numChildren();
        for(int i = 0; i < num_kids; i++)
        {
            Node kid = parent.getChild(i);

            if(kid instanceof Shape3D)
            {
                Geometry geom = ((Shape3D)kid).getGeometry();

                if(geom instanceof BufferGeometry)
                    bufferGeometry.add(geom);
                else if(geom instanceof VertexGeometry)
                    vertexGeometry.add(geom);
            }
            else if(kid instanceof Group)
            {
                descendGroup((Group)kid);
            }
        }
    }
}
