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

package org.j3d.renderer.aviatrix3d.pipeline;

// External imports
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

/**
 * Debugging Renderable object representing the view frustum.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class DebugFrustumRenderable
    implements ShapeRenderable,
               GeometryRenderable
{
    /** Dimensions of the raw frustum */
    private float[] frustumPoints;

    /** The field of view */
    private float fieldOfView;

    /** The aspect ratio */
    private float aspectRatio;

    DebugFrustumRenderable(float fov, float aspect, double[] points)
    {
        fieldOfView = fov;
        aspectRatio = aspect;

        frustumPoints = new float[6];
        frustumPoints[0] = (float)points[0];
        frustumPoints[1] = (float)points[1];
        frustumPoints[2] = (float)points[2];
        frustumPoints[3] = (float)points[3];
        frustumPoints[4] = (float)points[4];
        frustumPoints[5] = (float)points[5];
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
    @Override
    public boolean isVisible()
    {
        return true;
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
    @Override
    public boolean is2D()
    {
        return false;
    }

    /**
     * Get the centre of the shape object. Used for transparency depth sorting
     * based on the center of the bounds of the object.
     *
     * @param center The object to copy the center coordinates in to
     */
    @Override
    public void getCenter(float[] center)
    {
        // Not sure what to do about this one.
        center[0] = 0;
        center[1] = 0;
        center[2] = 0;
    }

    /**
     * Fetch the renderable that represents the geometry of this shape.
     *
     * @return The current geometry renderable or null if none
     */
    @Override
    public GeometryRenderable getGeometryRenderable()
    {
        return this;
    }

    /**
     * Fetch the renderable that represents the visual appearance modifiers of
     * this shape.
     *
     * @return The current appearance renderable or null if none
     */
    @Override
    public AppearanceRenderable getAppearanceRenderable()
    {
        return null;
    }

    //---------------------------------------------------------------
    // Methods defined by GeometryRenderable
    //---------------------------------------------------------------

    /**
     * Check to see if this geometry has anything that could be interpreted as
     * an alpha value. For example a Raster with RGBA values or vertex geometry
     * with 4-component colours for the vertices.
     *
     * @return true if there is any form of transparency
     */
    @Override
    public boolean hasTransparency()
    {
        return false;
    }

    /**
     * Render the geometry now.
     *
     * @param gl The GL context to render with
     */
    @Override
    public void render(GL2 gl)
    {
        float left = frustumPoints[0];
        float right = frustumPoints[1];
        float bottom = frustumPoints[2];
        float top = frustumPoints[3];
        float nearval = frustumPoints[4];
        float farval = frustumPoints[5];

        gl.glBegin(GL.GL_LINES);
        gl.glColor3f(0, 1, 1);

        gl.glVertex3f(left, top, nearval);
        gl.glVertex3f(right, top, nearval);

        gl.glVertex3f(left, bottom, nearval);
        gl.glVertex3f(right, bottom, nearval);

        gl.glVertex3f(left, top, nearval);
        gl.glVertex3f(left, bottom, nearval);

        gl.glVertex3f(right, top, nearval);
        gl.glVertex3f(right, bottom, nearval);

        float back_top = (float)(Math.tan(fieldOfView * 0.5) * farval);
        float back_bottom = -back_top;

        float back_right = back_top * aspectRatio;
        float back_left = -back_right;

        gl.glVertex3f(back_left, back_top, farval);
        gl.glVertex3f(back_right, back_top, farval);

        gl.glVertex3f(back_left, back_bottom, farval);
        gl.glVertex3f(back_right, back_bottom, farval);

        gl.glVertex3f(back_left, back_top, farval);
        gl.glVertex3f(back_left, back_bottom, farval);

        gl.glVertex3f(back_right, back_top, farval);
        gl.glVertex3f(back_right, back_bottom, farval);

        // Now the front to back lines
        gl.glVertex3f(left, top, nearval);
        gl.glVertex3f(back_left, back_top, farval);

        gl.glVertex3f(right, top, nearval);
        gl.glVertex3f(back_right, back_top, farval);

        gl.glVertex3f(left, bottom, nearval);
        gl.glVertex3f(back_left, back_bottom, farval);

        gl.glVertex3f(right, bottom, nearval);
        gl.glVertex3f(back_right, back_bottom, farval);

        gl.glVertex3f(left, top, nearval);
        gl.glVertex3f(back_left, back_top, farval);

        gl.glVertex3f(left, bottom, nearval);
        gl.glVertex3f(back_left, back_bottom, farval);

        gl.glVertex3f(right, top, nearval);
        gl.glVertex3f(back_right, back_top, farval);

        gl.glVertex3f(right, bottom, nearval);
        gl.glVertex3f(back_right, back_bottom, farval);

        gl.glEnd();
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
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        // We should always barf here as they're not the same as anything else.
        DebugFrustumRenderable sh = (DebugFrustumRenderable)o;

        if(o == this)
            return 0;

        return -1;
    }
}
