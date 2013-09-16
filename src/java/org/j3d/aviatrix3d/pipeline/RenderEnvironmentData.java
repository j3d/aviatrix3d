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

package org.j3d.aviatrix3d.pipeline;

// External imports
import javax.vecmath.Matrix4f;

// Local imports
import org.j3d.aviatrix3d.rendering.ObjectRenderable;
import org.j3d.aviatrix3d.rendering.ProfilingData;

/**
 * Data holder class used to pass the current environment data along the rendering
 * pipeline.
 * <p>
 *
 * End-user code should never be making use of this class unless the end user is
 * implementing a customised rendering pipeline. This class is used as a simple
 * internal collection of the per-frame renderable data that is passed along
 * each stage of the pipeline.
 *
 * @author Justin Couch
 * @version $Revision: 2.6 $
 */
public class RenderEnvironmentData
{
    /** Any user provided data registered with the effects processor */
    public Object userData;

    /** The current viewpoint instance */
    public ObjectRenderable viewpoint;

    /** Matrix representing the view frustum transformation */
    public Matrix4f viewTransform;

    /**
     * The ID of the layer this data represents  0 is the front-most active layer and
     * increases from there. As a layer may have multiple viewports, it is possible that
     * multiple instances of this class may have the same layer ID in a given frame.
     */
    public int layerId;

    /**
     * The ID of the layer that is contained within a single viewport that this
     * data represents.
     */
    public int subLayerId;

    /** The profiling timing data */
    public ProfilingData profilingData;

    /**
     * Create a new instance of this class. The variables are initialized
     * to their default values and arrays constructed.
     */
    public RenderEnvironmentData()
    {
        viewTransform = new Matrix4f();
    }
}
