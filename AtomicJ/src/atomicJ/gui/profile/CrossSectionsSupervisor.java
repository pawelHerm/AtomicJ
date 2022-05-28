
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

import java.awt.Cursor;
import java.awt.Window;


public interface CrossSectionsSupervisor
{
    public void requestMarkerMovement(Object profileKey, int markerIndex, double positionNew);
    public void attemptToAddNewMarker(double zSelected, double d, double margin);
    public void attemptToRemoveMarker(Object profileKey, double markerPosition);
    public boolean canNewMarkerBeAdded(double zSelected, double d, double margin);

    public CrossSectionMarkerParameters getMarkerParameters(Object profileKey, double d);
    public CrossSectionMarkerParameters getMarkerParameters(String type, Object profileKey, double d);

    public void requestCursorChange(Cursor cursor);
    public Window getPublicationSite();
}
