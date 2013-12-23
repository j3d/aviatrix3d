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
import org.j3d.geom.CylinderGenerator;
import org.j3d.geom.GeometryData;

// Local imports
import org.j3d.aviatrix3d.Appearance;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.Shape3D;
import org.j3d.aviatrix3d.TriangleStripArray;
import org.j3d.aviatrix3d.VertexGeometry;

/**
 * A simple cylinder that uses triangle strips for rendering.
 * <p>
 *
 * As we assume you may want to use this as a collidable object, we store the
 * {@link GeometryData} instance that is used to create the object in the
 * userData of the underlying {@link org.j3d.aviatrix3d.TriangleStripArray}.
 * The geometry does not have texture coordinates set.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class Cylinder extends Shape3D
    implements NodeUpdateListener
{
    /** The default height of the cylinder */
    private static final float DEFAULT_HEIGHT = 2;

    /** The default radius of the cylinder */
    private static final float DEFAULT_RADIUS = 1;

    /** Default number of segments used in the cylinder */
    private static final int DEFAULT_FACETS = 16;

    /** The generator used to modify the geometry */
    private CylinderGenerator generator;

    /** Data used to regenerate the cylinder */
    private GeometryData data;

    /**
     * Construct a default cylinder with no appearance set. The default size
     * of the cylinder is:<BR>
     * Height: 2.0<BR>
     * Radius: 1.0<BR>
     * Faces:  16
     */
    public Cylinder()
    {
        this(DEFAULT_HEIGHT, DEFAULT_RADIUS, DEFAULT_FACETS, null);
    }

    /**
     * Construct a default cylinder with the given appearance. The default size
     * of the cylinder is:<BR>
     * Height: 2.0<BR>
     * Radius: 1.0<BR>
     * Faces:  16
     *
     * @param app The appearance to use
     */
    public Cylinder(Appearance app)
    {
        this(DEFAULT_HEIGHT, DEFAULT_RADIUS, DEFAULT_FACETS, app);
    }

    /**
     * Construct a default cylinder with no appearance set and a custom
     * number of faces. <BR>
     * Height: 2.0<BR>
     * Radius: 1.0<BR>
     *
     * @param faces The number of faces to use around the side
     */
    public Cylinder(int faces)
    {
        this(DEFAULT_HEIGHT, DEFAULT_RADIUS, faces, null);
    }

    /**
     * Construct a default cylinder with no appearance set. The height and
     * radius as set to the new value and uses the default face count of
     * 16.
     *
     * @param height The height of the cylinder
     * @param radius The radius of the base of the cylinder
     */
    public Cylinder(float height, float radius)
    {
        this(height, radius, DEFAULT_FACETS, null);
    }

    /**
     * Construct a default cylinder with the given appearance and a custom
     * number of faces. <BR>
     * Height: 2.0<BR>
     * Radius: 1.0<BR>
     *
     * @param faces The number of faces to use around the side
     * @param app The appearance to use
     */
    public Cylinder(int faces, Appearance app)
    {
        this(DEFAULT_HEIGHT, DEFAULT_RADIUS, faces, app);
    }

    /**
     * Construct a default cylinder with the given appearance. The height and
     * radius as set to the new value and uses the default face count of
     * 16.
     *
     * @param height The height of the cylinder
     * @param radius The radius of the base of the cylinder
     * @param app The appearance to use
     */
    public Cylinder(float height, float radius, Appearance app)
    {
        this(height, radius, DEFAULT_FACETS, app);
    }

    /**
     * Construct a cylinder with all the values customisable.
     *
     * @param height The height of the cylinder
     * @param radius The radius of the base of the cylinder
     * @param faces The number of faces to use around the side
     * @param app The appearance to use
     */
    public Cylinder(float height, float radius, int faces, Appearance app)
    {
        data = new GeometryData();
        data.geometryType = GeometryData.TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator = new CylinderGenerator(height, radius, faces);
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
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeBoundsChanges(Object src)
    {
        generator.generate(data);

        VertexGeometry geom = (VertexGeometry)src;
        geom.setVertices(TriangleStripArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        ((TriangleStripArray)geom).setStripCount(data.stripCounts,
                                                 data.numStrips);
        geom.setNormals(data.normals);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    @Override
    public void updateNodeDataChanges(Object src)
    {
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Change the radius and height of the cylinder to the new values. If the
     * geometry write capability has been turned off, this will not do
     * anything.
     *
     * @param height The height of the cylinder
     * @param radius The radius of the base of the cylinder
     */
    public void setDimensions(float height, float radius)
    {
        generator.setDimensions(height, radius, true, true);

        VertexGeometry geom = (VertexGeometry)getGeometry();
        geom.boundsChanged(this);
    }

    /**
     * Set the facet count of the cylinder to the new value. If the geometry
     * write capability has been turned off, this will not do anything.
     *
     * @param faces The number of faces to use around the side
     */
    public void setFacetCount(int faces)
    {
        generator.setFacetCount(faces);

        VertexGeometry geom = (VertexGeometry)getGeometry();
        geom.boundsChanged(this);
    }
}
