
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

package atomicJ.readers.regularImage;


import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.ByteOrder;
import java.util.*;
import ome.units.UNITS;
import ome.xml.meta.MetadataRetrieve;

import loci.formats.FormatException;
import atomicJ.data.Coordinate4D;
import atomicJ.data.units.LociQuantityType;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.rois.ROIProxy;
import atomicJ.gui.rois.ROIRectangle.ROIRectangleProxy;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ImageSource;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.FileExtensionPatternFilter;


public class ZeissImageReader extends RegularImageReader
{  
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"lsm", "mdb"};
    private static final String DESCRIPTION = "Zeiss LSM image (.lsm, .mdb)";

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
        try 
        {
            atomicJ.utilities.ZeissLSMReader zeissReader = new atomicJ.utilities.ZeissLSMReader();
            loci.formats.ome.OMEXMLMetadata metadata = new loci.formats.ome.OMEXMLMetadataImpl();
            zeissReader.setMetadataStore(metadata);

            zeissReader.setId(f.getPath());

            int channelCount = metadata.getChannelCount(0);

            ByteOrder byteOrder = zeissReader.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
            boolean interleaved = zeissReader.isInterleaved();
            boolean index = zeissReader.isIndexed();

            int columnCount = metadata.getPixelsSizeX(0).getValue().intValue();
            int rowCount = metadata.getPixelsSizeY(0).getValue().intValue();

            UnitExpression xResolution = new UnitExpression(metadata.getPixelsPhysicalSizeX(0).value(UNITS.MICROM).doubleValue(), Units.MICRO_METER_UNIT);
            UnitExpression yResolution = new UnitExpression(metadata.getPixelsPhysicalSizeY(0).value(UNITS.MICROM).doubleValue(), Units.MICRO_METER_UNIT);

            UnitExpression xLength = xResolution.multiply(columnCount);
            UnitExpression yLength = yResolution.multiply(rowCount);

            LociPixelType pixelType = LociPixelType.getInstance(zeissReader.getPixelType());

            if(index)
            {
                double[][] palette = LociPixelType.UNIT8.equals(pixelType) ? ArrayUtilities.convertUInt8ToDouble(zeissReader.get8BitLookupTable()) : ArrayUtilities.convertUInt16ToDouble(zeissReader.get16BitLookupTable());
            }

            List<DoubleArrayReaderType> sampleReaders = Collections.nCopies(channelCount, pixelType.getDoubleArrayReader());

            List<ROIProxy> roiProxies = new ArrayList<>();

            for(int i = 0; i<metadata.getROICount(); i++)
            {
                String shapeType = metadata.getShapeType(i, 0);
                LociROIType roiType = LociROIType.getInstance(shapeType);
                ROIProxy roiProxy = roiType.buildROIProxy(metadata, i, rowCount, columnCount);
                if(roiProxy!= null)
                {
                    roiProxies.add(roiProxy);
                }
            }

            List<String> channelNames = new ArrayList<>();
            for(int i = 0; i<channelCount; i++)
            {
                channelNames.add(metadata.getChannelName(0, i));
            }

            int channelsPerPlane = zeissReader.getRGBChannelCount();
            int planeCountPerImage = zeissReader.getSizeC()/channelsPerPlane;
            int separateImageCount = zeissReader.getSizeZ()*zeissReader.getSizeT();

            ChannelProvider[] channelProviders = new ChannelProvider[separateImageCount];

            for(int i = 0; i<separateImageCount; i++)
            {
                StandardDensityMetadata imageMetadata = new StandardDensityMetadata();
                imageMetadata.setReadInROIs(roiProxies);

                byte[][] planeBytes = new byte[planeCountPerImage][];

                List<Coordinate4D> planeCoordinates = new ArrayList<>();
                for(int j = 0; j<planeCountPerImage; j++)
                {
                    int planeIndex = planeCountPerImage*i + j;

                    UnitExpression tCoordinate = LociQuantityType.convertToUnitExpression(metadata.getPlaneDeltaT(0, planeIndex));                    
                    UnitExpression xCoordinate = LociQuantityType.convertToUnitExpression(metadata.getPlanePositionX(0, planeIndex));
                    UnitExpression yCoordinate = LociQuantityType.convertToUnitExpression(metadata.getPlanePositionY(0, planeIndex));
                    UnitExpression zCoordinate = LociQuantityType.convertToUnitExpression(metadata.getPlanePositionZ(0, planeIndex));

                    planeCoordinates.add(new Coordinate4D(tCoordinate, xCoordinate, yCoordinate, zCoordinate));

                    planeBytes[j] = zeissReader.openBytes(planeIndex, new byte[channelsPerPlane*rowCount*columnCount*pixelType.getDoubleArrayReader().getByteSize()],0,0, columnCount, rowCount);
                }                    

                PlaneSetMetadata planeMetadata = new PlaneSetMetadata(planeCoordinates);

                channelProviders[i] = new WrappedBytesChannelProvider(planeBytes, channelsPerPlane, byteOrder, interleaved, sampleReaders, "Channels", channelNames, planeMetadata, rowCount, columnCount, xLength, yLength, imageMetadata);
            }

            zeissReader.close(true);

            return readImages(f, readingDirectives, channelProviders);

        } catch (FormatException | IOException e) {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);     
        }
    }

    public static enum LociROIType
    {
        RECTANGLE("Rectangle")
        {
            @Override
            public ROIProxy buildROIProxy(MetadataRetrieve metadata, int roiIndex, int rowCount, int columnCount)
            {
                double pixelXSizeInMicrons = metadata.getPixelsPhysicalSizeX(0).value(UNITS.MICROM).doubleValue();
                double pixelYSizeInMicrons = metadata.getPixelsPhysicalSizeY(0).value(UNITS.MICROM).doubleValue();

                double width = pixelXSizeInMicrons*metadata.getRectangleWidth(roiIndex, 0);
                double height = pixelYSizeInMicrons*metadata.getRectangleHeight(roiIndex, 0);

                double originX = pixelXSizeInMicrons*(metadata.getRectangleX(roiIndex, 0));
                double originY = pixelYSizeInMicrons*(rowCount - metadata.getRectangleY(roiIndex, 0)) - height;

                Rectangle2D roiShape = new Rectangle2D.Double(originX, originY, width, height);
                String label = Integer.toString(roiIndex);

                AffineTransform transform = convertOMETransformToStandardAffineTransform(metadata.getRectangleTransform(roiIndex, 0));

                ROIProxy proxy = new ROIRectangleProxy(roiShape, transform, label, true);

                return proxy;
            }
        },

        ELLIPSE("Ellipse")
        {
            @Override
            public ROIProxy buildROIProxy(MetadataRetrieve metadata, int roiIndex, int rowCount, int columnCount)
            {
                double pixelXSizeInMicrons = metadata.getPixelsPhysicalSizeX(0).value(UNITS.MICROM).doubleValue();
                double pixelYSizeInMicrons = metadata.getPixelsPhysicalSizeY(0).value(UNITS.MICROM).doubleValue();

                double originX = pixelXSizeInMicrons*metadata.getEllipseX(roiIndex, 0);
                double originY = pixelYSizeInMicrons*metadata.getEllipseY(roiIndex, 0);
                double width = pixelXSizeInMicrons*metadata.getEllipseRadiusX(roiIndex, 0);
                double height = pixelYSizeInMicrons*metadata.getEllipseRadiusY(roiIndex, 0);

                Ellipse2D roiShape = new Ellipse2D.Double(originX, originY, width, height);
                String label = metadata.getROIID(roiIndex);

                AffineTransform transform = convertOMETransformToStandardAffineTransform(metadata.getEllipseTransform(roiIndex, 0));

                ROIProxy proxy = new atomicJ.gui.rois.ROIEllipse.ROIEllipseProxy(roiShape,transform, label, true);
                return proxy;
            }
        },
        POLYGON("Polygon")
        {
            @Override
            public ROIProxy buildROIProxy(MetadataRetrieve metadata, int roiIndex, int rowCount, int columnCount)
            {
                String polygonPoints = metadata.getPolygonPoints(roiIndex, 0);

                String[] coordinateString = polygonPoints.split("\\s+");
                int pointCount = coordinateString.length;

                Path2D path = new Path2D.Double();                                

                double width = columnCount*metadata.getPixelsPhysicalSizeX(0).value(UNITS.MICROM).doubleValue();
                double height = rowCount*metadata.getPixelsPhysicalSizeY(0).value(UNITS.MICROM).doubleValue();

                double factorX = 1e6;;
                double factorY = 1e6;

                if(pointCount == 0)
                {
                    return null;
                }          

                String[] firstPoint = coordinateString[0].split(",");

                AffineTransform transform = convertOMETransformToStandardAffineTransform(metadata.getPolygonTransform(roiIndex, 0));

                double x0 = factorX*Double.parseDouble(firstPoint[0]) + width/2;
                double y0 = -factorY*Double.parseDouble(firstPoint[1]) + height/2;

                path.moveTo(x0, y0);


                for(int i = 1; i<pointCount;i++)
                {
                    String[] point = coordinateString[i].split(",");

                    double x = factorX*Double.parseDouble(point[0]) + width/2;
                    double y = -factorY*Double.parseDouble(point[1]) + height/2;

                    path.lineTo(x, y);
                }

                path.closePath();

                ROIProxy proxy = new atomicJ.gui.rois.ROIPolygon.ROIPathProxy(path, null, true);
                return proxy;
            }
        },
        UNKNOWN("") {
            @Override
            public ROIProxy buildROIProxy(MetadataRetrieve metadata, int roiIndex, int rowCount, int columnCount) {
                return null;
            }
        };

        private final String shapeName;

        LociROIType(String shapeName)
        {
            this.shapeName = shapeName;
        }

        public String getShapeName()
        {
            return shapeName;
        }

        public abstract ROIProxy buildROIProxy(MetadataRetrieve metadata, int roiIndex, int rowCount, int columnCount);

        public static AffineTransform convertOMETransformToStandardAffineTransform(ome.xml.model.AffineTransform omeTransform)
        {
            AffineTransform standardTransform = omeTransform != null ? new AffineTransform(omeTransform.getA00(), omeTransform.getA10(), omeTransform.getA01(), omeTransform.getA11(), omeTransform.getA02(), omeTransform.getA12()) : new AffineTransform();

            return standardTransform;
        }

        public static LociROIType getInstance(String shapeType)
        {            
            for(LociROIType lociROIType : LociROIType.values())
            {
                if(Objects.equals(lociROIType.shapeName, shapeType))
                {
                    return lociROIType;
                }
            }

            return UNKNOWN;
        }
    }
}

