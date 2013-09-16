/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2004 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.output.audio;

// External imports
import net.java.games.joal.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.MatrixUtils;
import org.j3d.util.IntHashMap;

import org.j3d.aviatrix3d.pipeline.audio.AudioDetails;
import org.j3d.aviatrix3d.pipeline.audio.AudioOutputDevice;
import org.j3d.aviatrix3d.pipeline.audio.AudioInstructions;
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;
import org.j3d.aviatrix3d.pipeline.RenderOp;
import org.j3d.aviatrix3d.rendering.AudioRenderable;
import org.j3d.aviatrix3d.rendering.DeletableRenderable;
import org.j3d.aviatrix3d.rendering.ProfilingData;

/**
 * Implementation of the most basic audio device, supporting the minimal
 * number of features using OpenAL.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>disposeFailedMsg: Error message when the disposal of an OpenAL context
 *     failed</li>
 * <li>makeCurrentFailedMsg: Error message when making the OpenAL context current for
 *     rendering failed</li>
 * <li>initFailedMsg: Error message when the OpenAL context initialisation
 *     failed</li>
 * </ul>
 *
 * @author Alan Hudson
 * @version $Revision: 2.14 $
 */
public class OpenALAudioDevice
    implements AudioOutputDevice
{
    /** Message when there was an error during the dispose process */
    private static final String DISPOSE_FAILURE_PROP =
		"org.j3d.aviatrix3d.output.audio.OpenALAudioDevice.disposeFailedMsg";

    /** Message when there is an error during the draw processing */
    private static final String DRAW_FAILURE_PROP =
		"org.j3d.aviatrix3d.output.audio.OpenALAudioDevice.makeCurrentFailedMsg";

    /** Message when there is an error during the surface initialisation. */
    private static final String INIT_FAILURE_PROP =
		"org.j3d.aviatrix3d.output.audio.OpenALAudioDevice.initFailedMsg";

    /** Default orientation that we set the listener to every frame */
    private static final FloatBuffer DEFAULT_ORIENTATION;

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 20;

    /** Request object for deletions, shader stuff etc */
    protected RenderableRequestData otherDataRequests;

    /** Number of items in the renderable list */
    private int numRenderables;

    /** Flag to say that init has been called on the canvas */
    private boolean initComplete;

    /** The AudioLibary to render to */
    private AL al;

    /** The Audio device context in use */
    private ALCcontext context;

    /** The audio device itself */
    private ALCdevice device;

    /** Instructions that need to be rendered this frame */
    private AudioInstructions commands;

    /** Matrix Utilities to invert matrices */
    private MatrixUtils matrixUtils;

    /** Scratch matrix val */
    private Matrix4f tmpMatrix;

    /** Listener Position */
    private Point3f listenerPoint;

    /** Scratch point */
    private Point3f tmpPoint;

    /** OpenAL initialization failed */
    private boolean initFailed;

    /** Request that the current drawing terminate immediately. App closing */
    private boolean terminate;

    /** The largest buffer/source id generated */
    private int lastId;

    /** Error reporter used to send out messages */
    private ErrorReporter errorReporter;

    /** Single threaded rendering mode operation state. Defaults to false. */
    private boolean singleThreaded;

    /**
     * Static constructor for generating the default orientation.
     */
    static
    {
        // Need to allocate a byte buffer of 24 bytes, to equate to an 6 floats
        // size is treated as bytes, not number of ints.
        ByteBuffer buf = ByteBuffer.allocateDirect(24);
        buf.order(ByteOrder.nativeOrder());
        DEFAULT_ORIENTATION = buf.asFloatBuffer();

        float[] values = { 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f };

        DEFAULT_ORIENTATION.put(values);
        DEFAULT_ORIENTATION.rewind();
    }

    /**
     * Construct a surface that requires the given set of capabilities.
     */
    public OpenALAudioDevice()
    {
        matrixUtils = new MatrixUtils();
        tmpMatrix = new Matrix4f();
        tmpPoint = new Point3f();
        listenerPoint = new Point3f(0,0,0);

        terminate = false;
        initComplete = false;
        initFailed = false;
        singleThreaded = false;

        lastId = 0;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
   }

    //---------------------------------------------------------------
    // Methods defined by AudioOutputDevice
    //---------------------------------------------------------------

    /**
     * Update the list of items to be rendered to the current list. Draw them
     * at the next oppourtunity.
     *
     * @param otherData data to be processed before the rendering
     * @param commands The list of drawable surfaces to render
     */
    public void setDrawableObjects(RenderableRequestData otherData,
                                   AudioInstructions commands)
    {
        this.commands = commands;
        otherDataRequests = otherData;
    }

    //---------------------------------------------------------------
    // Methods defined by OutputDevice
    //---------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter)
    {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }

    /**
     * Instruct the surface to draw the collected set of nodes now. The
     * registered view environment is used to draw to this surface. If no
     * view is registered, the surface is cleared and then this call is
     * exited. The drawing surface does not swap the buffers at this point.
     * <p>
     * The return value indicates success or failure in the ability to
     * render this frame. Typically it will indicate failure if the
     * underlying surface has been disposed of, either directly through the
     * calling of the method on this interface, or through an internal check
     * mechanism. If failure is indicated, then check to see if the surface has
     * been disposed of and discontinue rendering if it has.
     *
     * @param profilingData The timing and load data
     * @return true if the drawing succeeded, or false if not
     */
    public boolean draw(ProfilingData profilingData)
    {
        if(initFailed)
            return false;

        // tell the draw lock that it's ok to run now, so long as it's not called
        // before the canvas has completed initialisation.
        if(!initComplete)
        {
            if(commands == null || commands.numValid == 0)
                return true;

            try
            {
                al = ALFactory.getAL();
                ALC alc = ALFactory.getALC();

                device = alc.alcOpenDevice(null);
                context = alc.alcCreateContext(device, null);
                alc.alcMakeContextCurrent(context);
                al.alGetError();

                al.alDistanceModel(AL.AL_INVERSE_DISTANCE_CLAMPED);
                alc.alcMakeContextCurrent(null);

                initComplete = true;
            }
            catch(Exception e)
            {
				I18nManager intl_mgr = I18nManager.getManager();
				String msg = intl_mgr.getString(INIT_FAILURE_PROP);
                errorReporter.errorReport(msg, e);

                // Drop out and exit right now with failure indicated
                initFailed = true;
                return false;
            }
            catch(UnsatisfiedLinkError ule)
			{
				I18nManager intl_mgr = I18nManager.getManager();
				String msg = intl_mgr.getString(INIT_FAILURE_PROP);
                errorReporter.errorReport(msg, null);

                initFailed = true;
                return false;
            }
        }
        else
        {
            if(commands == null)
                return true;

            AudioInstructions localCommands = commands;
            int len = localCommands.numValid;

            if(len == 0)
                return true;

            if(len > lastId)
                lastId = len;

            try
            {
                ALC alc = ALFactory.getALC();
                alc.alcMakeContextCurrent(context);
                AudioRenderable obj;
                AudioDetails[] details = localCommands.renderList;

                // Update Listener
                matrixUtils.inverse(localCommands.renderData.viewTransform, tmpMatrix);
                tmpMatrix.transform(listenerPoint,tmpPoint);

                al.alListener3f(AL.AL_POSITION, tmpPoint.x, tmpPoint.y, tmpPoint.z);
                al.alListenerfv(AL.AL_ORIENTATION, DEFAULT_ORIENTATION);

                for(int i = 0; i < len && !terminate; i++)
                {
                    switch(localCommands.renderOps[i])
                    {
                        case RenderOp.START_RENDER:
                            obj = (AudioRenderable)details[i].renderable;
                            obj.render(al, details[i].transform);
                            break;

                        case RenderOp.STOP_RENDER:
                            obj = (AudioRenderable)details[i].renderable;
                            obj.postRender(al);
                            break;

                        default:
                            System.out.println("Unknown OP in AudioDevice");

                    }
                }

                alc.alcMakeContextCurrent(null);
            }
            catch(ALException oae)
            {
				I18nManager intl_mgr = I18nManager.getManager();
				String msg = intl_mgr.getString(DRAW_FAILURE_PROP);
                errorReporter.errorReport(msg, oae);
            }
        }

        return !terminate;
    }

    /**
     * Get the underlying object that this surface is rendered to. If it is a
     * screen display device, the surface can be one of AWT Component or
     * Swing JComponent. An off-screen buffer would be a form of AWT Image etc.
     *
     * @return The drawable surface representation
     */
    public Object getSurfaceObject()
    {
        return null;
    }

    /**
     * Instruct this surface that you have finished with the resources needed
     * and to dispose all rendering resources.
     */
    public void dispose()
    {
        // OpenAL has some shutdown code to do this.  This code occasionally
        // dies, so lets not run it for now.  We should cleanup all
        // sources and buffers though.
        terminate = true;

        if(initComplete)
        {
            try
            {
                ALC alc = ALFactory.getALC();
                alc.alcMakeContextCurrent(context);

                // TODO: Assume id's are issued sequentially.  Really need
                // a better way todo this.  OpenGL cleans this up for us, make joal do it?
                int[] buffer = new int[lastId + 1];

                for(int i = 0; i <= lastId; i++)
                    buffer[i] = i;

                al.alDeleteBuffers(buffer.length, buffer, 0);
                al.alDeleteSources(buffer.length, buffer, 0);

                alc.alcMakeContextCurrent(null);
                alc.alcDestroyContext(context);
                alc.alcCloseDevice(device);

                al = null;
            }
            catch(ALException oal)
            {
				I18nManager intl_mgr = I18nManager.getManager();
				String msg = intl_mgr.getString(DISPOSE_FAILURE_PROP);
                errorReporter.errorReport(msg, oal);
            }
        }
    }

    /**
     * Check to see the disposal state of the surface. Will return true if the
     * {@link #dispose} method has been called or an internal dispose handler
     * has detected the underlying surface is no longer valid to draw to.
     *
     * @return true if the surface is disposed and no longer usable
     */
    public boolean isDisposed()
    {
        return terminate || initFailed;
    }

    /**
     * Notification that this surface is being drawn to with a single thread.
     * This can be used to optmise internal state handling when needed in a
     * single versus multithreaded environment.
     * <p>
     *
     * This method should never be called by end user code. It is purely for
     * the purposes of the {@link org.j3d.aviatrix3d.management.RenderManager}
     * to inform the device about what state it can expect.
     *
     * @param state true if the device can expect single threaded behaviour
     */
    public void enableSingleThreaded(boolean state)
    {
        singleThreaded = state;
    }

    /**
     * If the output device is marked as single threaded, this instructs the
     * device that the current rendering thread has exited. Next time the draw
     * method is called, a new rendering context will need to be created for
     * a new incoming thread instance. Also, if any other per-thread resources
     * are around, clean those up now. This is called just before that thread
     * exits.
     */
    public void disposeSingleThreadResources()
    {
        // Do nothing for now until we implement this properly.
    }
}
