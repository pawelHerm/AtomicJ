
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
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import javax.swing.JPanel;

public class StraightStrokeSample extends JPanel
{
    private static final long serialVersionUID = 1L;

    private static final int PREFERRED_WIDTH = 100;
    private static final int PREFERRED_HEIGHT = 20;
    private static final int PREFERRED_X_PADDING = 5;
    private static final int PREFERRED_Y_PADDING = 3;
    private static final Paint paintDisabled = Color.lightGray;

    private Stroke stroke;
    private Paint strokePaint = Color.black;
    private Dimension preferredSize;

    public StraightStrokeSample() 
    {
        this.stroke = null;
        updatePrefrredSize();
    }

    public StraightStrokeSample(Stroke stroke) 
    {
        this.stroke = stroke;
        updatePrefrredSize();
    }

    public Paint getStrokePaint()
    {
        return strokePaint;
    }

    public void setStrokePaint(Paint paint)
    {
        this.strokePaint = paint;
        repaint();
    }

    private void updatePrefrredSize()
    {
        int preferredWidth = PREFERRED_WIDTH + 2*PREFERRED_X_PADDING;
        int preferredHeigt = PREFERRED_HEIGHT + 2*PREFERRED_Y_PADDING;
        this.preferredSize = new Dimension(preferredWidth, preferredHeigt);
    }

    public Stroke getStroke() 
    {
        return this.stroke;
    }

    public void setStroke(Stroke stroke) 
    {
        this.stroke = stroke;
        updatePrefrredSize();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() 
    {
        return this.preferredSize;
    }

    @Override
    public Dimension getMinimumSize() 
    {
        return this.preferredSize;
    }

    @Override
    public void paintComponent(Graphics g) 
    {  
        Graphics2D g2 = (Graphics2D) g;
        super.paintComponent(g2);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Dimension size = getSize();
        Insets insets = getInsets();

        double ww = size.getWidth() - insets.left - insets.right - 2*PREFERRED_X_PADDING;
        double hh = size.getHeight() - insets.top - insets.bottom;

        double xx = insets.left + PREFERRED_X_PADDING;
        double yy = insets.top + hh/2.;     

        GeneralPath path = new GeneralPath();
        path.moveTo(xx, yy);
        path.lineTo(xx + ww, yy);

        Paint paint = isEnabled() ? this.strokePaint : paintDisabled;

        if (this.stroke != null) 
        {
            g2.setPaint(paint);
            g2.setStroke(this.stroke);
            g2.draw(path);
        }
        g2.dispose();
    }
}
