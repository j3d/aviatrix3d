/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.device.input.keyboard;

// Local imports
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.j3d.device.input.TrackerDevice;
import org.j3d.device.input.Tracker;

// External imports
// None

/**
 * A keyboard device.  This device is a navigation only device.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class KeyboardDevice implements TrackerDevice, KeyListener
{
    private KeyboardTracker[] trackers;

    /** The device name */
    private String name;

	/**
	 * Construct a new keyboard device with the given name.
	 *
	 * @param name The name string to associate with this device
	 */
    public KeyboardDevice(String name)
    {
        trackers = new KeyboardTracker[1];
        trackers[0] = new KeyboardTracker();

        this.name = name;
    }

    //------------------------------------------------------------------------
    // Methods for InputDevice interface
    //------------------------------------------------------------------------

    /**
     * Get the name of this device.  Names are of the form class-#.  Valid
     * classes are Gamepad, Joystick, Wheel, Midi, GenericHID.
     *
     * @return The name
     */
    public String getName()
    {
        return name;
    }

    //------------------------------------------------------------------------
    // Methods for InputDevice interface
    //------------------------------------------------------------------------

    public Tracker[] getTrackers()
    {
        return trackers;
    }

    /**
     * Get a count of the number of trackers this device has.  This cannot
     * change during the life of a device.
     */
    public int getTrackerCount()
    {
        return 1;
    }

    //------------------------------------------------------------------------
    // Methods for KeyListener events
    //------------------------------------------------------------------------

    /**
     * Notification of a key press event. This will any one of the key value
     * fields depending on the value.
     *
     * @param evt The key event that caused this method to be called
     */
    public void keyPressed(KeyEvent evt)
    {
        trackers[0].keyPressed(evt);
    }

    /**
     * Notification of a key release event. This will any one of the key value
     * fields depending on the value.
     *
     * @param evt The key event that caused this method to be called
     */
    public void keyReleased(KeyEvent evt)
    {
        trackers[0].keyReleased(evt);
    }

    /**
     * Notification of a key type (press and release) event. This will any one
     * of the key value fields depending on the value.
     *
     * @param evt The key event that caused this method to be called
     */
    public void keyTyped(KeyEvent evt)
    {
        trackers[0].keyTyped(evt);
    }
}
