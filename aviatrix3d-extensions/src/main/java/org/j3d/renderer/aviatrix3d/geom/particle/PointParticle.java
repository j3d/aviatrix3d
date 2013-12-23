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
 * Particle that uses PointArrays as the basic geometry type.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.0 $
 */
class PointParticle extends AVParticle
{
    /**
     * Create a new numbered particle that writes it's values to the given
     * vertex and colour arrays.
     *
     * @param vertices The array to write vertex information to
     * @param colors The array to write optional colour information to
     * @param hasAlpha true if we have 4 component colour, 3 for false
     */
    PointParticle(float[] vertices, float[] colors, boolean hasAlpha)
    {
        super(vertices, colors, hasAlpha);
    }

    /**
     * Update the coordinate and colour information based on the particle's
     * position. Place them in the two arrays (previously set in the
     * constructor) at the specific offsets.
     *
     * @param vertexOffset The offset into the colour array for this object
     * @param colorOffset The offset into the colour array for this object
     */
    void writeValues(int vertexOffset, int colorOffset)
    {
        vertices[vertexOffset + X_COORD_INDEX] = (float)position.x;
        vertices[vertexOffset + Y_COORD_INDEX] = (float)position.y;
        vertices[vertexOffset + Z_COORD_INDEX] = (float)position.z;

        colors[colorOffset + RED_COLOR_INDEX] = red;
        colors[colorOffset + GREEN_COLOR_INDEX] = green;
        colors[colorOffset + BLUE_COLOR_INDEX] = blue;
        colors[colorOffset + ALPHA_COLOR_INDEX] = alpha;
    }
}
