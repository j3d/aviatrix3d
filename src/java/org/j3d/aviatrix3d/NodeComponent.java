/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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
import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;

// Local imports
// None

/**
 * The NodeComponent class is the superclass for all non renderable nodes.
 * These nodes provides data for other nodes.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public abstract class NodeComponent extends SceneGraphObject
{
    /**
     * Issue ogl commands needed for this component.
     *
     * @param gld The drawable context to use
     */
    public void renderState(GL gl, GLU glu)
    {
    }

    /**
     * Restore all openGL state.
     *
     * @param gld The drawable context to use
     */
    public void restoreState(GL gl, GLU glu)
    {
    }

    /**
     * Has an attribute been changed.  If not then the
     * renderState/restoreState might not be called.  Components
     * that always need to update state should set this to true.
     *
     * @return Has an attribute changed.
     */
    public boolean hasChanged()
    {
        return true;
    }
}