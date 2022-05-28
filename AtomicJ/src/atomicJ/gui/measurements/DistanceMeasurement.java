
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

package atomicJ.gui.measurements;


import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.Identifiable;
import atomicJ.gui.ModifierKey;

public interface DistanceMeasurement extends Identifiable
{
    public boolean isComplex();
    public boolean isClicked(Rectangle2D dataRectangle);
    public boolean isBoundaryClicked(Rectangle2D dataRectangle);
    public Shape getDistanceShape();
    public DistanceShapeFactors getDistanceShapeFactors();
    public Object getKey();
    public String getLabel();
    public DistanceMeasurement copy();
    //returns true if ROI was modified
    public abstract boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle);
}

