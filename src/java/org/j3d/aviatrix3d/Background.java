/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d;

// Standard imports
import java.util.HashMap;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;

// Local imports
// None

/**
 * Base collection of functionality marking background nodes of various types.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class Background extends Leaf
{
    /** Map of display contexts to maps */
    protected HashMap dispListMap;

    /**
     * Constructs a background node
     */
    protected Background()
    {
    }
}