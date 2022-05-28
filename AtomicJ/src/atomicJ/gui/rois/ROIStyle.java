
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

package atomicJ.gui.rois;

import java.awt.Paint;
import java.io.IOException;
import java.io.Serializable;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import atomicJ.gui.annotations.AnnotationStyle;
import atomicJ.utilities.SerializationUtilities;

import static atomicJ.gui.rois.PreferredROIStyle.*;

public class ROIStyle extends AnnotationStyle<PreferredROIStyle> implements Cloneable, Serializable 
{
    private static final long serialVersionUID = 1L; 

    public static final String ROI_STYLE_COMPLETELY_CHANGED = "RoiStyleCompletelyChaneged";

    private boolean outlineVisible = true;     

    private boolean isFilledUnfinishedStandard;
    private boolean isFilledFinishedStandard;

    private Paint paintFillFinishedStandard;
    private Paint paintFillUnfinishedStandard;

    public ROIStyle(Preferences pref, Paint defaultPaint) 
    {
        super(PreferredROIStyle.getInstance(pref, defaultPaint));
        setDefaultROIStyle(getPreferredStyle());
    }  

    private void setDefaultROIStyle(PreferredROIStyle prefStyle)
    {
        this.isFilledUnfinishedStandard = prefStyle.isFilledUnfinishedStandard();
        this.isFilledFinishedStandard = prefStyle.isFilledFinishedStandard();

        this.paintFillUnfinishedStandard = prefStyle.getPaintFillUnfinishedStandard();
        this.paintFillFinishedStandard = prefStyle.getPaintFillFinishedStandard();

        firePropertyChange(ROI_STYLE_COMPLETELY_CHANGED, false, true);
    }

    @Override
    public void setDefaultStyle()
    {	
        super.setDefaultStyle();
        setDefaultROIStyle(getPreferredStyle());  	
    }	

    @Override
    public void saveStyleAsDefault()
    {
        super.saveStyleAsDefault(); 

        Preferences pref = getPreferences();

        pref.putBoolean(ROI_FILLED_UNFINISHED_STANDARD, isFilledUnfinishedStandard);
        pref.putBoolean(ROI_FILLED_FINISHED_STANDARD, isFilledFinishedStandard);

        try 
        {	
            SerializationUtilities.putSerializableObject(pref, ROI_PAINT_FILL_UNFINISHED_STANDARD, paintFillUnfinishedStandard);
            SerializationUtilities.putSerializableObject(pref, ROI_PAINT_FILL_FINISHED_STANDARD, paintFillFinishedStandard);
        }
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }	
    }

    public Paint getPaintFillFinishedStandard()
    {
        return paintFillFinishedStandard;
    }

    public void setPaintFillFinishedStandard(Paint paintFillFinishedStandardNew)
    {
        Paint paintFillFinishedStandardOld = this.paintFillFinishedStandard;
        this.paintFillFinishedStandard = paintFillFinishedStandardNew;

        firePropertyChange(ROI_PAINT_FILL_FINISHED_STANDARD, paintFillFinishedStandardOld, paintFillFinishedStandardNew);	
    }

    public Paint getPaintFillUnfinishedStandard()
    {
        return paintFillUnfinishedStandard;
    }

    public void setPaintFillUnfinishedStandard(Paint paintFillUnfinishedStandardNew)
    {
        Paint paintFillUnfinishedStandardOld = this.paintFillUnfinishedStandard;
        this.paintFillUnfinishedStandard = paintFillUnfinishedStandardNew;

        firePropertyChange(ROI_PAINT_FILL_UNFINISHED_STANDARD, paintFillUnfinishedStandardOld, paintFillUnfinishedStandardNew);	
    }

    public boolean isFilledUnfinishedStandard()
    {
        return isFilledUnfinishedStandard;
    }

    public void setFilledUnfinishedStandard(boolean filledNew)
    {
        boolean filledOld = this.isFilledUnfinishedStandard;
        this.isFilledUnfinishedStandard = filledNew;

        firePropertyChange(ROI_FILLED_UNFINISHED_STANDARD, filledOld, filledNew);
    }


    public boolean isFilledFinishedStandard()
    {
        return isFilledFinishedStandard;
    }

    public void setFilledFinishedStandard(boolean filledNew)
    {
        boolean filledOld = this.isFilledFinishedStandard;
        this.isFilledFinishedStandard = filledNew;

        firePropertyChange(ROI_FILLED_FINISHED_STANDARD, filledOld, filledNew);
    }


    public boolean getOutlineVisible()
    {
        return outlineVisible;
    }

    public void setOutlineVisible(boolean outlineVisibleNew)
    {
        boolean outlineVisibleOld = this.outlineVisible;
        this.outlineVisible = outlineVisibleNew;	

        firePropertyChange(ROI_OUTLINE_VISIBLE, outlineVisibleOld, outlineVisibleNew);
    }
}
