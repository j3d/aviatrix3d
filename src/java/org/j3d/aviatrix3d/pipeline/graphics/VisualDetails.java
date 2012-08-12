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

package org.j3d.aviatrix3d.pipeline.graphics;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.rendering.Renderable;

/**
 * Class for passing the detailed rendering information about Renderables that
 * effect the visual output of a specific object.
 * <p>
 *
 * Visual details are those that are passed down the scene graph in a hierachical
 * manner, such as lights and clip planes. These objects are declared in
 * grouping nodes and effect all the children below them.
 *
 * @author Justin Couch
 * @version $Revision: 3.2 $
 */
public class VisualDetails implements Comparable<VisualDetails>
{
    /** The hash of this object */
    private int hash;

    /** The hash of this object */
    private int transHash;

    /** The lead node instance to be rendered. Will be either Shape or Clip */
    private Renderable renderable;

    /** The transform from the root of the scene graph to here */
    private float[] transform;

    /**
     * Construct a default instance with just the transform initialised
     */
    public VisualDetails()
    {
        transform = new float[16];
        hash = 0;
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param vd The details to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(VisualDetails vd)
    {
        if(vd == this)
            return 0;

        if(vd == null)
            return 1;

        if(renderable != vd.renderable)
        {
            if(renderable == null)
               return -1;
            else if(vd.renderable == null)
                return 1;

            int res = renderable.compareTo(vd.renderable);
            if(res != 0)
                return res;
        }

        return 0;
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare the given details to this one to see if they are equal. Equality
     * is defined as pointing to the same clipPlane source, with the same
     * transformation value.
     *
     * @param o The object to compare against
     * @return true if these represent identical objects
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof VisualDetails))
            return false;

        VisualDetails ld = (VisualDetails)o;

        if(renderable != ld.renderable)
            return false;

        return true;
    }

    /**
     * Calculate the hashcode for this object.
     *
     */
    public int hashCode()
    {
        return hash;
    }

	public int transHashCode() {
		return transHash;
	}

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Update the details with a new set of values.
     *
     * @param r The Renderable instance to be used
     * @param tx The transform array to copy
     */
    public void update(Renderable r, float[] tx)
    {
        renderable = r;
        System.arraycopy(tx, 0, transform, 0, 16);

        // Now regenerate the hash code
        float h = 0;

        // Original code, below is faster but is it as good?
/*
        for(int i = 0; i < 16; i++)
            h = 31 * h + Float.floatToIntBits(transform[i]);
*/

/*
        // Fastest so far!
        h = 31 * transform[0] + transform[1];
        h = 31 * h + transform[2];
        h = 31 * h + transform[3];
        h = 31 * h + transform[4];
        h = 31 * h + transform[5];
        h = 31 * h + transform[6];
        h = 31 * h + transform[7];
        h = 31 * h + transform[8];
        h = 31 * h + transform[9];
        h = 31 * h + transform[10];
        h = 31 * h + transform[11];
        h = 31 * h + transform[12];
        h = 31 * h + transform[13];
        h = 31 * h + transform[14];
        h = 31 * h + transform[15];

        long ans = Float.floatToIntBits(h) + renderable.hashCode();


        transHash = (int)(ans & 0xFFFFFFFF);
*/
		hash = transform.hashCode() + renderable.hashCode();
    }

    /**
     * Return the current transform array. Don't play with the return value.
     *
     * @return The current transform matrix array
     */
    public float[] getTransform()
    {
        return transform;
    }

    /**
     * Get the current clipPlane instance in use.
     *
     * @return A reference to the current clipPlane
     */
    public Renderable getRenderable()
    {
        return renderable;
    }

    /**
     * Clear the light reference and reset the entire object back to an empty
     * state. Used to clear references so that they are not held when the
     * object is not in use, thus prevent GC.
     */
    public void clear()
    {
        // Don't bother reseting the transform. It is effectively empty useless
        // at this point.
        renderable = null;
        hash = 0;
    }
}
