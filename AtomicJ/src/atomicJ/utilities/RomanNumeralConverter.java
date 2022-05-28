
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
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

package atomicJ.utilities;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class RomanNumeralConverter 
{
    private static Map<String, Integer> valueMap = new LinkedHashMap<>();

    static {
        valueMap.put("M",1000);valueMap.put("CM",900);
        valueMap.put("D",500);valueMap.put("CD",400);
        valueMap.put("C",100);valueMap.put("XC",90);
        valueMap.put("L",50);valueMap.put("XL",40);
        valueMap.put("X",10);valueMap.put("IX",9);
        valueMap.put("V",5);valueMap.put("IV",4);
        valueMap.put("I", 1);
    }

    public static String convertToRoman(int arabic) {

        if( arabic <= 0) {
            throw new IllegalArgumentException();
        }

        StringBuilder builder = new StringBuilder();

        for (Entry<String, Integer> entry : valueMap.entrySet())
        {
            double value = entry.getValue();

            while (arabic >= value) 
            {
                String digit = entry.getKey();
                builder.append(digit);
                arabic -= value;
            }
        }
        return builder.toString();
    }
}
