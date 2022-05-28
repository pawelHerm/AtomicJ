package atomicJ.readers.asylum;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import atomicJ.utilities.FileInputUtilities;

public class IgorWaveNote
{
    private static final String SCAN_SIZE_KEY = "ScanSize";

    private final int size; //size in bytes
    private final String text;
    private final Map<String, String> parsed;

    public IgorWaveNote(ByteBuffer buffer, int size)
    {
        this.size = size;

        char[] waveNoteChars = new char[size];
        FileInputUtilities.populateCharArrayWithBytes(waveNoteChars, buffer);
        this.text = FileInputUtilities.convertBytesToString(waveNoteChars);

        this.parsed = parseText();             
    }

    private Map<String, String> parseText()
    {
        String[] lines = text.split("[\\r\\n]+");   

        Map<String, String> keyValueMap = new LinkedHashMap<>();

        for(String line : lines)
        {
            int delimiterIndex = line.indexOf(':');

            if(delimiterIndex > -1)
            {
                String key = line.substring(0, delimiterIndex).trim();
                String value = line.substring(delimiterIndex + 1, line.length()).trim();

                keyValueMap.put(key, value);
            }
        }

        return keyValueMap;
    }

    public boolean isEmpty()
    {
        return (size == 0);
    }

    public int getByteSize()
    {
        return size;
    }

    public String getText()
    {
        return text;
    }

    public boolean containsKey(String key)
    {
        return parsed.containsKey(key);
    }

    public String getValue(String key)
    {
        return parsed.get(key);
    }
}