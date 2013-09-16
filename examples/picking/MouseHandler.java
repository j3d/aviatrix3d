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

import javax.vecmath.Vector3f;
import javax.vecmath.Point3f;
import javax.vecmath.Matrix4f;

import org.j3d.device.input.Tracker;
import org.j3d.device.input.TrackerState;
import org.j3d.device.input.ButtonModeConstants;

// Local imports
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

/**
 * A tracker implementation for mouse devices under OpenGL.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class MouseHandler
    implements ApplicationUpdateObserver,
               NodeUpdateListener
               MouseListener,
               MouseMotionListener
{
    private static final float[] GREEN = { 0, 1, 0 };
    private static final float[] BLUE = { 0, 0, 1 };

    /** The current buffer idx for new double buffered values */
    private int currentIndex;

    /** The last index for reading */
    private int lastIndex;

    /** Representation of the direction that we pick in */
    private Vector3f mousePickDirection;

    /** Representation of the eye in the world */
    private Point3f mouseEyePosition;

    /** The eye position in the image plate (canvas) world coords */
    private Point3f mousePosition;

    /** The matrix to read the view VWorld coordinates */
    private Matrix4f surfaceTransform;

    /** The surface to perform 2D to 3D calcs */
    private GraphicsOutputDevice surface;

    /** The last X position of the mouse on the surface */
    private int lastMouseX;

    /** The last Y position of the mouse on the surface */
    private int lastMouseY;

    /** What we are going to be doing the pick with */
    private PickRequest pointPick1;

    /** The group used to pick against for the mouse */
    private TransformGroup pickRoot;

    /** The material object we'll change colour when a pick intersection has been found */
    private Material material;

    /*
     * Construct a new instance of the tracker that interacts with the given
     * surface representaiton.
     *
     * @param surface The surface instance to track on. Must be non-null
     */
    MouseHandler(GraphicsOutputDevice surface, TransformGroup root, Material colouredObject)
    {
        this.surface = surface;

        pickRoot = root;
        material = colouredObject;

        mousePickDirection = new Vector3f();
        mouseEyePosition = new Point3f();
        mousePosition = new Point3f();

        surfaceTransform = new Matrix4f();

        currentIndex = 0;
        lastIndex = 1;

        pointPick1 = new PickRequest();
        pointPick1.pickGeometryType = PickRequest.PICK_RAY;
        pointPick1.pickSortType = PickRequest.SORT_ALL;
        pointPick1.pickType = PickRequest.FIND_ALL;
        pointPick1.generateVWorldMatrix = false;
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    @Override
    public void updateSceneGraph()
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

        int old_count = pointPick1.pickCount;

        transform.boundsChanged(this);
        pickRoot.pickSingle(pointPick1);

        if(old_count != pointPick1.pickCount)
            material.dataChanged(this);
    }

    /**
     * Notification that the AV3D internal shutdown handler has detected a
     * system-wide shutdown. The aviatrix code has already terminated rendering
     * at the point this method is called, only the user's system code needs to
     * terminate before exiting here.
     */
    @Override
    public void appShutdown()
    {
        // do nothing
    }

    //----------------------------------------------------------
    // Methods required by the UpdateListener interface.
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src)
    {
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src)
    {
        if(pointPick1.pickCount != 0)
            material.setEmissiveColor(GREEN);
        else
            material.setEmissiveColor(BLUE);
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
        surfaceTransform.transform(mousePosition);
        surfaceTransform.transform(mousePickDirection);

        state.worldPos[0] = (float)mousePosition.x;
        state.worldPos[1] = (float)mousePosition.y;
        state.worldPos[2] = (float)mousePosition.z;

        state.worldOri[0] = (float)mousePickDirection.x;
        state.worldOri[1] = (float)mousePickDirection.y;
        state.worldOri[2] = (float)mousePickDirection.z;
    }
}
