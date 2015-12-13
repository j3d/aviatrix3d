/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2005
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
import com.jogamp.opengl.GL;

// Local imports
// None

/**
 * Renderable object that applies some visual effect down the children
 * cullables of the scene graph and can be overriden.
 * <p>
 *
 * This interface is a close sibling to {@link EffectRenderable}, of which it
 * shares many common traits, but differs in several distinct ways. Firstly the
 * effects renderables have a fixed override pattern - classes lower down the
 * scene graph of the same time always override the parent. This interface is
 * configurable by the end user to allow either top down or bottom up overrides.
 * Secondly, the effects renderable adds additional rendering properties to the
 * basic appearance. This class overrides the appearance directly with a new
 * appearance.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface OverrideRenderable extends CascadeRenderable
{
    /**
     * Check which direction has preference for the appearance handling. If
     * this is true then this instance will override those lower down the
     * scene graph. If false, then those lower on the tree override this
     * instance.
     *
     * @return true if this overrides instances lower
     */
    public boolean overrideLower();

    /**
     * Fetch the renderable that represents the visual appearance modifiers of
     * the leaf nodes. If no renderable is returned then it is treated as
     * though it is not enabled, and ignored by the culling.
     *
     * @return The current appearance renderable or null if none
     */
    public AppearanceRenderable getAppearanceRenderable();

    /**
     * Check to see if this is a local appearance only or it should accumulate
     * the leaf node colours/textures.
     *
     * @return true if we should only use the appearance node details from here
     */
    public boolean useLocalOnlyAppearance();
}
