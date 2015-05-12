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
import com.jogamp.openal.ALFactory;

/**
 * Default implementation of the {@link OpenALProvider} that directly
 * maps to the factory classes provided by Jogamp.
 *
 * @author justin
 */
public class DefaultOpenALProvider implements OpenALProvider
{
    @Override
    public AL getAL()
    {
        return ALFactory.getAL();
    }

    @Override
    public ALC getALC()
    {
        return ALFactory.getALC();
    }
}
