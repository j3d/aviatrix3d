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

package org.j3d.aviatrix3d.pipeline.graphics;

// External imports
import java.text.MessageFormat;
import java.util.Locale;
import java.util.HashMap;
import java.util.Stack;

import com.jogamp.opengl.GL;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.util.HashSet;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

import org.j3d.aviatrix3d.pipeline.RenderOp;

/**
 * Implementation of common functionality used by sort stage that implement
 * state sorting.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidSortableMsg: Error message when a non Shape/Custom renderable
 *     manages to make it through the pipeline to here.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 3.12 $
 */
public abstract class BaseStateSortStage extends BaseSortStage
{
    /** Message for an invalid sortable */
    private static final String INVALID_SORTABLE_PROP =
        "org.j3d.aviatrix3d.pipeline.graphics.BaseStateSortStage.invalidSortableMsg";

    /** Maximum number of textures that OpenGL supports */
    private static final int MAX_GL_TEXTURES = 32;

    /** Maximum number of textures that OpenGL supports */
    private static final int MAX_GL_LIGHTS = 32;

    /** Maximum number of clip planes that OpenGL supports */
    private static final int MAX_GL_CLIPS = 32;

    /** The guess at the number of components in a shape */
    protected static final int GUESS_NUM_COMPONENTS = 5;

    /** Listing of int to Integer equivalents for texture IDs */
    protected static final Integer[] TEX_IDS;

    /** Comparator instance for the state sorting */
    protected StateSortComparator stateComparator;

    // Listing of the current items, while we're setting up the state handling.

    /** Temp array for holdin new lights on the object being processed */
    protected VisualDetails[] newLights;

    /** List of current lights that are being processed */
    protected Stack<VisualDetails> currentLights;

    /** List of lights for holding the difference between the current and new light lists */
    protected Stack<VisualDetails> oldLights;

    /** List to hold onto lights that are common between last node and this */
    protected Stack<VisualDetails> keepLights;

    /** Mapping of the VisualDetails instance to the global ID */
    protected HashMap<VisualDetails, Integer> lightIdMap;

    /** Temp array for holding new clip planes on the object being processed */
    protected VisualDetails[] newClipPlanes;

    /** Temp array for fetching clipPlane objects from the HashSets below. */
    protected Object[] clipTmp;

    /** Set of current clip planes that are being processed */
    protected HashSet currentClipPlanes;

    /** Set for holding the difference between the current and new sets */
    protected HashSet oldClipPlanes;

    /** Set to hold onto clip planes that are common between last node and this */
    protected HashSet keepClipPlanes;

    /** Mapping of the VisualDetails instance to the global ID */
    private HashMap<VisualDetails, Integer> clipIdMap;

    /** The currently valid appearance */
    protected AppearanceRenderable currentAppearance;

    /** The currently valid material */
    protected ObjectRenderable currentMaterial;

    /** The currently valid polygon attributes */
    protected AppearanceAttributeRenderable currentPolyAttr;

    /** The currently valid line attributes */
    protected AppearanceAttributeRenderable currentLineAttr;

    /** The currently valid point attributes */
    protected AppearanceAttributeRenderable currentPointAttr;

    /** The currently valid blend attributes */
    protected AppearanceAttributeRenderable currentBlendAttr;

    /** The currently valid depth buffer attributes */
    protected AppearanceAttributeRenderable currentDepthAttr;

    /** The currently valid stencil buffer attributes */
    protected AppearanceAttributeRenderable currentStencilAttr;

    /** The currently valid GLSlang shader*/
    protected ShaderRenderable currentShader;

    /** The currently valid shader arguments (GLSLang)*/
    protected ComponentRenderable currentShaderArgs;

    /** The currently valid local fog */
    protected Renderable currentFog;

    /** Listing of currently valid textures */
    protected ComponentRenderable[] currentTextures;

    /** Number of valid textures currently active */
    protected int numTextures;

    /**
     * Static constructor to initialise some of the constants
     */
    static
    {
        TEX_IDS = new Integer[32];
        for(int i = 0; i < 32; i++)
            TEX_IDS[i] = new Integer(GL.GL_TEXTURE0 + i);
    }

    /**
     * Create an empty sorting stage that initialises the internal structures
     * to assume that there is a minumum number of surfaces, both on and
     * offscreen.
     *
     * @param numSurfaces The number of surfaces that we're likely to
     *    encounter. Must be a non-negative number
     * @throws IllegalArgumentException numSurfaces was < 0
     */
    protected BaseStateSortStage(int numSurfaces)
    {
        super(numSurfaces);

        stateComparator = new StateSortComparator();

        currentLights = new Stack<VisualDetails>();
        keepLights = new Stack<VisualDetails>();
        oldLights = new Stack<VisualDetails>();

        lightIdMap = new HashMap<VisualDetails, Integer>();
        newLights = new VisualDetails[MAX_GL_LIGHTS];

        currentClipPlanes = new HashSet();
        keepClipPlanes = new HashSet();
        oldClipPlanes = new HashSet();
        clipIdMap = new HashMap<VisualDetails, Integer>();
        clipTmp = new Object[24];
        newClipPlanes = new VisualDetails[32];

        // 32 is max textures that OpenGL can support.
        currentTextures = new ComponentRenderable[MAX_GL_TEXTURES];
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Append a single object onto the existing list. Checks versus existing
     * state and starts and stops as required.
     *
     * @param node The node instance to look at appending
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int appendObject(GraphicsCullOutputDetails node,
                               GraphicsInstructions instr,
                               int offset)
    {
        int idx = offset;

        idx = appendLights(node, instr, idx);
        idx = appendClipPlanes(node, instr, idx);
        idx = updateFog(node.localFog, instr, idx);

        // Finally, map the shape instances
        if(node.renderable instanceof ShapeRenderable)
        {
            ShapeRenderable shape = (ShapeRenderable)node.renderable;

            AppearanceRenderable app = shape.getAppearanceRenderable();
            GeometryRenderable geom = shape.getGeometryRenderable();

            if(currentAppearance != app)
            {
                // If the new state is now null, but before it wasn't then
                // we need to clear all the current visual state. Otherwise
                // we have to change the state from the existing one and
                // setup the new state.
                if(app == null)
                {
                    idx = cleanupVisuals(instr, idx);
                }
                else
                {
                    idx = updateMaterial(app, instr, idx);
                    idx = updatePolyAttribs(app, instr, idx);
                    idx = updateLineAttribs(app, instr, idx);
                    idx = updatePointAttribs(app, instr, idx);
                    idx = updateBlendAttribs(app, instr, idx);
                    idx = updateDepthAttribs(app, instr, idx);
                    idx = updateStencilAttribs(app, instr, idx);
                    idx = updateTextures(app, instr, idx);
                    idx = updateShader(app, instr, idx);
                }

                currentAppearance = app;
            }

            instr.renderList[idx].renderable = geom;

            Matrix4d tx = node.transform;

            if(!shape.is2D())
            {
                instr.renderList[idx].transform[0] = tx.m00;
                instr.renderList[idx].transform[1] = tx.m10;
                instr.renderList[idx].transform[2] = tx.m20;
                instr.renderList[idx].transform[3] = tx.m30;

                instr.renderList[idx].transform[4] = tx.m01;
                instr.renderList[idx].transform[5] = tx.m11;
                instr.renderList[idx].transform[6] = tx.m21;
                instr.renderList[idx].transform[7] = tx.m31;

                instr.renderList[idx].transform[8] = tx.m02;
                instr.renderList[idx].transform[9] = tx.m12;
                instr.renderList[idx].transform[10] = tx.m22;
                instr.renderList[idx].transform[11] = tx.m32;

                instr.renderList[idx].transform[12] = tx.m03;
                instr.renderList[idx].transform[13] = tx.m13;
                instr.renderList[idx].transform[14] = tx.m23;
                instr.renderList[idx].transform[15] = tx.m33;

                if(geom instanceof CustomGeometryRenderable)
                {
                    instr.renderOps[idx] = RenderOp.RENDER_CUSTOM_GEOMETRY;
                    instr.renderList[idx].instructions = node.customData;
                }
                else
                    instr.renderOps[idx] = RenderOp.RENDER_GEOMETRY;
            }
            else
            {
                instr.renderList[idx].transform[0] = tx.m00;
                instr.renderList[idx].transform[1] = 0;
                instr.renderList[idx].transform[2] = 0;
                instr.renderList[idx].transform[3] = tx.m30;

                instr.renderList[idx].transform[4] = 0;
                instr.renderList[idx].transform[5] = tx.m11;
                instr.renderList[idx].transform[6] = 0;
                instr.renderList[idx].transform[7] = tx.m31;

                instr.renderList[idx].transform[8] = 0;
                instr.renderList[idx].transform[9] = 0;
                instr.renderList[idx].transform[10] = 0;
                instr.renderList[idx].transform[11] = 0;

                instr.renderList[idx].transform[12] = 0;
                instr.renderList[idx].transform[13] = 0;
                instr.renderList[idx].transform[14] = 0;
                instr.renderList[idx].transform[15] = 0;

                instr.renderOps[idx] = RenderOp.RENDER_GEOMETRY_2D;
            }

            idx++;
        }
        else if(node.renderable instanceof CustomRenderable)
        {
            // forcefully remove anything left
            idx = cleanupVisuals(instr, idx);

            instr.renderList[idx].renderable = node.renderable;
            instr.renderList[idx].instructions = node.customData;

            Matrix4d tx = node.transform;

            instr.renderList[idx].transform[0] = tx.m00;
            instr.renderList[idx].transform[1] = tx.m10;
            instr.renderList[idx].transform[2] = tx.m20;
            instr.renderList[idx].transform[3] = tx.m30;

            instr.renderList[idx].transform[4] = tx.m01;
            instr.renderList[idx].transform[5] = tx.m11;
            instr.renderList[idx].transform[6] = tx.m21;
            instr.renderList[idx].transform[7] = tx.m31;

            instr.renderList[idx].transform[8] = tx.m02;
            instr.renderList[idx].transform[9] = tx.m12;
            instr.renderList[idx].transform[10] = tx.m22;
            instr.renderList[idx].transform[11] = tx.m32;

            instr.renderList[idx].transform[12] = tx.m03;
            instr.renderList[idx].transform[13] = tx.m13;
            instr.renderList[idx].transform[14] = tx.m23;
            instr.renderList[idx].transform[15] = tx.m33;

            instr.renderOps[idx] = RenderOp.RENDER_CUSTOM;
            idx++;
        }
        else
        {
            I18nManager intl_mgr = I18nManager.getManager();
            Locale lcl = intl_mgr.getFoundLocale();
            String msg_pattern = intl_mgr.getString(INVALID_SORTABLE_PROP);

            String cls_name = node.renderable == null ?
                "null" : node.renderable.getClass().getName();

            Object[] msg_args = { node.renderable.getClass().getName() };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            String msg = msg_fmt.format(msg_args);

            errorReporter.warningReport(msg, null);
        }

        return idx;
    }

    /**
     * Append the lights onto the existing list. Checks versus existing
     * state and starts and stops as required.
     *
     * @param node The node instance to look at appending
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int appendLights(GraphicsCullOutputDetails node,
                               GraphicsInstructions instr,
                               int offset)
    {
        if(node.numLights == 0 && currentLights.size() == 0)
            return offset;

        // In most cases, the list of lights are identical because sets of
        // lights are usually placed high in the scene. When we have the same
        // number, it is likely that we have the identical sets of lights.
        // This just sees if they are the same, in the same order. This test
        // is not very rigorous, as if there is not an exact match, we fall
        // through to the more rigorous checking afterwards. However in 90+%
        // of the cases, this finds identical lists, so good enough to get
        // some major performance boosts.
        if (node.numLights == currentLights.size())
        {
            int len = node.numLights;
            boolean all_equal = true;

            for(int i = 0; i < len; i++)
            {
                VisualDetails vd = currentLights.get(i);

                if(node.lights[i].getRenderable() != vd.getRenderable())
                {
                    all_equal = false;
                    break;
                }
            }

            if(all_equal)
                return offset;
        }

        int idx = offset;

        // First drop all the lights into the queue and turn the state off on
        // the no-longer used ones.
        int new_count = 0;

        for(int i = 0; i < node.numLights; i++)
        {
            VisualDetails ld = node.lights[i];

            if(currentLights.contains(ld))
                keepLights.add(ld);
            else
                newLights[new_count++] = ld;
        }

        oldLights.addAll(currentLights);

        currentLights.retainAll(keepLights);

        oldLights.removeAll(currentLights);

        keepLights.clear();

        // Unmap the old lights.
        int num_old = oldLights.size();
        if(num_old != 0)
        {
            for(int i = 0; i < num_old; i++)
            {
                VisualDetails ld = (VisualDetails)oldLights.pop();
                Integer l_id = (Integer)lightIdMap.remove(ld);

                instr.renderList[idx].renderable = ld.getRenderable();
                instr.renderList[idx].id = l_id.intValue();

                // Don't need the transform for the light stopping. Save
                // CPU cycles by not copying it.

                instr.renderOps[idx] = RenderOp.STOP_LIGHT;
                idx++;
            }
        }

        // Map the new lights.
        if(new_count != 0)
        {
            for(int i = 0; i < new_count; i++)
            {
                VisualDetails ld = newLights[i];
                currentLights.add(ld);

                instr.renderList[idx].renderable = ld.getRenderable();

                System.arraycopy(ld.getTransform(),
                                 0,
                                 instr.renderList[idx].transform,
                                 0,
                                 16);

                lightIdMap.put(ld, new Integer(lastGlobalId));
                instr.renderList[idx].id = lastGlobalId++;
                instr.renderOps[idx] = RenderOp.START_LIGHT;
                idx++;
                newLights[i] = null;
            }
        }

        return idx;
    }

    /**
     * Append the clip planess onto the existing list. Checks versus existing
     * state and starts and stops as required.
     *
     * @param node The node instance to look at appending
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int appendClipPlanes(GraphicsCullOutputDetails node,
                                   GraphicsInstructions instr,
                                   int offset)
    {
        if(node.numClipPlanes == 0 && currentClipPlanes.size() == 0)
            return offset;

        int idx = offset;

        // Drop all the clips into the queue and turn the state off on
        // the no-longer used ones.
        int new_count = 0;

        for(int i = 0; i < node.numClipPlanes; i++)
        {
            VisualDetails ld = node.clipPlanes[i];

            if(currentClipPlanes.contains(ld))
                keepClipPlanes.add(ld);
            else
                newClipPlanes[new_count++] = ld;
        }

        currentClipPlanes.retainAll(keepClipPlanes, oldClipPlanes);
        keepClipPlanes.clear();

        // Unmap the old clips.
        int num_old = oldClipPlanes.size();
        if(num_old != 0)
        {
            if(num_old > clipTmp.length)
                clipTmp = new Object[num_old];

            oldClipPlanes.toArray(clipTmp);
            oldClipPlanes.clear();

            for(int i = 0; i < num_old; i++)
            {
                VisualDetails ld = (VisualDetails)clipTmp[i];
                Integer l_id = (Integer)clipIdMap.remove(ld);

                instr.renderList[idx].renderable = ld.getRenderable();
                instr.renderList[idx].id = l_id.intValue();

                // Don't need the transform for the clip stopping. Save
                // CPU cycles by not copying it.

                instr.renderOps[idx] = RenderOp.STOP_CLIP_PLANE;
                idx++;
                clipTmp[i] = null;
            }
        }

        // Map the new clips.
        if(new_count != 0)
        {
            for(int i = 0; i < new_count; i++)
            {
                VisualDetails ld = newClipPlanes[i];
                currentClipPlanes.add(ld);

                instr.renderList[idx].renderable = ld.getRenderable();

                System.arraycopy(ld.getTransform(),
                                 0,
                                 instr.renderList[idx].transform,
                                 0,
                                 16);

                clipIdMap.put(ld, new Integer(lastGlobalId));
                instr.renderList[idx].id = lastGlobalId++;
                instr.renderOps[idx] = RenderOp.START_CLIP_PLANE;
                idx++;
                newClipPlanes[i] = null;
            }
        }

        return idx;
    }

    /**
     * Check on the material setup and change as necessary.
     *
     * @param fog The Fog instance to source info from
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int updateFog(Renderable fog,
                            GraphicsInstructions instr,
                            int offset)
    {
        int idx = offset;

        if(fog != currentFog)
        {
            if(fog != null)
            {
                // Something has really changed? If so set the
                // state up to start running now.
                if(!fog.equals(currentFog))
                {
                    // Clear the old state if it is set
                    if(currentFog != null)
                    {
                        instr.renderList[idx].renderable = currentFog;
                        instr.renderOps[idx] = RenderOp.STOP_FOG;
                        idx++;
                    }

                    instr.renderList[idx].renderable = fog;
                    instr.renderOps[idx] = RenderOp.START_FOG;
                    idx++;
                }

                currentFog = fog;
            }
            else
            {
                // The new fogerial is null, so clear the existing
                // state and leave it empty.
                instr.renderList[idx].renderable = currentFog;
                instr.renderOps[idx] = RenderOp.STOP_FOG;
                idx++;

                currentFog = null;
            }
        }

        return idx;
    }


    /**
     * Check on the shader setup and change as necessary.
     *
     * @param app The appearance instance to source info from
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int updateShader(AppearanceRenderable app,
                               GraphicsInstructions instr,
                               int offset)
    {
        ShaderRenderable sh = app.getShaderRenderable();

        if(sh == currentShader)
            return offset;

        int idx = offset;
        ShaderComponentRenderable comp = null;

        // If we now have no shader, but there was one before, shut everything
        // down.
        if(sh == null)
        {
            comp = currentShader.getShaderRenderable(ShaderComponentRenderable.PROGRAM_SHADER);

            if(comp != null)
            {
                instr.renderList[idx].renderable = comp;
                instr.renderOps[idx] = RenderOp.STOP_SHADER_PROGRAM;
                idx++;
            }
            else
            {
                // Not a GLSL Shader
                instr.renderList[idx].renderable = currentShader;
                instr.renderOps[idx] = RenderOp.STOP_STATE;
                idx++;
            }

            currentShader = null;
            currentShaderArgs = null;

            return idx;
        }

        comp = sh.getShaderRenderable(ShaderComponentRenderable.PROGRAM_SHADER);

        if(comp != null)
        {
            // If the old one was GL14, halt it and replace it entirely with the
            // new one.
            if(currentShader != null)
            {
                ShaderComponentRenderable comp2 =
                    currentShader.getShaderRenderable(ShaderComponentRenderable.PROGRAM_SHADER);

                if(comp2 == null)
                {
                    // Not a GLSL Shader, so shut the old one down.
                    instr.renderList[idx].renderable = currentShader;
                    instr.renderOps[idx] = RenderOp.STOP_STATE;
                    idx++;
                }
            }

            // Now start the new GLSL shader

            instr.renderList[idx].renderable = comp;
            instr.renderOps[idx] = RenderOp.START_SHADER_PROGRAM;
            idx++;

            ComponentRenderable args = sh.getArgumentsRenderable();

            if(currentShaderArgs != args)
            {
                instr.renderList[idx].renderable = args;
                instr.renderOps[idx] = RenderOp.SET_SHADER_ARGS;
                idx++;

                currentShaderArgs = args;
            }
        }
        else
        {
            // There is no program for this shader, let's see if the current
            // shader is a GLSL shader. If so, shut it down.
            if(currentShader != null)
            {
                ShaderComponentRenderable comp2 =
                    currentShader.getShaderRenderable(ShaderComponentRenderable.PROGRAM_SHADER);

                if(comp2 != null)
                {
                    instr.renderList[idx].renderable = comp2;
                    instr.renderOps[idx] = RenderOp.STOP_SHADER_PROGRAM;
                    idx++;
                }
            }

            // Now start the new non-GLSL shader
            instr.renderList[idx].renderable = sh;
            instr.renderOps[idx] = RenderOp.START_STATE;
            idx++;

            ComponentRenderable args = sh.getArgumentsRenderable();

            if(currentShaderArgs != args)
            {
                instr.renderList[idx].renderable = args;
                instr.renderOps[idx] = RenderOp.SET_SHADER_ARGS;
                idx++;

                currentShaderArgs = args;
            }
        }

        currentShader = sh;

        return idx;
    }

    /**
     * Check on the material setup and change as necessary.
     *
     * @param app The appearance instance to source info from
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int updateMaterial(AppearanceRenderable app,
                                 GraphicsInstructions instr,
                                 int offset)
    {
        int idx = offset;

        ObjectRenderable mat = app.getMaterialRenderable();
        if(mat != currentMaterial)
        {
            if(mat != null)
            {
                // Something has really changed? If so set the
                // state up to start running now.
                if(!mat.equals(currentMaterial))
                {
                    // Clear the old state if it is set
                    if(currentMaterial != null)
                    {
                        instr.renderList[idx].renderable = currentMaterial;
                        instr.renderOps[idx] = RenderOp.STOP_STATE;
                        idx++;
                    }

                    instr.renderList[idx].renderable = mat;
                    instr.renderOps[idx] = RenderOp.START_STATE;
                    idx++;
                }

                currentMaterial = mat;
            }
            else
            {
                // The new material is null, so clear the existing
                // state and leave it empty.
                instr.renderList[idx].renderable = currentMaterial;
                instr.renderOps[idx] = RenderOp.STOP_STATE;
                idx++;

                currentMaterial = null;
            }
        }

        return idx;
    }

    /**
     * Check on the polygon attributes setup and change as necessary.
     *
     * @param app The appearance instance to source info from
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int updatePolyAttribs(AppearanceRenderable app,
                                    GraphicsInstructions instr,
                                    int offset)
    {
        int idx = offset;

        AppearanceAttributeRenderable attr =
            app.getAttributeRenderable(AppearanceAttributeRenderable.POLYGON_ATTRIBUTE);

        if(attr != currentPolyAttr)
        {
            if(attr != null)
            {
                // Something has really changed? If so set the
                // state up to start running now.
                if(!attr.equals(currentPolyAttr))
                {
                    // Clear the old state if it is set
                    if(currentPolyAttr != null)
                    {
                        instr.renderList[idx].renderable = currentPolyAttr;
                        instr.renderOps[idx] = RenderOp.STOP_STATE;
                        idx++;
                    }

                    instr.renderList[idx].renderable = attr;
                    instr.renderOps[idx] = RenderOp.START_STATE;
                    idx++;
                }

                currentPolyAttr = attr;
            }
            else
            {
                // The new attrerial is null, so clear the existing
                // state and leave it empty.
                instr.renderList[idx].renderable = currentPolyAttr;
                instr.renderOps[idx] = RenderOp.STOP_STATE;
                idx++;

                currentPolyAttr = null;
            }
        }

        return idx;
    }

    /**
     * Check on the line attributes setup and change as necessary.
     *
     * @param app The appearance instance to source info from
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int updateLineAttribs(AppearanceRenderable app,
                                    GraphicsInstructions instr,
                                    int offset)
    {
        int idx = offset;

        AppearanceAttributeRenderable attr =
            app.getAttributeRenderable(AppearanceAttributeRenderable.LINE_ATTRIBUTE);

        if(attr != currentLineAttr)
        {
            if(attr != null)
            {
                // Something has really changed? If so set the
                // state up to start running now.
                if(!attr.equals(currentLineAttr))
                {
                    // Clear the old state if it is set
                    if(currentLineAttr != null)
                    {
                        instr.renderList[idx].renderable = currentLineAttr;
                        instr.renderOps[idx] = RenderOp.STOP_STATE;
                        idx++;
                    }

                    instr.renderList[idx].renderable = attr;
                    instr.renderOps[idx] = RenderOp.START_STATE;
                    idx++;
                }

                currentLineAttr = attr;
            }
            else
            {
                // The new attrerial is null, so clear the existing
                // state and leave it empty.
                instr.renderList[idx].renderable = currentLineAttr;
                instr.renderOps[idx] = RenderOp.STOP_STATE;
                idx++;

                currentLineAttr = null;
            }
        }

        return idx;
    }

    /**
     * Check on the depth attributes setup and change as necessary.
     *
     * @param app The appearance instance to source info from
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int updateDepthAttribs(AppearanceRenderable app,
                                     GraphicsInstructions instr,
                                     int offset)
    {
        int idx = offset;

        AppearanceAttributeRenderable attr =
            app.getAttributeRenderable(AppearanceAttributeRenderable.DEPTH_ATTRIBUTE);

        if(attr != currentDepthAttr)
        {
            if(attr != null)
            {
                // Something has really changed? If so set the
                // state up to start running now.
                if(!attr.equals(currentDepthAttr))
                {
                    // Clear the old state if it is set
                    if(currentDepthAttr != null)
                    {
                        instr.renderList[idx].renderable = currentDepthAttr;
                        instr.renderOps[idx] = RenderOp.STOP_STATE;
                        idx++;
                    }

                    instr.renderList[idx].renderable = attr;
                    instr.renderOps[idx] = RenderOp.START_STATE;
                    idx++;
                }

                currentDepthAttr = attr;
            }
            else
            {
                // The new attrerial is null, so clear the existing
                // state and leave it empty.
                instr.renderList[idx].renderable = currentDepthAttr;
                instr.renderOps[idx] = RenderOp.STOP_STATE;
                idx++;

                currentDepthAttr = null;
            }
        }

        return idx;
    }

    /**
     * Check on the stencil attributes setup and change as necessary.
     *
     * @param app The appearance instance to source info from
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int updateStencilAttribs(AppearanceRenderable app,
                                       GraphicsInstructions instr,
                                       int offset)
    {
        int idx = offset;

        AppearanceAttributeRenderable attr =
            app.getAttributeRenderable(AppearanceAttributeRenderable.STENCIL_ATTRIBUTE);

        if(attr != currentStencilAttr)
        {
            if(attr != null)
            {
                // Something has really changed? If so set the
                // state up to start running now.
                if(!attr.equals(currentStencilAttr))
                {
                    // Clear the old state if it is set
                    if(currentStencilAttr != null)
                    {
                        instr.renderList[idx].renderable = currentStencilAttr;
                        instr.renderOps[idx] = RenderOp.STOP_STATE;
                        idx++;
                    }

                    instr.renderList[idx].renderable = attr;
                    instr.renderOps[idx] = RenderOp.START_STATE;
                    idx++;
                }

                currentStencilAttr = attr;
            }
            else
            {
                // The new attrerial is null, so clear the existing
                // state and leave it empty.
                instr.renderList[idx].renderable = currentStencilAttr;
                instr.renderOps[idx] = RenderOp.STOP_STATE;
                idx++;

                currentStencilAttr = null;
            }
        }

        return idx;
    }

    /**
     * Check on the polygon attributes setup and change as necessary.
     *
     * @param app The appearance instance to source info from
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int updatePointAttribs(AppearanceRenderable app,
                                     GraphicsInstructions instr,
                                     int offset)
    {
        int idx = offset;

        AppearanceAttributeRenderable attr =
            app.getAttributeRenderable(AppearanceAttributeRenderable.POINT_ATTRIBUTE);

        if(attr != currentPointAttr)
        {
            if(attr != null)
            {
                // Something has really changed? If so set the
                // state up to start running now.
                if(!attr.equals(currentPointAttr))
                {
                    // Clear the old state if it is set
                    if(currentPointAttr != null)
                    {
                        instr.renderList[idx].renderable = currentPointAttr;
                        instr.renderOps[idx] = RenderOp.STOP_STATE;
                        idx++;
                    }

                    instr.renderList[idx].renderable = attr;
                    instr.renderOps[idx] = RenderOp.START_STATE;
                    idx++;
                }

                currentPointAttr = attr;
            }
            else
            {
                // The new attrerial is null, so clear the existing
                // state and leave it empty.
                instr.renderList[idx].renderable = currentPointAttr;
                instr.renderOps[idx] = RenderOp.STOP_STATE;
                idx++;

                currentPointAttr = null;
            }
        }

        return idx;
    }

    /**
     * Check on the blending attributes setup and change as necessary.
     *
     * @param app The appearance instance to source info from
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int updateBlendAttribs(AppearanceRenderable app,
                                     GraphicsInstructions instr,
                                     int offset)
    {
        int idx = offset;

        AppearanceAttributeRenderable attr =
            app.getAttributeRenderable(AppearanceAttributeRenderable.BLEND_ATTRIBUTE);

        if(attr != currentBlendAttr)
        {
            if(attr != null)
            {
                // Something has really changed? If so set the
                // state up to start running now.
                if(!attr.equals(currentBlendAttr))
                {
                    // Clear the old state if it is set
                    if(currentBlendAttr != null)
                    {
                        instr.renderList[idx].renderable = currentBlendAttr;
                        instr.renderOps[idx] = RenderOp.STOP_STATE;
                        idx++;
                    }

                    instr.renderList[idx].renderable = attr;
                    instr.renderOps[idx] = RenderOp.START_STATE;
                    idx++;
                }

                currentBlendAttr = attr;
            }
            else
            {
                // The new attrerial is null, so clear the existing
                // state and leave it empty.
                instr.renderList[idx].renderable = currentBlendAttr;
                instr.renderOps[idx] = RenderOp.STOP_STATE;
                idx++;

                currentBlendAttr = null;
            }
        }

        return idx;
    }

    /**
     * Check on the texture listing setup and change as necessary.
     *
     * @param app The appearance instance to source info from
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int updateTextures(AppearanceRenderable app,
                                 GraphicsInstructions instr,
                                 int offset)
    {
        int idx = offset;

        int num_tex = app.numTextureRenderables();

        if(num_tex != numTextures)
        {
            if(num_tex == 0)
            {
                // Clear the old textures that were set
                for(int i = 0; i < numTextures; i++)
                {
                    instr.renderList[idx].renderable = currentTextures[i];
                    instr.renderList[idx].instructions = TEX_IDS[i];
                    instr.renderOps[idx] = RenderOp.STOP_TEXTURE;
                    idx++;

                    currentTextures[i] = null;
                }
            }
            else if(numTextures == 0)
            {
                // There were no old textures, so just list the new ones
                for(int i = 0; i < num_tex; i++)
                {
                    ComponentRenderable tex = app.getTextureRenderable(i);

                    instr.renderList[idx].renderable = tex;
                    instr.renderList[idx].instructions = TEX_IDS[i];
                    instr.renderOps[idx] = RenderOp.START_TEXTURE;
                    idx++;

                    currentTextures[i] = tex;
                }
            }
            else
            {
                int smaller = (numTextures < num_tex) ? numTextures : num_tex;

                for(int i = 0; i < smaller; i++)
                {
                    ComponentRenderable tex = app.getTextureRenderable(i);

                    if(!tex.equals(currentTextures[i]))
                    {
                        instr.renderList[idx].renderable = currentTextures[i];
                        instr.renderList[idx].instructions = TEX_IDS[i];
                        instr.renderOps[idx] = RenderOp.STOP_TEXTURE;
                        idx++;

                        instr.renderList[idx].renderable = tex;
                        instr.renderList[idx].instructions = TEX_IDS[i];
                        instr.renderOps[idx] = RenderOp.START_TEXTURE;
                        idx++;

                        currentTextures[i] = tex;
                    }
                }

                // Clean up what is left or add the new stuff
                if(numTextures < num_tex)
                {
                    for(int i = smaller; i < num_tex; i++)
                    {
                        ComponentRenderable tex = app.getTextureRenderable(i);
                        instr.renderList[idx].renderable = tex;
                        instr.renderList[idx].instructions = TEX_IDS[i];
                        instr.renderOps[idx] = RenderOp.START_TEXTURE;
                        idx++;

                        currentTextures[i] = tex;
                    }
                }
                else
                {
                    for(int i = smaller; i < numTextures; i++)
                    {
                        instr.renderList[idx].renderable = currentTextures[i];
                        instr.renderList[idx].instructions = TEX_IDS[i];
                        instr.renderOps[idx] = RenderOp.STOP_TEXTURE;
                        idx++;

                        currentTextures[i] = null;
                    }
                }
            }

            numTextures = num_tex;
        }
        else if(num_tex != 0)
        {
            // same number of texture units.
            // Special case the most common situation.
            if(num_tex == 1)
            {
                ComponentRenderable tex = app.getTextureRenderable(0);

                if(!tex.equals(currentTextures[0]))
                {
                    instr.renderList[idx].renderable = currentTextures[0];
                    instr.renderList[idx].instructions = TEX_IDS[0];
                    instr.renderOps[idx] = RenderOp.STOP_TEXTURE;
                    idx++;

                    instr.renderList[idx].renderable = tex;
                    instr.renderList[idx].instructions = TEX_IDS[0];
                    instr.renderOps[idx] = RenderOp.START_TEXTURE;
                    idx++;

                    currentTextures[0] = tex;
                }
            }
            else
            {
                for(int i = 0; i < numTextures; i++)
                {
                    ComponentRenderable tex = app.getTextureRenderable(i);

                    if(!tex.equals(currentTextures[i]))
                    {
                        instr.renderList[idx].renderable = currentTextures[i];
                        instr.renderList[idx].instructions = TEX_IDS[i];
                        instr.renderOps[idx] = RenderOp.STOP_TEXTURE;
                        idx++;

                        instr.renderList[idx].renderable = tex;
                        instr.renderList[idx].instructions = TEX_IDS[i];
                        instr.renderOps[idx] = RenderOp.START_TEXTURE;
                        idx++;

                        currentTextures[i] = tex;
                    }
                }
            }
        }

        return idx;
    }

    /**
     * At the end of the scene, we need to make sure that we've turned off all
     * the lights, clip planes and current rendering state that is left. Do
     * that now.
     *
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int cleanupObjects(GraphicsInstructions instr, int offset)
    {
        int idx = offset;

        int num_lights = currentLights.size();
        if(num_lights != 0)
        {
            for(int i = 0; i < num_lights; i++)
            {
                VisualDetails ld = (VisualDetails)currentLights.pop();
                Integer l_id = (Integer)lightIdMap.remove(ld);

                instr.renderList[idx].renderable = ld.getRenderable();
                instr.renderList[idx].id = l_id.intValue();

                // Don't need the transform for the light stopping. Save
                // CPU cycles by not copying it.

                instr.renderOps[idx] = RenderOp.STOP_LIGHT;
                idx++;
            }
        }

        // Now the clip planes.
        int num_clips = currentClipPlanes.size();
        if(num_clips != 0)
        {
            if(num_clips > clipTmp.length)
                clipTmp = new Object[num_clips];

            currentClipPlanes.toArray(clipTmp);
            currentClipPlanes.clear();

            for(int i = 0; i < num_clips; i++)
            {
                VisualDetails ld = (VisualDetails)clipTmp[i];
                Integer l_id = (Integer)clipIdMap.remove(ld);

                instr.renderList[idx].renderable = ld.getRenderable();
                instr.renderList[idx].id = l_id.intValue();

                // Don't need the transform for the clip stopping. Save
                // CPU cycles by not copying it.

                instr.renderOps[idx] = RenderOp.STOP_CLIP_PLANE;
                idx++;

                clipTmp[i] = null;
            }
        }

        return cleanupVisuals(instr, idx);
    }

    /**
     * Convenience method to clear all the current visual information.
     * Visuals are defined as material, shaders various attributes etc.
     *
     * @param offset The distance into instr array of values to start
     * @param instr Instruction instant to put the details into
     * @return The completed offset into the instr array we finished at
     */
    protected int cleanupVisuals(GraphicsInstructions instr, int offset)
    {
        int idx = offset;

        if(currentMaterial != null)
        {
            instr.renderList[idx].renderable = currentMaterial;
            instr.renderOps[idx] = RenderOp.STOP_STATE;
            idx++;

            currentMaterial = null;
        }

        if(currentShader != null)
        {
            ShaderComponentRenderable comp2 =
                currentShader.getShaderRenderable(ShaderComponentRenderable.PROGRAM_SHADER);

            if(comp2 != null)
            {
                instr.renderList[idx].renderable = comp2;
                instr.renderOps[idx] = RenderOp.STOP_SHADER_PROGRAM;
                idx++;
            }
            else
            {
                instr.renderList[idx].renderable = currentShader;
                instr.renderOps[idx] = RenderOp.STOP_STATE;
                idx++;
            }

            currentShader = null;
        }

        if(currentShaderArgs != null)
        {
            currentShaderArgs = null;
        }

        if(currentPolyAttr != null)
        {
            instr.renderList[idx].renderable = currentPolyAttr;
            instr.renderOps[idx] = RenderOp.STOP_STATE;
            idx++;

            currentPolyAttr = null;
        }

        if(currentLineAttr != null)
        {
            instr.renderList[idx].renderable = currentLineAttr;
            instr.renderOps[idx] = RenderOp.STOP_STATE;
            idx++;

            currentLineAttr = null;
        }

        if(currentPointAttr != null)
        {
            instr.renderList[idx].renderable = currentPointAttr;
            instr.renderOps[idx] = RenderOp.STOP_STATE;
            idx++;

            currentPointAttr = null;
        }

        if(currentBlendAttr != null)
        {
            instr.renderList[idx].renderable = currentBlendAttr;
            instr.renderOps[idx] = RenderOp.STOP_STATE;
            idx++;

            currentBlendAttr = null;
        }

        for(int i = 0; i < numTextures; i++)
        {
            instr.renderList[idx].renderable = currentTextures[i];
            instr.renderList[idx].instructions = TEX_IDS[i];
            instr.renderOps[idx] = RenderOp.STOP_TEXTURE;
            idx++;

            currentTextures[i] = null;
        }

        if(currentFog != null)
        {
            instr.renderList[idx].renderable = currentFog;
            instr.renderOps[idx] = RenderOp.STOP_FOG;
            idx++;

            currentFog = null;
        }

        numTextures = 0;
        currentAppearance = null;

        return idx;
    }
}
