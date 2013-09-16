/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.rendering;

// External imports
// None

// Local imports
// None

/**
 * A renderable object that visual part of a {@link ShapeRenderable} that
 * provides the visual attributes to render the geometry with.
 * <p>
 *
 * <b>Implementation Nodes</b>
 * <p>
 *
 * When deciding whether an object has transparency defined, the following
 * factors should be considered:
 *
 * <ul>
 * <li>BlendAttributes defined means it will go into the non-transparent
 *     (opaque) bucket and subject to normal state sorting.
 * </li>
 * <li>Shader of any sort will be responsible for its own blending, so
 *     is treated as opaque.
 * </li>
 * <li>Texture formats containing any form of transparency (ALPHA,
 *     INTENSITY_ALPHA, RGBA, ARGB etc).
 * </li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.6 $
 */
public interface AppearanceRenderable extends ObjectRenderable
{
    /**
     * State check to see whether the shape in it's current setup
     * is visible. Various reasons for this are stated in the class docs.
     *
     * @return true if the shape has something to render
     */
    public boolean isVisible();

    /**
     * Ask the appearance if it has any transparency values. The implementation
     * should determine this from it's internal set of state, such as looking
     * at material properties, texture formats etc.
     *
     * @return true if any form of non-opaque rendering is defined
     */
    public boolean hasTransparency();

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
    public boolean hasTransparencyInfo();

    /**
     * Fetch the sub-renderable for the given type. This can then be cast up
     * to the appropriate extended Renderable type.
     *
     * @param attrib The attribute type identifier from
     *   {@link AppearanceAttributeRenderable}
     * @return A renderable for that attribute if one is set, or null
     */
    public AppearanceAttributeRenderable getAttributeRenderable(int attrib);

    /**
     * Request the number of texture renderables that are available to
     * process in this node. OpenGL allows a maximum of 32 textures to be
     * defined.
     *
     * @return A number between 0 and 32
     */
    public int numTextureRenderables();

    /**
     * Request the renderable for the given texture unit number. If there is
     * no renderable for that number, return null.
     *
     * @param unitNumber The number of the texture unit to fetch
     * @return The matching texture unit renderable or null if not available
     */
    public TextureRenderable getTextureRenderable(int unitNumber);

    /**
     * Fetch the renderable that corresponds to the set programmable shader.
     * If no shader is set, return null.
     *
     * @return The current shader renderable or null
     */
    public ShaderRenderable getShaderRenderable();

    /**
     * Fetch the renderable that corresponds to material properties. If none is
     * set or valid for this visual appearance, return null
     *
     * @return The current shader renderable or null
     */
    public TransparentObjectRenderable getMaterialRenderable();
}
