/*****************************************************************************
 *                     j3d.org Copyright (c) 2012
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
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

import org.j3d.aviatrix3d.pipeline.graphics.GraphicsEnvironmentData;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

/**
 * Handles the rendering for a single output device, generating stereo by using
 * alternate frame renders to render left and right views to a single buffer.
 * <p>
 * The code expects that everything is set up before each call of the display()
 * callback. It does not handle any recursive rendering requests as that is
 * assumed to have been sorted out before calling this renderer.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>unknownProjTypeMsg: Error message when an unknown projection type makes
 *     its way through the rendering pipeline</li>
 * <li>negFocalLengthMsg: Error message when setting a focal distance that is
 *     a negative value</li>
 * <li>invalidAperatureAngleMsg: Error message when setting a camera aperature
 *     angle that is out of spec (in degrees)</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 3.26 $
 */
public abstract class BaseStereoProcessor extends BaseRenderingProcessor
    implements StereoRenderingProcessor
{
    /** Viewport projection type requested is unsupported */
    private static final String UNKNOWN_PROJECTION_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseStereoProcessor.unknownProjTypeMsg";

    /** Negative focal length given */
    private static final String NEG_FOCAL_LENGTH_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseStereoProcessor.negFocalLengthMsg";

    /** Camera aperature is not 0 < angle < 180 */
    private static final String INVALID_APERATURE_PROP =
        "org.j3d.aviatrix3d.output.graphics.BaseStereoProcessor.invalidAperatureAngleMsg";

    /** The current eye separation to use */
    protected float eyeSeparation;

    /**
     * Flag indicating if the buffers are avialable for implementing
     * stereo as required by this implementation.
     */
    protected boolean stereoAvailability;

    /** Camera aperature angle in radians. Default value of 45deg */
    protected double cameraAperature;

    /** Camera focal distance. Default value of zero means use near clip */
    protected float focalDistance;

    /**
     * Construct handler for rendering objects to the main screen.
     *
     * @param context The context that this processor is working on
     * @param owner The owning device of this processor
     */
    public BaseStereoProcessor(GLContext context, GraphicsOutputDevice owner)
    {
        super(context, owner);

        eyeSeparation = 0.005f;
        stereoAvailability = false;

        cameraAperature = Math.toRadians(45);
        focalDistance = 0;
    }

    //---------------------------------------------------------------
    // Methods defined by BaseRenderingProcesor
    //---------------------------------------------------------------

    /**
     * Called by the drawable when the surface resizes itself. Used to
     * reset the viewport dimensions.
     */
    @Override
    public void init()
    {
        super.init();

        GL gl = localContext.getGL();

        byte[] params = new byte[1];
        gl.glGetBooleanv(GL2.GL_STEREO, params, 0);

        stereoAvailability = (params[0] == GL.GL_TRUE);
    }

    //---------------------------------------------------------------
    // Methods defined by StereoRenderingProcessor
    //---------------------------------------------------------------

    /**
     * Check to see whether this surface supports stereo rendering. As this is
     * not known until after initialisation, this method will return false
     * until it can determine whether or not stereo is available.
     *
     * @return true Stereo support is currently available
     */
    @Override
    public boolean isStereoAvailable()
    {
        return stereoAvailability;
    }

    /**
     * Set the eye separation value when rendering stereo. The default value is
     * 0.33 for most applications. The absolute value of the separation is
     * always used. Ignored for this implementation.
     *
     * @param sep The amount of eye separation
     */
    @Override
    public void setStereoEyeSeparation(float sep)
    {
        eyeSeparation = (sep < 0) ? -sep : sep;
    }

    /**
     * Get the current eye separation value - always returns 0.
     *
     * @return sep The amount of eye separation
     */
    @Override
    public float getStereoEyeSeparation()
    {
        return eyeSeparation;
    }

    /**
     * Set the angular aperature of the camera. Measurement is in degrees
     * and is in the horizontal plane relative to the camera. By default,
     * this angle is 45 degrees.
     *
     * @param angle The angle in degress
     */
    @Override
    public void setCameraAperatureAngle(float angle)
    {
        if((angle <= 0) || (angle >= 180))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_APERATURE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();
            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(angle) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        cameraAperature = Math.toRadians(angle);
    }

    /**
     * Get the current camera aperature angle.
     *
     * @return The aperature angle, in degrees
     */
    @Override
    public float getCameraAperatureAngle()
    {
        return (float)Math.toDegrees(cameraAperature);
    }

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
    @Override
    public void setCameraFocalLength(float length)
    {
        if(length < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NEG_FOCAL_LENGTH_PROP);

            Locale lcl = intl_mgr.getFoundLocale();
            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(length) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        focalDistance = length;
    }

    /**
     * Get the current camera focal length. Returns a positive value, or
     * zero if this is using the near clip plane for the focal length.
     *
     * @return a zero or positive number
     */
    @Override
    public float getCameraFocalLength()
    {
        return focalDistance;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Setup the view environment data for drawing now.
     *
     * @param gl The gl context to draw with
     * @param data The view environment information to setup
     * @param left true if this is the left eye
     */
    protected void preLayerEnvironmentDraw(GL2 gl,
                                           GraphicsEnvironmentData data,
                                           boolean left)
    {
        if(!data.useStereo)
        {
            super.preLayerEnvironmentDraw(gl, data);
        }
        else
        {
            if(data.effectsProcessor != null)
                data.effectsProcessor.preDraw(gl, data.userData);

            if(data.background != null)
            {
                gl.glDepthMask(false);
                gl.glDisable(GL.GL_BLEND);
                gl.glDisable(GL.GL_DEPTH_TEST);

                // If it is colour only, then don't bother with rest of the
                // the projection setup. It will be wasted.
                if(((BackgroundRenderable)data.background).is2D())
                {
                    gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

                    data.background.render(gl);
                    data.background.postRender(gl);
                }
                else
                {
                    gl.glMatrixMode(GL2.GL_PROJECTION);
                    gl.glPushMatrix();
                    gl.glLoadIdentity();

                    gl.glFrustum(data.backgroundFrustum[0],
                                 data.backgroundFrustum[1],
                                 data.backgroundFrustum[2],
                                 data.backgroundFrustum[3],
                                 data.backgroundFrustum[4],
                                 data.backgroundFrustum[5]);

                    gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

                    gl.glMatrixMode(GL2.GL_MODELVIEW);
                    gl.glPushMatrix();

                    gl.glLoadMatrixd(data.backgroundTransform, 0);

                    data.background.render(gl);
                    data.background.postRender(gl);

                    gl.glPopMatrix();

                    gl.glMatrixMode(GL2.GL_PROJECTION);
                    gl.glPopMatrix();
                    gl.glMatrixMode(GL2.GL_MODELVIEW);
                }
            }

            // Always want to clean up here and make sure that we have the basics
            // enabled for the start of a rendering run. So, even though we turned
            // it off inside the if statement above, we turn it back on outside
            // here because the previous execution may have left everything in a
            // bad state - particularly if someone was using a preprocessor and
            // didn't clean up after themselves properly.
            gl.glDepthMask(true);
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glEnable(GL.GL_BLEND);

            renderViewpoint(gl, data, left);

            if(data.fog != null)
            {
                gl.glEnable(GL2.GL_FOG);
                data.fog.render(gl);
            }
        }
    }

    /**
     * Render the viewpoint setup.
     *
     * @param gl The gl context to draw with
     *     * @param data The view environment information to setup
     * @param left true if this is the left eye
     */
    private void renderViewpoint(GL2 gl,
                                 GraphicsEnvironmentData data,
                                 boolean left)

    {
        updateProjectionMatrix(gl, data, left);

        gl.glLoadIdentity();

/*
        if(left)
            gl.glTranslated(-eyeSeparation, 0, 0);
        else
            gl.glTranslated(eyeSeparation, 0, 0);

*/
        data.viewpoint.render(gl);

        gl.glMultMatrixd(data.cameraTransform, 0);
    }

    /**
     * Update the projection matrix.
     *
     * @param gl The gl context to draw with
     * @param data Environmental information to help the rendering
     * @param isleftEye true if this is the left eye
     */
    private void updateProjectionMatrix(GL2 gl,
                                        GraphicsEnvironmentData data,
                                        boolean isleftEye)
    {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        switch(data.viewProjectionType)
        {
            case ViewEnvironmentCullable.PERSPECTIVE_PROJECTION:
                // Code based on the asymetric frustum code here:
                // http://paulbourke.net/miscellaneous/stereographics/stereorender/

                double camera_near_clip = data.viewFrustum[4];
                double real_focal_dist =
                    (focalDistance == 0) ? camera_near_clip : focalDistance;
                double aspect_ratio = (float)data.viewport[GraphicsEnvironmentData.VIEW_WIDTH] /
                                             data.viewport[GraphicsEnvironmentData.VIEW_HEIGHT];
                double width_2 = camera_near_clip *
                                 Math.tan(cameraAperature * 0.5);

                double top = width_2;
                double bottom = -width_2;
                double left = 0;
                double right = 0;

                if(isleftEye)
                {
                    left = -aspect_ratio * width_2 -
                           0.5 * eyeSeparation *
                           camera_near_clip / real_focal_dist;
                    right = aspect_ratio * width_2 -
                            0.5 * eyeSeparation *
                            camera_near_clip / real_focal_dist;

                }
                else
                {
                    left = -aspect_ratio * width_2 +
                           0.5 * eyeSeparation *
                           camera_near_clip / real_focal_dist;
                    right = aspect_ratio * width_2 +
                            0.5 * eyeSeparation *
                            camera_near_clip / real_focal_dist;
                }

                gl.glFrustum(left,
                             right,
                             bottom,
                             top,
                             data.viewFrustum[4],
                             data.viewFrustum[5]);
                break;

            case ViewEnvironmentCullable.ORTHOGRAPHIC_PROJECTION:
                // TODO:
                // EEK! Orthographic stereo projection. Ah.... doesn't sound
                // right, but not sure what to do about it here. Should we
                // change this and try to use the toe-in method for this style
                // or close our eyes and hope?

                // NOTE:
                // should this not be 0, 0 for the start, and perhaps
                // using the glViewport x and y settings? - JC

                gl.glOrtho(0,
                           data.viewport[GraphicsEnvironmentData.VIEW_WIDTH],
                           0,
                           data.viewport[GraphicsEnvironmentData.VIEW_HEIGHT],
                           data.viewFrustum[4],
                           data.viewFrustum[5]);
                break;

            default:
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(UNKNOWN_PROJECTION_PROP);

                Locale lcl = intl_mgr.getFoundLocale();
                NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                Object[] msg_args = { new Integer(data.viewProjectionType) };
                Format[] fmts = { n_fmt };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                msg_fmt.setFormats(fmts);
                String msg = msg_fmt.format(msg_args);

                errorReporter.warningReport(msg, null);
        }

        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }
}
