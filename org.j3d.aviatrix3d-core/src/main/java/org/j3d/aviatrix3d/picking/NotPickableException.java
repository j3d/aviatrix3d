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
 * An exception for when an attempt is made to perform a pick on a node that
 * has been marked as not pickable.
 * <p>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class NotPickableException extends RuntimeException
{

    /**
     * Creates a new exception without detail message.
     */
    public NotPickableException()
    {
    }


    /**
     * Constructs an exception with the specified detail message.
     *
     * @param msg the detail message.
     */
    public NotPickableException(String msg)
    {
        super(msg);
    }
}
