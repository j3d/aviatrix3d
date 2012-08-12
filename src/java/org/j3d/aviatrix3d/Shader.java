/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.rendering.ShaderRenderable;

/**
 * Base representation of the functionality common across the various shader
 * types that can be applied to geometry.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public abstract class Shader extends NodeComponent
    implements ShaderRenderable
{
    /**
     * Constructs a Shader with default values.
     */
    public Shader()
    {
    }
}
