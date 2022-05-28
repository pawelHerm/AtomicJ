
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


public enum BasicAnnotationAnchor implements AnnotationAnchorCore
{
    EAST(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR), Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR), true), 
    WEST(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR), Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR), true), 
    NORTH(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR), Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR), true), 
    SOUTH(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR), Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR), true), 
    CENTER(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR), false), START(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR), true),
    END(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR), true), LABEL(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR), false);

    private final Cursor cursorVertical;
    private final Cursor cursorHorizontal;
    private final boolean onEdge;

    BasicAnnotationAnchor(Cursor cursor, boolean onEdge)
    {
        this(cursor, cursor, onEdge);
    }

    BasicAnnotationAnchor(Cursor cursorVertical, Cursor cursorHorizontal, boolean onEdge)
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
    public boolean isOnEdge() {
        return onEdge;
    }
}