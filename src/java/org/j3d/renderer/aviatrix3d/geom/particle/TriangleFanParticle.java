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
 * Particle that uses TriangleFanArrays as the basic geometry for each particle.
 * <p>
 *
 * Update methods are defined for a TriangleFanArray:
 *
 * <pre>
 *  <- width*2 ->
 *  3 --------- 2    / \
 *   |          |     |
 *   |          |     |
 *   |     +    |   height*2
 *   |          |     |
 *   |          |     |
 *  4 --------- 1    \ /
 *
 * </pre>
 * TriangleFan 1: 1,2,3,4
 * <p>
 *
 * Individual quads are not screen aligned at this point.
 *
 * @author Justin Couch
 * @version $Revision: 2.0 $
 */
class TriangleFanParticle extends AVParticle
{
    private static final int NUM_VERTICES_PER_PARTICLE = 4;

    private static final int COORD_POINT_1 = 0;
    private static final int COORD_POINT_2 = 3;
    private static final int COORD_POINT_3 = 6;
    private static final int COORD_POINT_4 = 9;

    private static final int TEX_POINT_1 = 0;
    private static final int TEX_POINT_2 = 2;
    private static final int TEX_POINT_3 = 4;
    private static final int TEX_POINT_4 = 6;

    /**
     * Create a new particle instance for the given index. Places the various
     * geometry values into the given arrays.
     *
     * @param index The particle number
     * @param vertices The array to write vertex information to
     * @param colors The array to write optional colour information to
     * @param normals The array to write normal information to
     * @param texCoords The array to write texture coordinate information to
     * @param hasAlpha true if we have 4 component colour, 3 for false
     */
    TriangleFanParticle(int index,
                 float[] vertices,
                 float[] normals,
                 float[] colors,
                 float[] texCoords,
                 boolean hasAlpha)
    {
        super(vertices, colors, hasAlpha);

        int normal_offset = index * NUM_VERTICES_PER_PARTICLE * 3;
        int tex_offset = index * NUM_VERTICES_PER_PARTICLE * 2;

        setTextureCoordinates(tex_offset, texCoords);
        setNormals(normal_offset, normals);
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
        if(colors != null)
        {
            if(haveAlphaColor)
            {
                colors[colorOffset + RED_COLOR_INDEX] = red;
                colors[colorOffset + GREEN_COLOR_INDEX] = green;
                colors[colorOffset + BLUE_COLOR_INDEX] = blue;
                colors[colorOffset + ALPHA_COLOR_INDEX] = alpha;

                colors[colorOffset + 4 + RED_COLOR_INDEX] = red;
                colors[colorOffset + 4 + GREEN_COLOR_INDEX] = green;
                colors[colorOffset + 4 + BLUE_COLOR_INDEX] = blue;
                colors[colorOffset + 4 + ALPHA_COLOR_INDEX] = alpha;

                colors[colorOffset + 8 + RED_COLOR_INDEX] = red;
                colors[colorOffset + 8 + GREEN_COLOR_INDEX] = green;
                colors[colorOffset + 8 + BLUE_COLOR_INDEX] = blue;
                colors[colorOffset + 8 + ALPHA_COLOR_INDEX] = alpha;

                colors[colorOffset + 12 + RED_COLOR_INDEX] = red;
                colors[colorOffset + 12 + GREEN_COLOR_INDEX] = green;
                colors[colorOffset + 12 + BLUE_COLOR_INDEX] = blue;
                colors[colorOffset + 12 + ALPHA_COLOR_INDEX] = alpha;
            }
            else
            {
                colors[colorOffset + RED_COLOR_INDEX] = red;
                colors[colorOffset + GREEN_COLOR_INDEX] = green;
                colors[colorOffset + BLUE_COLOR_INDEX] = blue;

                colors[colorOffset + 3 + RED_COLOR_INDEX] = red;
                colors[colorOffset + 3 + GREEN_COLOR_INDEX] = green;
                colors[colorOffset + 3 + BLUE_COLOR_INDEX] = blue;

                colors[colorOffset + 6 + RED_COLOR_INDEX] = red;
                colors[colorOffset + 6 + GREEN_COLOR_INDEX] = green;
                colors[colorOffset + 6 + BLUE_COLOR_INDEX] = blue;

                colors[colorOffset + 9 + RED_COLOR_INDEX] = red;
                colors[colorOffset + 9 + GREEN_COLOR_INDEX] = green;
                colors[colorOffset + 9 + BLUE_COLOR_INDEX] = blue;
            }
        }

        // point 1
        vertices[vertexOffset + COORD_POINT_1 + X_COORD_INDEX] = position.x + width;
        vertices[vertexOffset + COORD_POINT_1 + Y_COORD_INDEX] = position.y - height;
        vertices[vertexOffset + COORD_POINT_1 + Z_COORD_INDEX] = position.z;

        // point 2
        vertices[vertexOffset + COORD_POINT_2 + X_COORD_INDEX] = position.x + width;
        vertices[vertexOffset + COORD_POINT_2 + Y_COORD_INDEX] = position.y + height;
        vertices[vertexOffset + COORD_POINT_2 + Z_COORD_INDEX] = position.z;

        // point 3
        vertices[vertexOffset + COORD_POINT_3 + X_COORD_INDEX] = position.x - width;
        vertices[vertexOffset + COORD_POINT_3 + Y_COORD_INDEX] = position.y + height;
        vertices[vertexOffset + COORD_POINT_3 + Z_COORD_INDEX] = position.z;

        // point 4
        vertices[vertexOffset + COORD_POINT_4 + X_COORD_INDEX] = position.x - width;
        vertices[vertexOffset + COORD_POINT_4 + Y_COORD_INDEX] = position.y - height;
        vertices[vertexOffset + COORD_POINT_4 + Z_COORD_INDEX] = position.z;
    }

    /**
     * Convenience method to set up the texture coordinates for this quad.
     *
     * @param texCoordOffset The starting index into the texture coord array
     * @param normals The array to write values to
     */
    private void setTextureCoordinates(int texCoordOffset, float[] texCoords)
    {
       // point 1
       texCoords[texCoordOffset + TEX_POINT_1 + S_COORD_INDEX] = 1;
       texCoords[texCoordOffset + TEX_POINT_1 + T_COORD_INDEX] = 0;

       // point 2
       texCoords[texCoordOffset + TEX_POINT_2 + S_COORD_INDEX] = 1;
       texCoords[texCoordOffset + TEX_POINT_2 + T_COORD_INDEX] = 1;

       // point 3
       texCoords[texCoordOffset + TEX_POINT_3 + S_COORD_INDEX] = 0;
       texCoords[texCoordOffset + TEX_POINT_3 + T_COORD_INDEX] = 1;

       // point 4 = point 1
       texCoords[texCoordOffset + TEX_POINT_4 + S_COORD_INDEX] = 0;
       texCoords[texCoordOffset + TEX_POINT_4 + T_COORD_INDEX] = 0;
    }

    /**
     * Convenience method to set up the normals to point towards the Z axis
     * direction.
     *
     * @param normalOffset The starting index into the normal array
     * @param normals The array to write values to
     */
    private void setNormals(int normalOffset, float[] normals)
    {
        // point 1
        normals[normalOffset + COORD_POINT_1 + X_COORD_INDEX] = 0;
        normals[normalOffset + COORD_POINT_1 + Y_COORD_INDEX] = 0;
        normals[normalOffset + COORD_POINT_1 + Z_COORD_INDEX] = 1;

        // point 2
        normals[normalOffset + COORD_POINT_2 + X_COORD_INDEX] = 0;
        normals[normalOffset + COORD_POINT_2 + Y_COORD_INDEX] = 0;
        normals[normalOffset + COORD_POINT_2 + Z_COORD_INDEX] = 1;

        // point 3
        normals[normalOffset + COORD_POINT_3 + X_COORD_INDEX] = 0;
        normals[normalOffset + COORD_POINT_3 + Y_COORD_INDEX] = 0;
        normals[normalOffset + COORD_POINT_3 + Z_COORD_INDEX] = 1;

        // point 4
        normals[normalOffset + COORD_POINT_4 + X_COORD_INDEX] = 0;
        normals[normalOffset + COORD_POINT_4 + Y_COORD_INDEX] = 0;
        normals[normalOffset + COORD_POINT_4 + Z_COORD_INDEX] = 1;
    }
}
