package atomicJ.readers.park;

enum ParkSpectroscopyType
{
    REGULAR(0), //FD or IV cruve, old 
    INDENTER(1), //old
    FD(2), IV(3), 
    NANO_INDENTER(4), 
    PHOTO_CURRENT(5), //(TRPCM, PCM) 
    ID_CURVE(6), //(Current-Distance Curve, ICM Approach Curve)
    TA_CURVE(7); //(Thermal Analysis Curve for SThM)

    private final int code;

    ParkSpectroscopyType(int code)
    {
        this.code = code;
    }

    public static ParkSpectroscopyType getParkSpectroscopyType(int code)
    {
        for(ParkSpectroscopyType type : ParkSpectroscopyType.values())
        {
            if(type.code == code)
            {
                return type;
            }
        }

        throw new IllegalArgumentException("No ParkSpectroscopyType corresponds to the code " + code);
    }
}