
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

package atomicJ.resources;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;

import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.data.Channel1D;
import atomicJ.data.ChannelGroupTag;
import atomicJ.data.SampleCollection;

public interface SpectroscopyProcessedResource extends SpectroscopyResource
{
    public List<SampleCollection> getSampleCollectionsRawData();
    public Point2D getValidItemPosition(String type, String channelIdentifier, int itemIndex, Point2D dataPoint);
    public boolean isValidPosition(String type, String channelIdentifier, int itemIndex, Point2D dataPoint);
    public ChannelGroupTag getNextGroupMemberIdentity(String type, Object groupKey);

    public void itemMoved(String selectedType, Channel1D channel, int itemIndex, double[] newValue);
    public void channelTranslated(Channel1D channel);

    public Channel1D getChannel(String type, Object identifier);
    public void addChannel(String type, Channel1D channel);
    public void removeChannel(String type, Channel1D channel);

    public Optional<Channel1DDataTransformation> getFixContactXTransformationIfPossible();
    public Optional<Channel1DDataTransformation> getFixContactYTransformationIfPossible();
    public Optional<Channel1DDataTransformation> getFixContactAllAxesTransformationIfPossible();
    public Optional<Channel1DDataTransformation> getTransformationToForceSeparationIfPossible();
}
