package j3d.aviatrix3d.examples.eclipse.org.j3d.examples.aviatrix3d.swt;

// External imports
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

// Local imports
// None

/**
 * ActionDelegate specifically for loading the file.
 * <p>
 *
 * File loading may take quite some time, so this delegate offloads the
 * processing to a worker thread that eventually shows up on the
 * canvas.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class Load3DActionDelegate implements IViewActionDelegate
{
    /** The view we belong to */
    private IViewPart viewer;

    /** The model viewer used by this view */
    private ModelViewCanvas modelViewer;

    /** The current directory we load models from */
    private String currentDir;

    /**
     * Default public constructor.
     */
    public Load3DActionDelegate()
    {
    }

    //----------------------------------------------------------
    // Methods defined by IViewActionDelegate
    //----------------------------------------------------------

    /**
     * Initialise the delegate to handle this view instance.
     */
    public void init(IViewPart viewer)
    {
        this.viewer = viewer;
        modelViewer = ((File3DView)viewer).getModeller();
    }

    /**
     * Process an action now.
     */
    public void run(IAction action)
    {
        FileDialog dialog = new FileDialog(viewer.getSite().getShell(), SWT.OPEN);
        dialog.setText("Open image file");
        dialog.setFilterPath(currentDir);
        dialog.setFilterExtensions(new String[] { "*.dem; *.3ds; *.stl; *.bt" } );
        dialog.setFilterNames(new String[] { "3D Model" + " (dem, 3Ds, STL, VTerrain)" });

        String filename = dialog.open();
        if (filename != null)
        {
            modelViewer.load(filename);
            currentDir = dialog.getFilterPath();
        }
    }

    public void selectionChanged(IAction action, ISelection selection)
    {
        // do nothing
    }
}
