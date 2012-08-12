/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2007
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
// None

// Local imports
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;
import org.j3d.aviatrix3d.rendering.ShaderSourceRenderable;

/**
 * Data holder for passing graphics-oriented renderable requests
 * through the rendering pipeline.
 * <p>
 *
 * This class adds graphics-specific rendering requests to the generic
 * requests provided in the base class.
 *
 * Since this is not a high-use class, it is expected that a new instance will
 * be created each time that the class is needed. All arrays are the exact
 * length of the number of items to be processed. Any array that has no data
 * to be processed shall be null.
 *
 * @author Justin Couch
 * @version $Revision: 3.1 $
 */
public class GraphicsRequestData extends RenderableRequestData
{
    /** The change requestors for data changed sets */
    public ShaderSourceRenderable[] shaderInitList;

    /** The change requestors for bounds changed sets */
    public ShaderSourceRenderable[] shaderLogList;
}
