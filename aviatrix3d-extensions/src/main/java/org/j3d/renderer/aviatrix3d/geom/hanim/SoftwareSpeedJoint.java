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
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.j3d.maths.vector.Matrix4d;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.geom.hanim.HAnimObject;

/**
 * Implementation of the joint object that does in-place software evaluation of
 * the skin mesh updates and is optimised for speed.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>jointTypeSingleMsg: Error message when a non-speed joint given</li>
 * <li>jointTypeMultiMsg: Error message when a non-speed joint given</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class SoftwareSpeedJoint extends SoftwareJoint
{
    /** Speed joint added to a space joint */
    private static final String WRONG_TYPE_SINGLE_PROP =
        "org.j3d.renderer.aviatrix3d.geom.hanim.SoftwareSpeedJoint.jointTypeSingleMsg";

    /** Speed joint added to a space joint */
    private static final String WRONG_TYPE_MULTI_PROP =
        "org.j3d.renderer.aviatrix3d.geom.hanim.SoftwareSpeedJoint.jointTypeMultiMsg";


    /**
     * Create a new, default instance of the site.
     */
    SoftwareSpeedJoint()
    {
    }

    //----------------------------------------------------------
    // Methods defined by HAnimJoint
    //----------------------------------------------------------

    /**
     * Replace the existing children with the new set of children.
     *
     * @param kids The collection of child objects to now use
     * @param numValid The number kids to copy from the given array
     */
    @Override
    public void setChildren(HAnimObject[] kids, int numValid)
    {
        for(int i = 0; i < numChildren; i++)
        {
            if(!(kids[i] instanceof SoftwareSpeedJoint))
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg_pattern = intl_mgr.getString(WRONG_TYPE_MULTI_PROP);

                Locale lcl = intl_mgr.getFoundLocale();
                NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

                String cls_name = kids[i] == null ? "null" : kids[i].getClass().getName();
                Object[] msg_args = { new Integer(i), cls_name };
                Format[] fmts = { null, n_fmt };
                MessageFormat msg_fmt =
                    new MessageFormat(msg_pattern, lcl);
                msg_fmt.setFormats(fmts);
                String msg = msg_fmt.format(msg_args);

                throw new IllegalArgumentException(msg);
            }
        }

        super.setChildren(kids, numValid);
    }

    /**
     * Add a child node to the existing collection. Duplicates and null values
     * are allowed.
     *
     * @param kid The new child instance to add
     */
    @Override
    public void addChild(HAnimObject kid)
    {
        if(!(kid instanceof SoftwareSpeedJoint))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            Locale lcl = intl_mgr.getFoundLocale();
            String msg_pattern = intl_mgr.getString(WRONG_TYPE_SINGLE_PROP);

            String cls_name = kid == null ? "null" : kid.getClass().getName();
            Object[] msg_args = { cls_name };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        super.addChild(kid);
    }

    /**
     * All the skeletal changes are in for this frame, so update the matrix
     * values now. This should not be callable by the general public. Derived
     * classes may override this method, but should call it as well to ensure
     * the internal matrices are correctly updated.
     *
     * @param parentTransform The transformation into global coordinates of
     *   the parent of this joint
     * @param parentChanged Flag to indicate that the parent transformation
     *   matrix has changed or is still the same as last call
     */
    @Override
    protected void updateSkeleton(Matrix4d parentTransform,
                                  boolean parentChanged)
    {
        super.updateSkeleton(parentTransform, parentChanged);

        // now update the weighted vertices using the global matrix
        if(outputCoords == null)
            return;

        // if either of the normal items are dodgy, ignore
        if(numSourceNormals == 0)
        {
            float[] c_buf = (float[])outputCoords;

            for(int i = 0; i < numSkinCoord; i++)
            {
                int index = skinCoordIndex[i];
                if(!dirtyCoordinates[index])
                    continue;

                float x = sourceCoords[index * 3];
                float y = sourceCoords[index * 3 + 1];
                float z = sourceCoords[index * 3 + 2];

                double out_x = globalMatrix.m00 * x + globalMatrix.m01 * y +
                              globalMatrix.m02 * z + globalMatrix.m03;
                double out_y = globalMatrix.m10 * x + globalMatrix.m11 * y +
                              globalMatrix.m12 * z + globalMatrix.m13;
                double out_z = globalMatrix.m20 * x + globalMatrix.m21 * y +
                              globalMatrix.m22 * z + globalMatrix.m23;

                c_buf[index * 3] += out_x * skinCoordWeight[i];
                c_buf[index * 3 + 1] += out_y * skinCoordWeight[i];
                c_buf[index * 3 + 2] += out_z * skinCoordWeight[i];
            }
        }
        else
        {
            float[] c_buf = (float[])outputCoords;
            float[] n_buf = (float[])outputNormals;

            for(int i = 0; i < numSkinCoord; i++)
            {
                int index = skinCoordIndex[i];
                if(!dirtyCoordinates[index])
                    continue;

                float cx = sourceCoords[index * 3];
                float cy = sourceCoords[index * 3 + 1];
                float cz = sourceCoords[index * 3 + 2];

                float nx = sourceNormals[index * 3];
                float ny = sourceNormals[index * 3 + 1];
                float nz = sourceNormals[index * 3 + 2];

                double out_cx = globalMatrix.m00 * cx + globalMatrix.m01 * cy +
                               globalMatrix.m02 * cz + globalMatrix.m03;
                double out_cy = globalMatrix.m10 * cx + globalMatrix.m11 * cy +
                               globalMatrix.m12 * cz + globalMatrix.m13;
                double out_cz = globalMatrix.m20 * cx + globalMatrix.m21 * cy +
                               globalMatrix.m22 * cz + globalMatrix.m23;

                double out_nx = globalMatrix.m00 * nx + globalMatrix.m01 * ny +
                               globalMatrix.m02 * nz;
                double out_ny = globalMatrix.m10 * nx + globalMatrix.m11 * ny +
                               globalMatrix.m12 * nz;
                double out_nz = globalMatrix.m20 * nx + globalMatrix.m21 * ny +
                               globalMatrix.m22 * nz;

                c_buf[index * 3] += out_cx * skinCoordWeight[i];
                c_buf[index * 3 + 1] += out_cy * skinCoordWeight[i];
                c_buf[index * 3 + 2] += out_cz * skinCoordWeight[i];

                n_buf[index * 3] += out_nx * skinCoordWeight[i];
                n_buf[index * 3 + 1] += out_ny * skinCoordWeight[i];
                n_buf[index * 3 + 2] += out_nz * skinCoordWeight[i];
            }
        }

        dirty = false;
    }
}
