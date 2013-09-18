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

package org.j3d.aviatrix3d;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.rendering.*;

/**
 * An appearance that can be placed higher in the tree other than the
 * Appearance node and overrides the appearance of all objects lower in the
 * tree.
 * <p>
 *
 * This class can be used to override the appearance of everything in the tree,
 * for example, to highlight a sub section of parts of a greater assembly.
 * Two different types of behaviours are allowed: it can be used to always
 * override everything below, or if can be provided as a backup if there is no
 * appearance provided at the leaf nodes.
 * <p>
 *
 * An additional configuration option is available in either mode. When the
 * local appearance option is false it will add any missing appearance items
 * from the leaf node Appearance class instance that it does not have locally
 * specified. This allows you to, for example, provide a global shader, but
 * leave the definition of the material and normal map textures to the
 * individual shape node instances, without having to cart the shader around
 * everywhere. Another way of thinking about this, is that content loaded from
 * external sources that have colours and normal maps defined, don't have to
 * worry about loading in the root shader to change rendering styles as part of
 * the loader process. So long as your loader is consistent about which index
 * each texture goes in, you don't have to care about what rendering style
 * is applied globally (ie, normal texture is always texture unit 0, colour
 * texture always texture unit 1 etc etc).
 *
 * @author Justin Couch
 * @version $Revision: 2.5 $
 */
public class AppearanceOverride extends Leaf
    implements LeafCullable, OverrideRenderable
{
    /** Appearance to render the geometry with */
    private Appearance app;

    /** Enabled state flag */
    private boolean enabled;

    /** Flag for the override lower state */
    private boolean overrideLowerApps;

    /**
     * Should we accumulate other appearance properties at the leaf nodes or
     * should we only use our own.
     */
    private boolean localAppearanceOnly;

    /**
     * Creates an override that is disabled by default and does not override
     * the lower items on the tree.
     */
    public AppearanceOverride()
    {
        enabled = false;
        overrideLowerApps = false;
        localAppearanceOnly = true;
    }

    //---------------------------------------------------------------
    // Methods defined by LeafCullable
    //---------------------------------------------------------------

    /**
     * Get the type that this cullable represents.
     *
     * @return One of the _CULLABLE constants
     */
    @Override
    public int getCullableType()
    {
        return OVERRIDE_CULLABLE;
    }

    /**
     * Get the child renderable of this object.
     *
     * @return an array of nodes
     */
    @Override
    public Renderable getRenderable()
    {
        return this;
    }

    //---------------------------------------------------------------
    // Methods defined by OverrideRenderable
    //---------------------------------------------------------------

    /**
     * Check which direction has preference for the appearance handling. If
     * this is true then this instance will override those lower down the
     * scene graph. If false, then those lower on the tree override this
     * instance.
     *
     * @return true if this overrides instances lower
     */
    @Override
    public boolean overrideLower()
    {
        return overrideLowerApps;
    }

    /**
     * Check to see if this is a local appearance only or it should accumulate
     * the leaf node colours/textures.
     *
     * @return true if we should only use the appearance node details from here
     */
    @Override
    public boolean useLocalOnlyAppearance()
    {
        return localAppearanceOnly;
    }

    /**
     * Fetch the renderable that represents the visual appearance modifiers of
     * this shape.
     *
     * @return The current appearance renderable or null if none
     */
    @Override
    public AppearanceRenderable getAppearanceRenderable()
    {
        return app;
    }

    //----------------------------------------------------------
    // Methods defined by CascadeRenderable
    //----------------------------------------------------------

    /**
     * Get the current enabled state of the light.
     *
     * @return The current state
     */
    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    //---------------------------------------------------------------
    // Methods defined by Node
    //---------------------------------------------------------------

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * scene graph.
     *
     * @param state true if this should be marked as live now
     */
    @Override
    protected void setLive(boolean state)
    {
        // Ignore stuff that doesn't change the state
        if(state == alive)
            return;

        if(app != null)
            app.setLive(state);

        // This will also force the geometry recompute of the bounds.
        super.setLive(state);
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //---------------------------------------------------------------

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    @Override
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        if(app != null)
            app.setUpdateHandler(updateHandler);
    }

    /**
     * Check to see if this node is the same reference as the passed node that
     * is a parent of this node. This is the downwards check to ensure that
     * there is no cyclic scene graph structures at the point where someone
     * adds a node to the scenegraph. When the reference and this are the
     * same, an exception is generated. Since each class may have different
     * lists of child node setups, this should be overriden by any class that
     * can take children, and have the call passed along to the children.
     *
     * @param parent The reference to check against this class
     * @throws CyclicSceneGraphStructureException Equal parent and child
     */
    @Override
    protected void checkForCyclicChild(SceneGraphObject parent)
        throws CyclicSceneGraphStructureException
    {
        if(parent == this)
            throw new CyclicSceneGraphStructureException();

        if(app != null)
            app.checkForCyclicChild(parent);

        // Don't bother with geometry for now. That's just wasted CPU cycles.
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The objec to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    @Override
    public int compareTo(Object o)
        throws ClassCastException
    {
        AppearanceOverride app = (AppearanceOverride)o;
        return compareTo(app);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof AppearanceOverride))
            return false;
        else
            return equals((AppearanceOverride)o);
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Set the enabled state of this override. Can use this to turn it on and off
     * in a general fashion.
     *
     * @param state The new state of the override
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setEnabled(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        enabled = state;
    }

    /**
     * Set the override function to determine whether we should override
     * lower appearances or not.
     *
     * @param state Set true if this should trump lower instances in the tree
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setOverrideLower(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        overrideLowerApps = state;
    }

    /**
     * Check which direction has preference for the appearance handling. If
     * this is true then this instance will override those lower down the
     * scene graph. If false, then those lower on the tree override this
     * instance.
     *
     * @return true if this overrides instances lower
     */
    public boolean isOverridingLower()
    {
        return overrideLowerApps;
    }

    /**
     * Set the override function to determine whether we should override
     * also include any missing appearance options that are defined in leaf
     * Appearance nodes on the Shape nodes. This will not include any lower
     * override appearance instances in the tree, only the leaf nodes. Default
     * is to not use this ability.
     *
     * @param state Set true if this should trump lower instances in the tree
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setLocalAppearanceOnly(boolean state)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        localAppearanceOnly = state;
    }

    /**
     * Check whether the override shoudl include from the leaf Appearance nodes
     * any missing definitions.
     *
     * @return true if this overrides instances lower
     */
    public boolean isLocalAppearanceOnly()
    {
        return localAppearanceOnly;
    }

    /**
     * Get the current appearance associated with this shape.
     *
     * @return the current instance
     */
    public Appearance getAppearance()
    {
        return app;
    }

    /**
     * Set the appearance for this shape.
     *
     * @param newApp The appearance
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data changed callback method
     */
    public void setAppearance(Appearance newApp)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(getDataWriteTimingMessage());

        if(app != null)
            app.setLive(false);

        app = newApp;

        if(app != null)
        {
            app.setLive(alive);
            app.setUpdateHandler(updateHandler);
        }
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param sh The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(AppearanceOverride sh)
    {
        if(sh == null)
            return 1;

        if(sh == this)
            return 0;

        if(overrideLowerApps != sh.overrideLowerApps)
            return overrideLowerApps ? 1 : -1;

        if(app != sh.app)
        {
            if(app == null)
                return -1;
            else if(sh.app == null)
                return 1;

            int res = app.compareTo(sh.app);
            if(res != 0)
                return res;
        }

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param sh The shape instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(AppearanceOverride sh)
    {
        if(sh == this)
            return true;

        if(sh == null)
            return false;

        if(overrideLowerApps != sh.overrideLowerApps)
            return false;

        if((app != sh.app) && ((app == null) || !app.equals(sh.app)))
            return false;

        return true;
    }
}
