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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.geom.hanim.HAnimHumanoid;
import org.j3d.geom.hanim.HAnimObject;

/**
 * Common AV3D implementation of the Humanoid object that may be extended for
 * either shader or software implementation additions.
 * <p>
 *
 * Implements a rootTransform group to hold the geometry and rootTransformation
 * required by the Site object. The output objects are always FloatBuffers for
 * direct passing to OpenGL.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public abstract class AVHumanoid extends HAnimHumanoid
    implements NodeUpdateListener
{
    /** Speed joint added to a space joint */
    private static final String WRONG_NODE_MULTI_PROP =
        "org.j3d.renderer.aviatrix3d.geom.hanim.AVHumanoid.childTypeMultiMsg";

    /** Speed joint added to a space joint */
    private static final String WRONG_PART_MULTI_PROP =
        "org.j3d.renderer.aviatrix3d.geom.hanim.AVHumanoid.partTypeMultiMsg";


    /** The real transform group this occupies */
    private TransformGroup rootTransform;

    /** Group for containing the skeleton */
    private Group skeletonGroup;

    /** Group for containing the extra viewpoints */
    private Group viewpointGroup;

    /** Flag to indicate the bounds changed */
    private boolean boundsChanged;

    /** If there are explicit bounds, this is a valid object */
    private BoundingBox bounds;

    /**
     * Create a new, default instance of the site.
     */
    AVHumanoid()
    {
        skeletonGroup = new Group();
        viewpointGroup = new Group();

        rootTransform = new TransformGroup();
        rootTransform.addChild(skeletonGroup);
        rootTransform.addChild(viewpointGroup);

        boundsChanged = false;
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
    @Override
    public void updateNodeBoundsChanges(Object src)
    {
        if(src == rootTransform)
        {
            rootTransform.setTransform(localMatrix);

            if(boundsChanged)
            {
                boundsChanged = false;
                rootTransform.setBounds(bounds);
            }
        }
        else if(src == skeletonGroup)
        {
            skeletonGroup.removeAllChildren();

            for(int i = 0; i < numSkeleton; i++)
            {
                Node part = ((AVHumanoidPart)skeleton[i]).getSceneGraphObject();
                skeletonGroup.addChild(part);
            }
        }
        else if(src == viewpointGroup)
        {
            viewpointGroup.removeAllChildren();

            for(int i = 0; i < numViewpoints; i++)
                viewpointGroup.addChild((Node)viewpoints[i]);
        }
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeDataChanges(Object src)
    {
    }


    //----------------------------------------------------------
    // Methods defined by HAnimHumanoid
    //----------------------------------------------------------

    /**
     * Set a new value for the bboxCenter of this segment. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the bboxCenter is taken from the 1st three values.
     *
     * @param val The new bboxCenter value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    @Override
    public void setBboxCenter(float[] val)
    {
        super.setBboxCenter(val);

        if(bboxSize[0] == -1 && bboxSize[1] == -1 && bboxSize[2] == 0)
            return;

        if(bounds == null)
            bounds = new BoundingBox();

        float x = bboxCenter[0] - bboxSize[0];
        float y = bboxCenter[1] - bboxSize[1];
        float z = bboxCenter[2] - bboxSize[2];

        bounds.setMinimum(x, y, z);

        x = bboxCenter[0] + bboxSize[0];
        y = bboxCenter[1] + bboxSize[1];
        z = bboxCenter[2] + bboxSize[2];

        bounds.setMaximum(x, y, z);

        if(rootTransform.isLive())
        {
            boundsChanged = true;
            rootTransform.boundsChanged(this);
        }
        else
            rootTransform.setBounds(bounds);
    }

    /**
     * Set a new value for the bboxSize of this segment. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the bboxSize is taken from the 1st three values.
     *
     * @param val The new bboxSize value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
    @Override
    public void setBboxSize(float[] val)
    {
        super.setBboxSize(val);

        if(val[0] == -1 && val[1] == -1 && val[2] == 0)
            bounds = null;
        else if(bounds == null)
        {
            bounds = new BoundingBox();

            float x = bboxCenter[0] - bboxSize[0];
            float y = bboxCenter[1] - bboxSize[1];
            float z = bboxCenter[2] - bboxSize[2];

            bounds.setMinimum(x, y, z);

            x = bboxCenter[0] + bboxSize[0];
            y = bboxCenter[1] + bboxSize[1];
            z = bboxCenter[2] + bboxSize[2];

            bounds.setMaximum(x, y, z);
        }

        if(rootTransform.isLive())
        {
            boundsChanged = true;
            rootTransform.boundsChanged(this);
        }
        else
            rootTransform.setBounds(bounds);
    }

    /**
     * Replace the existing skeleton with the new set of sites and joints.
     * Viewpoints must be an instance of an Aviatrix3D Node.
     *
     * @param vps The collection of viewpoints objects to now use
     * @param numValid The number kids to copy from the given array
     */
    @Override
    public void setViewpoints(Object[] vps, int numValid)
    {
        if(viewpointGroup.isLive())
        {
            for(int i = 0; i < numValid; i++)
            {
                if(!(vps[i] instanceof Node))
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(WRONG_NODE_MULTI_PROP);

                    Locale lcl = intl_mgr.getFoundLocale();
                    NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                    String cls_name = vps[i] == null ? "null" : vps[i].getClass().getName();
                    Object[] msg_args = { new Integer(i), cls_name };
                    Format[] fmts = { null, n_fmt };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    msg_fmt.setFormats(fmts);
                    String msg = msg_fmt.format(msg_args);

                    throw new IllegalArgumentException(msg);
                }
            }

            viewpointGroup.boundsChanged(this);
        }
        else
        {
            viewpointGroup.removeAllChildren();

            for(int i = 0; i < numValid; i++)
            {
                if(!(vps[i] instanceof Node))
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(WRONG_NODE_MULTI_PROP);

                    Locale lcl = intl_mgr.getFoundLocale();
                    NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                    String cls_name = vps[i] == null ? "null" : vps[i].getClass().getName();
                    Object[] msg_args = { new Integer(i), cls_name };
                    Format[] fmts = { null, n_fmt };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    msg_fmt.setFormats(fmts);
                    String msg = msg_fmt.format(msg_args);

                    throw new IllegalArgumentException(msg);
                }

                viewpointGroup.addChild((Node)vps[i]);
            }
        }

        super.setViewpoints(vps, numValid);
    }

    /**
     * Replace the existing viewpoints with the new set of viewpoints.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     */
    @Override
    public void setSkeleton(HAnimObject[] kids, int numValid)
    {
        if(skeletonGroup.isLive())
        {
            for(int i = 0; i < numValid; i++)
            {
                if(!(kids[i] instanceof AVHumanoidPart))
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(WRONG_PART_MULTI_PROP);

                    Locale lcl = intl_mgr.getFoundLocale();
                    NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                    String cls_name = kids[i] == null ? "null" : kids[i].getClass().getName();
                    Object[] msg_args = { new Integer(i), cls_name };
                    Format[] fmts = { null, n_fmt };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    msg_fmt.setFormats(fmts);
                    String msg = msg_fmt.format(msg_args);

                    throw new IllegalArgumentException(msg);
                }
            }

            skeletonGroup.boundsChanged(this);
        }
        else
        {
            skeletonGroup.removeAllChildren();

            for(int i = 0; i < numValid; i++)
            {
                if(!(kids[i] instanceof AVHumanoidPart))
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(WRONG_PART_MULTI_PROP);

                    Locale lcl = intl_mgr.getFoundLocale();
                    NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                    String cls_name = kids[i] == null ? "null" : kids[i].getClass().getName();
                    Object[] msg_args = { new Integer(i), cls_name };
                    Format[] fmts = { null, n_fmt };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    msg_fmt.setFormats(fmts);
                    String msg = msg_fmt.format(msg_args);

                    throw new IllegalArgumentException(msg);
                }

                Node part = ((AVHumanoidPart)kids[i]).getSceneGraphObject();
                skeletonGroup.addChild(part);
            }
        }

        super.setSkeleton(kids, numValid);
    }

    /**
     * All the skeletal changes are in for this frame, so update the matrix
     * values now. If nothing has changed, don't bother doing any calculations
     * and return immediately.
     */
    @Override
    public void updateSkeleton()
    {
        if(matrixChanged) {
            if (rootTransform.isLive())
                rootTransform.boundsChanged(this);
            else
                updateNodeBoundsChanges(rootTransform);
        }

        super.updateSkeleton();
    }


    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the implemented scene graph object for this humanoid so that it
     * can be added directly to the scene.
     *
     * @return The scene graph object to use
     */
    public Node getSceneGraphObject()
    {
        return rootTransform;
    }

    /**
     * Set the list of geometry that should be used by this humanoid. Depending
     * on the underlying implementation of the skinned mesh algorithm, these may
     * or may not be directly inserted into the scene graph. When skinned mesh
     * animation takes place, the coordinates are automatically fed from this
     * humanoid to the appropriate geometry. For best performance, the geometry
     * should make use of the
     * {@link org.j3d.renderer.aviatrix3d.nodes.BufferGeometry} geometry
     * representation.
     *
     * @param skins List of representative shapes to use
     */
    public abstract void setSkin(Node[] skins, int numSkins);

    /**
     * Internal convenience method to add extra geometry as needed to the scene
     * graph. Only accessible to the derived class from this package. Should
     * only be called right at the start, during the constructor when the scene
     * graph is not yet live.
     *
     * @param node The node instance to add
     */
    void addNode(Node node)
    {
        rootTransform.addChild(node);
    }

    /**
     * Convenience method to allocate a NIO buffer for the vertex handling that
     * handles floats.
     *
     * @param size The number of floats to have in the array
     */
    protected FloatBuffer createBuffer(int size)
    {
        // Need to allocate a byte buffer 4 times the size requested because the
        // size is treated as bytes, not number of floats.
        ByteBuffer buf = ByteBuffer.allocateDirect(size * 4);
        buf.order(ByteOrder.nativeOrder());
        FloatBuffer ret_val = buf.asFloatBuffer();

        return ret_val;
    }
}
