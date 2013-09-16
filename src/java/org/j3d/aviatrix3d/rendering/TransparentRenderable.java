/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.rendering;

// External imports
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import javax.media.opengl.GL;

// Local imports
// None

/**
 * Marker interface for all leaf objects that wish to define themselves as
 * being potentially transparent and thus needed to be bucketed in the state
 * sorting process for separate treatment.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public interface TransparentRenderable extends Renderable
{
    /**
     * Check to see if this renderable object has anything that could be
     * interpreted as an alpha value. For example a Raster with RGBA values or
     * vertex geometry with 4-component colours for the vertices. Transparency
     * information is needed for depth sorting during rendering.
     */
    public boolean hasTransparency();
}
