/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
 *                          Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.Appearance;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.Shape3D;
import org.j3d.aviatrix3d.TriangleStripArray;
import org.j3d.aviatrix3d.VertexGeometry;

import org.j3d.geom.TorusGenerator;
import org.j3d.geom.GeometryData;

/**
 * A simple torus that uses triangle strips.
 * <p>
 *
 * As we assume you may want to use this as a collidable object, we store the
 * {@link GeometryData} instance that is used to create the object in the
 * userData of the underlying {@link org.j3d.aviatrix3d.TriangleStripArray}.
 * The geometry does not have texture coordinates set.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class Torus extends Shape3D
    implements NodeUpdateListener
{
    /** The default outer radius of the torus */
    private static final float DEFAULT_ORADIUS = 1.0f;

    /** The default inner radius of the torus */
    private static final float DEFAULT_IRADIUS = 0.25f;

    /** Default number of segments used in the outer radius */
    private static final int DEFAULT_OFACETS = 16;

    /** Default number of segments used in the inner radius */
    private static final int DEFAULT_IFACETS = 16;

    /** The generator used to modify the geometry */
    private TorusGenerator generator;

    /** Data used to regenerate the torus */
    private GeometryData data;

    /**
     * Construct a default torus with no appearance set. The default size
     * of the torus is: <BR>
     * Outer radius: 2.0<BR>
     * Inner radius: 1.0<BR>
     * Outer radius Faces:  16<BR>
     * Inner radius Faces:  16<BR>
     */
    public Torus()
    {
        this(DEFAULT_ORADIUS,
             DEFAULT_IRADIUS,
             DEFAULT_OFACETS,
             DEFAULT_IFACETS,
             null);
    }

    /**
     * Construct a default torus with the given appearance. The default size
     * of the torus is: <BR>
     * Outer radius: 2.0<BR>
     * Inner radius: 1.0<BR>
     * Outer radius Faces:  16<BR>
     * Inner radius Faces:  16<BR>
     *
     * @param app The appearance to use
     */
    public Torus(Appearance app)
    {
        this(DEFAULT_IRADIUS,
             DEFAULT_ORADIUS,
             DEFAULT_IFACETS,
             DEFAULT_OFACETS,
             app);
    }

    /**
     * Construct a default torus with no appearance set and a custom
     * number of faces. <BR>
     * Outer radius: 2.0<BR>
     * Inner radius: 1.0<BR>
     *
     * @param inner The number of faces to use around the inner radius
     * @param outer The number of faces to use around the outer radius
     */
    public Torus(int inner, int outer)
    {
        this(DEFAULT_IRADIUS, DEFAULT_ORADIUS, inner, outer, null);
    }

    /**
     * Construct a default torus with no appearance set. The height and
     * radius as set to the new value and uses the default face count of:<BR>
     * Outer radius Faces:  16<BR>
     * Inner radius Faces:  16<BR>
     *
     * @param innerRadius The inner radius of the torus
     * @param outerRadius The outer radius of the torus
     */
    public Torus(float innerRadius, float outerRadius)
    {
        this(innerRadius, outerRadius, DEFAULT_IFACETS, DEFAULT_OFACETS, null);
    }

    /**
     * Construct a default torus with the given appearance and a custom
     * number of faces. <BR>
     * Outer radius: 2.0<BR>
     * Inner radius: 1.0<BR>
     *
     * @param inner The number of faces to use around the inner radius
     * @param outer The number of faces to use around the outer radius
     * @param app The appearance to use
     */
    public Torus(int inner, int outer, Appearance app)
    {
        this(DEFAULT_ORADIUS, DEFAULT_IRADIUS, inner, outer, app);
    }

    /**
     * Construct a default torus with the given appearance. The height and
     * radius as set to the new value and uses the default face count of
     * Outer radius Faces:  16<BR>
     * Inner radius Faces:  16<BR>
     *
     * @param innerRadius The inner radius of the torus
     * @param outerRadius The outer radius of the torus
     * @param app The appearance to use
     */
    public Torus(float innerRadius, float outerRadius, Appearance app)
    {
        this(innerRadius, outerRadius, DEFAULT_IFACETS, DEFAULT_OFACETS, app);
    }

    /**
     * Construct a torus with all the values customisable.
     *
     * @param innerRadius The inner radius of the torus
     * @param outerRadius The outer radius of the torus
     * @param inner The number of faces to use around the inner radius
     * @param outer The number of faces to use around the outer radius
     * @param app The appearance to use
     */
    public Torus(float innerRadius,
                 float outerRadius,
                 int inner,
                 int outer,
                 Appearance app)
    {
        data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator = new TorusGenerator(innerRadius, outerRadius, inner, outer);
        generator.generate(data);

        TriangleStripArray geom = new TriangleStripArray();
        geom.setVertices(TriangleStripArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setStripCount(data.stripCounts, data.numStrips);
        geom.setNormals(data.normals);

        geom.setUserData(data);

        setAppearance(app);
        setGeometry(geom);
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener interface.
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src)
    {
        generator.generate(data);

        VertexGeometry geom = (VertexGeometry)src;
        geom.setVertices(TriangleStripArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        ((TriangleStripArray)geom).setStripCount(data.stripCounts,
                                                 data.numStrips);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src)
    {
        VertexGeometry geom = (VertexGeometry)src;
        geom.setNormals(data.normals);
    }

    /**
     * Change the radius and height of the torus to the new values. If the
     * geometry write capability has been turned off, this will not do
     * anything.
     *
     * @param innerRadius The inner radius of the torus
     * @param outerRadius The outer radius of the torus
     */
    public void setDimensions(float innerRadius, float outerRadius)
    {
        generator.setDimensions(innerRadius, outerRadius);

        VertexGeometry geom = (VertexGeometry)getGeometry();

        if(geom.isLive())
        {
            geom.boundsChanged(this);
            geom.dataChanged(this);
        }
        else
        {
            updateNodeBoundsChanges(geom);
            updateNodeDataChanges(geom);
        }
    }

    /**
     * Set the facet count of the torus to the new value. If the geometry
     * write capability has been turned off, this will not do anything.
     *
     * @param inner The number of faces to use around the inner radius
     * @param outer The number of faces to use around the outer radius
     */
    public void setFacetCount(int inner, int outer)
    {
        generator.setFacetCount(inner, outer);

        VertexGeometry geom = (VertexGeometry)getGeometry();

        if(geom.isLive())
        {
            geom.boundsChanged(this);
            geom.dataChanged(this);
        }
        else
        {
            updateNodeBoundsChanges(geom);
            updateNodeDataChanges(geom);
        }
    }
}
