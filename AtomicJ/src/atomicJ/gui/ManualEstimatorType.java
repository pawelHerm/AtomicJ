
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

import atomicJ.analysis.AbscissaEstimator;
import atomicJ.analysis.ContactEstimator;
import atomicJ.analysis.ManualContactEstimator;
import atomicJ.analysis.OrdinateEstimator;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;


public enum ManualEstimatorType 
{
    ORDINATE_ESTIMATOR("Ordinate", true, false) 
    {
        @Override
        public ContactEstimator getContactEstimator(UnitExpression distance, UnitExpression deflection) 
        {
            ContactEstimator estimator = new OrdinateEstimator(distance);
            return estimator;
        }
    }, 

    ABSCISSA_ESTIMATOR("Abscissa", false, true) 
    {
        @Override
        public ContactEstimator getContactEstimator(UnitExpression distance, UnitExpression deflection) 
        {
            ContactEstimator estimator = new AbscissaEstimator(deflection);
            return estimator;		
        }
    },

    POINT_ESTIMATOR("Point", true, true) 
    {
        @Override
        public ContactEstimator getContactEstimator(UnitExpression distance, UnitExpression deflection) 
        {
            double distanceMicrons = distance.derive(Units.MICRO_METER_UNIT).getValue();
            double deflectionMicrons = deflection.derive(Units.MICRO_METER_UNIT).getValue();

            ContactEstimator estimator = new ManualContactEstimator(distanceMicrons,deflectionMicrons);
            return estimator;
        }
    };

    private final String prettyName;

    private final boolean distanceRequired;
    private final boolean deflectionRequired;

    private ManualEstimatorType(String prettyName, boolean distanceRequired, boolean deflectionRequired)
    {
        this.prettyName = prettyName;
        this.distanceRequired = distanceRequired;
        this.deflectionRequired = deflectionRequired;
    }

    public boolean isDistanceRequired()
    {
        return distanceRequired;
    }

    public boolean isDeflectionRequired()
    {
        return deflectionRequired;
    }

    public abstract ContactEstimator getContactEstimator(UnitExpression distance, UnitExpression force);

    @Override
    public String toString()
    {
        return prettyName;
    }
}
