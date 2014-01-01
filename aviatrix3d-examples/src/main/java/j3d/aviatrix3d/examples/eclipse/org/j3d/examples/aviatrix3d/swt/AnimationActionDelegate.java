package j3d.aviatrix3d.examples.eclipse.org.j3d.examples.aviatrix3d.swt;

// External imports
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

// Local imports
// None

/**
 * ActionDelegate specifically for controlling the animation.
 * <p>
 *
 * Lots of simple actions for starting, stopped etc the 3D animation.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class AnimationActionDelegate implements IViewActionDelegate
{
    /** The tree viewer used by this view */
    private ModelViewCanvas modelViewer;

    /** The current directory we load models from */
    private String currentDir;

    /**
     * Default public constructor.
     */
    public AnimationActionDelegate()
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
        modelViewer = ((File3DView)viewer).getModeller();
    }

    /**
     * Process an action now.
     */
    public void run(IAction action)
    {
        String id = action.getId();

        if(id.equals("toolbar.start"))
        {
            modelViewer.startAnimation();
        }
        else if(id.equals("toolbar.stop"))
        {
            modelViewer.stopAnimation();
        }
    }

    public void selectionChanged(IAction action, ISelection selection)
    {
        // do nothing
    }
}
