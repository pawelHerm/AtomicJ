
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2015 by Paweł Hermanowicz
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

package atomicJ.readers.anasys;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import atomicJ.analysis.PhotodiodeSignalType;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.readers.SourceReadingState;
import atomicJ.readers.SourceReadingStateMonitored;
import atomicJ.readers.SourceReadingStateMute;
import atomicJ.sources.CalibrationState;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.IOUtilities;

public class AnasysSpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{    
    private static final String GROUPS_TAG = "Groups";
    private static final String AXD_SWEEP_WAVEFORM_TAG = "AXDSweepWaveform";
    private static final String SWEEP_TAG = "Sweep";
    private static final String SWEEP_CHANNEL_TAG = "SweepChannel";
    private static final String UNITS_TAG = "Units";
    private static final String START_TAG = "Start";
    private static final String END_TAG = "End";
    private static final String INCREMENT_TAG = "Increment";
    private static final String SAMPLE_BASE_64_TAG = "SampleBase64";

    private static final String DATA_CHANNEL_ATTRIBUTE = "DataChannel";
    private static final String DEFLECTION_ATTRIBUTE_VALUE = "deflection";

    private static final String AXZ_EXTENSION = "axz";

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"axd","axz"};
    private static final String DESCRIPTION = "Anasys force curve file (.axd, .axz)";
    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    @Override
    public boolean accept(File f) 
    {
        String[] acceptedExtensions = getAcceptedExtensions();

        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(acceptedExtensions);
        return filter.accept(f);       
    }

    @Override
    public List<SimpleSpectroscopySource> readSources(File f, SourceReadingDirectives readingDirectives) throws UserCommunicableException, IllegalImageException 
    {      
        String extension = IOUtilities.getExtension(f);
        boolean isAXZ = AXZ_EXTENSION.equals(extension);

        if(isAXZ)
        {

            try(GzipCompressorInputStream inputStream = new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(f)));)
            {  
                List<SimpleSpectroscopySource> readInSources = readSourceFromInputStream(f, inputStream, readingDirectives);                
                return readInSources;

            } catch (IOException | ParserConfigurationException | SAXException e) 
            {
                e.printStackTrace();
                throw new UserCommunicableException("Error occured while reading the file", e);     
            }
        }
        else
        {
            try(BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(f));) 
            {            
                List<SimpleSpectroscopySource> readInSources = readSourceFromInputStream(f, inputStream, readingDirectives);
                return readInSources;
            } 

            catch (IOException | RuntimeException | ParserConfigurationException | SAXException e) 
            {
                e.printStackTrace();

                throw new UserCommunicableException("Error occured while reading the file", e);     
            }  
        }              
    }
    private List<SimpleSpectroscopySource> readSourceFromInputStream(File f, InputStream inputStream, SourceReadingDirectives readingDirectives) throws ParserConfigurationException, SAXException, IOException, UserCommunicableException
    {
        List<SimpleSpectroscopySource> spectroscopySources = new ArrayList<>();

        Document xmlDocument = FileInputUtilities.readInXMLDocument(inputStream);
        //if we were to use XPath, we would need to take into account the namespace
        //XPath xpath = XPathFactory.newInstance().newXPath();

        //XPathExpression heightMapsPath = xpath.compile("//HeightMaps");           
        //Node heightMapsNodes2 = (Node)heightMapsPath.evaluate(xmlDocument, XPathConstants.NODE);//there can be multiple spectroscopy data in a single file

        List<Element> groupsNodes = FileInputUtilities.getImmediateChildElementsdByTagName(xmlDocument.getDocumentElement(), GROUPS_TAG);

        //in meters
        double startA = Double.NaN;
        //in meters
        double endA = Double.NaN;

        //in um
        double startB = Double.NaN;
        //in um
        double incrementB = Double.NaN;

        int curveCount = groupsNodes.size();
        boolean multipleCurves = curveCount > 1;

        SourceReadingState state  = curveCount > 10  ? new SourceReadingStateMonitored(curveCount, SourceReadingStateMonitored.FORCE_VOLUME_PROBLEM) :
            new SourceReadingStateMute(curveCount);

        for(int groupsIndex = 0; groupsIndex < groupsNodes.size();groupsIndex++)
        {
            Element groupsNode = groupsNodes.get(groupsIndex);
            NodeList sweepWaveformNodes = groupsNode.getElementsByTagName(AXD_SWEEP_WAVEFORM_TAG);//they are not immediate children of the Groups node

            //we don't handle the case when there are more than two branches in a single force distance curve
            Channel1DData approachReadIn = null;
            Channel1DData withdrawReadIn = null;

            for(int sweepWaveformIndex = 0; sweepWaveformIndex < sweepWaveformNodes.getLength(); sweepWaveformIndex++)
            {  
                if(readingDirectives.isCanceled())
                {
                    state.setOutOfJob();
                }
                if(state.isOutOfJob())
                {
                    return Collections.emptyList();
                }

                //this cast is safe
                Element sweepWavefromElement = (Element)sweepWaveformNodes.item(sweepWaveformIndex);
                String dataChannel = sweepWavefromElement.getAttribute(DATA_CHANNEL_ATTRIBUTE);

                Element sweepElement = FileInputUtilities.getFirstImmediateChildByTagName(sweepWavefromElement, SWEEP_TAG);

                Element unitYAxisNameElement = FileInputUtilities.getFirstImmediateChildByTagName(sweepElement, UNITS_TAG);
                String unitYAxisNameString = (unitYAxisNameElement != null) ? unitYAxisNameElement.getTextContent().trim(): "";
                PrefixedUnit unitYAxis = unitYAxisNameString.length() > 0 ? UnitUtilities.getSIUnit(unitYAxisNameString): SimplePrefixedUnit.getNullInstance();
                //
                //                Element startElement = FileInputUtilities.getFirstElementByTagName(sweepElement, START_TAG); 
                //                Element endElement = FileInputUtilities.getFirstElementByTagName(sweepElement, END_TAG);
                //                startA = (startElement != null) ? Double.parseDouble(startElement.getTextContent().trim()): Double.NaN;
                //                endA = (endElement != null) ? Double.parseDouble(endElement.getTextContent().trim()) : Double.NaN;   

                Quantity yQuantity = CalibrationState.getDefaultYQuantity(unitYAxis);
                double yFactor = unitYAxis.getConversionFactorTo(yQuantity.getUnit());

                Element incrementElement = FileInputUtilities.getFirstImmediateChildByTagName(sweepWavefromElement, INCREMENT_TAG);
                incrementB = (incrementElement != null) ? 0.001*Double.parseDouble(incrementElement.getTextContent().trim()) : Double.NaN;

                Element startBElement = FileInputUtilities.getFirstImmediateChildByTagName(sweepWavefromElement, START_TAG);
                startB = (startBElement != null) ? 0.001*Double.parseDouble(startBElement.getTextContent().trim()) : Double.NaN;           

                double[] data = readInData(sweepWavefromElement, yFactor);
                int pointCount = data.length;

                boolean channelSpecified = !Double.isNaN(incrementB) && !Double.isNaN(startB) && yQuantity != null & pointCount > 0;

                if(channelSpecified)
                {
                    Grid1D grid = new Grid1D(incrementB, startB, pointCount, Quantities.DISTANCE_MICRONS); 

                    if(incrementB < 0)
                    {
                        approachReadIn = new GridChannel1DData(data, grid, yQuantity);
                    }
                    else if(incrementB > 0)
                    {
                        withdrawReadIn = new GridChannel1DData(data, grid, yQuantity);
                    }
                }                  
            }

            boolean atLeastOneBranchSpecified = approachReadIn != null || withdrawReadIn != null;
            if(atLeastOneBranchSpecified)
            {
                String suffix = multipleCurves ? " (" + Integer.toString(groupsIndex) + ")" : "";
                String longName = f.getAbsolutePath() + suffix;
                String shortName = IOUtilities.getBareName(f) + suffix;

                Channel1DData approach = (approachReadIn != null) ? approachReadIn : FlexibleChannel1DData.getEmptyInstance(withdrawReadIn);//if approach is null, then withdraw cannot be null, because atLeastOneBranchSpecified is true 
                Channel1DData withdraw = (withdrawReadIn != null) ? withdrawReadIn : FlexibleChannel1DData.getEmptyInstance(approachReadIn);

                PhotodiodeSignalType photodiodeSignalTypeApproach = PhotodiodeSignalType.getSignalType(approach.getYQuantity(), PhotodiodeSignalType.VOLTAGE);
                PhotodiodeSignalType photodiodeSignalTypeWithdraw = PhotodiodeSignalType.getSignalType(withdraw.getYQuantity(), PhotodiodeSignalType.VOLTAGE);

                if(!photodiodeSignalTypeApproach.equals(photodiodeSignalTypeWithdraw))
                {
                    throw new UserCommunicableException("Photodiode signal are different for branches of the same force - distance curve");
                }

                StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName, approach, withdraw);
                source.setPhotodiodeSignalType(photodiodeSignalTypeApproach);

                spectroscopySources.add(source);
            }

            state.incrementAbsoluteProgress();

        }

        return spectroscopySources; 
    }

    private static double[] readInData(Element sweepWavefromElement, double yFactor)
    {
        Element samples64BaseElement = FileInputUtilities.getFirstElementByTagName(sweepWavefromElement, SAMPLE_BASE_64_TAG);
        String base64Samples = (samples64BaseElement != null) ? samples64BaseElement.getTextContent() : "";                

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] sampleBytes = decoder.decode(base64Samples);
        ByteBuffer sampleByteBuffer = ByteBuffer.wrap(sampleBytes);
        sampleByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int bytesPerSample = 4;
        int pointCount = (int)Math.floor(sampleBytes.length/bytesPerSample);
        DoubleArrayReaderType dataReader = DoubleArrayReaderType.getReaderForFloatingPointInput(bytesPerSample);
        double[] data = dataReader.readIn1DArray(pointCount, yFactor, sampleByteBuffer);

        return data;
    }
}

