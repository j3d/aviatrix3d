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

// External imports
import java.awt.Component;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.j3d.device.input.Tracker;
import org.j3d.device.input.TrackerState;
import org.j3d.device.input.ButtonModeConstants;

// Local imports
// None

/**
 * A tracker implementation for keyboard devices.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class KeyboardTracker extends Tracker implements KeyListener
{
    /** What events is this tracker reporting. */
    private static final int mask = MASK_POSITION;

    /** The tracker state to return */
    private TrackerState tstate;

    /** Whether the forward key is held down */
    private boolean active_forward;

    /** Whether the backward key is held down */
    private boolean active_backward;

    /** Whether the left key is held down */
    private boolean active_left;

    /** Whether the right key is held down */
    private boolean active_right;

    /** Are any keys held */
    private boolean inMotion;

    /** How fast should we rotate. 0 to 2 */
    private float rot_speed;

    /** How fast should we translate.  0 to 2 */
    private float trans_speed;

    public KeyboardTracker()
    {
        tstate = new TrackerState();
        trans_speed = 1.0f;
        rot_speed = 0.5f;
    }

    //------------------------------------------------------------------------
    // Methods defined by Tracker
    //------------------------------------------------------------------------

    /**
     * What action types does this sensor return.  This a combination
     * of ACTION masks.
     *
     * @return The action mask.
     */
    public int getActionMask()
    {
        return mask;
    }

    /**
     * Notification that tracker polling is beginning.
     */
    public void beginPolling()
    {
        // TODO: Should we double buffer?
    }

    /**
     * Notification that tracker polling is ending.
     */
    public void endPolling()
    {
    }

    /**
     * Get the current state of this tracker.
     *
     * @param layer The ID of the layer to get the state for
     * @param subLayer The ID of the sub layer within the parent layer
     * @param state The current state
     */
    public void getState(int layer, int subLayer, TrackerState state)
    {
        // Layer information is ignored.

        state.actionMask = mask;
        state.actionType = tstate.actionType;
        state.devicePos[0] = tstate.devicePos[0];
        state.devicePos[1] = tstate.devicePos[1];
        state.devicePos[2] = tstate.devicePos[2];
        state.deviceOri[0] = tstate.deviceOri[0];
        state.deviceOri[1] = tstate.deviceOri[1];
        state.deviceOri[2] = tstate.deviceOri[2];
        state.worldPos[0] = tstate.worldPos[0];
        state.worldPos[1] = tstate.worldPos[1];
        state.worldPos[2] = tstate.worldPos[2];
        state.worldOri[0] = tstate.worldOri[0];
        state.worldOri[1] = tstate.worldOri[1];
        state.worldOri[2] = tstate.worldOri[2];

        state.buttonMode[0] = ButtonModeConstants.WALK;

        // Switch to PAN for Doom style navigation
        //state.buttonMode[1] = ButtonModeConstants.PAN;
        state.buttonMode[1] = ButtonModeConstants.WALK;

        for(int i=0; i < tstate.buttonState.length; i++)
        {
            state.buttonState[i] = tstate.buttonState[i];
        }

        if (inMotion)
        {
            tstate.actionType = TrackerState.TYPE_DRAG;

            if (active_forward)
                tstate.devicePos[1] = -trans_speed;
            else if (active_backward)
                tstate.devicePos[1] = trans_speed;
            else
                tstate.devicePos[1] = 0;

            if (active_right)
                tstate.devicePos[0] = rot_speed;
            else if (active_left)
                tstate.devicePos[0] = -rot_speed;
            else
                tstate.devicePos[0] = 0;

        }
        else
            tstate.actionType = TrackerState.TYPE_NONE;
    }

    //------------------------------------------------------------------------
    // Methods defined by KeyListener
    //------------------------------------------------------------------------

    /**
     * Process a key press event.
     *
     * @param evt The event that caused this method to be called
     */
    public void keyPressed(KeyEvent evt)
    {
        boolean new_active=false;
        int button=0;

        switch(evt.getKeyCode())
        {
            case KeyEvent.VK_UP:
                if (!inMotion)
                    new_active = true;
                active_forward = true;
                button = 0;
                break;
            case KeyEvent.VK_RIGHT:
                if (!inMotion)
                    new_active = true;
                active_right = true;
                button = 1;
                break;
            case KeyEvent.VK_LEFT:
                if (!inMotion)
                    new_active = true;
                active_left = true;
                button = 1;
                break;
            case KeyEvent.VK_DOWN:
                if (!inMotion)
                    new_active = true;
                active_backward = true;
                button = 0;
                break;
        }

        if (evt.isShiftDown())
            trans_speed = 3.0f;
        else
            trans_speed = 1.0f;

        if (new_active)
        {
            inMotion = true;
            tstate.actionType = TrackerState.TYPE_PRESS;

            tstate.devicePos[0] = 0.0f;
            tstate.devicePos[1] = 0.0f;
            tstate.devicePos[2] = 0.0f;

            tstate.buttonState[0] = false;
            tstate.buttonState[1] = false;
            tstate.buttonState[button] = true;
        }
    }

    /**
     * Process a key release event.
     *
     * @param evt The event that caused this method to be called
     */
    public void keyReleased(KeyEvent evt)
    {
        if (evt.isShiftDown())
            trans_speed = 3.0f;
        else
            trans_speed = 1.0f;

        switch(evt.getKeyCode())
        {
            case KeyEvent.VK_UP:
                active_forward = false;
                break;
            case KeyEvent.VK_RIGHT:
                active_right = false;
                break;
            case KeyEvent.VK_LEFT:
                active_left = false;
                break;
            case KeyEvent.VK_DOWN:
               active_backward = false;
               break;
        }

        if (!active_forward && !active_right && !active_left)
        {
            inMotion = false;
            tstate.actionType = TrackerState.TYPE_RELEASE;
            tstate.buttonState[0] = false;
            tstate.buttonState[1] = false;
        }
    }

    /**
     * Process a key typed event.
     *
     * @param evt The event that caused this method to be called
     */
    public void keyTyped(KeyEvent evt)
    {
    }
}
