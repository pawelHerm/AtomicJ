
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

import java.awt.Paint;
import java.io.Serializable;
import java.util.prefs.Preferences;

import atomicJ.gui.annotations.AnnotationStyle;
import static atomicJ.gui.profile.PreferredProfileStyle.*;

public class ProfileStyle extends AnnotationStyle<PreferredProfileStyle> implements Cloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    public static final String PROFILE_STYLE_COMPLETELY_CHANGED = "ProfileCompletelyChanged";

    private boolean knobVisible;
    private KnobOrientation knobOrientation;
    private int knobWidth;
    private int knobHeight;

    private boolean isArrowheadVisibleUnfinishedStandard;
    private boolean isArrowheadVisibleUnfinishedHighlighted;
    private boolean isArrowheadVisibleFinishedStandard;
    private boolean isArrowheadVisibleFinishedHighlighted;

    private float arrowheadLengthUnfinishedStandard;
    private float arrowheadLengthUnfinishedHighlighted;
    private float arrowheadLengthFinishedStandard;
    private float arrowheadLengthFinishedHighlighted;

    private float arrowheadWidthUnfinishedStandard;
    private float arrowheadWidthUnfinishedHighlighted;
    private float arrowheadWidthFinishedStandard;
    private float arrowheadWidthFinishedHighlighted;

    public ProfileStyle(Preferences pref, Paint defaultPaint) 
    {
        super(PreferredProfileStyle.getInstance(pref, defaultPaint));
        setDefaultProfileStyle(getPreferredStyle());
    }  

    private void setDefaultProfileStyle(PreferredProfileStyle prefStyle)
    {
        setArrowheadVisibleUnfinishedStandard(prefStyle.isArrowheadVisibleUnfinishedStandard());

        this.knobVisible = prefStyle.isKnobVisible();
        this.knobOrientation = prefStyle.getKnobOrientation();

        this.knobWidth = prefStyle.getKnobWidth();
        this.knobHeight = prefStyle.getKnobHeight();

        this.isArrowheadVisibleUnfinishedHighlighted = prefStyle.isArrowheadVisibleUnfinishedHighlighted();
        this.isArrowheadVisibleFinishedStandard = prefStyle.isArrowheadVisibleFinishedStandard();
        this.isArrowheadVisibleFinishedHighlighted = prefStyle.isArrowheadVisibleFinishedHighlighted();

        this.arrowheadLengthUnfinishedStandard = prefStyle.getArrowheadLengthUnfinishedStandard();
        this.arrowheadLengthUnfinishedHighlighted = prefStyle.getArrowheadLengthUnfinishedHighlighted();
        this.arrowheadLengthFinishedStandard = prefStyle.getArrowheadLengthFinishedStandard();
        this.arrowheadLengthFinishedHighlighted = prefStyle.getArrowheadLengthFinishedHighlighted();

        this.arrowheadWidthUnfinishedStandard = prefStyle.getArrowheadLengthUnfinishedStandard();
        this.arrowheadWidthUnfinishedHighlighted = prefStyle.getArrowheadWidthUnfinishedHighlighted();
        this.arrowheadWidthFinishedStandard = prefStyle.getArrowheadWidthFinishedStandard();
        this.arrowheadWidthFinishedHighlighted = prefStyle.getArrowheadWidthFinishedHighlighted();

        firePropertyChange(PROFILE_STYLE_COMPLETELY_CHANGED, false, true);
    }

    @Override
    public void setDefaultStyle()
    {	
        super.setDefaultStyle();
        setDefaultProfileStyle(getPreferredStyle());  	
    }	

    @Override
    public void saveStyleAsDefault()
    {
        super.saveStyleAsDefault(); 

        Preferences pref = getPreferences();

        pref.putBoolean(PROFILE_KNOB_VISIBLE, knobVisible);
        pref.put(PROFILE_KNOB_ORIENTATION, knobOrientation.name());

        pref.putInt(PROFILE_KNOB_WIDTH, knobWidth);
        pref.putInt(PROFILE_KNOB_HEIGHT, knobHeight);

        pref.putBoolean(PROFILE_ARROWHEAD_VISIBLE_UNFINISHED_STANDARD, isArrowheadVisibleUnfinishedStandard);
        pref.putBoolean(PROFILE_ARROWHEAD_VISIBLE_UNFINISHED_HIGHLIGHTED, isArrowheadVisibleUnfinishedHighlighted);
        pref.putBoolean(PROFILE_ARROWHEAD_VISIBLE_FINISHED_STANDARD, isArrowheadVisibleFinishedStandard);
        pref.putBoolean(PROFILE_ARROWHEAD_VISIBLE_FINISHED_HIGHLIGHTED, isArrowheadVisibleFinishedHighlighted);

        pref.putFloat(PROFILE_ARROWHEAD_LENGTH_UNFINISHED_STANDARD, arrowheadLengthUnfinishedStandard);
        pref.putFloat(PROFILE_ARROWHEAD_LENGTH_UNFINISHED_HIGHLIGHTED, arrowheadLengthUnfinishedHighlighted);
        pref.putFloat(PROFILE_ARROWHEAD_LENGTH_FINISHED_STANDARD, arrowheadLengthFinishedStandard);
        pref.putFloat(PROFILE_ARROWHEAD_LENGTH_FINISHED_HIGHLIGHTED, arrowheadLengthFinishedHighlighted);

        pref.putFloat(PROFILE_ARROWHEAD_WIDTH_UNFINISHED_STANDARD, arrowheadWidthUnfinishedStandard);
        pref.putFloat(PROFILE_ARROWHEAD_WIDTH_UNFINISHED_HIGHLIGHTED, arrowheadWidthUnfinishedHighlighted);
        pref.putFloat(PROFILE_ARROWHEAD_WIDTH_FINISHED_STANDARD, arrowheadWidthFinishedStandard);
        pref.putFloat(PROFILE_ARROWHEAD_WIDTH_FINISHED_HIGHLIGHTED, arrowheadWidthFinishedHighlighted);
    }

    public boolean isKnobVisible()
    {
        return knobVisible;
    }

    public void setKnobVisible(boolean knobVisibleNew)
    {
        boolean knobVisibleOld = this.knobVisible;
        this.knobVisible = knobVisibleNew;

        firePropertyChange(PROFILE_KNOB_VISIBLE, knobVisibleOld, knobVisibleNew);
    }

    public KnobOrientation getKnobOrientation()
    {
        return knobOrientation;
    }

    public void setKnobOrientation(KnobOrientation knobOrientationNew)
    {
        KnobOrientation knobOrientationOld = this.knobOrientation;
        this.knobOrientation = knobOrientationNew;

        firePropertyChange(PROFILE_KNOB_ORIENTATION, knobOrientationOld, knobOrientationNew);
    }

    public int getKnobWidth()
    {
        return knobWidth;
    }

    public void setKnobWidth(int knobWidthNew)
    {
        int knobWidthOld = this.knobWidth;
        this.knobWidth = knobWidthNew;

        firePropertyChange(PROFILE_KNOB_WIDTH, knobWidthOld, knobWidthNew);
    }

    public int getKnobHeight()
    {
        return knobHeight;
    }

    public void setKnobHeight(int knobHeightNew)
    {
        int knobHeightOld = this.knobHeight;
        this.knobHeight = knobHeightNew;

        firePropertyChange(PROFILE_KNOB_HEIGHT, knobHeightOld, knobHeightNew);
    }

    public float getArrowheadLengthUnfinishedStandard()
    {
        return arrowheadLengthUnfinishedStandard;
    }

    public void setArrowheadLengthUnfinishedStandard(float arrowheadLengthNew)
    {
        float arrowheadLengthUnfinishedOld = this.arrowheadLengthUnfinishedStandard;
        this.arrowheadLengthUnfinishedStandard = arrowheadLengthNew;

        firePropertyChange(PROFILE_ARROWHEAD_LENGTH_UNFINISHED_STANDARD, arrowheadLengthUnfinishedOld, arrowheadLengthNew);	
    }

    public float getArrowheadLengthUnfinishedHighlighted()
    {
        return arrowheadLengthUnfinishedHighlighted;
    }

    public void setArrowheadLengthUnfinishedHighlighted(float arrowheadLengthNew)
    {
        float arrowheadLengthUnfinishedOld = this.arrowheadLengthUnfinishedHighlighted;
        this.arrowheadLengthUnfinishedHighlighted = arrowheadLengthNew;

        firePropertyChange(PROFILE_ARROWHEAD_LENGTH_UNFINISHED_HIGHLIGHTED, arrowheadLengthUnfinishedOld, arrowheadLengthNew);	
    }



    public float getArrowheadLengthFinishedStandard()
    {
        return arrowheadLengthFinishedStandard;
    }

    public void setArrowheadLengthFinishedStandard(float arrowheadLengthNew)
    {
        float arrowheadLengthFinishedOld = this.arrowheadLengthFinishedStandard;
        this.arrowheadLengthFinishedStandard = arrowheadLengthNew;

        firePropertyChange(PROFILE_ARROWHEAD_LENGTH_FINISHED_STANDARD, arrowheadLengthFinishedOld, arrowheadLengthNew);
    }

    public float getArrowheadLengthFinishedHighlighted()
    {
        return arrowheadLengthFinishedHighlighted;
    }

    public void setArrowheadLengthFinishedHighlighted(float arrowheadLengthNew)
    {
        float arrowheadLengthFinishedOld = this.arrowheadLengthFinishedHighlighted;
        this.arrowheadLengthFinishedHighlighted = arrowheadLengthNew;

        firePropertyChange(PROFILE_ARROWHEAD_LENGTH_FINISHED_HIGHLIGHTED, arrowheadLengthFinishedOld, arrowheadLengthNew);
    }


    public float getArrowheadWidthUnfinishedStandard()
    {
        return arrowheadWidthUnfinishedStandard;
    }

    public void setArrowheadWidthUnfinishedStandard(float arrowheadWidthNew)
    {
        float arrowheadWidthOld = this.arrowheadWidthUnfinishedStandard;
        this.arrowheadWidthUnfinishedStandard = arrowheadWidthNew;

        firePropertyChange(PROFILE_ARROWHEAD_WIDTH_UNFINISHED_STANDARD, arrowheadWidthOld, arrowheadWidthNew);	
    }

    public float getArrowheadWidthUnfinishedHighlighted()
    {
        return arrowheadWidthUnfinishedHighlighted;
    }

    public void setArrowheadWidthUnfinishedHighlighted(float arrowheadWidthNew)
    {
        float arrowheadWidthOld = this.arrowheadWidthUnfinishedHighlighted;
        this.arrowheadWidthUnfinishedHighlighted = arrowheadWidthNew;

        firePropertyChange(PROFILE_ARROWHEAD_WIDTH_UNFINISHED_HIGHLIGHTED, arrowheadWidthOld, arrowheadWidthNew);	
    }


    public float getArrowheadWidthFinishedStandard()
    {
        return arrowheadWidthFinishedStandard;
    }

    public void setArrowheadWidthFinishedStandard(float arrowheadWidthNew)
    {
        float arrowheadWidthOld = this.arrowheadWidthFinishedStandard;
        this.arrowheadWidthFinishedStandard = arrowheadWidthNew;

        firePropertyChange(PROFILE_ARROWHEAD_WIDTH_FINISHED_STANDARD, arrowheadWidthOld, arrowheadWidthNew);
    }

    public float getArrowheadWidthFinishedHighlighted()
    {
        return arrowheadWidthFinishedHighlighted;
    }

    public void setArrowheadWidthFinishedHighlighted(float arrowheadWidthNew)
    {
        float arrowheadWidthOld = this.arrowheadWidthFinishedHighlighted;
        this.arrowheadWidthFinishedHighlighted = arrowheadWidthNew;

        firePropertyChange(PROFILE_ARROWHEAD_WIDTH_FINISHED_HIGHLIGHTED, arrowheadWidthOld, arrowheadWidthNew);
    }

    public boolean isArrowheadVisibleUnfinishedStandard()
    {
        return isArrowheadVisibleUnfinishedStandard;
    }

    public void setArrowheadVisibleUnfinishedStandard(boolean visibleNew)
    {
        boolean visibleOld = this.isArrowheadVisibleUnfinishedStandard;
        this.isArrowheadVisibleUnfinishedStandard = visibleNew;

        firePropertyChange(PROFILE_ARROWHEAD_VISIBLE_UNFINISHED_STANDARD, visibleOld, visibleNew);
    }

    public boolean isArrowheadVisibleUnfinishedHighlighted()
    {
        return isArrowheadVisibleUnfinishedHighlighted;
    }

    public void setArrowheadVisibleUnfinishedHighlighted(boolean visibleNew)
    {
        boolean visibleOld = this.isArrowheadVisibleUnfinishedHighlighted;
        this.isArrowheadVisibleUnfinishedHighlighted = visibleNew;

        firePropertyChange(PROFILE_ARROWHEAD_VISIBLE_UNFINISHED_HIGHLIGHTED, visibleOld, visibleNew);
    }


    public boolean isArrowheadVisibleFinishedStandard()
    {
        return isArrowheadVisibleFinishedStandard;
    }

    public void setArrowheadVisibleFinishedStandard(boolean visibleNew)
    {
        boolean visibleOld = this.isArrowheadVisibleFinishedStandard;
        this.isArrowheadVisibleFinishedStandard = visibleNew;

        firePropertyChange(PROFILE_ARROWHEAD_VISIBLE_FINISHED_STANDARD, visibleOld, visibleNew);
    }

    public boolean isArrowheadVisibleFinishedHighlighted()
    {
        return isArrowheadVisibleFinishedHighlighted;
    }

    public void setArrowheadVisibleFinishedHighlighted(boolean visibleNew)
    {
        boolean visibleOld = this.isArrowheadVisibleFinishedHighlighted;
        this.isArrowheadVisibleFinishedHighlighted = visibleNew;

        firePropertyChange(PROFILE_ARROWHEAD_VISIBLE_FINISHED_HIGHLIGHTED, visibleOld, visibleNew);
    }
}
