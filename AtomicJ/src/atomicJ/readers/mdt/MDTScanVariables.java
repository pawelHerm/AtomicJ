package atomicJ.readers.mdt;

import java.nio.ByteBuffer;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;
import atomicJ.utilities.FileInputUtilities;

public class MDTScanVariables 
{
    private final int channelIndex; //unsigned byte (size 1), s_mode, measurement channel index – use one of two ADC channel name sets depending on s_a12 field to decode it to channel name (see below)
    private final MDTMicroscopyType microscopyType; //unsigned byte (size 1), s_dev, (0: STM mode, 1: AFM mode)
    private final int scanPointNoX; // signed short (size 2), s_nx, number of scan points in X direction
    private final int scanPointNoY; //signed short (size 2), s_ny, number of scan points in Y direction
    private final int reserved1; //signed short (size 2), s_rv6, now: reserved(obsolete: number of DAC quant’s in step)
    private final UnitExpression stepLength;
    private final int measurementToAverageCount; //usigned short (2 bytes), s_adt, number of single measurements to average (DOS version – in Win version depends on scan velocity) 
    private final int adcAplifierGainExponent; //unsigned byte (1 byte), s_adc_a, ADC amplifier gain as power of 10 (0: Gain=1, 1: Gain=10, 2: Gain=100, 3: Gain=1000) is set at the beginning of scanning and set back at the end
    private final MDTADCIndex adcIndex; //unsigned byte (1 byte) ADC index (is used to determine a channel set to use to define measurement channel from  s_mode field) (0: ADC1; 1:ADC2)
    /*!! VERSION DEPENDENT MEANING !!*/  private final int s_smp_in; //unsigned byte (1 byte) {electric input signal on Probe (0: Extension Slot #5,1: BiasV, 2: Ground) If Probe is grounded(s_smp_in=2) then potential is applied to sample in all other cases
    //sample is grounded and potential is applied to the probe} program version 8xx (8..46,48,…)
    private final int subtractedSurfaceOrder; //unsigned byte (1 byte), 
    private final int scanDirection; //usigned byte (1 byte), s_xy, scan direction, Scan direction (0:+Y+,1:+X+,2:-Y+,3:-X+,4:+Y-,5:+X-;6:-Y-;7:-X-) + 80h – in case of double pass scan
    private final boolean sizeIsPower2; //boolean, 1 byte, Nx, Ny=2^n - Arrays of this type are optimal for FFT and other numerical algorithms 
    private final double scanVelocity; //float (4 bytes), s_vel, scan velocity in Angstrom/second
    private final double setPoint; //float (4 bytes), s_i0, setpoint value
    private final double biasVoltage; //float, 4 bytes, s_ut, bias Voltage (Ut – tunnel current voltage) – potential between tip and sample or vice versa depending on s_smp_in field .
    private final boolean sDraw; //boolean, 1 byte, Show scanning results in progress – abolishes  all progress displaying features during scanning

    //reserved field, 1 byte, is skipped

    private final int xOffset; //4 bytes, s_x00, X offset in quant’s of DAC
    private final int yOffset; //4 bytes, s_y00, Y offset in quant's of DAC
    private final boolean isNonLinearCorrection; // 1 byte, s_cor, non-linear correction (0:Off,1:On)
    private final int fileOriginalFormatType; //1 byte, unsigned, s_oem, (0: MDT - original mdt file, <>0: Converted from other formats:   1: Park, 2: Omicron, 3: RHK, 4: DI, 5: Topometrics, 6: RScope, 7: WAT file, 8: PCX, 9: PGM, ….)
    private final int s_shos; //'1 byte, unsigned, DOS version - run oscillographs during scanning
    private final int s_shsl; //1 byte, unsigned, DOS version - show every scan line during scanning (00b:do not show, 10:show, 01b: show with autoscaling)
    private final MDTLiftModeStrategy liftModeStrategy;
    private final double feedbackGain;//4 bytes, float, s_fbg, feedback gain
    private final int dacQuantPerStep;//integer, 4 bytes, number of DAC quant’s in step for DOS version: ScanSize=nx*s_s*x.r, for Win version: ScanSize=nx*x.r
    private final boolean redrawEachScan; //1 byte, s_resc, redraw each scan

    private final MDTChannel channel;

    private MDTScanVariables(ByteBuffer buffer)
    {
        //the MDT technical notes state that the channelIndex, i.e. the s_mode field, is of the type uint and size 1 byte
        //however, it seems that this is not the case
        //as the same notes state somewhere else that the channelIndex can equal -1 
        this.channelIndex = buffer.get(); //unsigned byte (size 1), s_mode, measurement channel index – use one of two ADC channel name sets depending on s_a12 field to decode it to channel name (see below)
        this.microscopyType = MDTMicroscopyType.getMDTMicroscopyType(FileInputUtilities.getUnsigned(buffer.get())); //unsigned byte (size 1), s_dev, (0: STM mode, 1: AFM mode)
        this.scanPointNoX = buffer.getShort(); // signed short (size 2), s_nx, number of scan points in X direction
        this.scanPointNoY = buffer.getShort(); //signed short (size 2), s_ny, number of scan points in Y direction
        this.reserved1 = buffer.getShort(); //signed short (size 2), s_rv6, now: reserved (obsolete: number of DAC quant’s in step)
        this.stepLength = new UnitExpression(buffer.getFloat()/10., Units.NANO_METER_UNIT); //float (4 bytes), s_rs, step length in Angstroms
        this.measurementToAverageCount = FileInputUtilities.getUnsigned(buffer.getShort()); //unsigned short (2 bytes), s_adt, number of single measurements to average (DOS version – in Win version depends on scan velocity) 
        this.adcAplifierGainExponent = FileInputUtilities.getUnsigned(buffer.get()); //unsigned byte (1 byte), s_adc_a, ADC amplifier gain as power of 10 (0: Gain=1, 1: Gain=10, 2: Gain=100, 3: Gain=1000) is set at the beginning of scanning and set back at the end

        this.adcIndex = MDTADCIndex.getMDTADCIndex(FileInputUtilities.getUnsigned(buffer.get())); //unsigned byte (1 byte) ADC index (is used to determine a channel set to use to define measurement channel from  s_mode field) (0: ADC1; 1:ADC2)
        this.s_smp_in = FileInputUtilities.getUnsigned(buffer.get()); //unsigned byte (1 byte) {electric input signal on Probe (0: Extension Slot #5,1: BiasV, 2: Ground) If Probe is grounded(s_smp_in=2) then potential is applied to sample in all other cases
        //sample is grounded and potential is applied to the probe} program version 8xx (8..46,48,…)
        this.subtractedSurfaceOrder = FileInputUtilities.getUnsigned(buffer.get()); //unsigned byte (1 byte), 
        this.scanDirection = FileInputUtilities.getUnsigned(buffer.get()); //usigned byte (1 byte), s_xy, scan direction, Scan direction (0:+Y+,1:+X+,2:-Y+,3:-X+,4:+Y-,5:+X-;6:-Y-;7:-X-) + 80h – in case of double pass scan
        this.sizeIsPower2 = buffer.get() != 0; //boolean, 1 byte, Nx, Ny=2^n - Arrays of this type are optimal for FFT and other numerical algorithms 
        this.scanVelocity = buffer.getFloat(); //float (4 bytes), s_vel, scan velocity in Angstrom/second
        this.setPoint = buffer.getFloat(); //float (4 bytes), s_i0, setpoint value
        this.biasVoltage = buffer.getFloat(); //float, 4 bytes, s_ut, bias Voltage (Ut – tunnel current voltage) – potential between tip and sample or vice versa depending on s_smp_in field .
        this.sDraw = buffer.get() != 0; //boolean, 1 byte, Show scanning results in progress – abolishes  all progress displaying features during scanning

        buffer.get();// skips reserved byte

        this.xOffset = buffer.getInt(); //4 bytes, s_x00, X offset in quant’s of DAC
        this.yOffset = buffer.getInt(); //4 bytes, s_y00, Y offset in quant's of DAC
        this.isNonLinearCorrection = buffer.get() != 0; // 1 byte, s_cor, non-linear correction (0:Off,1:On)
        this.fileOriginalFormatType = FileInputUtilities.getUnsigned(buffer.get()); //1 byte, unsigned, s_oem, (0: MDT - original mdt file, <>0: Converted from other formats:   1: Park, 2: Omicron, 3: RHK, 4: DI, 5: Topometrics, 6: RScope, 7: WAT file, 8: PCX, 9: PGM, ….)
        this.s_shos = FileInputUtilities.getUnsigned(buffer.get()); //1 byte, unsigned, DOS version - run oscillographs during scanning
        this.s_shsl = FileInputUtilities.getUnsigned(buffer.get()); //1 byte, unsigned, DOS version - show every scan line during scanning (00b:do not show, 10:show, 01b: show with autoscaling)
        this.liftModeStrategy = MDTLiftModeStrategy.getMDTLiftModeStrategy(buffer.get());
        this.feedbackGain = buffer.getFloat();
        this.dacQuantPerStep = buffer.getInt();
        this.redrawEachScan = buffer.get() != 0;

        this.channel = MDTChannel.getMDTChannel(channelIndex, adcIndex);
    }

    public static MDTScanVariables readIn(ByteBuffer buffer)
    {
        MDTScanVariables scanVariables = new MDTScanVariables(buffer);
        return scanVariables;
    }

    public int getXPointCount()
    {
        return scanPointNoX;
    }

    public int getYPointCount()
    {
        return scanPointNoY;
    }

    public int getChannelIndex()
    {
        return channelIndex;
    }

    public MDTADCIndex getADCIndex()
    {
        return adcIndex;
    }

    public MDTChannel getChannel()
    {
        return channel;
    }

    public UnitExpression getStepLength()
    {
        return stepLength;
    }

    public static enum MDTMicroscopyType
    {
        STM("STM",0), AFM("AFM",0);

        private final String name;
        private final int code;

        MDTMicroscopyType(String name, int code)
        {
            this.name = name;
            this.code = code;
        }

        @Override
        public String toString()
        {
            return name;
        }

        public static MDTMicroscopyType getMDTMicroscopyType(int code)
        {
            for(MDTMicroscopyType type : MDTMicroscopyType.values())
            {
                if(type.code == code)
                {
                    return type;
                }
            }

            return null;

            //            throw new IllegalArgumentException("Invalid MDTMicroscopyType code: " + code);
        }
    }

    public static enum MDTLiftModeStrategy
    {
        STEP(0), FINE_INTERPOLATION(1), AVERAGE_SLOPE_CALCULATION_AND_LINEAR_MOVEMENT(2);

        private final int code;

        MDTLiftModeStrategy(int code)
        {
            this.code = code;
        }

        public static MDTLiftModeStrategy getMDTLiftModeStrategy(int code)
        {
            for(MDTLiftModeStrategy strategy : MDTLiftModeStrategy.values())
            {
                if(strategy.code == code)
                {
                    return strategy;
                }
            }

            throw new IllegalArgumentException("Invalid MDTLiftModeStrategy code: " + code);
        }
    }

    public static enum MDTADCIndex
    {
        ADC_1("ADC1",0), ADC_2("ADC2",1);

        private final String name;
        private final int code;

        MDTADCIndex(String name, int code)
        {
            this.name = name;
            this.code = code;
        }

        @Override
        public String toString()
        {
            return name;
        }

        public static MDTADCIndex getMDTADCIndex(int code)
        {
            for(MDTADCIndex adcIndex : MDTADCIndex.values())
            {
                if(adcIndex.code == code)
                {
                    return adcIndex;
                }
            }

            throw new IllegalArgumentException("Invalid MDTADCIndex code: " + code);
        }
    }

    public static enum MDTChannel
    {
        OFF("Off", -1), HEIGHT("Height", 0), DFL("DFL", 1), LATERAL_F("Lateral F", 2), BIAS_V("Bias V", 3), 
        CURRENT("Current", 4), FB_OUT("FB-Out", 5), MAG("MAG", 6), MAG_SIN("MAG*Sin", 7), MAG_COS("MAG*Cos", 8),
        RMS("RMS", 9),CALC_MAG("CalcMAG", 10), PHASE_1("Phase 1", 11), PHASE_2("Phase 2", 12), CALC_PHASE("CalcPhase", 13),
        EX_1("Ex1", 14, false), EX_2("Ex2", 15, false),HV_X("HvX", 16), HV_Y("HvY", 17), SNAP_BACK("SnapBack", 18);

        private final String name;
        private final int code;
        private final boolean adc2Supported;

        MDTChannel(String name, int code)
        {
            this(name, code, true);
        }

        MDTChannel(String name, int code, boolean adc2Supported)
        {
            this.name = name;
            this.code = code;
            this.adc2Supported = adc2Supported;
        }

        public String getName()
        {
            return name;
        }

        public int getCode()
        {
            return code;
        }

        public boolean isADCIndexSupported(MDTADCIndex adcIndex)
        {
            boolean supported = MDTADCIndex.ADC_1.equals(adcIndex) ? true : adc2Supported;
            return supported;
        }

        @Override
        public String toString()
        {
            return name;
        }

        public static MDTChannel getMDTChannel(int code)
        {
            for(MDTChannel channel : MDTChannel.values())
            {
                if(channel.code == code)
                {
                    return channel;
                }
            }

            throw new IllegalArgumentException("Invalid MDTChannel code: " + code);
        }

        public static MDTChannel getMDTChannel(int code, MDTADCIndex adcIndex)
        {
            MDTChannel channel = getMDTChannel(code);

            if(!channel.isADCIndexSupported(adcIndex))
            {
                throw new IllegalArgumentException("The channel " + channel.toString() + " does not support the ADC index " + adcIndex.toString());
            }

            return channel;
        }
    }
}
