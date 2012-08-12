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
import java.util.Set;

// Local imports
// None

/**
 * Representation of the capabilities of a graphics device's capabilities.
 * <p>
 *
 * The values in here are returned each time the underlying OpenGL context
 * changes and requires at least one rendering pass to have started before
 * this information is available.
 *
 * @author Justin Couch
 * @version $Revision: 3.3 $
 */
public interface SurfaceInfo
{
    /**
     * Get the major version number of OpenGL supported
     *
     * @return A positive number
     */
    public int getGLMajorVersion();

    /**
     * Get the minor version number of OpenGL supported
     *
     * @return A non-negative number
     */
    public int getGLMinorVersion();

    /**
     * Get the major version number of OpenGL shading language supported
     *
     * @return A positive number
     */
    public int getShaderMajorVersion();

    /**
     * Get the minor version number of OpenGL shading language supported
     *
     * @return A non-negative number
     */
    public int getShaderMinorVersion();

    /**
     * Get the vendor's driver version string
     *
     * @return A vendor-specific string
     */
    public String getDriverInfo();

    /**
     * Get the vendor string from this driver
     *
     * @return A vendor-specific string
     */
    public String getVendorString();

    /**
     * Get the list of extensions supported
     *
     * @return A set of all supported extension strings
     */
    public Set<String> getExtensions();

    /**
     * Get the maximum number of lights allowed in the scene.
     *
     * @return A non-negative number
     */
    public int getMaxLightCount();

    /**
     * Get the maximum number of textures allowed for an appearance.
     *
     * @return A non-negative number
     */
    public int getMaxTextureCount();

    /**
     * Get the maximum number of clip planes allowed in the scene.
     *
     * @return A non-negative number
     */
    public int getMaxClipPlanesCount();

    /**
     * Get the maximum number of multiple simultaneous render targets allowed
     * for a single shader.
     *
     * @return A non-negative number
     */
    public int getMaxMRTCount();

    /**
     * Get the maxium number of colour attachment points are supported by
     * a single FBO.
     *
     * @return A non-negative number
     */
    public int getMaxColorAttachmentsCount();
}
