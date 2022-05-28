
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

package atomicJ.sources;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;

import atomicJ.analysis.ForceCurveBranch;
import atomicJ.analysis.PhotodiodeSignalType;
import atomicJ.data.Channel1D;
import atomicJ.data.BasicSpectroscopyCurve;
import atomicJ.data.SpectroscopyCurve;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.curveProcessing.ProcessingBatchMemento;
import atomicJ.readers.MapDelayedCreator;
import atomicJ.readers.ReadingPack;


public interface SimpleSpectroscopySource extends SpectroscopySource, Channel1DSource<Channel1D>
{
    public static final String APPROACH = "Approach";
    public static final String WITHDRAW = "Withdraw";
    public static final String PAUSE = "Pause";
    public static final String DWELL = "Dwell";

    public boolean isSensitivityCalibrated();
    public boolean isSensitivityKnown();
    public double getSensitivity();
    public boolean isForceCalibrated();
    public boolean isSpringConstantKnown();
    public double getSpringConstant();

    public PrefixedUnit getSingleDataUnit();

    public ProcessingBatchMemento getProcessingMemento();
    public void setProcessingMemento(ProcessingBatchMemento memento);

    public boolean isFromMap();
    public void setForceMap(MapSource<?> src);
    public void setRecordingPoint(Point2D p);
    public Point2D getRecordingPoint();

    public default boolean isCorrespondingMapPositionKnown()
    {
        boolean known = (isFromMap() && getRecordingPoint() != null);
        return known;
    }

    public void setMapPosition(int mapPosition);
    public int getMapPosition();
    public String getMapPositionDescription();
    public MapSource<?> getForceMap();
    public MapDelayedCreator getMapDelayedCreator();
    public void setMapDelayedCreator(MapDelayedCreator mapDelayedCreator);
    public SpectroscopyCurve<Channel1D> getRecordedCurve();

    public boolean canBeUsedForCalibration(PhotodiodeSignalType signalType);

    public ReadingPack<ImageSource> getAccompanyingImageReadingPack();
    public void setAccompanyingImageReadingPack(ReadingPack<ImageSource> accompanyingImageInfo);
    public boolean isAccompanyingImagesAvailable();

    public List<BasicSpectroscopySource> getAdditionalCurveRecordings();

    //spring constant should be in nN/micron, i.e. 1000 times more then the SI value,
    //and sensitivity in microns/V, i.e. 10^6 times more then the SI value
    public BasicSpectroscopyCurve<Channel1D> getRecordedDeflectionCurve(double sensitivity, double springConstant);
    //spring constant should be in nN/micron, i.e. 1000 times more then the SI value
    //and sensitivity in microns/V, i.e. 10^6 times more then the SI value
    public BasicSpectroscopyCurve<Channel1D> getRecordedForceCurve(double sensitivity, double springConstant);
    //spring constant should be in nN/micron, i.e. 1000 times more then the SI value,
    //and sensitivity in microns/V, i.e. 10^6 times more then the SI value
    public BasicSpectroscopyCurve<Channel1D> getRecordedPhotodiodeCurve(double sensitivity, double springConstant) throws UserCommunicableException;
    public Channel1D getRecordedPhotodiodeApproachCurve(double sensitivity,
            double springConstant) throws UserCommunicableException;
    public Channel1D getRecordedPhotodiodeWithdrawCurve(double sensitivity,
            double springConstant) throws UserCommunicableException;

    public boolean containsApproachData();
    public boolean containsWithdrawData();
    public boolean containsData(ForceCurveBranch branch);

    @Override
    public List<Channel1D> getChannels();
    @Override
    public List<Channel1D> getChannels(Collection<String> identifiers);

    @Override
    public SimpleSpectroscopySource copy();
    public PhotodiodeSignalType getPhotodiodeSignalType();
    public void setPhotodiodeSignalType(PhotodiodeSignalType photodiodeSignalType);

    static boolean containsSourcesFromMap(List<SimpleSpectroscopySource> sources)
    {
        for(SimpleSpectroscopySource source : sources)
        {
            if(source.isCorrespondingMapPositionKnown())
            {
                return true;
            }
        }

        return false;
    }
}
