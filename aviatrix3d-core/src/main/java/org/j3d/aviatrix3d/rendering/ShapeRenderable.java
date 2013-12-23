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

package org.j3d.aviatrix3d.rendering;

// External imports
// None

// Local imports
// None

/**
 * Renderable object that applies geometry that appears on screen.
 * <p>
 *
 * <b>State Management</b>
 * <p>
 *
 * Geometry can be either visible or invisible. Invisible may arrive from a
 * number of different reasons, such as:
 * <p>
 *
 * <ul>
 * <li>No geometry to render</li>
 * <li>Completely transparent due to colour settings</li>
 * <li>Required resources not provided (eg missing texture)</li>
 * <li>Some form of user-visible control</li>
 * </ul>
 *
 * This state may change on any frame. During rendering this state is queried and
 * any geometry not indicating it is visible will be discarded.
 *
 * @author Justin Couch
 * @version $Revision: 2.3 $
 */
public interface ShapeRenderable extends Renderable
{
    /**
     * State check to see whether the shape in it's current setup
     * is visible. Various reasons for this are stated in the class docs.
     *
     * @return true if the shape has something to render
     */
    public boolean isVisible();

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
    public boolean is2D();

    /**
     * Get the centre of the shape object. Used for transparency depth sorting
     * based on the center of the bounds of the object.
     *
     * @param center The object to copy the center coordinates in to
     */
    public void getCenter(float[] center);

    /**
     * Fetch the renderable that represents the geometry of this shape.
     *
     * @return The current geometry renderable or null if none
     */
    public GeometryRenderable getGeometryRenderable();

    /**
     * Fetch the renderable that represents the visual appearance modifiers of
     * this shape.
     *
     * @return The current appearance renderable or null if none
     */
    public AppearanceRenderable getAppearanceRenderable();
}
