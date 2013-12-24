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

package org.j3d.renderer.aviatrix3d.loader.stl;

// External imports
import java.io.*;

import java.net.URL;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;
import org.j3d.loaders.stl.*;

import org.j3d.renderer.aviatrix3d.loader.AVLoader;
import org.j3d.renderer.aviatrix3d.loader.AVModel;

/**
 * Loader for the STL (Stereolithography) file into Aviatrix3D.
 * <p>
 *
 * In case that the file uses the binary STL format, no check can be done to
 * assure that the file is in STL format. A wrong format will only be
 * recognized if an invalid amount of data is contained in the file.
 * <p>
 *
 * STL models do not contain layers or any other form of scene graph objects
 * beyond the triangle geometry. All load flags are ignored.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class STLLoader implements AVLoader
{
    /** Message when the face count doesn't match the declared list */
    private static final String INVALID_FACE_COUNT_PROP =
        "org.j3d.renderer.aviatrix3d.loader.stl.STLLoader.faceCountMismatchMsg";

    /** The currently set load flags */
    private int loadFlags;

    /** Flag defining whether we should keep the internal model too. */
    private boolean keepModel;

    /**
     * Creates a STLLoader object.
     */
    public STLLoader()
    {
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
        STLFileReader reader = new STLFileReader(url);

        return loadInternal(reader);
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
System.out.println("STLLoader.load(InputStream) not implemented yet");
        return null;
//        return loadInternal(stream);
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
        STLFileReader reader = new STLFileReader(file);

        return loadInternal(reader);
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
    // Local methods
    //----------------------------------------------------------

    /**
     * Create a STLModel object from the contents of the STL file.
     *
     * @param reader The pre-created reader
     * @return The model representing the contents of the reader
     * @throws IOException something went wrong while reading the file
     */
    private STLModel loadInternal(STLFileReader reader)
        throws IOException
    {
        Group root_group = new Group();
        STLModel model = new STLModel(root_group);

        int num_objects = reader.getNumOfObjects();
        int[] num_facets = reader.getNumOfFacets();
        String[] names = reader.getObjectNames();

        double[] normal = new double[3];
        double[][] vertices = new double[3][3];

        float[] f_normals = new float[num_facets[0] * 9];
        float[] f_coords = new float[num_facets[0] * 9];

        for(int i = 0; i < num_objects; i++)
        {
            if(f_normals.length < num_facets[i] * 9)
            {
                f_normals = new float[num_facets[i] * 9];
                f_coords = new float[num_facets[i] * 9];
            }

            int index = 0;
            for(int j = 0; j < num_facets[i]; j ++)
            {
                if(reader.getNextFacet(normal, vertices))
                {
                    f_normals[j * 9] = (float)normal[0];
                    f_normals[j * 9 + 1] = (float)normal[1];
                    f_normals[j * 9 + 2] = (float)normal[2];

                    f_normals[j * 9 + 3] = (float)normal[0];
                    f_normals[j * 9 + 4] = (float)normal[1];
                    f_normals[j * 9 + 5] = (float)normal[2];

                    f_normals[j * 9 + 6] = (float)normal[0];
                    f_normals[j * 9 + 7] = (float)normal[1];
                    f_normals[j * 9 + 8] = (float)normal[2];

                    f_coords[j * 9] = (float)vertices[0][0];
                    f_coords[j * 9 + 1] = (float)vertices[0][1];
                    f_coords[j * 9 + 2] = (float)vertices[0][2];

                    f_coords[j * 9 + 3] = (float)vertices[1][0];
                    f_coords[j * 9 + 4] = (float)vertices[1][1];
                    f_coords[j * 9 + 5] = (float)vertices[1][2];

                    f_coords[j * 9 + 6] = (float)vertices[2][0];
                    f_coords[j * 9 + 7] = (float)vertices[2][1];
                    f_coords[j * 9 + 8] = (float)vertices[2][2];
                }
                else
				{
					I18nManager intl_mgr = I18nManager.getManager();
					String msg_pattern = intl_mgr.getString(INVALID_FACE_COUNT_PROP);

					Locale lcl = intl_mgr.getFoundLocale();

					NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

					Object[] msg_args = { new Integer(j), new Integer(num_facets[i])};
					Format[] fmts = { n_fmt, n_fmt };
					MessageFormat msg_fmt =
						new MessageFormat(msg_pattern, lcl);
					msg_fmt.setFormats(fmts);
					String msg = msg_fmt.format(msg_args);

                    throw new IOException(msg);
				}
            }

            TriangleArray geom = new TriangleArray();
            geom.setVertices(TriangleArray.COORDINATE_3,
                             f_coords,
                             num_facets[i] * 3);
            geom.setNormals(f_normals);

            Shape3D shape = new Shape3D();
            shape.setGeometry(geom);
            root_group.addChild(shape);

            if(names[i] != null)
                model.addNamedObject(names[i], shape);
        }

        return model;
    }
}
