
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

package atomicJ.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import atomicJ.data.SampleCollection;



public class SimpleResource extends AbstractResource implements DataModelResource
{
    private final List<SampleCollection> sampleCollections = new ArrayList<>();


    public SimpleResource(List<SampleCollection> sampleCollections, String shortName, String longName, File outputLocation)
    {
        super(outputLocation, shortName, longName);
        this.sampleCollections.addAll(sampleCollections);
    }

    @Override
    public List<SampleCollection> getSampleCollections() 
    {
        return sampleCollections;
    }
}
