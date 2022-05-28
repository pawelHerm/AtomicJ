
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import atomicJ.data.ChannelFilter;
import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.IOUtilities;


public class AnasysImageReader extends AbstractSourceReader<ImageSource>
{  
    private static final String HEIGHT_MAPS_TAG = "HeightMaps";
    private static final String HEIGHT_MAP_TAG = "HeightMap";
    private static final String RESOLUTION_TAG = "Resolution";
    private static final String SIZE_TAG = "Size";
    private static final String UNITS_TAG = "Units";
    private static final String UNIT_PREFIX_TAG = "UnitPrefix";
    private static final String X_TAG = "X";
    private static final String Y_TAG = "Y";
    private static final String POSITION_TAG = "Position";
    private static final String TAGS_TAG = "Tags";
    private static final String TAG_TAG = "Tag";

    private static final String LABEL_ATTRIBUTE = "Label";
    private static final String NAME_ATTRIBUTE = "NameAttribute";
    private static final String VALUE_ATTRIBUTE = "Value";

    private static final String TRACE_RETRACE = "TraceRetrace";
    private static final String RETRACE = "retrace";//must be in lower case

    private static final String SAMPLE_BASE_64_TAG = "SampleBase64";

    private static final String AXZ_EXTENSION = "axz";
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"axd","axz"};
    private static final String DESCRIPTION = "Anasys force curve file (.axd, .axz)";

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    @Override
    public boolean accept(File f) 
    {
        String[] acceptedExtensions = getAcceptedExtensions();

        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(acceptedExtensions);
        return filter.accept(f);       
    }

    @Override
    public List<ImageSource> readSources(File f, SourceReadingDirectives readingDirectives) throws UserCommunicableException, IllegalSpectroscopySourceException
    {      

        String extension = IOUtilities.getExtension(f);
        boolean isAXZ = AXZ_EXTENSION.equals(extension);

        if(isAXZ)
        {            
            try(GzipCompressorInputStream inputStream = new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(f)));)
            {                  
                List<ImageSource> readInSources = readSourceFromInputStream(f, inputStream, readingDirectives);                
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
                List<ImageSource> readInSources = readSourceFromInputStream(f, inputStream, readingDirectives);
                return readInSources;
            } 

            catch (IOException | RuntimeException | ParserConfigurationException | SAXException e) 
            {
                e.printStackTrace();

                throw new UserCommunicableException("Error occured while reading the file", e);     
            }  
        }              
    }

    private List<ImageSource> readSourceFromInputStream(File f, InputStream inputStream, SourceReadingDirectives readingDirectives) throws ParserConfigurationException, SAXException, IOException
    {      
        List<ImageSource> sources = new ArrayList<>();

        Document xmlDocument = FileInputUtilities.readInXMLDocument(inputStream);
        //if we were to use XPath, we would need to take into account the namespace
        //XPath xpath = XPathFactory.newInstance().newXPath();

        //XPathExpression heightMapsPath = xpath.compile("//HeightMaps");           
        //Node heightMapsNodes2 = (Node)heightMapsPath.evaluate(xmlDocument, XPathConstants.NODE);//there can be multiple spectroscopy data in a single file

        NodeList heightMapsNodes = xmlDocument.getElementsByTagName(HEIGHT_MAPS_TAG);

        int rowCount = -1;
        int columnCount = -1;
        double xScanSize = Double.NaN;
        double yScanSize = Double.NaN;

        List<ImageChannel> imageChannels = new ArrayList<>();
        List<String> channelIdentifiers = new ArrayList<>();

        for(int heightMapsIndex = 0; heightMapsIndex < heightMapsNodes.getLength();heightMapsIndex++)
        {
            Node heightMapsNode = heightMapsNodes.item(heightMapsIndex);
            List<Element> heightMapNodes = FileInputUtilities.getChildElementsWithTag(heightMapsNode, HEIGHT_MAP_TAG);

            for(Element heightMapNode : heightMapNodes)
            {                  
                Element resolutionElement = FileInputUtilities.getFirstElementByTagName(heightMapNode, RESOLUTION_TAG);
                Element rowCountElement = FileInputUtilities.getFirstElementByTagName(resolutionElement, Y_TAG); 
                Element columnCountElement = FileInputUtilities.getFirstElementByTagName(resolutionElement, X_TAG);
                rowCount = (rowCountElement != null) ? Integer.parseInt(rowCountElement.getTextContent().trim()): -1;
                columnCount = (columnCountElement != null) ? Integer.parseInt(columnCountElement.getTextContent().trim()) : -1;   

                Element sizeElement = FileInputUtilities.getFirstElementByTagName(heightMapNode, SIZE_TAG);
                Element sizeXElement = FileInputUtilities.getFirstElementByTagName(sizeElement, X_TAG);
                Element sizeYElement = FileInputUtilities.getFirstElementByTagName(sizeElement, Y_TAG);

                //in microns
                xScanSize = (sizeXElement != null) ? Double.parseDouble(sizeXElement.getTextContent().trim()) : Double.NaN;
                //in microns
                yScanSize = (sizeXElement != null) ? Double.parseDouble(sizeYElement.getTextContent().trim()) : Double.NaN;

                Element unitZPrefixElement = FileInputUtilities.getFirstElementByTagName(heightMapNode, UNIT_PREFIX_TAG);
                String unitZPrefixString = (unitZPrefixElement != null) ? unitZPrefixElement.getTextContent().trim(): "";

                Element unitZNameElement = FileInputUtilities.getFirstElementByTagName(heightMapNode, UNITS_TAG);
                String unitZNameString = (unitZNameElement != null) ? unitZNameElement.getTextContent().trim(): "";
                String unitZString = unitZPrefixString + unitZNameString;
                PrefixedUnit unitZ = unitZString.length() > 0 ? UnitUtilities.getSIUnit(unitZString): SimplePrefixedUnit.getNullInstance();

                String channelLabel = heightMapNode.getAttribute(LABEL_ATTRIBUTE);
                Quantity readInDataQuantity = new UnitQuantity(channelLabel, unitZ);

                int nrOfIdenticalBareIds = Collections.frequency(channelIdentifiers, channelLabel);
                channelIdentifiers.add(channelLabel);

                String identifier = (nrOfIdenticalBareIds == 0) ? channelLabel : channelLabel + " (" + Integer.toString(nrOfIdenticalBareIds + 1) + ")"; 

                Map<String, String> channelMetadata = extractMetadata(heightMapNode);
                boolean isTrace = (channelMetadata.containsKey(TRACE_RETRACE) ? channelMetadata.get(TRACE_RETRACE).toLowerCase().contains(RETRACE) : false);

                Element samples64BaseElement = FileInputUtilities.getFirstElementByTagName(heightMapNode, SAMPLE_BASE_64_TAG);
                String base64Samples = (samples64BaseElement != null) ? samples64BaseElement.getTextContent() : "";

                ChannelFilter filter = readingDirectives.getDataFilter();
                if(filter.accepts(identifier, readInDataQuantity) && !base64Samples.isEmpty() && !Double.isNaN(xScanSize) && !Double.isNaN(yScanSize) && columnCount > 0 && rowCount > 0)
                {
                    //if there is only 1 column/row, it does not matter what is the value of incrementX as long as it is not NaN. It can't be NaN, because Grid2D will use calculatiosn like origin + 0*NaN.
                    double incrementX = (columnCount > 1) ? xScanSize/(columnCount - 1) : 1;
                    double incrementY = (rowCount > 1) ? yScanSize/(rowCount - 1) : 1;
                    Grid2D grid = new Grid2D(incrementX, incrementY, 0, 0, rowCount, columnCount, Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);

                    Base64.Decoder decoder = Base64.getDecoder();
                    byte[] sampleBytes = decoder.decode(base64Samples);
                    ByteBuffer sampleByteBuffer = ByteBuffer.wrap(sampleBytes);
                    sampleByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    int pointCount = rowCount*columnCount;
                    double bytesPerSampleFraction = ((double)sampleBytes.length)/pointCount;
                    int bytesPerSampleInt = (int)Math.rint(bytesPerSampleFraction);

                    DoubleArrayReaderType dataReader = DoubleArrayReaderType.getReaderForFloatingPointInput(bytesPerSampleInt);

                    double[][] channelData = dataReader.readIn2DArrayRowByRow(rowCount, columnCount, 1, sampleByteBuffer);
                    ImageChannel imageChannel = new ImageChannel(channelData, grid, readInDataQuantity, identifier, isTrace);

                    imageChannels.add(imageChannel);
                }
            }
        }

        if(!imageChannels.isEmpty())
        {
            ImageSource source = new StandardImageSource(f);
            source.setChannels(imageChannels);
            sources.add(source);
        }

        return sources; 
    }

    private static Map<String, String> extractMetadata(Element heightMapElement)
    {
        Map<String, String> channelMetadata = new LinkedHashMap<>();
        NodeList tagsNodes = heightMapElement.getElementsByTagName(TAGS_TAG);
        for(int tagsIndex = 0; tagsIndex < tagsNodes.getLength(); tagsIndex++)
        {
            Node tagsNode = tagsNodes.item(tagsIndex);
            List<Element> metadataTags = FileInputUtilities.getChildElementsWithTag(tagsNode, TAG_TAG);

            for(Element tag : metadataTags)
            {
                String dataName = tag.getAttribute(NAME_ATTRIBUTE);
                String dataValue = tag.getAttribute(VALUE_ATTRIBUTE);

                if(!dataName.isEmpty() && !dataValue.isEmpty())
                {
                    channelMetadata.put(dataName, dataValue);
                }
            }
        }

        return channelMetadata;
    }
}

