
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

import java.awt.Cursor;
import java.awt.Window;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

import atomicJ.gui.measurements.DistanceMeasurementReceiver;
import atomicJ.gui.profile.ProfileReceiver;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIReceiver;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.rois.WandContourTracer;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.Channel2DDataTransformation;
import atomicJ.utilities.MultiMap;


public interface Channel2DSupervisor extends MouseInteractiveToolSupervisor, ProfileReceiver, ROIReceiver, 
MapMarkerReceiver, DistanceMeasurementReceiver, DomainMarkerReceiver
{
    public static final String ROI_SAMPLES_NEEDED = "ROISampleNeeded";

    public void transform(Channel2DDataTransformation tr);
    public void transform(Channel2DDataInROITransformation tr, ROIRelativePosition position);

    public MouseInputMode getMode();
    public void setMode(MouseInputMode mode);

    public MouseInputMode getMode(MouseInputType inputType);
    public void setAccessoryMode(MouseInputType inputType, MouseInputMode modeNew);

    public boolean areHistogramsAvaialable();
    public void showHistograms();
    public void drawHistograms();
    public void drawROIHistograms();
    public void drawROIHistograms(List<ROI> rois);

    public void showRawData();
    public void showROIRawData();
    public void showROIRawData(List<ROI> rois);

    public void showStatistics();
    public void showROIStatistics();
    public void showROIStatistics(Map<Object, ROI> rois);

    public void setROIStyleEditorVisible(boolean visible);
    public void setProfileStyleEditorVisible(boolean visible);

    public void setProfilesAvailable(boolean b);

    public int getCurrentROIIndex();
    public ROI getROIUnion();
    public Map<Object, ROI> getDrawableROIs();
    public Map<Object, ROI> getAllROIs();
    public boolean areROIsAvailable();
    public void setROIsAvailable(boolean b);
    public WandContourTracer getWandTracer();

    public void requestCursorChange(Cursor cursor);
    public void requestCursorChange(Cursor horizontalCursor, Cursor verticalCursor);

    public void setProfileGeometryVisible(boolean visible);
    public void showROIShapeFactors();

    public void addMarkerKnob(Object markerKey, double knobPosition);
    public void removeMarkerKnob(Object markerKey, int knobIndex);
    public void moveMarkerKnob(Object markerKey, int knobIndex, double knobPositionNew);
    public void respondToValueMarkerMovement(Point2D newPosition, Object markerKey);

    public void editGradient();
    public void editHistogramGradient();
    public void showGradientChooser();
    public void showGradientChooser(ColorGradientReceiver gradientReceiver);
    public void showGradientChooser(List<ColorGradientReceiver> gradientReceiver);
    public MultiMap<String, ? extends Channel2DChart<?>> getAllNonEmptyCharts();

    public void notifyAboutROISampleNeeded();
    public void notifyAboutAspectRatioLock();

    public Window getPublicationSite();
}
