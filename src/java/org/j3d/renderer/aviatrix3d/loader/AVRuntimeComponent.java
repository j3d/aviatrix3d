/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
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
// None

// Local imports
// None

/**
 * A piece of code that will execute runtime component parts of the loaded
 * model format.
 * <p>
 *
 * A given file may contain one or more runtime components to execute the
 * desired behaviour. It is expected that the implementation of this interface
 * will maintain any specific state that is needed to properly execute the
 * behaviours needed, such as clocking it based on some global clock, or every
 * X number of frames. This will be called once per frame per instance. For
 * example, this is an expected usage of this method and model:
 * <p>
 *
 * <pre>
 * class MyAppBehaviour implements ApplicationUpdateObserver
 * {
 *     private AVModel model;
 *
 *   ...
 *
 *     public void updateSceneGraph()
 *     {
 *         List runtimes = model.getRuntimeComponents();
 *         Iterator itr = runtimes.iterator();
 *         while(itr.hasNext())
 *         {
 *             AVRuntimeComponent rt = (AVRuntimeComponent)itr.next();
 *             rt.executeModelBehavior();
 *         }
 *     }
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface AVRuntimeComponent
{
    /**
     * Execute the behaviour of the runtime component now.
     */
    public void executeModelBehavior();
}
