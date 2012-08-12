/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2007
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

// Local imports
import org.j3d.aviatrix3d.rendering.ProfilingData;

/**
 * Graphics specific profiling data.
 *
 * @author Alan Hudson
 */
public class GraphicsProfilingData extends ProfilingData
{
    /** The total number of triangles in the scene */
    public long numTriangles;

    /** The total number of renderables in the scene */
    public long numRenderables;
}
