
// External imports
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import javax.imageio.ImageIO;
import javax.swing.*;

// Local imports
import org.j3d.renderer.aviatrix3d.texture.TextureCreateUtils;

/**
 * Example application that demonstrates how to use the loader interface
 * to load a file into the scene graph.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class NormalMapDemo extends JFrame
    implements ActionListener
{
    private JFileChooser openDialog;

    /** Renderer for the basic image */
    private ImageIcon srcIcon;

    private JLabel srcLabel;

    /** Renderer for the normal map version */
    private ImageIcon mapIcon;

    private JLabel mapLabel;

    /** Utility for munging textures to power of 2 size */
    private TextureCreateUtils textureUtils;

    public NormalMapDemo()
    {
        super("Normal map conversion demo");

        setSize(1280, 1024);
        setLocation(0, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textureUtils = new TextureCreateUtils();

        JPanel p1 = new JPanel(new BorderLayout());

        srcIcon = new ImageIcon();
        srcLabel = new JLabel();
        srcLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        srcLabel.setText("Source Image");

        mapIcon = new ImageIcon();
        mapLabel = new JLabel();
        mapLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        mapLabel.setText("NormalMap Image");

        JButton b = new JButton("Open A file");
        b.addActionListener(this);

        p1.add(b, BorderLayout.SOUTH);
        p1.add(srcLabel, BorderLayout.WEST);
        p1.add(mapLabel, BorderLayout.EAST);

        getContentPane().add(p1);
    }

    //---------------------------------------------------------------
    // Methods defined by WindowListener
    //---------------------------------------------------------------

    /**
     * Process the action event from the open button
     */
    public void actionPerformed(ActionEvent evt)
    {
        if(openDialog == null)
            openDialog = new JFileChooser();

        int ret_val = openDialog.showOpenDialog(this);
        if(ret_val != JFileChooser.APPROVE_OPTION)
            return;

        File file = openDialog.getSelectedFile();

        try
        {
            System.out.println("Loading external file: " + file);

            FileInputStream is = new FileInputStream(file);
            BufferedInputStream stream = new BufferedInputStream(is);
            BufferedImage img = ImageIO.read(stream);

            if(img == null)
            {
                System.out.println("Image load barfed");
                return;
            }

            srcIcon.setImage(img);
            srcLabel.setIcon(srcIcon);

            BufferedImage map_img = textureUtils.createNormalMap(img, null);
            mapIcon.setImage(map_img);
            mapLabel.setIcon(mapIcon);
        }
        catch(IOException ioe)
        {
            System.out.println("crashed " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    public static void main(String[] args)
    {
        NormalMapDemo demo = new NormalMapDemo();
        demo.setVisible(true);
    }
}
