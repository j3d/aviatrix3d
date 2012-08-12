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

package org.j3d.aviatrix3d.iutil;

// External imports

// Local imports
// None

/**
 * Internal interface used to communicate update notifications for the source
 * data used for audio.
 * <p>
 * Non Streaming usage of this case will just send an audioUpdate message with
 * a sequence of 0.  Only complete updates to this sound will be allowed after
 * that.
 * <p>
 * Streaming usage will start with a sequence of 1 and continue to feed
 * samples.  The source is expected to keep the Sound feed with data.  Streamed
 * updates must be of the same format, frequency and cannot be looped.
 * <p>
 * Streaming updates are not currently supported.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public interface AudioUpdateListener
{
    /**
     * Notification that the audio has updated a section of the sample.
     * This is generic for all audio sources correctly.
     *
     * @param format The format of the samples
     * @param frequency The frequency of the samples
     * @param seq The sample seq.  Seq 0 means complete file.
     *            Streamed sources start at 1
     */
    public void audioUpdated(int format, int frequency, int seq);

    /**
     * Notification that the audio's parameters have changed.
     *
     * @param loop Whether to loop this sample
     * @param pitch The pitch to play at.
     */
    public void paramsUpdated(boolean loop, float pitch);
}
