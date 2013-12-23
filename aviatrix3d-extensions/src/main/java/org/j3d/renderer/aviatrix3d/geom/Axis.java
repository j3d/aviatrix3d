/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
 *                              Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom;

// External imports
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.vecmath.Matrix4f;
import javax.vecmath.AxisAngle4f;

import org.j3d.util.I18nManager;

import org.j3d.geom.BoxGenerator;
import org.j3d.geom.ConeGenerator;
import org.j3d.geom.CoordinateUtils;
import org.j3d.geom.GeometryData;

// Local imports
import org.j3d.aviatrix3d.*;

/**
 * Representation of a set of axis around the coordinates.
 * <p>
 *
 * Each axis is color coordinated and the length can be adjusted.
 * <p>
 * X axis: Red<br>
 * Y axis: Green<br>
 * Z axis: Blue
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>negAxisMsg: Error message when the axis is less than zero</li>
 * </ul>
 *
 * @author Jason Taylor, based on the work by Justin Couch
 * @version $Revision: 1.3 $
 */
public class Axis extends Group
{
    /** User provided axis length was not positive */
    private static final String NEG_AXIS_PROP =
        "org.j3d.renderer.aviatrix3d.geom.Axis.negAxisMsg";

    /** Message when the drawing mode is not a valid value */
    private static final String INVALID_ALPHA_PROP =
        "org.j3d.renderer.aviatrix3d.geom.Axis.alphaComponentRangeMsg";

    /** The default length of the axis */
    private static final float DEFAULT_AXIS_LENGTH = 5;

    /** The size of the box shape on the end */
    private static final float DEFAULT_X_SIZE = 0.05f;

    /**
     * Create a default axis object with each item length 5 from the origin
     */
    public Axis()
    {
        this(DEFAULT_AXIS_LENGTH, 1);
    }
    /**
     * Create an axis object with the given axis length from the origin.
     *
     * @param length The length to use. Must be positive
     */
    public Axis(float length)
    {
        this(length, 1);
    }
    /**
     * Create an axis object with the given axis length from the origin.
     * The transparency of the axis can be controlled through the use of the
     * second parameter. It follows the standard alpha values. A value of
     * 0 is not visible, a value of 1 is completely visible.
     *
     * @param length The length to use. Must be positive
     * @param transparency The amount of alpha channel in the axis
     */
    public Axis(float length, float transparency)
    {
        if(length <= 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NEG_AXIS_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(length) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if((transparency < 0) || (transparency > 1))
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_ALPHA_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(transparency) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        float X_SIZE = DEFAULT_X_SIZE;

        // Increases the thickness in propotion to the length if longer then default
        if(length > DEFAULT_AXIS_LENGTH)
            X_SIZE = length * 0.04f;

        // Create a single item of geometry and then share/rotate as needed
        BoxGenerator box_gen = new BoxGenerator(X_SIZE, length, X_SIZE);

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        box_gen.generate(data);

        TriangleStripArray axis_array = new TriangleStripArray();
        axis_array.setVertices(TriangleStripArray.COORDINATE_3,
                               data.coordinates,
                               data.vertexCount);
        axis_array.setNormals(data.normals);
        axis_array.setStripCount(data.stripCounts, data.numStrips);

        ConeGenerator cone_gen = new ConeGenerator(X_SIZE * 4, X_SIZE * 2, 4);
        data.geometryType = GeometryData.TRIANGLE_FANS;
        data.vertexCount = 0;
        data.coordinates = null;
        data.normals = null;
        data.stripCounts = null;

        cone_gen.generate(data);

        CoordinateUtils cu = new CoordinateUtils();
        cu.translate(data.coordinates,
                     data.vertexCount,
                     0,
                     length * 0.5f + (X_SIZE * 2),
                     0);

        TriangleStripArray cone_array = new TriangleStripArray();
        cone_array.setVertices(TriangleStripArray.COORDINATE_3,
                               data.coordinates,
                               data.vertexCount);
        cone_array.setStripCount(data.stripCounts, data.numStrips);
        cone_array.setNormals(data.normals);

        float[] blue = {0, 0, 0.8f};
        Material blue_material = new Material();
        blue_material.setDiffuseColor(blue);
        blue_material.setLightingEnabled(true);
        blue_material.setTransparency(transparency);

        float[] red = {0.8f,0, 0};
        Material red_material = new Material();
        red_material.setDiffuseColor(red);
        red_material.setLightingEnabled(true);
        red_material.setTransparency(transparency);

        float[] green = {0, 0.8f, 0};
        Material green_material = new Material();
        green_material.setDiffuseColor(green);
        green_material.setLightingEnabled(true);
        green_material.setTransparency(transparency);

        Appearance x_app = new Appearance();
        x_app.setMaterial(red_material);

        Appearance y_app = new Appearance();
        y_app.setMaterial(green_material);

        Appearance z_app = new Appearance();
        z_app.setMaterial(blue_material);


        Shape3D x_shape1 = new Shape3D();
        x_shape1.setAppearance(x_app);
        x_shape1.setGeometry(axis_array);

        Shape3D x_shape2 = new Shape3D();
        x_shape2.setAppearance(x_app);
        x_shape2.setGeometry(cone_array);

        Shape3D y_shape1 = new Shape3D();
        y_shape1.setAppearance(y_app);
        y_shape1.setGeometry(axis_array);

        Shape3D y_shape2 = new Shape3D();
        y_shape2.setAppearance(y_app);
        y_shape2.setGeometry(cone_array);

        Shape3D z_shape1 = new Shape3D();
        z_shape1.setAppearance(z_app);
        z_shape1.setGeometry(axis_array);

        Shape3D z_shape2 = new Shape3D();
        z_shape2.setAppearance(z_app);
        z_shape2.setGeometry(cone_array);

        // The three axis values are all pointing up along the Y axis. Apply a
        // transform to X and Z to move them to the correct position.

        Matrix4f tx = new Matrix4f();
        tx.setIdentity();
        AxisAngle4f angle = new AxisAngle4f();

        // X Axis first
        tx.rotZ(-(float)(Math.PI * 0.5f));

        TransformGroup x_tg = new TransformGroup(tx);
        x_tg.setTransform(tx);
        x_tg.addChild(x_shape1);
        x_tg.addChild(x_shape2);

        tx.rotX((float)(Math.PI * 0.5f));

        TransformGroup z_tg = new TransformGroup(tx);
        z_tg.setTransform(tx);
        z_tg.addChild(z_shape1);
        z_tg.addChild(z_shape2);

        addChild(x_tg);
        addChild(y_shape1);
        addChild(y_shape2);
        addChild(z_tg);
    }
}
