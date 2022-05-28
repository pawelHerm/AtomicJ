
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;

public class PaintSampleFlexible extends JComponent 
{
    private static final long serialVersionUID = 1L;
    private static final Paint paintDisabled = Color.lightGray;

    private Paint paint;
    private final Dimension preferredSize = new Dimension(80, 12);

    public PaintSampleFlexible() 
    {
        this(Color.white);
    }

    public PaintSampleFlexible(Paint paint) 
    {
        this.paint = paint;
    }

    public Paint getPaint() 
    {
        return this.paint;
    }

    public void setPaint(final Paint paint) 
    {
        this.paint = paint;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return this.preferredSize;
    }

    @Override
    public void paintComponent(final Graphics g) {

        final Graphics2D g2 = (Graphics2D) g;
        final Dimension size = getSize();
        final Insets insets = getInsets();
        final double xx = insets.left;
        final double yy = insets.top;
        final double ww = size.getWidth() - insets.left - insets.right - 1;
        final double hh = size.getHeight() - insets.top - insets.bottom - 1;
        final Rectangle2D area = new Rectangle2D.Double(xx, yy, ww, hh);
        Paint paint = isEnabled() ? this.paint : paintDisabled;

        g2.setPaint(paint);
        g2.fill(area);
        g2.setPaint(Color.black);
        g2.draw(area);
    }
}
