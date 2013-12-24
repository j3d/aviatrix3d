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

package org.j3d.renderer.aviatrix3d.loader.discreet;

// External imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.loaders.discreet.ObjectMesh;
import org.j3d.renderer.aviatrix3d.loader.AVModel;
import org.j3d.renderer.aviatrix3d.loader.AVRuntimeComponent;

/**
 * Representation of a loaded 3DS model.
 * <p>
 *
 * If the user requested a raw model, the object returned from
 * {@link #getRawModel()} will be an instance of
 * {@link org.j3d.loaders.discreet.ObjectMesh}
 * <p>
 *
 * 3DS models do not contain layers.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
class MaxModel implements AVModel
{
    /** The group node representing the root of the scene. */
    private Group modelRoot;

    /** The object mesh, if requested */
    private ObjectMesh realMesh;

    /** Mapping of the names to object instances */
    private HashMap<String, SceneGraphObject> namedObjects;

    /** Mapping of objects to their externally named resources */
    private HashMap<SceneGraphObject, Object> externalObjects;

    /** List of lights encountered in the file */
    private ArrayList<Light> lights;

    /** List of viewpoints encountered in the file */
    private ArrayList<Viewpoint> viewpoints;

    /** List of backgrounds encountered in the file */
    private ArrayList<Background> backgrounds;

    /** List of fogs encountered in the file */
    private ArrayList<Fog> fogs;

    /** List of keyframe animations encountered in the file */
    private ArrayList<AVRuntimeComponent> runtimes;

    /**
     * Create a new model instance and prepare it for work
     *
     * @param rootNode The node that forms the root of the scene
     * @param mesh The internal format, if requested. May be null
     */
    MaxModel(Group rootNode, ObjectMesh mesh)
    {
        modelRoot = rootNode;
        realMesh = mesh;

        namedObjects = new HashMap<String, SceneGraphObject>();
        externalObjects = new HashMap<SceneGraphObject, Object>();
        lights = new ArrayList<Light>();
        viewpoints = new ArrayList<Viewpoint>();
        backgrounds = new ArrayList<Background>();
        fogs = new ArrayList<Fog>();
        runtimes = new ArrayList<AVRuntimeComponent>();
    }

    //---------------------------------------------------------------
    // Methods defined by AVModel
    //---------------------------------------------------------------

    /**
     * Get the root of the scene graph structure that represents this model.
     *
     * @return The grouping node that represents the root of the scene graph
     */
    @Override
    public Group getModelRoot()
    {
        return modelRoot;
    }

    /**
     * Get the raw model representation of the scene as defined by a
     * loader-specific set of classes. If the loader was instructed to discard
     * this information, the method returns null.
     *
     * @return An implementation-specific object that represents the raw model
     *    format structure or null
     */
    @Override
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
    @Override
    public Map<String, SceneGraphObject> getNamedObjects()
    {
        return namedObjects;
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
    @Override
    public Map<SceneGraphObject, Object> getExternallyDefinedFiles()
    {
        return externalObjects;
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
    @Override
    public List<Viewpoint> getViewpoints()
    {
        return viewpoints;
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
    @Override
    public List<Background> getBackgrounds()
    {
        return backgrounds;
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
    @Override
    public List<Fog> getFogs()
    {
        return fogs;
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
    @Override
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
    @Override
    public List<Light> getLights()
    {
        return lights;
    }

    /**
     * Get the list of runtime components that are contained in the file. The
     * list will contain the {@link AVRuntimeComponent} instances used for
     * controlling animation or any other runtime capabilities inherent to
     * the file format.If a file does not declare any runtime capabilities, or
     * the loader was requested not to load runtimes, this returns an empty
     * list.
     *
     * @return A list of the RuntimeComponent instances declared in the file
     */
    @Override
    public List<AVRuntimeComponent> getRuntimeComponents()
    {
        return runtimes;
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Register the named object with this instance.
     *
     * @param name The name to use
     * @param obj The object instance to register
     */
    void addNamedObject(String name, SceneGraphObject obj)
    {
        namedObjects.put(name, obj);
    }

    /**
     * Register an external object that is referenced by a single string
     * with this instance.
     *
     * @param obj The object instance to register
     * @param filename The external file name to use
     */
    void addExternalObject(SceneGraphObject obj, String filename)
    {
        externalObjects.put(obj, filename);
    }


    /**
     * Register an external object that is referenced by a collection of
     * alternate name strings with this instance.
     *
     * @param obj The object instance to register
     * @param filenames The external file name list to use
     */
    void addExternalObject(SceneGraphObject obj, String[] filenames)
    {
        externalObjects.put(obj, filenames);
    }

    /**
     * Add a light to the list for this model.
     *
     * @param l The light instance to add
     */
    void addLight(Light l)
    {
        lights.add(l);
    }

    /**
     * Add a viewpoint to the list for this model.
     *
     * @param vp The viewpoint instance to add
     */
    void addViewpoint(Viewpoint vp)
    {
        viewpoints.add(vp);
    }

    /**
     * Add a fog to the list for this model.
     *
     * @param f The fog instance to add
     */
    void addFog(Fog f)
    {
        fogs.add(f);
    }

    /**
     * Add a background to the list for this model.
     *
     * @param bg The background instance to add
     */
    void addBackground(Background bg)
    {
        backgrounds.add(bg);
    }

    /**
     * Add a runtime component to the list for this model. Will control one
     * of the keyframed actions like lights, mesh, camera etc.
     *
     * @param rt The runtime instance to add
     */
    void addRuntimeComponent(AVRuntimeComponent rt)
    {
        runtimes.add(rt);
    }

}
