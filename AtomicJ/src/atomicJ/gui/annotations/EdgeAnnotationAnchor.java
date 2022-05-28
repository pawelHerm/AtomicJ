
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

package atomicJ.gui.annotations;

import java.awt.Cursor;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
import java.util.EnumSet;
import java.util.Set;

import atomicJ.utilities.GeometryUtilities;


public enum EdgeAnnotationAnchor implements AnnotationAnchorCore
{
    EAST(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR), Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR), true) 
    {
        @Override
        public double getDistanceFromEdge(Point2D point, Point2D nwPoint, Point2D nePoint, Point2D sePoint, Point2D swPoint) 
        {
            return GeometryUtilities.getDistance(sePoint, nePoint, point);
        }

        @Override
        public double getPerpendicularEdgeLength(double width, double height)
        {
            return width;
        }

        @Override
        public double getParrallelLength(double width, double height)
        {
            return height;
        }

        @Override
        public double getMovementDistance(RectangularShape rShape, Point2D p)
        {
            double positionOld = rShape.getMaxX();
            double positionNew = p.getX();

            double movement = positionNew - positionOld;

            return movement;
        }

        @Override
        public EdgeAnnotationAnchor moveEdge(RectangularShape rShape, double distance, double minimalDimension)
        {
            double xOld = rShape.getX();
            double widthOld = rShape.getWidth();

            double yOld = rShape.getY();
            double height = rShape.getHeight();

            double widthNew = widthOld + distance;
            if(widthNew > minimalDimension)
            {
                rShape.setFrame(xOld, yOld, widthNew, height);
                return EAST;
            }
            else
            {
                rShape.setFrame(xOld + widthOld + distance, yOld, -distance, height);
                return WEST;
            }
        }


        @Override
        public AffineTransform getEdgeMovementTransformation(Point2D nwPoint, Point2D nePoint, Point2D sePoint, Point2D swPoint, Point2D p, double minimalDimension)
        {
            return moveEdge(sePoint, sePoint, swPoint, swPoint, nwPoint, p);
        }

        @Override
        public EdgeAnnotationAnchor getOppositeEdge()
        {
            return WEST;
        }

        @Override
        public Set<EdgeAnnotationAnchor> getOrthogonalEdges() 
        {
            return EnumSet.of(SOUTH, NORTH);
        }         
    }, 

    WEST(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR), Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR), true)
    {
        @Override
        public double getDistanceFromEdge(Point2D point, Point2D nwPoint, Point2D nePoint, Point2D sePoint, Point2D swPoint) 
        {
            return GeometryUtilities.getDistance(swPoint, nwPoint, point);
        }

        @Override
        public double getPerpendicularEdgeLength(double width, double height)
        {
            return width;
        }

        @Override
        public double getParrallelLength(double width, double height)
        {
            return height;
        }

        @Override
        public EdgeAnnotationAnchor moveEdge(RectangularShape rShape, double distance, double minimalDimension)
        {
            double xOld = rShape.getX();
            double widthOld = rShape.getWidth();

            double y = rShape.getY();
            double height = rShape.getHeight();

            double xNew = xOld - distance;
            double widthNew = widthOld + distance;
            if(widthNew > minimalDimension)
            {
                rShape.setFrame(xNew, y, widthNew, height);
                return WEST;
            }
            else
            {
                rShape.setFrame(xOld, y, xNew - xOld, height);
                return EAST;
            }
        }

        @Override
        public double getMovementDistance(RectangularShape rShape, Point2D p)
        {
            double positionOld = rShape.getMinX();
            double positionNew = p.getX();

            double movement = -(positionNew - positionOld);

            return movement;
        }

        @Override
        public AffineTransform getEdgeMovementTransformation(Point2D nwPoint, Point2D nePoint, Point2D sePoint, Point2D swPoint, Point2D p, double minimalDimension)
        {
            return moveEdge(swPoint, swPoint, sePoint, sePoint, nePoint, p);
        }

        @Override
        public EdgeAnnotationAnchor getOppositeEdge()
        {
            return EAST;
        }

        @Override
        public Set<EdgeAnnotationAnchor> getOrthogonalEdges() 
        {
            return EnumSet.of(SOUTH, NORTH);
        }
    }, 

    NORTH(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR), Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR), true) 
    {
        @Override
        public double getDistanceFromEdge(Point2D point, Point2D nwPoint, Point2D nePoint, Point2D sePoint, Point2D swPoint) 
        {
            return GeometryUtilities.getDistance(nwPoint, nePoint, point);
        }

        @Override
        public double getPerpendicularEdgeLength(double width, double height)
        {
            return height;
        }

        @Override
        public double getParrallelLength(double width, double height)
        {
            return width;
        }

        @Override
        public double getMovementDistance(RectangularShape rShape, Point2D p)
        {
            double positionOld = rShape.getMaxY();
            double positionNew = p.getY();

            double movement = positionNew - positionOld;

            return movement;
        }

        @Override
        public EdgeAnnotationAnchor moveEdge(RectangularShape rShape, double distance, double minimalDimension)
        {           
            double xOld = rShape.getX();
            double yOld = rShape.getY();
            double width = rShape.getWidth();
            double heightOld = rShape.getHeight();

            double heightNew = heightOld + distance;

            if(heightNew > minimalDimension)
            {
                rShape.setFrame(xOld, yOld, width, heightNew);
                return NORTH;
            }
            else
            {
                rShape.setFrame(xOld, yOld + heightOld + distance, width, -distance);
                return SOUTH;
            }        
        }

        @Override
        public AffineTransform getEdgeMovementTransformation(Point2D nwPoint, Point2D nePoint, Point2D sePoint, Point2D swPoint, Point2D p, double minimalDimension)
        {
            return moveEdge(nePoint, nePoint, sePoint, sePoint, swPoint, p);
        }

        @Override
        public EdgeAnnotationAnchor getOppositeEdge()
        {
            return SOUTH;
        }

        @Override
        public Set<EdgeAnnotationAnchor> getOrthogonalEdges()
        {
            return EnumSet.of(EAST, WEST);
        }
    }, 

    SOUTH(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR), Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR), true) {
        @Override
        public double getDistanceFromEdge(Point2D point, Point2D nwPoint,
                Point2D nePoint, Point2D sePoint, Point2D swPoint) 
        {
            return GeometryUtilities.getDistance(swPoint, sePoint, point);
        }

        @Override
        public double getPerpendicularEdgeLength(double width, double height)
        {
            return height;
        }      

        @Override
        public double getParrallelLength(double width, double height)
        {
            return width;
        }

        @Override
        public EdgeAnnotationAnchor moveEdge(RectangularShape rShape, double distance, double minimalDimension) 
        {
            double heightOld = rShape.getHeight();
            double yOld = rShape.getY();

            double xOld = rShape.getX();
            double width = rShape.getWidth();

            double yNew = yOld - distance;
            double heightNew = heightOld + distance;

            if(heightNew > minimalDimension)
            {
                rShape.setFrame(xOld, yNew, width, heightNew);
                return SOUTH;
            }
            else
            {                
                rShape.setFrame(xOld, yOld, width, yNew - yOld);
                return NORTH;
            }          
        }

        @Override
        public double getMovementDistance(RectangularShape rShape, Point2D p)
        {
            double positionOld = rShape.getMinY();
            double positionNew = p.getY();

            double movement = -(positionNew - positionOld);

            return movement;
        }

        @Override
        public AffineTransform getEdgeMovementTransformation(Point2D nwPoint, Point2D nePoint, Point2D sePoint, Point2D swPoint, Point2D p, double minimalDimension)
        {
            return moveEdge(swPoint, swPoint, nwPoint, nwPoint, nePoint, p);
        }

        @Override
        public EdgeAnnotationAnchor getOppositeEdge()
        {
            return NORTH;
        }

        @Override
        public Set<EdgeAnnotationAnchor> getOrthogonalEdges() 
        {
            return EnumSet.of(EAST, WEST);
        }
    };

    private final Cursor cursorVertical;
    private final Cursor cursorHorizontal;
    private final boolean onEdge;

    EdgeAnnotationAnchor(Cursor cursor, boolean onEdge)
    {
        this(cursor, cursor, onEdge);
    }

    EdgeAnnotationAnchor(Cursor cursorVertical, Cursor cursorHorizontal, boolean onEdge)
    {
        this.cursorVertical = cursorVertical;
        this.cursorHorizontal = cursorHorizontal;
        this.onEdge = onEdge;
    }

    @Override
    public Cursor getCursor(boolean isVertical)
    {
        Cursor cursor = isVertical ? cursorVertical : cursorHorizontal;
        return cursor;
    }

    @Override
    public boolean isOnEdge()
    {
        return onEdge;
    }

    public abstract EdgeAnnotationAnchor getOppositeEdge();
    public abstract Set<EdgeAnnotationAnchor> getOrthogonalEdges();

    public abstract double getDistanceFromEdge(Point2D point, Point2D nwPoint, Point2D nePoint, Point2D sePoint, Point2D swPoint);
    public abstract double getPerpendicularEdgeLength(double width, double height);
    public abstract double getParrallelLength(double width, double height);

    //returns true if the movement changes the caught edge, e.g. when we move the north edge below the southern edge,
    //it return true, because then we need to switch caught edge
    public AnnotationAnchorCore moveEdge(RectangularShape rShape, Point2D p, double minimalDimension) 
    {
        double distance = getMovementDistance(rShape, p);
        return moveEdge(rShape, distance, minimalDimension);    
    }

    public abstract AffineTransform getEdgeMovementTransformation(Point2D nwPoint, Point2D nePoint, Point2D sePoint, Point2D swPoint, Point2D p, double minimalDimension);

    public abstract double getMovementDistance(RectangularShape rShape, Point2D p);
    public abstract EdgeAnnotationAnchor moveEdge(RectangularShape rShape, double distance, double minimalDimension);

    private static AffineTransform moveEdge(Point2D movedPoint, Point2D vStart, Point2D vEnd, Point2D uStart, Point2D uEnd, Point2D edgeFinalPoint)
    {
        double sx = movedPoint.getX();
        double sy = movedPoint.getY();

        double vx = vEnd.getX() - vStart.getX();
        double vy = vEnd.getY() - vStart.getY();

        double ux = uEnd.getX() - uStart.getX();
        double uy = uEnd.getY() - uStart.getY();

        AffineTransform tr = buildEdgeMovementAffineTransform(ux, uy, vx, vy, sx, sy, edgeFinalPoint.getX(), edgeFinalPoint.getY());         
        return tr;
    }

    public static AffineTransform buildEdgeMovementAffineTransform(double ux, double uy, double vx, double vy, double sx, double sy, double px, double py)
    {       
        double denominator = uy*vx - ux*vy;

        double t = (py*ux - sy*ux - px*uy + sx*uy)/denominator;

        double m00 = (uy*vx + t*uy*vx - ux*vy)/denominator;         
        double m01 = -t*ux*vx/denominator;

        double a13Numerator = sy*t*ux*vx - sx*t*uy*vx - t*uy*vx*vx + t*ux*vx*vy;
        double m02 = a13Numerator/denominator;

        double m10 = t*uy*vy/denominator;            
        double m11 = 1 + t - (t*uy*vx)/denominator;
        double m12 = -sy*t - t*vy - (t*uy*(-sy*vx + sx*vy))/denominator;

        AffineTransform tr = new AffineTransform(m00, m10, m01, m11, m02, m12);

        return tr;
    }  
}