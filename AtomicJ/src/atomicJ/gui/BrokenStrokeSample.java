
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

import java.awt.BasicStroke;
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

public class BrokenStrokeSample extends JPanel
{
    private static final long serialVersionUID = 1L;

    private static final int PREFERRED_STROKE_WIDTH = 140;
    private static final int PREFERRED_STROKE_HEIGHT = 40;
    private static final int PREFERRED_X_PADDING = 14;
    private static final int PREFERRED_Y_PADDING = 10;

    private BasicStroke stroke;
    private Paint strokePaint = Color.black;
    private Dimension preferredSize;

    public BrokenStrokeSample() 
    {
        this(null);
    }

    public BrokenStrokeSample(BasicStroke stroke) 
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
        int additionalSpace = this.stroke == null ? 0 : (int)Math.max(0, 1.7*stroke.getLineWidth() - PREFERRED_X_PADDING); 
        int preferredWidth = PREFERRED_STROKE_WIDTH + 2*PREFERRED_X_PADDING + additionalSpace;
        int preferredHeigt = PREFERRED_STROKE_HEIGHT + 2*PREFERRED_Y_PADDING + additionalSpace;
        this.preferredSize = new Dimension(preferredWidth, preferredHeigt);
    }

    public Stroke getStroke() 
    {
        return this.stroke;
    }

    public void setStroke(BasicStroke stroke) 
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

        double availableWidth = size.getWidth() - insets.left - insets.right;
        double availableHeight = size.getHeight() - insets.top - insets.bottom;

        double additionalPadding = this.stroke == null ? 0: Math.max(0, stroke.getLineWidth()*.6 - 12);
        double padding = PREFERRED_X_PADDING + additionalPadding;

        double xx = insets.left + padding;
        double yy = insets.top + padding;     

        double ww = availableWidth - 2*padding;

        double hh = Math.max(0,availableHeight - 2*padding);

        GeneralPath path = new GeneralPath();
        path.moveTo(xx, yy);
        path.lineTo(xx + 0.45*ww, yy + hh);
        path.lineTo(xx + 0.55*ww, yy);
        path.lineTo(xx + ww, yy + hh);

        if (this.stroke != null) 
        {
            g2.setPaint(strokePaint);
            g2.setStroke(this.stroke);
            g2.draw(path);
        }
        g2.dispose();
    }
}
