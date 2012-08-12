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
import org.j3d.aviatrix3d.rendering.LayerCullable;
import org.j3d.aviatrix3d.rendering.ViewportCullable;

/**
 * An abstract layer definition that can be applied at the root of the
 * scene graph.
 * <p>
 *
 * A layer is a composite of objects that are applied in a sequential manner
 * to the given surface. Between each layer the depth buffer is cleared and
 * a new rendering is applied directly over the top of the previous. Colour
 * buffers or other buffers are not cleared.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public abstract class Layer extends SceneGraphObject
    implements LayerCullable
{
    /** The layer is a simple type, having single 3D viewport defined */
    public static final int SIMPLE = 0;

    /** The layer is a composite type, having many viewports defined */
    public static final int COMPOSITE = 1;

    /**
     * The layer is a 2D type, having only 2D rendering used and a single
     * viewport is defined.
     */
    public static final int SIMPLE_2D = 2;

    /**
     * The layer is a 2D type, having only 2D rendering used and a many
     * viewports defined.
     */
    public static final int COMPOSITE_2D = 3;



    /** The layer type constant */
    protected final int layerType;

    /**
     * Construct a new layer of the given type. One of the standard types may
     * be used, or a custom type.
     *
     * @param type The type constant for this layer
     */
    protected Layer(int type)
    {
        layerType = type;
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
    public boolean equals(Object o)
    {
        if(!(o instanceof Layer))
            return false;
        else
            return equals((Layer)o);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the cullable corresponding to the given internal buffer ID. This
     * should never be called by user code. It's the method that the rendering
     * pipeline uses to interface between the primary scene graph and the
     * internal multithreaded representation.
     *
     * @param buffer The ID of the buffer to fetch the cullable for
     * @return The corresponding culling representation
     */
    public LayerCullable getCullable(int buffer)
    {
        return this;
    }

    /**
     * Get the type that this layer represents
     *
     * @return A layer type descriptor
     */
    public int getType()
    {
        return layerType;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object. Derived instances
     * should override this to add texture-specific extensions.
     *
     * @param layer The layer instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Layer layer)
    {
        if(layer == null)
            return 1;

        if(layer == this)
            return 0;

        // are we the same type of layer?
        if(layer.layerType != layerType)
            return layerType < layer.layerType ? -1 : 1;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param layer The layer instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(Layer layer)
    {
        if(layer == this)
            return true;

        return (layer.layerType == layerType);
    }
}
