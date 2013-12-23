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

package org.j3d.aviatrix3d.pipeline.graphics;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.pipeline.RenderEnvironmentData;
import org.j3d.aviatrix3d.rendering.ObjectRenderable;
import org.j3d.aviatrix3d.rendering.RenderEffectsProcessor;

/**
 * Data holder class used to pass the current environment data along the rendering
 * pipeline.
 * <p>
 *
 * End-user code should never be making use of this class unless the end user is
 * implementing a customised rendering pipeline. This class is used as a simple
 * internal collection of the per-frame renderable data that is passed along
 * each stage of the pipeline.
 *
 * @author Justin Couch
 * @version $Revision: 3.5 $
 */
public class GraphicsEnvironmentData extends RenderEnvironmentData
{
    /** Index into the viewport size array for the X position */
    public static final int VIEW_X = 0;

    /** Index into the viewport size array for the Y position */
    public static final int VIEW_Y = 1;

    /** Index into the viewport size array for the width */
    public static final int VIEW_WIDTH = 2;

    /** Index into the viewport size array for the height */
    public static final int VIEW_HEIGHT = 3;

    /** If set, use this to do pre and post rendered effects */
    public RenderEffectsProcessor effectsProcessor;

    /** The current fog instance */
    public ObjectRenderable fog;

    /** The current background instance, if set */
    public ObjectRenderable background;

    /**
     * The amount of eye offset in use from the nominal middle. This is a
     * convenience data item to save the need to regenerate it at the renderer
     * level.
     */
    public float[] eyeOffset;

    /**
     * The 6 values that define the view frustum. The order is:<br/>
     * viewFrustum[0] = x minimum<br/>
     * viewFrustum[1] = x maximum<br/>
     * viewFrustum[2] = y minimum<br/>
     * viewFrustum[3] = y maximum<br/>
     * viewFrustum[4] = near clip<br/>
     * viewFrustum[5] = far clip
     */
    public double[] viewFrustum;

    /**
     * Represents custom projection matrix which was set in the
     * ViewEnvironment by the user.  This value gets filled in when the user
     * manually sets the projection matrix from the ViewEnvironment object.
     */
    public float[] projectionMatrix;

    /**
     * The 6 values that define the frustum to render the background with.
     * The order is:<br/>
     * backgroundFrustum[0] = x minimum<br/>
     * backgroundFrustum[1] = x maximum<br/>
     * backgroundFrustum[2] = y minimum<br/>
     * backgroundFrustum[3] = y maximum<br/>
     * backgroundFrustum[4] = near clip<br/>
     * backgroundFrustum[5] = far clip
     */
    public double[] backgroundFrustum;


    /**
     * The projection type - one of the values from ViewEnvironmentCullable. If
     * the projection is set to orthographic, then the viewFrustum holds the
     * near clip distance in index 4 and the far clip in index 5.
     */
    public int viewProjectionType;

    /**
     * Flag to say whether this environment should be rendered
     * using stereo or not. Not all layers will want stereo effects
     * used.
     */
    public boolean useStereo;

    /**
     * The current dimensions of the viewport, if set. Dimensions are described
     * as.<br/>
     * viewport[0] = x<br/>
     * viewport[1] = y<br/>
     * viewport[2] = width<br/>
     * viewport[3] = height<br/>
     */
    public int[] viewport;

    /**
     * The current dimensions of the scissor area, if set. Dimensions are
     * described as.<br/>
     * scissor[0] = x<br/>
     * scissor[1] = y<br/>
     * scissor[2] = width<br/>
     * scissor[3] = height<br/>
     */
    public int[] scissor;

    /**
     * Transform holding the background matrix, including all the
     * projection information needed to cancel out translations and scales
     * of the viewpoint projection matrix.
     */
    public double[] backgroundTransform;

    /**
     * The camera transformation matrix. This is the inverse
     * of the viewTransform, and also flattened into array form for
     * passing direct to Open GL
     */
    public double[] cameraTransform;

    /**
     * Create a new instance of this class. The variables are initialized
     * to their default values and arrays constructed.
     */
    public GraphicsEnvironmentData()
    {
        scissor = new int[4];
        viewport = new int[4];
        eyeOffset = new float[3];
        viewFrustum = new double[6];
        backgroundFrustum = new double[6];
        backgroundTransform = new double[16];
        cameraTransform = new double[16];
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Generate a string representation of this environment data.
     *
     * @return a formatted string of the details
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer("GraphicsEnvironmentData:\n");
        buf.append("Layer : ");
        buf.append(layerId);
        buf.append(" sub layer: ");
        buf.append(subLayerId);
        buf.append("\nViewport: ");
        buf.append(viewport[0]);
        buf.append(' ');
        buf.append(viewport[1]);
        buf.append(' ');
        buf.append(viewport[2]);
        buf.append(' ');
        buf.append(viewport[3]);
        buf.append("\nFog? ");
        buf.append(fog != null);
        buf.append(" Background? ");
        buf.append(background != null);
        buf.append(" Stereo: ");
        buf.append(useStereo);

        buf.append("\nEye Offset: ");
        buf.append(eyeOffset[0]);
        buf.append(' ');
        buf.append(eyeOffset[1]);
        buf.append(' ');
        buf.append(eyeOffset[2]);

        return buf.toString();
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Convenience method to set the viewport array from another array
     *
     * @param bounds An array of at least length 4
     */
    public void setViewport(int[] bounds)
    {
        viewport[0] = bounds[0];
        viewport[1] = bounds[1];
        viewport[2] = bounds[2];
        viewport[3] = bounds[3];
    }

    /**
     * Convenience method to set the scissor array from another array
     *
     * @param bounds An array of at least length 4
     */
    public void setScissor(int[] bounds)
    {
        scissor[0] = bounds[0];
        scissor[1] = bounds[1];
        scissor[2] = bounds[2];
        scissor[3] = bounds[3];
    }
}
