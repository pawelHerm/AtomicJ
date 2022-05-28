
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

package atomicJ.gui.boxplots;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.data.QuantitativeSample;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.KeyMapSelectionModel;
import atomicJ.gui.WizardPageModel;
import atomicJ.gui.statistics.ProcessedPackSampleModel;
import atomicJ.utilities.CollectionsUtilities;


import static atomicJ.gui.WizardModelProperties.*;

public class BoxSampleWizardModel <E extends Processed1DPack<E,?>> extends AbstractModel implements PropertyChangeListener
{      
    private final BoxAndWhiskersDestination destination;

    private WizardPageModel currentPageModel;

    private BoxSampleModel<E> boxSampleModel;
    private KeyMapSelectionModel<ProcessedPackFunction<? super E>, String> keySelectionModel;

    private final int modelCount = 2;

    private int currentPageIndex;
    private int currentStep;

    private boolean finishEnabled;
    private boolean backEnabled;
    private boolean nextEnabled;

    public BoxSampleWizardModel(BoxAndWhiskersDestination destination, KeyMapSelectionModel<ProcessedPackFunction<? super E>, String> keySelectionModel, BoxSampleModel<E> boxSamplesModel)
    {
        this.destination = destination;
        setPageModels(keySelectionModel, boxSamplesModel);        
    }

    public void setPageModels(KeyMapSelectionModel<ProcessedPackFunction<? super E>, String> keySelectionModel, BoxSampleModel<E> boxSampleModel)
    {
        setKeySelectionModel(keySelectionModel);
        setSamplesModel(boxSampleModel);

        initDefaults();
    }

    private void initDefaults()
    {
        this.currentPageIndex = 0;
        this.currentStep = 0;    

        WizardPageModel currentPageModelNew = getWizardPageModel(currentPageIndex);       
        setCurrentPageModel(currentPageModelNew);
    }

    private WizardPageModel getWizardPageModel(int index)
    {
        WizardPageModel model = index == 0 ? keySelectionModel : boxSampleModel;

        return model;
    }

    private void setKeySelectionModel(KeyMapSelectionModel<ProcessedPackFunction<? super E>, String> keySelectionModel)
    {
        if(keySelectionModel == null)
        {
            return;
        }

        if(this.keySelectionModel != null)
        {
            this.keySelectionModel.removePropertyChangeListener(this);
        }

        this.keySelectionModel = keySelectionModel; 
        this.keySelectionModel.addPropertyChangeListener(this);
    }

    private void setSamplesModel(BoxSampleModel<E> boxSampleModel)
    {
        if(boxSampleModel == null)
        {
            return;
        }

        if(this.boxSampleModel != null)
        {
            this.boxSampleModel.removePropertyChangeListener(this);
        }

        this.boxSampleModel = boxSampleModel;

        this.backEnabled = boxSampleModel.isBackEnabled();
        this.nextEnabled = boxSampleModel.isNextEnabled();              
        this.finishEnabled = boxSampleModel.isFinishEnabled();

        this.boxSampleModel.addPropertyChangeListener(this);


        initDefaults();
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();
        Object source = evt.getSource();

        boolean currentPageEvent = (source == currentPageModel);

        if(!currentPageEvent)
        {
            return;
        }

        if(BACK_ENABLED.equals(property))
        {
            boolean backEnabledNew = (boolean)evt.getNewValue();
            setBackEnabled(backEnabledNew);
        }
        else if(NEXT_ENABLED.equals(property))
        {
            boolean nextEnabledNew = (boolean)evt.getNewValue();
            setNextEnabled(nextEnabledNew);
        }
        else if(FINISH_ENABLED.equals(property))
        {
            boolean finishEnabledNew = (boolean)evt.getNewValue();
            setFinishEnabled(finishEnabledNew);                   
        }
    }

    public boolean isBackEnabled()
    {
        return backEnabled;
    }

    public void setBackEnabled(boolean enabledNew)
    {
        boolean enabledOld = backEnabled;
        backEnabled = enabledNew;

        firePropertyChange(BACK_ENABLED, enabledOld, enabledNew);
    }


    public boolean isNextEnabled()
    {
        return nextEnabled;
    }

    public void setNextEnabled(boolean enabledNew)
    {
        boolean enabledOld = nextEnabled;
        nextEnabled = enabledNew;

        firePropertyChange(NEXT_ENABLED, enabledOld, enabledNew);
    }

    public boolean isFinishEnabled()
    {
        return finishEnabled;
    }

    public void setFinishEnabled(boolean enabledNew)
    {
        boolean enabledOld = finishEnabled;
        finishEnabled = enabledNew;

        firePropertyChange(FINISH_ENABLED, enabledOld, enabledNew);
    }

    public WizardPageModel getCurrentPage()
    {
        return currentPageModel;
    }

    public int getCurrentPageIndex()
    {
        return currentPageIndex;
    }

    //sprawdzone
    public int next()
    {
        currentPageModel.next();

        currentStep++;
        int currentPageIndex = currentStep > 0 ? 1 : 0;
        setCurrentPage(currentPageIndex);

        return currentPageIndex;
    }

    //sprawdzone
    public int back()
    {
        currentPageModel.back();

        this.currentStep = Math.max(currentStep - 1, 0);
        int currentPageIndex = currentStep > 0 ? 1 : 0;;

        setCurrentPage(currentPageIndex);

        return currentPageIndex;
    }

    public void finish()
    {
        drawAndShowBoxPlots();
    }

    public void cancel()
    {      
    }

    //checked
    private void setCurrentPage(int currentPageIndexNew)
    {
        boolean withinRange = (currentPageIndexNew >= 0)&&(currentPageIndexNew < modelCount);

        if(withinRange && (currentPageIndexNew != currentPageIndex))
        {
            int currentPageIndexOld = this.currentPageIndex;

            WizardPageModel currentPageModelNew = getWizardPageModel(currentPageIndexNew);
            setCurrentPageModel(currentPageModelNew);

            firePropertyChange(WIZARD_PAGE, currentPageIndexOld, currentPageIndexNew);
        }
    }

    private void setCurrentPageModel(WizardPageModel currentPageModelNew)
    {
        this.currentPageModel = currentPageModelNew;

        boolean nextEnabledNew = currentPageModelNew.isNextEnabled();
        boolean finishEnabledNew = currentPageModelNew.isFinishEnabled();
        boolean backEnabledNew = currentPageModelNew.isBackEnabled();

        setFinishEnabled(finishEnabledNew);
        setNextEnabled(nextEnabledNew);
        setBackEnabled(backEnabledNew); 
    }  

    private void drawAndShowBoxPlots()
    {   
        Map<Object, Map<String, QuantitativeSample>> nameQuantitySamples = getNameTypeSamples();
        //quantity (i.e. function of the processed pack) name is the outer key
        Map<String, Map<Object, QuantitativeSample>> quantityNameSamples = CollectionsUtilities.swapNestedHierarchy(nameQuantitySamples);

        String name = "Box and whisker plots";
        File defaultOutputLocation = boxSampleModel.getDefaultOutputLocation();

        destination.publishBoxPlots(defaultOutputLocation, name, name, quantityNameSamples, true);
    }

    //the outer key is the name of the sample, the inner key is the type (i.e. the processed pack function)
    private Map<Object, Map<String, QuantitativeSample>> getNameTypeSamples()
    {
        Map<Object, Map<String, QuantitativeSample>> allQuantitativeSamples = new LinkedHashMap<>();

        List<ProcessedPackSampleModel<E>> sampleModels = boxSampleModel.getWellFormedSampleModels();
        List<String> sampleNames = boxSampleModel.getWellFormedSampleNames();
        Map<String, ProcessedPackFunction<? super E>> processedPackFunctions = keySelectionModel.getIncludedKeyValueMap();

        int sampleCount = sampleModels.size();

        for(int i = 0; i<sampleCount; i++)
        {
            //the key is the name of pack function, the value is calculated per each real sample
            Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

            ProcessedPackSampleModel<E> model = sampleModels.get(i);
            String sampleName = sampleNames.get(i);
            for(Entry<String, ProcessedPackFunction<? super E>> entry : processedPackFunctions.entrySet())
            {            
                String quantityName = entry.getKey();
                ProcessedPackFunction<? super E> f = entry.getValue();

                QuantitativeSample sample = model.getSample(f);
                samples.put(quantityName, sample);
            }

            allQuantitativeSamples.put(sampleName, samples);
        }

        return allQuantitativeSamples;
    }  
}
