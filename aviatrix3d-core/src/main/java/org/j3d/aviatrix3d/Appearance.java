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

package org.j3d.aviatrix3d;

// External imports
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

/**
 * Describes the appearance of an object.
 * <p>
 *
 * Attribute classes are used to control various visual details about the
 * object being rendered. Although a single instance of this class can take
 * all the attribute types at once, you'll need to consider what this object
 * is being used for. Most of the time you should only ever need to use one of
 * Point/Line/Polygon attributes.
 * <p>
 *
 * <b>Note:</b> If you have semi-transparent objects that need blending, you
 * do <i>not</i> need provide an instance of
 * {@link org.j3d.aviatrix3d.BlendAttributes}. Aviatrix3D will
 * internally handle the correct settings that you need, so there is no need
 * to provide your own. Only use it when you want to blend objects with
 * something different than the standard blending setup for non-transparent
 * and transparent objects as it will remove this object from the normal
 * transparency sorting processing and into a separate state.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.46 $
 */
public class Appearance extends NodeComponent
    implements AppearanceRenderable
{
    private static final String GET_TU_SIZE_PROP =
        "org.j3d.aviatrix3d.Appearance.getTexUnitSizeMsg";

    /** Listing of 32 texture IDs as Integers for the render() calls */
    private static final Integer[] TEX_IDS;

    /** Texture ID for only a single texture */
    private static final Integer SINGLE_TEXTURE;

    /** The material properites */
    private Material material;

    /** The texture properties */
    private TextureUnit[] textureUnits;

    /** Current number of valid textures */
    private int numTextures;

    /** The shader used by the appearance */
    private Shader shader;

    /** Attributes for rendering polygons */
    private PolygonAttributes polyAttr;

    /** Attributes for rendering lines */
    private LineAttributes lineAttr;

    /** Attributes for rendering points */
    private PointAttributes pointAttr;

    /** Attributes for blend management */
    private BlendAttributes blendAttr;

    /** Attributes for depth buffer management */
    private DepthAttributes depthAttr;

    /** Attributes for stencil buffer management */
    private StencilAttributes stencilAttr;

    /** Attributes for alpha testing management */
    private AlphaAttributes alphaAttr;

    /** Visibility flag for this object. If not visible, it's not rendered */
    private boolean visible;

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
     * The default constructor.
     */
    public Appearance()
    {
        visible = true;
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
        if(blendAttr != null || shader != null || alphaAttr != null)
            return false;

        if(material != null && material.hasTransparency())
            return true;

        boolean alpha_found = false;

        for(int j = 0; j < numTextures && !alpha_found; j++)
        {
            if(textureUnits[j] == null)
                continue;

            alpha_found = textureUnits[j].hasTransparency();
        }

        return alpha_found;
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
    @Override
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
    @Override
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
    @Override
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
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    @Override
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        if(material != null)
            material.setUpdateHandler(handler);

        if(blendAttr != null)
            blendAttr.setUpdateHandler(handler);

        if(alphaAttr != null)
            alphaAttr.setUpdateHandler(handler);

        if(polyAttr != null)
            polyAttr.setUpdateHandler(handler);

        if(lineAttr != null)
            lineAttr.setUpdateHandler(handler);

        if(pointAttr != null)
            pointAttr.setUpdateHandler(handler);

        if(depthAttr != null)
            depthAttr.setUpdateHandler(handler);

        if(stencilAttr != null)
            stencilAttr.setUpdateHandler(handler);

        for(int i = 0; i < numTextures; i++)
            textureUnits[i].setUpdateHandler(handler);

        if(shader != null)
            shader.setUpdateHandler(updateHandler);
    }

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * scene graph.
     *
     * @param state true if this should be marked as live now
     */
    @Override
    protected void setLive(boolean state)
    {
        if(state)
            liveCount++;
        else if(liveCount > 0)
            liveCount--;

        if((liveCount == 0) || !alive)
        {
            super.setLive(state);

            if(numTextures != 0)
            {
                for(int i = 0; i < numTextures; i++)
                    textureUnits[i].setLive(state);
            }

            if(polyAttr != null)
                polyAttr.setLive(state);

            if(lineAttr != null)
                lineAttr.setLive(state);

            if(pointAttr != null)
                pointAttr.setLive(state);

            if(alphaAttr != null)
                alphaAttr.setLive(state);

            if(blendAttr != null)
                blendAttr.setLive(state);

            if(depthAttr != null)
                depthAttr.setLive(state);

            if(stencilAttr != null)
                stencilAttr.setLive(state);

            if(material != null)
                material.setLive(state);

            if(shader != null)
                shader.setLive(state);
        }
    }

    /**
     * Check to see if this node is the same reference as the passed node that
     * is a parent of this node. This is the downwards check to ensure that
     * there is no cyclic scene graph structures at the point where someone
     * adds a node to the scenegraph. When the reference and this are the
     * same, an exception is generated. Since each class may have different
     * lists of child node setups, this should be overriden by any class that
     * can take children, and have the call passed along to the children.
     *
     * @param parent The reference to check against this class
     * @throws CyclicSceneGraphStructureException Equal parent and child
     */
    @Override
    protected void checkForCyclicChild(SceneGraphObject parent)
        throws CyclicSceneGraphStructureException
    {
        if(parent == this)
            throw new CyclicSceneGraphStructureException();

        for(int i = 0; i < numTextures; i++)
            textureUnits[i].checkForCyclicChild(parent);

        // Don't bother with anything else for now. That's just wasted CPU cycles.
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
        Appearance app = (Appearance)o;
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
        if(!(o instanceof Appearance))
            return false;
        else
            return equals((Appearance)o);
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Set the visibility state of any geometry associated with this instance.
     * This can only be set during the data-changed callback as it only effects
     * visual state and not rendering state. An object that is not visible is
     * still valid for the bounds, which means it can be picked etc. That is,
     * making something invisible does not effect the bounds of the parent
     * objects.
     *
     * @param state true to make the object visible
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setVisible(boolean state)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        visible = state;
    }

    /**
     * Set the material to use. Null will clear the material.
     *
     * @param mat The new material
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setMaterial(Material mat)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(material != null)
            material.setLive(false);

        material = mat;

        if(material != null)
        {
            material.setUpdateHandler(updateHandler);
            material.setLive(alive);
        }
    }

    /**
     * Get the current material in use.
     *
     * @return The current material instance or null
     */
    public Material getMaterial()
    {
        return material;
    }

    /**
     * Set the polygon rendering attributes to use. Null will clear the
     * current attributes.
     *
     * @param attr The new attributes or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setPolygonAttributes(PolygonAttributes attr)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(polyAttr != null)
            polyAttr.setLive(false);

        polyAttr = attr;

        if(polyAttr != null)
        {
            polyAttr.setUpdateHandler(updateHandler);
            polyAttr.setLive(alive);
        }
    }

    /**
     * Get the current polygon rendering attributes in use.
     *
     * @return The current attributes instance or null
     */
    public PolygonAttributes getPolygonAttributes()
    {
        return polyAttr;
    }

    /**
     * Set the line rendering attributes to use. Null will clear the
     * current attributes.
     *
     * @param attr The new attributes or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setLineAttributes(LineAttributes attr)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(lineAttr != null)
            lineAttr.setLive(false);

        lineAttr = attr;

        if(lineAttr != null)
        {
            lineAttr.setUpdateHandler(updateHandler);
            lineAttr.setLive(alive);
        }
    }

    /**
     * Get the current line rendering attributes in use.
     *
     * @return The current attributes instance or null
     */
    public LineAttributes getLineAttributes()
    {
        return lineAttr;
    }

    /**
     * Set the point rendering attributes to use. Null will clear the
     * current attributes.
     *
     * @param attr The new attributes or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setPointAttributes(PointAttributes attr)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(pointAttr != null)
            pointAttr.setLive(false);

        pointAttr = attr;

        if(pointAttr != null)
        {
            pointAttr.setUpdateHandler(updateHandler);
            pointAttr.setLive(alive);
        }
    }

    /**
     * Get the current pointgon rendering attributes in use.
     *
     * @return The current attributes instance or null
     */
    public PointAttributes getPointAttributes()
    {
        return pointAttr;
    }

    /**
     * Set the blend rendering attributes to use. Null will clear the
     * current attributes.
     * <p>
     * <b>Note</b> Setting a value will disable state sorting for
     * transparency on this object.
     *
     * @param attr The new attributes or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setBlendAttributes(BlendAttributes attr)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(blendAttr != null)
            blendAttr.setLive(false);

        blendAttr = attr;

        if(blendAttr != null)
        {
            blendAttr.setUpdateHandler(updateHandler);
            blendAttr.setLive(alive);
        }
    }

    /**
     * Get the current blend rendering attributes in use.
     *
     * @return The current attributes instance or null
     */
    public BlendAttributes getBlendAttributes()
    {
        return blendAttr;
    }

    /**
     * Set the alpha rendering attributes to use. Null will clear the
     * current attributes.
     * <p>
     * <b>Note</b> Setting a value will disable state sorting for
     * transparency on this object.
     *
     * @param attr The new attributes or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setAlphaAttributes(AlphaAttributes attr)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(alphaAttr != null)
            alphaAttr.setLive(false);

        alphaAttr = attr;

        if(alphaAttr != null)
        {
            alphaAttr.setUpdateHandler(updateHandler);
            alphaAttr.setLive(alive);
        }
    }

    /**
     * Get the current alpha rendering attributes in use.
     *
     * @return The current attributes instance or null
     */
    public AlphaAttributes getAlphaAttributes()
    {
        return alphaAttr;
    }

    /**
     * Set the depth buffer attributes to use. Null will clear the
     * current attributes.
     *
     * @param attr The new attributes or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setDepthAttributes(DepthAttributes attr)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(depthAttr != null)
            depthAttr.setLive(false);

        depthAttr = attr;

        if(depthAttr != null)
        {
            depthAttr.setUpdateHandler(updateHandler);
            depthAttr.setLive(alive);
        }
    }

    /**
     * Get the current depth buffer attributes in use.
     *
     * @return The current attributes instance or null
     */
    public DepthAttributes getDepthAttributes()
    {
        return depthAttr;
    }

    /**
     * Set the stencil buffer attributes to use. Null will clear the
     * current attributes.
     *
     * @param attr The new attributes or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setStencilAttributes(StencilAttributes attr)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(stencilAttr != null)
            stencilAttr.setLive(false);

        stencilAttr = attr;

        if(stencilAttr != null)
        {
            stencilAttr.setUpdateHandler(updateHandler);
            stencilAttr.setLive(alive);
        }
    }

    /**
     * Get the current stencil buffer attributes in use.
     *
     * @return The current attributes instance or null
     */
    public StencilAttributes getStencilAttributes()
    {
        return stencilAttr;
    }

    /**
     * Set the shader to use. Null will clear the current shader.
     *
     * @param s The new shader instance to use or null
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setShader(Shader s)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(shader != null)
            shader.setLive(false);

        shader = s;

        if(shader != null)
        {
            shader.setUpdateHandler(updateHandler);
            shader.setLive(alive);
        }
    }

    /**
     * Get the current shader in use.
     *
     * @return The current shader instance or null
     */
    public Shader getShader()
    {
        return shader;
    }

    /**
     * Set the texture units to use.  Null will disable texturing.
     *
     * @param texUnits The new Texture lists to set
     * @param num The number of valid entries in the array
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setTextureUnits(TextureUnit[] texUnits, int num)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if((textureUnits == null) || (textureUnits.length < num))
            textureUnits = new TextureUnit[num];

        // clear out remaining pointers
        for(int i = numTextures; i < num; i++)
            textureUnits[i] = null;

        for(int i = 0; i < num; i++)
        {
            textureUnits[i] = texUnits[i];
            textureUnits[i].setUpdateHandler(updateHandler);
        }

        numTextures = num;
    }

    /**
     * Get the current number of texture units that are valid
     *
     * @return a positive number
     */
    public int numTextureUnits()
    {
        return numTextures;
    }

    /**
     * Get the texture units used by the material.
     *
     * @param texUnits An array to copy the texture units into
     * @throws IllegalArgumentException The provided array is not big enough
     *   to hold all the texture units
     */
    public void getTextureUnits(TextureUnit[] texUnits)
    {
        if(texUnits.length < numTextures)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(GET_TU_SIZE_PROP);
            throw new IllegalArgumentException(msg);
        }

        // TODO: Revist using System.arraycopy as average number of texUnits goes up
        for(int i = 0; i < numTextures; i++) {
            texUnits[i] = textureUnits[i];
        }
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param app The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Appearance app)
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
     * @param app The appearance instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(Appearance app)
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
