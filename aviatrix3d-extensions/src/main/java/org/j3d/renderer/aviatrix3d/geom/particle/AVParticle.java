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
// None

// Local imports
import org.j3d.geom.particle.Particle;

/**
 * Internal Aviatrix3D-specific extension to the basic Particle object.
 * <p>
 *
 * This class is to allow extra methods and common variables to be managed as
 * a single interface.
 *
 * @author Justin Couch
 * @version $Revision: 2.0 $
 */
abstract class AVParticle extends Particle
{
    /** The particle vertex X offset from the base position */
    protected static final int X_COORD_INDEX = 0;

    /** The particle vertex Y offset from the base position */
    protected static final int Y_COORD_INDEX = 1;

    /** The particle vertex Z offset from the base position */
    protected static final int Z_COORD_INDEX = 2;

    /** The particle color red component offset from the base position */
    protected static final int RED_COLOR_INDEX = 0;

    /** The particle color green component offset from the base position */
    protected static final int GREEN_COLOR_INDEX = 1;

    /** The particle color blue component offset from the base position */
    protected static final int BLUE_COLOR_INDEX = 2;

    /** The particle color alpha component offset from the base position */
    protected static final int ALPHA_COLOR_INDEX = 3;

    /** The particle S texture coordinate offset from the base position */
    protected static final int S_COORD_INDEX = 0;

    /** The particle T texture coordinate offset from the base position */
    protected static final int T_COORD_INDEX = 1;

    /** Do we have 3 or 4 component colours? */
    protected final boolean haveAlphaColor;

    /** Array containing the current position coordinates. */
    protected float[] vertices;

    /** Array containing the current color values. */
    protected float[] colors;

    /**
     * Create a new particle that writes its values to the given vertex and
     * colour arrays.
     *
     * @param vertices The array to write vertex information to
     * @param colors The array to write optional colour information to
     * @param hasAlpha true if we have 4 component colour, 3 for false
     */
    AVParticle(float[] vertices, float[] colors, boolean hasAlpha)
    {
        haveAlphaColor = hasAlpha;
        this.vertices = vertices;
        this.colors = colors;
    }

    /**
     * Update the coordinate and colour information based on the particle's
     * position. Place them in the two arrays (previously set in the
     * constructor) at the specific offsets.
     *
     * @param vertexOffset The offset into the colour array for this object
     * @param colorOffset The offset into the colour array for this object
     */
    abstract void writeValues(int vertexOffset, int colorOffset);

    /**
     * Someone has done something silly and decided that bigger geometry is
     * needed. Here's the new arrays to use.
     *
     * @param vertices The array to write vertex information to
     * @param colors The array to write optional colour information to
     */
    void updateArrays(float[] vertices, float[] colors)
    {
        this.vertices = vertices;
        this.colors = colors;
    }
}
