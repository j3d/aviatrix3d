/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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

// Standard imports
import javax.vecmath.Matrix4f;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;

// Local imports
// None

/**
 * A Shape3D class wraps all geometry and appearance information.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class Shape3D extends Leaf
{
    /** Geometry that this shape uses */
    private Geometry geom;

    /** Appearance to render the geometry with */
    private Appearance app;

    /** Transformation matrix for this object */
    private float[] matrix;

    /**
     * Creates a shape with no geometry or appearance set.
     */
    public Shape3D()
    {
        matrix = new float[16];
    }

    /**
     * Set the geometry for this shape.
     *
     * @param newGeom The geometry
     */
    public void setGeometry(Geometry newGeom)
    {
         geom = newGeom;
         if (geom != null)
            geom.setUpdateHandler(updateHandler);
    }

    /**
     * Set the appearance for this shape.
     *
     * @param newApp The appearance
     */
    public void setAppearance(Appearance newApp)
    {
         app = newApp;
         if (app != null)
            app.setUpdateHandler(updateHandler);
    }

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    public void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        if (app != null)
            app.setUpdateHandler(updateHandler);

        if (geom != null)
            geom.setUpdateHandler(updateHandler);
    }

    /**
     * This method is called to render this node.  All openGL commands needed
     * to render the node should be executed.  Any transformations needed
     * should be added to the transformation stack premultiplied.  This
     * method must be re-entrant as it can be called from multiple
     * places at once.
     *
     * @param gld The drawable for setting the state
     */
    public void render(GL gl, GLU glu)
    {
/*
        gl.glPushMatrix();
        //System.out.println("Shape Trans1:");
        //SGUtils.printMatrix(gld, GLEnum.GL_MODELVIEW_MATRIX);

        gl.glMultMatrixf(transform.data);

        //System.out.println("Shape Trans2:");
        //SGUtils.printMatrix(gld, GLEnum.GL_MODELVIEW_MATRIX);
*/
        if(app != null)
            app.renderState(gl, glu);

        if(geom != null)
            geom.renderState(gl, glu);
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     *
     * @param gld The drawable for resetting the state
     */
    public void postRender(GL gl, GLU glu)
    {
        if(geom != null)
            geom.restoreState(gl, glu);

        if(app != null)
            app.restoreState(gl, glu);

        //gl.glPopMatrix();
    }
}
