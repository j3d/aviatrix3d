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
import java.util.Arrays;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import javax.media.opengl.GL;

import org.j3d.util.MatrixUtils;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

import org.j3d.aviatrix3d.pipeline.RenderOp;

/**
 * Implementation of the sort stage that does everything - state sorting and
 * depth sorted transparency.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidDepthMsg: Message when the user provides a number of depth bits
 *     that are not positive</li>
 * <li>invalidRenderableMsg: Message when we encounter an unknown renderable type
 *     during state sorting</li>
 * <li>countSizeMsg: Message that is primarily used
 *     for debugging when we completely messed up the depth count array size</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 3.20 $
 */
public class StateAndTransparencyDepthSortStage extends BaseStateSortStage
{
    /** Message when the number of depth bits <= 0 */
    private static final String INVALID_DEPTH_MSG_PROP =
        "org.j3d.aviatrix3d.pipeline.graphics.StateAndTransparencyDepthSortStage.invalidDepthMsg";

    /** Message when an unknown renderable is found */
    private static final String INVALID_REND_MSG_PROP =
        "org.j3d.aviatrix3d.pipeline.graphics.StateAndTransparencyDepthSortStage.invalidRenderableMsg";

    /** Error message when we get the depth count wrong */
    private static final String COUNT_SIZE_MSG_PROP =
        "org.j3d.aviatrix3d.pipeline.graphics.StateAndTransparencyDepthSortStage.countSizeMsg";

    /** The initial size of the transparent list */
    private static final int TRANSPARENT_LIST_SIZE = 200;

    /** The guess at the number of components in a shape */
    private static final int GUESS_NUM_COMPONENTS = 5;

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

    /** Temporary array for holding the opaque objects before sorting */
    private GraphicsCullOutputDetails[] opaqueList;

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

    // Listing of the current items, while we're setting up the state handling.

    /** The requested size for the arrays */
    private int reqdSize;

    /** Matrix utility code for doing inversions */
    private MatrixUtils matrixUtils;

    /**
     * Create an empty sorting stage that assumes just a single renderable
     * output.
     */
    public StateAndTransparencyDepthSortStage()
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
    public StateAndTransparencyDepthSortStage(int numSurfaces)
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
    public StateAndTransparencyDepthSortStage(int numSurfaces, int depthBits)
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
        opaqueList = new GraphicsCullOutputDetails[TRANSPARENT_LIST_SIZE];

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
        return 4 + scene.numNodes * GUESS_NUM_COMPONENTS;
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
        int instr_count = 2;

        for(int i = 0; i < scene.mainScene.numPasses; i++)
        {
            // Start/stop pass commands + up to 4 buffer state start
            // and stop commands.
            instr_count += 10 + scene.mainScene.numNodes[i] * GUESS_NUM_COMPONENTS;
        }

        return instr_count;
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
    protected int sortNodes(GraphicsCullOutputDetails[] nodes,
                            int numNodes,
                            GraphicsEnvironmentData data,
                            GraphicsInstructions instr,
                            int instrCount)
    {
        int new_size = estimateInstructionSize(nodes, numNodes);

        if(new_size > reqdSize) {
            reqdSize = new_size;

            realloc(instr, reqdSize);
            reqdSize = instr.renderList.length;
        }

        if(transparentList.length < reqdSize)
        {
            transparentList = new GraphicsCullOutputDetails[reqdSize];
            unsortedTransparentList = new GraphicsCullOutputDetails[reqdSize];

            depthList1 = new float[reqdSize];
            depthList2 = new int[reqdSize];

            opaqueList = new GraphicsCullOutputDetails[reqdSize];
        }

        Matrix4f camera = data.viewTransform;
        int idx = instrCount;
        int trans = 0;
        int opaque = 0;

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
                        unsortedTransparentList[trans++] = nodes[i];
                    else if(app.hasTransparencyInfo())
                    {
                        opaqueList[opaque++] = nodes[i];
                    }
                    else if(geom.hasTransparency())
                    {
                        new_size++;
                        unsortedTransparentList[trans++] = nodes[i];
                    }
                    else
                        opaqueList[opaque++] = nodes[i];

                    continue;
                }

                if(geom.hasTransparency())
                {
                    new_size++;
                    unsortedTransparentList[trans++] = nodes[i];
                    continue;
                }
                else
                    opaqueList[opaque++] = nodes[i];
            }
            else if(nodes[i].renderable instanceof CustomRenderable)
            {
                if(((CustomRenderable)nodes[i].renderable).hasTransparency())
                {
                    new_size++;
                    unsortedTransparentList[trans++] = nodes[i];
                }
                else
                    opaqueList[opaque++] = nodes[i];
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

            opaqueList[opaque++] = nodes[i];
        }

        if(terminate)
            return idx;

        Arrays.sort(opaqueList, 0, opaque, stateComparator);

        if(terminate)
            return idx;

        int start = 0;
        boolean done = false;

        while(!done)
        {
            try
            {
                for(int i = start; i < opaque && !terminate; i++)
                    idx = appendObject(opaqueList[i], instr, idx);

                done = true;
            }
            catch(ArrayIndexOutOfBoundsException be)
            {
                start = idx;

                realloc(instr, reqdSize);
            }
        }

        done = false;

        while(!done)
        {
            try
            {
                idx = cleanupObjects(instr, idx);
                done = true;
            }
            catch(ArrayIndexOutOfBoundsException be)
            {
                realloc(instr, reqdSize);
            }
        }

        new_size = idx + 1 + trans << 1;

        if(new_size > reqdSize)
            reqdSize = new_size;

        if(trans > 0 && instr.renderList.length < reqdSize)
            realloc(instr, reqdSize);

        if(terminate)
            return idx;


        // Now process the transparency listing. If there is no transparent
        // objects, then exit now.

        switch(trans)
        {
            case 0:
                return idx;

            case 1:
                instr.renderOps[idx++] = RenderOp.START_TRANSPARENT;

                idx = appendObject(unsortedTransparentList[0],
                                   instr, idx);

                // clear it from the list
                unsortedTransparentList[0] = null;

                done = false;

                while(!done)
                {
                    try
                    {
                        idx = cleanupObjects(instr, idx);
                        done = true;
                    }
                    catch(ArrayIndexOutOfBoundsException be)
                    {
                        realloc(instr, reqdSize);
                    }
                }

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
        float quanta = (depthAccuracy - 1) / depth_range;

        // Change all the values from floats to quantised ints
        for(int i = 0; i < trans; i++)
            depthList2[i] = (int)Math.floor(((depthList1[i] - min_depth) * quanta));

        // Now perform the counting sort on them. Initialise all the counts to
        // 0 again
        for(int i = 0; i < depthAccuracy; i++)
            countList[i] = 0;

        // TODO: Debug info to
        int jidx = 0;
        try
        {
            for(; jidx < trans; jidx++)
                countList[depthList2[jidx]]++;
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(COUNT_SIZE_MSG_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args =
            {
                depthList2[jidx],
                depth_range,
				quanta,
				depthAccuracy,
				min_depth
            };

            Format[] fmts = { n_fmt, n_fmt, n_fmt, n_fmt, n_fmt };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);

            String msg = msg_fmt.format(msg_args);

            errorReporter.warningReport(msg, e);
        }

        if(terminate)
            return idx;

        // countList now contains the number of elements equal to i
        for(int i = 1; i < depthAccuracy; i++)
            countList[i] += countList[i - 1];

        if(terminate)
            return idx;

        for(int j = trans; --j >= 0; )
        {
            int dl = depthList2[j];
            transparentList[countList[dl] - 1] = unsortedTransparentList[j];
            countList[dl]--;
        }

        instr.renderOps[idx++] = RenderOp.START_TRANSPARENT;

        done = false;
        start = 0;

        while(!done)
        {
            try
            {
                for(int i = start; i < trans && !terminate; i++)
                {
                    idx = appendObject(transparentList[i], instr, idx);

                    // clear it from the list
                    transparentList[i] = null;
                }

                done = true;
            }
            catch(ArrayIndexOutOfBoundsException be)
            {
                start = idx;

                realloc(instr, reqdSize);
            }
        }

        // Shouldn't be anything to clean up, but just make sure.
        done = false;

        while(!done)
        {
            try
            {
                idx = cleanupObjects(instr, idx);
                done = true;
            }
            catch(ArrayIndexOutOfBoundsException be)
            {
                realloc(instr, reqdSize);
            }
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
        int opaque = 0;

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
                        unsortedTransparentList[trans++] = nodes[i];
                    else if(app.hasTransparencyInfo())
                    {
                        opaqueList[opaque++] = nodes[i];
                    }
                    else if(geom.hasTransparency())
                    {
                        req_size++;
                        unsortedTransparentList[trans++] = nodes[i];
                    }
                    else
                        opaqueList[opaque++] = nodes[i];
                }

                if(geom.hasTransparency())
                {
                    req_size++;
                    unsortedTransparentList[trans++] = nodes[i];
                    continue;
                }
                else
                    opaqueList[opaque++] = nodes[i];
            }
            else if(nodes[i].renderable instanceof CustomRenderable)
            {
                if(((CustomRenderable)nodes[i].renderable).hasTransparency())
                {
                    req_size++;
                    transparentList[trans++] = nodes[i];
                    continue;
                }
                else
                    opaqueList[opaque++] = nodes[i];
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
            idx = appendObject(nodes[i], instr, idx);
        }

        // Now process the transparency listing. If there is no transparent
        // objects, then exit now.
        if(trans == 0)
            return idx;

        // Basically the same now as the first loop, just running through the
        // transparency listing rather than the node listing. Note that the
        // difference between the 2D and 3D versions is that we don't do any
        // depth sorting here. In a 2D scene, there is no concept of depth, so
        // we just ignore the sorting and drop everything onto the transparent
        // list as-is. Order is not important.
        instr.renderOps[idx++] = RenderOp.START_TRANSPARENT;

        for(int i = 0; i < trans && !terminate; i++)
        {
            idx = appendObject(transparentList[i], instr, idx);

            // clear it from the list
            transparentList[i] = null;
        }

        instr.renderOps[idx++] = RenderOp.STOP_TRANSPARENT;
        return idx;
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

}
