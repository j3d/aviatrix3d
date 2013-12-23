/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
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


/**
 * An exception for when an attempt is made to set a bounds or data changed
 * listener at a time when it is not allowed to be.
 * <p>
 *
 * Writing is only allowed during the
 * {@link ApplicationUpdateObserver#updateSceneGraph()} callback.
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class InvalidListenerSetTimingException extends RuntimeException
{

    /**
     * Creates a new exception without detail message.
     */
    public InvalidListenerSetTimingException()
    {
    }


    /**
     * Constructs an exception with the specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidListenerSetTimingException(String msg)
    {
        super(msg);
    }
}
