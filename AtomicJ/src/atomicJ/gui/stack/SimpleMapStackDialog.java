
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

package atomicJ.gui.stack;

import java.awt.Window;
import java.awt.geom.Point2D;
import java.util.prefs.Preferences;

import atomicJ.gui.MapChart;
import atomicJ.gui.SpectroscopySupervisor;
import atomicJ.gui.curveProcessing.RecalculateMapCurvesDialog;
import atomicJ.gui.curveProcessing.RecalculateMapCurvesModel;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.resources.MapImageResource;
import atomicJ.sources.SimpleSpectroscopySource;

public class SimpleMapStackDialog<E extends StackModel<MapImageResource>> 
extends SimpleStackView<E, MapChart<?>, StackPanel> implements  SpectroscopySupervisor
{
    private static final Preferences DEFAULT_PREF = Preferences.userNodeForPackage(StackMapView.class).node(StackMapView.class.getName());

    private final RecalculateMapCurvesDialog recalculateDialog;
    private final SpectroscopySupervisor parentSupervisor;

    public SimpleMapStackDialog(Window parent, SpectroscopySupervisor parentSupervisor,
            StackMapChart<?> chart, E stackModel, boolean temporary)
    {
        this(parent, parentSupervisor, chart, stackModel, DEFAULT_PREF, temporary);

    }

    public SimpleMapStackDialog(Window parent, SpectroscopySupervisor parentSupervisor, 
            StackMapChart<?> chart, E stackModel, Preferences pref, boolean temporary)
    {
        super(parent, chart, new StackPanel(true, false), stackModel, pref, temporary);		

        this.recalculateDialog = new RecalculateMapCurvesDialog(getAssociatedWindow(), false);

        this.parentSupervisor = parentSupervisor;
        getStackPanel().setSpectroscopySupervisor(this);
    }

    @Override
    public void jumpToResults(Point2D p) 
    {
        parentSupervisor.jumpToResults(getStackModel().getCorrespondingResource(), p);
    }

    @Override
    public void jumpToResults(MapImageResource resource, Point2D p) 
    {
        parentSupervisor.jumpToResults(resource, p);
    }

    @Override
    public void jumpToFigures(Point2D p) 
    {
        parentSupervisor.jumpToFigures(getStackModel().getCorrespondingResource(), p);
    }

    @Override
    public void jumpToFigures(MapImageResource resource, Point2D p) 
    {
        parentSupervisor.jumpToFigures(resource, p);
    }

    @Override
    public void jumpToResults(SimpleSpectroscopySource source) 
    {
        parentSupervisor.jumpToResults(source);
    }

    @Override
    public void jumpToFigures(SimpleSpectroscopySource source) 
    {
        parentSupervisor.jumpToFigures(source);
    }

    @Override
    public void recalculate(ROI roi, ROIRelativePosition position) 
    {
        parentSupervisor.recalculate(getStackModel().getCorrespondingResource(), roi, position);
    }

    @Override
    public void recalculate(MapImageResource resource, ROI roi, ROIRelativePosition position) 
    {
        parentSupervisor.recalculate(resource, roi, position);
    }

    @Override
    public void recalculate(Point2D p) 
    {
        parentSupervisor.recalculate(getStackModel().getCorrespondingResource(), p);
    }

    @Override
    public void recalculate(MapImageResource resource, Point2D p) 
    {
        parentSupervisor.recalculate(resource, p);
    }


    @Override
    public void recalculate(SimpleSpectroscopySource source) 
    {
        parentSupervisor.recalculate(source);	
    }

    @Override
    public void recalculateFullDialog()
    {
        RecalculateMapCurvesModel model = new RecalculateMapCurvesModel(getDrawableROIs(), getROIUnion());
        recalculateDialog.showDialog(model);

        if(model.isApplied())
        {
            recalculate(model.getSelectedROI(), model.getROIPosition());
        }
    }
}
