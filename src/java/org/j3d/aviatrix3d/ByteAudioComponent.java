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
import java.awt.image.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

// Local imports
// None

/**
 * A Audio component that uses raw byte data to be interpreted by the
 * format IDs passed in.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class ByteAudioComponent extends AudioComponent
{
    /**
     * Constructs a sound with default values.
     */
    public ByteAudioComponent()
    {
        this(0, 0, false, (ByteBuffer) null);
    }

    /**
     * Constructs a ByteAudioComponent using the specified format.  This is the
     * most effecient route to pass data in.
     *
     * @param format The audio format.  FORMAT_MONO8,FORMAT_MONO16,FORMAT_STEREO8,FORMAT_STEREO16
     * @param frequency The frequency of the samples
     * @param loop True if this sample should be looped
     * @param srcData The data to use
     */
    public ByteAudioComponent(int format,
                              int frequency,
                              boolean loop,
                              ByteBuffer srcData)
    {
        this.format = format;
        this.frequency = frequency;
        this.loop = loop;
        data = srcData;
    }

    /**
     * Constructs a ByteAudioComponent using the specified format.
     *
     * @param format The audio format.  FORMAT_MONO8,FORMAT_MONO16,FORMAT_STEREO8,FORMAT_STEREO16
     * @param frequency The frequency of the samples.
     * @param loop True if this sample should be looped
     * @param srcData The data to use as a byte array
     */
    public ByteAudioComponent(int format,
                              int frequency,
                              boolean loop,
                              byte[] srcData)
    {
        this.format = format;
        this.frequency = frequency;
        this.loop = loop;
        data = ByteBuffer.allocateDirect(srcData.length);
        data.put(srcData);
    }

    /**
     * Clear the data backing for this Component.
     */
    public void clearLocalData()
    {
        // TODO: Can this clear the byte buffer?
    }

    /**
     * Set the data for this component.
     *
     * @param format The audio format.  FORMAT_MONO8,FORMAT_MONO16,FORMAT_STEREO8,FORMAT_STEREO16
     * @param frequency The frequency of the samples.
     * @param srcData The data.
     * @param seq The seq being updated.  0 for non streaming data.
     */
    public void setData(int format, int frequency, byte[] srcData, int seq)
    {
        this.format = format;
        this.frequency = frequency;
        this.loop = loop;
        data = ByteBuffer.allocateDirect(srcData.length);
        data.put(srcData);

        sendAudioUpdate(seq);
    }

    /**
     * Set the data for this component.
     *
     * @param format The audio format.  FORMAT_MONO8,FORMAT_MONO16,FORMAT_STEREO8,FORMAT_STEREO16
     * @param frequency The frequency of the samples.
     * @param srcData The data.
     * @param seq The seq being updated.  0 for non streaming data.
     */
    public void setData(int format, int frequency, ByteBuffer srcData, int seq)
    {
        this.format = format;
        this.frequency = frequency;
        this.loop = loop;
        data = srcData;

        sendAudioUpdate(seq);
    }
}
