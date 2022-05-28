/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2015 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/


package atomicJ.readers.nanosurf;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class INISection
{
    private static final String KEY_OR_VALUE = "[^\\n\\r]*|\\([^\\)]*\\)|\"[^\"]*\"";
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("\\s*(" + KEY_OR_VALUE +")\\s*=\\s*("+ KEY_OR_VALUE + ")\\s*");;
    static final Pattern INIT_SECTION_PATTERN = Pattern.compile("(?ms)^\\[([^]\r\n]+)]((?:(?!^\\[[^]\r\n]+]).)*)");

    private final String name;
    private final Map<String, String> keyValuePairs; 

    private INISection(String name, Map<String, String> keyValuePairs)
    {
        this.name = name;
        this.keyValuePairs = keyValuePairs;
    }

    public String getName()
    {
        return name;
    }

    public Map<String, String> getKeyValuePairs()
    {
        return Collections.unmodifiableMap(keyValuePairs);
    }

    public static INISection build(String wholeSection)
    {
        Matcher matcher = INISection.INIT_SECTION_PATTERN.matcher(wholeSection);
        matcher.matches();

        String sectionName = matcher.group(1);
        String sectionBody = matcher.group(2);

        return new INISection(sectionName, extractKeyValuePairs(sectionBody));
    }

    private static Map<String, String> extractKeyValuePairs(String sectionBody)
    {
        Map<String, String> keyValuePairs = new LinkedHashMap<>();
        String[] lines = sectionBody.split("[\\r\\n]+");
        for(String line : lines)
        {            
            Matcher matcher = KEY_VALUE_PATTERN.matcher(line);

            if(matcher.matches())
            {
                keyValuePairs.put(matcher.group(1), matcher.group(2));
            }
        }

        return keyValuePairs;
    }   
}