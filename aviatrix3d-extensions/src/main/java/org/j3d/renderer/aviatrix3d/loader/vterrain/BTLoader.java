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

package org.j3d.renderer.aviatrix3d.loader.vterrain;

// External imports
import java.io.*;

import java.net.URL;
import java.net.URLConnection;

// Loader imports
import org.j3d.aviatrix3d.*;

import org.j3d.loaders.HeightMapSourceOrigin;
import org.j3d.loaders.vterrain.*;

import org.j3d.geom.GeometryData;
import org.j3d.geom.terrain.ElevationGridGenerator;
import org.j3d.loaders.HeightMapSource;

import org.j3d.renderer.aviatrix3d.loader.AVLoader;
import org.j3d.renderer.aviatrix3d.loader.AVModel;

/**
 * Loader for the VTerrain Project's BT file format.
 * <p>
 *
 * The mesh produced is, by default, triangle strip arrays. The X axis
 * represents East-West and the Z-axis represents North-South. +X is east,
 * -Z is North. Texture coordinates are generated for the extents based on
 * a single 0-1 scale for the width of the object.
 * <p>
 *
 * The loader produces a single mesh that represents the file's contents. No
 * further processing is performed in the current implementation to break the
 * points into smaller tiles or use multi-resolution terrain structures.
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.vterrain.org/Implementation/BT.html">
 *  http://www.vterrain.org/Implementation/BT.html
 * </a>
 *
 * The BT format does not contain any form of scene graph other than the raw
 * terrain geometry, so all load flags are ignored.
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class BTLoader
    implements AVLoader, HeightMapSource
{
    /** Constant for texture coordinate type. Always a single 2D array */
    private static final int[] TEX_COORD_TYPES =
        { VertexGeometry.TEXTURE_COORDINATE_2 };

    /** The currently set load flags */
    private int loadFlags;

    /** Flag defining whether we should keep the internal model too. */
    private boolean keepModel;

    /** Current parser */
    private BTParser parser;

    /** Generator of the grid structure for the geometry */
    private ElevationGridGenerator generator;

    /** Step information because it is not held anywhere else */
    private float[] gridStepData;

    /**
     * Construct a new default loader with no flags set
     */
    public BTLoader()
    {
        loadFlags = LOAD_ALL;
        keepModel = false;
    }

    //---------------------------------------------------------------
    // Methods defined by AVLoader
    //---------------------------------------------------------------

    /**
     * Load a model from the given URL.
     *
     * @param url The url to load the model from
     * @return A representation of the model at the URL
     * @throws IOException something went wrong while reading the file
     */
    @Override
    public AVModel load(URL url) throws IOException
    {
        InputStream input = null;

        try
        {
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();

            if(is instanceof BufferedInputStream)
                input = (BufferedInputStream)is;
            else
                input = new BufferedInputStream(is);
        }
        catch(IOException ioe)
        {
            throw new FileNotFoundException(ioe.getMessage());
        }

        return loadInternal(input);
    }

    /**
     * Load a model from the given input stream. If the file format would
     * prefer to use a {@link java.io.Reader} interface, then use the
     * {@link java.io.InputStreamReader} to convert this stream to the desired
     * type. The caller will be responsible for closing down the stream at the
     * end of this process.
     *
     * @param stream The stream to load the model from
     * @return A representation of the model from the stream contents
     * @throws IOException something went wrong while reading the file
     */
    @Override
    public AVModel load(InputStream stream) throws IOException
    {
        return loadInternal(stream);
    }

    /**
     * Load a model from the given file.
     *
     * @param file The file instance to load the model from
     * @return A representation of the model in the file
     * @throws IOException something went wrong while reading the file
     */
    @Override
    public AVModel load(File file) throws IOException
    {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream input = new BufferedInputStream(fis);

        return loadInternal(input);
    }

    /**
     * Set the flags for which parts of the file that should be loaded.
     * The flags are bit-fields, so can be bitwise OR'd together.
     *
     * @param flags The collection of flags to use
     */
    @Override
    public void setLoadFlags(int flags)
    {
        loadFlags = flags;
    }

    /**
     * Get the current set collection of load flags.
     *
     * @return A bitmask of flags that are currently set
     */
    @Override
    public int getLoadFlags()
    {
        return loadFlags;
    }

    /**
     * Define whether this loader should also keep around it's internal
     * representation of the file format, if it has one. If kept, this can be
     * retrieved through the {@link AVModel#getRawModel()} method and cast to
     * the appropriate class type.
     *
     * @param enable true to enable keeping the raw model, false otherwise
     */
    @Override
    public void keepInternalModel(boolean enable)
    {
        keepModel = enable;
    }

    /**
     * Check to see whether the loader should be currently keeping the internal
     * model.
     *
     * @return true when the internal model should be kept
     */
    @Override
    public boolean isInternalModelKept()
    {
        return keepModel;
    }

    //----------------------------------------------------------
    // Methods defined by HeightMapSource
    //----------------------------------------------------------

    /**
     * Return the height map created for the last stream parsed. If no stream
     * has been parsed yet, this will return null.
     *
     * @return The array of heights in [row][column] order or null
     */
    @Override
    public float[][] getHeights()
    {
        return parser.getHeights();
    }

    /**
     * Fetch information about the real-world stepping sizes that this
     * grid uses.
     *
     * @return The stepping information for width and depth
     */
    @Override
    public float[] getGridStep()
    {
        return gridStepData;
    }

    @Override
    public HeightMapSourceOrigin getOriginLocation()
    {
        return HeightMapSourceOrigin.BOTTOM_LEFT;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Get the header used to describe the last stream parsed. If no stream
     * has been parsed yet, this will return null.
     *
     * @return The header for the last read stream or null
     */
    public BTHeader getHeader()
    {
        return parser.getHeader();
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param input The inputsource for this reader
     * @return The scene description
     */
    private AVModel loadInternal(InputStream input)
        throws IOException
    {
        if(parser == null)
            parser = new BTParser(input);
        else
            parser.reset(input);

        float[][] heights = parser.parse();

        BTHeader header = parser.getHeader();

        float width = (float)(header.rightExtent - header.leftExtent);
        float depth = (float)(header.topExtent - header.bottomExtent);

        gridStepData = new float[2];
        gridStepData[0] = width / header.rows;
        gridStepData[1] = depth / header.columns;

        if(generator == null)
        {
            generator = new ElevationGridGenerator(width,
                                                   depth,
                                                   header.rows,
                                                   header.columns,
                                                   heights,
                                                   0);
        }
        else
        {
            generator.setDimensions(width, depth, header.rows, header.columns);
            generator.setTerrainDetail(heights, 0);
        }

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLE_STRIPS;
        data.geometryComponents = GeometryData.NORMAL_DATA |
                                  GeometryData.TEXTURE_2D_DATA;

        generator.generate(data);

        if(!keepModel)
        {
            parser.clear();
            generator = null;
        }

        // So that passed, well let's look at the building the scene now. All
        // we need to do is create a single big tri-strip array based on the
        // points.
        //
        // In a later variant, we may want to look at dividing this up into
        // collections of points dependent on the culling algorithm we are
        // going to use or to generate multi-resolution terrains.
        //
        // At some stage, we should use the HeightMapGenerator so that we
        // can throw the GeometryData into the ITSA for the collision and
        // terrain following code.
        Group root_group = new Group();
        BTModel model = new BTModel(root_group, header);

        IndexedTriangleStripArray geom =
            new IndexedTriangleStripArray();

        geom.setVertices(IndexedTriangleStripArray.COORDINATE_3,
                         data.coordinates,
                         data.vertexCount);
        geom.setIndices(data.indexes, data.indexesCount);
        geom.setStripCount(data.stripCounts, data.stripCounts.length);
        geom.setNormals(data.normals);

        float tmp_t[][] = { data.textureCoordinates };
        geom.setTextureCoordinates(TEX_COORD_TYPES, tmp_t, 1);

        Shape3D shape = new Shape3D();
        shape.setGeometry(geom);

        root_group.addChild(shape);

        return model;
    }
}
