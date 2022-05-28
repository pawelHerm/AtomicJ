
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

package atomicJ.gui.histogram;

import java.awt.Window;
import java.beans.PropertyChangeListener;

import atomicJ.statistics.BinningMethod;
import atomicJ.statistics.DistributionType;
import atomicJ.statistics.FitType;
import atomicJ.statistics.HistogramType;



public interface HistogramBinningModel
{	
    public String getUnit();	
    public boolean containsNonpositiveValues();	
    public HistogramDestination getHistogramDestination();	
    public Window getPublicationSite();
    public String getName();	
    public void setName(String newName);	
    public boolean isEntitled();	
    public void setEntitled(boolean entitledNew);
    public int getAllDataCount();
    public int getTrimmedDataCount();
    public int getDiscardedDataCount();
    public double getBinCount();
    public void setBinCount(Number count);
    public void specifyBinCount(Number count);
    public void setCountConsistentWithRange();
    public void setCountConsistentWithWidth();
    public void setWidthConsistentWithRangeAndCount();
    public double getBinWidth();
    public void setBinWidth(Double binWidthNew);
    public void specifyBinWidth(Double width);
    public boolean isFullRange();
    public void setFullRange(Boolean full);
    public Double getFractionOfSmallestTrimmed();
    public void setFractionOfSmallestTrimmed(Double trimSmallestNew);
    public Double getFractionOfLargestTrimmed();
    public void setFractionOfLargestTrimmed(Double trimLargestNew);
    public boolean isFitted();
    public void setFitted(Boolean fitted);
    public double getRangeMinimum();
    public void setRangeMinimum(Double min);
    public void specifyRangeMinimum(Double min);
    public double getRangeMaximum();
    public void setRangeMaximum(Double max);
    public void specifyRangeMaximum(Double max);
    public boolean isRangeExtensive();
    public HistogramType getHistogramType();	
    public void setHistogramType(HistogramType type);
    public DistributionType getDistributionType();	
    public void setDistributionType(DistributionType type);	
    public FitType getFitType();	
    public void setFitType(FitType type);	
    public BinningMethod getBinningMethod();	
    public void setBinningMethod(BinningMethod method);

    public void setUndoPoint();
    public void undo();

    public void removePropertyChangeListener(PropertyChangeListener listener);
    public void addPropertyChangeListener(PropertyChangeListener listener);
}
