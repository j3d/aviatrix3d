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

package org.j3d.renderer.aviatrix3d.pipeline;

import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.j3d.util.I18nManager;

import org.j3d.aviatrix3d.Viewport;

/**
 * Internal data holder for the layout management.
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>negWidthMsg: Error message when the width is less than zero</li>
 * <li>negHeightMsg: Error message when the height is less than zero</li>
 * </ul>
 *
 * @author justin
 */
class ViewportLayoutData
{
    /** User provided width length was not positive */
    private static final String NEG_WIDTH_PROP =
        "org.j3d.renderer.aviatrix3d.pipeline.ViewportLayoutData.negWidthMsg";

    /** User provided width length was not positive */
    private static final String NEG_HEIGHT_PROP =
        "org.j3d.renderer.aviatrix3d.pipeline.ViewportLayoutData.negHeightMsg";

    /** The starting percentage [0, 1] horizontally */
    final float startX;

    /** The starting percentage [0, 1] vertically */
    final float startY;

    /** The percentage [0, 1] of the width taken up */
    final float width;

    /** The percentage [0, 1] of the height taken up */
    final float height;

    final Viewport viewport;

    ViewportLayoutData(float startX, float startY, float width, float height, Viewport viewport)
    {
        if(width < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NEG_WIDTH_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { width };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        if(height < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(NEG_HEIGHT_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

            Object[] msg_args = { height };
            Format[] fmts = { n_fmt };
            MessageFormat msg_fmt =
                new MessageFormat(msg_pattern, lcl);
            msg_fmt.setFormats(fmts);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }

        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.viewport = viewport;
    }
}
