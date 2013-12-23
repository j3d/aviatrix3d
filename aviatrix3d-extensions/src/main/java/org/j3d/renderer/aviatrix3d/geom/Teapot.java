/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004
 *                          Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom;

// External imports
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

// Local imports
import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.BoundingVoid;

/**
 * A simple teapot that is a low-level geometry primitive.
 * <p>
 *
 * Internally this uses the OpenGL evaluators to work through the bezier patch
 * data for rendering. The code is liberally stolen from the GLUT source which
 * is copyright SGI. Converted to the JOGL API methods and different calling
 * conventions.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class Teapot extends Geometry
{
    private static final int[][] patchdata =
    {
     /* rim */
      {102, 103, 104, 105, 4, 5, 6, 7, 8, 9, 10, 11,
        12, 13, 14, 15},

     /* body */
      {12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, 26, 27},
      {24, 25, 26, 27, 29, 30, 31, 32, 33, 34, 35, 36,
        37, 38, 39, 40},
     /* lid */
      {96, 96, 96, 96, 97, 98, 99, 100, 101, 101, 101,
        101, 0, 1, 2, 3,},
      {0, 1, 2, 3, 106, 107, 108, 109, 110, 111, 112,
        113, 114, 115, 116, 117},
     /* bottom */
      {118, 118, 118, 118, 124, 122, 119, 121, 123, 126,
        125, 120, 40, 39, 38, 37},
     /* handle */
      {41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52,
        53, 54, 55, 56},
      {53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64,
        28, 65, 66, 67},
     /* spout */
      {68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
        80, 81, 82, 83},
      {80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91,
        92, 93, 94, 95}
    };

    private static final float[][] cpdata =
    {
        {0.2f, 0, 2.7f}, {0.2f, -0.112f, 2.7f}, {0.112f, -0.2f, 2.7f},
        {0, -0.2f, 2.7f}, {1.3375f, 0, 2.53125f}, {1.3375f, -0.749f, 2.53125f},
        {0.749f, -1.3375f, 2.53125f}, {0, -1.3375f, 2.53125f},
        {1.4375f, 0, 2.53125f}, {1.4375f, -0.805f, 2.53125f},
        {0.805f, -1.4375f, 2.53125f}, {0, -1.4375f, 2.53125f}, {1.5f, 0, 2.4f},
        {1.5f, -0.84f, 2.4f}, {0.84f, -1.5f, 2.4f}, {0, -1.5f, 2.4f},
        {1.75f, 0, 1.875f}, {1.75f, -0.98f, 1.875f}, {0.98f, -1.75f, 1.875f},
        {0, -1.75f, 1.875f}, {2, 0, 1.35f}, {2, -1.12f, 1.35f}, {1.12f, -2, 1.35f},
        {0, -2, 1.35f}, {2, 0, 0.9f}, {2, -1.12f, 0.9f}, {1.12f, -2, 0.9f},
        {0, -2, 0.9f}, {-2, 0, 0.9f}, {2, 0, 0.45f}, {2, -1.12f, 0.45f},
        {1.12f, -2, 0.45f}, {0, -2, 0.45f}, {1.5f, 0, 0.225f},
        {1.5f, -0.84f, 0.225f}, {0.84f, -1.5f, 0.225f}, {0, -1.5f, 0.225f},
        {1.5f, 0, 0.15f}, {1.5f, -0.84f, 0.15f}, {0.84f, -1.5f, 0.15f},
        {0, -1.5f, 0.15f}, {-1.6f, 0, 2.025f}, {-1.6f, -0.3f, 2.025f},
        {-1.5f, -0.3f, 2.25f}, {-1.5f, 0, 2.25f}, {-2.3f, 0, 2.025f},
        {-2.3f, -0.3f, 2.025f}, {-2.5f, -0.3f, 2.25f}, {-2.5f, 0, 2.25f},
        {-2.7f, 0, 2.025f}, {-2.7f, -0.3f, 2.025f}, {-3, -0.3f, 2.25f},
        {-3, 0, 2.25f}, {-2.7f, 0, 1.8f}, {-2.7f, -0.3f, 1.8f}, {-3, -0.3f, 1.8f},
        {-3, 0, 1.8f}, {-2.7f, 0, 1.575f}, {-2.7f, -0.3f, 1.575f},
        {-3, -0.3f, 1.35f}, {-3, 0, 1.35f}, {-2.5f, 0, 1.125f},
        {-2.5f, -0.3f, 1.125f}, {-2.65f, -0.3f, 0.9375f}, {-2.65f, 0, 0.9375f},
        {-2, -0.3f, 0.9f}, {-1.9f, -0.3f, 0.6f}, {-1.9f, 0, 0.6f},
        {1.7f, 0, 1.425f}, {1.7f, -0.66f, 1.425f}, {1.7f, -0.66f, 0.6f},
        {1.7f, 0, 0.6f}, {2.6f, 0, 1.425f}, {2.6f, -0.66f, 1.425f},
        {3.1f, -0.66f, 0.825f}, {3.1f, 0, 0.825f}, {2.3f, 0, 2.1f},
        {2.3f, -0.25f, 2.1f}, {2.4f, -0.25f, 2.025f}, {2.4f, 0, 2.025f},
        {2.7f, 0, 2.4f}, {2.7f, -0.25f, 2.4f}, {3.3f, -0.25f, 2.4f},
        {3.3f, 0, 2.4f}, {2.8f, 0, 2.475f}, {2.8f, -0.25f, 2.475f},
        {3.525f, -0.25f, 2.49375f}, {3.525f, 0, 2.49375f}, {2.9f, 0, 2.475f},
        {2.9f, -0.15f, 2.475f}, {3.45f, -0.15f, 2.5125f}, {3.45f, 0, 2.5125f},
        {2.8f, 0, 2.4f}, {2.8f, -0.15f, 2.4f}, {3.2f, -0.15f, 2.4f},
        {3.2f, 0, 2.4f}, {0, 0, 3.15f}, {0.8f, 0, 3.15f}, {0.8f, -0.45f, 3.15f},
        {0.45f, -0.8f, 3.15f}, {0, -0.8f, 3.15f}, {0, 0, 2.85f}, {1.4f, 0, 2.4f},
        {1.4f, -0.784f, 2.4f}, {0.784f, -1.4f, 2.4f}, {0, -1.4f, 2.4f},
        {0.4f, 0, 2.55f}, {0.4f, -0.224f, 2.55f}, {0.224f, -0.4f, 2.55f},
        {0, -0.4f, 2.55f}, {1.3f, 0, 2.55f}, {1.3f, -0.728f, 2.55f},
        {0.728f, -1.3f, 2.55f}, {0, -1.3f, 2.55f}, {1.3f, 0, 2.4f},
        {1.3f, -0.728f, 2.4f}, {0.728f, -1.3f, 2.4f}, {0, -1.3f, 2.4f},
        {0, 0, 0}, {1.425f, -0.798f, 0}, {1.5f, 0, 0.075f}, {1.425f, 0, 0},
        {0.798f, -1.425f, 0}, {0, -1.5f, 0.075f}, {0, -1.425f, 0},
        {1.5f, -0.84f, 0.075f}, {0.84f, -1.5f, 0.075f}
    };

    /** Texture coordinates to map between */
    private static final float[] tex =
        { 0, 0, 1, 0, 0, 1, 1, 1 };

    /** Working variable for the p coordinate of the evaluator */
    private float[] p;

    /** Working variable for the q coordinate of the evaluator */
    private float[] q;

    /** Working variable for the r coordinate of the evaluator */
    private float[] r;

    /** Working variable for the s coordinate of the evaluator */
    private float[] s;


    /** The scale that should be applied to this instance */
    private float scale;

    /** The primitive type to render the geometry as */
    private int gridSize;

    /** Temp var used to fetch the polygon mode each time we render */
    private int[] polygonMode;

    /**
     * Construct a default instance of the teapot with the given scale factor.
     *
     * @param scale The scale of the teapot to use
     */
    public Teapot(float scale, int gridSize)
    {
        this.scale = scale;
        this.gridSize = gridSize;

        p = new float[4 * 4 * 3];
        q = new float[4 * 4 * 3];
        r = new float[4 * 4 * 3];
        s = new float[4 * 4 * 3];

        polygonMode = new int[2];

        bounds = new BoundingVoid();
    }

    //---------------------------------------------------------------
    // Methods defined by GeometryRenderable
    //---------------------------------------------------------------

    /**
     * Check to see whether this shape is something that represents 2D or 3D
     * geometry. Pure 2D geometry is not effected by any
     * {@link org.j3d.aviatrix3d.rendering.EffectRenderable}, while 3D is.
     * Note that this can be changed depending on the type of geometry itself.
     * A Shape3D node with an IndexedLineArray that only has 2D coordinates is
     * as much a 2D geometry as a raster object.
     *
     * @return True if this is 2D geometry, false if this is 3D
     */
    public boolean is2D()
    {
        return false;
    }

    //---------------------------------------------------------------
    // Methods defined by ObjectRenderable
    //---------------------------------------------------------------

    /**
     * Issue ogl commands needed for this renderable object.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void render(GL2 gl)
    {
        int i, j, k, l;

        // Find out what fill state is currently set and then tell the
        // evaluator to use that.
        gl.glGetIntegerv(GL2.GL_POLYGON_MODE, polygonMode, 0);
        int type = polygonMode[0];

        gl.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_EVAL_BIT);
        gl.glEnable(GL2.GL_AUTO_NORMAL);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glEnable(GL2.GL_MAP2_VERTEX_3);
        gl.glEnable(GL2.GL_MAP2_TEXTURE_COORD_2);
        gl.glPushMatrix();
        gl.glRotatef(270.0f, 1, 0, 0);
        gl.glScalef(0.5f * scale, 0.5f * scale, 0.5f * scale);
        gl.glTranslatef(0, 0, -1.5f);

        for(i = 0; i < 10; i++)
        {
            for(j = 0; j < 4; j++)
            {
                for(k = 0; k < 4; k++)
                {
                    for (l = 0; l < 3; l++)
                    {
                        int pos = j * 12 + k * 3 + l;

                        p[pos] = cpdata[patchdata[i][j * 4 + k]][l];
                        q[pos] = cpdata[patchdata[i][j * 4 + (3 - k)]][l];

                        if(l == 1)
                            q[pos] *= -1;

                        if(i < 6)
                        {
                            r[pos] = cpdata[patchdata[i][j * 4 + (3 - k)]][l];
                            if(l == 0)
                                r[pos] *= -1;

                            s[pos] = cpdata[patchdata[i][j * 4 + k]][l];

                            switch(l)
                            {
                                case 0:
                                case 1:
                                    s[pos] *= -1;
                                    break;
                            }
                        }
                    }
                }
            }

            gl.glMap2f(GL2.GL_MAP2_TEXTURE_COORD_2, 0, 1, 2, 2, 0, 1, 4, 2, tex, 0);
            gl.glMap2f(GL2.GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, p, 0);

            gl.glMapGrid2f(gridSize, 0, 1, gridSize, 0, 1);
            gl.glEvalMesh2(type, 0, gridSize, 0, gridSize);

            gl.glMap2f(GL2.GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, q, 0);
            gl.glEvalMesh2(type, 0, gridSize, 0, gridSize);

            if(i < 6)
            {
                gl.glMap2f(GL2.GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, r, 0);
                gl.glEvalMesh2(type, 0, gridSize, 0, gridSize);

                gl.glMap2f(GL2.GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, s, 0);
                gl.glEvalMesh2(type, 0, gridSize, 0, gridSize);
            }
        }

        gl.glPopMatrix();
        gl.glPopAttrib();
    }

    //---------------------------------------------------------------
    // Methods defined by Geometry
    //---------------------------------------------------------------

    /**
     * Update this node's bounds and then call the parent to update it's
     * bounds. Used to propogate bounds changes from the leaves of the tree
     * to the root. A node implementation may decide when and where to tell
     * the parent(s)s that updates are ready.
     */
    @Override
    protected void updateBounds()
    {
    }

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    @Override
    protected void recomputeBounds()
    {
    }

    /**
     * Mark this node as having dirty bounds due to it's geometry having
     * changed.
     */
    @Override
    protected void markBoundsDirty()
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
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        Teapot tp = (Teapot)o;
        return compareTo(tp);
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
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Teapot))
            return false;
        else
            return equals((Teapot)o);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ta The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Teapot ta)
    {
        if(ta == null)
            return 1;

        if(ta == this)
            return 0;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ta The geometry instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(Teapot ta)
    {
        return (ta == this);
    }
}
