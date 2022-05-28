
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

package atomicJ.analysis;

public enum AutomaticContactEstimatorType 
{
    CLASSICAL_GOLDEN("Classical golden") 
    {
        @Override
        public ContactEstimator getContactEstimator(int baselineDegree, ContactEstimationGuide model) 
        {
            return new ClassicalFlexibleEstimator(BasicMinimumSearchStrategy.GOLDEN_SECTION, model);
        }
    }
    ,CLASSICAL_FOCUSED_GRID("Classical focused grid") 
    {
        @Override
        public ContactEstimator getContactEstimator(int baselineDegree, ContactEstimationGuide model) 
        {
            return new ClassicalFlexibleEstimator(BasicMinimumSearchStrategy.FOCUSED_GRID, model);
        }
    },
    CLASSICAL_EXHAUSTIVE("Classical exhaustive") 
    {
        @Override
        public ContactEstimator getContactEstimator(int baselineDegree, ContactEstimationGuide model)
        {
            return new ClassicalFlexibleEstimator(BasicMinimumSearchStrategy.EXHAUSTIVE, model);
        }
    }, 

    ROBUST_GOLDEN("Robust golden") 
    {
        @Override
        public ContactEstimator getContactEstimator(int baselineDegree, ContactEstimationGuide model) 
        {
            return new RobustFlexibleEstimator(BasicMinimumSearchStrategy.GOLDEN_SECTION, baselineDegree, model);
        }
    }, 

    ROBUST_FOCUSED_GRID("Robust focused grid") 
    {
        @Override
        public ContactEstimator getContactEstimator(int baselineDegree, ContactEstimationGuide model) 
        {
            return new RobustFlexibleEstimator(BasicMinimumSearchStrategy.FOCUSED_GRID, baselineDegree, model);
        }
    },
    ROBUST_EXHAUSTIVE("Robust exhaustive")
    {
        @Override
        public ContactEstimator getContactEstimator(int baselineDegree, ContactEstimationGuide model) 
        {
            return new RobustFlexibleEstimator(BasicMinimumSearchStrategy.EXHAUSTIVE, baselineDegree, model);
        }
    };

    private final String prettyName;

    AutomaticContactEstimatorType(String prettyName)
    {
        this.prettyName = prettyName;
    }

    @Override
    public String toString()
    {
        return prettyName;
    }

    public abstract ContactEstimator getContactEstimator(int baselineDegree, ContactEstimationGuide model);

    public static AutomaticContactEstimatorType getValue(String identifier, AutomaticContactEstimatorType fallBackValue)
    {
        AutomaticContactEstimatorType estimator = fallBackValue;

        if(identifier != null)
        {
            for(AutomaticContactEstimatorType est : AutomaticContactEstimatorType.values())
            {
                String estIdentifier = est.getIdentifier();
                if(estIdentifier.equals(identifier))
                {
                    estimator = est;
                    break;
                }
            }
        }

        return estimator;
    }


    public String getIdentifier()
    {
        return name();
    }
}
