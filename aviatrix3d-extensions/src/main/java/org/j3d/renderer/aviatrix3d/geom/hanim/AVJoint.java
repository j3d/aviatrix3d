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
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.geom.hanim.HAnimJoint;
import org.j3d.geom.hanim.HAnimObject;

/**
 * Common AV3D implementation of the Joint object that may be extended for
 * either shader or software implementation additions.
 * <p>
 *
 * Implements a transform group to hold the geometry and transformation
 * required by the Site object.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>childTypeSingleMsg: Error message when a non-AVHumanoidPart child given</li>
 * <li>childTypeMultiMsg: Error message when a non-AVHumanoidPart child given</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
class AVJoint extends HAnimJoint
    implements AVHumanoidPart, NodeUpdateListener
{
    /** Speed joint added to a space joint */
    private static final String WRONG_TYPE_SINGLE_PROP =
        "org.j3d.renderer.aviatrix3d.geom.hanim.AVJoint.childTypeSingleMsg";

    /** Speed joint added to a space joint */
    private static final String WRONG_TYPE_MULTI_PROP =
        "org.j3d.renderer.aviatrix3d.geom.hanim.AVJoint.childTypeMultiMsg";

    /** The real transform group this occupies */
    private TransformGroup transform;

    /** Remove the following list of children */
    private ArrayList<Node> removedChildren;

    /** Add the following list of children */
    private ArrayList<Node> addedChildren;

    /** Flag to indicate the bounds changed */
    private boolean boundsChanged;

    /** If there are explicit bounds, this is a valid object */
    private BoundingBox bounds;

    /**
     * Create a new, default instance of the site.
     */
    AVJoint()
    {
        transform = new TransformGroup();
        boundsChanged = false;
    }

    //----------------------------------------------------------
    // Methods defined by AVHumanoidPart
    //----------------------------------------------------------

    /**
     * Get the implemented scene graph object for this part.
     *
     * @return The scene graph object to use
     */
    public Node getSceneGraphObject()
    {
        return transform;
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
        transform.setTransform(localMatrix);

        // How many objects changed this last round?
        if(removedChildren != null)
        {
            for(int i = 0; i < removedChildren.size(); i++)
                transform.removeChild(removedChildren.get(i));

            removedChildren.clear();
            removedChildren = null;
        }

        if(addedChildren != null)
        {
            for(int i = 0; i < addedChildren.size(); i++)
                transform.addChild(addedChildren.get(i));

            addedChildren.clear();
            addedChildren = null;
        }

        if(boundsChanged)
        {
            boundsChanged = false;
            transform.setBounds(bounds);
        }
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src)
    {
    }


    //----------------------------------------------------------
    // Methods defined by HAnimJoint
    //----------------------------------------------------------

    /**
     * Set a new value for the bboxCenter of this segment. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the bboxCenter is taken from the 1st three values.
     *
     * @param val The new bboxCenter value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
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

        if(transform.isLive())
        {
            boundsChanged = true;
            transform.boundsChanged(this);
        }
        else
            transform.setBounds(bounds);
    }

    /**
     * Set a new value for the bboxSize of this segment. If the array is null or
     * not long enough an exception is generated. The array must be at least
     * 3 units long, and the bboxSize is taken from the 1st three values.
     *
     * @param val The new bboxSize value to use
     * @throws IllegalArgumentException The array is null or not long enough.
     */
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

        if(transform.isLive())
        {
            boundsChanged = true;
            transform.boundsChanged(this);
        }
        else
            transform.setBounds(bounds);
    }

    /**
     * Replace the existing children with the new set of children.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     */
    public void setChildren(HAnimObject[] kids, int numValid)
    {
        if(transform.isLive())
        {
            if(addedChildren == null)
                addedChildren = new ArrayList<Node>();

            for(int i = 0; i < numValid; i++)
            {
                if(!(kids[i] instanceof AVHumanoidPart))
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(WRONG_TYPE_MULTI_PROP);

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
                addedChildren.add(part);
            }

            if(removedChildren == null)
                removedChildren = new ArrayList<Node>();

            for(int i = 0; i < numChildren; i++)
            {
                Node part = ((AVHumanoidPart)children[i]).getSceneGraphObject();
                removedChildren.add(part);
            }

            transform.boundsChanged(this);
        }
        else
        {
            transform.removeAllChildren();

            for(int i = 0; i < numValid; i++)
            {
                if(!(kids[i] instanceof AVHumanoidPart))
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    String msg_pattern = intl_mgr.getString(WRONG_TYPE_MULTI_PROP);

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
                transform.addChild(part);
            }
        }

        super.setChildren(kids, numValid);
    }

    /**
     * Add a child node to the existing collection. Duplicates and null values
     * are allowed.
     *
     * @param kid The new child instance to add
     */
    public void addChild(HAnimObject kid)
    {
        if(!(kid instanceof AVHumanoidPart))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            Locale lcl = intl_mgr.getFoundLocale();
            String msg_pattern = intl_mgr.getString(WRONG_TYPE_SINGLE_PROP);

            String cls_name = kid == null ? "null" : kid.getClass().getName();
            Object[] msg_args = { cls_name };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        super.addChild(kid);

        if(transform.isLive())
        {
            if(addedChildren == null)
                addedChildren = new ArrayList<Node>();

            Node part = ((AVHumanoidPart)kid).getSceneGraphObject();
            addedChildren.add(part);

            transform.boundsChanged(this);
        }
        else
        {
            Node part = ((AVHumanoidPart)kid).getSceneGraphObject();
            transform.addChild(part);
        }
    }

    /**
     * Remove a child node from the existing collection. If there are
     * duplicates, only the first instance is removed. Only reference
     * comparisons are used.
     *
     * @param kid The child instance to remove
     */
    public void removeChild(HAnimObject kid)
    {
        // run through the children list to see if we have it
        for(int i = 0; i < numChildren; i++)
        {
            if(children[i] == kid)
            {
                if(transform.isLive())
                {
                    if(removedChildren == null)
                        removedChildren = new ArrayList<Node>();

                    Node part = ((AVHumanoidPart)kid).getSceneGraphObject();
                    removedChildren.add(part);

                    transform.boundsChanged(this);
                }
                else
                {
                    Node part = ((AVHumanoidPart)kid).getSceneGraphObject();
                    transform.removeChild(part);
                }

                break;
            }
        }

        super.removeChild(kid);
    }

    /**
     * All the skeletal changes are in for this frame, so update the matrix
     * values now. This should not be callable by the general public. Derived
     * classes may override this method, but should call it as well to ensure
     * the internal matrices are correctly updated.
     *
     * @param parentTransform The transformation into global coordinates of
     *   the parent of this joint
     * @param parentChanged Flag to indicate that the parent transformation
     *   matrix has changed or is still the same as last call
     */
    protected void updateSkeleton(Matrix4d parentTransform,
                                  boolean parentChanged)
    {
        if(matrixChanged) {
            if (transform.isLive())
                transform.boundsChanged(this);
            else
                updateNodeBoundsChanges(transform);
        }

        super.updateSkeleton(parentTransform, parentChanged);
    }
}
