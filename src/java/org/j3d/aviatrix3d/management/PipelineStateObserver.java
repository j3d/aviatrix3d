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

package org.j3d.aviatrix3d.management;

// External imports
// None

// Local imports
// None

/**
 * Wrapper around a single pipeline that controls its management in a separate
 * thread.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
interface PipelineStateObserver
{
    /**
     * Notification that the frame state has finished management.
     */
    public void frameFinished();
}
