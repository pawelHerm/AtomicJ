
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

import java.awt.geom.Path2D;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;


public class ROIMagicWandPath extends ROIPolygon
{
    private static final long serialVersionUID = 1L;

    public ROIMagicWandPath(Path2D shape, Integer key, ROIStyle style) 
    {
        super(shape, key, style);
    }

    public ROIMagicWandPath(Path2D shape, Integer key, String label, ROIStyle style) 
    {
        super(shape, key, label, style);
    }

    public ROIMagicWandPath(ROIMagicWandPath that)
    {
        super(that); 
    }

    public ROIMagicWandPath(ROIMagicWandPath that, ROIStyle style)
    {
        super(that, style); 
    }

    public ROIMagicWandPath(ROIMagicWandPath that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label); 
    }

    @Override
    public ROIMagicWandPath copy()
    {
        return new ROIMagicWandPath(this);
    }

    @Override
    public ROIMagicWandPath copy(ROIStyle style)
    {
        return new ROIMagicWandPath(this, style);
    }

    @Override
    public ROIMagicWandPath copy(ROIStyle style,Integer key, String label)
    {
        return new ROIMagicWandPath(this, style, key, label);
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
