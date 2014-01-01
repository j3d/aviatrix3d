/*
 * **************************************************************************
 *                        Copyright j3d.org (c) 2000 - ${year}
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read docs/lgpl.txt for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * **************************************************************************
 */

package org.j3d.aviatrix3d;

// External imports
import java.util.List;

// Local imports
// None

/**
 * Abstract interface allowing the end user to have a selection of graphics
 * capabilities that they can choose from if the operating system does not
 * exactly support the one that you've asked for in your instance of
 * {@link org.j3d.aviatrix3d.GraphicsRenderingCapabilities}
 *
 * @author justin
 */
public interface GraphicsRenderingCapabilitiesChooser
{
    /**
     * Chooses the index (0..available.length - 1) of the {@link org.j3d.aviatrix3d.GraphicsRenderingCapabilities}
     * most closely matching the desired one from the list of all supported. Some of the entries in the
     * <code>available</code> array may be null; the chooser must ignore these.
     * <p/>
     * The <em>windowSystemRecommendedChoice</em> parameter may be provided to the chooser by the underlying
     * window system; if this index is valid, it is recommended, but not necessarily required, that the chooser
     * select that entry.
     * <p/>
     * <em>Note:</em> this method is called automatically by the system when an instance is passed to
     * it regardless of whether it needs to be called or not (ie there's only one available to it).
     * It should generally not be invoked by users directly, unless it is desired to delegate the
     * choice to some other CapabilitiesChooser object.
     *
     * @param desired The capabilities that the end user requested
     * @param available The list that the operating system has determined is available
     * @param windowSystemRecommendedChoice The index of the recommended choice in the available list
     * @return A value between 0 and available.size() - 1 indicating the designed capabilities
     */
    public int chooseCapabilities(GraphicsRenderingCapabilities desired,
                                  List<GraphicsRenderingCapabilities> available,
                                  int windowSystemRecommendedChoice);
}
