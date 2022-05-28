
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

import static java.lang.Double.NaN;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

import atomicJ.utilities.GeometryUtilities;

public class ShapeFactors 
{
    private double centroidX;
    private double centroidY;
    private double area;
    private double perimeter;
    private double feretMin;
    private double feretMax;
    private double boxWidth;
    private double boxHeight;

    private ShapeFactors()
    {}

    public static ShapeFactors getShapeFactors(Rectangle2D r)
    {
        double width = r.getWidth();
        double height = r.getHeight();

        double feretMin = Math.min(width, height);
        double feretMax = Math.sqrt(width*width + height*height);

        ShapeFactors shapeFactors = new ShapeFactors();
        shapeFactors.area = width*height;
        shapeFactors.perimeter = 2*(width + height);
        shapeFactors.centroidX = r.getCenterX();
        shapeFactors.centroidY = r.getCenterY();
        shapeFactors.feretMin = feretMin;
        shapeFactors.feretMax = feretMax;
        shapeFactors.boxWidth = width;
        shapeFactors.boxHeight = height;

        return shapeFactors;
    }

    public static ShapeFactors getShapeFactors(Ellipse2D r)
    {
        double width = r.getWidth();
        double height = r.getHeight();
        double a = width/2.;
        double b = height/2.;

        double area = Math.PI*a*b;

        double h1 = (a - b)/(a + b);
        double h = h1*h1;

        //uses Ramanujan II approximation

        double perimeter = Math.PI*(a + b)*(1 + 3*h/(10 + Math.sqrt(4-3*h)));

        ShapeFactors shapeFactors = new ShapeFactors();
        shapeFactors.area = area;
        shapeFactors.perimeter = perimeter;
        shapeFactors.centroidX = r.getCenterX();
        shapeFactors.centroidY = r.getCenterY();
        shapeFactors.feretMin = Math.min(width, height);
        shapeFactors.feretMax = Math.max(width, height);
        shapeFactors.boxWidth = width;
        shapeFactors.boxHeight = height;

        return shapeFactors;
    }

    public static ShapeFactors getShapeFactorsForPolygon(Shape shape)
    {
        PathIterator it = new Area(shape).getPathIterator(null, 0);

        ShapeFactors shapeFactors = getShapeFactorsWithoutDiameters(shape, it);

        double[] feretDiameters = GeometryUtilities.getDiameters(shape.getPathIterator(null));

        shapeFactors.feretMin = feretDiameters[0];
        shapeFactors.feretMax = feretDiameters[1];

        return shapeFactors;
    }

    public static ShapeFactors getShapeFactorsForArbitraryShape(Shape shape, double flatness)
    {
        Area shapeArea = new Area(shape);
        PathIterator it = shapeArea.getPathIterator(new AffineTransform(), flatness);

        ShapeFactors shapeFactors = getShapeFactorsWithoutDiameters(shapeArea, it);

        double[] feretDiameters = GeometryUtilities.getFeretDiametersForArbitrary(shape);

        shapeFactors.feretMin = feretDiameters[0];
        shapeFactors.feretMax = feretDiameters[1];

        return shapeFactors;
    }


    private static ShapeFactors getShapeFactorsWithoutDiameters(Shape shape, PathIterator it)
    {
        double areaSigned = 0.0;
        double perimeter = 0.0;

        double centroidX = 0.0;
        double centroidY = 0.0;

        double[] coords = new double[6];

        double startX = NaN;
        double startY = NaN;

        Line2D segment = new Line2D.Double(NaN, NaN, NaN, NaN);

        while (! it.isDone()) 
        {
            int segmentType = it.currentSegment(coords);
            double x = coords[0];
            double y = coords[1];
            switch (segmentType) 
            {
            case PathIterator.SEG_CLOSE:

                segment.setLine(segment.getX2(), segment.getY2(), startX, startY);

                areaSigned = areaSigned + areaAddend(segment);
                perimeter = perimeter + perimeterAddend(segment);
                centroidX = centroidX + centroidXAddend(segment);
                centroidY = centroidY + centroidYAddend(segment);

                startX = NaN;
                startY = NaN;
                segment.setLine(NaN, NaN, NaN, NaN);
                break;

            case PathIterator.SEG_LINETO:

                segment.setLine(segment.getX2(), segment.getY2(), x, y);

                areaSigned = areaSigned + areaAddend(segment);
                perimeter = perimeter + perimeterAddend(segment);
                centroidX = centroidX + centroidXAddend(segment);
                centroidY = centroidY + centroidYAddend(segment);

                break;

            case PathIterator.SEG_MOVETO:
                startX = x;
                startY = y;
                segment.setLine(NaN, NaN, x, y);
                break;
            default:
                throw new IllegalArgumentException("PathIterator contains curved segments");
            }
            it.next();
        }        

        areaSigned = 0.5*areaSigned;
        centroidX = centroidX/(6*areaSigned);
        centroidY = centroidY/(6*areaSigned);

        Rectangle2D bounds = shape.getBounds2D();
        double boxWidth = bounds.getWidth();
        double boxHeight = bounds.getHeight();

        ShapeFactors shapeFactors = new ShapeFactors();

        shapeFactors.area = Math.abs(areaSigned);
        shapeFactors.perimeter = perimeter;
        shapeFactors.centroidX = centroidX;
        shapeFactors.centroidY = centroidY;
        shapeFactors.boxWidth = boxWidth;
        shapeFactors.boxHeight = boxHeight;

        return shapeFactors;
    }

    private static double areaAddend(Line2D s) 
    {
        double addend = s.getX1() * s.getY2() - s.getX2() * s.getY1();
        return addend;
    }

    private static double centroidXAddend(Line2D s)
    {
        double addend = (s.getX1() + s.getX2())*(s.getX1()*s.getY2() - s.getX2()*s.getY1());
        return addend;
    }

    private static double centroidYAddend(Line2D s)
    {
        double addend = (s.getY1() + s.getY2())*(s.getX1()*s.getY2() - s.getX2()*s.getY1());
        return addend;
    }

    private static double perimeterAddend(Line2D s)
    {
        double dx = s.getX2() - s.getX1();
        double dy = s.getY2() - s.getY1();

        double addend = Math.sqrt(dx*dx + dy*dy);
        return addend;
    }

    public double getArea()
    {
        return area;
    }


    public double getPerimeter()
    {
        return perimeter;
    }


    public double getCentroidX()
    {
        return centroidX;
    }


    public double getCentroidY()
    {
        return centroidY;
    }



    public double getFeretMin()
    {
        return feretMin;
    }


    public double getFeretMax()
    {
        return feretMax;
    }

    public double getBoxWidth()
    {
        return boxWidth;
    }

    public double getBoxHeight()
    {
        return boxHeight;
    }

    public double getCircularity()
    {
        double circularity = 4*Math.PI*area/(perimeter*perimeter);
        return circularity;
    }
}
