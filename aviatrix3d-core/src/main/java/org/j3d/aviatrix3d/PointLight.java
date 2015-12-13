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

// External imports
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

// Local imports
// None

/**
 * Representation of a point light source.
 * <p>
 *
 * A positional light has a position, but no orientation and attenuates
 * over distance from the position.
 *
 * @author Justin Couch
 * @version $Revision: 1.22 $
 */
public class PointLight extends Light
{
    /** The colour of the light */
    protected float[] position;

    /** The constant attentuation factor */
    protected float cAttenuation;

    /** The linear attentuation factor */
    protected float lAttenuation;

    /** The quadratic attentuation factor */
    protected float qAttenuation;

    /**
     * Creates a light with the colour set to black.
     */
    public PointLight()
    {
        super(POINT_TYPE);

        position = new float[4];
        position[3] = 1;

        cAttenuation = 1;
    }

    /**
     * Create a light with the given base colour.Colour must be in the range
     * [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     */
    public PointLight(float[] col)
        throws IllegalArgumentException
    {
        super(POINT_TYPE, col);

        position = new float[4];
        position[3] = 1;

        cAttenuation = 1;
    }

    /**
     * Create a light with the given base colour.Colour must be in the range
     * [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     * @param pos The position of the light
     */
    public PointLight(float[] col, float[] pos)
        throws IllegalArgumentException
    {
        super(POINT_TYPE, col);

        position = new float[] { pos[0], pos[1], pos[2], 1 };

        cAttenuation = 1;
    }

    //---------------------------------------------------------------
    // Methods defined by ComponentRenderable
    //---------------------------------------------------------------

    /**
     * Overloaded form of the render() method to render the light details given
     * the specific Light ID used by OpenGL. Since the active light ID for this
     * node may vary over time, a fixed ID cannot be used by OpenGL. The
     * renderer will always call this method rather than the normal render()
     * method. The normal post render will still be called
     *
     * @param gl The GL context to render with
     * @param lightId the ID of the light to make GL calls with
     */
    @Override
    public void render(GL2 gl, Object lightId)
    {
        int l_id = ((Integer)lightId).intValue();

        gl.glLightfv(l_id, GL2.GL_AMBIENT, ambientColor, 0);
        gl.glLightfv(l_id, GL2.GL_SPECULAR, specularColor, 0);
        gl.glLightfv(l_id, GL2.GL_DIFFUSE, diffuseColor, 0);
        gl.glLightfv(l_id, GL2.GL_POSITION, position, 0);
        gl.glLightf(l_id, GL2.GL_CONSTANT_ATTENUATION, cAttenuation);
        gl.glLightf(l_id, GL2.GL_LINEAR_ATTENUATION, lAttenuation);
        gl.glLightf(l_id, GL2.GL_QUADRATIC_ATTENUATION, qAttenuation);

        gl.glEnable(l_id);
    }

    /*
     * Overloaded form of the postRender() method to render the light details given
     * the specific Light ID used by OpenGL. Since the active light ID for this
     * node may vary over time, a fixed ID cannot be used by OpenGL. The
     * renderer will always call this method rather than the normal postRender()
     * method. The normal post render will still be called
     *
     * @param gl The GL context to render with
     * @param lightId the ID of the light to make GL calls with
     */
    @Override
    public void postRender(GL2 gl, Object lightId)
    {
        gl.glDisable(((Integer)lightId).intValue());
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

    /**
     * Mark this node as having dirty bounds due to one of it's children having
     * their bounds changed.
     */
    @Override
    protected void markBoundsDirty()
    {
        if(parent != null)
            parent.markBoundsDirty();
    }

    //---------------------------------------------------------------
    // Misc local methods
    //---------------------------------------------------------------

    /**
     * Set the position to the new value. Position is a vector that the
     * light is shining.
     *
     * @param pos The new position value to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds change callback method
     */
    public void setPosition(float[] pos)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        position[0] = pos[0];
        position[1] = pos[1];
        position[2] = pos[2];
    }

    /**
     * Set the position to the new value. Position is a vector that the
     * light is shining.
     *
     * @param x The x component of the position value to use
     * @param y The y component of the position value to use
     * @param z The z component of the position value to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds change callback method
     */
    public void setPosition(float x, float y, float z)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        position[0] = x;
        position[1] = y;
        position[2] = z;
    }

    /**
     * Retrieve the current position vector from the light.
     *
     * @param pos An array to copy the position into
     */
    public void getPosition(float[] pos)
    {
        pos[0] = position[0];
        pos[1] = position[1];
        pos[2] = position[2];
    }

    /**
     * Set the attenuation factors for the light. See class header
     * documentation for more inforamtion on these values.
     *
     * @param constant The constant attenuation factor
     * @param linear The linear attenuation factor
     * @param quad The quadratic attenuation factor
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     */
    public void setAttenuation(float constant, float linear, float quad)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        cAttenuation = constant;
        lAttenuation = linear;
        qAttenuation = quad;
    }

    /**
     * Set the attenuation factors for the light. See class header
     * documentation for more inforamtion on these values.
     *
     * @param values Each value in the order:<br>
     *    [0] The constant attenuation factor<br>
     *    [1] The linear attenuation factor<br>
     *    [2] The quadratic attenuation factor
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data change callback method
     */
    public void setAttenuation(float[] values)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        cAttenuation = values[0];
        lAttenuation = values[1];
        qAttenuation = values[2];
    }

    /**
     * Set the attenuation factors for the light. See class header
     * documentation for more inforamtion on these values.
     *
     * @param values Array to copy the values into, in the order:<br>
     *    [0] The constant attenuation factor<br>
     *    [1] The linear attenuation factor<br>
     *    [2] The quadratic attenuation factor
     */
    public void getAttenuation(float[] values)
    {
        values[0] = cAttenuation;
        values[1] = lAttenuation;
        values[2] = qAttenuation;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param l The light instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Light l)
    {
        int res = super.compareTo(l);
        if(res != 0)
            return res;

        PointLight pl = (PointLight)l;

        if(cAttenuation != pl.cAttenuation)
            return cAttenuation < pl.cAttenuation ? -1 : 1;

        if(lAttenuation != pl.lAttenuation)
            return lAttenuation < pl.lAttenuation ? -1 : 1;

        if(qAttenuation != pl.qAttenuation)
            return qAttenuation < pl.qAttenuation ? -1 : 1;

        return compareColor3(position, pl.position);
    }
}
