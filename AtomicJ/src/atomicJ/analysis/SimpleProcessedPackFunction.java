
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;


public enum SimpleProcessedPackFunction implements ProcessedPackFunction<ProcessedSpectroscopyPack>
{
    YOUNG_MODULUS
    {
        @Override
        public Quantity getEvaluatedQuantity()
        {
            return Quantities.YOUNG_MODULUS_KPA;
        }

        @Override
        public double evaluate(ProcessedSpectroscopyPack pack) 
        {
            if(pack == null)
            {
                return Double.NaN;
            }
            NumericalSpectroscopyProcessingResults results = pack.getResults();
            double modulus = results.getYoungModulus();
            return modulus;
        }
    }, 

    TRANSITION_INDENTATION
    {
        @Override
        public Quantity getEvaluatedQuantity()
        {
            return Quantities.TRANSITION_INDENTATION_MICRONS;
        }   

        @Override
        public double evaluate(ProcessedSpectroscopyPack pack) 
        {
            if(pack == null)
            {
                return Double.NaN;
            }
            NumericalSpectroscopyProcessingResults results = pack.getResults();
            double transitionIndentation = results.getTransitionIndentation();
            return transitionIndentation;
        }
    }, 

    TRANSITION_FORCE
    {
        @Override
        public Quantity getEvaluatedQuantity()
        {
            return Quantities.TRANSITION_FORCE_NANONEWTONS;
        }

        @Override
        public double evaluate(ProcessedSpectroscopyPack pack) 
        {
            if(pack == null)
            {
                return Double.NaN;
            }
            NumericalSpectroscopyProcessingResults results = pack.getResults();
            double transitionForce = results.getTransitionForce();
            return transitionForce;
        }
    }, 

    CONTACT_POSITION
    {
        @Override
        public Quantity getEvaluatedQuantity()
        {
            return Quantities.CONTACT_POSITION_MICRONS;
        }

        @Override
        public double evaluate(ProcessedSpectroscopyPack pack) 
        {
            if(pack == null)
            {
                return Double.NaN;
            }
            NumericalSpectroscopyProcessingResults results = pack.getResults();
            double contactPosition = results.getContactDisplacement();
            return contactPosition;
        }
    }, 

    CONTACT_FORCE
    {
        @Override
        public Quantity getEvaluatedQuantity()
        {
            return Quantities.CONTACT_FORCE_NANONEWTONS;
        }

        @Override
        public double evaluate(ProcessedSpectroscopyPack pack) 
        {
            if(pack == null)
            {
                return Double.NaN;
            }
            NumericalSpectroscopyProcessingResults results = pack.getResults();
            double contactForce = results.getContactForce();
            return contactForce;
        }
    },

    DEFORMATION
    {
        @Override
        public Quantity getEvaluatedQuantity()
        {
            return Quantities.DEFORMATION_MICRONS;
        }

        @Override
        public double evaluate(ProcessedSpectroscopyPack pack) 
        {
            if(pack == null)
            {
                return Double.NaN;
            }
            NumericalSpectroscopyProcessingResults results = pack.getResults();
            double deformation = results.getMaximalDefomation();
            return deformation;
        }
    }
    //
    //    ADHESION_FORCE
    //    {
    //        @Override
    //        public Quantity getEvaluatedQuantity()
    //        {
    //            return Quantities.ADHESION_FORCE_NANONEWTONS;
    //        }
    //
    //        @Override
    //        public double evaluate(ProcessedPack pack) 
    //        {
    //            if(pack == null)
    //            {
    //                return Double.NaN;
    //            }
    //
    //            NumericalResults results = pack.getResults();
    //            double adhesionForce = results.getAdhesionForce();
    //            return adhesionForce;
    //        }
    //    }
    ;

    public static Set<String> getAvailableQuantities()
    {
        Set<String> availableQuantities = new LinkedHashSet<>();

        for(SimpleProcessedPackFunction pf : SimpleProcessedPackFunction.values())
        {
            availableQuantities.add(pf.getEvaluatedQuantity().getName());
        }

        return availableQuantities;
    }

    public static Map<String, ProcessedPackFunction<ProcessedSpectroscopyPack>> getAvailableQuantitiesFunctionMap()
    {
        Map<String, ProcessedPackFunction<ProcessedSpectroscopyPack>> availableQuantitiesMap = new LinkedHashMap<>();

        for(ProcessedPackFunction<ProcessedSpectroscopyPack> pf : SimpleProcessedPackFunction.values())
        {
            availableQuantitiesMap.put(pf.getEvaluatedQuantity().getName(), pf);
        }

        return availableQuantitiesMap;
    }

    public static <E extends Processed1DPack<E,?>> Map<String, ProcessedPackFunction<? super E>> getAvailableQuantitiesFunctionMap(Collection<ProcessedPackFunction<? super E>> functions)
    {
        Map<String, ProcessedPackFunction<? super E>> availableQuantitiesMap = new LinkedHashMap<>();

        for(ProcessedPackFunction<? super E> pf : functions)
        {
            availableQuantitiesMap.put(pf.getEvaluatedQuantity().getName(), pf);
        }

        return availableQuantitiesMap;
    }
}
