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

// Standard imports
// None

// Application specific imports
// None

/**
 * Utility functionality for bounds management.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class BoundsUtils
{
    /** Temporary variables for fetching values */
    private float[] wkVec1;
    private float[] wkVec2;

    /**
     * Construct a default instance of the utils
     */
    public BoundsUtils()
    {
        wkVec1 = new float[3];
        wkVec2 = new float[3];
    }

    /**
     * Ensure that the parent bounds contains the child bounds entirely. It is
     * assumed that the bounds are in the same coordinate system.
     *
     * @param parent The parent bound to expand to include this bounds
     * @param child The child to make sure the parent contains
     */
    public void combine(BoundingVolume parent, BoundingVolume child)
    {
        switch(parent.getType())
        {
            case BoundingVolume.SPHERE_BOUNDS:
                break;

            case BoundingVolume.BOX_BOUNDS:
                break;
        }
    }

    /**
     * Ensure that the parent bounds contains the child bounds entirely. It is
     * assumed that the bounds are in the same coordinate system.
     *
     * @param parent The parent bound to expand to include this point
     * @param point Coordinates of the point to make sure the bounds include
     */
    public void combine(BoundingVolume parent, float[] point)
    {
        switch(parent.getType())
        {
            case BoundingVolume.SPHERE_BOUNDS:
                BoundingSphere sp = (BoundingSphere)parent;
                parent.getCenter(wkVec1);
                float r2 = sp.getRadiusSquared();
                float a = point[0] - wkVec1[0];
                float b = point[1] - wkVec1[1];
                float c = point[2] - wkVec1[2];
                float rad_sq = a * a + b * b + c * c;
                if(rad_sq > r2)
                    sp.setRadius((float)Math.sqrt(rad_sq), rad_sq);
                break;

            case BoundingVolume.BOX_BOUNDS:
                BoundingBox box = (BoundingBox)parent;
                box.getExtents(wkVec1, wkVec2);
                if(wkVec1[0] < point[0] ||
                   wkVec1[1] < point[1] ||
                   wkVec1[2] < point[2])
                    box.setMinimum(point);
                else if(wkVec2[0] > point[0] ||
                        wkVec2[1] > point[1] ||
                        wkVec2[2] > point[2])
                    box.setMaximum(point);
                break;
        }
    }


    /**
     * Set the parent bounds to be the smallest container of the child bounds.
     * The value of the containing bounds will be set to the minimal container.
     *
     * @param parent The bounds to set to contain the children
     * @param children Listing of all the bounds to contain
     */
    public void contain(BoundingVolume parent, BoundingVolume[] children)
    {
        switch(parent.getType())
        {
            case BoundingVolume.SPHERE_BOUNDS:
                contain((BoundingSphere)parent, children);
                break;

            case BoundingVolume.BOX_BOUNDS:
                contain((BoundingBox)parent, children);
                break;
        }
    }

    /**
     * Internal combination method to contain a sphere.
     *
     * @param parent The bounds to set to contain the children
     * @param children Listing of all the bounds to contain
     */
    private void contain(BoundingSphere parent, BoundingVolume[] children)
    {
        // find all the centers, and average them
        float x = 0;
        float y = 0;
        float z = 0;

        for(int i = 0; i < children.length; i++)
        {
            children[i].getCenter(wkVec1);
            x += wkVec1[0];
            y += wkVec1[1];
            z += wkVec1[2];
        }

        float div = 1 / children.length;
        x *= div;
        y *= div;
        z *= div;

        wkVec2[0] = x;
        wkVec2[1] = y;
        wkVec2[2] = z;

        parent.setCenter(wkVec2);

        // Check for the max radius squared to the farthest
        // extent of all children. That with the greatest sets the radius
        float rad_sq = 0;
        float r;

        for(int i = 0; i < children.length; i++)
        {
            children[i].getExtents(wkVec1, wkVec2);
            float a = x - wkVec1[0];
            float b = y - wkVec1[1];
            float c = z - wkVec1[2];

            r = a * a + b * b + c * c;
            if(r > rad_sq)
                rad_sq = r;

            a = x - wkVec2[0];
            b = y - wkVec2[1];
            c = z - wkVec2[2];

            r = a * a + b * b + c * c;
            if(r > rad_sq)
                rad_sq = r;
        }

        parent.setRadius((float)Math.sqrt(rad_sq), rad_sq);
    }

    /**
     * Internal combination method to contain a sphere.
     *
     * @param parent The bounds to set to contain the children
     * @param children Listing of all the bounds to contain
     */
    private void contain(BoundingBox parent, BoundingVolume[] children)
    {
        children[0].getExtents(wkVec1, wkVec2);

        float min_x = wkVec1[0];
        float min_y = wkVec1[1];
        float min_z = wkVec1[2];

        float max_x = wkVec2[0];
        float max_y = wkVec2[1];
        float max_z = wkVec2[2];

        for(int i = 1; i < children.length; i++)
        {
            children[i].getExtents(wkVec1, wkVec2);

            max_x = max_x > wkVec2[0] ? max_x : wkVec2[0];
            max_y = max_y > wkVec2[1] ? max_y : wkVec2[1];
            max_z = max_z > wkVec2[2] ? max_z : wkVec2[2];

            min_x = min_x < wkVec1[0] ? min_x : wkVec1[0];
            min_y = min_y < wkVec1[1] ? min_y : wkVec1[1];
            min_z = min_z < wkVec1[2] ? min_z : wkVec1[2];
        }

        parent.setMinimum(min_x, min_y, min_z);
        parent.setMaximum(max_x, max_y, max_z);
    }
}
