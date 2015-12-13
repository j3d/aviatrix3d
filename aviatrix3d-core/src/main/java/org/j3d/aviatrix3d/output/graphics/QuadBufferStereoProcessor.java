/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2009
 *                       j3d.org Copyright(c) 2009-2012
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
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawable;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

import org.j3d.aviatrix3d.pipeline.RenderOp;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsEnvironmentData;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsProfilingData;

/**
 * Handles the rendering for a single output device using quad-buffered stereo
 * techniques.
 * <p>
 * The code expects that everything is set up before each call of the display()
 * callback. It does not handle any recursive rendering requests as that is
 * assumed to have been sorted out before calling this renderer.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 3.25 $
 */
public class QuadBufferStereoProcessor extends BaseStereoProcessor
{
    /**
     * Construct handler for rendering objects to the main screen.
     *
     * @param context The context that this processor is working on
     * @param owner The owning device of this processor
     */
    public QuadBufferStereoProcessor(GLContext context,
                                     GraphicsOutputDevice owner)
    {
        super(context, owner);
    }

    //---------------------------------------------------------------
    // Methods defined by BaseRenderingProcessor
    //---------------------------------------------------------------

    /**
     * Called by the drawable to perform rendering by the client.
     *
     * @param profilingData The timing and load data
     */
    @Override
    public void display(GraphicsProfilingData profilingData)
    {
        GL base_gl = localContext.getGL();

        if(base_gl == null)
            return;

        GL2 gl = base_gl.getGL2();

        processRequestData(gl);

        if(terminate)
            return;

        // Draw the left eye first, then right
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glDrawBuffer(GL2.GL_BACK_LEFT);

        if(terminate)
            return;

        render(gl, true, profilingData);

        if(terminate)
            return;

        // Draw the left eye first, then right
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glDrawBuffer(GL2.GL_BACK_RIGHT);

        if(terminate)
            return;

        render(gl, false, profilingData);
    }

    /**
     * Setup the viewport environment to be drawn, but do not yet set up the
     * viewpoint and other per-layer-specific effects. If a viewport has
     * multiple layers, then each layer could potentially have a different
     * viewpoint etc.
     *
     * @param gl The gl context to draw with
     * @param data The view environment information to setup
     */
    @Override
    protected void setupViewport(GL2 gl, GraphicsEnvironmentData data)
    {
        if(!data.useStereo)
        {
            super.setupViewport(gl, data);
        }
        else
        {
            gl.glViewport(data.viewport[GraphicsEnvironmentData.VIEW_X],
                          data.viewport[GraphicsEnvironmentData.VIEW_Y],
                          data.viewport[GraphicsEnvironmentData.VIEW_WIDTH],
                          data.viewport[GraphicsEnvironmentData.VIEW_HEIGHT]);
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Perform the rendering loop for one of the two buffers, indicated by
     * the provided parameter.
     *
     * @param gl The gl context to draw with
     * @param left true if this is the left eye
     */
    private void render(GL2 gl, boolean left, GraphicsProfilingData profilingData)
    {
        // TODO:
        // May want to put some optimisations here for systems that can clear
        // both back buffers at once with a single call to
        // glDrawBuffer(GL_BACK) before the glClear();
        if(alwaysLocalClear)
        {
            // Need to reset the viewport back to full window size when we
            // clear because we never unset the viewport in the previous
            // frame, resulting in everything other than this one not being
            // cleared.
            GLDrawable drawable = localContext.getGLDrawable();

            int w = drawable.getSurfaceWidth();
            int h = drawable.getSurfaceHeight();

            gl.glViewport(0, 0, w, h);
            gl.glScissor(0, 0, w, h);

            gl.glClearColor(clearColor[0],
                            clearColor[1],
                            clearColor[2],
                            clearColor[3]);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        }

        float alpha_test = alphaCutoff;
        boolean two_pass_transparent = useTwoPassTransparent;
        boolean first_pass_alpha = true;
        int transparent_start_idx = 0;


        ObjectRenderable obj;
        ComponentRenderable comp;
        BufferStateRenderable buffer;
        GraphicsEnvironmentData data = null;
        int data_index = 0;
        int layer_data_index = 0;
        int mp_data_index = 0;
        int clear_buffer_bits = 0;
        boolean fog_active = false;
        boolean first_layer = true;
        ObjectRenderable current_fog = null;

        for(int i = 0; i < numRenderables && !terminate; i++)
        {
            switch(operationList[i])
            {
                case RenderOp.START_MULTIPASS:
                    data = environmentList[data_index];

                    // If this is not the first layer, render to one of the
                    // auxillary buffers and have that copy back to the main
                    // buffer when the layer is finished.
                    clear_buffer_bits = 0;
                    if(!first_layer)
                    {
                        gl.glDrawBuffer(GL2.GL_AUX0);
                        gl.glReadBuffer(GL2.GL_AUX0);

                        setupMultipassViewport(gl, data);
                    }
                    break;

                case RenderOp.STOP_MULTIPASS:
                    // If not the first layer, copy everything back and then
                    // reset the drawing and read layers back to the normal
                    // rendering setup.
                    if(!first_layer)
                    {
                        gl.glDrawBuffer(GL.GL_BACK);
                        gl.glRasterPos2i(data.viewport[GraphicsEnvironmentData.VIEW_X],
                                         data.viewport[GraphicsEnvironmentData.VIEW_Y]);

                        gl.glCopyPixels(0,
                                        0,
                                        data.viewport[GraphicsEnvironmentData.VIEW_WIDTH],
                                        data.viewport[GraphicsEnvironmentData.VIEW_HEIGHT],
                                        GL2.GL_COLOR);
                        gl.glReadBuffer(GL.GL_BACK);
                    }
                    break;

                case RenderOp.START_MULTIPASS_PASS:
                    if(clear_buffer_bits != 0)
                        gl.glClear(clear_buffer_bits);

                    data = environmentList[data_index];
                    mp_data_index = data_index;
                    data_index++;

                    preMPPassEnvironmentDraw(gl, data);
                    break;

                case RenderOp.STOP_MULTIPASS_PASS:
                    data = environmentList[mp_data_index];
                    postMPPassEnvironmentDraw(gl, data);
                    break;

                case RenderOp.SET_VIEWPORT_STATE:
                    ((ViewportRenderable)renderableList[i].renderable).render(gl);
                    break;

                case RenderOp.STOP_VIEWPORT_STATE:
                    data = environmentList[mp_data_index];
                    setupMultipassViewport(gl, data);
                    break;

                case RenderOp.START_BUFFER_STATE:
                    buffer = (BufferStateRenderable)renderableList[i].renderable;
                    buffer.setBufferState(gl);

                    if(buffer.checkClearBufferState())
                        clear_buffer_bits |= buffer.getBufferBitMask();
                    break;

                case RenderOp.SET_BUFFER_CLEAR:
                    buffer = (BufferStateRenderable)renderableList[i].renderable;

                    if(buffer.checkClearBufferState())
                        clear_buffer_bits |= buffer.getBufferBitMask();
                    else
                        clear_buffer_bits &= ~buffer.getBufferBitMask();
                    break;

                case RenderOp.CHANGE_BUFFER_STATE:
                    buffer = (BufferStateRenderable)renderableList[i].renderable;
                    buffer.updateBufferState(gl);

                    if(buffer.checkClearBufferState())
                        clear_buffer_bits |= buffer.getBufferBitMask();
                    else
                        clear_buffer_bits &= ~buffer.getBufferBitMask();
                    break;

                case RenderOp.STOP_BUFFER_STATE:
                    buffer = (BufferStateRenderable)renderableList[i].renderable;
                    buffer.clearBufferState(gl);
                    clear_buffer_bits &= ~buffer.getBufferBitMask();
                    break;

                case RenderOp.START_LAYER:
                    gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
                    data = environmentList[data_index];
                    layer_data_index = data_index;
                    data_index++;

                    fog_active = data.fog != null;
                    current_fog = data.fog;

                    preLayerEnvironmentDraw(gl, data, left);
                    break;

                case RenderOp.STOP_LAYER:
                    data = environmentList[layer_data_index];

                    postLayerEnvironmentDraw(gl, data, profilingData);
                    fog_active = false;
                    first_layer = false;
                    break;

                case RenderOp.START_VIEWPORT:
                    data = environmentList[data_index];
                    // EMF: there might be multiple layers per viewport;
                    // we increment data_index within START_LAYER

                    setupViewport(gl, data);
                    break;

                case RenderOp.STOP_VIEWPORT:
                    break;

                case RenderOp.START_RENDER:
                    // load the matrix to render
                    gl.glPushMatrix();
                    gl.glMultMatrixd(renderableList[i].transform, 0);
                    obj = (ObjectRenderable)renderableList[i].renderable;
                    obj.render(gl);
                    break;

                case RenderOp.STOP_RENDER:
                    obj = (ObjectRenderable)renderableList[i].renderable;
                    obj.postRender(gl);
                    gl.glPopMatrix();
                    break;

                case RenderOp.RENDER_GEOMETRY:
                    // load the matrix to render
                    gl.glPushMatrix();
                    gl.glMultMatrixd(renderableList[i].transform, 0);
                    ((GeometryRenderable)renderableList[i].renderable).render(gl);
                    gl.glPopMatrix();
                    break;

                case RenderOp.RENDER_GEOMETRY_2D:
                    // load the matrix to render
                    gl.glRasterPos2d(renderableList[i].transform[3],
                                     renderableList[i].transform[7]);
                    gl.glPixelZoom((float)renderableList[i].transform[0],
                                   (float)renderableList[i].transform[5]);
                    ((GeometryRenderable)renderableList[i].renderable).render(gl);
                    gl.glPopMatrix();
                    break;

                case RenderOp.RENDER_CUSTOM_GEOMETRY:
                    // load the matrix to render
                    gl.glPushMatrix();
                    gl.glMultMatrixd(renderableList[i].transform, 0);
                    CustomGeometryRenderable gr =
                        (CustomGeometryRenderable)renderableList[i].renderable;
                    gr.render(gl, renderableList[i].instructions);
                    gl.glPopMatrix();
                    break;

                case RenderOp.RENDER_CUSTOM:
                    // load the matrix to render
                    gl.glPushMatrix();
                    gl.glMultMatrixd(renderableList[i].transform, 0);

                    CustomRenderable cr =
                        (CustomRenderable)renderableList[i].renderable;
                    cr.render(gl, renderableList[i].instructions);
                    gl.glPopMatrix();
                    break;

                case RenderOp.START_STATE:
                    obj = (ObjectRenderable)renderableList[i].renderable;
                    obj.render(gl);
                    break;

                case RenderOp.STOP_STATE:
                    obj = (ObjectRenderable)renderableList[i].renderable;
                    obj.postRender(gl);
                    break;

                case RenderOp.START_LIGHT:
                    // Get the next available light ID
                    if(lastLightIdx >= availableLights.length)
                        continue;

                    Integer l_id = availableLights[lastLightIdx++];
                    lightIdMap.put(renderableList[i].id, l_id);

                    // load the matrix to render
                    gl.glPushMatrix();
                    gl.glMultMatrixd(renderableList[i].transform, 0);
                    comp = (ComponentRenderable)renderableList[i].renderable;
                    comp.render(gl, l_id);
                    gl.glPopMatrix();
                    break;

                case RenderOp.STOP_LIGHT:
                    if(lastLightIdx >= availableLights.length)
                        continue;

                    l_id = (Integer)lightIdMap.remove(renderableList[i].id);

                    comp = (ComponentRenderable)renderableList[i].renderable;
                    comp.postRender(gl, l_id);
                    availableLights[--lastLightIdx] = l_id;
                    break;

                case RenderOp.START_CLIP_PLANE:
                    // Get the next available clip plane ID
                    if(lastClipIdx >= availableClips.length)
                        continue;

                    Integer c_id = availableClips[lastClipIdx++];
                    clipIdMap.put(renderableList[i].id, c_id);

                    // load the matrix to render
                    gl.glPushMatrix();
                    gl.glMultMatrixd(renderableList[i].transform, 0);

                    comp = (ComponentRenderable)renderableList[i].renderable;
                    comp.render(gl, c_id);
                    gl.glPopMatrix();
                    break;

                case RenderOp.STOP_CLIP_PLANE:
                    if(lastClipIdx >= availableClips.length)
                        continue;

                    c_id = (Integer)clipIdMap.remove(renderableList[i].id);

                    comp = (ComponentRenderable)renderableList[i].renderable;
                    comp.postRender(gl, c_id);

                    availableClips[--lastClipIdx] = c_id;
                    break;

                case RenderOp.START_TRANSPARENT:
                    if(first_pass_alpha && two_pass_transparent)
                    {
                        gl.glEnable(GL2.GL_ALPHA_TEST);
                        gl.glAlphaFunc(GL.GL_GEQUAL, alpha_test);
                        transparent_start_idx = i;
                    }
                    else
                    {
                        gl.glDepthMask(false);
                        gl.glEnable(GL.GL_BLEND);
                        gl.glBlendFunc(GL.GL_SRC_ALPHA,
                                       GL.GL_ONE_MINUS_SRC_ALPHA);
                    }
                    break;

                case RenderOp.STOP_TRANSPARENT:
                    if(first_pass_alpha && two_pass_transparent)
                    {
                        // if this is the end of the first pass, reset the
                        // loop index back to the start of the transparent
                        // list and cycle through again, but this time with
                        // the blend function enabled.
                        first_pass_alpha = false;
                        i = transparent_start_idx - 1;

                        gl.glDisable(GL2.GL_ALPHA_TEST);
                    }
                    else
                    {
                        gl.glDisable(GL.GL_BLEND);
                        gl.glDepthMask(true);
                    }
                    break;

                case RenderOp.START_FOG:
                    if(!fog_active)
                    {
                        gl.glEnable(GL2.GL_FOG);
                        fog_active = true;
                    }

                    obj = (ObjectRenderable)renderableList[i].renderable;
                    obj.render(gl);
                    break;

                case RenderOp.STOP_FOG:
                    if(current_fog != null)
                        current_fog.render(gl);
                    else
                    {
                        obj = (ObjectRenderable)renderableList[i].renderable;
                        obj.postRender(gl);
                        fog_active = false;
                        gl.glDisable(GL2.GL_FOG);
                    }
                    break;

                case RenderOp.START_SHADER_PROGRAM:
                    ShaderComponentRenderable prog =
                        (ShaderComponentRenderable)renderableList[i].renderable;

                    if(!prog.isValid(gl))
                    {
                        currentShaderProgramId = INVALID_SHADER;
                        continue;
                    }

// TODO: Optimise this to avoid the allocation. Use IntHashMap for lookup.
                    currentShaderProgramId = new Integer(prog.getProgramId(gl));
                    prog.render(gl);
                    break;

                case RenderOp.STOP_SHADER_PROGRAM:
                    if(currentShaderProgramId == INVALID_SHADER)
                        continue;

                    obj = (ObjectRenderable)renderableList[i].renderable;
                    obj.postRender(gl);

                    currentShaderProgramId = INVALID_SHADER;
                    break;

                case RenderOp.SET_SHADER_ARGS:
                    if(currentShaderProgramId == INVALID_SHADER)
                        continue;

                    comp = (ComponentRenderable)renderableList[i].renderable;
                    comp.render(gl, currentShaderProgramId);
                    break;

                 case RenderOp.START_TEXTURE:
                    TextureRenderable texcomp =
                        (TextureRenderable)renderableList[i].renderable;
                    Integer id = (Integer)(renderableList[i].instructions);

                    texcomp.activateTexture(gl, id);
                    if(texcomp.isOffscreenBuffer())
                    {
                        BaseBufferDescriptor desc =
                            (BaseBufferDescriptor)texcomp.getBuffer(localContext);

                        // Will be null if the upstream cull stage has
                        // offscreen search disabled.
                        if(desc != null)
                            desc.bindBuffer(localContext);
                    }

                    texcomp.render(gl, id);
                    break;

                case RenderOp.STOP_TEXTURE:
                    texcomp = (TextureRenderable)renderableList[i].renderable;
                    id = (Integer)(renderableList[i].instructions);
                    texcomp.postRender(gl, id);

                    if(texcomp.isOffscreenBuffer())
                    {
                        BaseBufferDescriptor desc =
                            (BaseBufferDescriptor)texcomp.getBuffer(localContext);

                        // Will be null if the upstream cull stage has
                        // offscreen search disabled.
                        if(desc != null)
                            desc.unbindBuffer(localContext);
                    }
                    texcomp.deactivateTexture(gl, id);
                    break;
           }
        }

        if(terminate)
            return;

        gl.glFlush();
    }
}
