package atomicJ.readers.shimadzu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.FlexibleFlatChannel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitQuantity;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import gnu.trove.list.array.TDoubleArrayList;

public class ShimadzuSpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"txt"};
    private static final String DESCRIPTION = "Shimadzu force curve file (.txt)";
    
    private static final String COMMENT_ENTRY = "[COMMENT]";
    private static final Pattern PIXEL_DESCRIPION_PATTERN = Pattern.compile("\\d+\\s+pixels");

    private static final String PIXEL_COUNT_KEY = "Sampling Point";
    
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
    public List<SimpleSpectroscopySource> readSources(File f, SourceReadingDirectives readingDirectives) throws UserCommunicableException, IllegalImageException,
            IllegalSpectroscopySourceException
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>();
        
        try(BufferedReader bsr = new BufferedReader(new InputStreamReader(new FileInputStream(f), "ISO-8859-1"));) 
        {  
            String line;

            Map<String,String> metaData = new LinkedHashMap<>();

            while((line = bsr.readLine()) != null)
            {                 
                if(line.startsWith(COMMENT_ENTRY))
                {                  
                    break;
                }              

                String[] keyValuePair = line.split(":");
                if(keyValuePair.length > 1)
                {
                    metaData.put(keyValuePair[0], keyValuePair[1]);
                }             
            }
            
            bsr.readLine();//comment
            
            String quantities = bsr.readLine();
            String[] quantitiesTable = quantities.split(Character.toString((char)9));
            
            UnitQuantity distanceQuantity = UnitQuantity.buildQuantity(quantitiesTable[0]);
            Quantity xQuantity = Quantities.DISTANCE_MICRONS;
  
            double factorX = distanceQuantity.getUnit().getConversionFactorTo(xQuantity.getUnit());
                  
            String pixelCountDescription = metaData.get(PIXEL_COUNT_KEY);
            

            int distanceIndex = 0;
            int approachIndex = 1;
            int withdrawIndex = 2;
            
            boolean isApprachEmpty = false;
            boolean isWithdrawEmpty = false;
     
            
            boolean pixelCountKnown = pixelCountDescription != null && PIXEL_DESCRIPION_PATTERN.matcher(pixelCountDescription).matches();
            
            // System.out.println("pixelCountKnown "+pixelCountKnown);
            
            boolean sensitivityCalibrated = false;
            Quantity deflectionQuantity = sensitivityCalibrated ? Quantities.DEFLECTION_MICRONS : Quantities.DEFLECTION_VOLTS;

       
            if(pixelCountKnown)
            {
                int pixelCount = Integer.parseInt(pixelCountDescription.split("\\s+")[0]);
                
                double[][] pointsApproach = new double[pixelCount][];
                double[][] pointsWithdraw = new double[pixelCount][];
           
                int i = 0;
                
                while((line = bsr.readLine()) != null)
                { 
                    String[] curvePointCoordinates = line.split(Character.toString((char)9));
                    double distance = factorX*Double.parseDouble(curvePointCoordinates[distanceIndex]);
                    double approachDeflection = Double.parseDouble(curvePointCoordinates[approachIndex]);
                    double withdrawDeflection = Double.parseDouble(curvePointCoordinates[withdrawIndex]);

                    pointsApproach[i] = new double[] {distance, approachDeflection};
                    pointsWithdraw[i] = new double[] {distance, withdrawDeflection};
                    
                    i++;
                }
                
                SortedArrayOrder distanceOrder = SortedArrayOrder.getInitialXOrder(pointsApproach);
               
                Channel1DData approachDeflectionData = isApprachEmpty ? FlexibleChannel1DData.getEmptyInstance(xQuantity, deflectionQuantity) : new FlexibleChannel1DData(pointsApproach, xQuantity, deflectionQuantity, distanceOrder);
                Channel1DData withdrawDeflectionData = isWithdrawEmpty ? FlexibleChannel1DData.getEmptyInstance(xQuantity, deflectionQuantity) : new FlexibleChannel1DData(pointsWithdraw, xQuantity, deflectionQuantity, distanceOrder);

                SimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, approachDeflectionData, withdrawDeflectionData);
                sources.add(source);
            }
            
            else
            {
                TDoubleArrayList distanceValues = new TDoubleArrayList();
                TDoubleArrayList approachDeflectionValues = new TDoubleArrayList();
                TDoubleArrayList withdrawDeflectionValues = new TDoubleArrayList();
                
                while((line = bsr.readLine()) != null)
                { 
                    String[] curvePointCoordinates = line.split(Character.toString((char)9));
                    double distance = factorX*Double.parseDouble(curvePointCoordinates[distanceIndex]);
                    double approachDeflection = Double.parseDouble(curvePointCoordinates[approachIndex]);
                    double withdrawDeflection = Double.parseDouble(curvePointCoordinates[withdrawIndex]);

                    distanceValues.add(distance);
                    approachDeflectionValues.add(approachDeflection);
                    withdrawDeflectionValues.add(withdrawDeflection);                    
                }
                
                double[] distanceArray = distanceValues.toArray();
                double[] deflectionApproachArray = approachDeflectionValues.toArray();
                double[] deflectionWithdrawArray = withdrawDeflectionValues.toArray();

                SortedArrayOrder distanceOrder = SortedArrayOrder.getInitialOrder(distanceArray);
                Channel1DData approachDeflectionData = isApprachEmpty ? FlexibleChannel1DData.getEmptyInstance(xQuantity, deflectionQuantity) : new FlexibleFlatChannel1DData(distanceArray, deflectionApproachArray, xQuantity, deflectionQuantity, distanceOrder);
                Channel1DData withdrawDeflectionData = isWithdrawEmpty ? FlexibleChannel1DData.getEmptyInstance(xQuantity, deflectionQuantity) : new FlexibleFlatChannel1DData(distanceValues.toArray(), deflectionWithdrawArray, xQuantity, deflectionQuantity, distanceOrder);

                SimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, approachDeflectionData, withdrawDeflectionData);
                sources.add(source);
            }           
            
        }
        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);     
        } 
        
        return sources;
    }

}
