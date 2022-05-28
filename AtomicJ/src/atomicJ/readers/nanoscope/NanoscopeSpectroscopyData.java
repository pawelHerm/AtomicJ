/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 - 2018 by Pawe³ Hermanowicz
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


package atomicJ.readers.nanoscope;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;

import atomicJ.data.Grid1D;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.IntArrayReaderType;
import atomicJ.utilities.FileInputUtilities;

//In .spm files, there may be two similar reference fields:
//1) "\@Sens. Zsens:" in "\*Scanner list"
//2) "\@Sens. ZsensSens:" in "\*Ciao scan list"

//comments about Peak Force capture files

//the peak force amplitude can be found in the field "\Peak Force Amplitude", under the header "\*Ciao scan list"
//It is not written in the "\*Ciao image list records" that are represented by NanoscopePFCData objects, so this property of the NanoscopePFCData objects has to
//be read from the NanoscopeScaleList object and set for the NanoscopePFCData objects

// field "\PeakForce Capture" must be present and its value must be "Allow" for the ramp to be
//recognized as sinusoidal by the Nanoscope software

//field "\Sync distance" is necessary for Nanoscope to recognize that the ramp is sinusoidal
//other "Sync distance ..." fields cannot replace it


//Several fields are read in from under the header "\*Ciao scan list"
//Those are:"
// A) peakForceAmplitude (field "\Peak Force Amplitude")
// B) syncDistance (field "\Sync Distance QNM" or "\Sync Distance", see below)
// C) samplePoints ("\Sample Points")
// D) peakForceCaptureAllowed ("\PeakForce Capture")

//They are not written in the "\*Ciao force image list" records that are represented by NanoscopeSPMData objects,
//so these properties of the NanoscopeSPMData objects have to
//be read from the NanoscopeScanList object and set for the NanoscopeSPMData objects

//SPM files may contain more than one field that starts with Sync distance


//To find whether a Ciao Force image list contains deflection curve data, Nanoscope looks for the parameter Image Data (case-sensitive). Its form
//is  [internal-designation for selection] "external-designation for selection". 
//Nanoscope takes into account "external-designation for selection". External designation must be in double quotes,
//if it is not quoted, then its value is disregarded. Likewise, any text after the quoted external designations disregarded.
//if the external designation starts with Defl, then Nanoscope assumes that the current Ciao Force image list
//contains deflection curve


//Fields \Hold Samples: 59523 i \Hold Time: 5 are read from \*Ciao force list to establish to position of hold segments and force curves, also the duration of hold segments.
//I've verified this modifying Nanoscope files in a hex editor and checking how those modifications affect the way Matlab Toolbox reads the files


//using Matlab Toolbox, I verified that Z-offset field of the Height Sensor should be ignored
//only hard scale from the Z-scale field should be used,
//together with the soft scale is Sens. ZsensSens


//the \Data length filed in the \*Ciao force image list section is calculated based on the assumption that the length of both branches of the curve is the same and equal to the first number specified in the \Samps line field
//i.e. equal to the length of the withdraw branch. This means that if approach is longer than withdraw, we may have to read more bytes than specified in the \Data length field

public class NanoscopeSpectroscopyData extends NanoscopeData
{
    private static final String FORCE_IDENTIFIER = "\\*Ciao force image list";

    private static final String DATA_TYPE = "image data";

    private static final String RAMP_SIZE = "ramp size"; //we will use only the lower case field names, 
    //as Bruker files use lower and upper case letters inconsistently wet must compare this constants to the field.toLowerCase()

    //some older Digital Instruments files contain "Z scan size" field instead of "Ramp size" field
    private static final String Z_SCAN_SIZE = "z scan size";

    private static final String SAMPLES_PER_LINE = "samps/line";
    private static final String SPRING_CONSTANT = "spring constant";

    private NanoscopeSpectroscopyDataType dataType;

    private UnitExpression zScanSizeHardValue;
    private String zScanSizeSoftReference;
    private UnitExpression softzScanSizeScale;

    private UnitExpression rampSizeHardValue;
    private String rampSizeSoftReference;
    private UnitExpression softRampSizeScale;

    private int approachLengthReadIn = 0;
    private int withdrawLengthReadIn = 0;
    private double springConstant = Double.NaN;
    private double sensitivity = Double.NaN;

    private int pointCountInSingleHoldSegment = 0;

    private double peakForceAmplitude = Double.NaN;
    private double syncDistanceForAnalysis = Double.NaN;
    private int samplePoints = -1;
    private boolean peakForceCaptureAllowed = false;

    public NanoscopeSpectroscopyData()
    {
        super(FORCE_IDENTIFIER);
    }

    public static boolean isSectionBeginningStatic(String line)
    {
        boolean beginning = line != null && FORCE_IDENTIFIER.equals(line.trim());
        return beginning;
    }

    protected int getFirstIndex()
    {
        double numFcPoints = 2*getReadInWithdrawLength(); // Bruker's Matlab toolbox ignores the length of approach and assumes that withdraw curve and approach curve are of the same length. In Samps/Line field, withdraw length is specified first 
        //in addition, I saw PFC files that have wrong approach length specified (provided by Denis)

        double syncDstIndex = syncDistanceForAnalysis*(numFcPoints/samplePoints);

        double firstIndex = syncDstIndex - numFcPoints/2. + 1;
        if (firstIndex < 0)
        {
            firstIndex += numFcPoints;
        }

        return (int)Math.rint(firstIndex);
    }

    public double getPeakForceAmplitude()
    {
        return peakForceAmplitude;
    }

    public double getSyncDistanceFractionCorrectionFactor()
    {
        double fraction = syncDistanceForAnalysis - Math.floor(syncDistanceForAnalysis);

        double factor  = fraction < 0.5 ? -fraction : (1 - fraction);
        return factor;
    }

    protected boolean isSinusoidalRamp()
    {
        boolean isSinusoidalRamp = peakForceCaptureAllowed 
                && !Double.isNaN(peakForceAmplitude) && !Double.isNaN(syncDistanceForAnalysis) && samplePoints > -1;

                return isSinusoidalRamp;
    }

    protected boolean isLinearRampSpecified()
    {
        //checks whether ramp size is known
        boolean linearSpecified = rampSizeHardValue != null;
        linearSpecified = linearSpecified && softRampSizeScale != null;

        return linearSpecified;
    }

    protected boolean isZScanSizeSpecified()
    {
        //checks whether ramp size is known
        boolean specified = zScanSizeHardValue != null && softzScanSizeScale != null;;

        return specified;
    }

    @Override
    public boolean isFullySpecified()
    {
        boolean fullySpecified = super.isFullySpecified();
        boolean rampSpecified = isSinusoidalRamp() || isLinearRampSpecified() || isZScanSizeSpecified();
        boolean specified = fullySpecified && rampSpecified;

        return specified;
    }

    public double getSpringConstant()
    {
        return springConstant;
    }

    public int getPointCountInSingleHoldSegment()
    {
        return pointCountInSingleHoldSegment;
    }

    protected void parseAndSetZScanSize(String zScaleField)
    {        
        this.zScanSizeHardValue = extractHardValueNew(zScaleField);   
        this.zScanSizeSoftReference = extractSoftScaleReference(zScaleField);         
    }

    protected void parseAndSetRampSize(String rampSizeField)
    {        
        this.rampSizeHardValue = extractHardValue(rampSizeField);   
        this.rampSizeSoftReference = extractSoftScaleReference(rampSizeField);         
    }

    protected void parseAndSetSpringConstant(String springConstantField)
    {
        this.springConstant = parseDoubleValue(springConstantField);         
    }

    //In Samps/Line field, withdraw length is specified first. I established it based on the behaviour of the Nanoscope Matlab Toolkit. This is surprising.
    protected void parseAndSetSamplesPerLine(String samplesPerLineField)
    {        
        if(samplesPerLineField == null)
        {
            return;
        }       

        Matcher scanSizeMatcher = NanoscopeData.numberPattern.matcher(samplesPerLineField);
        boolean xFound = scanSizeMatcher.find();
        if(xFound)
        {
            this.withdrawLengthReadIn = Integer.parseInt(scanSizeMatcher.group(1));
        }

        boolean yFound = scanSizeMatcher.find();

        this.approachLengthReadIn = yFound ? Integer.parseInt(scanSizeMatcher.group(1)):  withdrawLengthReadIn;     
    }

    public void readInScannerList(NanoscopeScannerList scannerList)
    {
        readInReferenceFields(scannerList.getReferenceFields());
    }

    public void readInScanList(NanoscopeScanList scanList)
    {
        readInReferenceFields(scanList.getReferenceFields());

        this.peakForceAmplitude = scanList.getPeakForceAmplitude();
        this.syncDistanceForAnalysis = scanList.getSyncDistanceForAnalysis(); 
        this.peakForceCaptureAllowed = scanList.isPeakForceCaptureAllowed();
        this.samplePoints = scanList.getSamplePoints();
    }

    public void readInForceList(NanoscopeForceList forceList)
    {
        this.pointCountInSingleHoldSegment = forceList.getPointCountInSingleHoldSegment();
    }

    @Override
    public void readInReferenceFields(List<String> referenceFields)
    {
        super.readInReferenceFields(referenceFields);

        String rampSizeField = findReferenceField(rampSizeSoftReference, referenceFields);

        if(rampSizeField != null)
        {
            this.softRampSizeScale = extractSoftScale(rampSizeField);
        }

        String zScanSizeField = findReferenceField(zScanSizeSoftReference, referenceFields);

        if(zScanSizeField != null)
        {
            this.softzScanSizeScale = extractSoftScale(zScanSizeField);
        }
    }

    //returns the ramp size in microns !
    protected double getFullRampSize()
    {
        double size = !isLinearRampSpecified() ? getZScaleSize() : softRampSizeScale.multiply(rampSizeHardValue).derive(Units.MICRO_METER_UNIT).getValue();            
        return size;
    }

    private double getZScaleSize()
    {       
        double size = softzScanSizeScale.multiply(zScanSizeHardValue).derive(Units.MICRO_METER_UNIT).getValue();            
        return size;
    }

    protected double getDeflectionSensitivity()
    {
        return sensitivity;
    }

    @Override
    protected void setSoftZScale(UnitExpression softZScale)
    {
        super.setSoftZScale(softZScale);
        this.sensitivity = getSoftZScale().derive(Units.MICRO_METER_PER_VOLT_UNIT).getValue();
    }

    protected int calculateCurveCount()
    {
        int trueApproachLength = isSinusoidalRamp() ? withdrawLengthReadIn : approachLengthReadIn;// Bruker's Matlab toolbox ignores the length of approach and assumes that withdraw curve and approach curve are of the same length. In Samps/Line field, withdraw length is specified first 
        //in addition, I saw PFC files that have wrong approach length specified (provided by Denis)
        int trueWithdrawLength = isSinusoidalRamp() ? withdrawLengthReadIn : withdrawLengthReadIn;

        if(trueApproachLength + trueWithdrawLength == 0)
        {
            return 0;
        }

        int dataLength = getSpecifiedDataLength();
        int bytesPerPixel = getBytesPerPixel();

        int singleCurveLengthInBytes = (bytesPerPixel + getNumberOfBytesToSkipPerPoint())*(trueApproachLength + trueWithdrawLength + pointCountInSingleHoldSegment);

        int curveCount = dataLength/singleCurveLengthInBytes;

        return curveCount;
    }

    public boolean isForceMap()
    {
        int curveCount = calculateCurveCount();
        boolean forceMap = curveCount > 1;
        return forceMap;
    }

    public int getReadInApproachLength()
    {
        return approachLengthReadIn;
    }

    public int getReadInWithdrawLength()
    {
        return withdrawLengthReadIn;
    }

    public NanoscopeSpectroscopyDataType getDataType()
    {
        return dataType;
    }

    public Grid1D getApproachGrid()
    {
        Quantity xQuantity = Quantities.DISTANCE_MICRONS;

        double fullRampSize = getFullRampSize();
        double step = fullRampSize/(withdrawLengthReadIn);//Nanoscope Matlab toolkit always assumes that withdraw, not approach, occupies full ramp! this distinction is important
        //when one branch of the force curve is truncated
        //if the approach branch has more points than the withdraw branch, then the ramp of the approach branch is longer than the full ramp size!
        //this is correct behavior according to Matlab Nanoscope toolkit, so I implemented it here

        //in addition, the interval between neighbouring points in curves read-in with Nanoscope Matlab toolkit is equal to fullRampSize/(withdrawLength)
        //no to fullRampSize/(withdrawLength - 1). This is surprising, because this results in  the ramp of the read in curves being shorter than the ramp specified in the file
        //I think that is a bug in their software, but I copied this behaviour for consistency

        double rampSize = (approachLengthReadIn - 1) *step; //we need to use here (approachLength - 1), and not (approachLength) for consistency with the Nanoscope Matlab toolkit

        Grid1D approachGrid = new Grid1D(-step, rampSize, approachLengthReadIn, xQuantity);

        return approachGrid;
    }

    public Grid1D getWithdrawGrid()
    {
        Quantity xQuantity = Quantities.DISTANCE_MICRONS;
        double rampSize = getFullRampSize();
        //in addition, the interval between neighbouring points in curves read-in with Nanoscope Matlab toolkit is equal to fullRampSize/(withdrawLength)
        //no to fullRampSize/(withdrawLength - 1). This is surprising, because this results in  the ramp of the read in curves being shorter than the ramp specified in the file
        //I think that is a bug in their software, but I copied this behaviour for consistency
        double withdrawStep = rampSize/(withdrawLengthReadIn);
        Grid1D withdrawGrid = new Grid1D(withdrawStep, 0, withdrawLengthReadIn, xQuantity);

        return withdrawGrid;
    }

    @Override
    public void readField(String fieldRaw)
    {
        super.readField(fieldRaw);

        String field = dropPrefices(fieldRaw);
        String fieldLowerCase = field.toLowerCase();      

        if(fieldLowerCase.startsWith(DATA_TYPE))
        {
            String externalDesignation = extractExternalDesignationForSelection(field);
            this.dataType = NanoscopeSpectroscopyDataType.getDataType(externalDesignation);            
        }
        else if(fieldLowerCase.startsWith(RAMP_SIZE))
        {
            parseAndSetRampSize(field);
        }
        else if(fieldLowerCase.startsWith(Z_SCAN_SIZE))
        {
            parseAndSetZScanSize(field);
        }
        else if(fieldLowerCase.startsWith(SPRING_CONSTANT))
        {
            parseAndSetSpringConstant(field);
        }
        else if(fieldLowerCase.startsWith(SAMPLES_PER_LINE))
        {
            parseAndSetSamplesPerLine(field);
        }
    }

    //the \Data length filed in the \*Ciao force image list section is calculated based on the assumption that the length of both branches of the curve is the same and equal to the first number specified in the \Samps line field
    //i.e. equal to the length of the withdraw branch. This means that if approach is longer than withdraw, we may have to read more bytes than specified in the \Data length field
    //    protected int getNumberOfBytesToRead()
    //    {
    //        if(isSinusoidalRamp())
    //        {
    //            return getSpecifiedDataLength();
    //        }
    //        
    //        int bytesPerPixel = getBytesPerPixel();
    //        int toSkip = getNumberOfBytesToSkipPerPoint();
    //        int approachLength = getApproachLength();
    //        int withdrawLength = getWithdrawLength();
    //        
    //        int specifiedDataLength = getSpecifiedDataLength();
    //        int dataTrueLength = approachLength > withdrawLength ? specifiedDataLength + (bytesPerPixel + toSkip)*(approachLength - withdrawLength): specifiedDataLength;
    //        
    //        return dataTrueLength;
    //    }

    protected ByteBuffer readInBytes(File f) throws UserCommunicableException
    {        
        try (FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath()))
        {
            channel.position(getDataOffset());
            return FileInputUtilities.readBytesToBuffer(channel, getSpecifiedDataLength(), ByteOrder.LITTLE_ENDIAN);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading a file", e);
        }
    }

    protected int[] getRawData(ByteBuffer buffer) throws UserCommunicableException
    {        
        int dataLength = getSpecifiedDataLength();
        int bytesPerPixel = getBytesPerPixel();
        int toSkip = getNumberOfBytesToSkipPerPoint();
        int n = dataLength/(bytesPerPixel + toSkip);

        IntArrayReaderType intReader = getIntReader();
        int[] data = toSkip > 0 ? intReader.readIn1DArray(n, toSkip, buffer) : intReader.readIn1DArray(n, buffer);      

        return data;
    }

    public double[][] readInCurveChannels(ByteBuffer byteBuffer, double factor) throws UserCommunicableException
    {                      
        int errorValue = (int)getErrorValue();
        int toSkip = getNumberOfBytesToSkipPerPoint();

        IntArrayReaderType intReader = getIntReader();

        //      double[] approach = bytesToSkip > 0 ? reader.readIn1DArrayReversed(approachLength, bytesToSkip, factor, 0, byteBuffer) : reader.readIn1DArrayReversed(approachLength, factor, 0, byteBuffer);
        //            double[] withdraw = bytesToSkip > 0 ? reader.readIn1DArray(withdrawLength,bytesToSkip, factor, 0, byteBuffer) : reader.readIn1DArray(withdrawLength, factor, 0, byteBuffer);

        int[] approachLsbs = toSkip > 0 ? intReader.readIn1DArray(approachLengthReadIn, toSkip, byteBuffer) : intReader.readIn1DArray(approachLengthReadIn,  byteBuffer);
        if(approachLengthReadIn < withdrawLengthReadIn)
        {
            FileInputUtilities.skipBytes((withdrawLengthReadIn - approachLengthReadIn)*(getBytesPerPixel() + toSkip), byteBuffer);
        }

        int[] withdrawLsbs = toSkip > 0 ? intReader.readIn1DArray(withdrawLengthReadIn, toSkip, byteBuffer) : intReader.readIn1DArray(withdrawLengthReadIn,  byteBuffer);

        double[] approachData = new double[approachLengthReadIn];      
        double[] withdrawData = new double[withdrawLengthReadIn];

        approachData[approachLengthReadIn - 1] = approachLsbs[0]*factor;//we need to reverse order in the loop and not to reverse in int reader, because apporachData[0] is often errorValue or 0
        for(int i = 1; i < approachLengthReadIn; i++)
        {
            int readIn = approachLsbs[i];
            approachData[approachLengthReadIn - i - 1] = (readIn != errorValue) ? readIn*factor : approachData[approachLengthReadIn - i - 1 + 1]; 
        }

        withdrawData[0] = withdrawLsbs[0]*factor;
        for(int i = 1; i < withdrawLengthReadIn; i++)
        {
            int readIn = withdrawLsbs[i];
            withdrawData[i] = (readIn != errorValue) ? readIn*factor : withdrawData[i - 1]; 
        }          

        if(pointCountInSingleHoldSegment > 0)
        {
            FileInputUtilities.skipBytes(pointCountInSingleHoldSegment*(getBytesPerPixel() + toSkip), byteBuffer);
        }

        double[][] channelData = new double[][] {approachData, withdrawData};

        return channelData;
    }

    public int[] getReversedLSBValues(File f) throws UserCommunicableException
    {
        int[] lsbsRaw = getRawData(readInBytes(f));
        int[] lsb = removeErrorValuesAndReverseAll(lsbsRaw);

        return lsb;
    }

    private int[] removeErrorValuesAndReverseAll(int[] data)
    {   
        int errorValue = (int)getErrorValue();
        int n = data.length;
        int[] reversed = new int[n];

        reversed[n-1] = data[0];//this cannot be in the loop, because the loop contains reversed[n - 1 - i + 1]
        for(int i = 1; i < n;i++)
        {
            int val = data[i];
            reversed[n - 1 - i] = (val != errorValue) ? val : reversed[n - 1 - i + 1];            
        } 

        return reversed;
    }
}
