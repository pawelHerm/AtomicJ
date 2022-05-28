package atomicJ.readers.nanoscope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NanoscopeScannerList extends NanoscopeHeaderSection
{    
    private static final String IDENIFIER = "\\*Scanner list";
    private static final String RELATIVE_FIELD_PREFIX = "\\@";

    private final List<String> referenceFields = new ArrayList<>();

    public NanoscopeScannerList()
    {
        super(IDENIFIER);
    }

    //reference fields are the fields which starts with RELATIVE_FIELD_PREFIX
    public List<String> getReferenceFields()
    {
        return Collections.unmodifiableList(referenceFields);
    }

    @Override
    public void readField(String fieldRaw)
    {
        if(fieldRaw.startsWith(RELATIVE_FIELD_PREFIX))
        {           
            referenceFields.add(fieldRaw);  
            return;
        }
    }
}
