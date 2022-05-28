package atomicJ.readers.nanoscope;

public class NanoscopeFileList extends NanoscopeHeaderSection
{    
    private static final String FORCE_FILE_LIST_IDENIFIER = "\\*Force file list";
    private static final String FILE_LIST_INDENTIFIER = "\\*File list";

    private static final String VERSION = "version";
    private static final String DATA_LENGTH = "data length";

    private NanoscopeVersion version = NanoscopeVersion.getNullInstance();
    private int textHeaderLength = -1;

    public NanoscopeFileList()
    {
        super(FORCE_FILE_LIST_IDENIFIER, FILE_LIST_INDENTIFIER);
    }

    public int getTextHeaderLength()
    {
        return textHeaderLength;
    }

    public NanoscopeVersion getVersion()
    {
        return version;
    }

    @Override
    public void readField(String fieldRaw)
    {
        String field = dropPrefices(fieldRaw);
        String fieldLowerCase = field.toLowerCase(); //Nanoscope fields in the text header have inconsistent case, e.g. the files use Scan Size or Scan size, depending on the file version
        //so we check against field converted to lower case

        if(fieldLowerCase.startsWith(DATA_LENGTH))
        {           
            this.textHeaderLength = parseIntValue(fieldRaw);  
            return;
        }
        else if(fieldLowerCase.startsWith(VERSION))
        {
            String versionString = parseStringValue(fieldRaw);
            this.version = NanoscopeVersion.getInstance(versionString);
            return;
        }
    }
}
