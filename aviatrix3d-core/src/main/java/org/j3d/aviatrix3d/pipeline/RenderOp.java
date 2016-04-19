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
public enum RenderOp
{
    /** The unknown/general request to push state onto the stack */
    START_RENDER,

    /** The unknown/general request to pop state off the stack */
    STOP_RENDER,

    /** The unknown/general 2D request to push state onto the stack */
    START_RENDER_2D,

    /** The unknown/general 2D request to pop state off the stack */
    STOP_RENDER_2D,

    /**
     * Render a geometry item. Since this is a terminal for the OpenGL pipeline
     * state, no postRender call will be made.
     */
    RENDER_GEOMETRY,

    /**
     * Render a 2D geometry item. Since this is a terminal for the OpenGL pipeline
     * state, no postRender call will be made. Also, instead of using glMultMatrix
     * it will set glRasterPos and glPixelZoom from the provided matrix.
     */
    RENDER_GEOMETRY_2D,

    /**
     * Render a custom geometry item. This is an alternate terminal for the OpenGL
     * pipeline state for geometry types that may have done their own internal
     * custom behaviour (eg distance sorting or billboarding), no postRender call
     * will be made.
     */
    RENDER_CUSTOM_GEOMETRY,

    /**
     * A node implementing the
     * {@link org.j3d.aviatrix3d.rendering.CustomRenderable}
     * interface will be rendered. Needs to pass in the external instruction
     * setup.
     */
    RENDER_CUSTOM,

    /** The node component should push its state onto the stack */
    START_STATE,

    /** The node component should remove its state from the stack */
    STOP_STATE,

    /** A light should push its state onto the stack */
    START_LIGHT,

    /** A light is removing its state from the stack */
    STOP_LIGHT,

    /** Start of a collection of transparent geometry */
    START_TRANSPARENT,

    /** End of a collection of transparent geometry */
    STOP_TRANSPARENT,

    /** Start of a collection of objects that are used for shadows */
    START_SHADOW,

    /** End of a collection of objects that are used for shadows */
    STOP_SHADOW,

    /** Start of a collection of objects that are shadow generators */
    START_SHADOW_GENERATOR,

    /** End of a collection of objects that are shadow generators */
    STOP_SHADOW_GENERATOR,

    /** A clip plane should push its state onto the stack */
    START_CLIP_PLANE,

    /** A clip plane is removing its state from the stack */
    STOP_CLIP_PLANE,

    /** A local fog should push its state onto the stack */
    START_FOG,

    /** A local fog is removing its state from the stack, restore global fog. */
    STOP_FOG,

    /** Start a GLSLang shader program now */
    START_SHADER_PROGRAM,

    /** Stop using a GLSLang shader programs */
    STOP_SHADER_PROGRAM,

    /** Set a collection of shader arguments */
    SET_SHADER_ARGS,

    /**
     * Turn on a texture unit stage for rendering. The render instruction
     * will be accompanied by an Integer instance that holds the stage ID
     * to be used for this unit as it needs to be rendered right now.
     */
    START_TEXTURE,

    /**
     * Turn off a texture unit stage for rendering. See START_TEXTURE for
     * the details.
     */
    STOP_TEXTURE,

    /**
     * Start of a new layer. Use this to clear any buffers that may be needed
     * for this layer and begin the rendering process for the next layer.
     */
    START_LAYER,

    /** End of a layer. Clean up any post-compositing work */
    STOP_LAYER,

    /** Resize the viewport to a new size. */
    START_VIEWPORT,

    /** End of the current viewport. */
    STOP_VIEWPORT,

    /** Start a multipass process */
    START_MULTIPASS,

    /** Start a single pass of the multipass process */
    START_MULTIPASS_PASS,

    /** Start a buffer state change */
    START_BUFFER_STATE,

    /** Set the buffer clear bit, but don't run the render method */
    SET_BUFFER_CLEAR,

    /** Take an existing active buffer state and change it. */
    CHANGE_BUFFER_STATE,

    /** End using a particular buffer state, turning it off. */
    STOP_BUFFER_STATE,

    /** Stop a single pass of the multipass process */
    STOP_MULTIPASS_PASS,

    /** Stop a multipass rendering */
    STOP_MULTIPASS,

    /** Set up a custom viewport for this multipass pass */
    SET_VIEWPORT_STATE,

    /**
     * Stop a custom viewport for this multipass pass and return to the
     * parent viewport state from the containing scene.
     */
    STOP_VIEWPORT_STATE
}
