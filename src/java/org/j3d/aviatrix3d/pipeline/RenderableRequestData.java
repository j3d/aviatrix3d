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

package org.j3d.aviatrix3d.pipeline;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.rendering.DeletableRenderable;

/**
 * Data holder for passing renderable requests through the rendering pipeline.
 * <p>
 *
 * This class, and any derived from it, are used to pass renderables through
 * the pipeline untouched. These renderables are requesting some other form
 * of rendering services other than the basic drawing routines that the rest
 * of the pipeline processes. This basic class passes through objects that
 * have requested that they be deleted (ie texture object ID needs to be
 * replaced).
 * <p>
 *
 * Since this is not a high-use class, it is expected that a new instance will
 * be created each time that the class is needed. All arrays are the exact
 * length of the number of items to be processed. Any array that has no data
 * to be processed shall be null.
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public class RenderableRequestData
{
    /** Objects that have requested deletion processing */
    public DeletableRenderable[] deletionRequests;
}
