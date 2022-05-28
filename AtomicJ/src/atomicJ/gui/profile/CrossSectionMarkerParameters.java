
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

package atomicJ.gui.profile;

import java.awt.geom.Point2D;

public class CrossSectionMarkerParameters 
{
    private final String type;
    private final Object profileKey;
    private final Point2D controlPoint;

    public CrossSectionMarkerParameters(Point2D controlPoint, String type, Object profileKey) 
    {
        this.controlPoint = controlPoint;
        this.type = type;
        this.profileKey = profileKey;
    }

    public Point2D getControlPoint()
    {
        return controlPoint;
    }

    public Object getProfileKey()
    {
        return profileKey;
    }

    public String getType()
    {
        return type;
    }
}
