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

package org.j3d.renderer.aviatrix3d.texture;

// External imports
import java.io.IOException;
import java.net.URL;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.j3d.aviatrix3d.TextureComponent;
import org.j3d.aviatrix3d.Texture;

import org.j3d.util.I18nManager;
import org.j3d.util.IntHashMap;

// Local imports
import org.j3d.texture.CacheAlreadySetException;
import org.j3d.texture.TextureCache;
import org.j3d.texture.TextureCacheFactory;

/**
 * A representation of global cache for texture instance management.
 * <p>
 *
 * The cache works at the Aviatrix3D Texture or TextureComponent instance level
 * rather than down at the individual images. This allows the VM to discard
 * the lower level image instances if needed, allowing Java3D to do its own
 * management. In addition, it benefits runtime performance of the Java3D
 * scene graph by allowing textures instances to be shared, rather than
 * duplicated.
 * <p>
 *
 * Different types of cache implementations are allowed (ie different ways of
 * deciding when an texture no longer needs to be in the cache).
 * <p>
 *
 * The factory also supports the concept of the "default cache". This is used
 * when you want a simple system that doesn't really care about the cache type
 * used and just wants to use this class as a global singleton for storing the
 * texture information. The default cache type can be controlled through either
 * directly setting the value in this class, or using a system property. By
 * defining a value for the property
 * <pre>
 *   org.j3d.texture.DefaultCacheType
 * </pre>
 *
 * with one of the values (case-sensitive) <code>fixed</code>, <code>lru</code>
 * or <code>weakref</code>. Setting the type through the method call will
 * override this setting. However, the cache type can only be set once. All
 * further attempts will result in an exception.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidCacheTypeMsg: Error message for invalid cache type constant</li>
 * </ul>
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class AVTextureCacheFactory implements TextureCacheFactory
{
    /** Error message when the user provides a bad cache type */
    private static final String CACHE_TYPE_PROP =
		"org.j3d.renderer.aviatrix3d.texture.AVTextureCacheFactory.invalidCacheTypeMsg";

    /** ID of the unset cache type */
    private static final int NO_CACHE_SET = -1;

    /** Default cache type if nothing else is set */
    private static final int DEFAULT_CACHE_ID = FIXED_CACHE;

    /** Mapping of cacheType int to the cache implementation */
    private static IntHashMap cacheMap;

    /** The ID of the default cache type */
    private static int defaultCacheType;

    /**
     * Static initialiser to set up the class vars as needed.
     */
    static
    {
        cacheMap = new IntHashMap();
        defaultCacheType = NO_CACHE_SET;
    }

    /**
     * Private constructor to prevent direct instantiation of this class.
     */
    private AVTextureCacheFactory()
    {
    }

    /**
     * Set the default cache type to be used.
     *
     * @param type The default type ID
     * @throws CacheAlreadySetException The default type has already been set
     */
    public static void setDefaultCacheType(int type)
        throws CacheAlreadySetException
    {
        if(defaultCacheType != NO_CACHE_SET)
            throw new CacheAlreadySetException();

        defaultCacheType = type;
    }

    /**
     * Fetch the default cache provided by the factory. The type may be
     * previously specified using the system property or the
     * {@link #setDefaultCacheType(int)} method.
     *
     * @return The default cache implementation
     */
    public static AVTextureCache getCache()
    {
        if(defaultCacheType == NO_CACHE_SET)
        {
            // look up the system property
            String str = System.getProperty(DEFAULT_CACHE_PROP);

            if(str == null)
                defaultCacheType = DEFAULT_CACHE_ID;
            else if(str.equals("fixed"))
                defaultCacheType = FIXED_CACHE;
            else if(str.equals("lru"))
                defaultCacheType = LRU_CACHE;
            else if(str.equals("weakref"))
                defaultCacheType = WEAKREF_CACHE;
            else
                defaultCacheType = DEFAULT_CACHE_ID;
        }

        return getCache(defaultCacheType);
    }

    /**
     * Fetch the cache instance for the given type, creating a new instance
     * if necessary. If the cacheType refers to one of the standard, inbuilt
     * types then it will be automatically generated. If it is not a standard
     * type, an exception will be generated.
     *
     * @param cacheType An identifier of the required caching algorithm
     * @return A reference to the global cache of that type
     * @throws IllegalArgumentException The cacheType is not a valid type
     */
    public static AVTextureCache getCache(int cacheType)
    {
        AVTextureCache ret_val = (AVTextureCache)cacheMap.get(cacheType);

        if(ret_val == null)
        {
            switch(cacheType)
            {
                case FIXED_CACHE:
                    ret_val = new FixedTextureCache();
                    cacheMap.put(FIXED_CACHE, ret_val);
                    break;

                case LRU_CACHE:
                    ret_val = new LRUTextureCache();
                    cacheMap.put(LRU_CACHE, ret_val);
                    break;

                case WEAKREF_CACHE:
                    ret_val = new WeakRefTextureCache();
                    cacheMap.put(WEAKREF_CACHE, ret_val);
                    break;

                default:
					I18nManager intl_mgr = I18nManager.getManager();
					String msg_pattern = intl_mgr.getString(CACHE_TYPE_PROP);

					Locale lcl = intl_mgr.getFoundLocale();

					NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

					Object[] msg_args = { new Integer(cacheType) };
					Format[] fmts = { n_fmt };
					MessageFormat msg_fmt =
						new MessageFormat(msg_pattern, lcl);
					msg_fmt.setFormats(fmts);
					String msg = msg_fmt.format(msg_args);

					throw new IllegalArgumentException(msg);
            }
        }

        return ret_val;
    }

    /**
     * Register your custom instance of a texture cache. If the cacheType has
     * a value less than or equal to the last ID then an exception will be
     * generated. The ID, if it already exists will replace the existing
     * instance with the new one. Passing a value of null will de-register
     * the existing cache if previously registered. Standard types cannot be
     * re-registered.
     *
     * @param cacheType The ID to associate with this cache
     * @param cache The instance of the cache to register
     * @throws IllegalArgumentException The cacheType is invalid
     */
    public static void registerCacheType(int cacheType, AVTextureCache cache)
    {
        if(cacheType <= LAST_CACHE_ID)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg_pattern = intl_mgr.getString(CACHE_TYPE_PROP);

			Locale lcl = intl_mgr.getFoundLocale();

			NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

			Object[] msg_args = { new Integer(cacheType) };
			Format[] fmts = { n_fmt };
			MessageFormat msg_fmt =
				new MessageFormat(msg_pattern, lcl);
			msg_fmt.setFormats(fmts);
			String msg = msg_fmt.format(msg_args);

			throw new IllegalArgumentException(msg);
		}

        cacheMap.put(cacheType, cache);
    }
}
