/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.pipeline.graphics;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.rendering.ShapeRenderable;
import org.j3d.aviatrix3d.rendering.AppearanceRenderable;
import org.j3d.aviatrix3d.rendering.GeometryRenderable;

/**
 * Internal proxy renderable object is used to manage the case when we have
 * an override renderable appear on the stack and make sure it works its
 * way through the pipeline.
 *
 *
 * @author Justin Couch
 * @version $Revision: 3.3 $
 */
class OverrideShapeProxyRenderable implements ShapeRenderable
{
    /** The real shape renderable that is being proxied */
    private ShapeRenderable shape;

    /** The overriding renderable */
    private AppearanceRenderable override;

    /**
     * Construct an instance of this proxy that is used by the
     * system to hide an override.
     */
    OverrideShapeProxyRenderable(ShapeRenderable s, AppearanceRenderable or)
    {
        shape = s;
        override = or;
    }

    //---------------------------------------------------------------
    // Methods defined by ShapeRenderable
    //---------------------------------------------------------------

    /**
     * State check to see whether the shape in it's current setup
     * is visible. Various reasons for this are stated in the class docs.
     *
     * @return true if the shape has something to render
     */
    public boolean isVisible()
    {
        // Check from the override first, then the real shape.
        if(!override.isVisible())
            return false;

        return shape.isVisible();
    }

    /**
     * Check to see whether this shape is something that represents 2D or 3D
     * geometry. Pure 2D geometry is not effected by any
     * {@link EffectRenderable}, while 3D is. Note that this can be changed
     * depending on the type of geometry itself. A Shape3D node with an
     * IndexedLineArray that only has 2D coordinates is as much a 2D geometry
     * as a raster object.
     *
     * @return True if this is 2D geometry, false if this is 3D
     */
    public boolean is2D()
    {
        return shape.is2D();
    }

    /**
     * Get the centre of the shape object. Used for transparency depth sorting
     * based on the center of the bounds of the object.
     *
     * @param center The object to copy the center coordinates in to
     */
    public void getCenter(float[] center)
    {
        shape.getCenter(center);
    }

    /**
     * Fetch the renderable that represents the geometry of this shape.
     *
     * @return The current geometry renderable or null if none
     */
    public GeometryRenderable getGeometryRenderable()
    {
        return shape.getGeometryRenderable();
    }

    /**
     * Fetch the renderable that represents the visual appearance modifiers of
     * this shape.
     *
     * @return The current appearance renderable or null if none
     */
    public AppearanceRenderable getAppearanceRenderable()
    {
        return override;
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
        OverrideShapeProxyRenderable app = (OverrideShapeProxyRenderable)o;
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
        if(!(o instanceof OverrideShapeProxyRenderable))
            return false;
        else
            return equals((OverrideShapeProxyRenderable)o);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param sh The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(OverrideShapeProxyRenderable sh)
    {
        if(sh == null)
            return 1;

        if(sh == this)
            return 0;

        if(override != sh.override)
        {
            if(override == null)
                return -1;
            else if(sh.override == null)
                return 1;

            int res = override.compareTo(sh.override);
            if(res != 0)
                return res;
        }

        if(shape != sh.shape)
        {
            if(shape == null)
                return -1;
            else if(sh.shape == null)
                return 1;

            int res = shape.compareTo(sh.shape);
            if(res != 0)
                return res;
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param sh The shape instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(OverrideShapeProxyRenderable sh)
    {
        if(sh == this)
            return true;

        if(sh == null)
            return false;

        if((override != sh.override) &&
           ((override == null) || !override.equals(sh.override)))
            return false;

        if((shape != sh.shape) && ((shape == null) || !shape.equals(sh.shape)))
            return false;

        return true;
    }
}
