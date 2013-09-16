/*****************************************************************************
 *                        J3D.org Copyright (c) 2005 - 2006
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.util;

// External imports
import java.text.MessageFormat;
import java.util.Locale;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.HashSet;
import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.rendering.CustomRenderable;

/**
 * Utility class that traverses an Aviatrix3D scene graph, reporting
 * values to the end user through the use of the
 * {@link SceneGraphTraversalObserver}.	This code is not multi-thread safe.
 *
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>observerExceptionMsg: Error message when there was a user-land exception
 *     sending the observedNode callback</li>
 * <li>multithreadMsg: Error message when attempting to call this a second
 *     time while a current traversal is in progress.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public class SceneGraphTraverser
{
    /** Error message when the user code barfs */
    private static final String CALLBACK_ERROR_PROP =
		"org.j3d.renderer.aviatrix3d.util.SceneGraphTraverser.observerExceptionMsg";

    /** Error message when the user code barfs */
    private static final String INUSE_ERROR_PROP =
		"org.j3d.renderer.aviatrix3d.util.SceneGraphTraverser.multithreadMsg";

    /** Flag to describe if we are currently in a traversal */
    private boolean inUse;

    /** Temporary map during traversal for use references */
    private HashSet nodeRefs;

    /** The detailObs for the scene graph */
    private SceneGraphTraversalObserver observer;

    /** Class that represents the external reporter */
    private ErrorReporter errorReporter;

    /**
     * Create a new traverser ready to go.
     */
    public SceneGraphTraverser()
    {
        inUse = false;
        nodeRefs = new HashSet();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter)
    {

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }

    /**
     * Set the detailObs to be used. If an detailObs is already set, it is
     * replaced by the new one. A value of null will clear the current
     * detailObs.
     *
     * @param obs The new detailObs reference to use
     */
    public void setObserver(SceneGraphTraversalObserver obs)
    {
        observer = obs;
    }

    /**
     * Traverse the given scene graph now. If the call is currently in progress
     * then this will issue an exception. Any node can be used as the root
     * node. If no detailObs is set, this method returns immediately. This method
     * is equivalent to calling <code>traverseGraph(null, source);</code>.
     *
     * @param source The root of the scene graph to traverse
     * @throws IllegalStateException Attempt to call this method while it is
     *     currently traversing a scene graph
     */
    public void traverseGraph(SceneGraphObject source)
        throws IllegalStateException
    {
        traverseGraph(null, source);
    }

    /**
     * Traverse the given scene graph now with the option of providing an
     * explicit, parent reference. If the call is currently in progress
     * then this will issue an exception. Any node can be used as the root
     * node. If no observer is set or the source is null, this method returns
     * immediately.
     * <p>
     * A explicit root may be provided for various reasons. The most common
     * would be for loading externprotos where the root of the traversed graph
     * is actually going to be in a separate file and scene graph structure
     * from where we are starting this traversal from.
     *
     * @param source The root of the scene graph to traverse
     * @throws IllegalStateException Attempt to call this method while it is
     *     currently traversing a scene graph
     */
    public void traverseGraph(SceneGraphObject parent, SceneGraphObject source)
        throws IllegalStateException
    {
        if(inUse)
		{
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(INUSE_ERROR_PROP);

            throw new IllegalStateException(msg);
		}

        if((observer == null) || (source == null))
            return;

        inUse = true;

        try
        {
            processSimpleNode(parent, source, 0);
        }
        finally
        {
            // this is for error recovery
            inUse = false;
        }
    }

    /**
     * Clear the use map.  This will not be cleared between traversal calls
     */
    public void reset()
    {
        nodeRefs.clear();
    }

    /**
     * Internal convenience method that separates the startup traversal code
     * from the recursive mechanism using the detailed detailObs.
     *
     * @param parent The root of the current item to traverse
     * @param depth The scenegraph depth
     */
    private void recurseSceneGraphChild(SceneGraphObject parent, int depth)
    {
        if(parent instanceof Group)
        {
            int currDepth = depth+1;

            Group g = (Group)parent;
            int num_kids = g.numChildren();

            for(int i = 0; i < num_kids; i++)
                processSimpleNode(parent, g.getChild(i), currDepth);
        }
        else if(parent instanceof Layer)
        {
            Layer l = (Layer)parent;

            switch(l.getType())
            {
                case Layer.COMPOSITE:
                    CompositeLayer cl = (CompositeLayer)l;
                    for(int i = 0; i < cl.numViewports(); i++)
                        processSimpleNode(parent, cl.getViewport(i), depth+1);
                    break;

                case Layer.COMPOSITE_2D:
                    CompositeLayer2D cl2 = (CompositeLayer2D)l;
                    for(int i = 0; i < cl2.numViewports(); i++)
                        processSimpleNode(parent, cl2.getViewport(i), depth+1);
                    break;

                case Layer.SIMPLE:
                    SimpleLayer sl = (SimpleLayer)l;
                    processSimpleNode(parent, sl.getViewport(), depth+1);
                    break;

                case Layer.SIMPLE_2D:
                    SimpleLayer2D sl2 = (SimpleLayer2D)l;
                    processSimpleNode(parent, sl2.getViewport(), depth+1);
                    break;
            }
        }
        else if(parent instanceof Viewport)
        {
            Viewport vp = (Viewport)parent;

            switch(vp.getType())
            {
                case Viewport.COMPOSITE:
                    CompositeViewport cvp = (CompositeViewport)vp;
                    for(int i = 0; i < cvp.numViewportLayers(); i++)
                        processSimpleNode(cvp,
                                          cvp.getViewportLayer(i),
                                          depth+1);
                    break;

                case Viewport.FLAT:
                    Viewport2D vp2 = (Viewport2D)vp;
                    processSimpleNode(parent, vp2.getScene(), depth+1);
                    break;

                case Viewport.MULTIPASS:
                    MultipassViewport mvp = (MultipassViewport)vp;
                    processSimpleNode(parent, mvp.getScene(), depth+1);
                    break;

                case Viewport.SIMPLE:
                    SimpleViewport svp = (SimpleViewport)vp;
                    processSimpleNode(parent, svp.getScene(), depth+1);
                    break;
            }
        }
        else if(parent instanceof ViewportLayer)
        {
            ViewportLayer vp = (ViewportLayer)parent;

            switch(vp.getType())
            {
                case ViewportLayer.FLAT:
                    ViewportLayer2D vp2 = (ViewportLayer2D)vp;
                    processSimpleNode(parent, vp2.getScene(), depth+1);
                    break;

                case ViewportLayer.MULTIPASS:
                    MultipassViewportLayer mvp = (MultipassViewportLayer)vp;
                    processSimpleNode(parent, mvp.getScene(), depth+1);
                    break;

                case ViewportLayer.SIMPLE:
                    SimpleViewportLayer svp = (SimpleViewportLayer)vp;
                    processSimpleNode(parent, svp.getScene(), depth+1);
                    break;
            }
        }
        else if(parent instanceof RenderPass)
        {
            RenderPass pass = (RenderPass)parent;

            processSimpleNode(parent, pass.getAccumulationBufferState(), depth+1);
            processSimpleNode(parent, pass.getColorBufferState(), depth+1);
            processSimpleNode(parent, pass.getDepthBufferState(), depth+1);
            processSimpleNode(parent, pass.getStencilBufferState(), depth+1);
            processSimpleNode(parent, pass.getRenderedGeometry(), depth+1);
        }
        else if(parent instanceof Scene)
        {
            if(parent instanceof SimpleScene)
            {
                SimpleScene sc = (SimpleScene)parent;
                processSimpleNode(parent, sc.getRenderedGeometry(), depth+1);
            }
            else if(parent instanceof MultipassScene)
            {
                MultipassScene mps = (MultipassScene)parent;
                for(int i = 0; i < mps.numRenderPasses(); i++)
                    processSimpleNode(parent, mps.getRenderPass(i), depth+1);
            }
            else if(parent instanceof Scene2D)
            {
                Scene2D sc = (Scene2D)parent;
                processSimpleNode(parent, sc.getRenderedGeometry(), depth+1);
            }
        }
        else if(parent instanceof CustomRenderable)
        {
            // What to do here? Maybe need an extra callback interface on
            // the traversal observer that will pass back the real children
            // list to process.
        }
        else if (parent instanceof SharedNode)
        {
            SharedNode snode = (SharedNode) parent;

            processSimpleNode(parent, snode.getChild(), depth+1);
        }
        else if(parent instanceof Shape3D)
        {
            Shape3D shape = (Shape3D)parent;

            processSimpleNode(parent, shape.getAppearance(), depth+1);
            processSimpleNode(parent, shape.getGeometry(), depth+1);
        }
        else if(parent instanceof Appearance)
        {
            Appearance app = (Appearance)parent;

            processSimpleNode(parent, app.getMaterial(), depth+1);
            processSimpleNode(parent, app.getBlendAttributes(), depth+1);
            processSimpleNode(parent, app.getDepthAttributes(), depth+1);
            processSimpleNode(parent, app.getLineAttributes(), depth+1);
            processSimpleNode(parent, app.getPolygonAttributes(), depth+1);
            processSimpleNode(parent, app.getStencilAttributes(), depth+1);
            processSimpleNode(parent, app.getShader(), depth+1);

            int num_tex = app.numTextureUnits();
            TextureUnit[] tex = new TextureUnit[num_tex];
            app.getTextureUnits(tex);

            for(int i = 0; i < num_tex; i++)
                processSimpleNode(parent, tex[i], depth+1);
        }
        else if(parent instanceof TextureUnit)
        {
            TextureUnit tex = (TextureUnit)parent;

            processSimpleNode(parent, tex.getTexCoordGeneration(), depth+1);
            processSimpleNode(parent, tex.getTextureAttributes(), depth+1);
            processSimpleNode(parent, tex.getTexture(), depth+1);
        }
        else if(parent instanceof OffscreenTexture2D)
        {
            OffscreenTexture2D tex = (OffscreenTexture2D)parent;

            int num_layers = tex.numLayers();
            Layer[] layers = new Layer[num_layers];
            tex.getLayers(layers);

            for(int i = 0; i < num_layers; i++)
                processSimpleNode(parent, layers[i], depth+1);
        }
        else if(parent instanceof MRTOffscreenTexture2D)
        {
            MRTOffscreenTexture2D tex = (MRTOffscreenTexture2D)parent;

            int num_layers = tex.numLayers();
            Layer[] layers = new Layer[num_layers];
            tex.getLayers(layers);

            for(int i = 0; i < num_layers; i++)
                processSimpleNode(parent, layers[i], depth+1);
        }
        else if(parent instanceof AppearanceOverride)
        {
            AppearanceOverride app_ovr = (AppearanceOverride)parent;

            processSimpleNode(parent, app_ovr.getAppearance(), depth+1);
        }
        else if(parent instanceof Shader)
        {
            if(parent instanceof GLSLangShader)
            {
                GLSLangShader sh = (GLSLangShader)parent;
                processSimpleNode(parent, sh.getShaderArguments(), depth+1);
                processSimpleNode(parent, sh.getShaderProgram(), depth+1);
            }
            else if(parent instanceof GL14Shader)
            {
                GL14Shader sh = (GL14Shader)parent;
                processSimpleNode(parent, sh.getVertexShader(), depth+1);
                processSimpleNode(parent, sh.getFragmentShader(), depth+1);
            }
        }
        else if(parent instanceof ShaderProgram)
        {
            ShaderProgram prog = (ShaderProgram)parent;

            int num_obj = prog. getNumShaderObjects();
            ShaderObject[] objects = new ShaderObject[num_obj];

            for(int i = 0; i < num_obj; i++)
                processSimpleNode(parent, objects[i], depth+1);
        }
    }

    /**
     * Process a single simple node with its callback
     *
     * @param parent The parent node that was just processed
     * @param kid The child node that is about to be processed
     * @param depth The scenegraph depth
     */
    private void processSimpleNode(SceneGraphObject parent,
                                   SceneGraphObject kid,
                                   int depth)
    {

        if(kid == null)
            return;

        boolean copy = nodeRefs.contains(kid);
        if(!copy)
            nodeRefs.add(kid);

        try
        {
            observer.observedNode(parent, kid, copy, depth);
        }
        catch(Exception e)
        {
			I18nManager intl_mgr = I18nManager.getManager();
            Locale lcl = intl_mgr.getFoundLocale();
			String msg_pattern = intl_mgr.getString(CALLBACK_ERROR_PROP);

			Object[] msg_args = { observer.getClass().getName() };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
			String msg = msg_fmt.format(msg_args);

            errorReporter.warningReport(msg, e);
        }

        // now recurse
        recurseSceneGraphChild(kid,depth++);
    }
}
