package j3d.aviatrix3d.examples.shader;

// External imports
// None

// Local imports
import org.j3d.aviatrix3d.ShaderObject;
import org.j3d.aviatrix3d.ShaderProgram;

/**
 * Data holder class for dealing with shaders.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class ShaderHolder
{
    String title;

    /** Vert shader to print the results from */
    ShaderObject vertShader;

    /** Frag shader to print the results from */
    ShaderObject fragShader;

    /** Compiled shader program to print the results from */
    ShaderProgram completeShader;
}
