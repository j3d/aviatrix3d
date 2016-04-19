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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.jogamp.openal.*;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Point3d;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;
import org.j3d.util.MatrixUtils;

// Local imports

import org.j3d.aviatrix3d.pipeline.audio.AudioDetails;
import org.j3d.aviatrix3d.pipeline.audio.AudioOutputDevice;
import org.j3d.aviatrix3d.pipeline.audio.AudioInstructions;
import org.j3d.aviatrix3d.pipeline.RenderableRequestData;
import org.j3d.aviatrix3d.pipeline.RenderOp;
import org.j3d.aviatrix3d.rendering.AudioRenderable;
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
 * <li>initFailedMsg: Error message when the OpenAL context initialisation failed</li>
 * <li>nativeLibrariesMissingMsg: OpenAL failed to start due to missing native libraries</li>
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

    /** Message when there is an error during the surface initialisation. */
    private static final String MISSING_NATIVE_LIBS_PROP =
        "org.j3d.aviatrix3d.output.audio.OpenALAudioDevice.nativeLibrariesMissingMsg";

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
    private Matrix4d tmpMatrix;

    /** Listener Position */
    private Point3d listenerPoint;

    /** Scratch point */
    private Point3d tmpPoint;

    /** OpenAL initialization failed */
    private boolean initFailed;

    /** Request that the current drawing terminate immediately. App closing */
    private boolean terminate;

    /** The largest buffer/source id generated */
    private int lastId;

    /** Error reporter used to send out messages */
    private ErrorReporter errorReporter;

    /** Provider for our OpenAL context and device interfaces to abstract away the factory */
    private OpenALProvider openalProvider;

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
        tmpMatrix = new Matrix4d();
        tmpPoint = new Point3d();
        listenerPoint = new Point3d();

        terminate = false;
        initComplete = false;
        initFailed = false;

        lastId = 0;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        openalProvider = new DefaultOpenALProvider();
   }

    //---------------------------------------------------------------
    // Methods defined by AudioOutputDevice
    //---------------------------------------------------------------

    @Override
    public void setDrawableObjects(RenderableRequestData otherData,
                                   AudioInstructions commands)
    {
        this.commands = commands;
        otherDataRequests = otherData;
    }

    //---------------------------------------------------------------
    // Methods defined by OutputDevice
    //---------------------------------------------------------------

    @Override
    public void setErrorReporter(ErrorReporter reporter)
    {
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;
    }

    @Override
    public boolean draw(ProfilingData profilingData)
    {
        if(initFailed)
        {
            return false;
        }

        if(commands == null || commands.numValid == 0)
        {
            return true;
        }

        // tell the draw lock that it's ok to run now, so long as it's not called
        // before the canvas has completed initialisation.
        if(!initComplete)
        {
            try
            {
                al = openalProvider.getAL();
                ALC alc = openalProvider.getALC();

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
				String msg = intl_mgr.getString(MISSING_NATIVE_LIBS_PROP);
                errorReporter.errorReport(msg, null);

                initFailed = true;
                return false;
            }
        }
        else
        {
            // Multithreaded safety = take a local reference to the array and
            // length so that if we get a setDrawableObjects() call midway through
            // executing this loop we don't end up with oddball state.
            AudioInstructions localCommands = commands;
            int len = localCommands.numValid;

            if(len > lastId)
            {
                lastId = len;
            }

            try
            {
                ALC alc = openalProvider.getALC();
                alc.alcMakeContextCurrent(context);
                AudioRenderable obj;
                AudioDetails[] details = localCommands.renderList;

                // Update Listener
                matrixUtils.inverse(localCommands.renderData.viewTransform, tmpMatrix);
                tmpMatrix.transform(listenerPoint,tmpPoint);

                al.alListener3f(AL.AL_POSITION, (float)tmpPoint.x, (float)tmpPoint.y, (float)tmpPoint.z);
                al.alListenerfv(AL.AL_ORIENTATION, DEFAULT_ORIENTATION);

                for(int i = 0; i < len && !terminate; i++)
                {
                    switch(localCommands.renderOps[i])
                    {
                        case START_RENDER:
                            obj = (AudioRenderable)details[i].renderable;
                            obj.render(al, details[i].transform);
                            break;

                        case STOP_RENDER:
                            obj = (AudioRenderable)details[i].renderable;
                            obj.postRender(al);
                            break;

                        default:
                            errorReporter.warningReport("Unknown OP in AudioDevice", null);

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

    @Override
    public Object getSurfaceObject()
    {
        return null;
    }

    @Override
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
                ALC alc = openalProvider.getALC();
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

    @Override
    public boolean isDisposed()
    {
        return terminate || initFailed;
    }

    // ----- Local Methods ---------------------------------------------------

    /**
     * Override the use of the default OpenAL provider that this class uses
     * with a custom provider. Normally this will not need to be set by an
     * end user, mostly used by unit testing code. If the parameter is sent
     * as null, then the default provider is reinstated.
     *
     * @param provider The provider to use or null to use the default
     */
    public void setOpenALProvider(OpenALProvider provider)
    {
        openalProvider = provider != null ? provider : new DefaultOpenALProvider();
    }
}
