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

package org.j3d.renderer.aviatrix3d.device.input.mouse;

// External imports
import java.util.ArrayList;

import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

import org.j3d.device.input.InputDevice;
import org.j3d.device.input.DeviceManager;
import org.j3d.device.input.DeviceListener;

import org.j3d.util.ErrorReporter;
import org.j3d.util.DefaultErrorReporter;

// Local imports
// None

/**
 * Manages Mouse devices.  Assumes only 1 device currently.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class MouseManager implements DeviceManager {

    /** The device list */
    private ArrayList<InputDevice> devices;

    /** List of those who want to know about device changes. */
    private ArrayList<DeviceListener> deviceListeners;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /**
     * Create a basic manager for mice.
     */
    public MouseManager(GraphicsOutputDevice surface) {
        devices = new ArrayList<InputDevice>();

        deviceListeners = new ArrayList<DeviceListener>();
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        MouseDevice device = new MouseDevice(surface, "Mouse-0");
        devices.add(device);
    }

    //------------------------------------------------------------------------
    // Methods defined by DeviceManager
    //------------------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Get the number of devices discovered.
     *
     * @return The number of devices.
     */
    public int getNumDevices() {
        return devices.size();
    }

    /**
     * Get the device discovered by this manager.  All devices discovered
     * after this call will be reported to DeviceListeners.
     *
     * @return InputDevice[] An array of discovered devices.
     */
    public InputDevice[] getDevices() {
        InputDevice[] devs = new InputDevice[devices.size()];

        devices.toArray(devs);

        return devs;
    }

    /**
     * Add a listener for devices additions and removals.
     *
     * @param l The listener.  Nulls and duplicates will be ignored.
     */
    public void addDeviceListener(DeviceListener l) {
        if(!deviceListeners.contains(l))
            deviceListeners.add(l);

    }

    /**
     * Remove a listener for device additions and removals.
     *
     * @param l The listener.  Nulls and not found listeners will be ignored.
     */
    public void removeDeviceListener(DeviceListener l) {
        deviceListeners.remove(l);
    }
}
