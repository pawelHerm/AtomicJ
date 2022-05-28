package atomicJ.readers.nanoscope;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.UnitUtilities;


public abstract class NanoscopeHeaderSection
{    
    private static final String RELATIVE_FIELD_PREFIX = "\\@";

    final static Pattern REFERENCE_FIELD_PATTERN = Pattern.compile("\\@[0-9]*+:?(.*+)");
    final static Pattern SQUARE_BRACKETS_PATTERN = Pattern.compile("\\[(.*?)\\]");
    final static Pattern HARD_SCALE_PATTERN = Pattern.compile("\\(([^\\[\\]]*?)\\)\\s+([-0-9]++[0-9.Ee-]++)"); //the first group cannot contain [ or ], otherwise fields like  V [Sens. Log(Resistance)] (0.0003750000 log(Ohm)/LSB) 24.57600 log(Ohm) 
    //are incorrectly parsed
    final static Pattern QUOTATIONS_PATTERN = Pattern.compile("\"(.*?)\"");
    final static Pattern NUMBER_PATTERN = Pattern.compile("([-0-9]++[0-9.Ee-]*+)");

    private final List<String> identifiers;

    public NanoscopeHeaderSection(String... ids)
    {
        this.identifiers = Collections.unmodifiableList(Arrays.<String>asList(ids));
    }

    public boolean isSectionBeginning(String line)
    {
        boolean beginning = false;

        if(line != null)
        {
            for(String id: identifiers)
            {                
                if(id.equals(line.trim()))
                {
                    beginning = true;
                    break;
                }
            }
        }

        return beginning;
    }

    public abstract void readField(String fieldRaw);

    protected String readInFieldsToHeaderSection(BufferedReader in) throws IOException
    {
        String line;
        while((line = in.readLine()) != null && !line.startsWith("\\*") && !line.startsWith("\u001A"))
        {                              
            readField(line);
        }

        return line;
    }

    protected int parseIntValue(String field)
    {
        String[] words = field.split("\\s+");   

        int wordsCount = words.length;

        int value = -1;
        if(wordsCount > 0)
        {
            value = Integer.parseInt(words[wordsCount - 1]);
        }     

        return value;
    }

    protected double parseDoubleValue(String field)
    {
        String[] words = field.split("\\s+");   

        int wordsCount = words.length;

        double value = -1;
        if(wordsCount > 0)
        {
            value = Double.parseDouble(words[wordsCount - 1]);
        }     

        return value;
    }

    protected String parseStringValue(String field)
    {
        String[] words = field.split("\\s+");   

        int wordsCount = words.length;

        String value = "";
        if(wordsCount > 0)
        {
            value = words[wordsCount - 1];
        }     

        return value;
    }


    public UnitExpression parseUnitExpression(String field)
    {         
        UnitExpression expression = null;

        if(field == null)
        {
            return expression;
        }

        Matcher valueMatcher = NanoscopeData.numberPattern.matcher(field);
        valueMatcher.find();

        double value = Double.parseDouble(valueMatcher.group(1));

        //we assume that the last word is the unit

        String[] words = field.split("\\s+");   

        int wordsCount = words.length;

        String unitString = words[wordsCount - 1];
        PrefixedUnit unit = UnitUtilities.getSIUnit(unitString);  

        expression = new UnitExpression(value, unit);

        return expression;
    }

    protected UnitExpression extractHardScale(String field)
    {       
        double value = Double.NaN;
        PrefixedUnit unit = SimplePrefixedUnit.getNullInstance();

        if(field == null)
        {
            return new UnitExpression(value, unit);
        }

        Matcher matcher = HARD_SCALE_PATTERN.matcher(field);

        if(matcher.find())
        {
            String hardScaleString = matcher.group(1).trim();

            String[] words = hardScaleString.split("\\s+");              
            value = Double.parseDouble(words[0]);

            if(words.length > 1)
            {               
                //we want to get rid of LSB in the denominator
                unit = UnitUtilities.getSIUnit(words[1].split("/")[0]);            
            }
        }  

        return new UnitExpression(value, unit);
    }

    //we assume that the last word in the field is the hard value unit and that it is
    //preceded by the hard value itself
    protected UnitExpression extractHardValueNew(String field)
    {
        UnitExpression hardValue = new UnitExpression(Double.NaN, SimplePrefixedUnit.getNullInstance());;

        if(field == null)
        {
            return hardValue;
        }

        String[] words = field.split("\\s+");   

        int wordsCount = words.length;       

        if(wordsCount > 1)
        {
            double number = Double.parseDouble(words[wordsCount - 2]);
            PrefixedUnit unit = UnitUtilities.getSIUnit(words[wordsCount - 1]);
            hardValue = new UnitExpression(number,unit);
        }  

        return hardValue;
    }

    //we assume that the last word in the field is the hard value unit and that it is
    //preceded by the hard value itself
    protected UnitExpression extractHardValue(String field)
    {
        PrefixedUnit unit = extractScaleUnit(field);

        double hardScale = Double.NaN;

        if(field == null)
        {
            return new UnitExpression(hardScale, unit);
        }

        String[] words = field.split("\\s+");   

        int wordsCount = words.length;       

        if(wordsCount > 1)
        {
            String hardValueString = words[wordsCount - 2];
            hardScale = Double.parseDouble(hardValueString);
        }  

        return new UnitExpression(hardScale, unit);
    }

    protected String extractExternalDesignationForSelection(String selectionField)
    {
        String designation = null;

        if(selectionField == null)
        {
            return designation;
        }

        Matcher externalDesignationMatcher = QUOTATIONS_PATTERN.matcher(selectionField);
        if(externalDesignationMatcher.find())
        {
            designation = externalDesignationMatcher.group(1).trim();
        }

        return designation;
    }

    protected String extractInternalDesignationForSelection(String field)
    {
        String reference = null;

        if(field == null)
        {
            return reference;
        }

        Matcher internalDesignationeMatcher = SQUARE_BRACKETS_PATTERN.matcher(field);

        if(internalDesignationeMatcher.find())
        {
            reference = internalDesignationeMatcher.group(1).trim();
        }    

        return reference;
    }

    protected String extractSoftScaleReference(String field)
    {
        String reference = null;

        if(field == null)
        {
            return reference;
        }

        Matcher softScaleReferenceMatcher = SQUARE_BRACKETS_PATTERN.matcher(field);

        if(softScaleReferenceMatcher.find())
        {
            reference = "\\@" + softScaleReferenceMatcher.group(1).trim();
        }    

        return reference;
    }

    protected UnitExpression extractSoftScale(String field)
    {
        PrefixedUnit unit = extractScaleUnit(field);
        double softScaleValue = Double.NaN;

        if(field == null)
        {
            return new UnitExpression(softScaleValue, unit);
        }

        Matcher softScaleMatcher = NUMBER_PATTERN.matcher(field);

        if(softScaleMatcher.find())
        {
            String softScaleString = softScaleMatcher.group(1).trim();

            softScaleValue = Double.parseDouble(softScaleString);               
        }     

        return new UnitExpression(softScaleValue, unit);
    }

    private PrefixedUnit extractScaleUnit(String field)
    {
        PrefixedUnit unit = SimplePrefixedUnit.getNullInstance();

        if(field == null)
        {
            return unit;
        }

        String[] words = field.split("\\s+");   

        int wordsCount = words.length;

        if(wordsCount > 0)
        {
            String unitString = words[wordsCount - 1].trim();

            //if it is numeric, it means there was no unit
            if(!unitString.matches("-?\\d+(\\.\\d+)?"))
            {
                unit = UnitUtilities.getSIUnit(unitString);                 
            }
        }

        return unit;
    }

    protected String findReferenceField(String key, List<String> referenceFields)
    {        
        String softScaleField = null;

        for(String field : referenceFields)
        {            
            if(field.startsWith(key + ":"))
            {
                softScaleField = field;                
                break;
            }
        }

        return softScaleField;
    }

    protected String dropPrefices(String field)
    {
        String result = field.substring(1);

        if(field.startsWith(RELATIVE_FIELD_PREFIX))
        {
            Matcher matcher = REFERENCE_FIELD_PATTERN.matcher(field);

            if(matcher.find())
            {
                result = matcher.group(1);
            }
        }   

        return result;
    }
}