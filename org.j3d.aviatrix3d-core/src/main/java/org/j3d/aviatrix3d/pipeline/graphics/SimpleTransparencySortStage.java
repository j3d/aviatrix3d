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
import org.j3d.aviatrix3d.*;
import org.j3d.aviatrix3d.rendering.*;

import org.j3d.aviatrix3d.pipeline.RenderOp;

/**
 * Implementation of the sort stage that separates out the transparent and
 * non transparent objects, but without depth sorting.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>unknownRenderableMsg: Info message when we encounter a renderable type
 *     that we don't know how to sort</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 3.7 $
 */
public class SimpleTransparencySortStage extends BaseSortStage
{
	/** Warning message if we find an unhandled renderable */
	private static final String UNKNOWN_RENDERABLE_PROP =
		"org.j3d.aviatrix3d.pipeline.graphics.SimpleTransparencySortStage.unknownRenderableMsg";

    /** The initial size of the transparent list */
    private static final int TRANSPARENT_LIST_SIZE = 200;


    /** Temporary array for holding the transparent objects */
    private GraphicsCullOutputDetails[] transparentList;

    /** Temp array for reading texture units from the appearance */
    private TextureUnit[] textureTmp;

    /**
     * Create an empty sorting stage that assumes just a single renderable
     * output.
     */
    public SimpleTransparencySortStage()
    {
        this(LIST_START_SIZE);
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
    public SimpleTransparencySortStage(int numSurfaces)
    {
        super(numSurfaces);

        transparentList = new GraphicsCullOutputDetails[TRANSPARENT_LIST_SIZE];

        textureTmp = new TextureUnit[32];
    }

    //---------------------------------------------------------------
    // Methods defined by BaseSortStage
    //---------------------------------------------------------------

    /**
     * Sort a single set of nodes into the output details of a single layer of
     * a single viewport and place in the provided GraphicsInstructions
     * instance. The implementation of this method should only concern itself
     * with this set of nodes and not worry about dealing with nested scenes or
     * other viewports.
     *
     * @param nodes The list of nodes to perform sorting on
     * @param numNodes The number of valid items in the nodes array
     * @param instr Instruction instant to put the details into
     * @param instrCount Offset of current number of valid instructions
     * @return The current instruction count after sorting
     */
    protected int sortNodes(GraphicsCullOutputDetails[] nodes,
                            int numNodes,
                            GraphicsEnvironmentData data,
                            GraphicsInstructions instr,
                            int instrCount)
    {

        int req_size = estimateInstructionSize(nodes, numNodes);

        if(transparentList.length < req_size)
            transparentList = new GraphicsCullOutputDetails[req_size];

        int idx = instrCount;
        int trans = 0;

        for(int i = 0; i < numNodes && !terminate; i++)
        {
            // First check the geometry and so forth for something that
            // has an alpha channel. Need to check everything - geometry
            // colour values, textures and materials. Anything that says it
            // may have alpha is passed off onto the transparent list.
            // Now process the node type.
            if(nodes[i].renderable instanceof ShapeRenderable)
            {
                ShapeRenderable shape = (ShapeRenderable)nodes[i].renderable;

                AppearanceRenderable app = shape.getAppearanceRenderable();
                GeometryRenderable geom = shape.getGeometryRenderable();

                if(app != null)
                {
                    if(app.hasTransparency())
                    {
                        transparentList[trans++] = nodes[i];
                        continue;
                    }
                    else if(!app.hasTransparencyInfo() && geom.hasTransparency())
                    {
                        transparentList[trans++] = nodes[i];
                        continue;
                    }
                }

                if(geom.hasTransparency())
                {
                    transparentList[trans++] = nodes[i];
                    continue;
                }
            }
            else if(nodes[i].renderable instanceof CustomRenderable)
            {
                if(((CustomRenderable)nodes[i].renderable).hasTransparency())
                {
                    transparentList[trans++] = nodes[i];
                    continue;
                }
            }
            else
            {
				I18nManager intl_mgr = I18nManager.getManager();
				Locale lcl = intl_mgr.getFoundLocale();
				String msg_pattern = intl_mgr.getString(UNKNOWN_RENDERABLE_PROP);

				String arg = null;

				if(nodes[i].renderable != null)
					arg = nodes[i].renderable.getClass().getName();

				Object[] msg_args = { arg };

				MessageFormat msg_fmt =
					new MessageFormat(msg_pattern, lcl);

				String msg = msg_fmt.format(msg_args);

                errorReporter.warningReport(msg, null);
                continue;
            }

            // Oh, so we have some geometry that is not transparent, Cool. Drop
            // it onto the queue. Start with all the lights:
            idx = appendOutputShape(idx, nodes[i], instr);
        }

        // Now process the transparency listing. If there is no transparent
        // objects, then exit now.
        if(trans == 0)
            return idx;

        // Basically the same now as the first loop, just running through the
        // transparency listing rather than the node listing.
        instr.renderOps[idx++] = RenderOp.START_TRANSPARENT;

        for(int i = 0; i < trans && !terminate; i++)
        {
            idx = appendOutputShape(idx, transparentList[i], instr);

            // clear it from the list
            transparentList[i] = null;
        }

        instr.renderOps[idx++] = RenderOp.STOP_TRANSPARENT;
        return idx;
    }

    /**
     * Sort a single set of nodes into the output details of a single layer of
     * a single viewport and place in the provided GraphicsInstructions
     * instance. The implementation of this method should only concern itself
     * with this set of nodes and not worry about dealing with nested scenes or
     * other viewports.
     *
     * @param nodes The list of nodes to perform sorting on
     * @param numNodes The number of valid items in the nodes array
     * @param instr Instruction instant to put the details into
     * @param instrCount Offset of current number of valid instructions
     * @return The current instruction count after sorting
     */
    protected int sort2DNodes(GraphicsCullOutputDetails[] nodes,
                              int numNodes,
                              GraphicsEnvironmentData data,
                              GraphicsInstructions instr,
                              int instrCount)
    {
        int req_size = estimateInstructionSize(nodes, numNodes);

        if(transparentList.length < req_size)
            transparentList = new GraphicsCullOutputDetails[req_size];

        int idx = instrCount;
        int trans = 0;

        for(int i = 0; i < numNodes && !terminate; i++)
        {
            // First check the geometry and so forth for something that
            // has an alpha channel. Need to check everything - geometry
            // colour values, textures and materials. Anything that says it
            // may have alpha is passed off onto the transparent list.
            // Now process the node type.
            if(nodes[i].renderable instanceof ShapeRenderable)
            {
                ShapeRenderable shape = (ShapeRenderable)nodes[i].renderable;

                AppearanceRenderable app = shape.getAppearanceRenderable();
                GeometryRenderable geom = shape.getGeometryRenderable();

                if(app != null)
                {
                    if(app.hasTransparency())
                    {
                        transparentList[trans++] = nodes[i];
                        continue;
                    }
                    else if(!app.hasTransparencyInfo() && geom.hasTransparency())
                    {
                        transparentList[trans++] = nodes[i];
                        continue;
                    }
                }

                if(geom.hasTransparency())
                {
                    transparentList[trans++] = nodes[i];
                    continue;
                }
            }
            else if(nodes[i].renderable instanceof CustomRenderable)
            {
                if(((CustomRenderable)nodes[i].renderable).hasTransparency())
                {
                    transparentList[trans++] = nodes[i];
                    continue;
                }
            }
            else
            {
				I18nManager intl_mgr = I18nManager.getManager();
				Locale lcl = intl_mgr.getFoundLocale();
				String msg_pattern = intl_mgr.getString(UNKNOWN_RENDERABLE_PROP);

				String arg = null;

				if(nodes[i].renderable != null)
					arg = nodes[i].renderable.getClass().getName();

				Object[] msg_args = { arg };

				MessageFormat msg_fmt =
					new MessageFormat(msg_pattern, lcl);

				String msg = msg_fmt.format(msg_args);

                errorReporter.warningReport(msg, null);
                continue;
            }

            // Oh, so we have some geometry that is not transparent, Cool. Drop
            // it onto the queue. Start with all the lights:
            idx = appendOutputShape(idx, nodes[i], instr);
        }

        // Now process the transparency listing. If there is no transparent
        // objects, then exit now.
        if(trans == 0)
            return idx;

        // Basically the same now as the first loop, just running through the
        // transparency listing rather than the node listing.
        instr.renderOps[idx++] = RenderOp.START_TRANSPARENT;

        for(int i = 0; i < trans && !terminate; i++)
        {
            idx = appendOutputShape(idx, transparentList[i], instr);

            // clear it from the list
            transparentList[i] = null;
        }

        instr.renderOps[idx++] = RenderOp.STOP_TRANSPARENT;
        return idx;
    }

    /**
     * Estimate the required size of the instruction list needed for this scene
     * to be processed. This is an initial rough estimate that will be used to
     * make sure the arrays are at least big enough to start with. There is no
     * issue if this underestimates, as most sorting will continually check and
     * resize as needed. However, each resize is costly, so the closer this can
     * be to estimating the real size, the better for performance.
     *
     * @param scene The scene bucket to use for the source
     * @return A greater than zero value
     */
    protected int estimateInstructionSize(SceneRenderBucket scene)
    {
        int instr_count = 4 + scene.numNodes << 1;
        for(int m = 0; m < scene.numNodes; m++)
        {
            instr_count += (scene.nodes[m].numLights << 1) +
                           (scene.nodes[m].numClipPlanes << 1);
        }

        return instr_count;
    }

    /**
     * Estimate the required size of the instruction list needed for this scene
     * to be processed. This is an initial rough estimate that will be used to
     * make sure the arrays are at least big enough to start with. There is no
     * issue if this underestimates, as most sorting will continually check and
     * resize as needed. However, each resize is costly, so the closer this can
     * be to estimating the real size, the better for performance.
     *
     * @param scene The scene bucket to use for the source
     * @return A greater than zero value
     */
    protected int estimateInstructionSize(MultipassRenderBucket scene)
    {
        int instr_count = 4;

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

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Estimate the required size of the instruction list needed for this scene
     * to be processed. This is an initial rough estimate that will be used to
     * make sure the arrays are at least big enough to start with. There is no
     * issue if this underestimates, as most sorting will continually check and
     * resize as needed. However, each resize is costly, so the closer this can
     * be to estimating the real size, the better for performance.
     *
     * @param nodes The scene bucket to use for the source
     * @return A greater than zero value
     */
    private int estimateInstructionSize(GraphicsCullOutputDetails[] nodes,
                                        int numNodes)
    {
        int instr_count = 4 + numNodes << 1;
        for(int m = 0; m < numNodes; m++)
        {
            instr_count += (nodes[m].numLights << 1) +
                           (nodes[m].numClipPlanes << 1);
        }

        return instr_count;
    }

    /**
     * Convenience method to add a Shape3D to the list at the given index point.
     *
     * @param idx The starting place in the instruction list
     * @param node The node instance to add
     * @param instr List of instructions to add the node to
     * @return The update idx value taking into account the new nodes
     */
    private int appendOutputShape(int idx,
                                 GraphicsCullOutputDetails node,
                                 GraphicsInstructions instr)
    {
        if(node.numLights != 0)
        {
            for(int j = 0; j < node.numLights; j++)
            {
                VisualDetails ld = node.lights[j];
                instr.renderList[idx].renderable = ld.getRenderable();

                System.arraycopy(ld.getTransform(),
                                 0,
                                 instr.renderList[idx].transform,
                                 0,
                                 16);

                instr.renderList[idx].id = lastGlobalId++;
                instr.renderOps[idx] = RenderOp.START_LIGHT;
                idx++;
            }
        }

        if(node.numClipPlanes != 0)
        {
            for(int j = 0; j < node.numClipPlanes; j++)
            {
                VisualDetails cd = node.clipPlanes[j];
                instr.renderList[idx].renderable = cd.getRenderable();

                System.arraycopy(cd.getTransform(),
                                 0,
                                 instr.renderList[idx].transform,
                                 0,
                                 16);

                instr.renderList[idx].id = lastGlobalId++;
                instr.renderOps[idx] = RenderOp.START_CLIP_PLANE;
                idx++;
            }
        }

        int fog_id = 0;

        if(node.localFog != null)
        {
            fog_id = lastGlobalId++;
            instr.renderList[idx].id = fog_id;
            instr.renderList[idx].renderable = node.localFog;
            instr.renderOps[idx] = RenderOp.START_FOG;
            idx++;
        }

        // Now process the node type.
        // For this simple one, just use the basic render command.
        // Something more complex would pull the shape apart and do
        // state/depth/transparency sorting in this section.
        if(node.renderable instanceof ShapeRenderable)
        {
            ShapeRenderable shape = (ShapeRenderable)node.renderable;
            Matrix4d tx = node.transform;

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
        else if(node.renderable instanceof CustomRenderable)
        {
            instr.renderList[idx].renderable = node.renderable;
            instr.renderList[idx].instructions = node.customData;

            instr.renderList[idx].transform[0] = node.transform.m00;
            instr.renderList[idx].transform[1] = node.transform.m10;
            instr.renderList[idx].transform[2] = node.transform.m20;
            instr.renderList[idx].transform[3] = node.transform.m30;

            instr.renderList[idx].transform[4] = node.transform.m01;
            instr.renderList[idx].transform[5] = node.transform.m11;
            instr.renderList[idx].transform[6] = node.transform.m21;
            instr.renderList[idx].transform[7] = node.transform.m31;

            instr.renderList[idx].transform[8] = node.transform.m02;
            instr.renderList[idx].transform[9] = node.transform.m12;
            instr.renderList[idx].transform[10] = node.transform.m22;
            instr.renderList[idx].transform[11] = node.transform.m32;

            instr.renderList[idx].transform[12] = node.transform.m03;
            instr.renderList[idx].transform[13] = node.transform.m13;
            instr.renderList[idx].transform[14] = node.transform.m23;
            instr.renderList[idx].transform[15] = node.transform.m33;

            instr.renderOps[idx] = RenderOp.RENDER_CUSTOM;
            idx++;
        }

        if(node.localFog != null)
        {
            instr.renderList[idx].id = fog_id;
            instr.renderList[idx].renderable = node.localFog;
            instr.renderOps[idx] = RenderOp.STOP_FOG;
            idx++;
        }

        if(node.numClipPlanes != 0)
        {
            for(int j = node.numClipPlanes - 1; j >= 0; j--)
            {
                VisualDetails cd = node.clipPlanes[j];
                instr.renderList[idx].renderable = cd.getRenderable();

                instr.renderList[idx].id = lastGlobalId - (1 + j);

                // Don't need the transform for the light stopping. Save
                // CPU cycles by not copying it.

                instr.renderOps[idx] = RenderOp.STOP_CLIP_PLANE;
                idx++;
            }
        }

        if(node.numLights != 0)
        {
            for(int j = node.numLights - 1; j >= 0; j--)
            {
                VisualDetails ld = node.lights[j];
                instr.renderList[idx].renderable = ld.getRenderable();

                instr.renderList[idx].id = lastGlobalId - (1 + j);

                // Don't need the transform for the light stopping. Save
                // CPU cycles by not copying it.

                instr.renderOps[idx] = RenderOp.STOP_LIGHT;
                idx++;
            }
        }

        return idx;
    }
}
