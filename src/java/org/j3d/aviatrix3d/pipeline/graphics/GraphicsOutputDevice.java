/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2007
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
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

// Local imports
import org.j3d.aviatrix3d.pipeline.OutputDevice;

/**
 * Interface representing the output of a render pipeline that specifically
 * deals with graphical (visual) information.
 * <p>
 *
 * <p><b>Mouse Interaction</b></p>
 * <p>
 * When working with any form of 2D input, such as a mouse, you need to
 * transform it's device coordinates into the 3D space of the world, so that
 * you may then do picking or other projections in world space.
 * <p>
 * Each of the mouse methods require that you pass in extra information to
 * help with efficiency and specifically dealing with how a screen can be
 * divided up into multiple viewports, yet still enable dragging operations
 * to work correctly. The problem is that once you leave one viewport, you
 * want to keep using the projection from that same viewport, not another one
 * on the same layer. If you wish to keep the current viewport information the
 * you can pass in a string that represents the identifier of your input device
 * (in case you have multiple simultaneous input devices) and a flag to say
 * whether the last found projection information for that device should still
 * be used. If that flag is true, and the layer and sublayer IDs match, it will
 * just project using the previously fetched information. If any of these
 * conditions are not met, then a new set of projection information is fetched
 * and cached for that ID.
 * <p>
 *
 *
 *
 *
 * @author Justin Couch
 * @version $Revision: 3.9 $
 */
public interface GraphicsOutputDevice extends OutputDevice
{
    /**
     * The surface will render only non-stereo projection (traditional
     * monoscopic projection policy).
     */
    public static final int NO_STEREO = 0;

    /**
     * The stereo rendering type to be used should use quad buffered,
     * alternate frame rendering. In this type it renders to the left back
     * buffer, then right back buffer and then swaps both. This is useful if
     * you have HMDs which have two separate windows for the rendering.
     */
    public static final int QUAD_BUFFER_STEREO = 1;

    /**
     * The stereo rendering type draws to alternate eyes on each frame. Used if
     * you have shutter glasses where the user's vision is restricted to one
     * eye or the other, but not both at the same time. Only uses a single
     * double buffer to render alternate eye points
     */
    public static final int ALTERNATE_FRAME_STEREO = 2;

    /**
     * The stereo rendering uses two canvases - one for each eye. The canvases
     * are placed either side by side or vertically stacked, depending on the
     * rendering options requested.
     */
    public static final int TWO_CANVAS_STEREO = 3;

    /**
     * Check to see whether this surface supports stereo rendering. As this is
     * not known until after initialisation, this method will return false
     * until it can determine whether or not stereo is available.
     *
     * @return true Stereo support is currently available
     */
    public boolean isStereoAvailable();

    /**
     * Check to see whether this surface supports Quad buffer stereo rendering.
     * Quadbuffers uses the GL_BACK_LEFT and GL_BACK_RIGHT for rendering pairs
     * rather than drawing alternate frames to the same window.
     * <p>
     * As this is not known until after initialisation, this method will return
     * false until it can determine whether or not stereo is available.
     *
     * @return true Stereo support is currently available
     */
    public boolean isQuadStereoAvailable();

    /**
     * Set the rendering policy used when handling stereo. The policy must be
     * one of the _STEREO constants defined in this interface.
     *
     * @param policy The policy to currently use
     * @throws IllegalArgumentException The policy type is not one of the legal
     *    selections.
     */
    public void setStereoRenderingPolicy(int policy);

    /**
     * Get the current stereo rendering policy in use. If not explicitly set by
     * the user, then it will default to <code>NO_STEREO</code>.
     *
     * @return One of the *_STEREO values
     */
    public int getStereoRenderingPolicy();

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
     * Set the background colour that this surface should be cleared to before
     * the drawing step. Colours range from 0 to 1 in the normal manner.
     *
     * @param r The red component of the background clear colour
     * @param g The green component of the background clear colour
     * @param b The blue component of the background clear colour
     * @param a The alpha component of the background clear colour
     */
    public void setClearColor(float r, float g, float b, float a);

    /**
     * Set whether we should always force a local colour clear before
     * beginning any drawing. If this is set to false, then we can assume that
     * there is at least one background floating around that we can use to
     * clear whatever was drawn in the previous frame, and so we can ignore the
     * glClear(GL.GL_COLOR_BUFFER_BIT) call. The default is set to true.
     *
     * @param state true if we should always locally clear first
     */
    public void setColorClearNeeded(boolean state);

    /**
     * Enable or disable two pass rendering of transparent objects. By default
     * it is disabled. This flag applies to this surface and any offscreen
     * surfaces that are children of this surface (FBOs, PBuffers etc).
     *
     * @param state true if we should enable two pass rendering
     */
    public void enableTwoPassTransparentRendering(boolean state);

    /**
     * Check the state of the two pass transprent rendering flag.
     *
     * @return true if two pass rendering of transparent objects is enabled
     */
    public boolean isTwoPassTransparentEnabled();

    /**
     * If two pass rendering of transparent objects is enabled, this is the alpha
     * test value used when deciding what to render. The default value is 1.0. No
     * sanity checking is performed, but the value should be between [0,1].
     * <p>
     * This flag applies to this surface and any offscreen
     * surfaces that are children of this surface (FBOs, PBuffers etc).
     *
     * @param cutoff The alpha value at which to enable rendering
     */
    public void setAlphaTestCutoff(float cutoff);

    /**
     * Get the current value of the alpha test cutoff number. Will always
     * return the currently set number regardless of the state of the
     * two pass rendering flag.
     *
     * @return The currently set cut off value
     */
    public float getAlphaTestCutoff();

    /**
     * Update the list of items to be rendered to the current list. Draw them
     * at the next oppourtunity.
     *
     * @param otherData data to be processed before the rendering
     * @param commands The list of drawable surfaces to render
     * @param numValid The number of valid items in the array
     */
    public void setDrawableObjects(GraphicsRequestData otherData,
                                   GraphicsInstructions[] commands,
                                   int numValid);

    /**
     * Swap the buffers now if the surface supports multiple buffer drawing.
     * For surfaces that don't support multiple buffers, this does nothing.
     */
    public void swap();

    /**
     * Get the surface to VWorld transformation matrix.
     *
     * @param x The X coordinate in the entire surface
     * @param y The Y coordinate in the entire surface
     * @param layer The layer ID to fetch from. Layer 0 is the front-most
     * @param subLayer The ID of the viewport-layer that is needed. If there
     *   are no sub-layers, use 0.
     * @param matrix The matrix to copy into
     * @param deviceId A user-defined identifier for the requesting device when
     *    using the lastFound items
     * @param useLastFound Should we skip the search process and use the last
     *    data found for this layer/sublayer combo.
     *
     * @return Whether the coordinates where on the layer
     */
    public boolean getSurfaceToVWorld(int x,
                                   int y,
                                   int layer,
                                   int subLayer,
                                   Matrix4f matrix,
                                   String deviceId,
                                   boolean useLastFound);

    /**
     * Convert a pixel location to surface coordinates.
     *
     * @param x The X coordinate in the entire surface
     * @param y The Y coordinate in the entire surface
     * @param layer The layer ID to fetch from. Layer 0 is the front-most
     * @param subLayer The ID of the viewport-layer that is needed. If there
     *   are no sub-layers, use 0.
     * @param position The converted position.  It must be preallocated.
     * @param deviceId A user-defined identifier for the requesting device when
     *    using the lastFound items
     * @param useLastFound Should we skip the search process and use the last
     *    data found for this layer/sublayer combo.
     *
     * @return Whether the coordinates where on the layer
     */
    public boolean getPixelLocationInSurface(int x,
                                          int y,
                                          int layer,
                                          int subLayer,
                                          Point3f position,
                                          String deviceId,
                                          boolean useLastFound);

    /**
     * Get the Center Eye position in surface coordinates.
     *
     * @param x The X coordinate in the entire surface
     * @param y The Y coordinate in the entire surface
     * @param layer The layer ID to fetch from. Layer 0 is the front-most
     * @param subLayer The ID of the viewport-layer that is needed. If there
     *   are no sub-layers, use 0.
     * @param position The current eye position.  It must be preallocated.
     * @param deviceId A user-defined identifier for the requesting device when
     *    using the lastFound items
     * @param useLastFound Should we skip the search process and use the last
     *    data found for this layer/sublayer combo.
     *
     * @return Whether the coordinates where on the layer
     */
    public boolean getCenterEyeInSurface(int x,
                                      int y,
                                      int layer,
                                      int subLayer,
                                      Point3f position,
                                      String deviceId,
                                      boolean useLastFound);

    /**
     * Add a resize listener instance to this surface. Duplicate listener
     * instance add requests are ignored, as are null values.
     *
     * @param l The new listener instance to add
     */
    public void addGraphicsResizeListener(GraphicsResizeListener l);

    /**
     * Remove a resize listener from this surface. If the listener is not
     * currently registered the request is ignored.
     *
     * @param l The listener instance to remove
     */
    public void removeGraphicsResizeListener(GraphicsResizeListener l);

    /**
     * Add a surface info listener instance to this surface. Duplicate listener
     * instance add requests are ignored, as are null values.
     *
     * @param l The new listener instance to add
     */
    public void addSurfaceInfoListener(SurfaceInfoListener l);

    /**
     * Remove a surface info listener from this surface. If the listener is not
     * currently registered the request is ignored.
     *
     * @param l The listener instance to remove
     */
    public void removeSurfaceInfoListener(SurfaceInfoListener l);
}
