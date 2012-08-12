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
import java.text.Format;
import java.text.MessageFormat;
import java.util.Locale;

import java.awt.image.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.HashMap;

import javax.media.opengl.GL;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.iutil.SubTextureUpdateListener;

/**
 * Common representation of a component that contains source data to be used
 * in textures.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>listenerExceptionMsg: Error message when there was a user-land exception
 *     sending the subtexture update callback</li>
 * </ul>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.10 $
 */
public abstract class TextureComponent extends NodeComponent
    implements TextureSource
{
    /** Message when we failed to send a subtexture update event */
    private static final String TEXTURE_UPDATE_PROP =
        "org.j3d.aviatrix3d.TextureComponent.listenerExceptionMsg";

    /** Initial size of the listener list */
    private static final int LISTENER_SIZE = 1;

    /** Amount to resize the listener list when needed */
    private static final int LISTENER_INC = 2;

    /** Specifies the data is in byte format */
    public static final int TYPE_BYTE = DataBuffer.TYPE_BYTE;

    /** Specifies the data is in int format */
    public static final int TYPE_INT = DataBuffer.TYPE_INT;

    /** The width */
    protected int width;

    /** The format */
    protected int format;

    /** The size of the data buffer */
    protected int size;

    /** The type of the data */
    protected int type;

    /** Buffer to hold the data */
    protected ByteBuffer[] data;

    /** Flag describing whether the Y axis should be inverted before use. By default, yes */
    protected boolean invertY;

    /** The number of levels in this component. */
    protected int numLevels;

    /** Temp buffer used to transfer subimage updates to the listeners */
    protected byte[] copyBuffer;

    /** List of currently valid listeners */
    private SubTextureUpdateListener[] listeners;

    /** Number of currently valid listeners */
    private int numListeners;

    /**
     * Constructs an image with default values.
     *
     * @param numLevels The number of mipmap levels to create
     */
    public TextureComponent(int numLevels)
    {
        invertY = true;
        data = new ByteBuffer[numLevels];
        this.numLevels = numLevels;

        listeners = new SubTextureUpdateListener[LISTENER_SIZE];
    }

    //---------------------------------------------------------------
    // Methods defined by TextureSource
    //---------------------------------------------------------------

    /**
     * Get the width of this image.
     *
     * @return the width.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Get the number of levels in this component.
     *
     * @return The number of levels.
     */
    public int getNumLevels()
    {
        return numLevels;
    }

    /**
     * Get the format of this image at the given mipmap level.
     *
     * @param level The mipmap level to get the format for
     * @return the format.
     */
    public int getFormat(int level)
    {
        // force a conversion now so that format is set correctly.
        if(data[level] == null)
            data[level] = convertImage(level);

        return format;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Add a listener for subtexture change updates.
     *
     * @param l The listener instance to add
     */
    public void addUpdateListener(SubTextureUpdateListener l)
    {
        if(numListeners == listeners.length)
        {
            int old_size = listeners.length;
            int new_size = old_size + LISTENER_INC;

            SubTextureUpdateListener[] tmp =
                new SubTextureUpdateListener[new_size];

            System.arraycopy(listeners, 0, tmp, 0, old_size);

            listeners = tmp;
        }

        listeners[numListeners++] = l;
    }

    /**
     * Remove a listener for subtexture change updates.
     *
     * @param l The listener instance to add
     */
    public void removeUpdateListener(SubTextureUpdateListener l)
    {
        if(numListeners == 1)
        {
            if(listeners[0] == l)
            {
                listeners[0] = null;
                numListeners--;
            }
        }
        else
        {
            // run along the array to find the matching instance
            for(int i = 0; i < numListeners; i++)
            {
                if(listeners[i] == l)
                {
                    System.arraycopy(listeners,
                                     i + 1,
                                     listeners,
                                     i,
                                     numListeners - i - 1);
                    break;
                }
            }
        }
    }

    /**
     * Get the current value of the Y-axis inversion flag.
     *
     * @return true if the Y axis should be flipped
     */
    public boolean isYUp()
    {
        return invertY;
    }

    /**
     * Clear local data stored in this node.  Only data needed for
     * OpenGL calls will be retained;
     */
    public abstract void clearLocalData();

    /**
     * Get the underlying data object.  This will be
     * an array of the underlying data type.
     *
     * @param level Which image level needs to be converted
     * @return A reference to the data.
     */
    protected ByteBuffer getData(int level)
    {
        if(data[level] == null)
            data[level] = convertImage(level);

        return data[level];
    }

    /**
     * Clear the storage used for this object.
     *
     * @param level Which image level needs to be converted
     */
    protected void clearData(int level)
    {
        data[level] = null;
    }

    /**
     * Convenience method to convert a buffered image into a NIO array of the
     * corresponding type. Images typically need to be swapped when doing this
     * by the Y axis is in the opposite direction to the one used by OpenGL.
     *
     * @param level Which image level needs to be converted
     * @return an appropriate array type - either IntBuffer or ByteBuffer
     */
    protected abstract ByteBuffer convertImage(int level);

    /**
     * Ensure copyBuffer is large enough to hold the given number of pixels.
     *
     * @param size The number of bytes to hold
     */
    protected void checkCopyBufferSize(int size)
    {
        if((copyBuffer == null) || size > copyBuffer.length)
        {
            copyBuffer = new byte[size];
        }
    }

    /**
     * Send off a sub-image update event.
     *
     * @param x The start location x coordinate in texel space
     * @param y The start location y coordinate in texel space
     * @param z The start location z coordinate in texel space
     * @param width The width of the update in texel space
     * @param height The height of the update in texel space
     * @param depth The depth of the update in texel space
     * @param level The mipmap level that changed
     * @param pixels Buffer of the data that has updated
     */
    protected void sendTextureUpdate(int x,
                                     int y,
                                     int z,
                                     int width,
                                     int height,
                                     int depth,
                                     int level,
                                     byte[] pixels)
    {
        for(int i = 0; i < numListeners; i++)
        {
            try
            {
                listeners[i].textureUpdated(x,
                                            y,
                                            z,
                                            width,
                                            height,
                                            depth,
                                            level,
                                            pixels);
            }
            catch(Exception e)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                Locale lcl = intl_mgr.getFoundLocale();
                String msg_pattern = intl_mgr.getString(TEXTURE_UPDATE_PROP);

                Object[] msg_args = { listeners.getClass().getName() };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);

                //errorReporter.errorReport(msg, e);
                System.out.println(msg);
                e.printStackTrace();
            }
        }
    }

    /**
     * Convenience method that looks at the user provided image format and
     * returns the number of bytes per pixel
     *
     * @return A value between 1 and 4
     */
    protected int bytesPerPixel()
    {
        int ret_val = 4;

        switch(format)
        {
            case TextureComponent.FORMAT_RGB:
                ret_val = 3;
                break;

            case TextureComponent.FORMAT_RGBA:
                ret_val = 4;
                break;

            case TextureComponent.FORMAT_BGR:
                ret_val = 3;
                break;

            case TextureComponent.FORMAT_BGRA:
                ret_val = 4;
                break;

            case TextureComponent.FORMAT_INTENSITY_ALPHA:
                ret_val = 2;
                break;

            case TextureComponent.FORMAT_SINGLE_COMPONENT:
                ret_val = 1;
        }

        return ret_val;
    }
}
