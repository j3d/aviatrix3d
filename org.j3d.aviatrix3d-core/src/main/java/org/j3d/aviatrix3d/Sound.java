/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

// Local imports
import org.j3d.aviatrix3d.iutil.AudioUpdateListener;
import org.j3d.aviatrix3d.rendering.AudioRenderable;
import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.aviatrix3d.rendering.LeafCullable;
import org.j3d.aviatrix3d.rendering.Renderable;

/**
 * A Sound class represents all sound emiting nodes in the system.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.18 $
 */
public abstract class Sound extends Leaf
   implements LeafCullable, AudioRenderable, AudioUpdateListener
{
    /** The source feeding this sound */
    protected AudioComponent soundSource;

    /** The format of this sound.  Defined in AudioSource */
    protected int format;

    /** The frequency of the samples */
    protected int freq;

    /** Whether to loop this sample */
    protected boolean loop;

    /** The buffer created for this sound */
    protected int buffer;

    /** The sounds pitch */
    protected float pitch;

    /** The current seq */
    protected int seq;

    /** Has the sound data  changed */
    protected boolean dataChanged;

    /** Has the play state changed */
    protected boolean playChanged;

    /** Has the sound params */
    protected boolean paramsChanged;

    /** Is the sound playing */
    protected boolean playing;

    /** Is the sound paused */
    protected boolean paused;

    /** Is the sound currently enabled */
    protected boolean enabled;

    /**
     * A temporary value used to fetch values from OpenAL. This is allocated
     * to be the size of a single int in the constructor.
     */
    protected IntBuffer values;

    /**
     * Creates a sound. By default it is not enabled.
     */
    public Sound()
    {
        dataChanged = false;
        playChanged = false;
        paramsChanged = false;
        playing = false;
        paused = false;
        enabled = false;

        // Need to allocate a byte buffer of 4 bytes, to equate to an int
        // size is treated as bytes, not number of ints.
        ByteBuffer buf = ByteBuffer.allocateDirect(4);
        buf.order(ByteOrder.nativeOrder());
        values = buf.asIntBuffer();
    }

    //----------------------------------------------------------
    // Methods defined by Node
    //----------------------------------------------------------

    /**
     * Mark this node as having dirty bounds due to one of it's children having
     * their bounds changed.
     */
    @Override
    protected void markBoundsDirty()
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
     * Get the currently set bounds for this object. If no explicit bounds have
     * been set, then an implicit set of bounds is returned based on the
     * current scene graph state.
     *
     * @return The current bounds of this object
     */
    @Override
    public BoundingVolume getBounds()
    {
        return INVALID_BOUNDS;
    }

    //----------------------------------------------------------
    // Methods defined by AudioUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that the audio has updated a section of the sample.
     * This is generic for all audio sources correctly.
     *
     * @param format The format of the samples
     * @param frequency The frequency of the samples
     * @param seq The sample seq.  Seq 0 means complete file.
     *            Streamed sources start at 1
     */
    @Override
    public void audioUpdated(int format, int frequency, int seq)
    {

        this.format = format;
        this.freq = frequency;

        // Seq ignored for now
        this.seq = seq;

        dataChanged = true;
    }

    /**
     * Notification that the audio's parameters have changed.
     *
     * @param loop Whether to loop this sample
     * @param pitch The pitch to play at.
     */
    @Override
    public void paramsUpdated(boolean loop, float pitch)
    {
        this.pitch = pitch;
        this.loop = loop;

        paramsChanged = true;
    }

    //----------------------------------------------------------
    // Methods defined by LeafCullable
    //----------------------------------------------------------

    /**
     * Get the type that this cullable represents.
     *
     * @return One of the _CULLABLE constants
     */
    @Override
    public int getCullableType()
    {
        return AUDIO_CULLABLE;
    }

    /**
     * Get the child renderable of this object.
     *
     * @return an array of nodes
     */
    @Override
    public Renderable getRenderable()
    {
        return this;
    }


    //----------------------------------------------------------
    // Methods defined by AudioRenderable
    //----------------------------------------------------------

    /**
     * State check to see whether the sound is enabled.
     *
     * @return true if the sound has something to render
     */
    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Check to see if this renderable is spatialised in any way. Spatialised
     * means it would require proper head tracking, where non-spatialised just
     * represents a basic noise such as background sound.
     * <p>
     * Default implementation returns true. Derived classes should overrride as
     * needed.
     *
     * @return true always unless overrriden
     */
    @Override
    public boolean isSpatialised()
    {
        return true;
    }


    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the enabled state of the light. Can use this to turn it on and off
     * in a general fashion.
     *
     * @param state The new state of the light
     * @throws InvalidWriteTimingException This was not called during the
     *   data changed callback time
     */
    public void setEnabled(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        enabled = state;
    }

    /**
     * Start a sound playing. If the sound was previously paused, this will
     * restart it.
     *
     * @throws InvalidWriteTimingException This was not called during the
     *   data changed callback time
     */
    public void startSound()
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if (!playing || paused)
        {
            playChanged = true;
            playing = true;
            paused = false;
        }
    }

    /**
     * Stop a sound playing.
     *
     * @throws InvalidWriteTimingException This was not called during the
     *   data changed callback time
     */
    public void stopSound()
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if (playing)
        {
            playChanged = true;
            playing = false;
            paused = false;
        }
    }

    /**
     * Is this sound currently playing.
     *
     * @return Whether the sound is playing.
     */
    public boolean isPlaying()
    {
        return playing;
    }

    /**
     * Pause a sound playing. To restart the sound playing from it's
     * current position, use {@link #startSound}
     *
     * @throws InvalidWriteTimingException This was not called during the
     *   data changed callback time
     */
    public void pauseSound()
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if (playing && !paused)
        {
            playChanged = true;
            paused = true;
        }
    }

    /**
     * Is this sound currently paused;
     *
     * @return Whether the sound is paused.
     */
    public boolean isPaused()
    {
        return paused;
    }

    /**
     * Set the source for the sound to use.
     *
     * @param src The source of the sound
     * @throws InvalidWriteTimingException This was not called during the
     *   data changed callback time
     */
    public void setAudioSource(AudioComponent src)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        soundSource = src;

        this.format = src.getFormat();
        this.freq = src.getFrequency();
        this.loop = src.getLoop();
        this.pitch = src.getPitch();

        src.addUpdateListener(this);

        dataChanged = true;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param snd The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    protected int compareTo(Sound snd)
    {
        if(enabled != snd.enabled)
            return enabled ? 1 : -1;

        if(playing != snd.playing)
            return playing ? 1 : -1;

        if(paused != snd.paused)
            return paused ? 1 : -1;

        if(loop != snd.loop)
            return loop ? 1 : -1;

        if(format != snd.format)
            return format < snd.format ? -1 : 1;

        if(freq != snd.freq)
            return freq < snd.freq? -1 : 1;

        if(pitch != snd.pitch)
            return pitch < snd.pitch ? -1 : 1;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param snd The sound instance to be compared
     * @return true if the objects represent identical values
     */
    protected boolean equals(Sound snd)
    {
        if((enabled != snd.enabled) ||
           (playing != snd.playing) ||
           (paused != snd.paused) ||
           (loop != snd.loop) ||
           (freq != snd.freq) ||
           (pitch != snd.pitch))
            return false;

        return true;
    }
}
