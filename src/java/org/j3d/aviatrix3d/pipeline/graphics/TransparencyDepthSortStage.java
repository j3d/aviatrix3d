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
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.HashMap;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import org.j3d.util.I18nManager;
import org.j3d.util.MatrixUtils;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

import org.j3d.aviatrix3d.pipeline.RenderOp;

/**
 * Implementation of the sort stage that separates out the transparent and
 * non transparent objects, and then arranges them using depth sorting.
 * <p>
 *
 * The depth sorting algorithm used is to first assemble all the transparen
 * objects into a list. Calculate their distance from the screen using the
 * center of the bounds as the world space position. Normalise the depth to
 * an integer between 0 and 32K (16bit depth buffer equivalent) where 0 is the
 * near clipping plane and 32K is the far clipping plane. With the distance
 * values, perform a counting-sort operation to sort the objects into the
 * correct order with deepest first.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidDepthMsg: Message when the user provides a number of depth bits
 *     that are not positive</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 3.13 $
 */
public class TransparencyDepthSortStage extends BaseSortStage
{
    /** Message when the number of depth bits <= 0 */
    private static final String INVALID_DEPTH_MSG_PROP =
        "org.j3d.aviatrix3d.pipeline.graphics.TransparencyDepthSortStage.invalidDepthMsg";

    /** Message when an unknown renderable is found */
    private static final String INVALID_REND_MSG_PROP =
        "org.j3d.aviatrix3d.pipeline.graphics.TransparencyDepthSortStage.invalidRenderableMsg";

    /** The initial size of the transparent list */
    private static final int TRANSPARENT_LIST_SIZE = 200;

    /** Default accuracy bits. Matches most common video cards */
    private static final int DEFAULT_DEPTH_BITS = 16;

    /**
     * The accuracy of the depth quantisation. This will be a power of two
     * value that is used to setup the depth list for input to the counting
     * sort.
     */
    private final int depthAccuracy;

    /** Temporary array for holding the transparent objects */
    private GraphicsCullOutputDetails[] transparentList;

    /** Temporary array for holding the transparent objects prior to sorting */
    private GraphicsCullOutputDetails[] unsortedTransparentList;

    /** Temporary array for holding the transparent object depths */
    private float[] depthList1;

    /** Temporary array for holding the quantized depths */
    private int[] depthList2;

    /** The count list used during sorting */
    private int[] countList;

    /** Temp matrix used to hold the local to vworld values */
    private Matrix4f modelMatrix;

    /** Temp matrix used to hold inverted camera matrix */
    private Matrix4f cameraMatrix;

    /** Temporary point location while working out the camera-space depth */
    private Point3f wkPoint;

    /** Temporary array for fetching the center position */
    private float[] center;

    /** Matrix utility code for doing inversions */
    private MatrixUtils matrixUtils;

    /**
     * Create an empty sorting stage that assumes just a single renderable
     * output. The default list size is 1 and the number of depth bits is
     * 16.
     */
    public TransparencyDepthSortStage()
    {
        this(LIST_START_SIZE, DEFAULT_DEPTH_BITS);
    }

    /**
     * Create an empty sorting stage that initialises the internal structures
     * to assume that there is a minumum number of surfaces, both on and
     * offscreen. The number of depth bits is set to 16.
     *
     * @param numSurfaces The number of surfaces that we're likely to
     *    encounter. Must be a non-negative number
	 * @throws IllegalArgumentException numSurfaces was < 0
     */
    public TransparencyDepthSortStage(int numSurfaces)
    {
        this(numSurfaces, DEFAULT_DEPTH_BITS);
    }

    /**
     * Create an empty sorting stage that initialises the internal structures
     * to assume that there is a minumum number of surfaces, both on and
     * offscreen.
     *
     * @param numSurfaces The number of surfaces to start the internal lists
     *     sizes at.
     * @param depthBits The number of bits of depth precision to quantize the
     *     depth sorting to
     * @throws IllegalArgumentException Depth bits or num surfaces is zero or
     *     negative
     */
    public TransparencyDepthSortStage(int numSurfaces, int depthBits)
    {
        super(numSurfaces);

        if(depthBits < 1)
		{
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_DEPTH_MSG_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Integer(depthBits) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
		}

        int depth = 1;

        for(int i = 1; i < depthBits; i++)
            depth <<= 1;

        depthAccuracy = depth;

        transparentList = new GraphicsCullOutputDetails[TRANSPARENT_LIST_SIZE];
        unsortedTransparentList = new GraphicsCullOutputDetails[TRANSPARENT_LIST_SIZE];
        depthList1 = new float[TRANSPARENT_LIST_SIZE];
        depthList2 = new int[TRANSPARENT_LIST_SIZE];
        countList = new int[depthAccuracy];

        wkPoint = new Point3f();
        modelMatrix = new Matrix4f();
        cameraMatrix = new Matrix4f();
        center = new float[3];

        matrixUtils = new MatrixUtils();
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
        {
            transparentList = new GraphicsCullOutputDetails[req_size];
            unsortedTransparentList = new GraphicsCullOutputDetails[req_size];
            depthList1 = new float[req_size];
            depthList2 = new int[req_size];
        }

        Matrix4f camera = data.viewTransform;
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
                        unsortedTransparentList[trans++] = nodes[i];
                        continue;
                    }
                    else if(!app.hasTransparencyInfo() && geom.hasTransparency())
                    {
                        unsortedTransparentList[trans++] = nodes[i];
                        continue;
                    }
                }

                if(geom.hasTransparency())
                {
                    unsortedTransparentList[trans++] = nodes[i];
                    continue;
                }
            }
            else if(nodes[i].renderable instanceof CustomRenderable)
            {
                if(((CustomRenderable)nodes[i].renderable).hasTransparency())
                {
                    unsortedTransparentList[trans++] = nodes[i];
                    continue;
                }
            }
            else
            {
				I18nManager intl_mgr = I18nManager.getManager();
				Locale lcl = intl_mgr.getFoundLocale();
				String msg_pattern = intl_mgr.getString(INVALID_REND_MSG_PROP);

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

            idx = appendOutputShape(idx, nodes[i], instr);
        }

        // Now process the transparency listing. If there is no transparent
        // objects, then exit now.
        switch(trans)
        {
            case 0:
                return idx;

            case 1:
                instr.renderOps[idx++] = RenderOp.START_TRANSPARENT;

                idx = appendOutputShape(idx,
                                        unsortedTransparentList[0],
                                        instr);

                // clear it from the list
                unsortedTransparentList[0] = null;

                instr.renderOps[idx++] = RenderOp.STOP_TRANSPARENT;
                return idx;
        }

        // Time to work out the distance values.
        float max_depth = Float.NEGATIVE_INFINITY;
        float min_depth = Float.POSITIVE_INFINITY;

        matrixUtils.inverse(camera, cameraMatrix);

        for(int i = 0; i < trans && !terminate; i++)
        {
            // transform the center of the bounds first to world space and then
            // into camera space to work out the depth.
            modelMatrix.set(unsortedTransparentList[i].transform);
            ShapeRenderable shape = (ShapeRenderable)unsortedTransparentList[i].renderable;
            shape.getCenter(center);

            wkPoint.x = center[0];
            wkPoint.y = center[1];
            wkPoint.z = center[2];

            modelMatrix.transform(wkPoint);
            cameraMatrix.transform(wkPoint);

            depthList1[i] = wkPoint.z;

            if(max_depth < wkPoint.z)
                max_depth = wkPoint.z;

            if(min_depth > wkPoint.z)
                min_depth = wkPoint.z;
        }


        // Now work out the range of values available and how to quantize those
        // to the given accuracy. Take 1 from the depth accuracy so that the max
        // depth will always end up at one less than the final array size - to
        // avoid AAOB exceptions.
        float depth_range = max_depth - min_depth;
        float quanta = (depthAccuracy - 1)/ depth_range;

        // Change all the values from floats to quantised ints
        for(int i = 0; i < trans; i++)
            depthList2[i] = (int)((depthList1[i] - min_depth) * quanta);

        if(terminate)
            return idx;

        // Now perform the counting sort on them. Initialise all the counts to
        // 0 again
        for(int i = 0; i < depthAccuracy; i++)
            countList[i] = 0;

        if(terminate)
            return idx;

        for(int j = 0; j < trans; j++)
            countList[depthList2[j]]++;

        if(terminate)
            return idx;

        // countList now contains the number of elements equal to i
        for(int i = 1; i < depthAccuracy; i++)
            countList[i] += countList[i - 1];

        if(terminate)
            return idx;

        for(int j = trans; --j >= 0 && !terminate; )
        {
            int dl = depthList2[j];
            transparentList[countList[dl] - 1] = unsortedTransparentList[j];
            countList[dl]--;
        }

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
        // NOTE:
        // Since this is 2D and there is no depth associated with a 2D scene
        // we just go through looking for the bitmaps that have alpha values.
        // Those get rendered after the ones that do not. There's no depth
        // sorting to be done.
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
                        unsortedTransparentList[trans++] = nodes[i];
                        continue;
                    }
                    else if(!app.hasTransparencyInfo() && geom.hasTransparency())
                    {
                        unsortedTransparentList[trans++] = nodes[i];
                        continue;
                    }
                }

                if(geom.hasTransparency())
                {
                    unsortedTransparentList[trans++] = nodes[i];
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
				String msg_pattern = intl_mgr.getString(INVALID_REND_MSG_PROP);

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
     * @param scene The scene bucket to use for the source
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
            Matrix4f tx = node.transform;

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

                instr.renderList[idx].renderable = shape;
                instr.renderOps[idx] = RenderOp.START_RENDER;
                idx++;

                instr.renderList[idx].renderable = shape;
                instr.renderOps[idx] = RenderOp.STOP_RENDER;
                idx++;
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

            Matrix4f tx = node.transform;

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
