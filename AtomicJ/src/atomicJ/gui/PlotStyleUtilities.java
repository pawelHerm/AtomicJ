
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

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

public class PlotStyleUtilities  
{
    private static final Path2D star = new GeneralPath();
    private static final Path2D heart = new GeneralPath();
    private static final Path2D ex = new GeneralPath();
    private static final Path2D cross = new GeneralPath();
    private static final Path2D starThin = new GeneralPath();
    private static final Path2D starThick = new GeneralPath();


    static 
    {
        star.moveTo(15.7588, 13.3681);
        double[][] coordinates = {{17.7038, 20.4562}, {11.8604, 15.9976}, {5.72025, 20.0377}, {8.15495, 
            13.1025}, {2.41514, 8.51139}, {9.76323, 8.68385}, {12.356, 
                1.80621}, {14.4626, 8.84796}, {21.8049, 9.1885}};
        for(double[] p : coordinates)
        {
            star.lineTo(p[0], p[1]);
        }
        star.closePath();

        heart.moveTo(10, 20);
        heart.curveTo(10, 16.08, 18.74, 13.77, 19.25, 5.82) ;
        heart.curveTo(18.53, -0.9, 10, -2.1, 10, 
                6.42);
        heart.curveTo(10, -1.21, 1.47, -0.9, 0.75, 5.82);
        heart.curveTo(1.26, 13.77, 10, 16.08,
                10, 20);
        heart.closePath();

        ex.moveTo(4,16);
        ex.lineTo(16, 4);
        ex.moveTo(4, 4);
        ex.lineTo(16,16);

        cross.moveTo(10,4);
        cross.lineTo(10, 16);
        cross.moveTo(4, 10);
        cross.lineTo(16, 10);

        for(int i = 0; i<4;i++)
        {
            double angle = i*45*Math.PI/180.;
            starThin.moveTo(-6*Math.cos(angle) + 10, -6*Math.sin(angle) + 10);
            starThin.lineTo(6*Math.cos(angle) + 10, 6*Math.sin(angle) + 10);
        }
        for(int i = 0; i<6;i++)
        {
            double angle = i*30*Math.PI/180.;
            starThick.moveTo(-6*Math.cos(angle) + 10, -6*Math.sin(angle) + 10);
            starThick.lineTo(6*Math.cos(angle) + 10, 6*Math.sin(angle) + 10);
        }
    } 

    private static final Shape[] nonZeroAreaShapes = 
        {
                new Rectangle2D.Double(4,4,16,16),
                new Ellipse2D.Double(4,4,16,16),
                new Polygon(new int[] {4,12,20}, new int[] {20,4,20}, 3),
                new Polygon(new int[] {4,12,20,12}, new  int[]  {12,4,12,20},4),
                new Rectangle2D.Double(4,8,16,8),
                new Polygon(new int[] {4,20,12}, new int[] {4,4,20}, 3),
                new Ellipse2D.Double(4,8,16,8),
                new Polygon(new int[] {4,20,4}, new int[] {4,12,20},3),
                new Rectangle2D.Double(8,4,8,16),
                new Polygon(new int[] {4,20,20}, new int[] {12,4,20},3),
                star,
                heart
        };

    private static final Shape[] allMarkerShapes = 
        {
                new Rectangle2D.Double(4,4,16,16),
                new Ellipse2D.Double(4,4,16,16),
                new Polygon(new int[] {4,12,20}, new int[] {20,4,20}, 3),
                new Polygon(new int[] {4,12,20,12}, new  int[]  {12,4,12,20},4),
                new Rectangle2D.Double(4,8,16,8),
                new Polygon(new int[] {4,20,12}, new int[] {4,4,20}, 3),
                new Ellipse2D.Double(4,8,16,8),
                new Polygon(new int[] {4,20,4}, new int[] {4,12,20},3),
                new Rectangle2D.Double(8,4,8,16),
                new Polygon(new int[] {4,20,20}, new int[] {12,4,20},3),
                star,
                heart,
                cross,
                ex,
                starThin,
                starThick
        };


    public static Shape[] getAllShapes()
    {
        int n = allMarkerShapes.length;
        return Arrays.copyOf(allMarkerShapes,n);
    }

    public static Shape[] getNonZeroAreaShapes()
    {
        int n = nonZeroAreaShapes.length;
        return Arrays.copyOf(nonZeroAreaShapes,n);
    }
}
