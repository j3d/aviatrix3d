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
import org.j3d.aviatrix3d.PolygonAttributes;
import org.j3d.aviatrix3d.TriangleFanArray;
import org.j3d.aviatrix3d.TextureAttributes;
import org.j3d.geom.particle.Particle;
import org.j3d.geom.particle.ParticleInitializer;

/**
 * A ParticleSystem implementation that uses triangle fans in the shape of a
 * quad for representing each particle.
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
public class TriangleFanParticleSystem extends AVParticleSystem
{
    /** Error message indicating wrong number of colour components supplied */
    private static final String COLOR_COMP_PROP =
        "org.j3d.renderer.aviatrix3d.geom.particle.TriangleParticleSystem.numColorCompMsg";

    /** The texture type constant needed when updating texture coordinates */
    private static final int[] TEX_TYPES =
        { TriangleFanArray.TEXTURE_COORDINATE_2 };

    /** Number of colour components to generate */
    private final int numColors;

    /** The recommended polygon attributes. Only set if requested */
    private PolygonAttributes polyAttr;

    /** The recommended polygon attributes. Only set if requested */
    private TextureAttributes texAttr;

    /** The total number of particles we've created so far */
    private int createdParticleCount;

    /** Indicator of new particles created since last check */
    private boolean haveNewParticle;

    /** The fan counts for each particle */
    private int[] fanCount;

    /** The height of the particle */
    private float particleHeight;

    /** The width of the particle */
    private float particleWidth;

    /**
     * Create a new particle system using the given particle count, initialiser
     * and environment settings. Width and height default to 0.02m.
     *
     * @param name A name to register with this system. May be null.
     * @param particleCount The maximum number of particles to create
     * @param numColors The number of colour components to accept: 0, 3 or 4.
     */
    public TriangleFanParticleSystem(String name,
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
        createdParticleCount = 0;
        haveNewParticle = false;

        fanCount = new int[particleCount];
        for(int i = 0; i < particleCount; i++)
            fanCount[i] = 4;

        particleWidth = 0.02f;
        particleHeight = 0.02f;

        particleGeometry = new TriangleFanArray();
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
        return 4;
    }

    /**
     * Request the number of color components this particle uses. Should be a
     * value of 4 or 3 to indicate use or not of alpha channel.
     *
     * @return The number of color components in use
     */
    public final int numColorComponents()
    {
        return numColors;
    }

    /**
     * Request the number of texture coordinate components this particle uses.
     * Should be a value of 2 or 3 to indicate use or not of 2D or 3D textures.
     *
     * @return The number of color components in use
     */
    public final int numTexCoordComponents()
    {
        return 2;
    }

    /**
     * Create a new particle instance.
     *
     * @return The new instance created
     */
    public Particle createParticle()
    {
        // Register a data change listener now
        particleGeometry.dataChanged(this);
        haveNewParticle = true;

        TriangleFanParticle particle =
            new TriangleFanParticle(createdParticleCount++,
                                    vertices,
                                    normals,
                                    colors,
                                    texCoords[0],
                                    numColors == 4);
        particle.setHeight(particleHeight);
        particle.setWidth(particleWidth);

        return particle;
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
        TriangleFanArray geom = (TriangleFanArray)particleGeometry;

        geom.setVertices(TriangleFanArray.COORDINATE_3,
                         vertices,
                         particleCount * 4);

        if(haveNewParticle)
            geom.setFanCount(fanCount, particleCount);

    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src)
    {
        TriangleFanArray geom = (TriangleFanArray)particleGeometry;

        switch(numColors)
        {
            case 3:
                geom.setColors(false, colors);
                break;

            case 4:
                geom.setColors(true, colors);
                break;

            // do nothing for any other case.
        }

        if(genTexCoords)
            geom.setTextureCoordinates(TEX_TYPES, texCoords, 1);

        // Only update tex coords and normals if there has been a change
        // in the number of particles created. No need to change these
        // otherwise.
        if(haveNewParticle)
        {
            geom.setNormals(normals);
            haveNewParticle = false;
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
    public TextureAttributes getRecommendedTextureAttributes()
    {
        if(texAttr == null)
        {
            texAttr = new TextureAttributes();
            texAttr.setTextureMode(TextureAttributes.MODE_REPLACE);
        }

        return texAttr;
    }

    /**
     * Get the attributes that the particle system implementation would
     * prefer to have. It is not required that the application use these, but
     * it is recommended.
     */
    public PolygonAttributes getRecommendedPolygonAttributes()
    {
        if(polyAttr == null)
        {
            polyAttr = new PolygonAttributes();
            polyAttr.setCulledFace(PolygonAttributes.CULL_NONE);
        }

        return polyAttr;
    }

    /**
     * Set the size that particles should take in each dimension. This will
     * only effect particles created after this call.
     *
     * @param width The width of the particle in meters
     * @param height The height of the particle in meters
     */
    public void setParticleSize(float width, float height)
    {
        particleWidth = width;
        particleHeight = height;
    }
}
