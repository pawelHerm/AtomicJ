
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
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class GradientPaint implements Paint
{
    private final CustomContext context;
    private final ColorGradient gradient;

    public GradientPaint(ColorGradient gradient) 
    {
        this.gradient = gradient;
        this.context = new CustomContext(gradient);
    }

    public ColorGradient getGradient()
    {
        return gradient;
    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform transform, RenderingHints hints)
    {
        context.setUserBounds(userBounds.getX(), userBounds.getWidth(), transform);
        return context;
    }

    @Override
    public int getTransparency()
    {
        return OPAQUE;
    }  

    private static class CustomContext implements PaintContext 
    {
        private final ColorGradient table;
        private double userWidth;
        private double userX;
        private AffineTransform inverseTransform;

        public CustomContext(ColorGradient table)
        {
            this.table = table;
        }

        private void setUserBounds(double userOffset, double userWidth, AffineTransform transform)
        {
            this.userWidth = userWidth;
            this.userX = userOffset;

            try {
                this.inverseTransform = transform.createInverse();
            } catch (NoninvertibleTransformException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void dispose() {}

        @Override
        public ColorModel getColorModel() 
        {
            return ColorModel.getRGBdefault();
        }

        private double getValue(Point2D p)
        {
            Point2D transformedPoint = inverseTransform.transform(p, null);
            double user_delta_x = transformedPoint.getX() - userX;

            double value = user_delta_x/userWidth;
            return value;
        }

        @Override
        public Raster getRaster(int xOffset, int yOffset, int w, int h) 
        {
            WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);

            for (int j = 0; j < h; j++) 
            {
                for (int i = 0; i < w; i++) 
                {
                    double x = (i + xOffset);
                    double y = (j + yOffset);

                    double fraction = Math.max(0, Math.min(1,getValue(new Point2D.Double(x, y))));

                    Color color = table.getColor(fraction);         	            	   
                    raster.setPixel(i, j, new int[] {color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()});
                } 
            } 
            return raster;
        }	
    } 	
}




