package atomicJ.readers.regularImage;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;


public enum TIFFPhotometricInterpretation 
{
    WHITE_IS_ZERO(0, "Gray",Collections.singletonList("Gray")), BLACK_IS_ZERO(1, "Gray", Collections.singletonList("Gray")), RGB(2, "RGB",Collections.unmodifiableList(Arrays.asList("Red","Green","Blue"))),
    PALETTE_COLOR(3,"Indexed RGB",Collections.unmodifiableList(Arrays.asList("Red","Green","Blue"))),TRANSPARENCY_MASK(4, "Transparency",Collections.singletonList("Transparency")),
    CMYK(5, "CMYK",Collections.unmodifiableList(Arrays.asList("Cyan","Magenta","Yellow","Black"))), Y_CB_CR(6,"RGB",Collections.unmodifiableList(Arrays.asList("Red","Green","Blue"))), CIE_LAB(8,"CIE L*a*b",Collections.unmodifiableList(Arrays.asList("L","a","b"))), CFA_ARRAY(3280,"CFA Array",Collections.unmodifiableList(Arrays.asList("Red","Green","Blue")));

    private final int code;
    private final String lociReadColorSpace;
    private final List<String> channelNames;

    TIFFPhotometricInterpretation(int code, String lociReadColorSpace, List<String> lociChnannelNames)
    {
        this.code = code;
        this.channelNames = lociChnannelNames;
        this.lociReadColorSpace =  lociReadColorSpace;
    }

    public String getColorSpaceAsReadByBioFormats()
    {
        return lociReadColorSpace;
    }

    public List<String> getChannelNamesAsReadByBioFormats()
    {
        return channelNames;
    }

    public static TIFFPhotometricInterpretation get(int code)
    {
        for(TIFFPhotometricInterpretation interp : TIFFPhotometricInterpretation.values())
        {
            if(interp.code == code )
            {
                return interp;
            }
        }

        throw new IllegalArgumentException("No TIFFPhotometricInterpretation corresponds to the code " + code);
    }
}
