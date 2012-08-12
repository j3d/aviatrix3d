/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.navigation;

// External imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import javax.vecmath.*;

// Local imports
import org.j3d.util.MatrixUtils;

import org.j3d.aviatrix3d.TransformGroup;
import org.j3d.aviatrix3d.Viewpoint;

/**
 * This class will create smooth transitions from one viewpoint to another.
 * <p>
 *
 * The transition effects start when a new set of transform groups are used to
 * drive the view. To provide the driving factor, a Swing Timer object is used
 * rather than using the Java 3D behaviour system.
 *
 * @author <a href="http://www.ife.no/vr/">Halden VR Centre, Institute for Energy Technology</a><br>
 *   Updated for j3d.org by Justin Couch
 * @version $Revision $
 */
public class ViewpointTransition implements ActionListener
{
    /** The transform group above the view that is being moved each frame */
    private TransformGroup viewTg;

    /** A timer that drives our updates to the screen */
    private Timer timer;

    /** The time that this current transition is to take in milliseconds */
    private int totalTimeMS;

    /** The end time, in epoch coordinates, of the end of the transition */
    private long epochEndTime;

    /** Working calculation about how far along the transition we are */
    private float alfa;

    /** A delay between callbacks from the time for frames */
    private int  timerDelay = 0;

    /** Handler for doing lookAt calcs */
    private MatrixUtils matrixUtils;

    // The following are working variables that we want to create once and use
    // all the time. This saves on GC costs and speeds up the process quite
    // substantially.
    // The names are
    // <var> for the current working copy
    // <var>1 for the value at the start of transition
    // <var>2 for the value at the end of the transition

    private Point3f eye  = new Point3f();
    private Point3f eye1 = new Point3f();
    private Point3f eye2 = new Point3f();
    private Point3f center  = new Point3f();
    private Point3f center1 = new Point3f();
    private Point3f center2 = new Point3f();
    private Vector3f up  = new Vector3f();
    private Vector3f up1 = new Vector3f();
    private Vector3f up2 = new Vector3f();
    private Vector3f location1 = new Vector3f();
    private Vector3f location2 = new Vector3f();
    private Vector3f direction1 = new Vector3f();
    private Vector3f direction2 = new Vector3f();
    private Matrix4f previousFrameTx  =   new Matrix4f();
    private Matrix4f currentTx = new Matrix4f();
    private Matrix4f destinationTx = new Matrix4f();

    /** An observer for information about updates for this transition */
    private FrameUpdateListener updateListener;

    /**
     * Construct a new transition object ready to work.
     */
    public ViewpointTransition()
    {
        timer = new Timer(100, this);
        timer.setInitialDelay(0);
        timer.setRepeats(true);
        timer.setLogTimers(false);
        timer.setCoalesce(true);
        timer.stop();

        matrixUtils = new MatrixUtils();
    }

    /**
     * Set the listener for frame update notifications. By setting a value of
     * null it will clear the currently set instance
     *
     * @param l The listener to use for this transition
     */
    public void setFrameUpdateListener(FrameUpdateListener l)
    {
        updateListener = l;
    }

    /**
     * Transition between two locations represented by the initial
     * TranformGroup and the destination transform information starting
     * immediately.
     *
     * @param viewTg is the transformgroup to be transitioned that holds
     *    the view.
     * @param endTx is the final state to be transitioned to
     * @param totalTime The time to be spent with this transition
     *    (in miliseconds)
     */
    public void transitionTo(TransformGroup viewTg,
                             Matrix4f endTx,
                             int totalTime)
    {
        this.viewTg = viewTg;
        destinationTx = new Matrix4f(endTx);
        totalTimeMS = totalTime;

        epochEndTime = System.currentTimeMillis() + totalTime;
        timer.start();

        // Set up our internal transforms that we will be doing the morphing
        // along.
        viewTg.getTransform(currentTx);
        currentTx.get(location1);
        eye1.set(location1);
        direction1.set(0,0,-1);
        currentTx.transform(direction1);
        center1.add(eye1,direction1);
        up1.set(0,1,0);
        currentTx.transform(up1);

        // Make sure the destination transform is set up for the eye position
        destinationTx.get(location2);
        eye2.set(location2);
        direction2.set(0,0,-1);
        destinationTx.transform(direction2);
        center2.add(eye2,direction2);
        up2.set(0,1,0);
        destinationTx.transform(up2);
    }

    /**
     * Process an action event from the timer. This event is only for the time
     * and should not be associated with any other sort of action event like
     * menu callbacks.
     *
     * @param evt The event that caused this action to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        viewTg.getTransform(previousFrameTx);

        // If not equal, then someone was changing it so there is no point
        // in further transition.
        if(currentTx.equals(previousFrameTx))
        {
            // Hmmm. Magic number. No idea what the value 10 is for.
//            timerDelay = (int)10 + view.getLastFrameDuration() * 0.5f);
//            timer.setDelay(timerDelay);
            timer.setDelay(33);

            // How far into the transitiion are we?
            alfa= 1 - ((float)(epochEndTime - System.currentTimeMillis()) /
                       totalTimeMS);

            if(alfa > 1)
                alfa = 1;

            // Build the interpolated UVN camera coordinates
            eye.interpolate(eye1, eye2, alfa);
            center.interpolate(center1, center2, alfa);
            up.interpolate(up1, up2, alfa);

            // Setup the current transform position. Always normalise
            // otherwise it will grow non-congurent.
            matrixUtils.lookAt(eye, center, up, currentTx);
            currentTx.invert();

            try
            {
                viewTg.setTransform(currentTx);
            }
            catch(Exception e)
            {
                // If the set has screwed up then just set the value to the
                // final value and terminate the transition.
// System.out.println("Transition stopping due to invalid value");
                currentTx.set(destinationTx);
                viewTg.setTransform(currentTx);
                alfa = 9;
                //e.printStackTrace();
            }

            if(updateListener!=null)
                updateListener.viewerPositionUpdated(currentTx);
        }
        else
		{
            alfa=9;
		}

        // Should we now stop if the change is greater than the end of the
        // timescale provided.
        if(alfa >= 1)
        {
            timer.stop();

            if(updateListener != null)
                updateListener.transitionEnded(currentTx);
        }
    }
}
