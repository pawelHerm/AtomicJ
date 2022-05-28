package atomicJ.readers.nanoscope;

public class NanoscopeVersion 
{
    private static final NanoscopeVersion NULL_INSTANCE = new NanoscopeVersion(-1, -1);

    private final int majorVersion;
    private final int minorVersion;

    private NanoscopeVersion(int majorVersion, int minorVersion)
    {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public static NanoscopeVersion getInstance(String vs)
    {
        String versionString = (vs == null) ? "": vs.trim();
        int majorVersion = versionString.length() >= 4 ? Integer.parseInt(versionString.substring(2, 4), 16) : -1;
        int minorVersion = versionString.length() >= 4 ? Integer.parseInt(versionString.substring(4, 6), 16) : -1;

        NanoscopeVersion versioning = new NanoscopeVersion(majorVersion, minorVersion);
        return versioning;
    }

    public static NanoscopeVersion getNullInstance()
    {
        return NULL_INSTANCE;
    }

    public boolean isEarlierThan(int majorVersion, int minorVersion)
    {
        boolean earlier = this.majorVersion < majorVersion ||(this.majorVersion == majorVersion && this.minorVersion < minorVersion);

        return earlier;
    }

    public int getMajorVersion()
    {
        return majorVersion;
    }

    public int getMinorVersion()
    {
        return minorVersion;
    }

}
