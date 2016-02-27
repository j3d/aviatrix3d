/*
 * **************************************************************************
 *                        Copyright j3d.org (c) 2000 - ${year}
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read docs/lgpl.txt for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * **************************************************************************
 */

package org.j3d.aviatrix3d.output.audio;

import java.nio.IntBuffer;

import com.jogamp.openal.*;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import org.j3d.aviatrix3d.pipeline.RenderOp;
import org.j3d.aviatrix3d.pipeline.audio.AudioDetails;
import org.j3d.aviatrix3d.pipeline.audio.AudioEnvironmentData;
import org.j3d.aviatrix3d.pipeline.audio.AudioInstructions;
import org.j3d.aviatrix3d.rendering.AudioRenderable;

/**
 * Unit tests for the audio output device.
 *
 * @author justin
 */
public class OpenALAudioDeviceTest
{
    @Mock
    private OpenALProvider mockProvider;

    private AL mockAL;

    @Mock
    private ALC mockALC;

    private ALCcontext mockContext;

    private ALCdevice mockDevice;

    @Mock
    private ErrorReporter mockReporter;

    @BeforeMethod(groups = "unit")
    public void setupTests() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication("OpenALAudioDeviceTest", "config.i18n.org-j3d-aviatrix3d-resources-core");

        AL al = ALFactory.getAL();
        mockAL = spy(al);

        ALCcontext context = ALCcontext.create();
        mockContext = spy(context);

        ALCdevice device = ALCdevice.create();
        mockDevice = spy(device);

        when(mockProvider.getAL()).thenReturn(mockAL);
        when(mockProvider.getALC()).thenReturn(mockALC);

        when(mockALC.alcOpenDevice(anyString())).thenReturn(mockDevice);
        when(mockALC.alcCreateContext(mockDevice, null)).thenReturn(mockContext);

    }

    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        OpenALAudioDevice class_under_test = new OpenALAudioDevice();

        assertFalse(class_under_test.isDisposed(), "Should not be disposed after initial creation");
        assertNull(class_under_test.getSurfaceObject(), "No surface created until after first render");

        class_under_test.dispose();
        assertTrue(class_under_test.isDisposed(), "Should be disposed after calling dispose()");
    }

    @Test(groups = "unit")
    public void testDrawingWithNoData() throws Exception
    {
        OpenALAudioDevice class_under_test = new OpenALAudioDevice();

        assertTrue(class_under_test.draw(null), "Should successfully not draw anything");
    }

    @Test(groups = "unit")
    public void testDrawingInitialisation() throws Exception
    {
        OpenALAudioDevice class_under_test = new OpenALAudioDevice();
        class_under_test.setOpenALProvider(mockProvider);
        class_under_test.setErrorReporter(mockReporter);

        AudioRenderable mock_renderable = mock(AudioRenderable.class);

        // register an object to draw
        AudioDetails test_details = new AudioDetails();
        test_details.transform.setIdentity();
        test_details.renderable = mock_renderable;

        AudioInstructions test_instructions = new AudioInstructions();
        test_instructions.renderList = new AudioDetails[] { test_details };
        test_instructions.numValid = 1;

        class_under_test.setDrawableObjects(null, test_instructions);
        assertTrue(class_under_test.draw(null), "Should successfully not draw anything");

        verify(mockALC, times(1)).alcOpenDevice(anyString());
        verify(mockALC, times(1)).alcCreateContext(any(ALCdevice.class), any(IntBuffer.class));

        // Make sure the context was made current and cleared afterwards.
        verify(mockALC, times(1)).alcMakeContextCurrent(mockContext);
        verify(mockALC, times(1)).alcMakeContextCurrent(null);

        // Make sure dispose works correctly on shutdown too
        class_under_test.dispose();

        verify(mockALC, times(1)).alcCloseDevice(mockDevice);
        verify(mockALC, times(1)).alcDestroyContext(mockContext);

        assertTrue(class_under_test.isDisposed(), "Should mark the device as disposed");
    }

    @Test(groups = "unit")
    public void testDrawingFailedInitialisation() throws Exception
    {
        OpenALAudioDevice class_under_test = new OpenALAudioDevice();
        class_under_test.setOpenALProvider(mockProvider);
        class_under_test.setErrorReporter(mockReporter);

        AudioRenderable mock_renderable = mock(AudioRenderable.class);

        // register an object to draw
        AudioDetails test_details = new AudioDetails();
        test_details.transform.setIdentity();
        test_details.renderable = mock_renderable;

        AudioInstructions test_instructions = new AudioInstructions();
        test_instructions.renderList = new AudioDetails[] { test_details };
        test_instructions.numValid = 1;

        class_under_test.setDrawableObjects(null, test_instructions);

        doThrow(new RuntimeException("test")).when(mockALC).alcOpenDevice(null);

        assertFalse(class_under_test.draw(null), "Should not draw anything if error thrown");

        // Make sure create context does not get called in this case
        verify(mockALC, never()).alcCreateContext(any(ALCdevice.class), any(IntBuffer.class));
        verify(mockReporter, times(1)).errorReport(anyString(), any(Throwable.class));

        assertFalse(class_under_test.draw(null), "Should not successfully draw after failure");
        assertTrue(class_under_test.isDisposed(), "Should mark the device as disposed");
    }

    @Test(groups = "unit")
    public void testDrawingFailedNoNativeLibs() throws Exception
    {
        OpenALAudioDevice class_under_test = new OpenALAudioDevice();
        class_under_test.setOpenALProvider(mockProvider);
        class_under_test.setErrorReporter(mockReporter);

        AudioRenderable mock_renderable = mock(AudioRenderable.class);

        // register an object to draw
        AudioDetails test_details = new AudioDetails();
        test_details.transform.setIdentity();
        test_details.renderable = mock_renderable;

        AudioInstructions test_instructions = new AudioInstructions();
        test_instructions.renderList = new AudioDetails[] { test_details };
        test_instructions.numValid = 1;

        class_under_test.setDrawableObjects(null, test_instructions);

        doThrow(new UnsatisfiedLinkError("test")).when(mockALC).alcOpenDevice(null);

        assertFalse(class_under_test.draw(null), "Should not draw anything if error thrown");

        // Make sure create context does not get called in this case
        verify(mockALC, never()).alcCreateContext(any(ALCdevice.class), any(IntBuffer.class));
        verify(mockReporter, times(1)).errorReport(anyString(), (Throwable) isNull());

        assertFalse(class_under_test.draw(null), "Should not successfully draw after failure");
        assertTrue(class_under_test.isDisposed(), "Should mark the device as disposed");
    }

    @Test(groups = "unit")
    public void testDrawing() throws Exception
    {
        OpenALAudioDevice class_under_test = new OpenALAudioDevice();
        class_under_test.setOpenALProvider(mockProvider);
        class_under_test.setErrorReporter(mockReporter);

        AudioRenderable mock_renderable = mock(AudioRenderable.class);

        // register an object to draw
        AudioDetails test_details = new AudioDetails();
        test_details.transform.setIdentity();
        test_details.renderable = mock_renderable;

        AudioInstructions test_instructions = new AudioInstructions();
        test_instructions.renderData = new AudioEnvironmentData();
        test_instructions.renderData.viewTransform.setIdentity();
        test_instructions.renderList = new AudioDetails[] { test_details, test_details };
        test_instructions.numValid = 2;
        test_instructions.renderOps = new int[] { RenderOp.START_RENDER, RenderOp.STOP_RENDER };

        class_under_test.setDrawableObjects(null, test_instructions);

        // First draw to make sure init happens
        class_under_test.draw(null);

        // Now the real draw call. Should result in the renderable being called
        class_under_test.draw(null);

        // Make sure the context was made current and cleared twice - once during
        // the init draw() call, and then again during the proper draw.
        verify(mockALC, times(2)).alcMakeContextCurrent(mockContext);
        verify(mockALC, times(2)).alcMakeContextCurrent(null);

        verify(mock_renderable, times(1)).render(eq(mockAL), any(Matrix4d.class));
        verify(mock_renderable, times(1)).postRender(eq(mockAL));
    }

    @Test(groups = "unit")
    public void testExceptionInDisposeStillCloses() throws Exception
    {
        OpenALAudioDevice class_under_test = new OpenALAudioDevice();
        class_under_test.setOpenALProvider(mockProvider);
        class_under_test.setErrorReporter(mockReporter);

        AudioRenderable mock_renderable = mock(AudioRenderable.class);

        // register an object to draw
        AudioDetails test_details = new AudioDetails();
        test_details.transform.setIdentity();
        test_details.renderable = mock_renderable;

        AudioInstructions test_instructions = new AudioInstructions();
        test_instructions.renderList = new AudioDetails[] { test_details };
        test_instructions.numValid = 1;

        class_under_test.setDrawableObjects(null, test_instructions);

        doThrow(new ALException("test")).when(mockALC).alcDestroyContext(mockContext);

        // Force the init and then immediately dispose.
        class_under_test.draw(null);
        class_under_test.dispose();

        assertTrue(class_under_test.isDisposed(), "Should mark the device as disposed");
    }
}
