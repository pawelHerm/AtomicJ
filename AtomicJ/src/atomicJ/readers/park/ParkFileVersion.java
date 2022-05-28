package atomicJ.readers.park;

public enum ParkFileVersion
{
    VERSION_1(0x01000001), VERSION_2(0x01000002);

    private final int code;

    ParkFileVersion(int code)
    {
        this.code = code;
    }


    public static ParkFileVersion getParkFileVersion(int code)
    {
        for(ParkFileVersion version : ParkFileVersion.values())
        {
            if(version.code == code)
            {
                return version;
            }
        }

        throw new IllegalArgumentException("No ParkImageType corresponds to the code " + code);
    }
}