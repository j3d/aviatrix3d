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
import java.text.Format;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import java.nio.ByteBuffer;
import net.java.games.joal.AL;

import org.j3d.util.MatrixUtils;
import org.j3d.util.I18nManager;

// Local imports
// None

/**
 * A ConeSound class which emits a conical sound in one direction.
 * <p>
 * By default this class is mimics a ConeSound.  You must provide
 * parameters for the direction and cone angles to make it directional.
 *
 * The sound will attenuate by distance based on the refDistance
 * and maxDistance parameters.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>sourceCreateFailedMsg: Error message when the something when wrong
 *     creating the underlying OpenAL buffers</li>
 * <li>sourceParamsFailedMsg: Error message when setting up the source
 *     buffer params failed.</li>
 * </ul>
 *
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public class ConeSound extends PointSound
{
    /** Message when OpenAL buffer creation failed */
    private static final String BUFFER_CREATE_PROP =
        "org.j3d.aviatrix3d.ConeSound.sourceCreateFailedMsg";

    /** Message when OpenAL buffer creation failed */
    private static final String BUFFER_PARAMS_PROP =
        "org.j3d.aviatrix3d.ConeSound.sourceParamsFailedMsg";

    /** The OpenAL source */
    private int source;

    /** ConeSound Position */
    private Point3f position;

    /** Scratch point */
    private Point3f tmpPoint;

    /** Scratch matrix val */
    private Matrix4f tmpMatrix;

    /** The sound direction */
    private float[] direction;

    /** The cones inner angle in degrees */
    private float innerAngle;

    /** The cones outer angle in degrees */
    private float outerAngle;

    /**
     * Creates a sound.
     */
    public ConeSound()
    {
        position = new Point3f();
        tmpPoint = new Point3f();
        tmpMatrix = new Matrix4f();

        refDistance = 0;
        maxDistance = Float.MAX_VALUE;
        innerAngle = 360;
        outerAngle = 360;
        direction = new float[] {0,0,0};
        rolloffFactor = 1;
    }

    //----------------------------------------------------------
    // Methods defined by AudioRenderable
    //----------------------------------------------------------

    /**
     * This method is called to render this node.  All openGL commands needed
     * to render the node should be executed.  Any transformations needed
     * should be added to the transformation stack premultiplied.  This
     * method must be re-entrant as it can be called from multiple
     * places at once.
     *
     * @param al The OpenAL context to render with
     * @param transform The transformation stack to this node
     */
    public void render(AL al, Matrix4f transform)
    {
        transform.transform(position,tmpPoint);

        if(dataChanged)
        {
            buffer = soundSource.getBufferId(al, seq);
            if(buffer == -1)
                return;

            dataChanged = false;
            playChanged = true;

            // Bind buffer with a source.
            values.rewind();

            // Bind buffer with a source.
            al.alGenSources(1, values);
            source = values.get(0);

            int error = al.alGetError();
            if (error != AL.AL_NO_ERROR)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                Locale lcl = intl_mgr.getFoundLocale();
                String msg_pattern = intl_mgr.getString(BUFFER_CREATE_PROP);

                Object[] msg_args = { error };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);

                System.out.println(msg);
            }
            else
            {
                al.alSourcei(source, AL.AL_BUFFER, buffer);
                al.alSourcef(source, AL.AL_REFERENCE_DISTANCE, refDistance);
                al.alSourcef(source, AL.AL_ROLLOFF_FACTOR, rolloffFactor);
                al.alSourcef(source, AL.AL_MAX_DISTANCE, maxDistance);
                al.alSourcef(source, AL.AL_CONE_INNER_ANGLE, innerAngle);
                al.alSourcef(source, AL.AL_CONE_OUTER_ANGLE, outerAngle);
                al.alSourcefv(source, AL.AL_DIRECTION, direction, 0);
                al.alSourcef(source, AL.AL_GAIN, 1.0f);
                al.alSourcei(source, AL.AL_LOOPING, loop ? 1 : 0);
                al.alSourcef(source, AL.AL_PITCH, pitch);

                al.alSource3f(source, AL.AL_POSITION, tmpPoint.x, tmpPoint.y,tmpPoint.z);

                error = al.alGetError();
                if (error != AL.AL_NO_ERROR)
                {
                    I18nManager intl_mgr = I18nManager.getManager();
                    Locale lcl = intl_mgr.getFoundLocale();
                    String msg_pattern = intl_mgr.getString(BUFFER_PARAMS_PROP);

                    Object[] msg_args = { error };
                    MessageFormat msg_fmt =
                        new MessageFormat(msg_pattern, lcl);
                    String msg = msg_fmt.format(msg_args);

                    System.out.println(msg);
                }
            }
        }

        if(paramsChanged)
        {
            al.alSourcef(source, AL.AL_PITCH, pitch);
            al.alSourcei(source, AL.AL_LOOPING, loop ? 1 : 0);

            paramsChanged = false;
        }

        al.alSource3f(source, AL.AL_POSITION, tmpPoint.x, tmpPoint.y,tmpPoint.z);

        if(playChanged)
        {
            if(playing && paused)
            {
                al.alSourcePlay(source);
            }

            if(playing)
            {
                if(paused)
                    al.alSourcePause(source);
                else
                    al.alSourcePlay(source);
            }
            else
                al.alSourceStop(source);

            playChanged = false;
        }
        else if(playing)
        {
            // Bind buffer with a source.
            values.rewind();

            al.alGetSourcei(source, AL.AL_SOURCE_STATE, values);
            if(values.get(0) == AL.AL_STOPPED)
            {
                playing = false;
            }
        }
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     *
     * @param al The OpenAL context to draw with
     */
    public void postRender(AL al)
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
        ConeSound app = (ConeSound)o;
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
        if(!(o instanceof ConeSound))
            return false;
        else
            return equals((ConeSound)o);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the direction the cone is facing.
     *
     * @param dir The direction to emit sound.
     */
    public void setRefDistance(float[] dir)
    {
        if(alive && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        direction[0] = dir[0];
        direction[1] = dir[1];
        direction[2] = dir[2];
    }

    /**
     * Retrieve the direction of the cone.
     *
     * @param dir An array length 3 to copy the direction value into
     */
    public void getRefDistance(float[] dir)
    {
        dir[0] = direction[0];
        dir[1] = direction[1];
        dir[2] = direction[2];
    }

    /**
     * Set the inner angle for the cone.
     *
     * @param angle The inner angle
     */
    public void setInnerAngle(float angle)
    {
        if(alive && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        innerAngle = angle;
    }

    /**
     * Set the outer angle for the cone.
     *
     * @param angle The outer angle
     */
    public void setOuterAngle(float angle)
    {
        if(alive && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        outerAngle = angle;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param cs The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(ConeSound cs)
    {
        if(cs == null)
            return 1;

        if(cs == this)
            return 0;

        int res = super.compareTo(cs);
        if(res != 0)
            return res;

        res = compareVector(direction, cs.direction);
        if(res != 0)
            return res;

        if(innerAngle != cs.innerAngle)
            return innerAngle < cs.innerAngle ? -1 : 1;

        if(outerAngle != cs.outerAngle)
            return outerAngle < cs.outerAngle ? -1 : 1;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param cs The background instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(ConeSound cs)
    {
        if(cs == this)
            return true;

        if(cs == null)
            return false;

        if(!super.equals(cs))
            return false;

        if((innerAngle != cs.innerAngle) ||
           (outerAngle != cs.outerAngle) ||
           (direction[0] != cs.direction[0]) ||
           (direction[1] != cs.direction[1]) ||
           (direction[2] != cs.direction[2]))
            return false;

        return true;
    }

    /**
     * Compare 2 vector arrays of length 3 for equality
     *
     * @param a The first colour array to check
     * @param b The first colour array to check
     * @return -1 if a[i] < b[i], +1 if a[i] > b[i], otherwise 0
     */
    private int compareVector(float[] a, float[] b)
    {
        if(a[0] < b[0])
            return -1;
        else if(a[0] > b[0])
            return 1;

        if(a[1] < b[1])
            return -1;
        else if(a[1] > b[1])
            return 1;

        if(a[2] < b[2])
            return -1;
        else if(a[2] > b[2])
            return 1;

        return 0;
    }
}
