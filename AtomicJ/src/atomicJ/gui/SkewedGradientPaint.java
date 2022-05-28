
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

public class SkewedGradientPaint extends GradientPaint
{
    private final ColorGradient gradient;
    private final CustomContext context;
    private final RectangleCorner origin;
    private final double angle;

    public SkewedGradientPaint(ColorGradient gradient, RectangleCorner origin, double angle) 
    {
        super(gradient);
        this.gradient = gradient;
        this.origin = origin;
        this.angle = angle;
        this.context = new CustomContext(gradient, origin, angle);
    }

    public RectangleCorner getGradientOrigin()
    {
        return origin;
    }

    public double getAngle()
    {
        return angle;
    }

    @Override
    public ColorGradient getGradient()
    {
        return gradient;
    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints)
    {
        context.setBounds(deviceBounds, userBounds, xform);
        return context;
    }

    @Override
    public int getTransparency()
    {
        return BITMASK;
    }  

    private static class CustomContext implements PaintContext 
    {
        private final ColorGradient table;
        private final RectangleCorner corner;
        private final double angle;
        private double correctedAngle;
        private double userWidth;
        private double userHeight;
        private double userX;
        private double userY;
        private double grX = 0;
        private double grY = 0;

        private double grU0;
        private double grU1;

        private double gradientLength;

        private AffineTransform transform;

        public CustomContext(ColorGradient table, RectangleCorner corner, double angle)
        {
            this.table = table;
            this.corner = corner;
            this.angle = angle;

            if(RectangleCorner.UPPER_LEFT.equals(corner))
            {
                correctedAngle = angle;
            }
            else if(RectangleCorner.UPPER_RIGHT.equals(corner))
            {
                correctedAngle = angle + Math.PI/2.;
            }
            else if(RectangleCorner.BOTTOM_LEFT.equals(corner))
            {
                correctedAngle = -angle;
            }
            else if(RectangleCorner.BOTTOM_RIGHT.equals(corner))
            {
                correctedAngle = angle + Math.PI;
            }			 
        }

        void setBounds(Rectangle2D deviceBounds, Rectangle2D userBounds, AffineTransform transform)
        {
            this.userWidth = userBounds.getWidth();
            this.userHeight = userBounds.getHeight();
            this.userX = userBounds.getX();
            this.userY = userBounds.getY();

            try {
                this.transform = transform.createInverse();
            } catch (NoninvertibleTransformException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            updateGradientLength();
        }

        private void updateGradientLength()
        {
            grX = RectangleCorner.UPPER_LEFT.equals(corner) || RectangleCorner.BOTTOM_LEFT.equals(corner)
                    ? 0 : (userWidth);
            grY = RectangleCorner.UPPER_LEFT.equals(corner) || RectangleCorner.UPPER_RIGHT.equals(corner)
                    ? 0 : (userHeight);

            double x1 = RectangleCorner.UPPER_LEFT.equals(corner) || RectangleCorner.BOTTOM_LEFT.equals(corner)
                    ? userWidth : 0;
            double y1 = RectangleCorner.UPPER_LEFT.equals(corner) || RectangleCorner.UPPER_RIGHT.equals(corner)
                    ? userHeight : 0;

            grU0 = Math.cos(correctedAngle);
            grU1 = Math.sin(correctedAngle);

            gradientLength = Math.abs(grU0*(grX - x1) + grU1*(grY - y1));		   		   	   
        }

        private double getValue(Point2D p)
        {
            Point2D transformed = transform.transform(p, null);
            double x_delta = transformed.getX() - userX;
            double y_delta = transformed.getY() - userY;

            double value =  Math.abs((grU0*(grX - x_delta) + grU1*(grY - y_delta)))/(gradientLength);		   		   
            return value;
        }

        @Override
        public void dispose() {}

        @Override
        public synchronized ColorModel getColorModel() 
        {
            return ColorModel.getRGBdefault();
        }

        @Override
        public synchronized Raster getRaster(int xOffset, int yOffset, int w, int h) 
        {
            WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);

            for (int j = 0; j < h; j++) 
            {
                for (int i = 0; i < w; i++) 
                {
                    double x = (i + xOffset);
                    double y = (j + yOffset);

                    double fraction = Math.min(1,getValue(new Point2D.Double(x, y)));

                    Color color = table.getColor(fraction);

                    raster.setPixel(i, j, new int[] {color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()});
                } 
            } 
            return raster;
        }	
    } 	
}




