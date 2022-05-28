
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

package atomicJ.readers.nanoscope;

import java.awt.geom.Point2D;
import java.util.*;

import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;
import atomicJ.gui.UserCommunicableException;
import atomicJ.sources.SimpleSpectroscopySource;


public class NanoscopeMCACurveReader extends NanoscopeSpectroscopyReader
{
    private final UnitExpression xReferencePosition;
    private final UnitExpression yReferencePosition;

    public NanoscopeMCACurveReader(UnitExpression xReferencePosition, UnitExpression yReferencePosition)
    {
        this.xReferencePosition = xReferencePosition;
        this.yReferencePosition = yReferencePosition;
    }

    @Override
    protected void handleSourceCreation(List<SimpleSpectroscopySource> sources, NanoscopeScanList scanList, NanoscopeScannerList scannerList) throws UserCommunicableException
    {
        UnitExpression xPosition = xReferencePosition.subtract(scanList.getXOffset());
        UnitExpression yPosition = yReferencePosition.subtract(scanList.getYOffset());

        double x = xPosition.derive(Units.MICRO_METER_UNIT).getValue();
        double y = yPosition.derive(Units.MICRO_METER_UNIT).getValue();

        for(SimpleSpectroscopySource s : sources)
        {
            s.setRecordingPoint(new Point2D.Double(x, y));
        }
    }
}

