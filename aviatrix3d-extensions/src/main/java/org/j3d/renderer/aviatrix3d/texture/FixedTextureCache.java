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
import java.net.URL;
import java.io.IOException;
import java.util.HashMap;

import org.j3d.aviatrix3d.TextureComponent;
import org.j3d.aviatrix3d.TextureComponent2D;
import org.j3d.aviatrix3d.Texture;

// Local imports
import org.j3d.util.ImageUtils;

/**
 * A cache for texture instance management where the objects always stay in
 * the cache unless explicitly removed.
 * <p>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.4 $
 */
class FixedTextureCache extends AbstractTextureCache
{
	/** Mapping of the filename or URL to a full AV3D texture object */
    private HashMap<String, Texture> textureMap;

	/** Mapping of the filename or URL to a AV3D texture component object */
    private HashMap<String, TextureComponent> componentMap;

    /**
     * Construct a new instance of the empty cache.
     */
    FixedTextureCache()
    {
        textureMap = new HashMap<String, Texture>();
        componentMap = new HashMap<String, TextureComponent>();
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
        Texture texture = textureMap.get(filename);

        if(texture == null)
        {
            System.out.println("Creating textures not supported in FixedTextureCache");
/*
            TextureComponent img = (TextureComponent)componentMap.get(filename);

            if(img == null)
            {
                img = load2DImage(filename);
                componentMap.put(filename, img);
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

            textureMap.put(filename, texture);
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

        Texture texture = textureMap.get(file_path);

        if(texture == null)
        {
            System.out.println("Creating textures not supported in FixedTextureCache");
/*
            TextureComponent img = (TextureComponent)componentMap.get(file_path);

            if(img == null)
            {
                img = load2DImage(file_path);
                componentMap.put(file_path, img);
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

            textureMap.put(file_path, texture);
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
        TextureComponent ret_val = componentMap.get(filename);

        if(ret_val == null)
        {
            ret_val = load2DImage(filename);
            componentMap.put(filename, ret_val);
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
        TextureComponent ret_val = componentMap.get(file_path);

        if(ret_val == null)
        {
            System.out.println("Creating texture components not supported in FixedTextureCache");
/*
            ret_val = load2DImage(file_path);
            componentMap.put(file_path, ret_val);
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
        if (textureMap.containsKey(filename))
            return true;
        else
            return false;
    }

    /**
     * Check to see if a filename is cached for an TextureComponent.
     *
     * @param filename The filename loaded
     * @return Whether the filename is cached as an TextureComponent
     */
    public boolean checkTextureComponent(String filename) {
        if (componentMap.containsKey(filename))
            return true;
        else
            return false;
    }

    /**
     * Register a texture with the cache assigned to a filename.
     *
     * @param texture The texture to store
     * @param filename The filename to register
     */
    public void registerTexture(Texture texture, String filename)
    {
        textureMap.put(filename, texture);

            // TODO: Need to register components as well
/*
           TextureComponent component = texture.getImage(0);
           componentMap.put(filename, component);
*/
    }

    /**
     * Register an imagecomponent with the cache assigned to a filename.
     *
     * @param component The texture to store
     * @param filename The filename to register
     */
    public void registerTextureComponent(TextureComponent component, String filename)
    {
        componentMap.put(filename, component);
    }
}
