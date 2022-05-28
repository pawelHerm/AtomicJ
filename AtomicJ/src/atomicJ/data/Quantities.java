
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

package atomicJ.data;

import static atomicJ.data.Datasets.*;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.Units;

public class Quantities 
{
    private Quantities(){}

    public static final Quantity YOUNG_MODULUS_KPA = new UnitQuantity(YOUNG_MODULUS, Units.KILO_PASCAL_UNIT);	
    public static final Quantity POINTWISE_MODULUS_KPA = new UnitQuantity(POINTWISE_MODULUS, Units.KILO_PASCAL_UNIT);	
    public static final Quantity STIFFENING_KPA = new UnitQuantity(STIFFENING, Units.KILO_PASCAL_UNIT);  
    public static final Quantity STIFFNESS = new UnitQuantity("Stiffness", Units.NEWTON_PER_METER);

    public static final Quantity TIP_DISPLACEMENT_MICRONS = new UnitQuantity(TIP_DISPLACEMENT, Units.MICRO_METER_UNIT);  
    public static final Quantity INDENTATION_MICRONS = new UnitQuantity(INDENTATION, Units.MICRO_METER_UNIT);	
    public static final Quantity TRANSITION_INDENTATION_MICRONS = new UnitQuantity(TRANSITION_INDENTATION, Units.MICRO_METER_UNIT);	
    public static final Quantity TRANSITION_FORCE_NANONEWTONS = new UnitQuantity(TRANSITION_FORCE, Units.NANO_NEWTON_UNIT);	
    public static final Quantity CONTACT_POSITION_MICRONS = new UnitQuantity(CONTACT_POSITION, Units.MICRO_METER_UNIT);
    public static final Quantity CONTACT_FORCE_NANONEWTONS = new UnitQuantity(CONTACT_FORCE, Units.NANO_NEWTON_UNIT);
    public static final Quantity DEFORMATION_MICRONS = new UnitQuantity(DEFORMATION, Units.MICRO_METER_UNIT); 
    public static final Quantity ADHESION_FORCE_NANONEWTONS = new UnitQuantity(ADHESION_FORCE, Units.NANO_NEWTON_UNIT);
    public static final Quantity ADHESION_WORK_MILIJOULES = new UnitQuantity(ADHESION_WORK, Units.MILI_JOUL_PER_SQUARE_METER_UNIT);
    public static final Quantity HEIGHT_MICRONS = new UnitQuantity(HEIGHT, Units.MICRO_METER_UNIT);

    public static final Quantity DISTANCE_MICRONS = new UnitQuantity(DISTANCE, Units.MICRO_METER_UNIT);	
    public static final Quantity DEFLECTION_VOLTS = new UnitQuantity(DEFLECTION, Units.VOLT_UNIT);	
    public static final Quantity DEFLECTION_NANO_AMPERES = new UnitQuantity(DEFLECTION, Units.NANO_AMPERE_UNIT); 

    public static final Quantity DEFLECTION_MICRONS = new UnitQuantity(DEFLECTION, Units.MICRO_METER_UNIT);	
    public static final Quantity AMPLITUDE_VOLTS = new UnitQuantity(AMPLITUDE, Units.VOLT_UNIT);  
    public static final Quantity AMPLITUDE_MICRONS = new UnitQuantity(AMPLITUDE, Units.MICRO_METER_UNIT);  
    public static final Quantity PHASE_DEGREES = new UnitQuantity(PHASE, Units.DEGREE_UNIT);  
    public static final Quantity FORCE_NANONEWTONS = new UnitQuantity(FORCE, Units.NANO_NEWTON_UNIT);
    public static final Quantity FORCE_GRADIENTS_NANONEWTONS_MICROMETERS = new UnitQuantity(FORCE_GRADIENT, Units.NANO_NEWTON_PER_MICRO_METER_UNIT);
    public static final Quantity TIME_SECONDS = new UnitQuantity(TIME, Units.SECOND_UNIT);
}
