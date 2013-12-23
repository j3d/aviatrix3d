/*****************************************************************************
 *                        Yumtech, Inc Copyright (c) 2001 - 2006
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
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.j3d.util.I18nManager;

// Local imports
// None

/**
 * Data holder that passes the information about what is to be rendered from a
 * single viewport that has one or more layers.
 *
 * @author Justin Couch
 * @version $Revision: 3.5 $
 */
public class ViewportLayerCollection
{
    /** Message for the non-positive scene size */
    private static final String NEG_COUNT_MSG =
        "org.j3d.aviatrix3d.pipeline.graphics.ViewportCollection.invalidLayerCountMsg";

    /** Scene type is basic scene */
    public static final int SINGLE_SCENE = 1;

    /** Scene type is multipass scene */
    public static final int MULTIPASS_SCENE = 2;

    /** Scene type is 2D scene */
    public static final int FLAT_SCENE = 3;

    /** Listing of valid scenes to render from this latyer */
    public SceneRenderBucket[] scenes;

    /** The number of valid scenes to render in this layer */
    public int numScenes;

    /** Listing of valid scenes to render from this latyer */
    public MultipassRenderBucket[] multipass;

    /** The number of multipass scenes total, in all render buckets */
    public int numMultipass;

    /**
     * Indication of what scene type this layer represents. May be one of
     * normal, multipass or 2D.
     */
    public byte[] sceneType;

    /** The total number of buckets to render */
    public int numBuckets;

    /**
     * Create a default bucket for handling a layer that defaults to having a
     * single scene.
     * <p>
     * By default the multipass buckets are not allocated.
     */
    public ViewportLayerCollection()
    {
        this(1);
    }

    /**
     * Create a bucket for handling a layer that can hold the given number of
     * scenes to be rendered. If the value is <= 0 an exception is generated
     * <p>
     * By default the multipass buckets are not allocated.
     *
     * @param sceneCount The number of scenes to be handled
     * @throws IllegalArgumentException The size was non-positive
     */
    public ViewportLayerCollection(int sceneCount)
        throws IllegalArgumentException
    {
        if(sceneCount < 0)
		{
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NEG_COUNT_MSG);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Integer(sceneCount) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
		}

        scenes = new SceneRenderBucket[sceneCount];
        for(int i = 0; i < sceneCount; i++)
            scenes[i] = new SceneRenderBucket();

        sceneType = new byte[sceneCount];
    }

    /**
     * Ensure that there are enough items in the scenes array for the given
     * number of scenes to be processed. This will resize the array and
     * initialise more bucket instances as needed.
     *
     * @param size The minimum size needed
     */
    public void ensureSceneCapacity(int size)
    {
        if(scenes.length < size)
        {
            int old_size = scenes.length;

            SceneRenderBucket[] tmp = new SceneRenderBucket[size];
            System.arraycopy(scenes, 0, tmp, 0, scenes.length);
            scenes = tmp;

            for(int i = old_size; i < size; i++)
                scenes[i] = new SceneRenderBucket();

            int s2 = size + ((multipass == null) ? 0 : multipass.length);
            byte[] tmp2 = new byte[s2];
            System.arraycopy(sceneType, 0, tmp2, 0, sceneType.length);

            sceneType = tmp2;
        }
    }

    /**
     * Ensure that there are enough items in the scenes array for the given
     * number of scenes to be processed. This will resize the array and
     * initialise more bucket instances as needed.
     *
     * @param size The minimum size needed
     */
    public void ensureMultipassCapacity(int size)
    {
        if(multipass == null || multipass.length < size)
        {
            int old_size = (multipass == null) ? 0 : multipass.length;

            MultipassRenderBucket[] tmp = new MultipassRenderBucket[size];

            if(old_size != 0)
                System.arraycopy(multipass, 0, tmp, 0, multipass.length);

            multipass = tmp;

            for(int i = old_size; i < size; i++)
                multipass[i] = new MultipassRenderBucket();

            int s2 = size + sceneType.length;
            byte[] tmp2 = new byte[s2];
            System.arraycopy(sceneType, 0, tmp2, 0, sceneType.length);

            sceneType = tmp2;
        }
    }
}
