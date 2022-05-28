
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
import java.util.Map;

import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.Channel1DStandard;
import atomicJ.data.Quantities;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SimpleSpectroscopyCurve;
import atomicJ.data.StandardSample;


public class DynamicSpectroscopySource extends StandardSimpleSpectroscopySource implements SimpleSpectroscopySource
{
    private static final String AMPLITUDE_APPROACH = "Amplitude Approach";
    private static final String AMPLITUDE_WITHDRAW = "Amplitude Withdraw";

    private static final String PHASE_APPROACH = "Phase Approach";
    private static final String PHASE_WITHDRAW = "Phase Withdraw";

    private static final String AMPLITUDE_APPROACH_Y = "Amplitude Approach Y";
    private static final String AMPLITUDE_APPROACH_X = "Amplitude Approach X";
    private static final String AMPLITUDE_WITHDRAW_X = "Amplitude Withdraw X";
    private static final String AMPLITUDE_WITHDRAW_Y = "Amplitude Withdraw Y";

    private static final String PHASE_APPROACH_X = "Phase Approach X";
    private static final String PHASE_APPROACH_Y = "Phase Approach Y";
    private static final String PHASE_WITHDRAW_X = "Phase Withdraw X";
    private static final String PHASE_WITHDRAW_Y = "Phase Withdraw Y";

    public static final String AMPLITUDE_CURVE = "Amplitude curve";
    public static final String PHASE_CURVE = "Phase curve";

    private Channel1D amplitudeApproachChannel;
    private Channel1D amplitudeWithdrawChannel;

    private Channel1D phaseApproachChannel;
    private Channel1D phaseWithdrawChannel;

    public DynamicSpectroscopySource(File f, Channel1DData deflectionApproachData, Channel1DData deflectionWithdrawData)
    {
        super(f, deflectionApproachData, deflectionWithdrawData);	
    }
    public DynamicSpectroscopySource(File f, String shortName, String longName, Channel1DData deflectionApproachData, Channel1DData deflectionWithdrawData)
    {
        super(f, shortName, longName, deflectionApproachData, deflectionWithdrawData);
    }

    public DynamicSpectroscopySource(DynamicSpectroscopySource sourceOld)
    {
        super(sourceOld);

        this.amplitudeApproachChannel = sourceOld.amplitudeApproachChannel.getCopy();
        this.amplitudeWithdrawChannel = sourceOld.amplitudeWithdrawChannel.getCopy();

        this.phaseApproachChannel = sourceOld.phaseApproachChannel.getCopy();
        this.phaseWithdrawChannel = sourceOld.phaseWithdrawChannel.getCopy();
    }

    @Override
    public DynamicSpectroscopySource copy()
    {
        DynamicSpectroscopySource copy = new DynamicSpectroscopySource(this);
        return copy;
    }

    public void setAmplitudeData(Channel1DData approachData, Channel1DData withdrawData)
    {
        this.amplitudeApproachChannel = new Channel1DStandard(approachData, AMPLITUDE_APPROACH);
        this.amplitudeWithdrawChannel = new Channel1DStandard(withdrawData, AMPLITUDE_WITHDRAW);
    }

    public void setPhaseData(Channel1DData approachData, Channel1DData withdrawData)
    {
        this.phaseApproachChannel = new Channel1DStandard(approachData, PHASE_APPROACH);
        this.phaseWithdrawChannel = new Channel1DStandard(withdrawData, PHASE_WITHDRAW);
    }

    public SimpleSpectroscopyCurve getRecordedAmplitudeCurve()
    {
        SimpleSpectroscopyCurve afmCurve = new SimpleSpectroscopyCurve(amplitudeApproachChannel, amplitudeWithdrawChannel, RAW_CURVE);
        return afmCurve;
    }

    public SimpleSpectroscopyCurve getRecordedPhaseCurve()
    {
        SimpleSpectroscopyCurve afmCurve = new SimpleSpectroscopyCurve(phaseApproachChannel, phaseWithdrawChannel, RAW_CURVE);
        return afmCurve;
    }

    @Override
    public Map<String, QuantitativeSample> getSamples()
    {
        Map<String, QuantitativeSample> samples = super.getSamples();

        if(amplitudeApproachChannel != null && !amplitudeApproachChannel.isEmpty())
        {
            double[] approachXs = amplitudeApproachChannel.getXCoordinates();
            double[] approachYs = amplitudeApproachChannel.getYCoordinates();

            QuantitativeSample approachXsSample = new StandardSample(approachXs, AMPLITUDE_APPROACH_X, amplitudeApproachChannel.getXQuantity().changeName("Approach X"));
            QuantitativeSample approachYsSample = new StandardSample(approachYs, AMPLITUDE_APPROACH_Y, amplitudeApproachChannel.getYQuantity().changeName(AMPLITUDE_APPROACH_Y));

            samples.put(AMPLITUDE_APPROACH_X, approachXsSample );
            samples.put(AMPLITUDE_APPROACH_Y, approachYsSample );
        }

        if(amplitudeWithdrawChannel != null && !amplitudeWithdrawChannel.isEmpty())
        {
            double[] withdrawXs = amplitudeWithdrawChannel.getXCoordinates();
            double[] withdrawYs = amplitudeWithdrawChannel.getYCoordinates();

            QuantitativeSample withdrawXsSample = new StandardSample(withdrawXs, AMPLITUDE_WITHDRAW_X, amplitudeWithdrawChannel.getXQuantity().changeName("Withdraw X"));
            QuantitativeSample withdrawYsSample = new StandardSample(withdrawYs, AMPLITUDE_WITHDRAW_Y, amplitudeWithdrawChannel.getYQuantity().changeName(AMPLITUDE_WITHDRAW_Y));

            samples.put(AMPLITUDE_WITHDRAW_X, withdrawXsSample );
            samples.put(AMPLITUDE_WITHDRAW_Y, withdrawYsSample );
        }

        if(phaseApproachChannel != null && !phaseApproachChannel.isEmpty())
        {
            double[] approachXs = phaseApproachChannel.getXCoordinates();
            double[] approachYs = phaseApproachChannel.getYCoordinates();

            QuantitativeSample approachXsSample = new StandardSample(approachXs, PHASE_APPROACH_X, phaseApproachChannel.getXQuantity().changeName("Approach X"));
            QuantitativeSample approachYsSample = new StandardSample(approachYs, PHASE_APPROACH_Y, phaseApproachChannel.getYQuantity().changeName(PHASE_WITHDRAW_Y));

            samples.put(PHASE_APPROACH_X, approachXsSample );
            samples.put(PHASE_APPROACH_Y, approachYsSample );
        }

        if(phaseWithdrawChannel != null && !phaseWithdrawChannel.isEmpty())
        {
            double[] withdrawXs = phaseWithdrawChannel.getXCoordinates();
            double[] withdrawYs = phaseWithdrawChannel.getYCoordinates();

            QuantitativeSample withdrawXsSample = new StandardSample(withdrawXs, PHASE_WITHDRAW_X, Quantities.DISTANCE_MICRONS.changeName("Withdraw X"));
            QuantitativeSample withdrawYsSample = new StandardSample(withdrawYs, PHASE_WITHDRAW_Y, Quantities.PHASE_DEGREES.changeName(PHASE_WITHDRAW_Y));

            samples.put(PHASE_WITHDRAW_X, withdrawXsSample );
            samples.put(PHASE_WITHDRAW_Y, withdrawYsSample );
        }
        return samples;
    }
}
