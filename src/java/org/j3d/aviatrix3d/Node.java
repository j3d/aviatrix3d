/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d;

// Standard imports
//import org.web3d.vecmath.Matrix4f;
import javax.vecmath.Matrix4f;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;

// Local imports
// None

/**
 * A Node class is the base class for all renderable nodes in the SceneGraph.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public abstract class Node extends SceneGraphObject
{
    /** The parent of this node */
    protected Node parent;

    /** The premultiplied transformation applied to this node */
    protected Matrix4f transform;

    /** Bounding volume set by the user */
    protected BoundingVolume bounds;

    /** Was the bounds automatically calculated? */
    protected boolean implicitBounds;

    /**
     * Construct a new instance of this node, with implicit bounds calculation.
     */
    protected Node()
    {
        implicitBounds = true;
    }

    /**
     * This method is called to render this node.  All openGL commands needed
     * to render the node should be executed.  Any transformations needed
     * should be added to the transformation stack premultiplied.  This
     * method must be re-entrant as it can be called from multiple
     * places at once.
     *
     * @param gld The drawable context to use
     */
    public void render(GL gl, GLU glu)
    {
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     *
     * @param gld The drawable context to use
     */
    public void postRender(GL gl, GLU glu)
    {
    }

    /**
     * Specify this nodes parent. Should not be called directly by external
     * callers. Setting a value of null will clear the existing parent. Bit
     * broken right now as it doesn't handle multiple-parents like needed in
     * a proper scene graph.
     *
     * @param p The new parent instance to call or null
     */
    protected void setParent(Node p)
    {
        parent = p;
    }

    /**
     * Tell a node to update its bounding rep now.
     */
    public void updateBounds()
    {
    }

    /**
     * Tell a node to update its transform.
     *
     * @param parentTrans The new transform to use
     */
    public void updateTransform(Matrix4f parentTrans)
    {
        if (parentTrans != null)
        {
            // Default for all nodes is to use its parent transform
            transform = parentTrans;
        }
    }

    /**
     * Get the transformation matrix for this node.
     *
     * @return The current matrix in use
     */
    public Matrix4f getTransform()
    {
        return transform;
    }

    /**
     * Set the bounds to the given explicit value. When set, auto computation
     * of the bounds of this node is turned off. A value of null can be used
     * to clear the current explicit bounds and return to auto computation.
     *
     * @param b The new bounds to use or null to clear
     */
    public void setBounds(BoundingVolume b)
    {
        bounds = b;
        implicitBounds = (bounds == null);
    }

    /**
     * Get the currently set bounds for this object. If no explicit bounds have
     * been set, then an implicit set of bounds is returned based on the
     * current scene graph state.
     *
     * @return The current bounds of this object
     */
    public BoundingVolume getBounds()
    {
        // need to check and set a new instance here if needed.
        if(implicitBounds && bounds == null)
            recomputeBounds();

        return bounds;
    }

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    protected void recomputeBounds()
    {
        bounds = new BoundingSphere(0);
    }
}