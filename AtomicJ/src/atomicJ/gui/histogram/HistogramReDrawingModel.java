
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

package atomicJ.gui.histogram;

import static atomicJ.gui.histogram.HistogramModelProperties.*;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.AbstractWizardPageModel;
import atomicJ.gui.WizardModelProperties;
import atomicJ.gui.selection.multiple.SampleSelectionModel;



public class HistogramReDrawingModel extends AbstractModel implements PropertyChangeListener
{
    private boolean backEnabled;
    private boolean nextEnabled;
    private boolean skipEnabled;
    private boolean finishEnabled;

    private String task;
    private final SampleSelectionModel selectionModel;
    private HistogramMultiSampleBinningModel binningModel;
    private AbstractWizardPageModel currentPageModel;

    private final HistogramDestination destination;
    private final List<SampleCollection> sampleCollections;

    public HistogramReDrawingModel(HistogramDestination destination, List<SampleCollection> sampleCollections)
    {
        this.destination = destination;
        this.sampleCollections = sampleCollections;
        this.selectionModel = new SampleSelectionModel(sampleCollections,  "<html>Which datasets would you like to plot?</html>", true, false);
        setCurrentPageModel(selectionModel);
    }

    public SampleSelectionModel getSelectionModel()
    {
        return selectionModel;
    }

    public HistogramMultiSampleBinningModel getBinningModel()
    {
        return binningModel;
    }

    public Window getPublicationSite()
    {
        return destination.getHistogramPublicationSite();
    }

    public String getTask()
    {
        return task;
    }

    public boolean isBackEnabled()
    {
        return backEnabled;
    }

    private void setBackEnabled(boolean enabledNew)
    {
        boolean enabledOld = backEnabled;
        this.backEnabled = enabledNew;

        firePropertyChange(WizardModelProperties.BACK_ENABLED, enabledOld, enabledNew);
    }

    public boolean isNextEnabled()
    {
        return nextEnabled;
    }

    private void setNextEnabled(boolean enabledNew)
    {
        boolean enabledOld = nextEnabled;
        this.nextEnabled = enabledNew;

        firePropertyChange(WizardModelProperties.NEXT_ENABLED, enabledOld, enabledNew);
    }

    public boolean isSkipEnabled()
    {
        return skipEnabled;
    }

    private void setSkipEnabled(boolean enabledNew)
    {
        boolean enabledOld = skipEnabled;
        this.skipEnabled = enabledNew;

        firePropertyChange(WizardModelProperties.SKIP_ENABLED, enabledOld, enabledNew);
    }

    public boolean isFinishEnabled()
    {
        return finishEnabled;
    }

    private void setFinishEnabled(boolean enabledNew)
    {
        boolean enabledOld = finishEnabled;
        this.finishEnabled = enabledNew;

        firePropertyChange(WizardModelProperties.FINISH_ENABLED, enabledOld, enabledNew);
    }

    public void back()
    {
        if(backEnabled)
        {
            currentPageModel.back();
        }
    }

    public void next()
    {
        if(nextEnabled)
        {
            currentPageModel.next();
        }
    }

    public void skip()
    {
        if(skipEnabled)
        {
            currentPageModel.skip();
        }
    }

    public void finish()
    {
        if(finishEnabled)
        {
            currentPageModel.finish();
        }
    }

    private void setCurrentPageModel(AbstractWizardPageModel pageModel)
    {
        if(currentPageModel != null)
        {
            currentPageModel.removePropertyChangeListener(this);
        }
        this.currentPageModel = pageModel;
        currentPageModel.addPropertyChangeListener(this);
        pullModelProperties();	
    }

    private void pullModelProperties()
    {
        boolean backEnabledNew = currentPageModel.isBackEnabled();
        setBackEnabled(backEnabledNew);
        boolean nextEnabledNew = currentPageModel.isNextEnabled();
        setNextEnabled(nextEnabledNew);
        boolean skipEnabledNew = currentPageModel.isSkipEnabled();
        setSkipEnabled(skipEnabledNew);
        boolean finishEnabledNew = currentPageModel.isFinishEnabled();
        setFinishEnabled(finishEnabledNew);
        String taskNew = currentPageModel.getTaskDescription();
        setTask(taskNew);
    }

    private void setTask(String taskNew)
    {
        String taskOld = task;
        this.task = taskNew;

        firePropertyChange(WizardModelProperties.TASK, taskOld, taskNew);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {

        String name = evt.getPropertyName();		

        if(name.equals(WizardModelProperties.BACK_ENABLED))
        {
            boolean newVal = (boolean)evt.getNewValue();	
            setBackEnabled(newVal);
        }
        else if(name.equals(WizardModelProperties.NEXT_ENABLED))
        {
            boolean newVal = (boolean)evt.getNewValue();	
            setNextEnabled(newVal);			
        }
        else if(name.equals(WizardModelProperties.SKIP_ENABLED))
        {
            boolean newVal = (boolean)evt.getNewValue();	
            setSkipEnabled(newVal);			
        }
        else if(name.equals(WizardModelProperties.FINISH_ENABLED))
        {
            boolean newVal = (boolean)evt.getNewValue();
            setFinishEnabled(newVal);
        }	
        else if(name.equals(WizardModelProperties.TASK))
        {
            String newVal = (String)evt.getNewValue();
            setTask(newVal);
        }
        else if(name.equals(SELECTION_FINISHED))
        {
            HistogramMultiSampleBinningModel binningModel = buildMainModel();
            this.binningModel = binningModel;
            setCurrentPageModel(binningModel);
            firePropertyChange(evt);
        }
    }

    private HistogramMultiSampleBinningModel buildMainModel()
    {
        Map<HistogramResourceIdentifier,List<HistogramSampleModel>> sampleModelsMap =  new LinkedHashMap<>();

        for(SampleCollection collection: sampleCollections)
        {
            Map<String, QuantitativeSample> includedSamples = collection.getIncludedSamples(2);
            List<HistogramSampleModel> histogramModels = new ArrayList<>();

            if(includedSamples.isEmpty())
            {
                continue;
            }

            for(Entry<String, QuantitativeSample> entry: includedSamples.entrySet())
            {
                String identifier = entry.getKey();
                QuantitativeSample sample = entry.getValue();

                HistogramSampleModel model = new HistogramSampleModel(identifier, sample);
                histogramModels.add(model);
            }

            String shortName = collection.getShortName();
            String longName = collection.getName();
            File defaultOutputDirectory = collection.getDefaultOutputDirectory();

            HistogramResourceIdentifier histogramResourceIdentifier = new HistogramResourceIdentifier(shortName, longName, defaultOutputDirectory);

            sampleModelsMap.put(histogramResourceIdentifier, histogramModels);
        }

        HistogramMultiSampleBinningModel mainModel = new HistogramMultiSampleBinningModel(destination, sampleModelsMap, false, true);
        return mainModel;
    }
}
