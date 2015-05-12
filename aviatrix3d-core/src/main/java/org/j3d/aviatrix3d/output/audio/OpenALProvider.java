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

import com.jogamp.openal.AL;
import com.jogamp.openal.ALC;

/**
 * Abstraction of the provider of OpenAL interfaces so that we can
 * properly test the outputs that are sent to OpenAL. The default
 * implementation of this interface will be used by the device, so
 * there is no real need to provide your own of this unless you are
 * mocking it in unit tests.
 *
 * @author justin
 */
public interface OpenALProvider
{
    /**
     * Get the main open AL library interface
     *
     * @return A non-null OpenAL abstraction
     */
    public AL getAL();

    /**
     * Get the main open AL context management library interface
     *
     * @return A non-null OpenAL Context abstraction
     */
    public ALC getALC();
}
