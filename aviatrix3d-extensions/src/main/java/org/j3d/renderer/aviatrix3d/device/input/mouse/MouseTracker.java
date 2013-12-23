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
import java.awt.Component;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.j3d.device.input.Tracker;
import org.j3d.device.input.TrackerState;
import org.j3d.device.input.ButtonModeConstants;

// Local imports
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;
import org.j3d.maths.vector.Matrix4d;
import org.j3d.maths.vector.Point3d;
import org.j3d.maths.vector.Vector3d;

/**
 * A tracker implementation for mouse devices under OpenGL.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class MouseTracker extends Tracker
    implements MouseListener, MouseMotionListener, MouseWheelListener
{

    /** What actions might we perform */
    private static final int mask = MASK_PICKING | MASK_POSITION | MASK_ORIENTATION;

    /** The internal identifier of this tracker device */
    private final String deviceId;

    /** The latest state */
    private TrackerState[] tstate;

    /** The current buffer idx for new double buffered values */
    private int currentIndex;

    /** The last index for reading */
    private int lastIndex;

    /** Representation of the direction that we pick in */
    private Vector3d mousePickDirection;

    /** Representation of the eye in the world */
    private Point3d mouseEyePosition;

    /** The eye position in the image plate (canvas) world coords */
    private Point3d mousePosition;

    /** The matrix to read the view VWorld coordinates */
    private Matrix4d surfaceTransform;

    /** The surface to perform 2D to 3D calcs */
    private GraphicsOutputDevice surface;

    /** The last X position of the mouse on the surface */
    private int lastMouseX;

    /** The last Y position of the mouse on the surface */
    private int lastMouseY;

    /*
     * Construct a new instance of the tracker that interacts with the given
     * surface representaiton.
     *
     * @param surface The surface instance to track on. Must be non-null
     * @param id A unique identification string for this tracker instance
     */
    public MouseTracker(GraphicsOutputDevice surface, String id)
    {
        this.surface = surface;
        deviceId = id;

        mousePickDirection = new Vector3d();
        mouseEyePosition = new Point3d();
        mousePosition = new Point3d();

        surfaceTransform = new Matrix4d();

        currentIndex = 0;
        lastIndex = 1;

        tstate = new TrackerState[2];

        tstate[0] = new TrackerState();
        tstate[0].numButtons = 3;

        tstate[0].buttonMode[0] = ButtonModeConstants.NAV1;
        tstate[0].pickingEnabled[0] = true;
        tstate[0].buttonMode[1] = ButtonModeConstants.NAV1;
        tstate[0].pickingEnabled[1] = false;
        tstate[0].buttonMode[2] = ButtonModeConstants.NAV2;
        tstate[0].pickingEnabled[2] = false;

        tstate[1] = new TrackerState();
        tstate[1].numButtons = 3;

        tstate[1].buttonMode[0] = ButtonModeConstants.NAV1;
        tstate[1].pickingEnabled[0] = true;
        tstate[1].buttonMode[1] = ButtonModeConstants.NAV1;
        tstate[1].pickingEnabled[1] = false;
        tstate[1].buttonMode[2] = ButtonModeConstants.NAV2;
        tstate[1].pickingEnabled[2] = false;
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
    @Override
    public int getActionMask()
    {
        return mask;
    }

    /**
     * Notification that tracker polling is beginning.
     */
    @Override
    public void beginPolling()
    {
        if (currentIndex == 0)
        {
            currentIndex = 1;
            lastIndex = 0;
        }
        else
        {
            currentIndex = 0;
            lastIndex = 1;
        }
    }

    /**
     * Notification that tracker polling is ending.
     */
    @Override
    public void endPolling() {
        if (tstate[currentIndex].actionType != TrackerState.TYPE_NONE)
		{
            // got something in between
            tstate[lastIndex].actionType = TrackerState.TYPE_NONE;
        }
        else
        {
            tstate[currentIndex].actionType = TrackerState.TYPE_NONE;
            tstate[lastIndex].actionType = TrackerState.TYPE_NONE;
        }
    }

    /**
     * Get the current state of this tracker.
     *
     * @param state The current state
     * @param layer The ID of the layer to get the state for
     * @param subLayer The ID of the sub layer within the parent layer
     */
    @Override
    public void getState(int layer, int subLayer, TrackerState state)
    {
        state.actionMask = mask;
        state.actionType = tstate[lastIndex].actionType;
        state.devicePos[0] = tstate[lastIndex].devicePos[0];
        state.devicePos[1] = tstate[lastIndex].devicePos[1];
        state.devicePos[2] = tstate[lastIndex].devicePos[2];

        // No orientation for the mouse in local coords
        state.deviceOri[0] = 0;
        state.deviceOri[1] = 0;
        state.deviceOri[2] = 0;

        transformMouse(layer, subLayer, state);

        state.numButtons = tstate[lastIndex].numButtons;
        for(int i=0; i < tstate[lastIndex].numButtons; i++)
        {
            state.buttonMode[i] = tstate[lastIndex].buttonMode[i];
            state.buttonState[i] = tstate[lastIndex].buttonState[i];
            state.pickingEnabled[i] = tstate[lastIndex].pickingEnabled[i];
        }

        state.wheelClicks = tstate[lastIndex].wheelClicks;

        state.shiftModifier = tstate[lastIndex].shiftModifier;
        state.altModifier = tstate[lastIndex].altModifier;
        state.ctrlModifier = tstate[lastIndex].ctrlModifier;
    }

    //------------------------------------------------------------------------
    // Methods defined by MouseListener
    //------------------------------------------------------------------------

    /**
     * Process a mouse press event.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void mousePressed(MouseEvent evt)
    {
        int mods = evt.getModifiersEx();

        if ((mods & MouseEvent.BUTTON1_DOWN_MASK) ==
            MouseEvent.BUTTON1_DOWN_MASK)
            tstate[currentIndex].buttonState[0] = true;
        else
            tstate[currentIndex].buttonState[0] = false;

        if ((mods & MouseEvent.BUTTON2_DOWN_MASK) ==
            MouseEvent.BUTTON2_DOWN_MASK)
            tstate[currentIndex].buttonState[1] = true;
        else
            tstate[currentIndex].buttonState[1] = false;

        if ((mods & MouseEvent.BUTTON3_DOWN_MASK) ==
            MouseEvent.BUTTON3_DOWN_MASK)
            tstate[currentIndex].buttonState[2] = true;
        else
            tstate[currentIndex].buttonState[2] = false;

        updateMouseDetails(evt);

        tstate[currentIndex].actionType = TrackerState.TYPE_PRESS;

        tstate[currentIndex].shiftModifier = evt.isShiftDown();
        tstate[currentIndex].altModifier = evt.isAltDown();
        tstate[currentIndex].ctrlModifier = evt.isControlDown();
    }

    /**
     * Process a mouse release event.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void mouseReleased(MouseEvent evt)
    {
        updateMouseDetails(evt);

        if (tstate[currentIndex].actionType == TrackerState.TYPE_PRESS)
            tstate[currentIndex].actionType = TrackerState.TYPE_CLICK;
        else
            tstate[currentIndex].actionType = TrackerState.TYPE_RELEASE;

        tstate[currentIndex].shiftModifier = evt.isShiftDown();
        tstate[currentIndex].altModifier = evt.isAltDown();
        tstate[currentIndex].ctrlModifier = evt.isControlDown();
    }

    /**
     * Process a mouse click event.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void mouseClicked(MouseEvent evt)
    {
        tstate[currentIndex].shiftModifier = evt.isShiftDown();
        tstate[currentIndex].altModifier = evt.isAltDown();
        tstate[currentIndex].ctrlModifier = evt.isControlDown();
    }

    /**
     * Process a mouse enter event.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void mouseEntered(MouseEvent evt)
    {
        tstate[currentIndex].shiftModifier = evt.isShiftDown();
        tstate[currentIndex].altModifier = evt.isAltDown();
        tstate[currentIndex].ctrlModifier = evt.isControlDown();
    }

    /**
     * Process a mouse exited event.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void mouseExited(MouseEvent evt)
    {
        tstate[currentIndex].shiftModifier = evt.isShiftDown();
        tstate[currentIndex].altModifier = evt.isAltDown();
        tstate[currentIndex].ctrlModifier = evt.isControlDown();
    }

    //------------------------------------------------------------------------
    // Methods defined by MouseMotionListener
    //------------------------------------------------------------------------

    /**
     * Process a mouse drag event
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void mouseDragged(MouseEvent evt)
    {
        if (tstate[currentIndex].actionType == TrackerState.TYPE_NONE ||
            tstate[currentIndex].actionType == TrackerState.TYPE_DRAG)
        {
            updateMouseDetails(evt);
            tstate[currentIndex].actionType = TrackerState.TYPE_DRAG;
        }

        tstate[currentIndex].shiftModifier = evt.isShiftDown();
        tstate[currentIndex].altModifier = evt.isAltDown();
        tstate[currentIndex].ctrlModifier = evt.isControlDown();
    }

    /**
     * Process a mouse movement event.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void mouseMoved(MouseEvent evt)
    {
        if (tstate[currentIndex].actionType == TrackerState.TYPE_NONE ||
            tstate[currentIndex].actionType == TrackerState.TYPE_MOVE)
        {
            updateMouseDetails(evt);

            tstate[currentIndex].actionType = TrackerState.TYPE_MOVE;
        }

        tstate[currentIndex].shiftModifier = evt.isShiftDown();
        tstate[currentIndex].altModifier = evt.isAltDown();
        tstate[currentIndex].ctrlModifier = evt.isControlDown();
    }

    //------------------------------------------------------------------------
    // Method defined by MouseWheelListener
    //------------------------------------------------------------------------

    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe)
    {
        tstate[currentIndex].wheelClicks = mwe.getWheelRotation( );
        tstate[currentIndex].actionType = TrackerState.TYPE_WHEEL;

        tstate[currentIndex].shiftModifier = mwe.isShiftDown();
        tstate[currentIndex].altModifier = mwe.isAltDown();
        tstate[currentIndex].ctrlModifier = mwe.isControlDown();
    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------

    /**
     * Copy the mouse state from the given event into our local variables for
     * later processing.
     *
     * @param evt The mouse event
     */
    private void updateMouseDetails(MouseEvent evt)
    {
        Component comp = (Component)evt.getSource();
        float height = comp.getHeight();
        float width = comp.getWidth();

        lastMouseX = evt.getX();
        lastMouseY = evt.getY();

        tstate[currentIndex].devicePos[0] = lastMouseX / width;
        tstate[currentIndex].devicePos[1] = lastMouseY / height;
        tstate[currentIndex].devicePos[2] = 0;

        // Put into Aviatrix3D semantics
        lastMouseY = (int) (height - lastMouseY);
    }

    /**
     * Generate the mouse pick position and shape in the world coordinates
     * based on the last recorded screen position. The results of the position
     * and orientation will be calculated and put into the given state object.
     * The results of the position and orientation calcs will be left in
     * mouseEyePosition and mousePickDirection.
     *
     * @param layer The ID of the layer to get the state for
     * @param subLayer The ID of the sub layer within the parent layer
     * @param state The state object to copy the calculations into
     */
    private void transformMouse(int layer, int subLayer, TrackerState state)
    {
        boolean valid = true;

        boolean is_drag = (tstate[lastIndex].actionType == TrackerState.TYPE_DRAG);

        if (!surface.getSurfaceToVWorld(lastMouseX,
            lastMouseY,
            layer,
            subLayer,
            surfaceTransform,
            deviceId,
            is_drag))
        {
            valid = false;
        }

        if (!surface.getCenterEyeInSurface(lastMouseX,
            lastMouseY,
            layer,
            subLayer,
            mouseEyePosition,
            deviceId,
            is_drag))
        {
            valid = false;
        }

        if (!surface.getPixelLocationInSurface(lastMouseX,
            lastMouseY,
            layer,
            subLayer,
            mousePosition,
            deviceId,
            is_drag))
        {
            valid = false;
        }

        if (!valid)
        {
            state.actionMask = TrackerState.TYPE_NONE;
            return;
        }

        mousePickDirection.sub(mousePosition, mouseEyePosition);
        surfaceTransform.transform(mousePosition, mousePosition);
        surfaceTransform.transform(mousePickDirection, mousePickDirection);

        state.worldPos[0] = (float)mousePosition.x;
        state.worldPos[1] = (float)mousePosition.y;
        state.worldPos[2] = (float)mousePosition.z;

        state.worldOri[0] = (float)mousePickDirection.x;
        state.worldOri[1] = (float)mousePickDirection.y;
        state.worldOri[2] = (float)mousePickDirection.z;
    }
}
