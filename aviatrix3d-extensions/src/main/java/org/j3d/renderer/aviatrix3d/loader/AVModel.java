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
import java.util.List;
import java.util.Map;

// Local imports
import org.j3d.aviatrix3d.*;

/**
 * Representation of a loaded model that came from a {@link AVLoader}
 * implementation.
 * <p>
 *
 * The model contains all of the Aviatrix3D scenegraph structures as requested
 * by the user of the loader interface. For example, if the loader was
 * instructed to only load geometry, then it should not be possible to fetch
 * runtime information from this model instance.
 * <p>
 *
 * If the user asks the loader to maintain the raw model structure as part of
 * the load process, then that will be available through the
 * {@link #getRawModel()} method. The returned object type is dependent on the
 * model format and loader implementation. The derived documentation must
 * document what this class is so that the user can cast to the higher level
 * interface.
 * <p>
 *
 * The order of the structures returned by the methods of this interface are
 * not guaranteed to map to the order they were declared in the source file.
 * <p>
 *
 * <b>Layers</b>
 * <p>
 *
 * The behaviour of this class is dependent on layers.
 * <p>
 * When the user requests that layers are loaded, and some are found in the
 * requested file, then no root model is defined {@link #getModelRoot()} will
 * return null. All content is accessed through the {@link #getLayers()}
 * method. Layers are returned in the order of front-most to rear-most. Layers
 * that are defined, but contain no content will still have valid layer
 * object instances. With all layers, unless the file format contains a
 * specific size, the {@link org.j3d.aviatrix3d.Viewport} will have zero size.
 * It is up to the calling application to ensure that viewports are correctly
 * sized before adding them to the scene.
 * <p>
 *
 * When no layer loading is requested, or the loaded file does not contain any
 * layers, then the {@link #getLayers()} method will return an empty list and
 * {@link #getModelRoot()} will return a valid object representing the root of
 * the loaded model.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public interface AVModel
{
    /**
     * Get the root of the scene graph structure that represents this model.
     * <p>
     * If this model contains loaded layers, it will not return a root object.
     *
     * @return The grouping node that represents the root of the scene graph
     */
    public Group getModelRoot();

    /**
     * Get the raw model representation of the scene as defined by a
     * loader-specific set of classes. If the loader was instructed to discard
     * this information, the method returns null.
     *
     * @return An implementation-specific object that represents the raw model
     *    format structure or null
     */
    public Object getRawModel();

    /**
     * Get a mapping of any internally named objects to their corresponding
     * scene graph structure. The key of the map is the name defined in the
     * file format, and the value is the aviatrix3D scene graph structure
     * that they map to. The exact mapping that each makes is dependent on
     * the loader implementation.
     *
     * @return A map of strings to SceneGraphObject instances
     */
    public Map<String, SceneGraphObject> getNamedObjects();

    /**
     * Get the listing of the external resources declared as being needed by
     * this file. External resources are keyed by the object to their
     * provided file name string or strings from the file. The value string(s)
     * will be exactly as declared in the file. It is expected the user
     * application will resolve to fully qualified path names to read the rest
     * of the files required. The name map may be either a String or String[]
     * depending on the implementation.
     *
     * @return a map of the objects to their requested file name(s)
     */
    public Map<SceneGraphObject, Object> getExternallyDefinedFiles();

    /**
     * Get the list of viewpoints that are contained in the file. The list
     * will contain the {@link org.j3d.aviatrix3d.Viewpoint} instances
     * corresponding to each viewpoint declared in the file. If a file does
     * not declare any viewpoints, or the loader was requested not to load
     * viewpoints, this returns an empty list.
     *
     * @return A list of the viewpoint instances declared in the file
     */
    public List<Viewpoint> getViewpoints();

    /**
     * Get the list of backgrounds that are contained in the file. The list
     * will contain the {@link org.j3d.aviatrix3d.Background} instances
     * corresponding to each background declared in the file. If a file does
     * not declare any backgrounds, or the loader was requested not to load
     * backgrounds, this returns an empty list.
     *
     * @return A list of the background instances declared in the file
     */
    public List<Background> getBackgrounds();

    /**
     * Get the list of fogs that are contained in the file. The list
     * will contain the {@link org.j3d.aviatrix3d.Fog} instances
     * corresponding to each fog declared in the file. If a file does
     * not declare any fogs, or the loader was requested not to load
     * fogs, this returns an empty list.
     *
     * @return A list of the fog instances declared in the file
     */
    public List<Fog> getFogs();

    /**
     * Get the list of layers that are contained in the file. The list will
     * contain the {@link org.j3d.aviatrix3d.Layer} instances corresponding
     * to each layer declared in the file. If a file does not declare any
     * layers, or the loader was requested not to load layers, this returns
     * an empty list. Unlike the other methods, this method will guarantee to
     * return the layer instances in the order of front to rear.
     * <p>
     * If this model contains loaded layers, it will not return a root object.
     *
     * @return A list of the layer instances declared in the file
     * @since Aviatrix3D 2.0
     */
    public List<Layer> getLayers();

    /**
     * Get the list of lights that are contained in the file. The list
     * will contain the {@link org.j3d.aviatrix3d.Light} instances
     * corresponding to each light declared in the file. If a file does
     * not declare any lights, or the loader was requested not to load
     * lights, this returns an empty list.
     *
     * @return A list of the light instances declared in the file
     */
    public List<Light> getLights();

    /**
     * Get the list of runtime components that are contained in the file. The
     * list will contain the {@link AVRuntimeComponent} instances used for
     * controlling animation or any other runtime capabilities inherent to
     * the file format.If a file does not declare any runtime capabilities, or
     * the loader was requested not to load runtimes, this returns an empty
     * list.
     *
     * @return A list of the AVRuntimeComponent instances declared in the file
     */
    public List<AVRuntimeComponent> getRuntimeComponents();
}
