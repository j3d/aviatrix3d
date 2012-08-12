/*****************************************************************************
 *                  Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.rendering;

// External imports
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import javax.media.opengl.GL;

// Local imports
// None

/**
 * Convenience marker interface for combining the object and transparent
 * renderable interfaces together to save a heap of casting.
 * <p>
 *
 * Created because we can't always assume that object renderable is also
 * one that we need to know about transparency, and several transparent
 * renderables that are not ObjectRenderables.
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public interface TransparentObjectRenderable
    extends TransparentRenderable, ObjectRenderable
{
}
