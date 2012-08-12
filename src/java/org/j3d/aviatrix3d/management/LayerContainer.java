/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.management;

// External imports
import java.util.ArrayList;

// Local imports
import org.j3d.aviatrix3d.*;

/**
 * A grouping node that can have multiple parents, thus allowing a graph
 * structure to the scene graph.
 *
 * Normal nodes cannot have more than one parent, so this class provides
 * the ability to have more than one. In doing so, it overrides the normal
 * methods provided by Node to provide the shared functionality.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class LayerContainer extends BaseSceneGraphObject
{
    /** The list of children nodes */
    private ArrayList<Layer> layers;

    /**
     * The default constructor
     */
    LayerContainer()
    {
         layers = new ArrayList<Layer>();
    }

    //----------------------------------------------------------
    // Methods defined by SceneGraphObject
    //----------------------------------------------------------

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
    protected void checkForCyclicChild(SceneGraphObject parent)
        throws CyclicSceneGraphStructureException
    {
        if(parent == this)
            throw new CyclicSceneGraphStructureException();

        for(int i = 0; i < layers.size(); i++)
        {
            Layer l = layers.get(i);

            if(l != null)
                checkForCyclicChild(l, parent);
        }
    }

    /**
     * Set the scenegraph update handler for this node.  It will notify
     * all its children of the value. A null value will clear the current
     * handler.
     *
     * @param handler The instance to use as a handler
     */
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        for(int i = 0; i < layers.size(); i++)
        {
            Layer l = layers.get(i);

            if(l != null)
                setUpdateHandler(l);
        }
    }

    /**
     * Notification that this object is live now.
     */
    protected void setLive(boolean state)
    {
        // Ignore stuff that doesn't change the state
        if(state == alive)
            return;

        for(int i = 0; i < layers.size(); i++)
        {
            Layer l = layers.get(i);

            if(l != null)
                setLive(l, state);
        }

        // Call this after, that way the bounds are recalculated here with
        // the correct bounds of all the children set up.
        super.setLive(state);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------


    /**
     * Change the layers over to the new set. This will leave any existing
     * layers in place (and liveness state) and adding or removing old
     * layers.
     *
     * @param layers The collection of layers, in order, to render
     * @param numLayers The number of valid layers to use
     */
    void changeLayers(Layer[] newLayers, int numLayers)
    {
        ArrayList<Layer> old_layers = layers;

        layers = new ArrayList<Layer>();
        boolean live = isLive();

        for(int i = 0; i < numLayers; i++)
        {
            if(old_layers.contains(newLayers[i]))
            {
                old_layers.remove(newLayers[i]);
                layers.add(newLayers[i]);
            }
            else
            {
                layers.add(newLayers[i]);
                setLive(newLayers[i], live);
                setUpdateHandler(newLayers[i]);
            }
        }

        // Set to non-live and no update handler any layers that are left.
        for(int i = 0; i < old_layers.size(); i++)
        {
            Layer l = old_layers.get(i);
            setLive(l, false);
            clearUpdateHandler(l);
        }
    }
}
