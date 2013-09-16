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
import java.lang.ref.WeakReference;
import java.net.URL;
import java.io.IOException;
import java.util.HashMap;

import org.j3d.aviatrix3d.TextureComponent;
import org.j3d.aviatrix3d.TextureComponent2D;
import org.j3d.aviatrix3d.Texture;

// Local imports
import org.j3d.util.ImageUtils;

/**
 * A cache for texture instance management where the objects stay according
 * to Java's WeakReference system.
 * <p>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.6 $
 */
class WeakRefTextureCache extends AbstractTextureCache
{
	/** Mapping of the filename or URL to a full AV3D texture object */
    private HashMap<String, WeakReference<Texture>> textureMap;

	/** Mapping of the filename or URL to a AV3D texture component object */
    private HashMap<String, WeakReference<TextureComponent>> componentMap;

    /**
     * Construct a new instance of the empty cache.
     */
    WeakRefTextureCache()
    {
        textureMap = new HashMap<String, WeakReference<Texture>>();
        componentMap = new HashMap<String, WeakReference<TextureComponent>>();
    }

    /**
     * Fetch the texture named by the filename. The filename may be
     * either absolute or relative to the classpath.
     *
     * @param filename The filename to fetch
     * @return The texture instance for that filename
     * @throws IOException An I/O error occurred during loading
     */
    public Texture fetchTexture(String filename)
        throws IOException
    {
        Texture texture = getTexture(filename);

        if(texture == null)
        {
            System.out.println("Creating textures not supported in WeakRefTextureCache");
/*
            TextureComponent img = getTextureComponent(filename);

            if(img == null)
            {
                img = load2DImage(filename);
                ref = new WeakReference(img);
                componentMap.put(filename, ref);
            }

            int format = texUtils.getTextureFormat(img);

            if(img instanceof TextureComponent2D)
            {
                texture = new Texture2D(Texture.BASE_LEVEL,
                                        format,
                                        img.getWidth(),
                                        img.getHeight());
            }
            else
            {
                texture = new Texture3D(Texture.BASE_LEVEL,
                                        format,
                                        img.getWidth(),
                                        img.getHeight(),
                                        ((TextureComponent3D)img).getDepth());
            }

            texture.setImage(0, img);
            ref = new WeakReference(texture);
            textureMap.put(filename, ref);
*/
        }

        return texture;
    }

    /**
     * Fetch the texture named by the URL.
     *
     * @param url The URL to read data from
     * @return The texture instance for that URL
     * @throws IOException An I/O error occurred during loading
     */
    public Texture fetchTexture(URL url)
        throws IOException
    {
        String file_path = url.toExternalForm();

        Texture texture = getTexture(file_path);

        if(texture == null)
        {
            System.out.println("Creating textures not supported in WeakRefTextureCache");
/*
            TextureComponent img = getTextureComponent(file_path);

            if(img == null)
            {
                img = load2DImage(file_path);
                ref = new WeakReference(img);
                componentMap.put(file_path, ref);
            }

            int format = texUtils.getTextureFormat(img);

            if(img instanceof TextureComponent2D)
            {
                texture = new Texture2D(Texture.BASE_LEVEL,
                                        format,
                                        img.getWidth(),
                                        img.getHeight());
            }
            else
            {
                texture = new Texture3D(Texture.BASE_LEVEL,
                                        format,
                                        img.getWidth(),
                                        img.getHeight(),
                                        ((TextureComponent3D)img).getDepth());
            }

            texture.setImage(0, img);
            ref = new WeakReference(texture);
            textureMap.put(file_path, ref);
*/
        }

        return texture;
    }

    /**
     * Param fetch the imagecomponent named by the filename. The filename may
     * be either absolute or relative to the classpath.
     *
     * @param filename The filename to fetch
     * @return The TextureComponent instance for that filename
     * @throws IOException An I/O error occurred during loading
     */
    public TextureComponent fetchTextureComponent(String filename)
        throws IOException
    {
        TextureComponent ret_val = getTextureComponent(filename);

        if(ret_val == null)
        {
            System.out.println("Creating texture components not supported in WeakRefTextureCache");
/*
            ret_val = load2DImage(filename);
            ref = new WeakReference(ret_val);
            componentMap.put(filename, ref);
*/
        }

        return ret_val;
    }

    /**
     * Fetch the image component named by the URL.
     *
     * @param url The URL to read data from
     * @return The TextureComponent instance for that URL
     * @throws IOException An I/O error occurred during loading
     */
    public TextureComponent fetchTextureComponent(URL url)
        throws IOException
    {
        String file_path = url.toExternalForm();
        TextureComponent ret_val = getTextureComponent(file_path);

        if(ret_val == null)
        {
            System.out.println("Creating texture components not supported in WeakRefTextureCache");
/*
            ret_val = load2DImage(file_path);
            ref = new WeakReference(ret_val);
            componentMap.put(file_path, ref);
*/
        }

        return ret_val;
    }

    /**
     * Explicitly remove the named texture and image component from the cache.
     * If the objects have already been freed according to the rules of the
     * cache system, this request is silently ignored.
     *
     * @param filename The name the texture was registered under
     */
    public void releaseTexture(String filename)
    {
        textureMap.remove(filename);
        componentMap.remove(filename);
    }

    /**
     * Explicitly remove the named texture and image component from the cache.
     * If the objects have already been freed according to the rules of the
     * cache system, this request is silently ignored.
     *
     * @param url The URL the texture was registered under
     */
    public void releaseTexture(URL url)
    {
        String file_path = url.toExternalForm();
        textureMap.remove(file_path);
        componentMap.remove(file_path);
    }

    /**
     * Clear the entire cache now. It will be empty after this call, forcing
     * all fetch requests to reload the data from the source. Use with
     * caution.
     */
    public void clearAll()
    {
        textureMap.clear();
        componentMap.clear();
    }

    /**
     * Check to see if a filename is cached for a Texture.
     *
     * @param filename The filename loaded
     * @return Whether the filename is cached as a Texture
     */
    public boolean checkTexture(String filename)
    {
        boolean ret_val = false;
        WeakReference<Texture> ref = textureMap.get(filename);
        if(ref != null)
        {
            Texture tex = ref.get();
            if(tex != null)
                ret_val = true;
            else
                textureMap.remove(filename);
        }
        return ret_val;
    }

    /**
     * Check to see if a filename is cached for an TextureComponent.
     *
     * @param filename The filename loaded
     * @return Whether the filename is cached as an TextureComponent
     */
    public boolean checkTextureComponent(String filename)
    {
        boolean ret_val = false;
        WeakReference<TextureComponent> ref = componentMap.get( filename );
        if(ref != null)
        {
            TextureComponent tex = ref.get();
            if(tex != null)
                ret_val = true;
            else
                componentMap.remove(filename);
        }
        return ret_val;
    }

    /**
     * Register a texture with the cache assigned to a filename.
     *
     * @param texture The texture to store
     * @param filename The filename to register
     */
    public void registerTexture(Texture texture, String filename)
    {
        WeakReference<Texture> ref = new WeakReference<Texture>(texture);
        textureMap.put(filename, ref);

        // TODO: Need to register components as well
/*
       TextureComponent component = texture.getImage(0);
       ref = new WeakReference(component);
       componentMap.put(filename, ref);
*/
    }

    /**
     * Register an imagecomponent with the cache assigned to a filename.
     *
     * @param texture The texture to store
     * @param filename The filename to register
     */
    public void registerTextureComponent(TextureComponent component, String filename)
    {
        WeakReference<TextureComponent> ref =
        	new WeakReference<TextureComponent>(component);

        componentMap.put(filename, ref);
    }

    //------------------------------------------------------------------------
    // Local methods
    //------------------------------------------------------------------------

    /**
     * Fetch a texture from the maps. Perform checks to make sure it is still
     * a valid reference and clears the map entry if it is not.
     *
     * @param name The name of the texture to get (the map key)
     * @return The Texture instance, if still valid, else null
     */
    private Texture getTexture(String name)
    {
        WeakReference<Texture> ref = textureMap.get(name);

        if(ref == null)
            return null;

        Texture ret_val = ref.get();
        if(ret_val == null)
            textureMap.remove(name);

        return ret_val;
    }

    /**
     * Fetch an image component from the maps. Perform checks to make sure it
     * is still a valid reference and clears the map entry if it is not.
     *
     * @param name The name of the component to get (the map key)
     * @return The TextureComponent instance, if still valid, else null
     */
    private TextureComponent getTextureComponent(String name)
    {
        WeakReference<TextureComponent> ref = componentMap.get(name);

        if(ref == null)
            return null;

        TextureComponent ret_val = ref.get();
        if(ret_val == null)
            componentMap.remove(name);

        return ret_val;
    }
}
