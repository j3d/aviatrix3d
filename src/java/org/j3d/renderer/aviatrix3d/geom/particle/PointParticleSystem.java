/*****************************************************************************
 *                        Copyright (c) 2004 Justin Couch
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom.particle;

// External imports
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.PointArray;
import org.j3d.aviatrix3d.PointAttributes;
import org.j3d.geom.particle.Particle;
import org.j3d.geom.particle.ParticleInitializer;

/**
 * A ParticleSystem implementation that uses points for representing each
 * particle.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>numColorCompMsg: Error message when user gave a colour component
 *     size that was not 0, 3 or 4.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.0 $
 */
public class PointParticleSystem extends AVParticleSystem
{
    /** Error message indicating wrong number of colour components supplied */
    private static final String COLOR_COMP_PROP =
        "org.j3d.renderer.aviatrix3d.geom.particle.PointParticleSystem.numColorCompMsg";

    /** Number of colour components to generate */
    private final int numColors;

    /** The recommended attributes for this system. Only set if requested */
    private PointAttributes attributes;

    /** The total number of particles we've created so far */
    private int createdParticleCount;

    /**
     * Create a new particle system using the given particle count, initialiser
     * and environment settings.
     *
     * @param name A name to register with this system. May be null.
     * @param particleCount The maximum number of particles to create
     * @param numColors The number of colour components to accept: 0, 3 or 4.
     * @throws IllegalArgumentException The number of colour components was not
     *    in the acceptable set of values.
     */
    public PointParticleSystem(String name,
                              int particleCount,
                              int numColors)
    {
        super(name, particleCount);

        if(numColors != 0 && numColors != 3 && numColors != 4)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(COLOR_COMP_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(numColors) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        this.numColors = numColors;
        particleGeometry = new PointArray();
        createdParticleCount = 0;

        initializeArrays();
    }

    //---------------------------------------------------------------
    // Methods defined by ParticleFactory
    //---------------------------------------------------------------

    /**
     * Request the number of coordinates each particle will use. Used so that
     * the manager can allocate the correct length array.
     *
     * @return The number of coordinates this particle uses
     */
    public final int coordinatesPerParticle()
    {
        return 1;
    }

    /**
     * Request the number of color components this particle uses. Should be a
     * value of 4 or 3 to indicate use or not of alpha channel.
     *
     * @return The number of color components in use
     */
    public final int numColorComponents()
    {
        return 4;
    }

    /**
     * Request the number of texture coordinate components this particle uses.
     * Should be a value of 2 or 3 to indicate use or not of 2D or 3D textures.
     *
     * @return The number of color components in use
     */
    public final int numTexCoordComponents()
    {
        return 0;
    }

    /**
     * Create a new particle instance.
     *
     * @return The new instance created
     */
    public Particle createParticle()
    {
        createdParticleCount++;
        return new PointParticle(vertices, colors, numColors == 4);
    }

    /**
     * Notification that this particle system has been removed from the scene
     * graph and it cleanup anything needed right now. Does nothing.
     */
    public void onRemove()
    {
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src)
    {
        ((PointArray)particleGeometry).setVertices(PointArray.COORDINATE_3,
                                                   vertices,
                                                   particleCount);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src)
    {
        switch(numColors)
        {
            case 3:
                ((PointArray)particleGeometry).setColors(false, colors);
                break;

            case 4:
                ((PointArray)particleGeometry).setColors(true, colors);
                break;

            // do nothing for any other case.
        }
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Get the attributes that the particle system implementation would
     * prefer to have. It is not required that the application use these, but
     * it is recommended.
     */
    public PointAttributes getRecommendedAttributes()
    {
        if(attributes == null)
        {
            attributes = new PointAttributes();
            attributes.setPointSize(10);
            attributes.setAntiAliased(true);
        }

        return attributes;
    }
}
