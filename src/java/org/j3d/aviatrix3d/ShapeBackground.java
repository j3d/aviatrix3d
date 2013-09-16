/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
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

// Local imports
import javax.media.opengl.GL;

import org.j3d.util.I18nManager;

// External imports
// None

/**
 * Background node that renders a list of user-provided Shape3D instances.
 * <p>
 *
 * Backgrounds are rendered as the first item but do not interact with the
 * normal geometry in the rendering process. Typically, backgrounds are
 * rendered in a fixed volume (a unit box or sphere is the most common) with
 * depthbuffer reads and writes disabled. Ordinary geometry is then drawn over
 * the top.  Backgrounds must fit within clipping planes of [0.1,1].
 *
 * Rendering is performed in the order the nodes are added to the instance.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidIndexRangeMsg: Error message when an index is either negative or
 *     too big for the amount of data provided.</li>
 * <li>nullShapeMsg: </li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.18 $
 */
public class ShapeBackground extends Background
{
    /** Error message when an index is negative or too big */
    private static final String INDEX_RANGE_PROP =
        "org.j3d.aviatrix3d.ShapeBackground.invalidIndexRangeMsg";

    /** Error message when user provided a null shape reference */
    private static final String NULL_SHAPE_PROP =
        "org.j3d.aviatrix3d.ShapeBackground.nullShapeMsg";

    /** Collection of shapes that define this background */
    private Shape3D[] geometry;

    /** The last shape on the list */
    private int numGeometry;

    /**
     * Constructs a background node for a base colour of black.
     */
    public ShapeBackground()
    {
        this(null);
    }

    /**
     * Construct a background node for a user-provided colour. The colour
     * provided should have 3 or 4 elements. If 3 are provided, a fully opaque
     * background is assumed. If less than 3 elements are provided, an exception
     * is generated. If the array is null, this assumes the a default black
     * background.
     *
     * @param c The array of colours to use, or null
     * @throws IllegalArgumentException The colour array is not long enough
     */
    public ShapeBackground(float[] c)
    {
        super(c);

        numGeometry = 0;
        geometry = new Shape3D[5];
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

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

        for(int i=0; i < numGeometry; i++)
        {
            if(geometry[i] != null)
                geometry[i].setUpdateHandler(handler);
        }
    }

    /**
     * Notification that this object is live now.
     *
     * @param state true to set this to live
     */
    protected void setLive(boolean state)
    {
        // Ignore stuff that doesn't change the state
        if(state == alive)
            return;

        for(int i = 0; i < numGeometry; i++)
        {
            if(geometry[i] != null)
                geometry[i].setLive(state);
        }

        // Call this after, that way the bounds are recalculated here with
        // the correct bounds of all the children set up.
        super.setLive(state);
    }

    //----------------------------------------------------------
    // Methods defined by BackgroundRenderable
    //----------------------------------------------------------

    /**
     * Check to see whether this shape is something that represents 2D or 3D
     * renderable background. Pure 2D backgrounds do not need transformation
     * stacks or frustums set up to render - they can blit straight to the
     * screen as needed.
     *
     * @return True if this is 2D background, false if this is 3D
     */
    public boolean is2D()
    {
        return false;
    }

    //----------------------------------------------------------
    // Methods defined by ObjectRenderable
    //----------------------------------------------------------

    /**
     * Issue ogl commands needed for this component
     *
     * @param gl The gl context to draw with
     */
    public void render(GL gl)
    {
        if(useClearColor)
        {
            gl.glClearColor(color[0], color[1], color[2], color[3]);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        }

        for(int i = 0; i < numGeometry; i++)
        {
            if(!geometry[i].isVisible())
                continue;

            geometry[i].render(gl);
            geometry[i].postRender(gl);
        }
    }

    /**
     * Restore all openGL state to the given drawable.
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL gl)
    {
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The objec to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
        throws ClassCastException
    {
        ShapeBackground app = (ShapeBackground)o;
        return compareTo(app);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof ShapeBackground))
            return false;
        else
            return equals((ShapeBackground)o);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Add a shape to be rendered to the end of the listing.
     *
     * @param shape The object instance to be rendered
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void addShape(Shape3D shape)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(shape == null)
            return;

        if(numGeometry == geometry.length)
        {
            Shape3D[] tmp = new Shape3D[numGeometry + 5];
            System.arraycopy(geometry, 0, tmp, 0, numGeometry);
            geometry = tmp;
        }

        geometry[numGeometry++] = shape;

        if(shape.isLive() != alive)
            shape.setLive(alive);

        shape.setParent(this);
        shape.setUpdateHandler(updateHandler);
    }

    /**
     * Remove the shape at the given index position. If the index is
     * out of range, then this will generate an exception.
     *
     * @param idx The index of the shape to be fetched
     * @return The shape at the given index
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public Shape3D removeShape(int idx)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(idx < 0 || idx >= numGeometry)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INDEX_RANGE_PROP) + idx;
            throw new IllegalArgumentException(msg);
        }

        Shape3D shape = geometry[idx];

        if(geometry[idx] != null)
        {
            geometry[idx].setLive(false);
            geometry[idx].removeParent(this);
        }

        int lastIndex = numGeometry - 1;
        if(idx != lastIndex)
        {
           System.arraycopy(geometry,
                            idx + 1,
                            geometry,
                            idx,
                            lastIndex - idx);
        }

        geometry[lastIndex] = null;
        numGeometry--;

        return shape;
    }

    /**
     * Change the shape at the given index. A shape can only be replaced by
     * another shape. If you wish to delete it, then use the remove method,
     * not this one with a null value. A null value is illegal here.
     *
     * @param idx The index of the shape to be fetched
     * @param shape The object instance to be rendered
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setShape(Shape3D shape, int idx)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(shape == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NULL_SHAPE_PROP);
            throw new IllegalArgumentException(msg);
        }

        if(idx < 0 || idx >= numGeometry)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INDEX_RANGE_PROP) + idx;
            throw new IllegalArgumentException(msg);
        }

        if(geometry[idx] != null)
        {
            geometry[idx].setLive(false);
            geometry[idx].removeParent(this);
        }

        geometry[idx] = shape;

        if(shape.isLive() != alive)
            shape.setLive(alive);

        shape.setParent(this);
        shape.setUpdateHandler(updateHandler);
    }

    /**
     * Get the current the shape and the given index position. If the index is
     * out of range, then this will generate an exception.
     *
     * @param idx The index of the shape to be fetched
     * @return The shape at the given index
     */
    public Shape3D getShape(int idx)
    {
        if(idx < 0 || idx >= numGeometry)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INDEX_RANGE_PROP) + idx;
            throw new IllegalArgumentException(msg);
        }

        return geometry[idx];
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param bg The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(ShapeBackground bg)
    {
        if(bg == null)
            return 1;

        if(bg == this)
            return 0;

        int res = compareColor4(color, bg.color);
        if(res != 0)
            return res;

        for(int i = 0; i < 6; i++)
        {
            if(geometry[i] != bg.geometry[i])
            {
                if(geometry[i] == null)
                    return -1;
                else if(bg.geometry[i] == null)
                    return 1;

                if(geometry[i] != bg.geometry[i])
                    return geometry[i].hashCode() < bg.geometry[i].hashCode() ? -1 : 1;
            }
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param bg The background instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(ShapeBackground bg)
    {
        if(bg == this)
            return true;

        if(bg == null)
            return false;

        if(!equalsColor4(color, bg.color))
            return false;

        for(int i = 0; i < 6; i++)
        {
            if(geometry[i] != bg.geometry[i])
                return false;
        }

        return true;
    }
}
