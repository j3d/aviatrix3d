/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.picking;


/**
 * An exception for when an attempt is made to perform a pick on a node when
 * it is not permitted by the scene graph.
 * <p>
 *
 * Picking is only permitted during one of the two update callbacks
 * on NodeUpdateListener.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class InvalidPickTimingException extends RuntimeException
{

    /**
     * Creates a new exception without detail message.
     */
    public InvalidPickTimingException()
    {
    }


    /**
     * Constructs an exception with the specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidPickTimingException(String msg)
    {
        super(msg);
    }
}
