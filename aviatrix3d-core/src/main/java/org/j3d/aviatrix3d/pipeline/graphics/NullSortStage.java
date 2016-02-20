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
import java.text.MessageFormat;
import java.util.Locale;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

import org.j3d.aviatrix3d.pipeline.RenderOp;

/**
 * Implementation of the sort stage that does nothing.
 * <p>
 *
 * The sort stage just takes the given nodes and expands them into an array
 * renders and then immediately pops the node. No sorting on output is done.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidSortableMsg: Error message when a non Shape/Custom renderable
 *     manages to make it through the pipeline to here.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 3.8 $
 */
public class NullSortStage extends BaseSortStage
{
    /** Message for an invalid sortable */
    private static final String INVALID_SORTABLE_PROP =
        "org.j3d.aviatrix3d.pipeline.graphics.NullSortStage.invalidSortableMsg";

    /**
     * Create an empty sorting stage that assumes just a single renderable
     * output.
     */
    public NullSortStage()
    {
        this(LIST_START_SIZE);
    }

    /**
     * Create an empty sorting stage that initialises the internal structures
     * to assume that there is a minumum number of surfaces, both on and
     * offscreen.
     *
     */
    public NullSortStage(int numSurfaces)
    {
        super(numSurfaces);
    }

    //---------------------------------------------------------------
    // Methods defined by BaseSortStage
    //---------------------------------------------------------------

    @Override
    protected int sortNodes(GraphicsCullOutputDetails[] nodes,
                            int numNodes,
                            GraphicsEnvironmentData data,
                            GraphicsInstructions instr,
                            int instrCount)
    {
        int idx = instrCount;
        int fog_id = 0;

        for(int i = 0; i < numNodes && !terminate; i++)
        {
            // First drop all the lights into the queue.
            if(nodes[i].numLights != 0)
            {
                for(int j = 0; j < nodes[i].numLights; j++)
                {
                    VisualDetails ld = nodes[i].lights[j];
                    instr.renderList[idx].renderable = ld.getRenderable();

                    float[] src_tx = ld.getTransform();

                    for(int k = 0; k < 16; k++)
                    {
                        instr.renderList[idx].transform[k] = src_tx[k];
                    }

                    instr.renderList[idx].id = lastGlobalId++;
                    instr.renderOps[idx] = RenderOp.START_LIGHT;
                    idx++;
                }
            }

            // Next drop all the clip planes into the queue.
            if(nodes[i].numClipPlanes != 0)
            {
                for(int j = 0; j < nodes[i].numClipPlanes; j++)
                {
                    VisualDetails cd = nodes[i].clipPlanes[j];
                    instr.renderList[idx].renderable = cd.getRenderable();

                    float[] src_tx = cd.getTransform();

                    for(int k = 0; k < 16; k++)
                    {
                        instr.renderList[idx].transform[k] = src_tx[k];
                    }

                    instr.renderList[idx].id = lastGlobalId++;
                    instr.renderOps[idx] = RenderOp.START_CLIP_PLANE;
                    idx++;
                }
            }

            if(nodes[i].localFog != null)
            {
                fog_id = lastGlobalId++;
                instr.renderList[idx].id = fog_id;
                instr.renderList[idx].renderable = nodes[i].localFog;
                instr.renderOps[idx] = RenderOp.START_FOG;
                idx++;
            }

            if(nodes[i].renderable instanceof ShapeRenderable)
            {
                ShapeRenderable shape = (ShapeRenderable)nodes[i].renderable;
                Matrix4d tx = nodes[i].transform;

                if(!shape.is2D())
                {
                    Renderable r = shape.getAppearanceRenderable();

                    if(r != null)
                    {
                        instr.renderList[idx].renderable = r;
                        instr.renderOps[idx] = RenderOp.START_STATE;
                        idx++;
                    }

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

                    instr.renderList[idx].renderable = shape.getGeometryRenderable();
                    instr.renderOps[idx] = RenderOp.RENDER_GEOMETRY;
                    idx++;

                    if(r != null)
                    {
                        instr.renderList[idx].renderable = r;
                        instr.renderOps[idx] = RenderOp.STOP_STATE;
                        idx++;
                    }
                }
                else
                {
                    Renderable r = shape.getAppearanceRenderable();

                    if(r != null)
                    {
                        instr.renderList[idx].renderable = r;
                        instr.renderOps[idx] = RenderOp.START_RENDER;
                        idx++;
                    }

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

                    instr.renderList[idx].renderable = shape.getGeometryRenderable();
                    instr.renderOps[idx] = RenderOp.RENDER_GEOMETRY_2D;
                    idx++;

                    if(r != null)
                    {
                        instr.renderList[idx].renderable = r;
                        instr.renderOps[idx] = RenderOp.STOP_RENDER;
                        idx++;
                    }
                }
            }
            else if(nodes[i].renderable instanceof CustomRenderable)
            {
                instr.renderList[idx].renderable = nodes[i].renderable;
                instr.renderList[idx].instructions = nodes[i].customData;

                Matrix4d tx = nodes[i].transform;

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

                String cls_name = nodes[i].renderable == null ?
                    "null" : nodes[i].renderable.getClass().getName();

                Object[] msg_args = { cls_name };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);

                errorReporter.warningReport(msg, null);
            }

            if(nodes[i].localFog != null)
            {
                instr.renderList[idx].id = fog_id;
                instr.renderList[idx].renderable = nodes[i].localFog;
                instr.renderOps[idx] = RenderOp.STOP_FOG;
                idx++;
            }

            // Clean up the clip planess
            if(nodes[i].numClipPlanes != 0)
            {
                for(int j = nodes[i].numClipPlanes - 1; j >= 0; j--)
                {
                    VisualDetails cd = nodes[i].clipPlanes[j];
                    instr.renderList[idx].renderable = cd.getRenderable();

                    instr.renderList[idx].id = lastGlobalId - (1 + j);

                    // Don't need the transform for the clip stopping. Save
                    // CPU cycles by not copying it.

                    instr.renderOps[idx] = RenderOp.STOP_CLIP_PLANE;
                    idx++;
                }
            }

            // Clean up the lights
            if(nodes[i].numLights != 0)
            {
                for(int j = nodes[i].numLights - 1; j >= 0; j--)
                {
                    VisualDetails ld = nodes[i].lights[j];
                    instr.renderList[idx].renderable = ld.getRenderable();

                    instr.renderList[idx].id = lastGlobalId - (1 + j);

                    // Don't need the transform for the light stopping. Save
                    // CPU cycles by not copying it.

                    instr.renderOps[idx] = RenderOp.STOP_LIGHT;
                    idx++;
                }
            }
        }

        return idx;
    }

    @Override
    protected int sort2DNodes(GraphicsCullOutputDetails[] nodes,
                              int numNodes,
                              GraphicsEnvironmentData data,
                              GraphicsInstructions instr,
                              int instrCount)
    {
        int idx = instrCount;

        for(int i = 0; i < numNodes && !terminate; i++)
        {
            if(nodes[i].renderable instanceof ShapeRenderable)
            {
                ShapeRenderable shape = (ShapeRenderable)nodes[i].renderable;

                if(!shape.is2D())
                    continue;

                // For this simple one, just use the basic render command.
                // Something more complex would pull the shape apart and do
                // state/depth/transparency sorting in this section.
                Matrix4d tx = nodes[i].transform;

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

                instr.renderList[idx].renderable = nodes[i].renderable;
                instr.renderOps[idx] = RenderOp.START_RENDER_2D;
                idx++;

                instr.renderList[idx].renderable = nodes[i].renderable;
                instr.renderOps[idx] = RenderOp.STOP_RENDER_2D;
                idx++;
            }
            else if(nodes[i].renderable instanceof CustomRenderable)
            {
                instr.renderList[idx].renderable = nodes[i].renderable;
                instr.renderList[idx].instructions = nodes[i].customData;

                Matrix4d tx = nodes[i].transform;

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

                String cls_name = nodes[i].renderable == null ?
                    "null" : nodes[i].renderable.getClass().getName();

                Object[] msg_args = { cls_name };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                String msg = msg_fmt.format(msg_args);

                errorReporter.warningReport(msg, null);
            }
        }

        return idx;
    }

    @Override
    protected int estimateInstructionSize(SceneRenderBucket scene)
    {
        int instr_count = 2 + scene.numNodes << 1;
        for(int m = 0; m < scene.numNodes; m++)
        {
            instr_count += (scene.nodes[m].numLights << 1) +
                           (scene.nodes[m].numClipPlanes << 1);
        }

        return instr_count;
    }

    @Override
    protected int estimateInstructionSize(MultipassRenderBucket scene)
    {
        int instr_count = 2;

        for(int i = 0; i < scene.mainScene.numPasses; i++)
        {
            // Start/stop pass commands + up to 4 buffer state start
            // and stop commands.
            instr_count += 10 + scene.mainScene.numNodes[i] << 1;

            for(int j = 0; j < scene.mainScene.numNodes[i]; j++)
            {
                instr_count += (scene.mainScene.nodes[i][j].numLights << 1) +
                               (scene.mainScene.nodes[i][j].numClipPlanes << 1);
            }
        }

        return instr_count;
    }
}
