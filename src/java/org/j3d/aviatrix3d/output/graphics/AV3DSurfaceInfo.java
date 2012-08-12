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

package org.j3d.aviatrix3d.output.graphics;

// External imports
import java.util.Collections;
import java.util.Set;

// Local imports
import org.j3d.aviatrix3d.pipeline.graphics.SurfaceInfo;

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
class AV3DSurfaceInfo implements SurfaceInfo
{
    /** The GL major version number */
    private int glMajorVersion;

    /** The GL minor version number */
    private int glMinorVersion;

    /** The shading language major version number */
    private int glslMajorVersion;

    /** The shading language minor version number */
    private int glslMinorVersion;

    /** The driver version string */
    private String driverInfo;

    /** The vendor info string */
    private String vendorInfo;

    /** Maximum number of lights in the scene */
    private int maxLights;

    /** Maximum number of textures in the scene */
    private int maxTextures;

    /** Maximum number of clip planes in the scene */
    private int maxClipPlanes;

    /** Maximum number of render targets in the scene */
    private int maxMRTs;

    /** Maximum number of color attachments for a FBO in the scene */
    private int maxColorAttachments;

    /** The set of GL extensions used */
    private Set<String> extensions;

    /**
     * Construct an instance of this class with the given details. Keeps a
     * reference to the extensions map rather than copying it.
     */
    AV3DSurfaceInfo(int major,
                    int minor,
                    int shaderMajor,
                    int shaderMinor,
                    String driver,
                    String vendor,
                    int lights,
                    int textures,
                    int clips,
                    int mrts,
                    int maxColors,
                    Set<String> exts)
    {
        glMajorVersion = major;
        glMinorVersion = minor;
        glslMajorVersion = shaderMajor;
        glslMinorVersion = shaderMinor;
        driverInfo = driver;
        vendorInfo = vendor;
        maxLights = lights;
        maxTextures = textures;
        maxClipPlanes = clips;
        maxMRTs = mrts;
        maxColorAttachments = maxColors;
        extensions = Collections.unmodifiableSet(exts);
    }

    //----------------------------------------------------------
    // Methods defined by Object
    //----------------------------------------------------------

    /**
     * Generate a string version of this information
     *
     * @return A string with pretty printing
     */
    @Override
    public String toString()
    {
        // Don't internationalise this because it is mostly debugging, rather than
        // error messages.
        StringBuilder bldr = new StringBuilder("Graphics card info");
        bldr.append("GL Version ");
        bldr.append(getGLMajorVersion());
        bldr.append(".");
        bldr.append(getGLMinorVersion());
        bldr.append("Vendor: ");
        bldr.append(getVendorString());
        bldr.append("Driver: ");
        bldr.append(getDriverInfo());
        bldr.append("Shader Version ");
        bldr.append(getShaderMajorVersion());
        bldr.append(".");
        bldr.append(getShaderMinorVersion());
        bldr.append("Max lights:       ");
        bldr.append(getMaxLightCount());
        bldr.append("Max clip planes:  ");
        bldr.append(getMaxClipPlanesCount());
        bldr.append("Max textures:     ");
        bldr.append(getMaxTextureCount());
        bldr.append("Max RTs:          ");
        bldr.append(getMaxMRTCount());
        bldr.append("Max Color Attach: ");
        bldr.append(getMaxColorAttachmentsCount());

        return bldr.toString();
    }

    //----------------------------------------------------------
    // Methods defined by SurfaceInfo
    //----------------------------------------------------------

    /**
     * Get the major version number of OpenGL supported
     *
     * @return A positive number
     */
    public int getGLMajorVersion()
    {
        return glMajorVersion;
    }

    /**
     * Get the minor version number of OpenGL supported
     *
     * @return A non-negative number
     */
    public int getGLMinorVersion()
    {
        return glMinorVersion;
    }

    /**
     * Get the major version number of OpenGL shading language supported
     *
     * @return A positive number
     */
    public int getShaderMajorVersion()
    {
        return glslMajorVersion;
    }

    /**
     * Get the minor version number of OpenGL shading language supported
     *
     * @return A non-negative number
     */
    public int getShaderMinorVersion()
    {
        return glslMinorVersion;
    }

    /**
     * Get the vendor's driver version string
     *
     * @return A vendor-specific string
     */
    public String getDriverInfo()
    {
        return driverInfo;
    }

    /**
     * Get the vendor string from this driver
     *
     * @return A vendor-specific string
     */
    public String getVendorString()
    {
        return vendorInfo;
    }

    /**
     * Get the list of extensions supported
     *
     * @return A set of all supported extension strings
     */
    public Set<String> getExtensions()
    {
        return extensions;
    }

    /**
     * Get the maximum number of lights allowed in the scene.
     *
     * @return A non-negative number
     */
    public int getMaxLightCount()
    {
        return maxLights;
    }

    /**
     * Get the maximum number of textures allowed for an appearance.
     *
     * @return A non-negative number
     */
    public int getMaxTextureCount()
    {
        return maxTextures;
    }

    /**
     * Get the maximum number of clip planes allowed in the scene.
     *
     * @return A non-negative number
     */
    public int getMaxClipPlanesCount()
    {
        return maxClipPlanes;
    }

    /**
     * Get the maximum number of multiple simultaneous render targets allowed
     * for a single shader.
     *
     * @return A non-negative number
     */
    public int getMaxMRTCount()
    {
        return maxMRTs;
    }

    /**
     * Get the maxium number of colour attachment points are supported by
     * a single FBO.
     *
     * @return A non-negative number
     */
    public int getMaxColorAttachmentsCount()
    {
        return maxColorAttachments;
    }
}
