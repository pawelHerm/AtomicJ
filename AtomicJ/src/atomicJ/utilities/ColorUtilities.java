package atomicJ.utilities;

import java.awt.color.ColorSpace;
import java.util.ArrayList;
import java.util.List;

public class ColorUtilities 
{
    public static String getColorSpaceName(int space)
    {
        if(ColorSpace.CS_CIEXYZ == space)
        {
            return "CIE XYZ";
        }
        else if(ColorSpace.CS_sRGB == space)
        {
            return "sRGB";
        }     
        else if(ColorSpace.CS_LINEAR_RGB == space)
        {
            return "Linear RGB";
        }
        else if(ColorSpace.TYPE_RGB == space)
        {
            return "RGB";
        }  
        else if(ColorSpace.CS_GRAY == space)
        {
            return "Gray";
        }
        else if(ColorSpace.TYPE_CMY== space)
        {
            return "CMY";
        }
        else if(ColorSpace.TYPE_CMYK == space)
        {
            return "CMYK";
        }
        else if(ColorSpace.TYPE_HSV == space)
        {
            return "HSV";
        }
        else if(ColorSpace.TYPE_HLS == space)
        {
            return "HLS";
        }
        else if(ColorSpace.TYPE_GRAY == space)
        {
            return "Gray";
        }
        else if(ColorSpace.TYPE_Lab == space)
        {
            return "Lab";
        }
        else if(ColorSpace.TYPE_Luv == space)
        {
            return "Luv";
        }
        else if(ColorSpace.TYPE_YCbCr == space)
        {
            return "YCbCr";
        }
        else if(ColorSpace.TYPE_XYZ == space)
        {
            return "XYZ";
        }
        else if(ColorSpace.TYPE_Yxy == space)
        {
            return "Yxy";
        }
        else if(ColorSpace.CS_PYCC == space)
        {
            return "Photo YCC";
        }
        else if(ColorSpace.TYPE_2CLR == space)
        {
            return "2CLR";
        }
        else if(ColorSpace.TYPE_3CLR == space)
        {
            return "3CLR";
        }
        else if(ColorSpace.TYPE_4CLR == space)
        {
            return "4CLR";
        }
        else if(ColorSpace.TYPE_5CLR == space)
        {
            return "5CLR";
        }
        else if(ColorSpace.TYPE_6CLR == space)
        {
            return "6CLR";
        }
        else if(ColorSpace.TYPE_7CLR == space)
        {
            return "7-component";
        }
        else if(ColorSpace.TYPE_8CLR == space)
        {
            return "8-component";
        }
        else if(ColorSpace.TYPE_9CLR == space)
        {
            return "9-component";
        }
        else if(ColorSpace.TYPE_ACLR == space)
        {
            return "10-component";
        }
        else if(ColorSpace.TYPE_BCLR == space)
        {
            return "11-component";
        }
        else if(ColorSpace.TYPE_CCLR == space)
        {
            return "12-component";
        }
        else if(ColorSpace.TYPE_DCLR == space)
        {
            return "13-component";
        }
        else if(ColorSpace.TYPE_ECLR == space)
        {
            return "14-component";
        }
        else if(ColorSpace.TYPE_FCLR == space)
        {
            return "15-component";
        }
        return "Unrecognized";
    }

    public static List<String> getColorChannelNames(ColorSpace colorSpace, boolean hasAlpha)
    {
        List<String> channelNames = new ArrayList<>();

        int n = colorSpace.getNumComponents();

        for(int i = 0; i<n; i++)
        {
            channelNames.add(colorSpace.getName(i));
        }

        if(hasAlpha)
        {
            channelNames.add("Alpha");
        }

        return channelNames;
    }
}

