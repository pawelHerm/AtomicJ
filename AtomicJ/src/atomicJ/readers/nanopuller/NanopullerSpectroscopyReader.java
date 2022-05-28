/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2018 by Pawe³ Hermanowicz
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

package atomicJ.readers.nanopuller;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.Units;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.UserDialogDecisionState;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReader;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.readers.nanopuller.NanopullerFileStructureModel.NanopullerFileStructure;
import atomicJ.readers.nanopuller.NanopullerFileStructureModel.QuantityIncreaseDirection;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.IOUtilities;


public class NanopullerSpectroscopyReader implements SourceReader<SimpleSpectroscopySource>
{
    private static final String DELIMITER = "((?:\\p{javaWhitespace}*)[\\n\\t]+(?:\\p{javaWhitespace}*))";

    private static final String ACCEPTED_EXTENSION = "txt";
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {ACCEPTED_EXTENSION};
    private static final String DESCRIPTION = "Nanopuller file (.txt)";

    private static final String SCAN_START = "scanstart";

    private NanopullerFileStructure lastSpecifiedFileStructure = null;
    private NanopullerFileStructureData fileStructureData = new NanopullerDefaultStructureDirectives(new NanopullerFileStructure(QuantityIncreaseDirection.TOWARDS_SAMPLE, QuantityIncreaseDirection.TOWARDS_SAMPLE));

    private static boolean isFileBeginnigConsistentWithNanopullerFile(File f)
    {
        try (FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath()))
        {                   
            try(Scanner scanner = new Scanner(channel);) 
            {
                scanner.useLocale(Locale.US);
                while(scanner.hasNext())
                {
                    String line = scanner.nextLine().trim();

                    if(line.startsWith(SCAN_START))
                    {
                        return true;
                    }                                     
                    else if(!line.isEmpty())
                    {
                        return false;
                    }
                }
            }      
        }
        catch (Exception e) {return false;}

        return false;
    }

    @Override
    public boolean prepareSourceReader(List<File> files) throws UserCommunicableException
    {
        boolean canceled = false;

        Map<File, NanopullerFileStructure> fileStructures = new HashMap<>();
        NanopullerFileStructure defaultStructure = new NanopullerFileStructure(QuantityIncreaseDirection.TOWARDS_SAMPLE, QuantityIncreaseDirection.TOWARDS_SAMPLE);

        for(File f : files)
        {
            String ext1 = FilenameUtils.getExtension(f.getName());
            if(ACCEPTED_EXTENSION.equals(ext1) && isFileBeginnigConsistentWithNanopullerFile(f))
            {
                NanopullerFileStructureModel model = waitAndGetUserSpecifiedFileStructure(f.getName());
                UserDialogDecisionState userDecision = model.getUserDialogDecisionState();

                if(UserDialogDecisionState.CANCEL.equals(userDecision))
                {
                    canceled = true;
                    break;
                }
                else if(UserDialogDecisionState.APPLY_TO_ALL.equals(userDecision))
                {
                    defaultStructure = model.getNanopullerFileStructure();
                    this.lastSpecifiedFileStructure = defaultStructure;
                    break;
                }
                else if(UserDialogDecisionState.APPLY.equals(userDecision))
                {
                    NanopullerFileStructure currentFileStructure = model.getNanopullerFileStructure();
                    this.lastSpecifiedFileStructure = currentFileStructure;
                    fileStructures.put(f, currentFileStructure);
                }
            }
        }

        this.fileStructureData = fileStructures.isEmpty() ? new NanopullerDefaultStructureDirectives(defaultStructure) : new NanopullerMixedStructureDirectives(fileStructures, defaultStructure);

        return canceled;
    }

    @Override
    public List<SimpleSpectroscopySource> readSources(File f, SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {
        try (FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath()))
        {                   
            try(Scanner scanner = new Scanner(channel);) 
            {
                NanopullerFileStructure currentFileStructure = fileStructureData.getFileStructure(f);

                List<double[]> approachPoints = new ArrayList<>();
                List<double[]> withdrawPoints = new ArrayList<>();

                scanner.useDelimiter(DELIMITER); 
                scanner.useLocale(Locale.GERMANY);

                //scan start
                scanner.nextLine();

                //optical lever sensitivity                
                String sensitivityLine = scanner.nextLine().trim();
                String[] sensitivityLineSplitted = sensitivityLine.split(":");
                UnitExpression sensitivity = parseUnitExpression(sensitivityLineSplitted[0], sensitivityLineSplitted[1]);

                String springConstantLine = scanner.nextLine().trim();
                String[] springConstantLineSplitted = springConstantLine.split(":");

                UnitExpression springConstant = parseUnitExpression(springConstantLineSplitted[0], springConstantLineSplitted[1]);

                //velocity
                scanner.nextLine();
                //recording time
                scanner.nextLine();

                QuantityIncreaseDirection forceIncreaseDirection = currentFileStructure.getForceIncreaseDirection();
                QuantityIncreaseDirection distanceIncreaseDirection = currentFileStructure.getPiezoPositionIncreaseDirection();

                double distanceFactor = QuantityIncreaseDirection.TOWARDS_SAMPLE.equals(distanceIncreaseDirection)? -0.001: 0.001;//the z - piezo position; it is specified in nm, we convert it to um
                double forceFactor = QuantityIncreaseDirection.TOWARDS_SAMPLE.equals(forceIncreaseDirection)? -0.001: 0.001;//force in pN, we convert it to nN

                while(scanner.hasNextDouble())
                {
                    double[] withdrawPoint = new double[2];
                    withdrawPoint[0] = distanceFactor*scanner.nextDouble();
                    withdrawPoint[1] = forceFactor*scanner.nextDouble();

                    double[] approachPoint = new double[2];
                    approachPoint[0] = distanceFactor*scanner.nextDouble();
                    approachPoint[1] = forceFactor*scanner.nextDouble(); 

                    withdrawPoints.add(withdrawPoint);
                    approachPoints.add(approachPoint);
                }

                Quantity xQuantity = Quantities.DISTANCE_MICRONS;
                Quantity yQuantity = Quantities.FORCE_NANONEWTONS;

                double[][] approach = SortedArrayOrder.DESCENDING.sortX(approachPoints.toArray(new double[][] {}));
                double[][] withdraw = SortedArrayOrder.ASCENDING.sortX(withdrawPoints.toArray(new double[][] {}));

                Channel1DData approachChannelData = new FlexibleChannel1DData(approach, xQuantity, yQuantity, SortedArrayOrder.DESCENDING);
                Channel1DData withdrawChannelData = new FlexibleChannel1DData(withdraw,xQuantity, yQuantity, SortedArrayOrder.ASCENDING);

                StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, IOUtilities.getBareName(f), f.getAbsolutePath(), approachChannelData, withdrawChannelData);
                source.setSensitivity(sensitivity.derive(Units.MICRO_METER_PER_VOLT_UNIT).getValue());
                source.setSpringConstant(springConstant.derive(Units.NEWTON_PER_METER).getValue());

                List<SimpleSpectroscopySource> sources = Collections.<SimpleSpectroscopySource>singletonList(source);
                return sources;
            } 
            catch (RuntimeException | ParseException e) 
            {
                e.printStackTrace();

                throw new UserCommunicableException("Error occured while reading the file", e);     
            } 
        }
        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);     
        }  
    }   

    private static UnitExpression parseUnitExpression(String quantityUnitString, String valueString) throws ParseException
    {
        Quantity quantity = UnitQuantity.buildQuantity(quantityUnitString);
        NumberFormat format = NumberFormat.getNumberInstance(Locale.GERMANY);
        double value = format.parse(valueString.trim()).doubleValue();
        UnitExpression numericExpression = new UnitExpression(value, quantity.getUnit());

        return numericExpression;
    }

    private NanopullerFileStructureModel waitAndGetUserSpecifiedFileStructure(String fileName) throws UserCommunicableException
    {
        QuantityIncreaseDirection forceIncreaseDirection = this.lastSpecifiedFileStructure != null ? this.lastSpecifiedFileStructure.getForceIncreaseDirection() : QuantityIncreaseDirection.TOWARDS_SAMPLE;
        QuantityIncreaseDirection piezoPositionIncreaseDirection = this.lastSpecifiedFileStructure != null ? this.lastSpecifiedFileStructure.getPiezoPositionIncreaseDirection() : QuantityIncreaseDirection.TOWARDS_SAMPLE;
        final NanopullerFileStructureModel model = new NanopullerFileStructureModel(forceIncreaseDirection, piezoPositionIncreaseDirection, fileName, true);

        Runnable runnable = new Runnable() 
        {               
            @Override
            public void run() 
            {
                NanopullerFileStructureDialog dialog = new NanopullerFileStructureDialog(AtomicJ.getApplicationFrame(),model);
                dialog.setVisible(true);
            }
        };

        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);     
        }

        return model;
    }

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
        boolean accept = filter.accept(f) && isFileBeginnigConsistentWithNanopullerFile(f);

        return accept;
    }


    private static class NanopullerDefaultStructureDirectives extends NanopullerFileStructureData
    {
        private final NanopullerFileStructure defaultFileStructure;

        public NanopullerDefaultStructureDirectives(NanopullerFileStructure defaultFileStructure) 
        {
            this.defaultFileStructure = defaultFileStructure;
        }

        @Override
        public NanopullerFileStructure getFileStructure(File f) 
        {
            return defaultFileStructure;
        }
    }

    private static class NanopullerMixedStructureDirectives extends NanopullerFileStructureData
    {
        private final NanopullerFileStructure defaultFileStructure;
        private final Map<File, NanopullerFileStructure> customStructures;

        public NanopullerMixedStructureDirectives(Map<File, NanopullerFileStructure> customStructures, NanopullerFileStructure defaultFileStructure) 
        {
            this.defaultFileStructure = defaultFileStructure;
            this.customStructures = new HashMap<>(customStructures);
        }

        @Override
        public NanopullerFileStructure getFileStructure(File f) 
        {
            NanopullerFileStructure customFileStructure = customStructures.get(f);
            NanopullerFileStructure fileStructure = (customFileStructure != null) ? customFileStructure : defaultFileStructure;
            return fileStructure;
        }
    }
}