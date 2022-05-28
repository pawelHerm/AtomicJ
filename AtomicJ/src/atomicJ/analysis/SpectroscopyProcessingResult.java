
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

package atomicJ.analysis;

public final class SpectroscopyProcessingResult 
{
    private final ProcessedSpectroscopyPack processed;
    private final VisualizableSpectroscopyPack visualizable;
    private final MapProcessingSettings mapSettings;

    public SpectroscopyProcessingResult(ProcessedSpectroscopyPack processed, VisualizableSpectroscopyPack visualizable, MapProcessingSettings mapSettings)
    {
        this.processed = processed;
        this.visualizable = visualizable;
        this.mapSettings = mapSettings;
    }

    public ProcessedSpectroscopyPack getProcessedPack()
    {
        return processed;
    }

    public VisualizableSpectroscopyPack getVisualizablePack()
    {
        return visualizable;
    }

    public ProcessingSettings getProcessingSettings()
    {
        return processed.getProcessingSettings();
    }

    public VisualizationSettings getVisualizationSettings()
    {
        return visualizable.getVisualizationSettings();
    }

    public MapProcessingSettings getMapSettings()
    {
        return mapSettings;
    }
}
