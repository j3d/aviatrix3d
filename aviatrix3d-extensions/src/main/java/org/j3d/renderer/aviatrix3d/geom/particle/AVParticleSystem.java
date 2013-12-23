/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom.particle;

// External imports

// Local imports
import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.geom.particle.ParticleInitializer;
import org.j3d.geom.particle.ParticleSystem;

/**
 * Base particle system implementation for geometry implemented over
 * Aviatrix3D scene graphs.
 * <p>
 *
 * The node updater is registered for bounds changing events every frame.
 * However, data changed listeners are not registered, unless per-vertex
 * colours are being generated. If a derived class wishes to update other
 * aspects, such as normals, texture coordinates or vertex attributes, then
 * they will need to make their own arrangements to request the updates.
 * <p>
 * Per-vertex colours can be turned on or off by the derived class simply
 * by changing the return value from <code>numColorComponents()</code> to be
 * zero.
 *
 * @author Justin Couch
 * @version $Revision: 2.0 $
 */
public abstract class AVParticleSystem extends ParticleSystem
    implements NodeUpdateListener
{
    /** Array containing the current position coordinates */
    protected float[] vertices;

    /**
     * Array containing the current texture coordinates. Single tex coord
     * set only is initialised by default.
     */
    protected float[][] texCoords;

    /** Array containing the current color values */
    protected float[] colors;

    /** Array containing the current normals */
    protected float[] normals;

    /** The shape containing the geometry */
    protected Geometry particleGeometry;

    /**
     * Flag indicating we also need to force-feed new arrays to the particles
     * because the user upped the particle count last frame.
     */
    protected boolean sendNewArrays;

    /**
     * Create a new particle system that represents the given type.
     *
     * @param systemType An identifier describing the current system type
     * @param particleCount The max nuumber of particles to create
     */
    public AVParticleSystem(String systemType, int particleCount)
    {
        super(systemType, particleCount);
        sendNewArrays = false;
    }

    /**
     * Update the arrays for the geometry object. Calls the bounds and data
     * change notifcation methods. Derived classes need to handle the methods
     * defined by NodeUpdateListener.
     */
    protected void updateGeometry()
    {
        int num_particles = particleList.size();

        int color_offset = numColorComponents() * coordinatesPerParticle();
        int coord_mult = coordinatesPerParticle() * 3;
        int tex_mult = coordinatesPerParticle() * numTexCoordComponents();

        if(sendNewArrays)
        {
// This does not correctly handle texture coordinate and normal arrays that
// should also be updated.
            if((tex_mult == 0) || (texCoordInterp == null))
            {
                for(int i = 0; i < num_particles; i++)
                {
                    AVParticle p = (AVParticle)particleList.next();
                    p.updateArrays(vertices, colors);
                    p.writeValues(i * coord_mult, i * color_offset);
                }
            }
            else
            {
                for(int i = 0; i < num_particles; i++)
                {
                    AVParticle p = (AVParticle)particleList.next();
                    p.updateArrays(vertices, colors);
                    p.writeValues(i * coord_mult, i * color_offset);

                    float lifetime = timeNow - p.getBirthTime();
                    texCoordInterp.interpolate(lifetime,
                                               i * tex_mult,
                                               texCoords[0]);
                }
            }

            sendNewArrays = false;
        }
        else
        {
            if((tex_mult == 0) || (texCoordInterp == null))
            {
                for(int i = 0; i < num_particles; i++)
                {
                    AVParticle p = (AVParticle)particleList.next();
                    p.writeValues(i * coord_mult, i * color_offset);
                }
            }
            else
            {
                for(int i = 0; i < num_particles; i++)
                {
                    AVParticle p = (AVParticle)particleList.next();
                    p.writeValues(i * coord_mult, i * color_offset);

                    float lifetime = timeNow - p.getBirthTime();
                    texCoordInterp.interpolate(lifetime,
                                               i * tex_mult,
                                               texCoords[0]);
                }
            }
        }

        particleList.reset();

        particleGeometry.boundsChanged(this);

        if(colors != null)
            particleGeometry.dataChanged(this);


    }

    /**
     * Get the scene graph object that represents this particle system and can
     * be inserted into the scene graph.
     */
    public Geometry getNode()
    {
        return particleGeometry;
    }

    /**
     * Set up the arrays used internally now. This must be called by the
     * derived class during it's constructor.
     */
    protected void initializeArrays()
    {
        int base_size = maxParticleCount * coordinatesPerParticle();

        int n_vert = base_size * 3;
        int n_colors = base_size * numColorComponents();
        int n_normals = base_size * 3;
        int n_tex_coords = base_size * numTexCoordComponents();

        vertices = new float[n_vert];
        normals = new float[n_normals];

        if(n_colors != 0)
            colors = new float[n_colors];

        if(n_tex_coords != 0)
            texCoords = new float[1][n_tex_coords];
    }

    /**
     * Change the maximum number of particles that can be generated. If the
     * number is greater than the currently set value, it will permit more to
     * be made according to the normal creation speed. If the number is less
     * than the current amount, then no new particles will be created until the
     * current total has died down below the new maximum value.
     *
     * @param maxCount The new maximum particle count to use
     * @throws IllegalArgumentException The particle count was negative
     */
    public void setMaxParticleCount(int maxCount)
    {
        super.setMaxParticleCount(maxCount);

        if(maxCount * coordinatesPerParticle() > vertices.length)
        {
            initializeArrays();
            sendNewArrays = true;
        }
    }
}
