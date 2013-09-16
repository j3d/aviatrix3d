/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.pipeline;

// External imports
import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;

// Local imports
import org.j3d.util.HashSet;


/**
 * Default implementation of the RenderPipelineManager that handles multiple
 * simultaneous rendering threads.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class DefaultRenderManager
    implements GLEventListener, NodeUpdateHandler, RenderPipelineManager
{

    /** The initial size of the children list */
    private static final int CHANGELIST_START_SIZE = 200;

    /** The increment size of the list if it gets overflowed */
    private static final int CHANGELIST_INCREMENT = 100;

    /** The number of channels we are managing */
    private int numChannels;

    /** The GLDrawables this SG manages */
    private GLDrawable[] channels;

    /** Status of the rendering channels */
    private boolean[] doneRendering;

    /** Status of the application rendering */
    private boolean appFrameDone;

    /** The Scene Graph root */
    private Group root;

    /** The change requestors */
    private NodeUpdateListener[] cl1;

    /** The current place to add change requestors */
    private int lastCl1;

    /** HashSet to determine duplicates in the change List */
    private HashSet setCl1;

    /** The cull threads */
    private CullStage[] cull;

    /** The draw threads */
    private DrawStage[] draw;

    /** The current active Viewpoint */
    private Viewpoint viewpoint;

    /** Mutex to marshal SG access */
    private Object mutex;

    /** The minimum frame cycle time */
    private int minimumCycleTime;
    private long lastTime;

    /** Has the initial processChangeList happend */
    private boolean initialized;

    /**
     * Constructor a new DefaultRenderManager.
     * Currently only supports one canvas internally.
     *
     * @param canvases The surfaces this SG will be drawn to
     */
    public DefaultRenderManager(GLDrawable[] canvases)
    {
        initialized = false;

        numChannels = canvases.length;
        this.channels = new GLDrawable[numChannels];

        //System.arraycopy(canvases, 0, channels, 0, numChannels);
        channels[0] = canvases[0];
        doneRendering = new boolean[numChannels];

        cl1 = new NodeUpdateListener[CHANGELIST_START_SIZE];
        setCl1 = new HashSet(CHANGELIST_START_SIZE);

        lastCl1 = 0;

        channels[0].addGLEventListener(this);
        cull = new CullStage[1];
        cull[0] = new CullStage();

        draw = new DrawStage[1];
        draw[0] = new DrawStage(channels[0]);

        minimumCycleTime = 0;
        lastTime = System.currentTimeMillis();

        mutex = new Object();
    }

    /**
     * Constructs a new DefaultRenderManager for a single channel
     *
     * @param channel The channel to render to
     */
    public DefaultRenderManager(GLDrawable channel)
    {
        this(new GLDrawable[] { channel });
    }

    /**
     * Set the scene for this manager.  null will remove
     * the set scene.
     *
     * @param scene The new scene
     */
    public void setScene(Group scene)
    {
        root = scene;

        // Tell the nodes there DefaultRenderManager
        root.setUpdateHandler(this);
        initialized = false;
    }

    /**
     * Notification that the application is done processing for this frame.
     */
    public void appFrameFinished()
    {

        // Throttle the apps runtime
        long diff = System.currentTimeMillis() - lastTime;
        lastTime = System.currentTimeMillis();

        if(diff < minimumCycleTime)
        {
            try
            {
                Thread.sleep((minimumCycleTime - diff));
            }
            catch(InterruptedException ie)
            {
                System.out.println("Interrupt");
                // Ignore
            }
        }

        synchronized(mutex)
        {
            processChangeList(0);
        }

        initialized = true;
    }

    protected void drawFrameFinished(int channel)
    {
    }

    /**
     * Set the active viewpoint. A null viewpoint is invalid.
     */
    public void setActiveViewpoint(Viewpoint vp)
    {
        if (viewpoint != null)
            viewpoint.setActive(false);
        viewpoint = vp;

        vp.setActive(true);
    }

    /**
     * Set the minimum frame cycle time in milleseconds.  The start of each frame will be at
     * least this amount of time apart.  The default value is 0.
     */
    public void setMinimumFrameCycleTime(int minimumTime)
    {
        minimumCycleTime = minimumTime;
    }

    //---------------------------------------------------------------
    // Methods defined by NodeUpdateHandler
    //---------------------------------------------------------------

    /**
     * Notify the DefaultRenderManager that you have updates to the SG that might alter
     * a nodes bounds.
     *
     * @param l The change requestor
     */
    public synchronized void boundsChanged(NodeUpdateListener l)
    {
        // Generates garbage

        // Check for duplicates
        if(setCl1.contains(l))
            return;

        setCl1.add(l);
        // Add to a single ChangeList for now
        resizeChangeList1();
        cl1[lastCl1++] = l;
    }

    /**
     * Notify the DefaultRenderManager that you have updates to the SG that will not
     * alter a nodes bounds.
     *
     * @param l The change requestor
     */
    public synchronized void dataChanged(NodeUpdateListener l)
    {
        // Generates garbage
        // Check for duplicates
        if (setCl1.contains(l)) return;

        setCl1.add(l);

        // Add to a single ChangeList for now
        resizeChangeList1();
        cl1[lastCl1++] = l;
    }

    //---------------------------------------------------------------
    // Methods required by GLEventListener
    //---------------------------------------------------------------

    public void init(GLDrawable drawable)
    {
        GLFunc gl = drawable.getGL();
        GLUFunc glu = drawable.getGLU();
        GLContext glj = drawable.getGLContext();

        gl.glClearColor(0, 0, 0, 0);
        gl.glEnable(GL_CULL_FACE);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(gl.GL_LIGHTING);
        gl.glHint(gl.GL_PERSPECTIVE_CORRECTION_HINT, gl.GL_NICEST);

        // TODO:  Do we need this?
        gl.glEnable(GL_NORMALIZE);

        glj.gljCheckGL();
    }

    public void cleanup(GLDrawable drawable)
    {
    }

    public void reshape(GLDrawable gld,int width,int height)
    {
        float h = (float)width / (float)height;
        GLFunc gl = gld.getGL();
        GLUFunc glu = gld.getGLU();

        gl.glViewport(0,0,width,height);
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();

        // TODO: hardcoded back clip, need to get value from NavigationInfo
        glu.gluPerspective(45, h, 1, 1000);
        gl.glMatrixMode(GL_MODELVIEW);
    }

    public void display(GLDrawable gld)
    {
        if (root == null || !initialized)
            return;

        GLFunc gl = gld.getGL();

        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        CullStage c = cull[0];
        DrawStage d = draw[0];

        synchronized(mutex)
        {
            c.cull(root);
        }

        if (viewpoint != null)
            viewpoint.setupView(gld);

        d.draw(c.getRenderList(), c.getRenderOp(), c.getRenderListSize());
    }

    public void preDisplay(GLDrawable drawable)
    {
    }

    public void postDisplay(GLDrawable drawable)
    {
    }

    //---------------------------------------------------------------
    // Misc Internal methods
    //---------------------------------------------------------------

    /**
     * Process the changeList
     */
    private void processChangeList(int idx)
    {
        // Assume only one change list for now
        for(int i=0; i < lastCl1; i++)
        {
            cl1[i].updateNode();
        }

        setCl1.clear();
    }

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeChangeList1()
    {

        if((lastCl1 + 1) == cl1.length)
        {
            int old_size = cl1.length;
            int new_size = old_size + CHANGELIST_INCREMENT;

            NodeUpdateListener[] tmp_nodes = new NodeUpdateListener[new_size];

            System.arraycopy(cl1, 0, tmp_nodes, 0, old_size);

            cl1 = tmp_nodes;
        }
    }
}