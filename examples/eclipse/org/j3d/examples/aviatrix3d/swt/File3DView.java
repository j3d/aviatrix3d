package org.j3d.examples.aviatrix3d.swt;

// External imports
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.part.ViewPart;

// Local imports
// None

/**
 * An Eclipse view that shows a single {@link 3D file} instance.
 * <p>
 *
 * This shows the content of a file in a 3D window, rotating it. It is mostly
 * based from the loader example, but adds eclipse plugin handling to the
 * process to get the initial window to open.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class File3DView extends ViewPart
{
    /** The tree viewer used by this view */
    private ModelViewCanvas modelViewer;

    /**
     * Default public constructor.
     */
    public File3DView()
    {
    }

    //----------------------------------------------------------
    // Methods defined by ViewPart
    //----------------------------------------------------------

    /**
     * Create the contents of this tree view.
     */
    public void createPartControl(Composite parent)
    {
        modelViewer = new ModelViewCanvas(parent);
    }

    /**
     * Set the focus to this view and perform any view-specific initialisation.
     */
    public void setFocus()
    {
        modelViewer.getCanvas().setFocus();
    }

    /**
     * Dispose of the viewer completely
     */
    public void dispose()
    {
        modelViewer.shutdown();
        modelViewer.getCanvas().dispose();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the canvas wrapper we're using.
     */
    public ModelViewCanvas getModeller()
    {
        return modelViewer;
    }
}
