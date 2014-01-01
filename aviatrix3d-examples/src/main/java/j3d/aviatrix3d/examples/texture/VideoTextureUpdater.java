package j3d.aviatrix3d.examples.texture;

// Standard imports

import javax.media.*;

import java.net.URL;

import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;

// Application Specific imports
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;
import org.j3d.aviatrix3d.*;

/**
 * Handler for illustrating updating textures on demand.
 * <p/>
 * Since sub-image updates are ignored for any texture that has not yet
 * been part of a live scene graph, or drawn yet we have to put a set of
 * frame delays into the system to make sure the texture has been drawn
 * at least once.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class VideoTextureUpdater
        implements ApplicationUpdateObserver, ControllerListener
{
    /** The renderer needed to control the video processing */
    private VideoTextureRenderer renderer;

    /** JMF processor doing the work for us */
    private Processor processor;

    /** Object to synchronise on */
    private Object waitSync;

    /** Flag to say the state transition was successful */
    private boolean stateTransOK;

    /**
     * Create a new updater that works with the given renderer
     *
     * @param mediaSource place to grab the MPEG from
     * @param rend The renderer instance to manage
     */
    public VideoTextureUpdater(URL mediaSource, VideoTextureRenderer rend)
    {
        renderer = rend;
        stateTransOK = true;
        waitSync = new Object();

        try
        {
            MediaLocator ml = new MediaLocator(mediaSource);

/*
            Format[] formats = renderer.getSupportedInputFormats();
            ProcessorModel pm = new ProcessorModel(ml, formats, null);
            processor = Manager.createRealizedProcessor(pm);
*/

            processor = Manager.createProcessor(ml);
            processor.addControllerListener(this);
            processor.configure();

            if (!waitForState(processor.Configured))
            {
                System.out.println("Failed to configure the processor");
                return;
            }

            // use processor as a player
            processor.setContentDescriptor(null);

            // obtain the track control
            TrackControl[] controls = processor.getTrackControls();

            for (int i = 0; i < controls.length; i++)
            {
                Format format = controls[i].getFormat();
                try
                {
                    if (format instanceof VideoFormat)
                    {
                        controls[i].setRenderer(renderer);
//                        controls[i].setFormat(renderer.getSupportedInputFormats()[0]);
                    }
                }
                catch (UnsupportedPlugInException e)
                {
                    System.err.println("Unsupported plugin??? " + e);
                }

            }

            // prefetch
            processor.prefetch();

            if (!waitForState(processor.Prefetched))
            {
                System.out.println("Failed to prefech the processor");
                return;
            }

            processor.start();
        }
        catch (Exception e)
        {
            System.out.println("Error starting movie handling");
            System.out.println(e.getMessage());
        }
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
        renderer.syncTextureUpdate();
    }

    /**
     * Notification that the AV3D internal shutdown handler has detected a
     * system-wide shutdown. The aviatrix code has already terminated rendering
     * at the point this method is called, only the user's system code needs to
     * terminate before exiting here.
     */
    public void appShutdown()
    {
        // do nothing
    }

    //----------------------------------------------------------------------
    // Methods defined by ControllerListener
    //----------------------------------------------------------------------

    /**
     * Callback for controller events.  This method is called by the
     * processor whenever state changes.
     *
     * @param event - describes the nature of the state change.
     */
    public synchronized void controllerUpdate(ControllerEvent event)
    {
//System.out.println("got event " + event);
        // bare minimum handling of events here.
        if (event instanceof ConfigureCompleteEvent ||
                event instanceof RealizeCompleteEvent ||
                event instanceof PrefetchCompleteEvent)
        {
            synchronized (waitSync)
            {
                stateTransOK = true;
                waitSync.notifyAll();
            }
        }
        else if (event instanceof ResourceUnavailableEvent)
        {
            synchronized (waitSync)
            {
                stateTransOK = false;
                waitSync.notifyAll();
            }
        }
        else if (event instanceof EndOfMediaEvent)
        {
//            processor.setMediaTime(new Time(0));
//            processor.start();
        }

/*
        if(event instanceof ConfigureCompleteEvent)
        {
            processor.setContentDescriptor(null);
            TrackControl[] controls = processor.getTrackControls();

            for(int i = 0; i < controls.length; i++)
            {
                Format format = controls[i].getFormat();
                try
                {
                    if(format instanceof VideoFormat)
                    {
                        controls[i].setRenderer(renderer);
                        controls[i].setFormat(renderer.getSupportedInputFormats()[0]);
                    }

                }
                catch(UnsupportedPlugInException e)
                {
                    System.err.println("Unsupported plugin??? "+ e);
                }
            }

            processor.realize();
        }
        else if(event instanceof RealizeCompleteEvent)
        {
            processor.start();
        }
        else if(event instanceof PrefetchCompleteEvent)
        {
//            processor.setMediaTime(new Time(0));
            processor.start();
        }
        else if(event instanceof StartEvent)
        {
System.out.println("got start");
        }
        else if(event instanceof EndOfMediaEvent)
        {
            // Cause the texture update to loop infinitely by reseting the
            // time to zero and issuing another start.
//            processor.setMediaTime(new Time(0));
//            processor.start();
        }
        else if(event instanceof ControllerErrorEvent)
        {
            System.out.println("Got some form of error " + event);
        }
*/
    }

    public boolean waitForState(int state)
    {
        synchronized (waitSync)
        {
            try
            {
                while (processor.getState() != state && stateTransOK)
                {
                    waitSync.wait();
                }
            }
            catch (Exception e)
            {
            }

            return stateTransOK;
        }
    }
}
