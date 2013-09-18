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
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
// None

/**
 * Representation of a spot light source.
 * <p>
 *
 * A spot light has a direction, location and directs light as a conical shape
 * in the given direction. Within the spotlight, there's a maximum angle that
 * the light is effective to, and a drop-off rate of the light from the center
 * to the maximum angle.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>angleRangeMsg: Error message when spot angle is not [0,90] deg</li>
 * <li>negExponentMsg: Error message when falloff exponent given is negative</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.23 $
 */
public class SpotLight extends Light
{
    /** Message when the spot angle is out of range */
    private static final String ANGLE_RANGE_PROP =
        "org.j3d.aviatrix3d.SpotLight.angleRangeMsg";

    /** Message when the spot exponent is negative */
    private static final String NEG_EXP_PROP =
        "org.j3d.aviatrix3d.SpotLight.negExponentMsg";

    /** The colour of the light */
    private float[] direction;

    /** The colour of the light */
    private float[] position;

    /** Cut-off angle for the maximum extent of the spotlight in degrees */
    private float cutOffAngle;

    /** drop-off rate for the spotlight from the center to cutoffAngle */
    private float dropOffRate;

    /** The constant attentuation factor */
    private float cAttenuation;

    /** The linear attentuation factor */
    private float lAttenuation;

    /** The quadratic attentuation factor */
    private float qAttenuation;

    /**
     * Creates a light with the colour set to black.
     */
    public SpotLight()
    {
        super(SPOT_TYPE);

        direction = new float[3];
        position = new float[4];
        cAttenuation = 1;
        cutOffAngle = 45;
        dropOffRate = 0;

        direction[2] = -1;
        position[3] = 1;
    }

    /**
     * Create a light with the given base colour.Colour must be in the range
     * [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     */
    public SpotLight(float[] col)
        throws IllegalArgumentException
    {
        super(SPOT_TYPE, col);

        direction = new float[3];
        position = new float[3];
        cAttenuation = 1;
        cutOffAngle = 45;
        dropOffRate = 0;

        direction[2] = -1;
        position[3] = 1;
    }

    /**
     * Create a light with the given base colour.Colour must be in the range
     * [0, 1] otherwise an exception is generated.
     *
     * @param col The new colour value to use
     * @param dir The direction of the light
     */
    public SpotLight(float[] col, float[] pos, float[] dir)
        throws IllegalArgumentException
    {
        super(SPOT_TYPE, col);

        direction = new float[] { dir[0], dir[1], dir[2] };
        position = new float[] { pos[0], pos[1], pos[2], 1 };
        cAttenuation = 1;
        cutOffAngle = 45;
        dropOffRate = 0;
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
        gl.glLightfv(l_id, GL2.GL_POSITION, position, 0);
        gl.glLightfv(l_id, GL2.GL_SPOT_DIRECTION, direction, 0);
        gl.glLightfv(l_id, GL2.GL_DIFFUSE, diffuseColor, 0);
        gl.glLightfv(l_id, GL2.GL_SPECULAR, specularColor, 0);
        gl.glLightf(l_id, GL2.GL_SPOT_EXPONENT, dropOffRate);
        gl.glLightf(l_id, GL2.GL_SPOT_CUTOFF, cutOffAngle);
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
        int l_id = ((Integer)lightId).intValue();

        gl.glLightf(l_id, GL2.GL_SPOT_CUTOFF, 180.0f);
        gl.glDisable(l_id);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Set the cut-off angle. The angle must lie in the range [0, 90]
     * degrees. Although OpenGL allows the special value of 180 to be
     * used, that functions just like a pointlight, so use that class
     * instead.
     *
     * @param angle The angle in degrees [0, 90]
     * @throws IllegalArgumentException The angle was outside the range
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setCutOffAngle(float angle)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(angle < 0 || angle > 90)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(ANGLE_RANGE_PROP) + angle;
            throw new IllegalArgumentException(msg);
        }

        cutOffAngle = angle;
    }

    /**
     * Get the current gut-off angle. The angle will lie in the range
     * [0, 90] degrees.
     *
     * @return The angle in degrees [0, 90]
     */
    public float getCutOffAngle()
    {
        return cutOffAngle;
    }

    /**
     * Set the value of the exponent that can be used to control how the light
     * intensity drops off from the center to the cut off angle. By default a
     * value of zero is used, and provides an even light value from beginning
     * to edge.
     *
     * @param exp The value of the exponent
     * @throws IllegalArgumentException The exponent was less than zero
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setDropOffRateExponent(float exp)
        throws IllegalArgumentException, InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(exp < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(NEG_EXP_PROP) + exp;
            throw new IllegalArgumentException(msg);
        }

        dropOffRate = exp;
    }

    /**
     * Get the current drop off exponent rate.
     *
     * @return A value greater than or equal to zero.
     */
    public float getDropOffRateExponent()
    {
        return dropOffRate;
    }

    /**
     * Set the direction to the new value. Direction is a vector that the
     * light is shining.
     *
     * @param dir The new direction value to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setDirection(float[] dir)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        direction[0] = dir[0];
        direction[1] = dir[1];
        direction[2] = dir[2];
    }

    /**
     * Set the direction to the new value. Direction is a vector that the
     * light is shining.
     *
     * @param x The x component of the direction value to use
     * @param y The y component of the direction value to use
     * @param z The z component of the direction value to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setDirection(float x, float y, float z)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        direction[0] = x;
        direction[1] = y;
        direction[2] = z;
    }

    /**
     * Retrieve the current direction vector from the light.
     *
     * @param dir An array to copy the direction into
     */
    public void getDirection(float[] dir)
    {
        dir[0] = direction[0];
        dir[1] = direction[1];
        dir[2] = direction[2];
    }

    /**
     * Set the position to the new value. Position is a vector that the
     * light is shining.
     *
     * @param pos The new position value to use
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setPosition(float[] pos)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

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
     *   of the NodeUpdateListener data changed callback method
     */
    public void setPosition(float x, float y, float z)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

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
     *   of the NodeUpdateListener data changed callback method
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
     *   of the NodeUpdateListener data changed callback method
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

        SpotLight sl = (SpotLight)l;

        if(cAttenuation != sl.cAttenuation)
            return cAttenuation < sl.cAttenuation ? -1 : 1;

        if(lAttenuation != sl.lAttenuation)
            return lAttenuation < sl.lAttenuation ? -1 : 1;

        if(qAttenuation != sl.qAttenuation)
            return qAttenuation < sl.qAttenuation ? -1 : 1;

        if(cutOffAngle != sl.cutOffAngle)
            return cutOffAngle < sl.cutOffAngle ? -1 : 1;

        if(dropOffRate != sl.dropOffRate)
            return dropOffRate < sl.dropOffRate ? -1 : 1;

        res = compareColor3(direction, sl.direction);
        if(res != 0)
            return res;

        return compareColor3(position, sl.position);
    }
}
