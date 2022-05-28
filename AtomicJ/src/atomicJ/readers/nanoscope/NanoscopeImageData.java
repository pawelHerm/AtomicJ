package atomicJ.readers.nanoscope;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.regex.Matcher;

import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.utilities.FileInputUtilities;

public class NanoscopeImageData extends NanoscopeData
{    
    /*
     * I introduced several modifications to the original Nanoscope files, to find out how the NanoScope Analysis software
     * interprets various fields. I have found that:
     * 
     * - the fields "\Samps/line" and "\Number of lines" are disregarded, both when they are under the header "\*Ciao scan list"
     *   and "\*Ciao image list"
     *   
     * - the Samples/Line and Lines fields displayed in NanoScope Analysis are read from the fields "Valid data len X" and "Valid data len Y", respectively
     * 
     * - the field "\Scan Size" in "\*Ciao image list" is disregarded
     * 
     * - the scan size in is calculated based on the field "\Scan Size" in "\*Ciao scan list". The single scan size value
     *   is treated as width. The corresponding height is calculated form the aspect ratio value in "\*Ciao image list".
     *   The value of the field "\Aspect Ratio" in "\*Ciao scan list" is disregarded.
     * 
     * - if the value of aspect ratio is grater than 1 (i.e. width is less than height), Nanoscope Analysis assumes that aspect ratio is 1
     * 
     * - the fields "\X Offset" and "\Y Offset" in "\*Ciao scan list" are disregarded when rendering the image;
     * however they are important for positioning force curve over the image
     * 
     * - the "\Plane fit" field in "\*Ciao image list" is disregarded. It seems that the plane fit values are set during
     *   image recording. They are not changed when plane correction is performed in Nanoscope Analysis software.
     *   
     *   - the hard scale for images has to be calculated from the corresponding hard value as hardValue/2^16 (assuming, that
     *   there are two bytes per pixel). The corresponding hard scale usually is identical, BUT NOT ALWYAYS.
     *   
     *   - the 7E byte is used to represent micro sign (this is the byte for tilde sign in extended ASCII).
     */

    //Nanoscope fields in the text header have inconsistent case, e.g. the files use Scan Size or Scan size, depending on the file version
    //so we check against field converted to lower case


    //this properties should be specified for each image list

    //see http://www.physics.arizona.edu/~smanne/DI/software/fileformats.html
    private static final String PLANE_FIT = "plane fit";

    private static final String ASPECT_RATIO = "aspect ratio";

    //both in \*Ciao scan list and in \*Ciao image list
    private static final String SAMPLES_PER_LINE = "samps/line";

    //both in \*Ciao scan list and in \*Ciao image list
    private static final String NUMBER_OF_LINES = "number of lines";

    //value consist of : lengthA lengthB unit (with tilde instead of micro sign)
    private static final String SCAN_SIZE = "scan size"; 

    private static final String FRAME_DIRECTION = "frame direction";
    private static final String FRAME_DIRECTION_UP = "up";

    private static final String VALID_DATA_X_START = "valid data start x";
    private static final String VALID_DATA_Y_START = "valid data start y";
    private static final String VALID_DATA_X_LENGTH = "valid data len x";
    private static final String VALID_DATA_Y_LENGTH = "valid data len y";  

    private UnitExpression xScanSize = null; 
    private UnitExpression yScanSize = null; 

    //these values are calculated based on the scan size from the \*Ciao scan list
    //and the aspect ration, from the current \*Ciao image list
    private UnitExpression externalScanXSize;
    private UnitExpression externalScanYSize;

    private double aspectRatio = 1;

    private boolean frameDirectionUp;

    private int validDataStartX = -1;
    private int validDataStartY = -1;
    private int validDataLengthX = -1;
    private int validDataLengthY = -1;

    private double planeFitX = 0;
    private double planeFitY = 0;
    private double planeFitOffset = 0;
    private int planeFitType= 5;

    /*
     * the type parameter is:
1 offset removed
2 full plane fit 
3 captured plane removed
5 no plane fit removed

If the type is 5 (no plane fit), then no plane fit or offset has been applied to the data. 
If the type is 1 (offset only), then you can recover the original raw data by the scaling 
as described above, then adding the offset. If the plane fit type is 2, then a 1st order plane
 fit has been removed from the data. To recover the original unfit data 
is a bit difficult to describe in words, so maybe some pseudo c code will make it clearer:
     */

    private static final String CIAO_IMAGE_LIST_IDENTIFER = "\\*Ciao image list";
    private static final String IMAGE_LIST_IDENTIFER = "\\*Image list";
    private static final String NCAFM_LIST_IDENTIFIER = "\\*NCAFM image list";
    private static final String AFM_LIST_IDENTIFIER = "\\*AFM image list";
    private static final String STM_LIST_IDENTIFIER = "\\*STM image list";

    public NanoscopeImageData()
    {
        super(CIAO_IMAGE_LIST_IDENTIFER, IMAGE_LIST_IDENTIFER, NCAFM_LIST_IDENTIFIER, AFM_LIST_IDENTIFIER, STM_LIST_IDENTIFIER);
    }

    public static boolean isSectionBeginningStatic(String line)
    {
        boolean beginning = false;

        String[] ids = new String[] {CIAO_IMAGE_LIST_IDENTIFER, IMAGE_LIST_IDENTIFER, NCAFM_LIST_IDENTIFIER, AFM_LIST_IDENTIFIER, STM_LIST_IDENTIFIER};

        if(line != null)
        {
            for(String id: ids)
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


    public Grid2D getGrid()
    {
        int dataLengthX = getValidDataLengthX();
        int dataLengthY = getValidDataLengthY();

        double xIncrement = externalScanXSize.getValue()/(dataLengthX - 1);
        double yIncrement = externalScanYSize.getValue()/(dataLengthY - 1);

        Quantity xQuantity = new UnitQuantity("Distance", externalScanXSize.getUnit());
        Quantity yQuantity = new UnitQuantity("Distance", externalScanYSize.getUnit());

        Grid2D grid = new Grid2D(xIncrement, yIncrement, 0, 0, dataLengthY, dataLengthX, xQuantity, yQuantity);

        return grid;
    }

    public boolean isFrameDirectionUp()
    {
        return frameDirectionUp;
    }

    public int getValidDataLengthX()
    {
        return validDataLengthX;
    }

    public int getValidDataLengthY()
    {
        return validDataLengthY;
    }


    //the scan size can be found both under the header "\*Ciao scan list" (one value)
    //and in the "\*Ciao image list" records (to values). The Nanoscope software takes into account only 
    //"\*Ciao scan list", treats it as width of the scan area and calculates the height using the aspect ratio
    //read from "\*Ciao image list records".
    //As a result, we have to read the scan size from NanoscopeScaleList object and set it for the NanoscopePFCData objects
    public void setScanSize(UnitExpression scanSize)
    {        
        this.externalScanXSize = scanSize;
        this.externalScanYSize = scanSize.multiply(1./aspectRatio);
    }

    @Override
    public boolean isFullySpecified()
    {
        boolean fullySpecified = super.isFullySpecified();

        fullySpecified = fullySpecified && (externalScanXSize != null);
        fullySpecified = fullySpecified && (externalScanYSize != null);

        fullySpecified = fullySpecified && validDataStartX >= 0;
        fullySpecified = fullySpecified && validDataStartY >= 0;

        fullySpecified = fullySpecified && validDataLengthX >= 0;
        fullySpecified = fullySpecified && validDataLengthY >= 0;

        return fullySpecified;
    }

    public ImageChannel buildImageChannel(File f) throws UserCommunicableException
    {        
        int dataOffset = getDataOffset();
        int imageByteSize = getSpecifiedDataLength();
        int rowCount = validDataLengthY;
        int columnCount = validDataLengthX;
        int bytesToSkip = getNumberOfBytesToSkipPerPoint();

        try (FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath()))
        {
            channel.position(dataOffset);

            ByteBuffer byteBuffer = FileInputUtilities.readBytesToBuffer(channel, imageByteSize, ByteOrder.LITTLE_ENDIAN);
            UnitExpression zScale = getZScaleBasedOnHardValueAndBytePerPixel();

            double factor = zScale.getValue();

            Grid2D grid = getGrid();

            DoubleArrayReaderType reader = getDoubleReader();         
            double[][] data = bytesToSkip > 0 ? reader.readIn2DArrayRowByRow(rowCount, columnCount, bytesToSkip, factor, 0, byteBuffer) : reader.readIn2DArrayRowByRow(rowCount, columnCount, factor, byteBuffer);      

            ImageChannel imageChannel = new ImageChannel(data, grid, getZQuantity(), getDataName(), false);

            return imageChannel;
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }
    }

    public Quantity getZQuantity()
    {
        PrefixedUnit softZScaleUnit = getZScale().getUnit();
        Quantity quantity = new UnitQuantity(getDataName(), softZScaleUnit);

        return quantity;
    }

    private void parseAndSetValidDataLengthX(String dataLengthXField)
    {
        validDataLengthX = parseIntValue(dataLengthXField);
    }

    private void parseAndSetValidDataLengthY(String dataLengthYField)
    {
        validDataLengthY = parseIntValue(dataLengthYField);
    }

    private void parseAndSetValidDataStartX(String dataStartXField)
    {
        validDataStartX = parseIntValue(dataStartXField);
    }

    private void parseAndSetValidDataStartY(String dataStartYField)
    {
        validDataStartY = parseIntValue(dataStartYField);
    }

    private void parseAndSetPlaneFitParameters(String planeFitField)
    {
        String fieldValue = planeFitField.split(":")[1].trim();

        String[] words = fieldValue.split("\\s++");   

        int wordsCount = words.length;

        if(wordsCount == 4)
        {
            planeFitX = Double.parseDouble(words[0]);
            planeFitY = Double.parseDouble(words[1]);
            planeFitOffset = Double.parseDouble(words[2]);
            planeFitType = Integer.parseInt(words[3]);
        }
    }

    private void parseAndSetAspectRatio(String field)
    {
        //aspect ratio field has the following stracture : Aspect Ratio: width:height,
        //so we can split it using  ":" as a delimiting pattern into three words
        String[] words = field.split(":");   
        int wordsCount = words.length;

        if(wordsCount == 3)
        {
            this.aspectRatio = Double.parseDouble(words[1])/Double.parseDouble(words[2]);            
        }
    }

    public void parseAndSetFrameDirection(String frameDirectionFieldLowerCase)
    {            
        String[] words = frameDirectionFieldLowerCase.split("\\s+");   

        int wordsCount = words.length;

        if(wordsCount > 0)
        {
            String direction = words[wordsCount - 1];
            this.frameDirectionUp = FRAME_DIRECTION_UP.equals(direction);//we use fieldLowerCase here on purpose, in case Bruker changes "Allow" to "allow"
        }     
    }

    //if the scanSizeField contains only one numeric value, then we assume that the scan size
    //is a square
    public void parseAndSetScanSize(String scanSizeField)
    {         
        if(scanSizeField == null)
        {
            return;
        }

        //we assume that the last word is the unit

        String[] words = scanSizeField.split("\\s+");   

        int wordsCount = words.length;

        String unitString = words[wordsCount - 1];
        PrefixedUnit scanSizeUnit = UnitUtilities.getSIUnit(unitString);  

        Matcher scanSizeMatcher = NanoscopeData.numberPattern.matcher(scanSizeField);
        boolean xFound = scanSizeMatcher.find();

        double xScanSizeValue = xFound ? Double.parseDouble(scanSizeMatcher.group(1)) : 1;

        boolean yFound = scanSizeMatcher.find();

        double yScanSizeVaue = yFound ? Double.parseDouble(scanSizeMatcher.group(1)):  xScanSizeValue;

        this.xScanSize = new UnitExpression(xScanSizeValue, scanSizeUnit).deriveSimpleForm();
        this.yScanSize = new UnitExpression(yScanSizeVaue, scanSizeUnit).deriveSimpleForm();
    }

    @Override
    public void readField(String fieldRaw)
    {
        super.readField(fieldRaw);

        String field = dropPrefices(fieldRaw);
        String fieldLowerCase = field.toLowerCase(); //Nanoscope fields in the text header have inconsistent case, e.g. the files use Scan Size or Scan size, depending on the file version
        //so we check against field converted to lower case

        if(fieldLowerCase.startsWith(SCAN_SIZE))
        {            
            parseAndSetScanSize(field);
        }
        else if(fieldLowerCase.startsWith(ASPECT_RATIO))
        {
            parseAndSetAspectRatio(field);
        }
        else if(fieldLowerCase.startsWith(PLANE_FIT))
        {
            parseAndSetPlaneFitParameters(field);
        }
        else if(fieldLowerCase.startsWith(FRAME_DIRECTION))
        {
            parseAndSetFrameDirection(fieldLowerCase);//we use fieldLowerCase here on purpose, in case Bruker changes "Allow" to "allow"
        }
        else if(fieldLowerCase.startsWith(VALID_DATA_X_LENGTH))
        {
            parseAndSetValidDataLengthX(field);
        }
        else if(fieldLowerCase.startsWith(VALID_DATA_Y_LENGTH))
        {
            parseAndSetValidDataLengthY(field);
        }
        else if(fieldLowerCase.startsWith(VALID_DATA_X_START))
        {
            parseAndSetValidDataStartX(field);
        }
        else if(fieldLowerCase.startsWith(VALID_DATA_Y_START))
        {
            parseAndSetValidDataStartY(field);
        }
    }

    //plane correction code
    //  if(false)
    //  {            
    //      data = new double[rowCount][columnCount];
    //
    //      double dataLengthX = getValidDataLengthX();
    //      double dataLengthY = getValidDataLengthY();
    //
    //      for(int i = 0; i<rowCount; i++)
    //      {
    //          double yCorrection = -0.5*planeFitY + planeFitY*i/dataLengthY;
    //
    //          for(int j = 0; j<columnCount; j++)
    //          {
    //              double xCorrection = -0.5*planeFitX + planeFitX*i/dataLengthX;
    //
    //              data[i][j] = (byteBuffer.getShort() + xCorrection + yCorrection + planeFitOffset)*factor;
    //          }
    //      } 
    //
    //  }
    //  else
    //  {

    //  }
}
