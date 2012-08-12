/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
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
import java.text.NumberFormat;
import java.util.Locale;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector4f;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.ViewEnvironmentCullable;

/**
 * Representation of the physical environment setup used to connect
 * a virtual Viewpoint object to the real one that is rendered on a drawable
 * surface.
 * </p>
 * <p>
 * Most of the properties of this class are changed during the app update
 * observer callback cycle. Anything that is generated will be done using the
 * currently set data at that time.
 * </p>
 * <p>
 * A view environment cannot be directly created. You must fetch the
 * environment from it's parent {@link Scene} instance.
 * </p>
 * <p>
 * <b>Frustum Generation</b>
 * </p>
 * <p>
 * The view frustum is generated depends on the aspect ratio. If the user
 * sets an explicit aspect ratio, this is used in preference. If no aspect
 * ratio is set (or the value set to <= 0) then the dimensions of the
 * viewport that this is contained within are used to automatically calculate
 * an aspect ratio.
 * </p>
 * <p>
 * The aspect ratio is not set by default.
 * </p>
 *
 * <b>Stereo support</b>
 * </p>
 * <p>
 * If the output device is capable of supporting stereo rendering, this class
 * can be used to enable it. Since the stereo flags are supplied here, that
 * allows the end user to control which layer(s) should be rendered in stereo
 * and which should not. For example, a HUD may want to have a text overlay
 * that has no depth applied, rendered over the top of the main layer, which
 * has stereo applied. One environment has the flag set, the other does not.
 * <p>
 * By default, stereo is disabled.
 * </p>
 *
 * <p>
 * <b>Field of View and aspect ratio</b>
 * </p>
 * <p>
 * The field of view calculation defines the viewing angle that is used
 * in the Y axis - definition the minimum and maximum Y extents for the view
 * frustum. If an explicit aspect ratio is set then the X extents are
 * calculated using that, otherwise the dimensions of the viewport are
 * used to calculate an aspect ratio, and finally the X extents.
 * </p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidStencilFunctionMsg: Error message when the given function type
 *     does not match one of the available types.</li>
 * <li>invalidStencilOpMsg: Error message when the given operation type
 *     does not match one of the available types.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.35 $
 */
public class ViewEnvironment extends SceneGraphObject
    implements ViewEnvironmentCullable
{
    /** Message when the projection type provided is invalid */
    private static final String INVALID_PROJECTION_PROP =
        "org.j3d.aviatrix3d.ViewEnvironment.invalidProjectionMsg";

    /** Field of view is out of the range [0, 180] */
    private static final String INVALID_FOV_PROP =
        "org.j3d.aviatrix3d.ViewEnvironment.invalidFOVMsg";

    /** Invalid clip distance. It is negative */
    private static final String NEGATIVE_CLIP_PROP =
        "org.j3d.aviatrix3d.ViewEnvironment.negativeClipMsg";

    /** The far clip distance is less than the near clip distance */
    private static final String INVERT_CLIP_PROP =
        "org.j3d.aviatrix3d.ViewEnvironment.invertedClipMsg";

    /** The X value in the ortho params is >= the right position */
    private static final String INVERT_ORTHO_X_PROP =
        "org.j3d.aviatrix3d.ViewEnvironment.invertedOrthoXMsg";

    /** The Y value in the ortho params is >= the bottom position */
    private static final String INVERT_ORTHO_Y_PROP =
        "org.j3d.aviatrix3d.ViewEnvironment.invertedOrthoYMsg";


    /** The projection type is perspective mode */
    public static final int PERSPECTIVE_PROJECTION =
        ViewEnvironmentCullable.PERSPECTIVE_PROJECTION;

    /** The projection type is perspective mode */
    public static final int ORTHOGRAPHIC_PROJECTION =
        ViewEnvironmentCullable.ORTHOGRAPHIC_PROJECTION;

    /** The projection type is an infinite perspective matrix */
    public static final int INFINITE_PROJECTION =
        ViewEnvironmentCullable.INFINITE_PROJECTION;

    /** The projection type is a custom user-provided matrix */
    public static final int CUSTOM_PROJECTION =
        ViewEnvironmentCullable.CUSTOM_PROJECTION;

    /** Index into the viewport size array for the X position */
    public static final int VIEW_X = ViewEnvironmentCullable.VIEW_X;

    /** Index into the viewport size array for the Y position */
    public static final int VIEW_Y = ViewEnvironmentCullable.VIEW_Y;

    /** Index into the viewport size array for the width */
    public static final int VIEW_WIDTH = ViewEnvironmentCullable.VIEW_WIDTH;

    /** Index into the viewport size array for the height */
    public static final int VIEW_HEIGHT = ViewEnvironmentCullable.VIEW_HEIGHT;

    /** The stereo setting for this environment */
    private boolean useStereo;

    /** The perspective/orthographic setting for this environment */
    private int projectionType;

    /** The field of view used */
    private double fov;

    /** Current near clip plane setting */
    private double nearClip;

    /** Current far clip plane setting */
    private double farClip;

    /** The aspectRatio ratio */
    private double aspectRatio;

    /** If defined, the explicit viewport to use */
    private int[] viewportSize;

    /** If defined, the explicit scissor region to use */
    private int[] scissorSize;

    /** The currently calculated view frustum */
    private double[] viewFrustum;

    /** The ortho left plane coordinate */
    private double orthoLeft;

    /** The ortho right plane coordinate */
    private double orthoRight;

    /** The ortho bottom plane coordinate */
    private double orthoBottom;

    /** The ortho top plane coordinate */
    private double orthoTop;

    /** The last calculated, or manually set projection matrix.  */
    private float[] projectionMatrix;

    /**
     * Flag indicating that there have been some changes and the frustum needs
     * to be recalculated the next time it is asked for.
     */
    private boolean frustumChanged;

    /**
     * Create a default instance of this class with stereo false,
     * perspective projection and field of view set to 45 degrees.
     */
    ViewEnvironment()
    {
        useStereo = false;
        projectionType = PERSPECTIVE_PROJECTION;
        fov = 45;

        nearClip = 0.01;
        farClip = 1000;
        viewportSize = new int[4];
        scissorSize = new int[4];
        viewFrustum = new double[6];
        projectionMatrix = new float[16];
        frustumChanged = true;

        orthoLeft = -1;
        orthoRight = 1;
        orthoBottom = -1;
        orthoTop = 1;
    }

    //----------------------------------------------------------
    // Methods defined by ViewEnvironmentCullable
    //----------------------------------------------------------

    /**
     * Check to see if stereo has been enabled for this environment.
     *
     * @return true if stereo rendering is to be used
     */
    public boolean isStereoEnabled()
    {
        return useStereo;
    }

    /**
     * Check to see if stereo has been enabled for this environment.
     *
     * @return true if stereo rendering is to be used
     */
    public int getProjectionType()
    {
        return projectionType;
    }

    /**
     * Get the currently set dimensions of the viewport. This is automatically
     * pushed down from the parent Viewport instance. The values are described
     * as<br/>
     * viewport[0] = x<br/>
     * viewport[1] = y<br/>
     * viewport[2] = width<br/>
     * viewport[3] = height<br/>
     *
     * @return The current viewport size
     */
    public int[] getViewportDimensions()
    {
        return viewportSize;
    }

    /**
     * Get the currently set dimensions of the scissor area. The values are
     * described as<br/>
     * viewport[0] = x<br/>
     * viewport[1] = y<br/>
     * viewport[2] = width<br/>
     * viewport[3] = height<br/>
     *
     * @return The current viewport size
     */
    public int[] getScissorDimensions()
    {
        return scissorSize;
    }

    /**
     * Get the frustum based on the projectionType.  Perspective used
     * the current view setup of FoV, near and far clipping distances and
     * the aspectRatio ratio, generate the 6 parameters that describe a view
     * frustum. These parameters are what could be used as arguments to the
     * glFrustum call. The parameter order for perspective is:
     * [x min, x max, y min, y max, z near, z far]
     * Orthographic uses the parameters specified in the setOrthoParams.
     * The parameter order for orthographic:
     * [left, right, bottom, top, near, far]
     *
     * @param frustum An array at least 6 in length for the values generated
     */
    public void getViewFrustum(double[] frustum)
    {
        generateViewFrustum(frustum);
    }

    /**
     * Get the currently set field of view. The field of view is specified in
     * degrees.
     *
     * @return A value 0 <= x <= 180;
     */
    public double getFieldOfView()
    {
        return fov;
    }

    /**
     * Get the projection matrix that is generated for this environment.
     * The projection matrix may be customised, or generated from the other
     * data that is available from this class. The appropriate behaviour on the
     * part of the caller will depend on the projection type flag.
     *
     * @param matrix An array of length 16 to put the projection matrix in to
     *    The format is row-major
     */
    public void getProjectionMatrix(float[] matrix)
    {
        updateProjMatrix();

        matrix[0] = projectionMatrix[0];
        matrix[1] = projectionMatrix[1];
        matrix[2] = projectionMatrix[2];
        matrix[3] = projectionMatrix[3];
        matrix[4] = projectionMatrix[4];
        matrix[5] = projectionMatrix[5];
        matrix[6] = projectionMatrix[6];
        matrix[7] = projectionMatrix[7];
        matrix[8] = projectionMatrix[8];
        matrix[9] = projectionMatrix[9];
        matrix[10] = projectionMatrix[10];
        matrix[11] = projectionMatrix[11];
        matrix[12] = projectionMatrix[12];
        matrix[13] = projectionMatrix[13];
        matrix[14] = projectionMatrix[14];
        matrix[15] = projectionMatrix[15];
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the stereo flag used for this environment.
     *
     * @param stereo True if stereo should be rendered
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setStereoEnabled(boolean stereo)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        useStereo = stereo;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Set the perspective projection flag used for this environment.
     *
     * @param type One of ORTHOGRAPHIC_PROJECTION or PERSPECTIVE_PROJECTION
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     * @throws IllegalArgumentException The type is not valid
     */
    public void setProjectionType(int type)
        throws IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        if(type < PERSPECTIVE_PROJECTION || type > CUSTOM_PROJECTION)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INVALID_PROJECTION_PROP);

            throw new IllegalArgumentException(msg);
        }

        projectionType = type;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Set the field of view to be used. The value supplied must be in degrees.
     *
     * @param angle The angle in degress
     * @throws IllegalArgumentException The angle is less than or equal to zero
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setFieldOfView(double angle)
        throws IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        if((angle <= 0) || (angle > 180))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_FOV_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Double(angle) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        fov = angle;
        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Set aspect ratio, which is the ratio of window Width / Height.
     * An aspect ratio of <= 0 will be calculated from the current
     * window or viewport dimensions.
     *
     * @param aspect The new aspectRatio ratio.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setAspectRatio(double aspect)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        aspectRatio = aspect;
        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Get the currently set aspect ratio.  Width / Height.
     *
     * @return The aspect ratio
     */
    public double getAspectRatio()
    {
        return aspectRatio;
    }

    /**
     * Set the near clipping distance to be used by the application.
     *
     * @param d The distance to set the near clip plane to
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setNearClipDistance(double d)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        if((projectionType == PERSPECTIVE_PROJECTION) && d < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NEGATIVE_CLIP_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Double(d) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(d >= farClip)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVERT_CLIP_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Double(d), new Double(farClip) };
            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        nearClip = d;
        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Get the current setting of the far clip plane.
     *
     * @return The current value, which is less than the far plane
     */
    public double getNearClipDistance()
    {
        return nearClip;
    }

    /**
     * Set the near clipping distance to be used by the application.
     *
     * @param near The distance to set the near clip plane
     * @param far The distance to the far clip plane
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setClipDistance(double near, double far)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        if((projectionType == PERSPECTIVE_PROJECTION) && near < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NEGATIVE_CLIP_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Double(near) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if((projectionType == PERSPECTIVE_PROJECTION) && far < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NEGATIVE_CLIP_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Double(far) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(near >= far)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVERT_CLIP_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Double(near), new Double(far) };
            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        nearClip = near;
        farClip = far;
        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Set the far clipping distance to be used by the application.
     *
     * @param d The distance to set the near clip plane to
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setFarClipDistance(double d)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        if((projectionType == PERSPECTIVE_PROJECTION) && d < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NEGATIVE_CLIP_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Double(d) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(d <= nearClip)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVERT_CLIP_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Double(nearClip), new Double(d) };
            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        farClip = d;
        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Set a custom projection matrix for this view environment. This will
     * automatically set the projection type to {@link #CUSTOM_PROJECTION}.
     *
     * @param matrix A length 16 array of the 4x4 matrix, in row major form
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setProjectionMatrix(float[] matrix)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        projectionType = CUSTOM_PROJECTION;
        projectionMatrix[0] = matrix[0];
        projectionMatrix[1] = matrix[1];
        projectionMatrix[2] = matrix[2];
        projectionMatrix[3] = matrix[3];
        projectionMatrix[4] = matrix[4];
        projectionMatrix[5] = matrix[5];
        projectionMatrix[6] = matrix[6];
        projectionMatrix[7] = matrix[7];
        projectionMatrix[8] = matrix[8];
        projectionMatrix[9] = matrix[9];
        projectionMatrix[10] = matrix[10];
        projectionMatrix[11] = matrix[11];
        projectionMatrix[12] = matrix[12];
        projectionMatrix[13] = matrix[13];
        projectionMatrix[14] = matrix[14];
        projectionMatrix[15] = matrix[15];

        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Get the current setting of the far clip plane.
     *
     * @return The current value, which is greater than the near plane
     */
    public double getFarClipDistance()
    {
        return farClip;
    }

    /**
     * Set the Orthographic view parameters.
     *
     * @param left The left plane coordinate
     * @param right The right plane coordinate
     * @param bottom The bottom plane coordinate
     * @param top The top plane coordinate
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setOrthoParams(double left,
                               double right,
                               double bottom,
                               double top)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(getAppUpdateWriteTimingMessage());

        if(left >= right)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVERT_ORTHO_X_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Double(left), new Double(right) };
            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(top <= bottom)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVERT_ORTHO_Y_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Double(top), new Double(bottom) };
            Format[] fmts = { n_fmt, n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        orthoLeft = left;
        orthoRight = right;
        orthoBottom = bottom;
        orthoTop = top;

        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Generate a frustum based on the projectionType.  Perspective used
     * the current view setup of FoV, near and far clipping distances and
     * the aspectRatio ratio, generate the 6 parameters that describe a view
     * frustum. These parameters are what could be used as arguments to the
     * glFrustum call.
     * <p>
     * The parameter order for perspective is:<br />
     * [x min, x max, y min, y max, z near, z far]<br />
     * Orthographic uses the parameters specified in the setOrthoParams.
     * The parameter order for orthographic:<br />
     * [left, right, bottom, top, near, far]<br />
     * If a custom matrix is provided, this will generate all zeroes in the
     * returned array values.
     *
     * @param frustum An array at least 6 in length for the values generated
     */
    public void generateViewFrustum(double[] frustum)
    {
        if(frustumChanged)
            recalcFrustum();

        frustum[0] = viewFrustum[0];
        frustum[1] = viewFrustum[1];
        frustum[2] = viewFrustum[2];
        frustum[3] = viewFrustum[3];
        frustum[4] = viewFrustum[4];
        frustum[5] = viewFrustum[5];
    }

    /**
     * Convenience method to generate the 6 frustum planes from the current
     * projection information held. Note that these are local to the camera's
     * coordinate system and do not include the viewpoint transformation
     * matrix in the calculation.
     * <p>
     * Planes are in the order: left, right, top, bottom, near, far
     *
     * @param planes The planes to place the data in
     */
    public void generateViewFrustumPlanes(Vector4f[] planes)
    {
        updateProjMatrix();

        Matrix4f prjMatrix = new Matrix4f();
        prjMatrix.m00 = projectionMatrix[0];
        prjMatrix.m01 = projectionMatrix[1];
        prjMatrix.m02 = projectionMatrix[2];
        prjMatrix.m03 = projectionMatrix[3];
        prjMatrix.m10 = projectionMatrix[4];
        prjMatrix.m11 = projectionMatrix[5];
        prjMatrix.m12 = projectionMatrix[6];
        prjMatrix.m13 = projectionMatrix[7];
        prjMatrix.m20 = projectionMatrix[8];
        prjMatrix.m21 = projectionMatrix[9];
        prjMatrix.m22 = projectionMatrix[10];
        prjMatrix.m23 = projectionMatrix[11];
        prjMatrix.m30 = projectionMatrix[12];
        prjMatrix.m31 = projectionMatrix[13];
        prjMatrix.m32 = projectionMatrix[14];
        prjMatrix.m33 = projectionMatrix[15];

        float x, y, z, w;

        /* Extract the numbers for the LEFT plane */
        x = prjMatrix.m03 + prjMatrix.m00;
        y = prjMatrix.m13 + prjMatrix.m10;
        z = prjMatrix.m23 + prjMatrix.m20;
        w = prjMatrix.m33 + prjMatrix.m30;

        float t = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
        planes[0].x = x * t;
        planes[0].y = y * t;
        planes[0].z = z * t;
        planes[0].w = w * t;

        /* Extract the numbers for the RIGHT plane */
        x = prjMatrix.m03 - prjMatrix.m00;
        y = prjMatrix.m13 - prjMatrix.m10;
        z = prjMatrix.m23 - prjMatrix.m20;
        w = prjMatrix.m33 - prjMatrix.m30;

        /* Normalize the result */
        t = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
        planes[1].x = x * t;
        planes[1].y = y * t;
        planes[1].z = z * t;
        planes[1].w = w * t;

        /* Extract the TOP plane */
        x = prjMatrix.m03 - prjMatrix.m01;
        y = prjMatrix.m13 - prjMatrix.m11;
        z = prjMatrix.m23 - prjMatrix.m21;
        w = prjMatrix.m33 - prjMatrix.m31;

        t = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
        planes[2].x = x * t;
        planes[2].y = y * t;
        planes[2].z = z * t;
        planes[2].w = w * t;

        /* Extract the BOTTOM plane */
        x = prjMatrix.m03 + prjMatrix.m01;
        y = prjMatrix.m13 + prjMatrix.m11;
        z = prjMatrix.m23 + prjMatrix.m21;
        w = prjMatrix.m33 + prjMatrix.m31;

        t = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
        planes[3].x = x * t;
        planes[3].y = y * t;
        planes[3].z = z * t;
        planes[3].w = w * t;

        /* Extract the NEAR plane */
        x = prjMatrix.m03 + prjMatrix.m02;
        y = prjMatrix.m13 + prjMatrix.m12;
        z = prjMatrix.m23 + prjMatrix.m22;
        w = prjMatrix.m33 + prjMatrix.m32;

        t = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
        planes[4].x = x * t;
        planes[4].y = y * t;
        planes[4].z = z * t;
        planes[4].w = w * t;

        /* Extract the FAR plane */
        x = prjMatrix.m03 - prjMatrix.m02;
        y = prjMatrix.m13 - prjMatrix.m12;
        z = prjMatrix.m23 - prjMatrix.m22;
        w = prjMatrix.m33 - prjMatrix.m32;

        /////////////////////////////////////////////////////////////////
        // rem - temporary change to fix x3d earth culling issue
        //t = 1.0f / (float)Math.sqrt(x * x + y * y + z * z);
        t = (float)Math.sqrt(x * x + y * y + z * z);
        if(t == 0)
            t = 1/1.19209290E-07f;
        else
            t = 1/t;
        /////////////////////////////////////////////////////////////////

        planes[5].x = x * t;
        planes[5].y = y * t;
        planes[5].z = z * t;
        planes[5].w = w * t;
    }

    /**
     * Set the scissor area dimensions to reduce the amount of the parent
     * viewport that is rendered to. Dimesions are relative to the window
     * coordinates, not to the viewport. If width or height are set to zero
     * or negative the scissor is ignored and the entire viewport is
     * rendered. This is the default state.
     *
     * @param x The lower left x coordinate for the view
     * @param y The lower left y coordinate for the view
     * @param width The width of the viewport in pixels
     * @param height The height of the viewport in pixels
     */
    public void setScissorDimensions(int x, int y, int width, int height)
    {
        scissorSize[VIEW_X] = x;
        scissorSize[VIEW_Y] = y;
        scissorSize[VIEW_WIDTH] = width;
        scissorSize[VIEW_HEIGHT] = height;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Convert a pixel location to surface coordinates. This uses the current
     * view frustum calculation.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param position The converted position.  It must be preallocated.
     */
    public void getPixelLocationInSurface(float x, float y, Point3f position)
    {
        getPixelLocationInSurface(x, y, null, position);
    }

    /**
     * Convert a pixel location to surface coordinates. This uses the current
     * view frustum calculation and an optional amount of eye offset (which
     * is particularly useful for stereo calculations).
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param offset Any Eye offset amount needed for a canvas offset or null
     * @param position The converted position.  It must be preallocated.
     */
    public void getPixelLocationInSurface(float x,
                                          float y,
                                          Point3f offset,
                                          Point3f position)
    {
        if(viewportSize[VIEW_WIDTH] == 0)
            return;

        if(frustumChanged)
            recalcFrustum();

        if(offset != null)
        {
            double vmx1 = viewFrustum[0] - offset.x;
            double vmx2 = viewFrustum[1] - offset.x;
            double vmy1 = viewFrustum[2] - offset.y;
            double vmy2 = viewFrustum[3] - offset.y;
            double vmd1 = viewFrustum[4] - offset.z;

            position.x = (float)((vmx2 - vmx1) *
                           (x / viewportSize[VIEW_WIDTH] - 0.5f));
            position.y = (float)((vmy2 - vmy1) *
                           ((viewportSize[VIEW_HEIGHT] - y) /
                            viewportSize[VIEW_HEIGHT] - 0.5f));
            position.z = (float) -vmd1;
        }
        else
        {
            position.x = (float)((viewFrustum[1] - viewFrustum[0]) *
                           (x / viewportSize[VIEW_WIDTH] - 0.5f));
            position.y = (float)((viewFrustum[3] - viewFrustum[2]) *
                           ((viewportSize[VIEW_HEIGHT] - y) /
                            viewportSize[VIEW_HEIGHT] - 0.5f));
            position.z = (float) -viewFrustum[4];
        }
    }

    /**
     * Set the viewport dimensions from the parent viewport. These dimensions
     * are pushed down through the scene to the viewport.
     *
     * @param x The lower left x coordinate for the view
     * @param y The lower left y coordinate for the view
     * @param width The width of the viewport in pixels
     * @param height The height of the viewport in pixels
     */
    void setViewportDimensions(int x, int y, int width, int height)
    {
        viewportSize[VIEW_X] = x;
        viewportSize[VIEW_Y] = y;
        viewportSize[VIEW_WIDTH] = width;
        viewportSize[VIEW_HEIGHT] = height;
        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Internal convenience method to recalculate the view frustum when
     * needed. If a custom matrix is encountered, the view frustum is reset
     * to all zeroes.
     */
    private void recalcFrustum()
    {
        switch(projectionType)
        {
            case ORTHOGRAPHIC_PROJECTION:
                viewFrustum[0] = orthoLeft;
                viewFrustum[1] = orthoRight;
                viewFrustum[2] = orthoBottom;
                viewFrustum[3] = orthoTop;
                viewFrustum[4] = nearClip;
                viewFrustum[5] = farClip;
                break;

            // Infinite and perspective share the same plane setup
            case PERSPECTIVE_PROJECTION:
            case INFINITE_PROJECTION:
                double xmin = 0;
                double xmax = 0;
                double ymin = 0;
                double ymax = 0;

                ymax = nearClip * Math.tan(fov * Math.PI / 360.0);
                ymin = -ymax;

                if(aspectRatio <= 0)
                {
                    double ar = (double)viewportSize[VIEW_WIDTH] /
                                viewportSize[VIEW_HEIGHT];
                    xmin = ymin * ar;
                    xmax = ymax * ar;
                }
                else
                {
                    xmin = ymin * aspectRatio;
                    xmax = ymax * aspectRatio;
                }

                viewFrustum[0] = xmin;
                viewFrustum[1] = xmax;
                viewFrustum[2] = ymin;
                viewFrustum[3] = ymax;
                viewFrustum[4] = nearClip;
                viewFrustum[5] = farClip;

                break;

            case CUSTOM_PROJECTION:
                viewFrustum[0] = 0;
                viewFrustum[1] = 0;
                viewFrustum[2] = 0;
                viewFrustum[3] = 0;
                viewFrustum[4] = 0;
                viewFrustum[5] = 0;
        }

        frustumChanged = false;
    }

    /**
     * Recalculate the internal projection matrix
     */
    private void updateProjMatrix()
    {
        if(frustumChanged)
            recalcFrustum();

        float left = (float)viewFrustum[0];
        float right = (float)viewFrustum[1];
        float bottom = (float)viewFrustum[2];
        float top = (float)viewFrustum[3];
        float nearval = (float)viewFrustum[4];
        float farval = (float)viewFrustum[5];
        float x, y, z, w;
        float a, b, c, d;

        switch(projectionType)
        {
            case PERSPECTIVE_PROJECTION:
                x = (2.0f * nearval) / (right - left);
                y = (2.0f * nearval) / (top - bottom);
                a = (right + left) / (right - left);
                b = (top + bottom) / (top - bottom);
                c = -(farval + nearval) / ( farval - nearval);
                d = -(2.0f * farval * nearval) / (farval - nearval);

                //////////////////////////////////////////////
                // rem: clamping to the limit as farval approaches infinity
                // see: http://www.terathon.com/gdc07_lengyel.ppt
                if (d < -Float.MAX_VALUE)
                {
                    d = -2;
                }
                //////////////////////////////////////////////

                projectionMatrix[0] = x;
                projectionMatrix[1] = 0;
                projectionMatrix[2] = a;
                projectionMatrix[3] = 0;
                projectionMatrix[4] = 0;
                projectionMatrix[5] = y;
                projectionMatrix[6] = b;
                projectionMatrix[7] = 0;
                projectionMatrix[8] = 0;
                projectionMatrix[9] = 0;
                projectionMatrix[10] = c;
                projectionMatrix[11] = d;
                projectionMatrix[12] = 0;
                projectionMatrix[13] = 0;
                projectionMatrix[14] = -1;
                projectionMatrix[15] = 0;
                break;

            case ORTHOGRAPHIC_PROJECTION:
                x = 2.0f / (right - left);
                y = 2.0f / (top - bottom);
                z = -2.0f / (farval - nearval);
                a = -(right + left) / (right - left);
                b = -(top + bottom) / (top - bottom);
                c = -(farval + nearval) / (farval - nearval);

                projectionMatrix[0] = x;
                projectionMatrix[1] = 0;
                projectionMatrix[2] = 0;
                projectionMatrix[3] = a;
                projectionMatrix[4] = 0;
                projectionMatrix[5] = y;
                projectionMatrix[6] = 0;
                projectionMatrix[7] = b;
                projectionMatrix[8] = 0;
                projectionMatrix[9] = 0;
                projectionMatrix[10] = z;
                projectionMatrix[11] = c;
                projectionMatrix[12] = 0;
                projectionMatrix[13] = 0;
                projectionMatrix[14] = 0;
                projectionMatrix[15] = 1;
                break;

            case INFINITE_PROJECTION:
                float near2 = 2 * (float)nearClip;
                float r_minus_l = right - left;
                float r_plus_l = right + left;
                float t_minus_b = top - bottom;
                float t_plus_b = top + bottom;

                projectionMatrix[0] = near2 / r_minus_l;
                projectionMatrix[1] = 0;
                projectionMatrix[2] = 0;
                projectionMatrix[3] = 0;

                projectionMatrix[4] = 0;
                projectionMatrix[5] = near2 / t_minus_b;
                projectionMatrix[6] = 0;
                projectionMatrix[7] = 0;

                projectionMatrix[8] = r_plus_l / r_minus_l;
                projectionMatrix[9] = t_plus_b / t_minus_b;
                projectionMatrix[10] = -1;
                projectionMatrix[11] = -1;

                //////////////////////////////////////////////
                // rem, this set was indexed 8, 9, 10, 11
                projectionMatrix[12] = 0;
                projectionMatrix[13] = 0;
                projectionMatrix[14] = -near2;
                projectionMatrix[15] = 0;
                //////////////////////////////////////////////

                break;

            // Don't do anything for custom projection matrices
        }
    }
}
