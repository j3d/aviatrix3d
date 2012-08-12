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
import java.awt.image.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.text.Format;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.HashMap;
import net.java.games.joal.AL;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.iutil.AudioUpdateListener;

/**
 * Common representation of a component that contains source data to be used
 * in audio.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>bufferCreateFailedMsg: Error message when the something when wrong
 *     creating the underlying OpenAL buffers/li>
 * </ul>
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public abstract class AudioComponent extends NodeComponent
    implements AudioSource
{
    /** Message when OpenAL buffer creation failed */
    private static final String BUFFER_CREATE_PROP =
        "org.j3d.aviatrix3d.AudioComponent.bufferCreateFailedMsg";

    /** paramUpdate listener callback exception */
    private static final String PARAM_UPDATE_PROP =
        "org.j3d.aviatrix3d.AudioComponent.paramListenerMsg";

    /** audioUpdate listener callback exception */
    private static final String AUDIO_UPDATE_PROP =
        "org.j3d.aviatrix3d.AudioComponent.audioListenerMsg";

    /** Initial size of the listener list */
    private static final int LISTENER_SIZE = 1;

    /** Amount to resize the listener list when needed */
    private static final int LISTENER_INC = 2;

    /** Buffer to hold the data */
    protected ByteBuffer data;

    /** The buffer id, -1 if unassigned */
    private int bufferId;

    /** List of currently valid listeners */
    private AudioUpdateListener[] listeners;

    /** Number of currently valid listeners */
    private int numListeners;

    /** The format of the data.  Defined in AudioSource */
    protected int format;

    /** The frequency */
    protected int frequency;

    /** The pitch */
    protected float pitch;

    /** Does the sample loop */
    protected boolean loop;

    /**
     * Constructs an Audio component with default values.
     */
    public AudioComponent()
    {
        bufferId = -1;
        listeners = new AudioUpdateListener[LISTENER_SIZE];
        pitch = 1.0f;
    }

    //---------------------------------------------------------------
    // Methods defined by AudioSource
    //---------------------------------------------------------------

    /**
     * Get the format of this audio source. Defintions are provided in
     * AudioSource.
     *
     * @return the format.
     */
    public int getFormat()
    {
        return format;
    }

    /**
     * Get the frequency of this sound source.
     *
     * @return the format
     */
    public int getFrequency()
    {
        return frequency;
    }

    /**
     * Does the sample loop.
     *
     * @return TRUE if it loops.
     */
    public boolean getLoop()
    {
        return loop;
    }

    /**
     * The pitch to play the sound at.
     *
     * @return the pitch, (0 to 2)
     */
    public float getPitch()
    {
        return pitch;
    }

    /**
     * Change whether the audio component should loop or not
     *
     * @param val true if the source should loop
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data callback method
     */
    public void setLoop(boolean val)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        loop = val;
        sendParamUpdate();
    }

    /**
     * Set the pitch to play the sound at. This is a multiplier of the base
     * time that the sound would normally play in. A value of 0.5 says the
     * sound takes twice as long to play (and thus drops everything by an
     * octave).
     *
     * @param val The pitch multiplier to now use (0 to 2)
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data callback method
     */
    public void setPitch(float val)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        pitch = val;
        sendParamUpdate();
    }

    /**
     * Get a bufferId for a given seq and context.
     *
     * @param seq The sequence number
     * @return The bufferId.
     */
    public int getBufferId(AL al, int seq)
    {
        // OpenAL Docs say all contexts can share buffers, so reuse.
        if (bufferId != -1)
            return bufferId;
        else
        {
            int[] buffer = new int[1];

            al.alGenBuffers(1, buffer, 0);
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

                return -1;
            }

            al.alBufferData(buffer[0], format, data, data.limit(), frequency);
            bufferId = buffer[0];

            return bufferId;
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Add a listener for audio change updates.
     *
     * @param l The listener instance to add
     */
    public void addUpdateListener(AudioUpdateListener l)
    {
        if(numListeners == listeners.length)
        {
            int old_size = listeners.length;
            int new_size = old_size + LISTENER_INC;

            AudioUpdateListener[] tmp =
                new AudioUpdateListener[new_size];

            System.arraycopy(listeners, 0, tmp, 0, old_size);

            listeners = tmp;
        }

        listeners[numListeners++] = l;
    }

    /**
     * Remove a listener for audio change updates.
     *
     * @param l The listener instance to add
     */
    public void removeUpdateListener(AudioUpdateListener l)
    {
        if(numListeners == 1)
        {
            if(listeners[0] == l)
            {
                listeners[0] = null;
                numListeners--;
            }
        }
        else
        {
            // run along the array to find the matching instance
            for(int i = 0; i < numListeners; i++)
            {
                if(listeners[i] == l)
                {
                    System.arraycopy(listeners,
                                     i + 1,
                                     listeners,
                                     i,
                                     numListeners - i - 1);
                    break;
                }
            }
        }
    }

    /**
     * Clear local data stored in this node.  Only data needed for
     * OpenGL calls will be retained;
     */
    public abstract void clearLocalData();

    /**
     * Send off a audio update event.
     *
     * @param seq The sample seq.  Seq 0 means complete file.  Streamed sources start at 1
     */
    protected void sendAudioUpdate(int seq)
    {
        for(int i = 0; i < numListeners; i++)
        {
            try
            {
                listeners[i].audioUpdated(format, frequency, seq);
            }
            catch(Exception e)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                Locale lcl = intl_mgr.getFoundLocale();
                String msg_pattern = intl_mgr.getString(AUDIO_UPDATE_PROP);

                String cls_name = listeners[i] == null ?
                    "null" : listeners[i].getClass().getName();

                Object[] msg_args = { cls_name, e.getMessage() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);

                System.out.println(msg);
                e.printStackTrace();
            }
        }
    }

    /**
     * Send off a audio update event.
     */
    protected void sendParamUpdate()
    {
        for(int i = 0; i < numListeners; i++)
        {
            try
            {
                listeners[i].paramsUpdated(loop, pitch);
            }
            catch(Exception e)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                Locale lcl = intl_mgr.getFoundLocale();
                String msg_pattern = intl_mgr.getString(PARAM_UPDATE_PROP);

                String cls_name = listeners[i] == null ?
                    "null" : listeners[i].getClass().getName();

                Object[] msg_args = { cls_name, e.getMessage() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);

                System.out.println(msg);
                e.printStackTrace();
            }
        }
    }

}
