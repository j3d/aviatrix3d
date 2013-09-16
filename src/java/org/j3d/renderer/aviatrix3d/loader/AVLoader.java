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

package org.j3d.renderer.aviatrix3d.loader;

// External imports
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

// Local imports
// None

/**
 * The definition of a class that is capable of parsing a file format and
 * turning it into an Aviatrix3D scene graph.
 * <p>
 *
 * Loaders provide a common interface regardless of the type of file format
 * that is being loaded. The flags allow you to control exactly what should
 * be created from a loading process. For example, many applications only
 * want the static geometry to be loaded, while ignoring any animation data
 * provided. Many of the more complex model formats, such as those that come
 * from modelling tools like 3DS Max or Maya include a wealth of useful
 * renderable structure, and also a lot of useless stuff, such as parameters
 * for the editor configuration or raytracing parameters. These are ignored
 * by the loader interface as it focuses on working with realtime model data
 * only.
 * <p>
 *
 * <b>Behavioural Requirements</b>
 *
 * The default behaviour of a loader implementation, should the user not set
 * any flags shall be as follows:
 * <ul>
 * <li>The file should be loaded with all capabilities
 * defined.</li>
 * <li>The internal model of the file is discarded and only the aviatrix3D
 * scene graph portion is kept.</li>
 * <li>Each call to <code>load()</code> is independent. No state is kept
 * between calls other than flag settings.</li>
 * <li>Access is considered to be single threaded. If two threads make a call
 *  at roughly the same time, the effects are likely to be bad. For example,
 *  calling load() in one thread and then changing the flags in another thread.
 *  before the load has returned.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface AVLoader
{
    /** Load the geometry and structural portions of the file */
    public static final int GEOMETRY = 0x01;

    /** Load the background(s) defined in the file */
    public static final int BACKGROUNDS = 0x02;

    /** Load the viewpoints defined in the file */
    public static final int VIEWPOINTS = 0x04;

    /**
     * Load code that will handling any runtime capabilities (such as
     * animation) defined in the file.
     */
    public static final int RUNTIMES = 0x08;

    /** Load the fogs defined in the file */
    public static final int FOGS = 0x10;

    /** Load the lights defined in the file */
    public static final int LIGHTS = 0x20;

    /** Load the layers defined in the file */
    public static final int LAYERS = 0x40;

    /** Load everything in the file */
    public static final int LOAD_ALL = 0xFFFFFFFF;

    /**
     * Load a model from the given URL.
     *
     * @param url The url to load the model from
     * @return A representation of the model at the URL
     * @throws IOException something went wrong while reading the file
     */
    public AVModel load(URL url) throws IOException;

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
    public AVModel load(InputStream stream) throws IOException;

    /**
     * Load a model from the given file.
     *
     * @param file The file instance to load the model from
     * @return A representation of the model in the file
     * @throws IOException something went wrong while reading the file
     */
    public AVModel load(File file) throws IOException;

    /**
     * Set the flags for which parts of the file that should be loaded.
     * The flags are bit-fields, so can be bitwise OR'd together.
     *
     * @param flags The collection of flags to use
     */
    public void setLoadFlags(int flags);

    /**
     * Get the current set collection of load flags.
     *
     * @return A bitmask of flags that are currently set
     */
    public int getLoadFlags();

    /**
     * Define whether this loader should also keep around it's internal
     * representation of the file format, if it has one. If kept, this can be
     * retrieved through the {@link AVModel#getRawModel()} method and cast to
     * the appropriate class type.
     *
     * @param enable true to enable keeping the raw model, false otherwise
     */
    public void keepInternalModel(boolean enable);

    /**
     * Check to see whether the loader should be currently keeping the internal
     * model.
     *
     * @return true when the internal model should be kept
     */
    public boolean isInternalModelKept();
}
