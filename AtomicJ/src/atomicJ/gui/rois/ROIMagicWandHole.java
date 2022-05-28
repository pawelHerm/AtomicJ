
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

package atomicJ.gui.rois;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.util.Set;

import atomicJ.gui.ModifierKey;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;


public class ROIMagicWandHole extends ROIPolygon
{
    private static final long serialVersionUID = 1L;

    private final Area datasetArea;

    public ROIMagicWandHole(Area datasetArea, Path2D shape, Integer key, ROIStyle style) 
    {
        super(shape, key, style);

        this.datasetArea = datasetArea;
    }

    public ROIMagicWandHole(Area datasetArea, Path2D shape, Integer key, String label, ROIStyle style) 
    {
        super(shape, key, label, style);

        this.datasetArea = datasetArea;
    }

    public ROIMagicWandHole(ROIMagicWandHole that)
    {
        this(that, that.getStyle()); 
    }

    public ROIMagicWandHole(ROIMagicWandHole that, ROIStyle style)
    {
        super(that, style); 

        this.datasetArea = new Area(that.datasetArea);
    }

    public ROIMagicWandHole(ROIMagicWandHole that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label); 

        this.datasetArea = new Area(that.datasetArea);
    }


    @Override
    public ROIMagicWandHole copy()
    {
        return new ROIMagicWandHole(this);
    }

    @Override
    public ROIMagicWandHole copy(ROIStyle style)
    {
        return new ROIMagicWandHole(this, style);
    }

    @Override
    public ROIMagicWandHole copy(ROIStyle style, Integer key, String label)
    {
        return new ROIMagicWandHole(this, style, key, label);
    }

    @Override
    protected void lineTemporailyTo(double x, double y, boolean notify)
    {
        lineTo(x, y, notify);
    }

    @Override
    public Path2D getModifiableShape()
    {
        return super.getROIShape();
    }

    @Override
    public void mouseMovedDuringConstruction(double x, double y, Set<ModifierKey> modifiers)
    {
        if(modifiers.contains(ModifierKey.CONTROL))
        {
            lineTemporailyTo(x, y, true);
        }
        else
        {
            lineTo(x, y, true); 
        }
    }

    @Override
    public void mousePressedDuringConstruction(double x, double y, Set<ModifierKey> modifiers)
    {}


    @Override
    public Path2D getROIShape()
    {
        Area roiShape = new Area(this.datasetArea);
        roiShape.subtract(new Area(super.getROIShape()));
        return new GeneralPath(roiShape);
    }

    @Override
    public MouseInputMode getMouseInputMode(MouseInputMode oldMode)
    {
        if(oldMode.isROI())
        {
            return oldMode;
        }
        return MouseInputModeStandard.WAND_ROI;
    }
}
