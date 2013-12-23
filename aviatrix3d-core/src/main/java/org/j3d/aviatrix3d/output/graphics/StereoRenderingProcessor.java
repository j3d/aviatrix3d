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

package org.j3d.aviatrix3d.output.graphics;

// External imports
// None

// Local imports
// None

/**
 * Internal interface representing a renderer with stereo support.
 * <p>
 *
 * Provides some convenient abstraction methods. The camera model for this
 * is based on the <i>parallel axis asymmetric frustum perspective projection</i>
 * described by Paul Bourke at:
 * <a href="http://paulbourke.net/miscellaneous/stereographics/stereorender/">http://paulbourke.net/miscellaneous/stereographics/stereorender/</a>
 * <p>
 * This image, shamelessly copied from that page will help describe the
 * terminology and definition of what each value does:
 * <img src="doc-files/stereo_camera_details.gif"/>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface StereoRenderingProcessor extends RenderingProcessor
{
    /**
     * Check to see whether this surface supports stereo rendering. As this is
     * not known until after initialisation, this method will return false
     * until it can determine whether or not stereo is available.
     *
     * @return true Stereo support is currently available
     */
    public boolean isStereoAvailable();

    /**
     * Set the eye separation value when rendering stereo, defined as the
     * distance from the center axis to one eye. The default value is 0.33 for
     * most applications. The absolute value of the separation is always used.
     *
     * @param sep The amount of eye separation
     */
    public void setStereoEyeSeparation(float sep);

    /**
     * Get the current eye separation value, defined as the distance from the
     * center axis to one eye. If we are in no-stereo mode then this will
     * return zero.
     *
     * @return sep The amount of eye separation
     */
    public float getStereoEyeSeparation();

    /**
     * Set the angular aperature of the camera. Measurement is in degrees
     * and is in the horizontal plane relative to the camera. By default,
     * this angle is 45 degrees.
     *
     * @param angle The angle in degress
     */
    public void setCameraAperatureAngle(float angle);

    /**
     * Get the current camera aperature angle.
     *
     * @return The aperature angle, in degrees
     */
    public float getCameraAperatureAngle();

    /**
     * Set the focal length to be used to calculate the zero parallax
     * depth. The value must always be positive to indicate the focal length
     * is in front of the camera. By default, this is set to the near clip
     * plane distance if the provided value is zero.
     *
     * @param length a positive focal length value or zero to use the
     *    near clip plane as the focal distance
     * @throws IllegalArgumentException focal length was negative
     */
    public void setCameraFocalLength(float length);

    /**
     * Get the current camera focal length. Returns a positive value, or
     * zero if this is using the near clip plane for the focal length.
     *
     * @return a zero or positive number
     */
    public float getCameraFocalLength();
}
