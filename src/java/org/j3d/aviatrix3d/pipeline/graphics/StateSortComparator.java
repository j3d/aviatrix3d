/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2004 - 2006
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
import java.util.HashMap;
import java.util.Comparator;

// Local imports
import org.j3d.aviatrix3d.rendering.*;

/**
 * A comparator that provides the mechanics for state sorting, given
 * configurable priority listing.
 * <p>
 *
 * Since this is a comparator, the basic sorting algorithm will be capped at
 * O(n log n) as given by the lower bounds for any comparison-based sort.
 * <p>
 *
 * <h3>Comparison Rules</h3>
 * <p>
 *
 * The following rules are used for comparing objects of the same type:
 * <p>
 *
 * <p>
 * Bias towards objects with state first over objects with no state. Thus
 * given two objects a and b. If b is null, a is greater than b and return
 * +1. If a is null and b is non null, return -1.
 * <p>
 *
 * Where no explicit test is defined, use a per-object test with it's
 * appropriate <code>compareTo()</code> method. If the method returns 0 then
 * continue on with the next in the priority order, otherwise exit with the
 * appropriate state set.
 *
 * <h4>Lights</h4>
 *
 * <ol>
 * <li>Number of lights are zero, ignore the test</li>
 * <li>Number of lights is not equal, -1 to the lower number</li>
 * <li>Use per-light comparison</li>
 * </ol>
 *
 * <h4>Textures</h4>
 *
 * <ol>
 * <li>Number of texture units are zero, ignore the test</li>
 * <li>Number of texture units is not equal, -1 to the lower number</li>
 * <li>Use per-texture unit comparison</li>
 * </ol>
 *
 * @author Justin Couch
 * @version $Revision: 3.5 $
 */
public class StateSortComparator 
    implements Comparator<GraphicsCullOutputDetails>
{
    /** Priority state is lights */
    public static final int LIGHTS = 1;

    /** Priority state is material values  */
    public static final int MATERIALS = 2;

    /** Priority state is fragment shaders  */
    public static final int FRAGMENT_SHADERS = 3;

    /** Priority state is vertex shaders  */
    public static final int VERTEX_SHADERS = 4;

    /** Priority state is shader objects (ie GL 1.5/2.0)  */
    public static final int SHADER_OBJECTS = 5;

    /** Priority state is checking polygon attributes */
    public static final int POLYGON_ATTRIBS = 6;

    /** Priority state is checking line attributes */
    public static final int LINE_ATTRIBS = 7;

    /** Priority state is checking point attributes */
    public static final int POINT_ATTRIBS = 8;

    /**
     * Priority state is checking blend attributes. Theoretically we should
     * Never see this here as the blending attributes should be pre-sorted
     * out of the listing first. But, if we're going to do that, we may have
     * to deal with a separate set of state sorting for the blended geometry
     * from the main geometry.
     */
    public static final int BLEND_ATTRIBS = 9;

    /** Priority state is checking clip planes */
    public static final int CLIP_PLANES = 10;

    /** Priority state is checking local fog */
    public static final int LOCAL_FOG = 11;

    /** Priority state is checking depth buffer attributes */
    public static final int DEPTH_ATTRIBS = 12;

    /** Priority state is checking stencil buffer attributes */
    public static final int STENCIL_ATTRIBS = 13;

    /** Priority state is texture unit 0 */
    public static final int TEXTURE_UNIT_0 = 32;

    /** Priority state is texture unit 1 */
    public static final int TEXTURE_UNIT_1 = 33;

    /** Priority state is texture unit 2 */
    public static final int TEXTURE_UNIT_2 = 34;

    /** Priority state is texture unit 3 */
    public static final int TEXTURE_UNIT_3 = 35;


    /** The default ordering to sort our priorities based on. */
    private static final int[] DEFAULT_PRIORITIES =
    {
        LIGHTS,
        TEXTURE_UNIT_0,
        TEXTURE_UNIT_1,
        TEXTURE_UNIT_2,
        TEXTURE_UNIT_3,
        VERTEX_SHADERS,
        FRAGMENT_SHADERS,
        MATERIALS,
        LOCAL_FOG,
        POLYGON_ATTRIBS,
        LINE_ATTRIBS,
        POINT_ATTRIBS,
        BLEND_ATTRIBS,
        DEPTH_ATTRIBS,
        STENCIL_ATTRIBS,
        CLIP_PLANES
    };


    /** Listing of priorities */
    private int[] priorityOrder;

    /**
     * Construct an instance of this comparator using the default priority
     * settings.
     */
    public StateSortComparator()
    {
        this(null);
    }

    /**
     * Construct an instance of this comparator given the listed set of
     * priorities. If any priority is repeated, the second (or more) instance
     * is ignored. If the list instance is null or zero length, the default
     * order is assumed.
     *
     * @param priList The listing of priorities to use
     */
    public StateSortComparator(int[] priList)
    {
        if((priList == null) || (priList.length == 0))
            priList = DEFAULT_PRIORITIES;

        priorityOrder = new int[priList.length];

        // check using a simple run through the list. SUre it's n^2, but since
        // we're only expecting 8-10 values, this is relatively trivial time
        // cost.
        int idx = 0;
        for(int i = 0; i < priList.length; i++)
        {
            boolean exists = false;

            for(int j = 0; j < idx; j++)
            {
                if(priorityOrder[j] == priList[i])
                {
                    exists = true;
                    break;
                }
            }

            if(!exists)
                priorityOrder[idx++] = priList[i];
        }
    }

    /**
     * Compares its two arguments for order. Returns a negative integer, zero,
     * or a positive integer as the first argument is less than, equal to, or
     * greater than the second.
     *
     * @param cd1 The first object to be compared
     * @param cd2 The second object to be compared
     * @return -1, 0, or 1 depending on equality
     * @throws ClassCastException Can't be compared as the types are wrong
     */
    public int compare(GraphicsCullOutputDetails cd1,
                       GraphicsCullOutputDetails cd2)
    {
        // Can this test be ignored? We'll certainly never be inserting the
        // same instance of GraphicsCullOutputDetails twice into the graph. It's more
        // likely that the lights or something else would be first.
        if(cd1 == cd2)
            return 0;

        // Break the two apart into the component pieces, making the checks
        // easier. Non-Shape3D objects (CustomRenderable) are considered
        // having no graphics state, and are sorted last.
        ShapeRenderable shape1 = null;
        ShapeRenderable shape2 = null;
        AppearanceRenderable app1 = null;
        AppearanceRenderable app2 = null;

        if(cd1.renderable instanceof ShapeRenderable)
        {
            shape1 = (ShapeRenderable)cd1.renderable;
            app1 = shape1.getAppearanceRenderable();
        }

        if(cd2.renderable instanceof ShapeRenderable)
        {
            shape2 = (ShapeRenderable)cd2.renderable;
            app2 = shape2.getAppearanceRenderable();
        }

        // if the two appearances or shapes are the same instance, no point
        // going any further. Just test for the lights and clip planes being
        // different only. If there is no difference, these are equal and
        // return straight away.
        if((shape1 == shape2) || (app1 == app2))
        {
            if(cd1.numLights == cd2.numLights)
            {
                if(cd1.numLights == 0)
                {
                    EffectRenderable fog1 = cd1.localFog;
                    EffectRenderable fog2 = cd2.localFog;

                    if(fog1 != fog2)
                    {
                        if(fog1 == null)
                            return -1;
                        else if(fog2 == null)
                            return 1;

                        int res = fog1.compareTo(fog2);
                        if(res != 0)
                            return res;
                    }

                    // Now check the clip planes are the same
                    if(cd1.numClipPlanes == cd2.numClipPlanes)
                    {
                        if(cd1.numClipPlanes == 0)
                            return 0;

                        // Some planes are the same, let's see how many
                        VisualDetails[] cp1 = cd1.clipPlanes;
                        VisualDetails[] cp2 = cd2.clipPlanes;

                        for(int j = 0; j < cd1.numClipPlanes; j++)
                        {
                            int res = cp1[j].compareTo(cp2[j]);
                            if(res != 0)
                                return res;
                        }
                    }
                    else
                    {
                        return cd1.numClipPlanes < cd2.numClipPlanes ? -1 : 1;
                    }
                }

                // Some lights are the same, let's see how many
                VisualDetails[] ld1 = cd1.lights;
                VisualDetails[] ld2 = cd2.lights;

                for(int j = 0; j < cd1.numLights; j++)
                {
                    int res = ld1[j].compareTo(ld2[j]);
                    if(res != 0)
                        return res;
                }
            }
            else
            {
                return cd1.numLights < cd2.numLights ? -1 : 1;
            }
			//////////////////////////////////////////////////////////////
			// rem: change to 'fix' sorting when using TimSort
			/*
			if(shape1 != shape2)
			{
				GeometryRenderable geom1 = shape1.getGeometryRenderable();
				GeometryRenderable geom2 = shape2.getGeometryRenderable();
				
				if (geom1 != geom2) 
				{
					int res = geom1.compareTo(geom2);
					if (res != 0) 
					{
						return res;
					}
				}
			}
			*/
			//////////////////////////////////////////////////////////////
            return 0;
        }

        // They're not the same, and they're not both null, so quick check to
        // see if either is null. If so, we just bail right now as nothing else
        // is going to change the order.
        if(app1 == null)
            return -1;
        else if(app2 == null)
            return 1;

        int num_tu1 = 0;
        int num_tu2 = 0;

        for(int i = 0; i < priorityOrder.length; i++)
        {
            switch(priorityOrder[i])
            {
                case LIGHTS:
                    // For now, assume that the lights are going to be declared
                    // in the same order for both objects. This may be a wrong
                    // Assumption as the culling and scene graph get more
                    // sophisticated, but
                    if(cd1.numLights == cd2.numLights)
                    {
                        if(cd1.numLights == 0)
                            continue;

                        // Some lights are the same, let's see how many
                        VisualDetails[] ld1 = cd1.lights;
                        VisualDetails[] ld2 = cd2.lights;

                        for(int j = 0; j < cd1.numLights; j++)
                        {
                            int res = ld1[j].compareTo(ld2[j]);
                            if(res != 0)
                                return res;
                        }
                    }
                    else
                    {
                        return cd1.numLights < cd2.numLights ? -1 : 1;
                    }

                    break;

                case MATERIALS:
                    ObjectRenderable mat1 = app1.getMaterialRenderable();
                    ObjectRenderable mat2 = app2.getMaterialRenderable();

                    if(mat1 == mat2)
                        continue;

                    if(mat1 == null)
                        return -1;
                    else if(mat2 == null)
                        return 1;

                    int res = mat1.compareTo(mat2);
                    if(res != 0)
                        return res;

                    break;

                case LOCAL_FOG:
                    EffectRenderable fog1 = cd1.localFog;
                    EffectRenderable fog2 = cd2.localFog;

                    if(fog1 == fog2)
                        continue;

                    if(fog1 == null)
                        return -1;
                    else if(fog2 == null)
                        return 1;

                    res = fog1.compareTo(fog2);
                    if(res != 0)
                        return res;

                    break;

                case SHADER_OBJECTS:
                    ShaderRenderable sh1 = app1.getShaderRenderable();
                    ShaderRenderable sh2 = app2.getShaderRenderable();

                    if(sh1 == sh2)
                        continue;

                    if(sh1 == null)
                        return -1;
                    else if(sh2 == null)
                        return 1;

                    res = sh1.compareTo(sh2);
                    if(res != 0)
                        return res;

                    break;

                case FRAGMENT_SHADERS:
                    sh1 = app1.getShaderRenderable();
                    sh2 = app2.getShaderRenderable();

                    if(sh1 == sh2)
                        continue;

                    if(sh1 == null)
                        return -1;
                    else if(sh2 == null)
                        return 1;

                    ShaderComponentRenderable sc1 =
                        sh1.getShaderRenderable(ShaderComponentRenderable.FRAGMENT_SHADER);
                    ShaderComponentRenderable sc2 =
                        sh2.getShaderRenderable(ShaderComponentRenderable.FRAGMENT_SHADER);

                    if(sc1 == sc2)
                        continue;

                    if(sc1 == null)
                        return -1;
                    else if(sc2 == null)
                        return 1;

                    // Need to do comparison here based on strings
                    break;

                case VERTEX_SHADERS:
                    sh1 = app1.getShaderRenderable();
                    sh2 = app2.getShaderRenderable();

                    if(sh1 == sh2)
                        continue;

                    if(sh1 == null)
                        return -1;
                    else if(sh2 == null)
                        return 1;

                    sc1 = sh1.getShaderRenderable(ShaderComponentRenderable.VERTEX_SHADER);
                    sc2 = sh2.getShaderRenderable(ShaderComponentRenderable.VERTEX_SHADER);

                    if(sc1 == sc2)
                        continue;

                    if(sc1 == null)
                        return -1;
                    else if(sc2 == null)
                        return 1;

                    break;

                case POLYGON_ATTRIBS:
                    AppearanceAttributeRenderable aa1 =
                        app1.getAttributeRenderable(AppearanceAttributeRenderable.POLYGON_ATTRIBUTE);
                    AppearanceAttributeRenderable aa2 =
                        app2.getAttributeRenderable(AppearanceAttributeRenderable.POLYGON_ATTRIBUTE);

                    if(aa1 == aa2)
                        continue;

                    if(aa1 == null)
                        return -1;
                    else if(aa2 == null)
                        return 1;

                    res = aa1.compareTo(aa2);
                    if(res != 0)
                        return res;

                    break;

                case LINE_ATTRIBS:
                    aa1 = app1.getAttributeRenderable(AppearanceAttributeRenderable.LINE_ATTRIBUTE);
                    aa2 = app2.getAttributeRenderable(AppearanceAttributeRenderable.LINE_ATTRIBUTE);

                    if(aa1 == aa2)
                        continue;

                    if(aa1 == null)
                        return -1;
                    else if(aa2 == null)
                        return 1;

                    res = aa1.compareTo(aa2);
                    if(res != 0)
                        return res;

                    break;

                case POINT_ATTRIBS:
                    aa1 = app1.getAttributeRenderable(AppearanceAttributeRenderable.POINT_ATTRIBUTE);
                    aa2 = app2.getAttributeRenderable(AppearanceAttributeRenderable.POINT_ATTRIBUTE);

                    if(aa1 == aa2)
                        continue;

                    if(aa1 == null)
                        return -1;
                    else if(aa2 == null)
                        return 1;

                    res = aa1.compareTo(aa2);
                    if(res != 0)
                        return res;

                    break;

                case BLEND_ATTRIBS:
                    aa1 = app1.getAttributeRenderable(AppearanceAttributeRenderable.BLEND_ATTRIBUTE);
                    aa2 = app2.getAttributeRenderable(AppearanceAttributeRenderable.BLEND_ATTRIBUTE);

                    if(aa1 == aa2)
                        continue;

                    if(aa1 == null)
                        return -1;
                    else if(aa2 == null)
                        return 1;

                    res = aa1.compareTo(aa2);
                    if(res != 0)
                        return res;

                    break;

                case DEPTH_ATTRIBS:
                    aa1 = app1.getAttributeRenderable(AppearanceAttributeRenderable.DEPTH_ATTRIBUTE);
                    aa2 = app2.getAttributeRenderable(AppearanceAttributeRenderable.DEPTH_ATTRIBUTE);

                    if(aa1 == aa2)
                        continue;

                    if(aa1 == null)
                        return -1;
                    else if(aa2 == null)
                        return 1;

                    res = aa1.compareTo(aa2);
                    if(res != 0)
                        return res;

                    break;

                case STENCIL_ATTRIBS:
                    aa1 = app1.getAttributeRenderable(AppearanceAttributeRenderable.STENCIL_ATTRIBUTE);
                    aa2 = app2.getAttributeRenderable(AppearanceAttributeRenderable.STENCIL_ATTRIBUTE);

                    if(aa1 == aa2)
                        continue;

                    if(aa1 == null)
                        return -1;
                    else if(aa2 == null)
                        return 1;

                    res = aa1.compareTo(aa2);
                    if(res != 0)
                        return res;

                    break;

                case CLIP_PLANES:
                    // For now, assume that the clip planes are going to be declared
                    // in the same order for both objects. This may be a wrong
                    // Assumption as the culling and scene graph get more
                    // sophisticated, but
                    if(cd1.numClipPlanes == cd2.numClipPlanes)
                    {
                        if(cd1.numClipPlanes == 0)
                            continue;

                        // Some lights are the same, let's see how many
                        VisualDetails[] cp1 = cd1.clipPlanes;
                        VisualDetails[] cp2 = cd2.clipPlanes;

                        for(int j = 0; j < cd1.numClipPlanes; j++)
                        {
                            res = cp1[j].compareTo(cp2[j]);
                            if(res != 0)
                                return res;
                        }
                    }
                    else
                    {
                        return cd1.numClipPlanes < cd2.numClipPlanes ? -1 : 1;
                    }

                    break;

                default:
                    if(priorityOrder[i] >= TEXTURE_UNIT_0)
                    {
                        int texture_unit = priorityOrder[i] - TEXTURE_UNIT_0;

                        // OpenGL only allows 32 texture units so this may be a
                        // bogus value.
                        if(texture_unit > 32)
                            continue;

                        num_tu1 = app1.numTextureRenderables();
                        num_tu2 = app2.numTextureRenderables();

                        ComponentRenderable tu1 = app1.getTextureRenderable(texture_unit);
                        ComponentRenderable tu2 = app2.getTextureRenderable(texture_unit);

                        if((num_tu1 == 0 && num_tu2 == 0) || (tu1 == tu2))
                            continue;
                        else if((num_tu1 == 0) || (tu1 == null))
                            return -1;
                        else if((num_tu2 == 0) || (tu2 == null))
                            return 1;

                        res = tu1.compareTo(tu2);
                        if(res != 0)
                            return res;
                    }
            }
        }

        // if everything has passed here then they must be equal.
        return 0;
    }

    /**
     * Indicates whether some other object is "equal to" this Comparator by
     * checking to see if it is the same class instance, and then if the
     * priority listing between the two is the same.
     */
    public boolean equals(Object obj)
    {
        if(!(obj instanceof StateSortComparator))
            return false;

        StateSortComparator ssc = (StateSortComparator)obj;

        for(int i = 0; i < priorityOrder.length; i++)
            if(priorityOrder[i] != ssc.priorityOrder[i])
                return false;

        return true;
    }
}
