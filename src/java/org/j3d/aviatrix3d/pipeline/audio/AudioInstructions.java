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

package org.j3d.aviatrix3d.pipeline.audio;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.pipeline.RenderInstructions;

/**
 * Class for passing the detailed rendering information for a single device.
 * from the sorter through to the renderable device.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 2.0 $
 */
public class AudioInstructions extends RenderInstructions
{
    /** Visual data such as viewpoint, background etc */
    public AudioEnvironmentData renderData;

    /** The list of nodes in sorted order */
    public AudioDetails[] renderList;

    /**
     * Construct a new instance of this class with the arrays initialised
     * to a default size.
     */
    public AudioInstructions()
    {
        renderList = new AudioDetails[LIST_START_SIZE];

        for(int i = 0; i < LIST_START_SIZE; i++)
            renderList[i] = new AudioDetails();
    }

}
