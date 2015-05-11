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

package org.j3d.aviatrix3d.management;

import org.j3d.aviatrix3d.Layer;
import org.j3d.aviatrix3d.rendering.ViewportCullable;

/**
 * Utility class for testing layer processing in the local unit tests.
 *
 * @author justin
 */
class TestLayer extends Layer
{
    TestLayer()
    {
        super(Layer.SIMPLE);
    }

    @Override
    public ViewportCullable getCullableViewport(int viewportIndex)
    {
        return null;
    }

    @Override
    public int numCullableChildren()
    {
        return 0;
    }
};
