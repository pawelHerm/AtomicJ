package atomicJ.readers.park;

enum ParkImageType
{
    MAPPED_2D(0), LINE_PROFILE(1), SPECTROSCOPY(2);

    private final int code;

    ParkImageType(int code)
    {
        this.code = code;
    }

    public static ParkImageType getParkImageType(int code)
    {
        for(ParkImageType type : ParkImageType.values())
        {
            if(type.code == code)
            {
                return type;
            }
        }

        throw new IllegalArgumentException("No ParkImageType corresponds to the code " + code);
    }
}