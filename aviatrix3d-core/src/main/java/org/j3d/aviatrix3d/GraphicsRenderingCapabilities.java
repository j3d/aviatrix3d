/***************************************************************************
 *                        Copyright j3d.org (c) 2000 - ${year}
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read docs/lgpl.txt for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * **************************************************************************/

package org.j3d.aviatrix3d;

/**
 * End user definition of the rendering capabilities requested from the system
 * before attempting to render. This abstracts away the JOGL
 * {@link javax.media.opengl.GLCapabilities} into an AV3D-specific class so
 * that there is no end user requirement to also import JOGL classes into their
 * project.
 * <p/>
 * The default setup has a single opaque RGB buffer with 16 bit depth,
 * double buffered, no stereo.
 *
 * @author Justin Couch
 */
public class GraphicsRenderingCapabilities
{
    /** Number of bits for the red component of the primary buffer. Default value 8 */
    public int redBits  = 8;

    /** Number of bits for the green component of the primary buffer. Default value 8 */
    public int greenBits = 8;

    /** Number of bits for the blue component of the primary buffer. Default value 8 */
    public int blueBits = 8;

    /** Number of bits for the alpha component of the primary buffer. Default value 0 */
    public int alphaBits = 0;

    /** Flag to enable double buffered rendering. Default value is true */
    public boolean doubleBuffered = true;

    /**
     * Flag for requesting implicit stereo rendering. The technique used depends on
     * what the hardware has available. Default value is false.
     */
    public boolean stereo = false;

    /** Number of bits for the depth buffer. Default value 16 */
    public int depthBits      = 16;

    /** Number of red bits for the accumulation buffer. Default value 0 */
    public int accumRedBits   = 0;

    /** Number of green bits for the accumulation buffer. Default value 0 */
    public int accumGreenBits = 0;

    /** Number of blue bits for the accumulation buffer. Default value 0 */
    public int accumBlueBits  = 0;

    /** Number of alpha bits for the accumulation buffer. Default value 0 */
    public int accumAlphaBits = 0;

    /** Number of bits for the stencil buffer. Default value 0 */
    public int stencilBits    = 0;

    /**
     * Flag to enable the use of floating point, rather than integer buffer
     * for rendering. Only used for offscreen drawables such as MRTT and FBOs.
     * Defaults to false
     */
    public boolean useFloatingPointBuffers = false;

    /**
     * Flag to enable the use of MSAA sample buffers when rendering. Defaults
     * to false.
     */
    public boolean useSampleBuffers = false;

    /**
     * If {@link #useSampleBuffers} is true, this is the number of samples
     * to use per output pixel. Default value of 2.
     */
    public int numSamples = 2;

    /** Support for transparent windows containing OpenGL content */
    public boolean backgroundOpaque = true;

    /** Number of bits for the red component when non-opaque buffer. Default value 0 */
    public int transparentValueRed = 0;

    /** Number of bits for the green component when non-opaque buffer. Default value 0 */
    public int transparentValueGreen = 0;

    /** Number of bits for the blue component when non-opaque buffer. Default value 0 */
    public int transparentValueBlue = 0;

    /** Number of bits for the alpha component when non-opaque buffer. Default value 0 */
    public int transparentValueAlpha = 0;
}
