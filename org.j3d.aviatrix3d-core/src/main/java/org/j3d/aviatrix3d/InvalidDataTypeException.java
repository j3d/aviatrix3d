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
 * An exception for when an attempt is made to set or fetch a value from any
 * node in the scene graph, but the user-provided data type does not match that
 * required.
 * <p>
 *
 * This generic exception can be found anywhere in the scene graph nodes. In
 * particular, you'll find it creeping up in methods that could be used to
 * fetch data for multiple internal parameter types, but require a specific
 * data type. For example, the method takes a float[] but the internal data
 * is stored as int[].
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public class InvalidDataTypeException extends RuntimeException
{

    /**
     * Creates a new exception without detail message.
     */
    public InvalidDataTypeException()
    {
    }


    /**
     * Constructs an exception with the specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidDataTypeException(String msg)
    {
        super(msg);
    }
}
