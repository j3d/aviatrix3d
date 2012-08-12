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
import java.util.ArrayList;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.renderer.aviatrix3d.nodes.BufferGeometry;

import org.j3d.geom.hanim.HAnimHumanoid;
import org.j3d.geom.hanim.HAnimObject;
import org.j3d.util.ErrorReporter;

/**
 * Common AV3D implementation of the Humanoid object that uses GLSL shaders
 * to implement the mesh skinning algorithm.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
class ShaderHumanoid extends AVHumanoid
    implements NodeUpdateListener, ShaderObjectParent
{
    /** Arguments used to feed the shader with */
    private ShaderArguments shaderArgs;

    /** Complete program, if needed, that holds vertex shader and args */
    private HAnimShader completeShader;

    /** List of per-vertex weights assigned per index. Max 4 per index. */
    private float[] vertexWeights;

    /** List of per-vertex matrix bones assigned per index. Max 4 per index. */
    private float[] vertexMatrixIndices;

    /** The count of the number of bones effecting each vertex. Max 4 per index */
    private float[] vertexBoneCount;

    /** 4x4 Matrix per bone. */
    private float[] boneMatrices;

    /** Group for containing the skin Shape nodes */
    private Group skinGroup;

    /** Geometry instances that implement BufferGeometry */
    private ArrayList<BufferGeometry> bufferGeometry;

    /** Geometry instances that implement VertexGeometry */
    private ArrayList<VertexGeometry> vertexGeometry;

    /** Collection of nodes that is the skin */
    private Node[] skin;

    /** The number of valid items in the skin list */
    private int numSkin;

    /** Children that have requested attribute updates */
    private ArrayList<ShaderJoint> updatedChildAttribList;

    /**
     * Create a new, default instance of the site.
     */
    ShaderHumanoid()
    {
        bufferGeometry = new ArrayList<BufferGeometry>();
        vertexGeometry = new ArrayList<VertexGeometry>();
        updatedChildAttribList = new ArrayList<ShaderJoint>();

        skinGroup = new Group();
        addNode(skinGroup);

        completeShader = new HAnimShader();
        shaderArgs = completeShader.getShaderArguments();
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
        if(src instanceof VertexGeometry)
            setAttributes((VertexGeometry)src);
        else if(src instanceof BufferGeometry)
            setAttributes((BufferGeometry)src);
        else
            super.updateNodeDataChanges(src);
    }

    //----------------------------------------------------------
    // Methods defined by HAnimObject
    //----------------------------------------------------------

    /**
     * Register an error reporter with the object so that any errors generated
     * by the object can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter)
    {
        super.setErrorReporter(reporter);

        completeShader.setErrorReporter(reporter);
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

        if(vertexWeights == null || vertexWeights.length < numElements * 4)
        {
            vertexWeights = new float[numElements * 4];
            vertexMatrixIndices = new float[numElements * 4];
            vertexBoneCount = new float[numElements];
        }

        updateAttributes();
    }

    /**
     * Replace the existing skeleton with the new set of skeleton. The skeleton
     * can only consist of a single Joint and multiple Site objects. Any other
     * HAnim object types shall issue an exception.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     * @throws IllegalArgumentException One of the provided s
     */
    public void setSkeleton(HAnimObject[] kids, int numValid)
    {
        super.setSkeleton(kids, numValid);

        updateAttributes();
    }

    /**
     * All the skeletal changes are in for this frame, so update the matrix
     * values now. If nothing has changed, don't bother doing any calculations
     * and return immediately.
     */
    public void updateSkeleton()
    {
        // keep this so that we know if we have to recalculate the matrices
        // and update the shader
        boolean has_changed = skeletonChanged || matrixChanged;

        if(boneMatrices == null || boneMatrices.length < objectCount * 16)
        {
            boneMatrices = new float[objectCount * 16];
            has_changed = true;
        }

        super.updateSkeleton();

        // After the matrices have been recalculated, update them all into the
        // global array that we'll be passing to the shader.
        if(has_changed)
        {
            for(int i = 0; i < numSkeleton; i++)
            {
                if(skeleton[i] instanceof ShaderJoint)
                    ((ShaderJoint)skeleton[i]).updateMatrices(boneMatrices);
            }

            shaderArgs.setUniformMatrix("boneMatrix",
                                        4,
                                        boneMatrices,
                                        objectCount,
                                        true);
        }

        int size = updatedChildAttribList.size();

        if(size != 0)
        {
            for(int i = 0; i < size; i++)
            {
                ShaderJoint joint = updatedChildAttribList.get(i);
                joint.updateAttributes(vertexWeights,
                                       vertexMatrixIndices,
                                       vertexBoneCount,
                                       false);
            }

            updatedChildAttribList.clear();

            size = vertexGeometry.size();
            for(int i = 0; i < size; i++)
                vertexGeometry.get(i).dataChanged(this);

            size = bufferGeometry.size();
            for(int i = 0; i < size; i++)
                bufferGeometry.get(i).dataChanged(this);
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
     * {@link org.j3d.renderer.aviatrix3d.geom.BufferGeometry} class.
     *
     * @param shapes List of representative shapes to use
     */
    public void setSkin(Node[] skins, int numSkins)
    {
        // Walk the nodes looking for geometry
        bufferGeometry.clear();
        vertexGeometry.clear();

        int max_textures = 0;
        for(int i = 0; i < numSkins; i++)
        {
            int tex_cnt = 0;

            if(skins[i] instanceof Shape3D)
                tex_cnt = updateShader((Shape3D)skins[i]);
            else if(skins[i] instanceof Group)
                tex_cnt = descendGroup((Group)skins[i]);
            else if(skins[i] instanceof SharedNode)
            {
                Node kid = ((SharedNode)skins[i]).getChild();

                while(kid instanceof SharedNode)
                    kid = ((SharedNode)kid).getChild();

                if(kid instanceof Shape3D)
                    tex_cnt = updateShader((Shape3D)kid);
                else if(kid instanceof Group)
                    tex_cnt = descendGroup((Group)kid);
            }

            if(max_textures < tex_cnt)
                max_textures = tex_cnt;
        }

        completeShader.setMaxTextureUnits(max_textures);

        if(skinGroup.isLive())
            skinGroup.boundsChanged(this);
        else
        {
            for(int i = 0; i < numSkins; i++)
                skinGroup.addChild(skins[i]);
        }
    }

    //----------------------------------------------------------
    // Methods defined by ShaderObjectParent
    //----------------------------------------------------------

    /**
     * Notification that the child has changed something in the attributes and
     * will need to reset the new values. A change could be in the weights,
     * indexed fields or new children added.
     *
     * @param child Reference to the child that has changed
     */
    public void childAttributesChanged(HAnimObject child)
    {
        // Only care about ShaderJoint nodes here
        if(!(child instanceof ShaderJoint))
            return;

        ShaderJoint j = (ShaderJoint)child;

        if(!updatedChildAttribList.contains(j))
            updatedChildAttribList.add(j);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Convenience method to clear out the existing attributes and create
     * a whole new set for passing to the shader.
     */
    private void updateAttributes()
    {
        for(int i = 0; i < numSkinCoords / 3; i++)
            vertexBoneCount[i] = 0;

        for(int i = 0; i < numSkeleton; i++)
        {
            if(skeleton[i] instanceof ShaderJoint)
                ((ShaderJoint)skeleton[i]).updateAttributes(vertexWeights,
                                                            vertexMatrixIndices,
                                                            vertexBoneCount,
                                                            true);
        }

        // Also need to clear out any pending updates requested as we've just
        // covered everything in the that last loop.
        updatedChildAttribList.clear();

        int size = vertexGeometry.size();
        for(int i = 0; i < size; i++)
        {
            VertexGeometry geom = (VertexGeometry)vertexGeometry.get(i);
            if(geom.isLive())
                geom.dataChanged(this);
            else
                setAttributes(geom);
        }

/*
        size = bufferGeometry.size();
        for(int i = 0; i < size; i++)
        {
            BufferGeometry geom = (BufferGeometry)bufferGeometry.get(i);
            if(geom.isLive())
                geom.dataChanged(this);
            else
                setAttributes(s_geom);
        }
*/
    }

    /**
     * Descend down through to the layers of hell, in search of the holy
     * Geometry instance.
     *
     * @param parent The gateway to hell
     * @return The maximum number of texture units found in group's children
     */
    private int descendGroup(Group parent)
    {
        int num_kids = parent.numChildren();
        int max_textures = 0;

        for(int i = 0; i < num_kids; i++)
        {
            Node kid = parent.getChild(i);
            int tex_cnt = 0;

            if(kid instanceof Shape3D)
                tex_cnt = updateShader((Shape3D)kid);
            else if(kid instanceof Group)
                tex_cnt = descendGroup((Group)kid);
            else if(kid instanceof SharedNode)
            {
                Node s_kid = ((SharedNode)kid).getChild();

                while(s_kid instanceof SharedNode)
                    s_kid = ((SharedNode)s_kid).getChild();

                if(s_kid instanceof Shape3D)
                    tex_cnt = updateShader((Shape3D)s_kid);
                else if(s_kid instanceof Group)
                    tex_cnt = descendGroup((Group)s_kid);
            }

            if(max_textures < tex_cnt)
                max_textures = tex_cnt;
        }

        return max_textures;
    }

    /**
     * From the given shape node, update the shader information. Both
     * the shader is set, and the per-vertex attributes are set.
     *
     * @param shape The shape instance to work on
     * @return The number of texture units found
     */
    private int updateShader(Shape3D shape)
    {
        int ret_val = 0;

        Appearance app = shape.getAppearance();
        Geometry s_geom = shape.getGeometry();

        if(s_geom instanceof BufferGeometry)
            bufferGeometry.add((BufferGeometry)s_geom);
        else if(s_geom instanceof VertexGeometry)
            vertexGeometry.add((VertexGeometry)s_geom);


        if(app == null)
        {
            app = new Appearance();
            shape.setAppearance(app);
        }
        else
            ret_val = app.numTextureUnits();

        app.setShader(completeShader);

        if(s_geom != null)
        {
            if(s_geom.isLive())
                s_geom.dataChanged(this);
            else
                setAttributes(s_geom);
        }

        return ret_val;
    }

    /**
     * Internal convenience method to commonise the setting of per-vertex
     * attributes.
     *
     * @param geom The geometry instance to set them on
     */
    private void setAttributes(Geometry geom)
    {
        if(geom instanceof VertexGeometry)
        {
/*
System.out.println("Setting vertexWeights");
for(int i=0; i < vertexWeights.length; i++)
    System.out.print(vertexWeights[i] + " ");
System.out.println();
System.out.println("Setting vertexMatrixIndices");
for(int i=0; i < vertexWeights.length; i++)
    System.out.print(vertexMatrixIndices[i] + " ");
System.out.println();
*/
            VertexGeometry v_geom = (VertexGeometry)geom;
            v_geom.setAttributes(HAnimShader.WEIGHT_ATTRIB_INDEX,
                                 4,
                                 vertexWeights,
                                 false);

            v_geom.setAttributes(HAnimShader.MATRIX_ATTRIB_INDEX,
                                 4,
                                 vertexMatrixIndices,
                                 false);

        }
/*
        else if(geom instanceof BufferGeometry)
        {
            BufferGeometry b_geom = (BufferGeometry)geom;
            b_geom.setAttributes(WEIGHT_ATTRIB_INDEX,
                                 4,
                                 vertexWeights,
                                 false);

            b_geom.setAttributes(MATRIX_ATTRIB_INDEX,
                                 4,
                                 vertexMatrixIndices,
                                 false,
                                 false);

        }
*/
    }
}
