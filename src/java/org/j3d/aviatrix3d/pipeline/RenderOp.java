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

package org.j3d.aviatrix3d.pipeline;

// External imports
// None

// Local imports
// None

/**
 * Constants used to define render operations as the output of the sort stage.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.9 $
 */
public interface RenderOp
{
    /** The unknown/general request to push state onto the stack */
    public static final int START_RENDER = 1;

    /** The unknown/general request to pop state off the stack */
    public static final int STOP_RENDER = 2;

    /** The unknown/general 2D request to push state onto the stack */
    public static final int START_RENDER_2D = 3;

    /** The unknown/general 2D request to pop state off the stack */
    public static final int STOP_RENDER_2D = 4;

    /**
     * Render a geometry item. Since this is a terminal for the OpenGL pipeline
     * state, no postRender call will be made.
     */
    public static final int RENDER_GEOMETRY = 5;

    /**
     * Render a 2D geometry item. Since this is a terminal for the OpenGL pipeline
     * state, no postRender call will be made. Also, instead of using glMultMatrix
     * it will set glRasterPos and glPixelZoom from the provided matrix.
     */
    public static final int RENDER_GEOMETRY_2D = 6;

    /**
     * Render a custom geometry item. This is an alternate terminal for the OpenGL
     * pipeline state for geometry types that may have done their own internal
     * custom behaviour (eg distance sorting or billboarding), no postRender call
     * will be made.
     */
    public static final int RENDER_CUSTOM_GEOMETRY = 7;

    /**
     * A node implementing the
     * {@link org.j3d.aviatrix3d.rendering.CustomRenderable}
     * interface will be rendered. Needs to pass in the external instruction
     * setup.
     */
    public static final int RENDER_CUSTOM = 9;

    /** The node component should push its state onto the stack */
    public static final int START_STATE = 10;

    /** The node component should remove its state from the stack */
    public static final int STOP_STATE = 11;

    /** A light should push its state onto the stack */
    public static final int START_LIGHT = 12;

    /** A light is removing its state from the stack */
    public static final int STOP_LIGHT = 13;

    /** Start of a collection of transparent geometry */
    public static final int START_TRANSPARENT = 14;

    /** End of a collection of transparent geometry */
    public static final int STOP_TRANSPARENT = 15;

    /** Start of a collection of objects that are used for shadows */
    public static final int START_SHADOW = 16;

    /** End of a collection of objects that are used for shadows */
    public static final int STOP_SHADOW = 17;

    /** Start of a collection of objects that are shadow generators */
    public static final int START_SHADOW_GENERATOR = 18;

    /** End of a collection of objects that are shadow generators */
    public static final int STOP_SHADOW_GENERATOR = 19;

    /** A clip plane should push its state onto the stack */
    public static final int START_CLIP_PLANE = 20;

    /** A clip plane is removing its state from the stack */
    public static final int STOP_CLIP_PLANE = 21;

    /** A local fog should push its state onto the stack */
    public static final int START_FOG = 22;

    /** A local fog is removing its state from the stack, restore global fog. */
    public static final int STOP_FOG = 23;

    /** Start a GLSLang shader program now */
    public static final int START_SHADER_PROGRAM = 24;

    /** Stop using a GLSLang shader programs */
    public static final int STOP_SHADER_PROGRAM = 25;

    /** Set a collection of shader arguments */
    public static final int SET_SHADER_ARGS = 26;

    /**
     * Turn on a texture unit stage for rendering. The render instruction
     * will be accompanied by an Integer instance that holds the stage ID
     * to be used for this unit as it needs to be rendered right now.
     */
    public static final int START_TEXTURE = 27;

    /**
     * Turn off a texture unit stage for rendering. See START_TEXTURE for
     * the details.
     */
    public static final int STOP_TEXTURE = 28;

    /**
     * Start of a new layer. Use this to clear any buffers that may be needed
     * for this layer and begin the rendering process for the next layer.
     */
    public static final int START_LAYER = 29;

    /** End of a layer. Clean up any post-compositing work */
    public static final int STOP_LAYER = 30;

    /** Resize the viewport to a new size. */
    public static final int START_VIEWPORT = 31;

    /** End of the current viewport. */
    public static final int STOP_VIEWPORT = 32;

    /** Start a multipass process */
    public static final int START_MULTIPASS = 33;

    /** Start a single pass of the multipass process */
    public static final int START_MULTIPASS_PASS = 34;

    /** Start a buffer state change */
    public static final int START_BUFFER_STATE = 35;

    /** Set the buffer clear bit, but don't run the render method */
    public static final int SET_BUFFER_CLEAR = 36;

    /** Take an existing active buffer state and change it. */
    public static final int CHANGE_BUFFER_STATE = 37;

    /** End using a particular buffer state, turning it off. */
    public static final int STOP_BUFFER_STATE = 38;

    /** Stop a single pass of the multipass process */
    public static final int STOP_MULTIPASS_PASS = 39;

    /** Stop a multipass rendering */
    public static final int STOP_MULTIPASS = 40;

    /** Set up a custom viewport for this multipass pass */
    public static final int SET_VIEWPORT_STATE = 41;

    /**
     * Stop a custom viewport for this multipass pass and return to the
     * parent viewport state from the containing scene.
     */
    public static final int STOP_VIEWPORT_STATE = 42;
}
