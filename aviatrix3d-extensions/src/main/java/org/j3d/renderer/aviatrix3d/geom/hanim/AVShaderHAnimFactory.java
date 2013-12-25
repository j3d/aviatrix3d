/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005-
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom.hanim;

// External imports
// None

// Local imports
import org.j3d.geom.hanim.*;

/**
 * An implementation of the {@link HAnimFactory} that provide nodes that
 * implements skinned mesh rendering using GLSL shaders.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class AVShaderHAnimFactory implements HAnimFactory
{
    /**
     * Create a new default Displacer instance.
     *
     * @return a new instance of the HAnimDisplacer object
     */
    @Override
    public HAnimDisplacer createDisplacer()
    {
        return new HAnimDisplacer();
    }

    /**
     * Create a new default Site instance.
     *
     * @return a new instance of the HAnimSite object
     */
    @Override
    public HAnimSite createSite()
    {
        return new AVSite();
    }

    /**
     * Create a new default Segment instance.
     *
     * @return a new instance of the HAnimSegment object
     */
    @Override
    public HAnimSegment createSegment()
    {
        return new AVSegment();
    }

    /**
     * Create a new default Joint instance.
     *
     * @return a new instance of the HAnimJoint object
     */
    @Override
    public HAnimJoint createJoint()
    {
        return new ShaderJoint();
    }

    /**
     * Create a new default Humanoid instance.
     *
     * @return a new instance of the HAnimHumanoid object
     */
    @Override
    public HAnimHumanoid createHumanoid()
    {
        return new ShaderHumanoid();
    }

    /**
     * Create a new empty manager instance.
     *
     * @return a new instance of the HumanoidManager object
     */
    @Override
    public HumanoidManager createManager()
    {
        return new HumanoidManager();
    }
}
