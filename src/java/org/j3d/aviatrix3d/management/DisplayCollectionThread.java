/*****************************************************************************
 *                   Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.management;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsRenderPipeline;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.aviatrix3d.rendering.DeletableRenderable;
import org.j3d.aviatrix3d.rendering.ShaderSourceRenderable;

/**
 * Wrapper around a display collection that controls its management in a separate
 * thread.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class DisplayCollectionThread extends Thread
{
    /** flag to tell the thread to exit now */
    private boolean terminate;

    /** The pipeline we're managing */
    private DisplayCollection display;

    /** Callback to tell the system we've finished this render */
    private PipelineStateObserver observer;

    /** Lock object used to control when to run the display */
    private Object displayLock;

    /** Indicate that this is waiting for management to complete */
    private boolean waiting;

    /** Flag to decide whether we should only display the last frame */
    private boolean noProcessing;

    /**
     * Construct an instance that manages the given pipeline.
     */
    DisplayCollectionThread(DisplayCollection dc)
    {
        super("Aviatrix3D multithreaded display collection");

        display = dc;
        terminate = false;
        waiting = false;
        noProcessing = false;
        displayLock = new Object();
    }

    //---------------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------------

    /**
     * Endless loop that does the display calls asynchronously.
     */
    public void run()
    {
        boolean valid_drawable = true;

        while(!terminate && valid_drawable)
        {
            try
            {
                boolean draw_failed;

                if(noProcessing)
                    draw_failed = display.displayOnly();
                else
                    draw_failed = display.process();

                if(terminate)
                    break;

                if(draw_failed)
                {
                    if(display.isDisposed())
                        valid_drawable = false;
                }

                if(terminate)
                    break;

                synchronized(displayLock)
                {
                    if(observer != null)
                        observer.frameFinished();

                    if(terminate)
                        break;

                    displayLock.wait();
                }

                Thread.yield();
            }
            catch(InterruptedException ie)
            {
            }
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Force a halt of the current processing. Any processing in progress
     * should exit immediately. Used to abort the current scene processing due
     * to application shutdown or complete scene replacement.
     */
    void halt()
    {
        display.halt();

        try
        {
            synchronized(displayLock)
            {
                if(observer != null)
                    observer.frameFinished();

                displayLock.wait();
            }
        }
        catch(InterruptedException ie)
        {
        }
    }

    /**
     * Instruct the pipeline to only display and not do any processing.
     */
    void displayOnly()
    {
        noProcessing = true;

        synchronized(displayLock)
        {
            displayLock.notify();
        }
    }

    /**
     * Request the contained pipeline renders now. If the pipeline is currently
     * processing a previous display request, this is ignored.
     */
    void render()
    {
        noProcessing = false;

        synchronized(displayLock)
        {
            displayLock.notify();
        }
    }

    /**
     * Force a shutdown of the pipeline now. If the display is currently
     * processing, the current processing finishes before the thread
     * terminates.
     */
    void shutdown()
    {
        terminate = true;
        display.halt();

        synchronized(displayLock)
        {
            displayLock.notify();
        }
    }

    /**
     * Set the observer instance used to watch for when this pipeline has
     * finished management.
     *
     * @param pso
     */
    void setStateObserver(PipelineStateObserver pso)
    {
        observer = pso;
    }

    /**
     * Queue up a objects for deletion with the next rendering pass. This is
     * called once per rendering cycle with the values to be processed in the
     * next pass. It will contain the complete list, so you can assume that
     * the list size only needs to be set once.  Local copies should be made
     * of the array as it may be overwritten by the caller during the next
     * cycle.
     *
     * @param deleted The items to be processed for deletion
     * @param num The number of valid items in this array
     */
    void queueDeletedObjects(DeletableRenderable[] deleted, int num)
    {
        display.queueDeletedObjects(deleted, num);
    }

    /**
     * Queue up a collection of shader objects for processing on the next
     * frame. These processing requests are for either initialisation or log
     * handling, not for management.
     *
     * @param initList The shaders needing initialisation
     * @param numInit The number of shaders needing initialisation
     * @param logList The shaders needing log fetching
     * @param numLog The number of shaders needing log fetching
     */
    void queueShaderObjects(ShaderSourceRenderable[] initList,
                                   int numInit,
                                   ShaderSourceRenderable[] logList,
                                   int numLog)
    {
        display.queueShaderObjects(initList, numInit, logList, numLog);
    }

    /**
     * Notification that the timing model permits changing the layers now.
     *
     * @param state true to enable layers to be changed, false to disable
     */
    void enableLayerChange(boolean state)
    {
        display.enableLayerChange(state);
    }
}
