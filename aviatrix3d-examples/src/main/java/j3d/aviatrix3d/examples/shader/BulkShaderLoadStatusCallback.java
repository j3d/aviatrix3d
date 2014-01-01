
// External imports
import java.util.ArrayList;

// Local imports
import org.j3d.aviatrix3d.*;

/**
 * Simple test helper for getting the status of a shader loading
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class BulkShaderLoadStatusCallback
    implements ApplicationUpdateObserver
{
    /** Frame counter so we know when to print out shader debug */
    private int frameCount;

    /** list of shader holders to look at */
    private ArrayList<ShaderHolder> shaderList;

    /**
     *
     */
    public BulkShaderLoadStatusCallback()
    {
        shaderList = new ArrayList<ShaderHolder>();
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph()
    {
        if(frameCount == 4)
        {
            for(int i =  0; i < shaderList.size(); i++)
            {
                ShaderHolder h = shaderList.get(i);

                System.out.println(h.title);
                System.out.println("vert log " + h.vertShader.getLastInfoLog());
                System.out.println("frag log " + h.fragShader.getLastInfoLog());
                System.out.println("link log " + h.completeShader.getLastInfoLog());
            }

            frameCount++;
        }
        else if(frameCount < 4)
            frameCount++;
    }

    /**
     * Notification that the AV3D internal shutdown handler has detected a
     * system-wide shutdown. The aviatrix code has already terminated rendering
     * at the point this method is called, only the user's system code needs to
     * terminate before exiting here.
     */
    public void appShutdown()
    {
        // do nothing
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Add a new shader for watching, to the list
     */
    public void addShader(String title,
                          ShaderObject vert,
                          ShaderObject frag,
                          ShaderProgram comp)
    {

        ShaderHolder h = new ShaderHolder();

        h.title = title;
        h.vertShader = vert;
        h.fragShader = frag;
        h.completeShader = comp;

        shaderList.add(h);
    }
}
