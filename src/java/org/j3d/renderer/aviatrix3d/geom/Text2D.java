/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.renderer.aviatrix3d.geom;

// External imports
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.ArrayList;

import javax.media.opengl.GL;

import org.j3d.geom.CharacterCreator;
import org.j3d.geom.CharacterData;

import org.j3d.util.I18nManager;

// Local imports
import org.j3d.aviatrix3d.BoundingBox;
import org.j3d.aviatrix3d.Geometry;
import org.j3d.aviatrix3d.InvalidWriteTimingException;
import org.j3d.aviatrix3d.picking.NotPickableException;

/**
 * Flat, ploygonalised 2D text representation.
 * <p>
 *
 * A single line of text is normalise to always be one unit high regardless of
 * the original font size. The relative size can then be changed using a
 * multiplication factor, though the polygonalisation stays the same. If using
 * large text, and need fine polygonalisation, then making use of a custom
 * {@link org.j3d.geom.CharacterCreator} with a large point size font and finer
 * flatness would be recommended.
 * <p>
 *
 * Because all the methods for setting values in this class can influence the
 * final bounds of the object, they are all required to use the boundsChanged()
 * callback from NodeUpdateListener.
 * <p>
 *
 * Since the text is 2D, it has no depth. If an intersection test is created
 * such that the ray is parallel to the local X-Y plane and does not change in
 * Z depth, then no intersection will be generated. The implementation attempts
 * to optimise the testing based on the bounds of the individual character
 * before checking polygons.
 * <p>
 *
 * The default setup following construction follows English requirements:<br>
 * horizontal: true<br>
 * leftToRight: true<br>
 * topToBottom: true<br>
 * size: 1.0<br>
 * spacing: 1.0<br>
 * horizontal justification: JUSTIFY_BEGIN <br>
 * vertical justification: JUSTIFY_BEGIN
 *
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>invalidSpacingMsg: Error message when text spacing is not positive.</li>
 * <li>invalidSizeMsg: Error message when text size is not positive</li>
 * <li>invalidJustificationMsg: Error message when an invalid justification type
 *     is requested</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.19 $
 */
public class Text2D extends Geometry
{
    /** Message when the user provides a spacing that is <= 0 */
    private static final String INVALID_SPACING_PROP =
		"org.j3d.renderer.aviatrix3d.geom.Text2D.invalidSpacingMsg";

    /** Message when the user provides a size that is <= 0 */
    private static final String INVALID_SIZE_PROP =
		"org.j3d.renderer.aviatrix3d.geom.Text2D.invalidSizeMsg";

    /** Message when the user provides a dodfy justification flag */
    private static final String INVALID_JUSTIFY_PROP =
		"org.j3d.renderer.aviatrix3d.geom.Text2D.invalidJustificationMsg";

    /** The global default generator instance to use */
    private static final CharacterCreator DEFAULT_CREATOR =
        new CharacterCreator(new Font(null, Font.PLAIN, 12), 0.01);

    /** Justify to the first character of the line */
    public static final int JUSTIFY_FIRST = 0;

    /** Justify to the beginning of the line */
    public static final int JUSTIFY_BEGIN = 1;

    /** Justify about the middle */
    public static final int JUSTIFY_MIDDLE = 2;

    /** Justify to the end of the string */
    public static final int JUSTIFY_END = 3;


    /** Creates our character data for us */
    private CharacterCreator generator;

    /** The set of strings to be rendered */
    private String[] text;

    /** The individual character representations for each line of text */
    private ArrayList[] textCharacters;

    /** The characters adjusted for leftToRight & topToBottom */
    private ArrayList[] renderedCharacters;

    /** The required length for a single text string to take up */
    private float[] lengths;

    /** The calculated start position of each string from the char bounds */
    private float[] lineStartPos;

    /** The "height" of the gylphs per line. Used to set out the spacing */
    private float[] lineSpacing;

    /** The maximum extents any particular line is allowed to have */
    private float maxExtents;

    /** Is the text to be rendered horizontally or vertically. */
    private boolean horizontal;

    /** Should the horizontal organisation be left to right or reversed.*/
    private boolean leftToRight;

    /** Should the vertical organisation be top to bottom or reversed.*/
    private boolean topToBottom;

    /** The size scaling factor */
    private float sizeScale;

    /** The scale of the normal size spacing */
    private float spacingScale;

    /** Flag indicating the horizontal justification strategy to use */
    private int hJustification;

    /** Flag indicating the vertical justification strategy to use */
    private int vJustification;

    /** A working array for grabbing vertex and bounds info */
    private float[] wkTmp;

    /**
     * Create a new, default text representation. No font information is set,
     * so a plain, 12 point font is used.
     */
    public Text2D()
    {
        this(null);
        wkTmp = new float[3];
    }

    /**
     * Create a new text representation using the provided
     * {@link org.j3d.geom.CharacterCreator}. If the argument is null, then
     * the default generator is used.
     *
     * @param charGen The creator of characters to use, or null
     */
    public Text2D(CharacterCreator charGen)
    {
        if(charGen != null)
            generator = charGen;
        else
            generator = DEFAULT_CREATOR;

        bounds = INVALID_BOUNDS;
        horizontal = true;
        leftToRight = true;
        topToBottom = true;
        sizeScale = 1;
        spacingScale = 1;
        hJustification = JUSTIFY_BEGIN;
        vJustification = JUSTIFY_BEGIN;
        maxExtents = 0;

        wkTmp = new float[3];
    }


    //----------------------------------------------------------
    // Methods defined by ObjectRenderable
    //----------------------------------------------------------

    /**
     * This method is called to render this node.  All openGL commands needed
     * to render the node should be executed.  Any transformations needed
     * should be added to the transformation stack premultiplied.  This
     * method must be re-entrant as it can be called from multiple
     * places at once.
     *
     * @param gl The gl context to draw with
     */
    public void render(GL gl)
    {
        if(text == null) {
            return;
        }
        // The normals all point along the +Z axis in the local frame of
        // reference.
        gl.glNormal3f(0, 0, 1);

/*
        gl.glBegin(GL.GL_LINES);
        gl.glVertex3f(0, 0.5f, 0);
        gl.glVertex3f(0, -0.5f, 0);

        gl.glVertex3f(0.5f, 0, 0);
        gl.glVertex3f(-0.5f, 0, 0);

        gl.glVertex3f(0, -lineSpacing[0], 0);
        gl.glVertex3f(5, -lineSpacing[0], 0);
        gl.glEnd();
*/

        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);

        if(horizontal)
        {
            float y = 0;

            for(int i = 0; i < text.length; i++)
            {
                ArrayList chr = renderedCharacters[i];
                int size = chr.size();

                y -= lineSpacing[i];

                gl.glPushMatrix();
                gl.glTranslatef(lineStartPos[i], y, 0);
                gl.glScalef(sizeScale, sizeScale, 1);

                for(int j = 0; j < size; j++)
                {
                    CharacterData data = (CharacterData)chr.get(j);

                    // just move forward for whitespace
                    if (data.coordinates == null) {
                        gl.glTranslatef(data.bounds.width, 0, 0);
                        continue;
                    }

                    gl.glVertexPointer(3, GL.GL_FLOAT, 0, data.coordinates);
                    gl.glDrawElements(GL.GL_TRIANGLES,
                                      data.numIndex,
                                      GL.GL_UNSIGNED_INT,
                                      data.coordIndex);
                    gl.glTranslatef(data.bounds.width, 0, 0);
                }

                gl.glPopMatrix();

            }
        }
        else
        {
            float x = 0;

            for(int i = 0; i < text.length; i++)
            {
                ArrayList chr = renderedCharacters[i];
                int size = chr.size();

                gl.glPushMatrix();
                gl.glTranslatef(x, lineStartPos[i], 0);
                gl.glScalef(sizeScale, sizeScale, 1);

                for(int j = 0; j < size; j++)
                {
                    CharacterData data = (CharacterData)chr.get(j);

                    // just move forward for whitespace
                    if (data.coordinates == null) {
                        gl.glTranslatef(0, -data.bounds.height, 0);
                        continue;
                    }

                    gl.glVertexPointer(3, GL.GL_FLOAT, 0, data.coordinates);
                    gl.glDrawElements(GL.GL_TRIANGLES,
                                      data.numIndex,
                                      GL.GL_UNSIGNED_INT,
                                      data.coordIndex);
                    gl.glTranslatef(0, -data.bounds.height, 0);
                }

                gl.glPopMatrix();

                x += lineSpacing[i];
            }
        }

        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     *
     * @param gl The gl context to draw with
     */
    public void postRender(GL gl)
    {
        // do nothing
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The objec to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
        throws ClassCastException
    {
        Text2D txt = (Text2D)o;
        return compareTo(txt);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof Text2D))
            return false;
        else
            return equals((Text2D)o);
    }

    //----------------------------------------------------------
    // Methods defined by Geometry
    //----------------------------------------------------------

    /**
     * Check to see if this geometry is making the geometry visible or not.
     * Returns true if the defined number of coordinates and strips are both
     * non-zero.
     *
     * @return true when the geometry is visible
     */
    protected boolean isVisible()
    {
        return (text != null) && (text.length != 0);
    }

    /**
     * Check to see whether this shape is something that represents 2D or 3D
     * geometry. Pure 2D geometry is not effected by any
     * {@link org.j3d.aviatrix3d.rendering.EffectRenderable}, while 3D is.
     * Note that this can be changed depending on the type of geometry itself.
     * A Shape3D node with an IndexedLineArray that only has 2D coordinates is
     * as much a 2D geometry as a raster object.
     *
     * @return True if this is 2D geometry, false if this is 3D
     */
    public boolean is2D()
    {
        return false;
    }

    //----------------------------------------------------------
    // Methods defined by SceneGraphObject
    //----------------------------------------------------------

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * scene graph.
     *
     * @param state true if this should be marked as live now
     */
    protected void setLive(boolean state)
    {
        // Ignore stuff that doesn't change the state
        if(state == alive)
            return;

        boolean old_state = alive;

        super.setLive(state);

        if(!old_state && state)
            recomputeBounds();
    }

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    protected void recomputeBounds()
    {
        if(text == null)
        {
            bounds = INVALID_BOUNDS;
            return;
        }

        if(bounds == INVALID_BOUNDS)
            bounds = new BoundingBox();

        float max_x = 0;
        float max_y = 0;

        // Impl Note:
        // To work out the start position for each line, we first set the
        // individual line calculated length into lineStartPos. Then once we
        // know the complete extents, then calculate the start positions
        // for each line based on the calculated maximum extents

        boolean empty_lines = false;

        if(horizontal)
        {
            Font font = generator.getFont();
            FontRenderContext frc = generator.getFontRenderContext();

            for(int i = 0; i < text.length; i++)
            {
                ArrayList chr = textCharacters[i];
                int size = chr.size();
                float local_y = 0;
                float local_x = 0;

                if (size == 0)
                {
                    // Need to skip a line.  Use the first non zero spacing we encounter
                    empty_lines = true;
                    continue;
                }

                for(int j = 0; j < size; j++)
                {
                    CharacterData data = (CharacterData)chr.get(j);
                    local_x += (float)data.bounds.width;
                    float y = (float)data.bounds.height;
                    if(y > local_y)
                        local_y = y;
                }

                CharacterData data = (CharacterData)chr.get(0);
//System.out.println("Normal y = " + local_y);

                LineMetrics line_m = font.getLineMetrics(text[i], frc);
                local_y = line_m.getAscent() * data.scale;

//System.out.println("local_y = " + local_y + " scale " + data.scale);

                lineStartPos[i] = local_x * sizeScale;
                lineSpacing[i] = local_y * sizeScale * spacingScale;

//                lineStartPos[i] = local_x * sizeScale;
//                lineSpacing[i] = local_y * sizeScale * spacingScale;

//System.out.println("line [" + i + "] " + local_y + " " + sizeScale + " " + spacingScale);

                // Not handling maxExtents limits yet.
                if(lengths[i] > 0)
                    lineStartPos[i] = lengths[i];

                if(local_x > max_x)
                    max_x = (lengths[i] > 0) ? lengths[i] : local_x;

                max_y += local_y;
            }

            max_x *= sizeScale * text.length;
            max_y *= sizeScale * spacingScale * text.length;

            if(maxExtents > 0)
                max_x = maxExtents;

            // Doesn't correctly deal with limited line lengths etc
            switch(hJustification)
            {
                case JUSTIFY_FIRST:
                case JUSTIFY_BEGIN:
                    for(int i = 0; i < text.length; i++)
                        lineStartPos[i] = 0;
                    break;

                case JUSTIFY_MIDDLE:
                    for(int i = 0; i < text.length; i++)
                        lineStartPos[i] = lineStartPos[i] * -0.5f;
                    break;

                case JUSTIFY_END:
                    for(int i = 0; i < text.length; i++)
                        lineStartPos[i] = max_x - lineStartPos[i];
                    break;
            }

        }
        else
        {
            for(int i = 0; i < text.length; i++)
            {
                ArrayList chr = textCharacters[i];
                int size = chr.size();
                float local_x = 0;
                float local_y = 0;

                if (size == 0)
                {
                    empty_lines = true;
                }

                for(int j = 0; j < size; j++)
                {
                    CharacterData data = (CharacterData)chr.get(j);
                    local_y += (float)data.bounds.width;
                    float x = (float)data.bounds.height;
                    if(x > local_x)
                        local_x = x;
                }

                lineStartPos[i] = local_y * sizeScale;
                lineSpacing[i] = local_x * sizeScale * spacingScale;

                if(lengths[i] > 0)
                    lineStartPos[i] = lengths[i];

                if(local_y > max_y)
                    max_y = (lengths[i] > 0) ? lengths[i] : local_y;

                max_x += local_x;
            }

            max_x *= sizeScale * spacingScale * text.length;
            max_y *= sizeScale * text.length;

            if(maxExtents > 0)
                max_x = maxExtents;

            switch(vJustification)
            {
                case JUSTIFY_FIRST:
                case JUSTIFY_BEGIN:
                    for(int i = 0; i < text.length; i++)
                        lineStartPos[i] = 0;
                    break;

                case JUSTIFY_MIDDLE:
                    for(int i = 0; i < text.length; i++)
                        lineStartPos[i] = lineStartPos[i] * -0.5f;
                    break;

                case JUSTIFY_END:
                    for(int i = 0; i < text.length; i++)
                        lineStartPos[i] = max_y - lineStartPos[i];
                    break;
            }
        }

        if (empty_lines)
        {
            // Handle empty lines, use spacing of first non empty line
            // If all are empty then just do nothing
            float spacing = 0;

            for(int i=0; i < lineSpacing.length; i++)
            {
                if (lineSpacing[i] > 0)
                {
                    spacing = lineSpacing[i];
                    break;
                }
            }

            for(int i=0; i < lineSpacing.length; i++)
            {
                if (lineSpacing[i] == 0)
                {
                    lineSpacing[i] = spacing;
                    break;
                }
            }
        }

        float start_x = 0;
        float start_y = 0;
        float end_x = 0;
        float end_y = 0;

        switch(hJustification)
        {
            case JUSTIFY_FIRST:
                end_x = max_x;
                break;

            case JUSTIFY_BEGIN:
                end_x = max_x;
                break;

            case JUSTIFY_MIDDLE:
                start_x = -max_x * 0.5f;
                end_x = max_x * 0.5f;
                break;

            case JUSTIFY_END:
                start_x = -max_x;
                break;
        }

        switch(vJustification)
        {
            case JUSTIFY_FIRST:
                end_y = -max_y;
                break;

            case JUSTIFY_BEGIN:
                end_y = -max_y;
                break;

            case JUSTIFY_MIDDLE:
                start_y = max_y * 0.5f;
                end_y = -max_y * 0.5f;
                break;

            case JUSTIFY_END:
                start_y = max_y;
                break;
        }

        ((BoundingBox)bounds).setMinimum(start_x, end_y, 0);
        ((BoundingBox)bounds).setMaximum(end_x, start_y, 0);

        // Now reorganise the lettering according to the top to bottom and
        // left to right flags.
        if(leftToRight)
        {
            for(int i = 0; i < textCharacters.length; i++)
            {
                renderedCharacters[i].clear();
                renderedCharacters[i].addAll(textCharacters[i]);
            }
        }
        else
        {
            for(int i = 0; i < textCharacters.length; i++)
            {
                renderedCharacters[i].clear();
                ArrayList this_line = textCharacters[i];
                int num_chars = this_line.size();

                for(int j = 0; j < num_chars; j++)
                    renderedCharacters[i].add(this_line.get(num_chars - j - 1));
            }
        }

        if(!topToBottom)
        {
            int num_lines = renderedCharacters.length;
            for(int i = 0; i < num_lines / 2; i++)
            {
                ArrayList tmp = renderedCharacters[i];
                renderedCharacters[i] = renderedCharacters[num_lines - i - 1];
                renderedCharacters[num_lines - i - 1] = tmp;

                float f_tmp = lineStartPos[i];
                lineStartPos[i] = lineStartPos[num_lines - i - 1];
                lineStartPos[num_lines - i - 1] = f_tmp;

                f_tmp = lengths[i];
                lengths[i] = lengths[num_lines - i - 1];
                lengths[num_lines - i - 1] = f_tmp;

                f_tmp = lineSpacing[i];
                lineSpacing[i] = lineSpacing[num_lines - i - 1];
                lineSpacing[num_lines - i - 1] = f_tmp;
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by Geometry
    //----------------------------------------------------------

    /**
     * Check for all intersections against this geometry using a line segment and
     * return the exact distance away of the closest picking point. Default
     * implementation always returns false indicating that nothing was found.
     * Derived classes should override and provide a real implementation.
     *
     * @param start The start point of the segment
     * @param end The end point of the segment
     * @param findAny True if it only has to find a single intersection and can
     *   exit as soon as it finds the first intersection. False if it must find
     *   the closest polygon
     * @param dataOut An array to put the data in for the intersection. Exact
     *   format is described by the flags
     * @param dataOutFlags A set of derived-class specific flags describing what
     *   data should be included in the output array
     * @return True if an intersection was found according to the input request
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    public boolean pickLineSegment(float[] start,
                                   float[] end,
                                   boolean findAny,
                                   float[] dataOut,
                                   int dataOutFlags)
        throws NotPickableException
    {
        super.pickLineSegment(start, end, findAny, dataOut, dataOutFlags);

        // Since we know that the text lies in the X-Y plane, we can cheat on
        // the comparison test. If the Z value of the two end points both lie
        // on the same side of the plane, there's no way it can intersect with
        // the segment, so just return early.
        if(((start[2] < 0) && (end[2] < 0)) ||
           ((start[2] > 0) && (end[2] > 0)))
            return false;

        // Ok, so we have an intersection with the plane of the text, let's
        // find the intersection point with the plane of the text and determine
        // whether that is inside the bounds.
        // normal dot direction
        float n_x = 0;
        float n_y = 0;
        float n_z = 1;

        float d_x = end[0] - start[0];
        float d_y = end[1] - start[1];
        float d_z = end[2] - start[2];

        float n_dot_dir = n_x * d_x + n_y * d_y + n_z * d_z;

        // ray and plane parallel?
        if(n_dot_dir == 0)
            return false;

        BoundingBox bbox = (BoundingBox)bounds;
        bbox.getMinimum(wkTmp);

        // d dot first point of the polygon
        float d = n_x * wkTmp[0] +
                  n_y * wkTmp[1] +
                  n_z * wkTmp[2];

        float n_dot_o = n_x * start[0] + n_y * start[1] + n_z * start[2];

        // t = (d - N.O) / N.D
        float t = (d - n_dot_o) / n_dot_dir;

        // So we have an intersection with the plane of the polygon and the
        // segment/ray. Using the winding rule to see if inside or outside
        // First store the exact intersection point anyway, regardless of
        // whether this is an intersection or not.
        dataOut[0] = start[0] + d_x * t;
        dataOut[1] = start[1] + d_y * t;
        dataOut[2] = start[2] + d_z * t;

        return true;
    }

    /**
     * Check for all intersections against this geometry using a line ray and
     * return the exact distance away of the closest picking point. Default
     * implementation always returns false indicating that nothing was found.
     * Derived classes should override and provide a real implementation.
     *
     * @param origin The start point of the ray
     * @param direction The direction vector of the ray
     * @param findAny True if it only has to find a single intersection and can
     *   exit as soon as it finds the first intersection. False if it must find
     *   the closest polygon
     * @param dataOut An array to put the data in for the intersection. Exact
     *   format is described by the flags
     * @param dataOutFlags A set of derived-class specific flags describing what
     *   data should be included in the output array
     * @return True if an intersection was found according to the input request
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    public boolean pickLineRay(float[] origin,
                               float[] direction,
                               boolean findAny,
                               float[] dataOut,
                               int dataOutFlags)
        throws NotPickableException
    {
        super.pickLineRay(origin, direction, findAny, dataOut, dataOutFlags);

        // Since we know that the text lies in the X-Y plane, we can cheat on
        // the comparison test. If the Z value of the point is negative, there's
        // no way it can intersect with the ray, so just return early.
        if(origin[2] < 0)
            return false;

        // We're in front, but does the direction point away (z component of
        // direction is positive?
        if(direction[2] > 0)
            return false;

        // Ok, so we have an intersection with the plane of the text, let's
        // find the intersection point with the plane of the text and determine
        // whether that is inside the bounds.
        // normal dot direction
        float n_x = 0;
        float n_y = 0;
        float n_z = 1;

        float n_dot_dir =
            n_x * direction[0] + n_y * direction[1] + n_z * direction[2];

        // ray and plane parallel?
        if(n_dot_dir == 0)
            return false;

        BoundingBox bbox = (BoundingBox)bounds;
        bbox.getMinimum(wkTmp);

        // d dot first point of the polygon
        float d = n_x * wkTmp[0] +
                  n_y * wkTmp[1] +
                  n_z * wkTmp[2];

        float n_dot_o = n_x * origin[0] + n_y * origin[1] + n_z * origin[2];

        // t = (d - N.O) / N.D
        float t = (d - n_dot_o) / n_dot_dir;

        // intersection before the origin
        if(t < 0)
            return false;

        // So we have an intersection with the plane of the polygon and the
        // segment/ray. Using the winding rule to see if inside or outside
        // First store the exact intersection point anyway, regardless of
        // whether this is an intersection or not.
        dataOut[0] = origin[0] + direction[0] * t;
        dataOut[1] = origin[1] + direction[1] * t;
        dataOut[2] = origin[2] + direction[2] * t;

        return true;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Replace the internal character generator with another. If the argument
     * is null, then the default is used again.
     *
     * @param charGen The creator of characters to use, or null
     */
    public void setCharacterCreator(CharacterCreator charGen)
    {
        if(charGen != null)
            generator = charGen;
        else
            generator = DEFAULT_CREATOR;
    }

    /**
     * Set the list of strings that you want to have rendered. Each string
     * appears on a new line.
     *
     * @param text An array of strings to render
     * @param numText The number of strings to render
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setText(String[] text, int numText)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        int old_lines = (this.text == null) ? 0 : this.text.length;

        if(numText == 0)
        {
            this.text = null;
            textCharacters = null;
        }
        else
        {
            if(numText != old_lines)
            {
                this.text = new String[numText];
                textCharacters = new ArrayList[numText];
                renderedCharacters = new ArrayList[numText];
                lineStartPos = new float[numText];
                lineSpacing = new float[numText];
                lengths = new float[numText];

                for(int i = 0; i < numText; i++)
                {
                    textCharacters[i] = new ArrayList();
                    renderedCharacters[i] = new ArrayList();
                }
            }
            else
            {
                for(int i = 0; i < numText; i++)
                {
                    textCharacters[i].clear();
                    renderedCharacters[i].clear();
                }
            }

            System.arraycopy(text, 0, this.text, 0, numText);

            for(int i = 0; i < numText; i++)
            {
                char[] characters = text[i].toCharArray();
                generator.createCharacterTriangles(characters,
                                                   characters.length,
                                                   textCharacters[i]);
                int len = textCharacters[i].size();

                for(int j=0; j < len; j++)
                {
                    CharacterData data = (CharacterData)textCharacters[i].get(j);

                    // ignore whitespace
                    if (data.coordinates == null)
                    {
                        continue;
                    }

                    data.coordinates.rewind();
                    data.coordIndex.rewind();
                }
            }
        }

//Alan: Added, not sure if necessary
        recomputeBounds();
    }

    /**
     * Get a reference to the currently set collection of strings. If no
     * strings are set, then this returns null.
     *
     * @return The current internal array of strings
     */
    public String[] getText()
    {
        return text;
    }

    /**
     * Change whether the text should be rendered vertically or horizontally.
     * Horizontal text is the default setting.
     *
     * @param horizontal true if the text should be rendered horizontally
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setHorizontal(boolean horizontal)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        this.horizontal = horizontal;
    }

    /**
     * Get the state of the horizontal rendering flag. If this returns true,
     * text is rendered horizontally.
     *
     * @return true if rendering horizontally, false for vertical
     */
    public boolean isHorizontal()
    {
        return horizontal;
    }

    /**
     * Change whether the text should be placing characters left to right or
     * right to left. Left to right is the default setting.
     *
     * @param leftToRight true if the text should be rendered leftToRightly
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setLeftToRight(boolean leftToRight)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        this.leftToRight = leftToRight;
    }

    /**
     * Get the state of the left to right rendering flag. If this returns true,
     * text is rendered left to right.
     *
     * @return true if rendering left to right, false for right to left
     */
    public boolean isLeftToRight()
    {
        return leftToRight;
    }

    /**
     * Change whether the text should be placed from top to bottom or bottom
     * to top. Top to bottom is the default setting.
     *
     * @param topToBottom true if the text should be rendered top to bottom
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setTopToBottom(boolean topToBottom)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        this.topToBottom = topToBottom;
    }

    /**
     * Get the state of the top to bottom rendering flag. If this returns true,
     * text is rendered top to bottom.
     *
     * @return true if rendering top to bottom, false for bottom to top
     */
    public boolean isTopToBottom()
    {
        return topToBottom;
    }

    /**
     * Change spacing multiplier between rows of text. Spacing is used to
     * multiply the standard height between lines to generate a wider distance
     * between lines - such as double spaced text. The value must be a
     * non-zero, non-negative value.
     *
     * @param spacing true if the text should be rendered horizontally
     * @throws IllegalArgumentException The value was not positive
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setSpacing(float spacing)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(spacing <= 0)
		{
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_SPACING_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(spacing) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
		}

        spacingScale = spacing;
    }

    /**
     * Get the value of the line spacing multiplier.
     *
     * @return A value greater than zero
     */
    public float getSpacing()
    {
        return spacingScale;
    }

    /**
     * Change size multiplier of a single row of text. Size is used to adjust
     * the raw text size in units of the text. The default text size scales the
     * text to be one unit high (or wide, depending on horizontal rendering
     * setting) and this allows the text to be some other size than that. The
     * value must be a non-zero, non-negative value.
     *
     * @param size true if the text should be rendered horizontally
     * @throws IllegalArgumentException The value was not positive
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setSize(float size)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if(size <= 0)
		{
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_SIZE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Float(size) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
		}

        sizeScale = size;
    }

    /**
     * Get the value of the line size multiplier.
     *
     * @return A value greater than zero
     */
    public float getSize()
    {
        return sizeScale;
    }

    /**
     * Change the horizontal justification of the text.
     *
     * @param justify One of the JUSTIFY_ values
     * @throws IllegalArgumentException The value was not valid
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setHorizontalJustification(int justify)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if((justify < JUSTIFY_FIRST) || (justify > JUSTIFY_END))
		{
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_JUSTIFY_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Integer(justify) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
		}

        hJustification = justify;
    }

    /**
     * Get the value of the line size multiplier.
     *
     * @return A value greater than zero
     */
    public float getHorizontalJustification()
    {
        return hJustification;
    }

    /**
     * Change the vertical justification of the text.
     *
     * @param justify One of the JUSTIFY_ values
     * @throws IllegalArgumentException The value was not valid
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setVerticalJustification(int justify)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(getBoundsWriteTimingMessage());

        if((justify < JUSTIFY_FIRST) || (justify > JUSTIFY_END))
		{
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(INVALID_JUSTIFY_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { new Integer(justify) };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
		}

        vJustification = justify;
    }

    /**
     * Get the value of the line size multiplier.
     *
     * @return A value greater than zero
     */
    public float getVerticalJustification()
    {
        return vJustification;
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param txt The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(Text2D txt)
    {
        if(txt == null)
            return 1;

        if(txt == this)
            return 0;

        if(generator != txt.generator)
            return (generator.hashCode() < txt.generator.hashCode()) ? -1 : 1;

        if(text.length == txt.text.length)
        {
            for(int i = 0; i < text.length; i++)
            {
                int res = text[i].compareTo(txt.text[i]);
                if(res != 0)
                    return res;
            }
        }
        else
        {
            return text.length < txt.text.length ? -1 : 1;
        }

        if(topToBottom != txt.topToBottom)
            return topToBottom ? 1 : -1;

        if(leftToRight != txt.leftToRight)
            return leftToRight ? 1 : -1;

        if(horizontal != txt.horizontal)
            return horizontal ? 1 : -1;

        if(sizeScale != txt.sizeScale)
            return sizeScale < txt.sizeScale ? 1 : -1;

        if(spacingScale != txt.spacingScale)
            return spacingScale < txt.spacingScale ? 1 : -1;

        if(hJustification != txt.hJustification)
            return hJustification < txt.hJustification ? 1 : -1;

        if(vJustification != txt.vJustification)
            return vJustification < txt.vJustification ? 1 : -1;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param txt The Text2D instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(Text2D txt)
    {
        if(txt == this)
            return true;

        if((txt == null) ||
           (generator != txt.generator) ||
           (text.length != txt.text.length) ||
           (topToBottom != txt.topToBottom) ||
           (leftToRight != txt.leftToRight) ||
           (horizontal != txt.horizontal) ||
           (sizeScale != txt.sizeScale) ||
           (spacingScale != txt.spacingScale) ||
           (hJustification != txt.hJustification) ||
           (vJustification != txt.vJustification))
        return false;

        return true;
    }
}
