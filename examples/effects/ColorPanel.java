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

// Standard imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextField;


// Application Specific imports
// none

/**
 * A simple panel for getting colour values
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ColorPanel extends Panel
{
    private TextField redTf;
    private TextField greenTf;
    private TextField blueTf;

    /**
     * Create a new panel using the basic colour
     */
    public ColorPanel(int r, int g, int b)
    {
        super(new GridLayout(1, 3));

        redTf = new TextField(Integer.toString(r), 3);
        greenTf = new TextField(Integer.toString(g), 3);
        blueTf = new TextField(Integer.toString(b), 3);

        add(redTf);
        add(greenTf);
        add(blueTf);
    }

    public Color getColor()
    {
        int r = Integer.parseInt(redTf.getText());
        int g = Integer.parseInt(greenTf.getText());
        int b = Integer.parseInt(blueTf.getText());

        return new Color(r, g, b);
    }
}
