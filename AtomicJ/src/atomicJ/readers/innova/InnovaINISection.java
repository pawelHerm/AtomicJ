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


package atomicJ.readers.innova;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InnovaINISection
{
    private static final String KEY_OR_VALUE = "[^\\n\\r]*";
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("\\s*(" + KEY_OR_VALUE +")\\s*=\\s*("+ KEY_OR_VALUE + ")\\s*");
    static final Pattern INIT_SECTION_PATTERN = Pattern.compile("(?ms)^\\[([^]\r\n]+)]((?:(?!^\\[[^]\r\n]+]).)*)");

    private final String name;
    private final String text;
    private final Map<String, String> keyValuePairs; 

    private InnovaINISection(String name, Map<String, String> keyValuePairs, String text)
    {
        this.name = name;
        this.keyValuePairs = keyValuePairs;
        this.text = text;
    }

    public String getName()
    {
        return name;
    }

    public String getText()
    {
        return text;
    }

    public Map<String, String> getKeyValuePairs()
    {
        return Collections.unmodifiableMap(keyValuePairs);
    }

    public static InnovaINISection build(String wholeSection)
    {
        Matcher matcherSection = InnovaINISection.INIT_SECTION_PATTERN.matcher(wholeSection);
        matcherSection.matches();

        String sectionName = matcherSection.group(1);
        String sectionBody = matcherSection.group(2);

        Map<String, String> keyValuePairs = new LinkedHashMap<>();
        String text = "";

        try (Scanner scanner = new Scanner(sectionBody))
        {
            scanner.useDelimiter(Pattern.compile("[\\r\\n]+"));

            while (scanner.hasNext(KEY_VALUE_PATTERN) || scanner.hasNext("\\s*")) 
            {
                String line = scanner.nextLine();
                Matcher matcherLine = KEY_VALUE_PATTERN.matcher(line);

                if(matcherLine.matches())
                {
                    keyValuePairs.put(matcherLine.group(1), matcherLine.group(2));
                }
            }

            //can't be lower case "z", see http://stackoverflow.com/questions/22350037/behavior-of-using-z-vs-z-as-scanner-delimiter
            text = scanner.useDelimiter("\\Z").next();
        }

        return new InnovaINISection(sectionName, keyValuePairs,text);
    }
}