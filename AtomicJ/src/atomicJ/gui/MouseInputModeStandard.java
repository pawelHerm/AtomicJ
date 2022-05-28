
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

public enum MouseInputModeStandard implements MouseInputMode
{
    NORMAL(false, false, false), POLYGON_ROI(true, false, false), RECTANGULAR_ROI(true, false, false), ELIPTIC_ROI(true, false, false),
    FREE_HAND_ROI(true, false, false), WAND_ROI(true, false, false), ROI_LINE_SPLIT(true, false, false), ROI_POLY_LINE_SPLIT(true, false, false), ROI_FREE_HAND_SPLIT(true, false, false),ROTATE_ROI(true, false, false), PROFILE_LINE(false, false, true), PROFILE_POLYLINE(false, false, true), PROFILE_FREEHAND(false, false, true),
    DISTANCE_MEASUREMENT_LINE(false, true, false), DISTANCE_MEASUREMENT_POLYLINE(false, true, false), DISTANCE_MEASUREMENT_FREEHAND(false, true, false), INSERT_MAP_MARKER(false, false, false),
    INSERT_DOMAIN_MARKER(false, false, false), INSERT_RANGE_MARKER(false, false, false), TOOL_MODE(false, false, false);

    private final boolean isROI;
    private final boolean isMeasurement;
    private final boolean isProfile;

    MouseInputModeStandard(boolean isROI, boolean isMeasurement, boolean isProfile)
    {
        this.isROI = isROI;
        this.isMeasurement = isMeasurement;
        this.isProfile = isProfile;
    }

    @Override
    public boolean isROI()
    {
        return isROI;
    }

    @Override
    public boolean isMeasurement()
    {
        return isMeasurement;
    }

    @Override
    public boolean isProfile()
    {
        return isProfile;
    }

    @Override
    public boolean isMoveDataItems(Object key)
    {
        return false;
    }

    @Override
    public boolean isMoveDataItems(Object movableDatasetKey, DataModificationType movementType) 
    {
        return false;
    }

    @Override
    public boolean isDrawDataset(Object datasetGroupTag) 
    {
        return false;
    }
}