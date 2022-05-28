package atomicJ.readers.regularImage;

public enum TIFFExtraChannelType 
{
    UNSPECIFIED(0)
    {
        @Override
        public String getExtraChannelName(int index, int channelCount,
                int extraOfThisTypeIndex, int extraChannelOfThisTypeCount) 
        {
            String name = "Band " + Integer.toString(index);
            return name;
        }
    }, ASSOCIATED_ALPHA(1) 
    {
        @Override
        public String getExtraChannelName(int index, int channelCount,
                int extraOfThisTypeIndex, int extraChannelOfThisTypeCount) 
        {
            String name = extraChannelOfThisTypeCount > 1 ? "Alpha " +  Integer.toString(extraOfThisTypeIndex) : "Alpha";
            return name;
        }
    }, UNASSOCIATED_ALPHA(2) {
        @Override
        public String getExtraChannelName(int index, int channelCount,
                int extraOfThisTypeIndex, int extraChannelOfThisTypeCount)
        {
            String name = extraChannelOfThisTypeCount > 1 ? "Unassociated alpha " +  Integer.toString(extraOfThisTypeIndex) : "Unassociated alpha";
            return name;
        }
    };

    private final int code;

    TIFFExtraChannelType(int code)
    {
        this.code = code;
    }

    public abstract String getExtraChannelName(int index, int channelCount, int extraOfThisTypeIndex, int extraChannelOfThisTypeCount);

    public static TIFFExtraChannelType getExtraChannelType(int code)
    {
        for(TIFFExtraChannelType type : TIFFExtraChannelType.values())
        {
            if(type.code == code)
            {
                return type;
            }
        }

        throw new IllegalArgumentException("No TIFFExtraChannelType corresponds to the code " + code);
    }
}
