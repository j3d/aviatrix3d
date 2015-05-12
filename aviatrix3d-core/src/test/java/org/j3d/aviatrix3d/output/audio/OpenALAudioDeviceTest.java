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

import com.jogamp.openal.AL;
import com.jogamp.openal.ALC;
import com.jogamp.openal.ALCdevice;
import org.j3d.util.I18nManager;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import org.j3d.aviatrix3d.pipeline.audio.AudioDetails;
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

    @Mock
    private AL mockAL;

    @Mock
    private ALC mockALC;

    @BeforeMethod(groups = "unit")
    public void setupTests() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication("OpenALAudioDeviceTest", "config.i18n.org-j3d-aviatrix3d-resources-core");

        when(mockProvider.getAL()).thenReturn(mockAL);
        when(mockProvider.getALC()).thenReturn(mockALC);
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
    }
}
