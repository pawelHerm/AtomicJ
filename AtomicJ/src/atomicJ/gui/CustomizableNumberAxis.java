
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

import java.awt.*;
import java.awt.geom.Rectangle2D;

import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.Plot;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;

import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.units.LightweightTickUnits;

public class CustomizableNumberAxis extends CustomizableNumberBaseAxis implements PreferencesSource, NumberFormatReceiver
{
    private static final long serialVersionUID = 1L;

    private boolean restoreDefaultAutoRange = false;

    private Quantity dataQuantity;
    private Quantity displayedQuantity;
    private double prefixScalingFactor;

    private LightweightTickUnits tickUnits;

    private final boolean unitsRotationIndependent;

    public CustomizableNumberAxis(Quantity dataQuantity, Preferences pref)
    {
        this(dataQuantity, pref, false);
    }

    public CustomizableNumberAxis(Quantity dataQuantity, Preferences pref, boolean unitsRotationIndependent)
    {
        this(dataQuantity, PreferredAxisStyle.getInstance(pref), unitsRotationIndependent); 
    }

    public CustomizableNumberAxis(Quantity dataQuantity, PreferredAxisStyle style)
    {
        this(dataQuantity, style, false);
    }

    public CustomizableNumberAxis(Quantity dataQuantity, PreferredAxisStyle style, boolean unitsRotationIndependent)
    {
        super(dataQuantity.getLabel(), style, style.buildTickUnits(1), false, false);

        this.unitsRotationIndependent = unitsRotationIndependent;
        this.dataQuantity = dataQuantity;
        this.displayedQuantity = dataQuantity;
        this.prefixScalingFactor = 1;
        this.tickUnits = (LightweightTickUnits) getStandardTickUnits();            
    }

    public CustomizableNumberAxis(CustomizableNumberAxis that)
    {
        super(that);

        this.dataQuantity = that.dataQuantity;
        this.displayedQuantity = that.displayedQuantity;
        this.prefixScalingFactor = that.prefixScalingFactor;
        this.restoreDefaultAutoRange = that.restoreDefaultAutoRange;

        this.unitsRotationIndependent = that.unitsRotationIndependent;

        setAutoRangeIncludesZero(false);
        setAutoRangeStickyZero(false);

        try
        {
            this.tickUnits = (LightweightTickUnits) that.tickUnits.clone();
        } 
        catch (CloneNotSupportedException e)
        {
            e.printStackTrace();
        }
        setStandardTickUnits(tickUnits);   
    }   

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public void setPlot(Plot plot)
    {
        super.setPlot(plot);

        setPreferredAxisUnit();
    }

    public UnitExpression java2DToDataUnitExpression(double java2DValue, Rectangle2D area, RectangleEdge edge)
    {
        double value = java2DToValue(java2DValue, area, edge);
        UnitExpression expr = new UnitExpression(value, dataQuantity.getUnit());

        return expr;
    }

    public UnitExpression java2DToAxisUnitExpression(double java2DValue, Rectangle2D area, RectangleEdge edge)
    {
        double value = java2DToValue(java2DValue, area, edge);
        UnitExpression expr = new UnitExpression(value, dataQuantity.getUnit()).derive(displayedQuantity.getUnit());

        return expr;
    }

    @Override
    public String getName()
    {
        String name = dataQuantity.getName();
        return name;
    }

    public PrefixedUnit getDataUnit()
    {
        PrefixedUnit unit = dataQuantity.getUnit();
        return unit;
    }

    public Quantity getDataQuantity()
    {
        return dataQuantity;
    }

    public UnitExpression getRangeLength()
    {
        return new UnitExpression(getRange().getLength(), dataQuantity.getUnit());
    }

    public void setDataQuantity(Quantity dataQuantityNew)
    {                
        if(!Objects.equals(this.dataQuantity, dataQuantityNew))
        {
            this.dataQuantity = dataQuantityNew;    
            this.displayedQuantity = dataQuantityNew;
            this.prefixScalingFactor = 1;

            this.tickUnits.setConversionFactor(prefixScalingFactor);
            setStandardTickUnits(tickUnits);

            setLabel(displayedQuantity.getLabel()); 

            if(isAutoTickUnitSelection())
            {            
                setPreferredAxisUnit();
            }
        }      
    }

    public Quantity getDisplayedQuantity()
    {
        return displayedQuantity;
    }

    public PrefixedUnit getDisplayedUnit()
    {
        PrefixedUnit unit = displayedQuantity.getUnit();
        return unit;
    }

    public double getDisplayedToDataScaling()
    {
        return prefixScalingFactor;
    }

    public List<PrefixedUnit> getProposedDisplayedUnits()
    {
        List<PrefixedUnit> proposedUnits = new ArrayList<>();

        PrefixedUnit dataUnit = dataQuantity.getUnit();
        PrefixedUnit nextUnit = dataUnit.getNext();
        PrefixedUnit previousUnit = dataUnit.getPrevious(); 

        proposedUnits.add(nextUnit);
        proposedUnits.add(dataUnit);
        proposedUnits.add(previousUnit);

        return proposedUnits;
    }

    public void setDisplayedUnit(PrefixedUnit unitNew)
    {
        if(unitNew == null)
        {
            return;
        }

        String unitNameNew = unitNew.getFullName();
        String unitNameOld = this.displayedQuantity.getFullUnitName();

        if(!Objects.equals(unitNameOld, unitNameNew))
        {
            Quantity displayedQuantityNew = displayedQuantity.deriveQuantity(unitNew);

            PrefixedUnit axisUnitNew = displayedQuantityNew.getUnit();
            PrefixedUnit dataUnit = dataQuantity.getUnit();

            this.displayedQuantity = displayedQuantityNew;            
            this.prefixScalingFactor = dataUnit.getConversionFactorTo(axisUnitNew);

            this.tickUnits.setConversionFactor(prefixScalingFactor);
            setStandardTickUnits(tickUnits);

            updateUnitInAxisLabel(unitNameOld, unitNameNew);            
        }      
    }


    //UNSURE
    @Override
    public void setTickUnit(NumberTickUnit unit, boolean notify, boolean turnOffAutoSelect)
    {
        super.setTickUnit(unit, notify, turnOffAutoSelect);

        if(isAutoTickUnitSelection())
        {            
            setPreferredAxisUnit();
        }
    }

    public void setPreferredAxisUnit()
    {
        if(!dataQuantity.hasDimension())
        {
            return;
        }

        double step = 0.25*Math.max(Math.abs(getRange().getUpperBound()), Math.abs(getRange().getLowerBound()));

        PrefixedUnit dataUnit = this.dataQuantity.getUnit(); 
        PrefixedUnit preferredUnit = dataUnit.getPreferredCompatibleUnit(4*step);      

        setDisplayedUnit(preferredUnit);
    }

    private void updateUnitInAxisLabel(String unitNameOld, String unitNameNew)
    {        
        String labelNew = replaceLast(getLabel(), Pattern.quote(unitNameOld), unitNameNew);    

        setLabel(labelNew);    
    }

    private static String replaceLast(String label, String regex, String replacement) 
    {
        return label.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

    public TickUnitSource getDefaultTickUnits()
    {
        return tickUnits;
    }

    public void setRestoreDefaultAutoRange(boolean restoreNew)
    {
        this.restoreDefaultAutoRange = restoreNew;
    }

    public boolean isRestoreDefaultAutoRange()
    {
        return restoreDefaultAutoRange;
    }

    @Override
    public boolean isTickLabelTrailingZeroes()
    {
        return tickUnits.isTickLabelTrailingZeroes();
    }

    @Override
    public void setTickLabelShowTrailingZeroes(boolean trailingZeroes)
    {
        tickUnits.setTickLabelShowTrailingZeroes(trailingZeroes);	
        updateCurrentTickUnitFormat();
    }

    @Override
    public boolean isTickLabelGroupingUsed()
    {
        return tickUnits.isTickLabelGroupingUsed();
    }

    @Override
    public void setTickLabelGroupingUsed(boolean used)
    {
        tickUnits.setTickLabelGroupingUsed(used);   	
        updateCurrentTickUnitFormat();
    }

    @Override
    public char getTickLabelGroupingSeparator()
    {
        return tickUnits.getTickLabelGroupingSeparator();
    }

    @Override
    public void setTickLabelGroupingSeparator(char separatorNew)
    {
        char separatorOld = tickUnits.getTickLabelGroupingSeparator();
        tickUnits.setTickLabelGroupingSeparator(separatorNew);

        if(separatorOld != separatorNew)
        {
            updateCurrentTickUnitFormat();
        }        
    }

    @Override
    public char getTickLabelDecimalSeparator()
    {
        return tickUnits.getTickLabelDecimalSeparator();
    }

    @Override
    public void setTickLabelDecimalSeparator(char separator)
    {
        tickUnits.setTickLabelDecimalSeparator(separator);   	
        updateCurrentTickUnitFormat();
    }

    @Override
    public void autoAdjustRange()
    {	    	            
        if(restoreDefaultAutoRange)
        {
            Range range = getDefaultAutoRange();
            setRange(range, false, false);            
        }
        else
        {
            super.autoAdjustRange();
        }      
    }

    private void updateCurrentTickUnitFormat()
    {  
        NumberTickUnit oldTickUnit = getTickUnit();
        double size = oldTickUnit.getSize();

        NumberTickUnit currentTickUnit = tickUnits.getTickUnitForSize(size);

        setTickUnit(currentTickUnit, true, false);
    }

    //modified from NumberAxis.java, by David Gilbert
    @Override
    protected void selectHorizontalAutoTickUnit(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) 
    {
        // start with the current tick unit...
        TickUnitSource tickUnits = getStandardTickUnits();
        TickUnit unit1 = tickUnits.getCeilingTickUnit(getTickUnit());
        double unit1Width = lengthToJava2D(unit1.getSize(), dataArea, edge);

        double tickLabelDimension = unitsRotationIndependent ? Math.max(estimateMaximumTickLabelHeight(g2), estimateMaximumTickLabelWidth(g2, getTickUnit()))
                : estimateMaximumTickLabelWidth(g2, getTickUnit());

        // then extrapolate...
        double guess = (tickLabelDimension / unit1Width) * unit1.getSize();

        NumberTickUnit unit2 = (NumberTickUnit) tickUnits.getCeilingTickUnit(
                guess);
        double unit2Width = lengthToJava2D(unit2.getSize(), dataArea, edge);

        if(unitsRotationIndependent)
        {
            tickLabelDimension = Math.max(estimateMaximumTickLabelHeight(g2), estimateMaximumTickLabelWidth(g2, unit2));;

            if (tickLabelDimension > unit2Width) 
            {
                unit2 = (NumberTickUnit) tickUnits.getLargerTickUnit(unit2);
            }

        }
        else
        {         
            tickLabelDimension = estimateMaximumTickLabelWidth(g2, unit2);
            if (1.2*tickLabelDimension > unit2Width) {
                unit2 = (NumberTickUnit) tickUnits.getLargerTickUnit(unit2);
            }
        }

        setTickUnit(unit2, false, false);

    }

    //modified from NumberAxis.java, by David Gilbert
    @Override
    protected void selectVerticalAutoTickUnit(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) 
    {        
        // start with the current tick unit...
        TickUnitSource tickUnits = getStandardTickUnits();
        TickUnit unit1 = tickUnits.getCeilingTickUnit(getTickUnit());
        double unitHeight = lengthToJava2D(unit1.getSize(), dataArea, edge);

        double tickLabelDimension = unitsRotationIndependent ? Math.max(estimateMaximumTickLabelHeight(g2), estimateMaximumTickLabelWidth(g2, getTickUnit()))
                : estimateMaximumTickLabelHeight(g2);

        // then extrapolate...
        double guess = (tickLabelDimension / unitHeight) * unit1.getSize();

        NumberTickUnit unit2 = (NumberTickUnit) tickUnits.getCeilingTickUnit(guess);
        double unit2Height = lengthToJava2D(unit2.getSize(), dataArea, edge);

        if(unitsRotationIndependent)
        {
            tickLabelDimension = Math.max(estimateMaximumTickLabelHeight(g2), estimateMaximumTickLabelWidth(g2, unit2));

            if (tickLabelDimension > unit2Height) 
            {
                unit2 = (NumberTickUnit) tickUnits.getLargerTickUnit(unit2);
            }
        }
        else
        {                     
            tickLabelDimension = estimateMaximumTickLabelHeight(g2);
            if (1.2*tickLabelDimension > unit2Height) 
            {
                unit2 = (NumberTickUnit) tickUnits.getLargerTickUnit(unit2);
            }
        }
        setTickUnit(unit2, false, false);

    }
}
