
// External imports
import javax.media.Buffer;
import javax.media.Format;
import javax.media.Control;

import javax.media.format.VideoFormat;
import javax.media.format.RGBFormat;
import javax.media.format.YUVFormat;
import javax.media.renderer.VideoRenderer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

// Local imports
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.ByteTextureComponent2D;

/**
 * JMF Renderer for taking RGB output and processing it as a
 * sub texture update.
 *
 * This demo is based on the ideas provided in the example SimpleAWTRenderer
 * that comes with JMF 2.0. It presents a very simple playback interface with
 * no time-based control over the video stream. If you would like to have
 * greater control, then a look through the Xj3D codebase at how it handles
 * the MovieTexture will give you a lot of insight.
 *
 * @author Justin Couch
 */
public class VideoTextureRenderer implements VideoRenderer, NodeUpdateListener
{
    /** Listing indicating no control objects are available */
    private static final Control[] NO_CONTROLS = new Control[0];

    /** The descriptive name of this renderer */
    private static final String PLUGIN_NAME = "Example AV3D Video Renderer";

    /** List of formats that we want to work with */
    private static final Format[] supportedFormats;

    /** The standard size in pixels we'd like the texture to be */
    public static final int TEXTURE_FRAME_SIZE = 512;


    /** The input format that was given to use by JMF */
    private Format currentFormat;

    /** The TextureComponent that we'll be writing the video to */
    private ByteTextureComponent2D outputTexture;

    /** The width of the current frame in pixels */
    private int frameWidth;

    /** The height of the current frame in pixels */
    private int frameHeight;

    /** The processed pixels of the last frame completed */
    private byte[] processedFrame;

    /** Work array for copying the pixels into as they are being processed */
    private byte[] inProgressFrame;

    /** Flag indicating that a new frame is available since last check */
    private boolean newFrameAvailable;

    /**
     * Static constructor to initialised the set of formats that we want to
     * work with.
     */
    static
    {
        // Prepare supported input formats and preferred format. We want to
        // use separate ints per component because it's faster to decode and
        // set into the Aviatrix3D array than using them all in the one int and
        // using bitmasking to separate each component.
        Dimension frame_size = new Dimension(TEXTURE_FRAME_SIZE,
                                             TEXTURE_FRAME_SIZE);

/*
        Format req_rgb = new RGBFormat(null, //frame_size,
                                       Format.NOT_SPECIFIED,
                                       Format.byteArray,
                                       Format.NOT_SPECIFIED,
                                       24,            // bitsPerPixel
                                       3,
                                       2,
                                       1,
                                       3,             // pixel stride
                                       Format.NOT_SPECIFIED,
                                       Format.TRUE  ,
                                       Format.NOT_SPECIFIED);
*/

        Format req_rgb = new RGBFormat(null, //frame_size,
                                       Format.NOT_SPECIFIED,
                                       Format.byteArray,
                                       Format.NOT_SPECIFIED,
                                       24,            // bitsPerPixel
                                       1,
                                       2,
                                       3,
                                       3,             // pixel stride
                                       Format.NOT_SPECIFIED,
                                       Format.TRUE  ,
                                       Format.NOT_SPECIFIED);

        supportedFormats = new VideoFormat[1];
        supportedFormats[0] = req_rgb;
    }

    /**
     * Create a new video texture output for working with JMF and
     * depositing the texture to the given texture.
     */
    public VideoTextureRenderer(ByteTextureComponent2D texture)
    {
        outputTexture = texture;
    }

    //----------------------------------------------------------
    // Methods required by the NodeUpdateListener interface.
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src)
    {
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src)
    {
        outputTexture.updateSubImage(0,
                                     0,
                                     frameWidth,
                                     frameHeight,
                                     0,
                                     processedFrame);
        newFrameAvailable = false;
    }

    //----------------------------------------------------------------------
    // Methods defined by VideoRenderer (JMF)
    //----------------------------------------------------------------------

    /**
     * Returns an AWT component that it will render to. Returns null
     * if it is not rendering to an AWT component.
     *
     * @return null always as we handle our own rendering
     */
    public Component getComponent()
    {
        return null;
    }

    /**
     * Requests the renderer to draw into a specified AWT component.
     * Returns false if the renderer cannot draw into the specified
     * component.
     *
     * @return false always as we handle our own rendering
     */
    public boolean setComponent(Component comp)
    {
        return false;
    }

    /**
     * Sets the region in the component where the video is to be rendered to.
     * Video is to be scaled if necessary. Ignored for this class.
     *
     * @param rect The bounds to use
     */
    public void setBounds(Rectangle rect)
    {
    }

    /**
     * Returns the region in the component where the video will be
     * rendered to. Returns null if the entire component is being used.
     *
     * @returns null always
     */
    public Rectangle getBounds()
    {
        return null;
    }

    //----------------------------------------------------------------------
    // Methods defined by Renderer (JMF)
    //----------------------------------------------------------------------


    /**
     * Instruction indicating that the rendering process has started.
     */
    public void start()
    {
    }

    /**
     * Instruction indicating that the rendering process has stopped.
     */
    public void stop()
    {
    }

    /**
     * Lists the possible input formats supported by this plug-in.
     *
     * @return An array of supported formats
     */
    public Format[] getSupportedInputFormats()
    {
        return supportedFormats;
    }

    /**
     * Set the data input format and check for matching with what we want.
     *
     * @param format What the system wants to give us
     * @return What we really want, again....
     */
    public Format setInputFormat(Format format)
    {
        Format ret_val = null;

        if(format instanceof RGBFormat)
        {
            RGBFormat rgb_f = (RGBFormat)format;
            Dimension size = rgb_f.getSize();
            frameWidth = size.width;
            frameHeight = size.height;
            currentFormat = format;

System.out.println("frame width: " + frameWidth + " height: " + frameHeight);
            // Do we need it flipped, based on the Y-up flag of the
            // texture component?
            int flip = outputTexture.isYUp() ? Format.TRUE : Format.FALSE;

            ret_val = new RGBFormat(size,
                                    rgb_f.getMaxDataLength(),
                                    rgb_f.getDataType(),
                                    rgb_f.getFrameRate(),
                                    rgb_f.getBitsPerPixel(),
                                    rgb_f.getRedMask(),
                                    rgb_f.getGreenMask(),
                                    rgb_f.getBlueMask(),
                                    rgb_f.getPixelStride(),
                                    rgb_f.getLineStride(),
                                    flip,
                                    rgb_f.getEndian());

            // now, create the buffers to the right size
            int req_size = frameWidth * frameHeight * 3;
            if((processedFrame == null) || (processedFrame.length < req_size))
            {
                processedFrame = new byte[req_size];
                inProgressFrame = new byte[req_size];
            }
        }

        return ret_val;
    }

    /**
     * Processes the data now and make it ready for the texture.
     *
     * @param buffer The source for the data
     * @return A control code indicating success or failure
     */
    public synchronized int process(Buffer buffer)
    {
        if(buffer.isEOM())
            return BUFFER_PROCESSED_OK;

        Format inf = buffer.getFormat();
        if(inf == null)
            return BUFFER_PROCESSED_FAILED;

        if((inf != currentFormat) || !buffer.getFormat().equals(currentFormat))
        {
            if(setInputFormat(inf) != null)
                return BUFFER_PROCESSED_FAILED;
        }

        Object data = buffer.getData();
        if(!(data instanceof byte[]))
            return BUFFER_PROCESSED_FAILED;


        // Take the pixels and write them out to individual bytes
        byte[] src_pixels = (byte[])data;
        int num_pixels = frameWidth * frameHeight * 3;

        System.arraycopy(src_pixels, 0, inProgressFrame, 0, num_pixels);

        // Swap the two buffers and set the ready flag.
        byte[] tmp = processedFrame;
        processedFrame = inProgressFrame;
        inProgressFrame = tmp;
        newFrameAvailable = true;

        // Convert and write to our local image
        return BUFFER_PROCESSED_OK;
    }

    //----------------------------------------------------------------------
    // Methods defined by Plugin (JMF)
    //----------------------------------------------------------------------

    public String getName()
    {
        return PLUGIN_NAME;
    }

    /**
     * Opens the plugin.
     */
    public void open()
    {
    }

    /**
     * Resets the state of the plug-in. Typically at end of media or when media
     * is repositioned.
     */
    public void reset()
    {
    }

    /**
     * Close and release the resources used by the renderer.
     */
    public void close()
    {
    }

    //----------------------------------------------------------------------
    // Methods defined by Controls (JMF)
    //----------------------------------------------------------------------

    /**
     * Obtain the collection of supported controls for this renderer.
     *
     * @return A zero length array for no controls
     */
    public Object[] getControls()
    {
        return NO_CONTROLS;
    }

    /**
     * Return the control based on a control type for the PlugIn. Since we
     * don't support controls, this always returns null.
     *
     * @param controlType A name describing the control
     * @return null always
     */
    public Object getControl(String controlType)
    {
        return null;
    }

    //----------------------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------------------

    /**
     * Tell the renderer to notify the texture now if there are any updates
     * ready to roll. This synchronises between the external threading of JMF
     * and the update requirements of Aviatrix3D.
     */
    public void syncTextureUpdate()
    {
        if(newFrameAvailable) {
            if (outputTexture.isLive())
                outputTexture.dataChanged(this);
            else {
                // ignore as its not visible
            }
        }
    }
}
