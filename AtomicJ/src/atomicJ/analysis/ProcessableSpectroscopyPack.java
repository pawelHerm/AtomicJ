
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

import atomicJ.sources.IdentityTag;
import atomicJ.sources.SimpleSpectroscopySource;

public final class ProcessableSpectroscopyPack 
{
    private final SimpleSpectroscopySource sourceToProcess;
    private final ProcessingSettings processSettings;
    private final VisualizationSettings visualizationSettings;
    private final MapProcessingSettings mapProcessingSettings;

    private final IdentityTag batch;

    private ContactEstimator contactEstimator;
    private ForceEventEstimator adhesionForceEstimator;
    private ForceEventEstimator jumpEstimator;

    public ProcessableSpectroscopyPack(SimpleSpectroscopySource sourcesToProcess, ProcessingSettings settings,
            MapProcessingSettings mapProcessingSettings, VisualizationSettings visSettings, IdentityTag batch)
    {
        this.sourceToProcess = sourcesToProcess;
        this.processSettings = settings;
        this.mapProcessingSettings= mapProcessingSettings;
        this.visualizationSettings = visSettings;
        this.batch = batch;
    }

    public ContactEstimator getContactEstimator()
    {
        return contactEstimator;
    }

    public void setContactEstimator(ContactEstimator estimator)
    {
        this.contactEstimator = estimator;
    }

    public ForceEventEstimator getAdhesionForceEstimator()
    {
        return adhesionForceEstimator;
    }

    public void setAdhesionForceEstimator(ForceEventEstimator adhesionForceEstimator)
    {
        this.adhesionForceEstimator = adhesionForceEstimator;
    }

    public ForceEventEstimator getJumpEstimator()
    {
        return jumpEstimator;
    }

    public void setJumpEstimator(ForceEventEstimator jumpEstimator)
    {
        this.jumpEstimator = jumpEstimator;
    }

    public SimpleSpectroscopySource getSourceToProcess()
    {
        return sourceToProcess;
    }

    public ProcessingSettings getProcessingSettings()
    {
        return processSettings;
    }

    public MapProcessingSettings getMapSettings()
    {
        return mapProcessingSettings;
    }

    public VisualizationSettings getVisualizationSettings()
    {
        return visualizationSettings;
    }

    public IdentityTag getBatchIdentityTag()
    {
        return batch;
    }
}
