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

package org.j3d.aviatrix3d.output.graphics;


/**
 * An exception indicating that Pbuffers are unavailable to be created, and
 * thus the {@link PbufferSurface} will be invalid.
 *
 * @author  Justin Couch
 * @version $Revision: 3.1 $
 */
public class PbufferUnavailableException extends RuntimeException
{

    /**
     * Creates a new exception without detail message.
     */
    public PbufferUnavailableException()
    {
    }


    /**
     * Constructs an exception with the specified detail message.
     *
     * @param msg the detail message.
     */
    public PbufferUnavailableException(String msg)
    {
        super(msg);
    }
}
