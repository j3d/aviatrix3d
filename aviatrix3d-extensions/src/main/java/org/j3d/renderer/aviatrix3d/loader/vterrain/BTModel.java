/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
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
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.loaders.vterrain.BTHeader;
import org.j3d.renderer.aviatrix3d.loader.AVModel;
import org.j3d.renderer.aviatrix3d.loader.AVRuntimeComponent;

/**
 * Representation of a loaded BT model.
 * <p>
 *
 * If the user requested a raw model, the object returned from
 * {@link #getRawModel()} will be an instance of
 * {@link org.j3d.loaders.vterrain.ObjectMesh}
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class BTModel implements AVModel
{
    /** The group node representing the root of the scene. */
    private Group modelRoot;

    /** The object mesh, if requested */
    private BTHeader realMesh;

    /**
     * Create a new model instance and prepare it for work
     *
     * @param rootNode The node that forms the root of the scene
     * @param mesh The internal format, if requested. May be null
     */
    BTModel(Group rootNode, BTHeader mesh)
    {
        modelRoot = rootNode;
        realMesh = mesh;
    }

    //---------------------------------------------------------------
    // Methods defined by AVModel
    //---------------------------------------------------------------

    /**
     * Get the root of the scene graph structure that represents this model.
     *
     * @return The grouping node that represents the root of the scene graph
     */
    public Group getModelRoot()
    {
        return modelRoot;
    }

    /**
     * Get the raw model representation of the scene as defined by a
     * loader-specific set of classes. If the loader was instructed to discard
     * this information, the method returns null.
     *
     * @return An instance of {@link BTHeader} or null if not requested
     */
    public Object getRawModel()
    {
        return realMesh;
    }

    /**
     * Get a mapping of any internally named objects to their corresponding
     * scene graph structure. The key of the map is the name defined in the
     * file format, and the value is the aviatrix3D scene graph structure
     * that they map to. The exact mapping that each makes is dependent on
     * the loader implementation.
     *
     * @return A map of strings to SceneGraphObject instances
     */
    public Map<String, SceneGraphObject> getNamedObjects()
    {
        return Collections.EMPTY_MAP;
    }

    /**
     * Get the listing of the external resources declared as being needed by
     * this file. External resources are keyed by the object to their
     * provided file name string or strings from the file. The value string(s)
     * will be exactly as declared in the file. It is expected the user
     * application will resolve to fully qualified path names to read the rest
     * of the files required.
     *
     * @return a map of the objects to their requested file name(s)
     */
    public Map<SceneGraphObject, Object> getExternallyDefinedFiles()
    {
        return Collections.EMPTY_MAP;
    }

    /**
     * Get the list of viewpoints that are contained in the file. The list
     * will contain the {@link org.j3d.aviatrix3d.Viewpoint} instances
     * corresponding to each viewpoint declared in the file. If a file does
     * not declare any viewpoints, or the loader was requested not to load
     * viewpoints, this returns an empty list.
     *
     * @return A list of the viewpoint instances declared in the file
     */
    public List getViewpoints()
    {
        return Collections.EMPTY_LIST;
    }

    /**
     * Get the list of backgrounds that are contained in the file. The list
     * will contain the {@link org.j3d.aviatrix3d.Background} instances
     * corresponding to each background declared in the file. If a file does
     * not declare any backgrounds, or the loader was requested not to load
     * backgrounds, this returns an empty list.
     *
     * @return A list of the background instances declared in the file
     */
    public List<Background> getBackgrounds()
    {
        return Collections.EMPTY_LIST;
    }

    /**
     * Get the list of fogs that are contained in the file. The list
     * will contain the {@link org.j3d.aviatrix3d.Fog} instances
     * corresponding to each fog declared in the file. If a file does
     * not declare any fogs, or the loader was requested not to load
     * fogs, this returns an empty list.
     *
     * @return A list of the fog instances declared in the file
     */
    public List<Fog> getFogs()
    {
        return Collections.EMPTY_LIST;
    }

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
    public List<Layer> getLayers()
    {
        return Collections.EMPTY_LIST;
    }

    /**
     * Get the list of lights that are contained in the file. The list
     * will contain the {@link org.j3d.aviatrix3d.Light} instances
     * corresponding to each light declared in the file. If a file does
     * not declare any lights, or the loader was requested not to load
     * lights, this returns an empty list.
     *
     * @return A list of the light instances declared in the file
     */
    public List<Light> getLights()
    {
        return Collections.EMPTY_LIST;
    }

    /**
     * Get the list of runtime components that are contained in the file. The
     * list will contain the {@link RuntimeComponent} instances used for
     * controlling animation or any other runtime capabilities inherent to
     * the file format.If a file does not declare any runtime capabilities, or
     * the loader was requested not to load runtimes, this returns an empty
     * list.
     *
     * @return A list of the RuntimeComponent instances declared in the file
     */
    public List<AVRuntimeComponent> getRuntimeComponents()
    {
        return Collections.EMPTY_LIST;
    }
}
