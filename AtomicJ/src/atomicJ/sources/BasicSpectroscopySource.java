
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

import static atomicJ.data.Datasets.RAW_CURVE;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import atomicJ.data.Channel1D;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.SimpleSpectroscopyCurve;
import atomicJ.data.StandardSample;
import atomicJ.data.StandardSampleCollection;

public class BasicSpectroscopySource extends AbstractChannelSource<Channel1D>
{
    private static final String APPROACH_X = "Approach X";
    private static final String APPROACH_Y = "Approach Y";

    private static final String WITHDRAW_X = "Withdraw X";
    private static final String WITHDRAW_Y = "Withdraw Y";

    private Channel1D approachData;
    private Channel1D withdrawData;

    public BasicSpectroscopySource(File f)
    {
        super(f);
    }

    public BasicSpectroscopySource(BasicSpectroscopySource sourceOld)
    {
        super(sourceOld);

        this.approachData = sourceOld.approachData.getCopy();
        this.withdrawData = sourceOld.withdrawData.getCopy();
    }

    @Override
    public BasicSpectroscopySource copy()
    {
        BasicSpectroscopySource copy = new BasicSpectroscopySource(this);
        return copy;
    }

    public void setApproachData(Channel1D approachData)
    {
        this.approachData = approachData;;
    }

    public void setWithdrawData(Channel1D withdrawData)
    {
        this.withdrawData = withdrawData;;
    }

    public SimpleSpectroscopyCurve getRecordedCurve()
    {
        SimpleSpectroscopyCurve afmCurve = new SimpleSpectroscopyCurve(approachData, withdrawData, RAW_CURVE);
        return afmCurve;
    }

    public Map<String, QuantitativeSample> getSamples()
    {
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        if(approachData != null && !approachData.isEmpty())
        {
            double[] approachXs = approachData.getXCoordinates();
            double[] approachYs = approachData.getYCoordinates();

            QuantitativeSample approachXsSample = new StandardSample(approachXs, APPROACH_X, approachData.getXQuantity().changeName(APPROACH_X),"","");
            QuantitativeSample approachYsSample = new StandardSample(approachYs, APPROACH_Y, approachData.getYQuantity().changeName(APPROACH_Y),"","");

            samples.put(APPROACH_X, approachXsSample );
            samples.put(APPROACH_Y, approachYsSample );
        }

        if(withdrawData != null && !withdrawData.isEmpty())
        {
            double[] withdrawXs = withdrawData.getXCoordinates();
            double[] withdrawYs = withdrawData.getYCoordinates();

            QuantitativeSample withdrawXsSample = new StandardSample(withdrawXs, WITHDRAW_X, withdrawData.getXQuantity().changeName(WITHDRAW_X),"","");
            QuantitativeSample withdrawYsSample = new StandardSample(withdrawYs, WITHDRAW_Y, withdrawData.getYQuantity().changeName(WITHDRAW_Y),"","");

            samples.put(WITHDRAW_X, withdrawXsSample );
            samples.put(WITHDRAW_Y, withdrawYsSample );
        }


        return samples;
    }

    @Override
    public List<SampleCollection> getSampleCollections()
    {
        SampleCollection collection = new StandardSampleCollection(getSamples(), getShortName(), getShortName(), getDefaultOutputLocation());

        List<SampleCollection> collections = Collections.singletonList(collection);
        return collections;
    }

    @Override
    public List<Channel1D> getChannels() 
    {
        return getNonEmptyChannels();
    }

    @Override
    public List<String> getIdentifiers()
    {
        List<String> identifiers = new ArrayList<>();
        List<Channel1D> nonEmptyChannels = getNonEmptyChannels();

        for(Channel1D ch: nonEmptyChannels)
        {
            identifiers.add(ch.getIdentifier());
        }

        return identifiers;
    }

    private List<Channel1D> getNonEmptyChannels()
    {
        List<Channel1D> channels = new ArrayList<>();

        if(approachData != null && !approachData.isEmpty())
        {
            channels.add(approachData);
        }

        if(withdrawData != null && !withdrawData.isEmpty())
        {
            channels.add(withdrawData);
        }

        return channels;
    }

    @Override
    public List<Channel1D> getChannels(Collection<String> identifiers) 
    {
        List<Channel1D> channelsForIdentifiers = new ArrayList<>();

        if(approachData != null && !approachData.isEmpty() && identifiers.contains(approachData.getIdentifier()))
        {
            channelsForIdentifiers.add(approachData);
        }

        if(withdrawData != null && !withdrawData.isEmpty() && identifiers.contains(withdrawData.getIdentifier()))
        {
            channelsForIdentifiers.add(withdrawData);
        }

        return channelsForIdentifiers;

    }
}
