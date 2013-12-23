/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom.volume;

// External imports
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Vector4d;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.rendering.Cullable;
import org.j3d.aviatrix3d.rendering.CustomCullable;
import org.j3d.aviatrix3d.rendering.CullInstructions;

/**
 * Node that implements a simple OctTree-style data structure useful for volume
 * rendering of large datasets.
 * <p>
 *
 * The core implementation is a fairly simplistic distance-based algorithm to
 * determine when the level sets should be changed over. The class uses a single
 * Node object for the low-detail model, and allows the provision of a set of
 * nodes for the next detail level, allowing for nesting of the tree objects if
 * required.
 * <p>
 *
 * For describing the high-detail geometry, there is no specific geometric
 * representation implied by this node. There are just up to 8 children that can
 * be supplied, and that the user should make sure to place them in the correct
 * octant location using transformations as required.
 *
 * <p>
 * For bounds calculation, the bounds of the low-res model are used.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>validDetailCountMsg: Error message when the number of high detail nodes
 *     is not in the range [0,8].</li>
 * <li>negRangeMsg: Error message when user provides a negative range value</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class OctTree extends BaseNode
    implements CustomCullable, TransformHierarchy
{
    /** Error message when the high detail geometry is out of range */
    private static final String HIGH_DETAIL_NUM_PROP =
        "org.j3d.renderer.aviatrix3d.geom.volume.OctTree.validDetailCountMsg";

    /** Error message when the user provides a negative range value */
    private static final String NEG_RANGE_PROP =
        "org.j3d.renderer.aviatrix3d.geom.volume.OctTree.negRangeMsg";

    /**
     * The range at which the geometry detail should switch over. This is actually
     * the range-squared value.
     */
    private float range;

    /** The low-detail model to use */
    private Node lowDetailGeom;

    /** The geometry for each of the high-res portions. */
    private Node[] highDetailGeom;

    /** The cullable low-detail model to use */
    private Cullable lowDetailOutput;

    /** The cullable for each of the high-res portions. */
    private Cullable[] highDetailOutput;

    /** The number of valid high-detail geometry children */
    private int numHighDetailGeom;

    /**
     * Create a new empty instance of the oct tree. The range is set to zero,
     * meaning it will always display the low res data, unless the user changes
     * the range.
     */
    public OctTree()
    {
        highDetailGeom = new Node[8];
        highDetailOutput = new Cullable[8];
    }

    //----------------------------------------------------------
    // Methods defined by CustomCullable
    //----------------------------------------------------------

    /**
     * Check this node for children to traverse. The angular resolution is
     * defined as Field Of View (in radians) / viewport width in pixels.
     *
     * @param output Fill in the child information here
     * @param vworldTx The transformation from the root of the scene to
     *    this node according to the current traversal path
     * @param viewTransform The transformation from the root of the scene
     *    graph to the active viewpoint
     * @param frustumPlanes Listing of frustum planes in the order: right,
     *    left, bottom, top, far, near
     * @param angularRes Angular resolution of the screen, or 0 if not
     *    calculable from the available data.
     */
    public void cullChildren(CullInstructions output,
                             Matrix4d vworldTx,
                             Matrix4d viewTransform,
                             Vector4d[] frustumPlanes,
                             float angularRes)
    {
        // if the distance from the viewTransform to vworldTx is less than
        // range, then return the high-detail, otherwise return the low detail.

        double x = vworldTx.m03 - viewTransform.m03;
        double y = vworldTx.m13 - viewTransform.m13;
        double z = vworldTx.m23 - viewTransform.m23;

        output.hasTransform = false;

        if(x * x + y * y + z * z < range)
        {
            output.numChildren = numHighDetailGeom;

            if(output.children.length < 8)
                output.resizeChildren(8);

            System.arraycopy(highDetailOutput,
                             0,
                             output.children,
                             0,
                             numHighDetailGeom);
        }
        else if(lowDetailGeom != null)
        {
            output.numChildren = 1;
            output.children[0] = lowDetailOutput;
        }
        else
            output.numChildren = 0;
    }

    //----------------------------------------------------------
    // Methods defined by Node
    //----------------------------------------------------------

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    protected void recomputeBounds()
    {
        if(!implicitBounds)
            return;

        if(lowDetailGeom == null)
        {
            bounds = INVALID_BOUNDS;
            return;
        }
        else
            bounds = lowDetailGeom.getBounds();
    }

    /**
     * Request a recomputation of the bounds of this object. If this object is
     * not currently live, you can request a recompute of the bounds to get the
     * most current values. If this node is currently live, then the request is
     * ignored.
     * <p>
     * This will recurse down the children asking all of them to recompute the
     * bounds. If a child is found to be during this process, that branch will
     * not update, and thus the value used will be the last updated (ie from the
     * previous frame it was processed).
     */
    public void requestBoundsUpdate()
    {
        if(alive || (lowDetailGeom == null) || !implicitBounds)
            return;

        lowDetailGeom.requestBoundsUpdate();

        for(int i = 0; i < 8; i++)
            highDetailGeom[i].requestBoundsUpdate();

        // Clear this as we are no longer dirty.
        recomputeBounds();
    }

    //----------------------------------------------------------
    // Methods defined by SceneGraphObject
    //----------------------------------------------------------

    /**
     * Check to see if this node is the same reference as the passed node that
     * is a parent of this node. This is the downwards check to ensure that
     * there is no cyclic scene graph structures at the point where someone
     * adds a node to the scenegraph. When the reference and this are the
     * same, an exception is generated. Since each class may have different
     * lists of child node setups, this should be overriden by any class that
     * can take children, and have the call passed along to the children.
     *
     * @param parent The reference to check against this class
     * @throws CyclicSceneGraphStructureException Equal parent and child
     */
    protected void checkForCyclicChild(SceneGraphObject parent)
        throws CyclicSceneGraphStructureException
    {
        if(parent == this)
            throw new CyclicSceneGraphStructureException();

        if(lowDetailGeom != null)
            checkForCyclicChild(lowDetailGeom, parent);

        for(int i = 0; i < 8; i++)
        {
            if(highDetailGeom[i] != null)
                checkForCyclicChild(highDetailGeom[i], parent);
        }
    }

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        if(lowDetailGeom != null)
            setUpdateHandler(lowDetailGeom);

        for(int i = 0; i < 8; i++)
        {
            if(highDetailGeom[i] != null)
                setUpdateHandler(highDetailGeom[i]);
        }
    }

    /**
     * Notification that this object is live now.
     */
    protected void setLive(boolean state)
    {
        // Ignore stuff that doesn't change the state
        if(state == alive)
            return;

        if(lowDetailGeom != null)
            setLive(lowDetailGeom, state);

        for(int i = 0; i < 8; i++)
        {
            if(highDetailGeom[i] != null)
                setLive(highDetailGeom[i], state);
        }

        // Call this after, that way the bounds are recalculated here with
        // the correct bounds of all the children set up.
        super.setLive(state);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the range at which this geometry should change from low-detail to
     * high-detail models. The range should be a non-negative value. A range
     * value of zero will imply that the low detail geometry will always be
     * shown. Range is treated as a spherical distance from the center of
     * the local coordinate system of this geometry.
     *
     * @param distance The range at which the geometry should swap
     * @throws IllegalArgumentException The range value was negative
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void setRange(float distance)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(distance < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NEG_RANGE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(distance) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        range = distance * distance;
    }

    /**
     * Get the currently set range at which the high detail model will be
     * shown.
     *
     * @return A non-negative range value
     */
    public float getRange()
    {
        return (float)Math.sqrt(range);
    }

    /**
     * Set the low-detail geometry instance to use. Setting a null value will
     * clear the currently set geometry. The low detail can be any form of
     * scene graph desired.
     *
     * @param geom The sub scene graph to use for the low-detail geometry
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed callback method
     */
    public void setLowDetail(Node geom)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(lowDetailGeom != null)
        {
            setParent(lowDetailGeom, null);
            setLive(lowDetailGeom, false);
            clearUpdateHandler(lowDetailGeom);
        }

        lowDetailGeom = geom;

        if(geom instanceof Cullable)
            lowDetailOutput = (Cullable)geom;
        else
            lowDetailOutput = null;

        if(lowDetailGeom != null)
        {
            setParent(lowDetailGeom, this);
            setLive(lowDetailGeom, alive);
            setUpdateHandler(lowDetailGeom);
        }

        markBoundsDirty();
    }

    /**
     * Get the currently set low-detail geometry structure. If none is set, null
     * is returned.
     *
     * @return The current geometry structure or null
     */
    public Node getLowDetail()
    {
        return lowDetailGeom;
    }

    /**
     * Set the low-detail geometry instance to use. Setting a null value will
     * clear the currently set geometry. The low detail can be any form of
     * scene graph desired.
     *
     * @param geom The sub scene graph list to use for the high-detail geometry
     * @param numValid The number of valid bits of geometry to use from the array
     * @throws IllegalArgumentException numValid was outside [0, 8]
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds changed  callback method
     */
    public void setHighDetail(Node[] geom, int numValid)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(numValid < 0 || numValid > 8)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(HIGH_DETAIL_NUM_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(numValid) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        // Clear the old one
        for(int i = 0; i < numHighDetailGeom; i++)
        {
            setParent(highDetailGeom[i], null);
            setLive(highDetailGeom[i], false);
            clearUpdateHandler(highDetailGeom[i]);
        }

        for(int i = 0; i < numValid; i++)
        {
            setParent(geom[i], this);
            setLive(geom[i], alive);
            setUpdateHandler(geom[i]);
        }

        System.arraycopy(geom, 0, highDetailGeom, 0, numValid);
        numHighDetailGeom = numValid;

        for(int i = 0; i < numValid; i++)
        {
            if(geom[i] instanceof Cullable)
                highDetailOutput[i] = (Cullable)geom[i];
            else
                highDetailOutput[i] = null;
        }
    }

    /**
     * Get the number of valid high-detail children in use by this node.
     *
     * @return A number between 0 and 8
     */
    public int numHighDetailGeom()
    {
        return numHighDetailGeom;
    }

    /**
     * Get the currently set low-detail geometry structure. If none is set, null
     * is returned.
     *
     * @param geom An array to copy the values into
     */
    public void getHighDetail(Node[] geom)
    {
        System.arraycopy(highDetailGeom, 0, geom, 0, numHighDetailGeom);
    }
}
