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
// None

// Local imports
// None

import com.jogamp.openal.AL;

/**
 * Marker interface representing a class that provides source data for a
 * {@link Sound} object.
 * <p>
 *
 * The source can come from many different places, such as external data
 * images, streams etc), or dynamically generated (multipass rendering). All
 * texture objects require a source of data, and a single source can be added
 * to multiple texture objects.
 * <p>
 *
 * It is not intended that this interface be used directly as there is not much
 * directly usable information. Derived versions of this interface will provide
 * more interesting and source-specific behaviour.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface AudioSource
{
    /** Specifies the source is a mono channel, 8 bit */
    public static final int FORMAT_MONO8 = 1;

    /** Specifies the source is a mono channel, 16 bit */
    public static final int FORMAT_MONO16 = 2;

    /** Specifies the source is a mono channel, 8 bit */
    public static final int FORMAT_SETERO8 = 3;

    /** Specifies the source is a mono channel, 16 bit */
    public static final int FORMAT_STEREO16 = 4;

    /**
     * Get the format of this sound source.
     *
     * @return the format
     */
    public int getFormat();

    /**
     * Get the frequency of this sound source.
     *
     * @return the format
     */
    public int getFrequency();

    /**
     * Does this source loop.
     *
     * @return the format
     */
    public boolean getLoop();

    /**
     * The pitch to play the sound at.
     *
     * @return the pitch, (0 to 2)
     */
    public float getPitch();

    /**
     * Get a bufferId for a given seq and context.  Returns
     * -1 if the buffer has not been created.  When created
     * a AudioUpdateListener event will be issued.
     *
     * @param seq The sequence number
     * @return The bufferId.
     */
    public int getBufferId(AL al, int seq);
}
