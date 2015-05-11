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
 * Simple internal class for managing all the layers at the top of a scene graph
 * like a normal node for propagation of updates and live state.
 *
 * @author Justin Couch
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
         layers = new ArrayList<>();
    }

    //----------------------------------------------------------
    // Methods defined by SceneGraphObject
    //----------------------------------------------------------

    @Override
    protected void checkForCyclicChild(SceneGraphObject parent)
        throws CyclicSceneGraphStructureException
    {
        if(parent == this)
        {
            throw new CyclicSceneGraphStructureException();
        }

        for(int i = 0; i < layers.size(); i++)
        {
            Layer l = layers.get(i);

            if(l != null)
            {
                checkForCyclicChild(l, parent);
            }
        }
    }

    @Override
    protected void setUpdateHandler(NodeUpdateHandler handler)
    {
        super.setUpdateHandler(handler);

        for(int i = 0; i < layers.size(); i++)
        {
            Layer l = layers.get(i);

            if(l != null)
            {
                setUpdateHandler(l);
            }
        }
    }

    @Override
    protected void setLive(boolean state)
    {
        // Ignore stuff that doesn't change the state
        if(state == alive)
        {
            return;
        }

        for(int i = 0; i < layers.size(); i++)
        {
            Layer l = layers.get(i);

            if(l != null)
            {
                setLive(l, state);
            }
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
     * @param newLayers The collection of layers, in order, to render
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
