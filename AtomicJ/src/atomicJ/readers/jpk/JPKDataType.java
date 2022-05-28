package atomicJ.readers.jpk;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum JPKDataType 
{
    FLOAT(Collections.unmodifiableList(Arrays.asList("float-data","float")),false),INTEGER(Collections.unmodifiableList(Arrays.asList("integer-data","memory-integer-data")),true),
    SHORT(Collections.unmodifiableList(Arrays.asList("short-data","short","memory-short-data")),true);

    private final boolean requiresEncoder;
    private final List<String> keys;

    JPKDataType(List<String> keys, boolean requiresEncoder)
    {
        this.requiresEncoder = requiresEncoder;
        this.keys = keys;
    }

    public boolean isEncoderRequired()
    {
        return requiresEncoder;
    }

    public static JPKDataType getDataType(String key) 
    {
        for (JPKDataType type : values()) 
        {
            if (type.keys.contains(key)) 
            {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid data type: " + key);
    }
}
