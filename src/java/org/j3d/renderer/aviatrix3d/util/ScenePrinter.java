/*****************************************************************************
 *                           J3D.org Copyright (c) 2005 - 2006
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.util;

// External imports
import javax.vecmath.Matrix4f;

// Local imports
import org.j3d.aviatrix3d.*;

import org.j3d.aviatrix3d.rendering.BoundingVolume;
import org.j3d.aviatrix3d.picking.PickableObject;

/**
 * Utility class that allows printing of a scene graph, with options to control
 * what is printed.
 * <p>
 *
 * By default, the printer only shows the basic graph structure. Options all
 * you to print out extra information. Fully qualified class names are printed
 * out, but the option exists to just use the class name only.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class ScenePrinter
    implements SceneGraphTraversalObserver
{
    /** The traverser used to go through the scene graph */
    private SceneGraphTraverser sgt;

    /** Flag controlling printing bounding boxes */
    private boolean printBounds;

    /** Flag controlling printing pick mask flags */
    private boolean printPickMask;

    /** Flag controlling printing number of children of groups */
    private boolean printChildCount;

    /** Flag for printing of vertical dashes rather than spaces for indents */
    private boolean printDashes;

    /** Flag for printing of the node's hashcode. */
    private boolean printHashCode;

    /** Flag to say if printing short or long class names */
    private boolean useShortNames;

    /** Flag to say if printing a matrix from TransformGroups or not */
    private boolean printMatrix;

    /** Matrix for fetching the transform info */
    private Matrix4f matrix;

    /**
     * Create a new printer with all flags turned off.
     */
    public ScenePrinter()
    {
        sgt = new SceneGraphTraverser();
        sgt.setObserver(this);

        printPickMask = false;
        printBounds = false;
        printChildCount = false;
        printDashes = false;
        useShortNames = false;
        printHashCode = false;
        printMatrix = false;
    }

    //---------------------------------------------------------------
    // Methods defined by SceneGraphTraversalObserver
    //---------------------------------------------------------------

    /**
     * Notification of a scene graph object that has been traversed in the
     * scene.
     *
     * @param parent The parent node of this node
     * @param child The child node that is being observed
     * @param shared true if the object reference has already been traversed
     *    and this is beyond the first reference
     * @param depth The depth of traversal from the top of the tree.  Starts at 0 for top.
     */
    public void observedNode(SceneGraphObject parent,
                             SceneGraphObject child,
                             boolean shared,
                             int depth)
    {
        if(parent == null)
            System.out.println("ROOT");

        indent(depth, true);

        if(child == null)
        {
            System.out.println("NULL child");
        }
        else
        {
            String name = child.getClass().getName();

            if(useShortNames)
            {
                int pos = name.lastIndexOf(".");
                name = name.substring(pos + 1);
            }

            System.out.print(name);

            if(printHashCode)
            {
                System.out.print(" 0x");
                System.out.print(Integer.toHexString(child.hashCode()));
            }

            if(printPickMask && (child instanceof PickableObject))
            {
                System.out.print(" 0x");
                System.out.print(Integer.toHexString(((PickableObject)child).getPickMask()));
            }

            if(printChildCount && (child instanceof Group))
            {
                Group grp = (Group)child;
                System.out.print(" childCount: " + grp.numChildren());
            }

            if(printBounds && child instanceof Node)
            {
                Node node = (Node)child;
                BoundingVolume bounds = node.getBounds();

                if(bounds instanceof BoundingVoid)
                    System.out.print(" Bounds: VOID");
                else
                    System.out.print(" Bounds: " + bounds);
            }


            if(shared)
                System.out.println(" (copy) ");
            else
                System.out.println();

            if(printMatrix && (child instanceof TransformGroup))
                printMatrix((TransformGroup)child, depth);
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Print the scene graph from the given root node.
     *
     * @param root The root of the scene graph to print from
     */
    public void dumpGraph(SceneGraphObject root)
    {
        sgt.traverseGraph(root);
    }

    /**
     * Set the flag for printing the bounds information. If this is on, the
     * bounds will be printed along with the node class name.
     *
     * @param state true to enable printing out of bounding box information
     */
    public void enableBoundsPrinting(boolean state)
    {
        printBounds = state;
    }

    /**
     * Check to see the current state of the bounds printing flag.
     *
     * @return true if bounds printing is enabled
     */
    public boolean isBoundsPrintingEnabled()
    {
        return printBounds;
    }

    /**
     * Set the flag for printing the hash code information. If this is on, the
     * hash code will be printed along with the node class name.
     *
     * @param state true to enable printing out of bounding box information
     */
    public void enableHashCodePrinting(boolean state)
    {
        printHashCode = state;
    }

    /**
     * Check to see the current state of the hash code printing flag.
     *
     * @return true if hash code printing is enabled
     */
    public boolean isHashCodePrintingEnabled()
    {
        return printHashCode;
    }

    /**
     * Set the flag for printing the pick mask information. If this is on, the
     * pick mask will be printed along with the node class name. The mask will
     * be printed in Hexidecimal notation.
     *
     * @param state true to enable printing out of the pick mask
     */
    public void enablePickMaskPrinting(boolean state)
    {
        printPickMask = state;
    }

    /**
     * Check to see the current state of the pick mask printing flag.
     *
     * @return true if pick mask printing is enabled
     */
    public boolean isPickMaskPrintingEnabled()
    {
        return printPickMask;
    }

    /**
     * Set the flag for printing the child count of groups. If this is on, the
     * number of children a group contains will be printed along with the node
     * class name.
     *
     * @param state true to enable printing out the number of children
     */
    public void enableChildCountPrinting(boolean state)
    {
        printChildCount = state;
    }

    /**
     * Check to see the current state of the child count printing flag.
     *
     * @return true if child count printing is enabled
     */
    public boolean isChildCountPrintingEnabled()
    {
        return printChildCount;
    }

    /**
     * Set the flag for the alternate indenting scheme, that uses dashes
     * instead of spaces for each level of intent.
     *
     * @param state true to enable printing dashes rather than spaces
     */
    public void enableDashPrinting(boolean state)
    {
        printDashes = state;
    }

    /**
     * Check to see the current state of the alternate indent printing flag.
     *
     * @return true if dash printing is enabled
     */
    public boolean isDashPrintingEnabled()
    {
        return printDashes;
    }

    /**
     * Set the flag for printing out just the class name rather than the
     * fully-qualified class name.
     *
     * @param state true to enable printing dashes rather than spaces
     */
    public void printShortNames(boolean state)
    {
        useShortNames = state;
    }

    /**
     * Check to see the current state of the alternate indent printing flag.
     *
     * @return true if dash printing is enabled
     */
    public boolean isShortNamesEnabled()
    {
        return useShortNames;
    }

    /**
     * Set the flag for printing out the transformation matrix of any
     * encountered transform groups.
     *
     * @param state true to enable printing out of matrix information
     */
    public void enableTransformMatrixPrinting(boolean state)
    {
        printMatrix = state;

        if(state)
            matrix = new Matrix4f();
    }

    /**
     * Check to see the current state of the transform matrix printing flag.
     *
     * @return true if matrix printing is enabled
     */
    public boolean isTransformMatrixPrintingEnabled()
    {
        return printMatrix;
    }

    /**
     * Internal convenience method to print out a matrix from the transform
     * group and have it be correctly indented.
     *
     * @param tg The group to source the matrix from
     * @param depth The indenting depth to use
     */
    private void printMatrix(TransformGroup tg, int depth)
    {
        tg.getTransform(matrix);

        // Indents the normal depth and then adds 2 spaces for the
        // transform depth.
        indent(depth, false);
        System.out.print("| [");
        System.out.print(matrix.m00);
        System.out.print(' ');
        System.out.print(matrix.m01);
        System.out.print(' ');
        System.out.print(matrix.m02);
        System.out.print(' ');
        System.out.print(matrix.m03);
        System.out.println(" ]");

        indent(depth, false);
        System.out.print("| [");
        System.out.print(matrix.m10);
        System.out.print(' ');
        System.out.print(matrix.m11);
        System.out.print(' ');
        System.out.print(matrix.m12);
        System.out.print(' ');
        System.out.print(matrix.m13);
        System.out.println(" ]");

        indent(depth, false);
        System.out.print("| [");
        System.out.print(matrix.m20);
        System.out.print(' ');
        System.out.print(matrix.m21);
        System.out.print(' ');
        System.out.print(matrix.m22);
        System.out.print(' ');
        System.out.print(matrix.m23);
        System.out.println(" ]");

        indent(depth, false);
        System.out.print("| [");
        System.out.print(matrix.m30);
        System.out.print(' ');
        System.out.print(matrix.m31);
        System.out.print(' ');
        System.out.print(matrix.m32);
        System.out.print(' ');
        System.out.print(matrix.m33);
        System.out.println(" ]");
    }

    /**
     * Print the indent for a line to the given depth.
     *
     * @param depth The number of spaces to indent
     * @param label true to include a branch label, false for whitespace
     */
    private void indent(int depth, boolean label)
    {
        if(printDashes)
        {
            for(int i = 0; i < depth; i++)
                System.out.print("|");

            if(label)
                System.out.print("+");
            else
                System.out.print('|');
        }
        else
        {
            for(int i = 0; i < depth + 1; i++)
                System.out.print(' ');
        }
    }
}
