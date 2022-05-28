
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

package atomicJ.readers.nanoscope;

import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.Quantities;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.FileReadingPack;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.ReaderFileFilter;
import atomicJ.readers.ReadingPack;
import atomicJ.sources.FlexibleMapSource;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.IOUtilities;


/*
 * The structure of  MCA file :
 * 
 * - the list of force curve files is preceded by the FrcFileInfo key word
 * - the force curve entry starts with sixteen bytes "entry header" 01 00 49 44 14 00 00 00 85 00 00 00 ff ff ff ff(hexadecimal) i.e. ..ID.
 * - the 85 byte in the sequence above is crucial - the file will not be opened by the Nanoscope Analysis
 *    if the 85 byte is changed, neither 00, nor any positive byte
 *    - the ff ff ff ff sequence can be modified and nothing changes in Nanoscope
 * - after the "entry header" there is a path to the file with the force curve
 * - after the path, there is the "entry tail ", starting with 8 null bytes, 4 bytes and  the "position bytes". Thus
 * , its structure is 00 00 00 00 00 00 00 00 01 00 00 00 xx xx xx xx yy yy yy yy 01 80
 * 
 * - if the MCA file does not contain the information about force curve files and the position of the curves within
 * the scan area, Nanoscope Analysis calculates the position of a curve as follows:
 * 
 *      [x, y] = [imageXOffset, imageYOffset] + [0.5*imageWidth, 0.5*imageHeight] - [curveXOffset, curveYOffset]
 *      
 *      (The above equation is in the form suitable for AtomicJ.
 *      Please note that in the case of Nanoscope Analysis, the origin of plot axes is in the upper left corner
 *      of the image, while in the case of AtomicJ it is in the bottom left.)
 * 
 * - image name is introduced by the byte sequence 80 3f cd cc 4c 3d 13 i.e. by €?ÍĚL=.

 */

public class NanoscopeMCAReader extends AbstractSourceReader<SimpleSpectroscopySource>
{
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"mca"};
    private static final String DESCRIPTION = "Nanoscope multi-curve file (.mca)";

    private static final String ID = "ID\u0014";
    private static final Pattern IMAGE_NAME_SEQUENCE = Pattern.compile("\u0080\u003f\u00cd\u00cc\u004c\u003d.([^\u0000]++)");
    private static final String FRC_FILE_INFO = "FrcFileInfo";

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
        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(ACCEPTED_EXTENSIONS);
        return filter.accept(f);       
    }

    @Override
    public List<SimpleSpectroscopySource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {	 
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        String firstMatch = null;

        try(Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(f), "ISO-8859-1")));) 
        {
            String found = scanner.findWithinHorizon(IMAGE_NAME_SEQUENCE, 0);

            Matcher matcher = IMAGE_NAME_SEQUENCE.matcher(found);
            firstMatch = matcher.matches() ? matcher.group(1) : null;
        } 

        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);		
        } 

        File directory = f.getParentFile();
        File imageFile = new File(f.getParent(), firstMatch);

        NanoscopeScanList imageScanList = new NanoscopeScanListReader().read(imageFile);

        UnitExpression xOffset = imageScanList.getXOffset();
        UnitExpression yOffset = imageScanList.getYOffset();
        UnitExpression scanHalfSize = imageScanList.getScanSize().multiply(0.5);

        NanoscopeMCACurveReader curveReader = new NanoscopeMCACurveReader(xOffset.add(scanHalfSize), yOffset.add(scanHalfSize));

        List<File> potentialCurveFiles = IOUtilities.findAcceptableChildrenFiles(new File[] {directory}, new ReaderFileFilter(curveReader));

        for(File pf : potentialCurveFiles)
        {
            if(readingDirective.isCanceled())
            {
                return Collections.emptyList();
            }

            List<SimpleSpectroscopySource> readIn = curveReader.readSources(pf, readingDirective);
            if(!readIn.isEmpty())
            {
                sources.add(readIn.get(0));
            }
        }

        List<Point2D> recordingPoints = new ArrayList<>();

        for(SimpleSpectroscopySource s : sources)
        {
            recordingPoints.add(s.getRecordingPoint());
        }

        ReadingPack<ImageSource> imageReadingPack = new FileReadingPack<>(imageFile, new NanoscopeImageReader());

        double probingDensity = FlexibleChannel2DData.calculateProbingDensityGeometryPoints(recordingPoints);
        ChannelDomainIdentifier dataDomain = new ChannelDomainIdentifier(probingDensity, ChannelDomainIdentifier.getNewDomainKey());

        MapSource<?> mapSource = new FlexibleMapSource(imageFile, sources, dataDomain, 
                Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);

        mapSource.setMapAreaImageReadingPack(imageReadingPack);

        return sources; 

    }
}

