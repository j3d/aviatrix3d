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

import javax.vecmath.Matrix4f;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.geom.hanim.HAnimSite;

/**
 * Common AV3D implementation of the Site object.
 * <p>
 *
 * Implements a transform group to hold the geometry and transformation
 * required by the Site object.
 * <p>
 *
 * The child of a Site node is required to be an instance of
 * {@link org.j3d.aviatrix3d.Node}.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>childTypeSingleMsg: Error message when a non-node child given</li>
 * <li>childTypeMultiMsg: Error message when a non-node child given</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class AVSite extends HAnimSite
    implements AVHumanoidPart, NodeUpdateListener
{
    /** Speed joint added to a space joint */
    private static final String WRONG_TYPE_SINGLE_PROP =
        "org.j3d.renderer.aviatrix3d.geom.hanim.AVSite.childTypeSingleMsg";

    /** Speed joint added to a space joint */
    private static final String WRONG_TYPE_MULTI_PROP =
        "org.j3d.renderer.aviatrix3d.geom.hanim.AVSite.childTypeMultiMsg";

    /** The real transform group this occupies */
    private TransformGroup transform;

    /** Remove the following list of children */
    private ArrayList<Node> removedChildren;

    /** Add the following list of children */
    private ArrayList<Node> addedChildren;

    /**
     * Create a new, default instance of the site.
     */
    AVSite()
    {
        transform = new TransformGroup();
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
    // Methods defined by HAnimSite
    //----------------------------------------------------------

    /**
     * Replace the existing children with the new set of children.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     */
    public void setChildren(Object[] kids, int numValid)
    {
        if(transform.isLive())
        {
            if(addedChildren == null)
                addedChildren = new ArrayList<Node>();

            for(int i = 0; i < numValid; i++)
            {
                if(!(kids[i] instanceof Node))
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

                addedChildren.add((Node)kids[i]);
            }

            if(removedChildren == null)
                removedChildren = new ArrayList<Node>();

            for(int i = 0; i < numChildren; i++)
                removedChildren.add((Node)children[i]);

            transform.boundsChanged(this);
        }
        else
        {
            transform.removeAllChildren();

            for(int i = 0; i < numValid; i++)
            {
                if(!(kids[i] instanceof Node))
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

                transform.addChild((Node)kids[i]);
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
    public void addChild(Object kid)
    {
        if(!(kid instanceof Node))
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

            addedChildren.add((Node)kid);

            transform.boundsChanged(this);
        }
        else
        {
            transform.addChild((Node)kid);
        }
    }

    /**
     * Remove a child node from the existing collection. If there are
     * duplicates, only the first instance is removed. Only reference
     * comparisons are used.
     *
     * @param kid The child instance to remove
     */
    public void removeChild(Object kid)
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

                    removedChildren.add((Node)kid);

                    transform.boundsChanged(this);
                }
                else
                {
                    transform.removeChild((Node)kid);
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
    protected void updateLocation(Matrix4f parentTransform,
                                  boolean parentChanged)
    {
        if(matrixChanged)
            transform.boundsChanged(this);

        super.updateLocation(parentTransform, parentChanged);
    }
}
