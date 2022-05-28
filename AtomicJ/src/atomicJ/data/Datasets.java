
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

public final class Datasets
{
    public static final String APPROACH = "Approach"; 	
    public static final String AVERAGED_APPROACH = "Averaged approach";   
    public static final String APPROACH_SMOOTHED = "Approach smoothed";	
    public static final String APPROACH_TRIMMED = "Approach trimmed";	
    public static final String WITHDRAW = "Withdraw"; 
    public static final String AVERAGED_WITHDRAW = "Averaged withdraw"; 
    public static final String WITHDRAW_SMOOTHED = "Withdraw smoothed";	
    public static final String WITHDRAW_TRIMMED = "Withdraw trimmed";	
    public static final String CONTACT_POINT = "Contact point";
    public static final String PULL_OFF_POINT = "Pull off point";
    public static final String MODEL_TRANSITION_POINT = "Transition";
    public static final String MODEL_TRANSITION_POINT_FORCE_CURVE = "Transition force curve";
    public static final String MODEL_TRANSITION_POINT_INDENTATION_CURVE = "Transition indentation curve";
    public static final String MODEL_TRANSITION_POINT_POINTWISE_MODULUS = "Transition pointwise modulus";

    public static final String RECORDED_CURVE = "Recorded curve";
    public static final String AVERAGED_RECORDED_CURVE = "Recorded curve";

    public static final String INDENTATION = "Indentation";	
    public static final String AVERAGED_INDENTATION_DATA = "Averaged indentation"; 

    public static final String INDENTATION_FIT = "Indentation fit"; 	
    public static final String INDENTATION_DATA = "Indentation data";	
    public static final String INDENTATION_COMPOSITE = "ndentation composite";
    public static final String POINTWISE_MODULUS = "Pointwise modulus";	
    public static final String AVERAGED_POINTWISE_MODULUS_DATA = "Averaged pointwise modulus"; 

    public static final String POINTWISE_MODULUS_DATA = "Pointwise modulus data";	
    public static final String POINTWISE_MODULUS_COMPOSITE = "Pointwise modulus composite";
    public static final String POINTWISE_MODULUS_FIT = "Pointwise modulus fit";	
    public static final String FIT = "Fit"; 	
    public static final String SMOOTHED = "Smoothed";
    public static final String TRIMMED ="Trimmed";	
    public static final String FORCE_CURVE = "Force curve";	
    public static final String DEFLECTION_CURVE = "Deflection curve";	
    public static final String RAW_CURVE = "Raw curve";	

    public static final String YOUNG_MODULUS = "Young's modulus";	
    public static final String STIFFENING = "Stiffening";
    public static final String FORCE = "Force";
    public static final String TIP_DISPLACEMENT = "Tip displacement";
    public static final String TRANSITION_INDENTATION = "Transition indentation";	
    public static final String TRANSITION_FORCE = "Transition force";	
    public static final String CONTACT_POSITION = "Contact position";
    public static final String CONTACT_FORCE = "Contact force";
    public static final String DEFORMATION = "Deformation "; //ugly hack, we used space at the end, because Bruker files has Deformation channels. AtomicJ creates deformation channel too, so there may be problems
    public static final String ADHESION_FORCE = "Adhesion force";
    public static final String JUMP_FORCE = "Jump force";
    public static final String ADHESION_WORK = "Adhesion work";
    public static final String LIFT_OFF = "Lift off";

    public static final String HEIGHT = "Height";
    public static final String DISTANCE = "Distance";
    public static final String DEFLECTION = "Deflection";
    public static final String PHASE = "Phase";
    public static final String AMPLITUDE = "Amplitude";
    public static final String FORCE_GRADIENT = "Force gradient";
    public static final String TIME = "Time";

    public static final String X_COORDINATE = "X";
    public static final String Y_COORDINATE = "Y";

    public static final String TOPOGRAPHY_TRACE = "Topography trace";
    public static final String TOPOGRAPHY_RETRACE = "Topography retrace";
    public static final String DEFLECTION_TRACE = "Deflection trace";
    public static final String DEFLECTION_RETRACE = "Deflection retrace";
    public static final String FRICTION_TRACE = "Friction trace";
    public static final String FRICTION_RETRACE = "Friction retrace";

    public static final String CROSS_SECTION = "Cross section";

    //Color channels of photos
    public static final String RED = "Red";
    public static final String GREEN = "Green";
    public static final String BLUE = "Blue";
    public static final String GRAY = "Gray";

    //PLOT TYPES

    public static final String CALIBRATION_PLOT = "Calibration plot";	
    public static final String TRIMMING_PLOT = "Trimming plot";	
    public static final String CONTACT_SELECTION_PLOT = "Contact selection plot";
    public static final String HSTOGRAM_PLOT = "Histogram plot";
    public static final String RANGE_SELECTION_HSTOGRAM_PLOT = "Range selection histogram plot";
    public static final String BOX_AND_WHISKER_PLOT = "Box and whisker plot";

    public static final String MAP_PLOT = "Map plot";	
    public static final String DENSITY_PLOT = "Density plot";	
    public static final String IMAGE_PLOT = "Image plot";	
    public static final String LIVE_PREVIEW_INDENTATION_PLOT = "Live preview indentation plot";	
    public static final String LIVE_PREVIEW_POINTWISE_MODULUS_PLOT = "Live preview pointwise modulus plot";	
    public static final String AFM_CURVE_PREVIEW_PLOT = "AFM curve preview plot";	
    public static final String AMPLITUDE_CURVE_PREVIEW_PLOT = "Amplitude curve preview plot";    
    public static final String PHASE_CURVE_PREVIEW_PLOT = "Phase curve preview plot";    

    public static final String FORCE_CURVE_PLOT = "Force curve plot";	
    public static final String INDENTATION_PLOT = "Indentation plot";	
    public static final String POINTWISE_PLOT = "Pointwise plot";	
    public static final String CROSS_SECTION_PLOT = "Crosssection plot";
    public static final String SLICE_PLOT = "Slice plot";

    public static final String AVERAGED_FORCE_CURVE_PLOT = "Averaged force curve plot";   
    public static final String AVERAGED_INDENTATION_PLOT = "Averaged indentation plot";   
    public static final String AVERAGED_POINTWISE_PLOT = "Averaged pointwise plot";

    public static final String POINTWISE_MODULUS_STACK = "Pointwise modulus stack";
    public static final String FORCE_STACK = "Force stack";
    public static final String STIFFENING_STACK = "Stiffening stack";
    public static final String FORCE_GRADIENT_STACK = "Force gradient stack";
    public static final String FORCE_CONTOUR_MAPPING = "Force contour mapping";
    public static final String STACK_SLICE = "Stack slice";
}

