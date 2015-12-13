/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2004 - 2006
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
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

/**
 * Internal proxy renderable object used for when we need to completely
 * override the appearance handling for combining overrides with leaf
 * appearance objects
 *
 *
 * @author Justin Couch
 * @version $Revision: 3.2 $
 */
class OverrideAppearanceProxyRenderable implements AppearanceRenderable
{
    /** Listing of 32 texture IDs as Integers for the render() calls */
    private static final Integer[] TEX_IDS;

    /** Texture ID for only a single texture */
    private static final Integer SINGLE_TEXTURE;

    /** The material properites */
    private TransparentObjectRenderable material;

    /** The texture properties */
    private TextureRenderable[] textureUnits;

    /** Current number of valid textures */
    private int numTextures;

    /** The shader used by the appearance */
    private ShaderRenderable shader;

    /** Attributes for rendering polygons */
    private AppearanceAttributeRenderable polyAttr;

    /** Attributes for rendering lines */
    private AppearanceAttributeRenderable lineAttr;

    /** Attributes for rendering points */
    private AppearanceAttributeRenderable pointAttr;

    /** Attributes for blend management */
    private AppearanceAttributeRenderable blendAttr;

    /** Attributes for depth buffer management */
    private AppearanceAttributeRenderable depthAttr;

    /** Attributes for stencil buffer management */
    private AppearanceAttributeRenderable stencilAttr;

    /** Attributes for alpha testing management */
    private AppearanceAttributeRenderable alphaAttr;

    /** Visible flag from the primary */
    private boolean visible;

    /** Pre-determined transparency flag */
    private boolean transparent;

    /**
     * Static initialiser creates the texture IDs
     */
    static
    {
        TEX_IDS = new Integer[32];
        for(int i = 0; i < 32; i++)
            TEX_IDS[i] = new Integer(GL.GL_TEXTURE0 + i);

        SINGLE_TEXTURE = new Integer(-1);
    }

    /**
     * Construct an instance of this proxy that is used by the
     * system to hide an override.
     */
    OverrideAppearanceProxyRenderable(AppearanceRenderable primary,
                                      AppearanceRenderable secondary)
    {
        visible = false;
        transparent = false;

        // Start by populating everything from the primary. Then if
        // anything is null, copy in the secondary. We could delegate
        // everything but that means during the rendering loop there
        // will be a big pile of if statements. We want to avoid that
        // because it blows performance badly, so let's just make a
        // direct copy of everything now.
        if(primary != null)
        {
            visible = primary.isVisible();

            material = primary.getMaterialRenderable();
            shader = primary.getShaderRenderable();

            numTextures = primary.numTextureRenderables();

            if(numTextures != 0)
            {
                textureUnits = new TextureRenderable[numTextures];
                for(int i = 0; i < numTextures; i++)
                    textureUnits[i] = primary.getTextureRenderable(i);
            }

            // Welcome to Java's bloody long lines. Yeah, I could probably use a
            // static import here to shorten it up a bit.
            blendAttr = primary.getAttributeRenderable(AppearanceAttributeRenderable.BLEND_ATTRIBUTE);
            depthAttr = primary.getAttributeRenderable(AppearanceAttributeRenderable.DEPTH_ATTRIBUTE);
            lineAttr = primary.getAttributeRenderable(AppearanceAttributeRenderable.LINE_ATTRIBUTE);
            pointAttr = primary.getAttributeRenderable(AppearanceAttributeRenderable.POINT_ATTRIBUTE);
            polyAttr = primary.getAttributeRenderable(AppearanceAttributeRenderable.POLYGON_ATTRIBUTE);
            stencilAttr = primary.getAttributeRenderable(AppearanceAttributeRenderable.STENCIL_ATTRIBUTE);
            alphaAttr = primary.getAttributeRenderable(AppearanceAttributeRenderable.ALPHA_ATTRIBUTE);
        }

        // Now work through what we're missing

        if(secondary != null)
        {
            if(material == null)
                material = secondary.getMaterialRenderable();

            if(shader == null)
                shader = secondary.getShaderRenderable();

            if(numTextures == 0)
            {
                numTextures = secondary.numTextureRenderables();

                if(numTextures != 0)
                {
                    textureUnits = new TextureRenderable[numTextures];
                    for(int i = 0; i < numTextures; i++)
                        textureUnits[i] = secondary.getTextureRenderable(i);
                }
            }

            if(blendAttr == null)
                blendAttr = secondary.getAttributeRenderable(AppearanceAttributeRenderable.BLEND_ATTRIBUTE);

            if(depthAttr == null)
                depthAttr = secondary.getAttributeRenderable(AppearanceAttributeRenderable.DEPTH_ATTRIBUTE);

            if(lineAttr == null)
                lineAttr = secondary.getAttributeRenderable(AppearanceAttributeRenderable.LINE_ATTRIBUTE);

            if(pointAttr == null)
                pointAttr = secondary.getAttributeRenderable(AppearanceAttributeRenderable.POINT_ATTRIBUTE);

            if(polyAttr == null)
                polyAttr = secondary.getAttributeRenderable(AppearanceAttributeRenderable.POLYGON_ATTRIBUTE);

            if(stencilAttr == null)
                stencilAttr = secondary.getAttributeRenderable(AppearanceAttributeRenderable.STENCIL_ATTRIBUTE);

            if(alphaAttr == null)
                alphaAttr = secondary.getAttributeRenderable(AppearanceAttributeRenderable.ALPHA_ATTRIBUTE);

        }

        // Now we have everything, let's pre-determine the transparency
        // flag so that we only need to do it once.
        if(blendAttr != null || shader != null || alphaAttr != null)
            transparent = false;
        else if(material != null && material.hasTransparency())
            transparent = true;
        else
        {
            transparent = false;

            for(int j = 0; j < numTextures && !transparent; j++)
            {
                if(textureUnits[j] == null)
                    continue;

                transparent = textureUnits[j].hasTransparency();
            }
        }
    }

    //---------------------------------------------------------------
    // Methods defined by AppearanceRenderable
    //---------------------------------------------------------------

    /**
     * Check to see if this appearance is making the geometry visible or
     * not.
     *
     * @return true when the geometry is visible
     */
    @Override
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * Ask the appearance if it has any transparency values. The implementation
     * should determine this from it's internal set of state, such as looking
     * at material properties, texture formats etc.
     *
     * @return true if any form of non-opaque rendering is defined
     */
    @Override
    public boolean hasTransparency()
    {
        return transparent;
    }

    /**
     * Additional state information to supplement {@link #hasTransparency} by
     * stating whether we have any set of sub-renderables that even define
     * transparency information. This is used for when an appearance renderable
     * defines, say, polygon attributes, but no material or blend attributes at
     * all. In this case you want to know about the 2-sided rendering but need
     * to later check the geometry for colour with alpha  being used.
     *
     * @return true if We can categorically state that no transparency should
     *   be considered in this rendering
     */
    @Override
    public boolean hasTransparencyInfo()
    {
        return (material != null) || (numTextures != 0) ||
               (blendAttr != null) || (shader != null);
    }

    /**
     * Fetch the sub-renderable for the given type. This can then be cast up
     * to the appropriate extended Renderable type.
     *
     * @param attrib The attribute type identifier from
     *   {@link AppearanceAttributeRenderable}
     * @return A renderable for that attribute if one is set, or null
     */
    public AppearanceAttributeRenderable getAttributeRenderable(int attrib)
    {
        switch(attrib)
        {
            case AppearanceAttributeRenderable.BLEND_ATTRIBUTE:
                return blendAttr;

            case AppearanceAttributeRenderable.DEPTH_ATTRIBUTE:
                return depthAttr;

            case AppearanceAttributeRenderable.LINE_ATTRIBUTE:
                return lineAttr;

            case AppearanceAttributeRenderable.POINT_ATTRIBUTE:
                return pointAttr;

            case AppearanceAttributeRenderable.POLYGON_ATTRIBUTE:
                return polyAttr;

            case AppearanceAttributeRenderable.STENCIL_ATTRIBUTE:
                return stencilAttr;

            case AppearanceAttributeRenderable.ALPHA_ATTRIBUTE:
                return alphaAttr;

            default:
                return null;
        }
    }

    /**
     * Request the number of texture renderables that are available to
     * process in this node. OpenGL allows a maximum of 32 textures to be
     * defined.
     *
     * @return A number between 0 and 32
     */
    @Override
    public int numTextureRenderables()
    {
        return numTextures;
    }

    /**
     * Request the renderable for the given texture unit number. If there is
     * no renderable for that number, return null.
     *
     * @param unitNumber The number of the texture unit to fetch
     * @return The matching texture unit renderable or null if not available
     */
    @Override
    public TextureRenderable getTextureRenderable(int unitNumber)
    {
        if(unitNumber < 0 || unitNumber >= numTextures)
            return null;
        else
            return textureUnits[unitNumber];
    }

    /**
     * Fetch the renderable that corresponds to the set programmable shader.
     * If no shader is set, return null.
     *
     * @return The current shader renderable or null
     */
    public ShaderRenderable getShaderRenderable()
    {
        return shader;
    }

    /**
     * Fetch the renderable that corresponds to material properties. If none is
     * set or valid for this visual appearance, return null
     *
     * @return The current shader renderable or null
     */
    public TransparentObjectRenderable getMaterialRenderable()
    {
        return material;
    }

    //---------------------------------------------------------------
    // Methods defined by ObjectRenderable
    //---------------------------------------------------------------

    /**
     * Issue ogl commands needed for this component.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void render(GL2 gl)
    {
        if(numTextures == 1)
        {
            gl.glPushAttrib(GL2.GL_TEXTURE_BIT);
            textureUnits[0].activateTexture(gl, SINGLE_TEXTURE);
            textureUnits[0].render(gl, SINGLE_TEXTURE);
        }
        else if(numTextures > 1)
        {
            gl.glPushAttrib(GL2.GL_TEXTURE_BIT);
            for(int i = 0; i < numTextures ; i++)
            {
                textureUnits[i].activateTexture(gl, TEX_IDS[i]);
                textureUnits[i].render(gl, TEX_IDS[i]);
            }
        }

        if(blendAttr != null)
            blendAttr.render(gl);

        if(alphaAttr != null)
            alphaAttr.render(gl);

        if(polyAttr != null)
            polyAttr.render(gl);

        if(lineAttr != null)
            lineAttr.render(gl);

        if(pointAttr != null)
            pointAttr.render(gl);

        if(depthAttr != null)
            depthAttr.render(gl);

        if(stencilAttr != null)
            stencilAttr.render(gl);

        if(material != null)
        {
            gl.glPushAttrib(GL2.GL_LIGHTING_BIT);
            material.render(gl);
        }

        if(shader != null)
            shader.render(gl);
    }

    /**
     * Restore all openGL state.
     *
     * @param gl The gl context to draw with
     */
    @Override
    public void postRender(GL2 gl)
    {
        if(shader != null)
            shader.postRender(gl);

        if(material != null)
        {
            material.postRender(gl);
            gl.glPopAttrib();
        }

        if(pointAttr != null)
            pointAttr.postRender(gl);

        if(lineAttr != null)
            lineAttr.postRender(gl);

        if(polyAttr != null)
            polyAttr.postRender(gl);

        if(blendAttr != null)
            blendAttr.postRender(gl);

        if(alphaAttr != null)
            alphaAttr.postRender(gl);

        if(depthAttr != null)
            depthAttr.postRender(gl);

        if(stencilAttr != null)
            stencilAttr.postRender(gl);

        if(numTextures == 1)
        {
            textureUnits[0].postRender(gl, SINGLE_TEXTURE);
            textureUnits[0].deactivateTexture(gl, SINGLE_TEXTURE);
            gl.glPopAttrib();
        }
        else if(numTextures > 1)
        {
            for(int i = numTextures - 1; i >= 0; i--)
            {
                textureUnits[i].postRender(gl, TEX_IDS[i]);
                textureUnits[i].deactivateTexture(gl, TEX_IDS[i]);
            }

            gl.glPopAttrib();
        }
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The objec to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        OverrideAppearanceProxyRenderable app = (OverrideAppearanceProxyRenderable)o;
        return compareTo(app);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof OverrideAppearanceProxyRenderable))
            return false;
        else
            return equals((OverrideAppearanceProxyRenderable)o);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param app The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(OverrideAppearanceProxyRenderable app)
    {
        if(app == null)
            return 1;

        if(app == this)
            return 0;

        if(visible != app.visible)
            return visible ? 1 : -1;

        if(material != app.material)
        {
            if(material == null)
                return -1;
            else if(app.material == null)
                return 1;

            int res = material.compareTo(app.material);
            if(res != 0)
                return res;
        }

        if(numTextures != app.numTextures)
        {
            return numTextures < app.numTextures ? -1 : 1;
        }
        else
        {
            for(int i = 0; i < numTextures; i++)
            {
                if(textureUnits[i] != app.textureUnits[i])
                {
                    if(textureUnits[i] == null)
                        return -1;
                    else if(app.textureUnits[i] == null)
                        return 1;

                    int res = textureUnits[i].compareTo(app.textureUnits[i]);
                    if(res != 0)
                        return res;
                }
            }
        }

        if(polyAttr != app.polyAttr)
        {
            if(polyAttr == null)
                return -1;
            else if(app.polyAttr == null)
                return 1;

            int res = polyAttr.compareTo(app.polyAttr);
            if(res != 0)
                return res;
        }

        if(lineAttr != app.lineAttr)
        {
            if(lineAttr == null)
                return -1;
            else if(app.lineAttr == null)
                return 1;

            int res = lineAttr.compareTo(app.lineAttr);
            if(res != 0)
                return res;
        }

        if(pointAttr != app.pointAttr)
        {
            if(pointAttr == null)
                return -1;
            else if(app.pointAttr == null)
                return 1;

            int res = pointAttr.compareTo(app.pointAttr);
            if(res != 0)
                return res;
        }

        if(blendAttr != app.blendAttr)
        {
            if(blendAttr == null)
                return -1;
            else if(app.blendAttr == null)
                return 1;

            int res = blendAttr.compareTo(app.blendAttr);
            if(res != 0)
                return res;
        }

        if(shader != app.shader)
        {
            if(shader == null)
                return -1;
            else if(app.shader == null)
                return 1;

            int res = shader.compareTo(app.shader);
            if(res != 0)
                return res;
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param app The shape instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(OverrideAppearanceProxyRenderable app)
    {
        if(app == this)
            return true;

        if(app == null)
            return false;

        if((visible != app.visible) ||
           (numTextures != app.numTextures))
            return false;

        if((material != app.material) &&
           ((material == null) || !material.equals(app.material)))
            return false;

        for(int i = 0; i < numTextures; i++)
        {
            if((textureUnits[i] != app.textureUnits[i]) &&
               ((textureUnits[i] == null) || !textureUnits[i].equals(app.textureUnits[i])))
                return false;
        }

        if((polyAttr != app.polyAttr) &&
           ((polyAttr == null) || !polyAttr.equals(app.polyAttr)))
            return false;

        if((lineAttr != app.lineAttr) &&
           ((lineAttr == null) || !lineAttr.equals(app.lineAttr)))
            return false;

        if((pointAttr != app.pointAttr) &&
           ((pointAttr == null) || !pointAttr.equals(app.pointAttr)))
            return false;

        if((blendAttr != app.blendAttr) &&
           ((blendAttr == null) || !blendAttr.equals(app.blendAttr)))
            return false;

        if((shader != app.shader) &&
           ((shader == null) || !shader.equals(app.shader)))
            return false;

        return true;
    }
}
