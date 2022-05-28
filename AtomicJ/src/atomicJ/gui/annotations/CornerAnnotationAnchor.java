
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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;


public enum CornerAnnotationAnchor implements AnnotationAnchorCore
{
    NORTHEAST(EdgeAnnotationAnchor.NORTH, EdgeAnnotationAnchor.EAST, Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR), true)
    {
        @Override
        public boolean isCornerCaught(Point2D nwPoint, Point2D nePoint, Point2D sePoint, Point2D swPoint, Rectangle2D probingRectangle)
        {
            return probingRectangle.contains(nePoint);
        }

        @Override
        public CornerAnnotationAnchor getOppositeCorner() 
        {
            return SOUTHWEST;
        }
    },

    NORTHWEST(EdgeAnnotationAnchor.NORTH, EdgeAnnotationAnchor.WEST, Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR), true) 
    {
        @Override
        public boolean isCornerCaught(Point2D nwPoint, Point2D nePoint, Point2D sePoint, Point2D swPoint, Rectangle2D probingRectangle)
        {
            return probingRectangle.contains(nwPoint);
        }

        @Override
        public CornerAnnotationAnchor getOppositeCorner()
        {
            return SOUTHEAST;
        }
    },

    SOUTHEAST(EdgeAnnotationAnchor.SOUTH, EdgeAnnotationAnchor.EAST, Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR), true) {
        @Override
        public boolean isCornerCaught(Point2D nwPoint, Point2D nePoint,
                Point2D sePoint, Point2D swPoint, Rectangle2D probingRectangle) {
            return probingRectangle.contains(sePoint);
        }

        @Override
        public CornerAnnotationAnchor getOppositeCorner() 
        {
            return NORTHWEST;
        }
    }, 
    SOUTHWEST(EdgeAnnotationAnchor.SOUTH, EdgeAnnotationAnchor.WEST,Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR), true) {
        @Override
        public boolean isCornerCaught(Point2D nwPoint, Point2D nePoint,
                Point2D sePoint, Point2D swPoint, Rectangle2D probingRectangle) {
            return probingRectangle.contains(swPoint);
        }

        @Override
        public CornerAnnotationAnchor getOppositeCorner() 
        {
            return NORTHEAST;
        }
    };

    private final Cursor cursorVertical;
    private final Cursor cursorHorizontal;
    private final EdgeAnnotationAnchor edgeY;
    private final EdgeAnnotationAnchor edgeX;
    private final boolean onEdge;

    CornerAnnotationAnchor(EdgeAnnotationAnchor edgeY, EdgeAnnotationAnchor edgeX, Cursor cursor, boolean onEdge)
    {
        this(edgeY, edgeX, cursor, cursor, onEdge);
    }

    CornerAnnotationAnchor(EdgeAnnotationAnchor edgeY, EdgeAnnotationAnchor edgeX, Cursor cursorVertical, Cursor cursorHorizontal, boolean onEdge)
    {
        this.edgeY = edgeY;
        this.edgeX = edgeX;
        this.cursorVertical = cursorVertical;
        this.cursorHorizontal = cursorHorizontal;
        this.onEdge = onEdge;
    }

    public EdgeAnnotationAnchor getEdgeY()
    {
        return edgeY;
    }

    public EdgeAnnotationAnchor getEdgeX()
    {
        return edgeX;
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

    public abstract CornerAnnotationAnchor getOppositeCorner();

    public abstract boolean isCornerCaught(Point2D nwPoint, Point2D nePoint, Point2D sePoint, Point2D swPoint, Rectangle2D probingRectangle);

    public static CornerAnnotationAnchor getInstanceFor(EdgeAnnotationAnchor firstEdge, EdgeAnnotationAnchor secondEdge)
    {
        for(CornerAnnotationAnchor corner : CornerAnnotationAnchor.values())
        {
            if(Objects.equals(corner.edgeX, firstEdge)  && Objects.equals(corner.edgeY, secondEdge))
            {
                return corner;
            }
        }

        throw new IllegalArgumentException("No corner known for edges " + firstEdge + " and " + secondEdge);
    }
}