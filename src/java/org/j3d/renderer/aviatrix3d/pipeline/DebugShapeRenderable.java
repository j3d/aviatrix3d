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
import javax.vecmath.Matrix4f;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

/**
 * Debugging Renderable object representing a shape node during traversal
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class DebugShapeRenderable
    implements ShapeRenderable,
               GeometryRenderable
{
    /** Min position of the bounds */
    private float[] minBounds;

    /** Max position of the bounds */
    private float[] maxBounds;

    /** The center of the box */
    private float[] minBox;

    /** The center of the box */
    private float[] maxBox;

    /** Should we also show and calculate the parent box */
    private boolean showParent;

    DebugShapeRenderable(boolean useParent,
                         float[] min,
                         float[] max,
                         float[] center,
                         float[] size,
                         Matrix4f mat)
    {
        showParent = useParent;
        minBounds = new float[3];
        maxBounds = new float[3];

        minBounds[0] = min[0];
        minBounds[1] = min[1];
        minBounds[2] = min[2];
        maxBounds[0] = max[0];
        maxBounds[1] = max[1];
        maxBounds[2] = max[2];

        if(showParent)
        {
            minBox = new float[3];
            maxBox = new float[3];

            // min coordinate
            float x = center[0] - size[0];
            float y = center[1] - size[1];
            float z = center[2] - size[2];

            // Project the vertex into world space
            minBox[0] = mat.m00 * x + mat.m01 * y + mat.m02 * z + mat.m03;
            minBox[1] = mat.m10 * x + mat.m11 * y + mat.m12 * z + mat.m13;
            minBox[2] = mat.m20 * x + mat.m21 * y + mat.m22 * z + mat.m23;

            x = center[0] + size[0];
            y = center[1] + size[1];
            z = center[2] + size[2];

            // Project the vertex into world space
            maxBox[0] = mat.m00 * x + mat.m01 * y + mat.m02 * z + mat.m03;
            maxBox[1] = mat.m10 * x + mat.m11 * y + mat.m12 * z + mat.m13;
            maxBox[2] = mat.m20 * x + mat.m21 * y + mat.m22 * z + mat.m23;
        }
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
    public void getCenter(float[] center)
    {
        center[0] = (maxBounds[0] + minBounds[0]) * 0.5f;
        center[1] = (maxBounds[1] + minBounds[1]) * 0.5f;
        center[2] = (maxBounds[2] + minBounds[2]) * 0.5f;
    }

    /**
     * Fetch the renderable that represents the geometry of this shape.
     *
     * @return The current geometry renderable or null if none
     */
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
    public boolean hasTransparency()
    {
        return false;
    }

    /**
     * Render the geometry now.
     *
     * @param gl The GL context to render with
     */
    public void render(GL gl)
    {
        gl.glBegin(GL.GL_LINES);
        gl.glColor3f(0, 0, 1);

        gl.glVertex3f(minBounds[0], minBounds[1], minBounds[2]);
        gl.glVertex3f(minBounds[0], minBounds[1], maxBounds[2]);

        gl.glVertex3f(minBounds[0], minBounds[1], minBounds[2]);
        gl.glVertex3f(minBounds[0], maxBounds[1], minBounds[2]);

        gl.glVertex3f(minBounds[0], minBounds[1], minBounds[2]);
        gl.glVertex3f(maxBounds[0], minBounds[1], minBounds[2]);


        gl.glVertex3f(maxBounds[0], maxBounds[1], maxBounds[2]);
        gl.glVertex3f(maxBounds[0], maxBounds[1], minBounds[2]);

        gl.glVertex3f(maxBounds[0], maxBounds[1], maxBounds[2]);
        gl.glVertex3f(maxBounds[0], minBounds[1], maxBounds[2]);

        gl.glVertex3f(maxBounds[0], maxBounds[1], maxBounds[2]);
        gl.glVertex3f(minBounds[0], maxBounds[1], maxBounds[2]);


        gl.glVertex3f(minBounds[0], minBounds[1], maxBounds[2]);
        gl.glVertex3f(minBounds[0], maxBounds[1], maxBounds[2]);

        gl.glVertex3f(minBounds[0], minBounds[1], maxBounds[2]);
        gl.glVertex3f(maxBounds[0], minBounds[1], maxBounds[2]);

        gl.glVertex3f(minBounds[0], maxBounds[1], maxBounds[2]);
        gl.glVertex3f(minBounds[0], maxBounds[1], minBounds[2]);

        gl.glVertex3f(maxBounds[0], maxBounds[1], minBounds[2]);
        gl.glVertex3f(minBounds[0], maxBounds[1], minBounds[2]);

        gl.glVertex3f(maxBounds[0], maxBounds[1], minBounds[2]);
        gl.glVertex3f(maxBounds[0], minBounds[1], minBounds[2]);

        gl.glVertex3f(maxBounds[0], minBounds[1], minBounds[2]);
        gl.glVertex3f(maxBounds[0], minBounds[1], maxBounds[2]);
        gl.glEnd();

        if(showParent)
        {
            gl.glPopMatrix();

            gl.glBegin(GL.GL_LINES);
            gl.glColor3f(1, 0, 1);

            gl.glVertex3f(minBox[0], minBox[1], minBox[2]);
            gl.glVertex3f(minBox[0], minBox[1], maxBox[2]);

            gl.glVertex3f(minBox[0], minBox[1], minBox[2]);
            gl.glVertex3f(minBox[0], maxBox[1], minBox[2]);

            gl.glVertex3f(minBox[0], minBox[1], minBox[2]);
            gl.glVertex3f(maxBox[0], minBox[1], minBox[2]);


            gl.glVertex3f(maxBox[0], maxBox[1], maxBox[2]);
            gl.glVertex3f(maxBox[0], maxBox[1], minBox[2]);

            gl.glVertex3f(maxBox[0], maxBox[1], maxBox[2]);
            gl.glVertex3f(maxBox[0], minBox[1], maxBox[2]);

            gl.glVertex3f(maxBox[0], maxBox[1], maxBox[2]);
            gl.glVertex3f(minBox[0], maxBox[1], maxBox[2]);


            gl.glVertex3f(minBox[0], minBox[1], maxBox[2]);
            gl.glVertex3f(minBox[0], maxBox[1], maxBox[2]);

            gl.glVertex3f(minBox[0], minBox[1], maxBox[2]);
            gl.glVertex3f(maxBox[0], minBox[1], maxBox[2]);

            gl.glVertex3f(minBox[0], maxBox[1], maxBox[2]);
            gl.glVertex3f(minBox[0], maxBox[1], minBox[2]);

            gl.glVertex3f(maxBox[0], maxBox[1], minBox[2]);
            gl.glVertex3f(minBox[0], maxBox[1], minBox[2]);

            gl.glVertex3f(maxBox[0], maxBox[1], minBox[2]);
            gl.glVertex3f(maxBox[0], minBox[1], minBox[2]);

            gl.glVertex3f(maxBox[0], minBox[1], minBox[2]);
            gl.glVertex3f(maxBox[0], minBox[1], maxBox[2]);
            gl.glEnd();

            // Push it back down again as the next call after this will pop it
            gl.glPushMatrix();
        }
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
        DebugShapeRenderable sh = (DebugShapeRenderable)o;

        float[] a = minBounds;
        float[] b = sh.minBounds;

        if(a[0] < b[0])
            return -1;
        else if (a[0] > b[0])
            return 1;

        if(a[1] < b[1])
            return -1;
        else if (a[1] > b[1])
            return 1;

        if(a[2] < b[2])
            return -1;
        else if (a[2] > b[2])
            return 1;

        if(a[3] < b[3])
            return -1;
        else if (a[3] > b[3])
            return 1;

        a = maxBounds;
        b = sh.maxBounds;

        if(a[0] < b[0])
            return -1;
        else if (a[0] > b[0])
            return 1;

        if(a[1] < b[1])
            return -1;
        else if (a[1] > b[1])
            return 1;

        if(a[2] < b[2])
            return -1;
        else if (a[2] > b[2])
            return 1;

        if(a[3] < b[3])
            return -1;
        else if (a[3] > b[3])
            return 1;

        return 0;
    }

}
