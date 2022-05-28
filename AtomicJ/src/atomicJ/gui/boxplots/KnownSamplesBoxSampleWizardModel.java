
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
import java.util.Map;

import atomicJ.data.QuantitativeSample;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.WizardPageModel;
import atomicJ.gui.selection.multiple.SampleSelectionModel;
import atomicJ.utilities.MetaMap;
import static atomicJ.gui.WizardModelProperties.*;

public class KnownSamplesBoxSampleWizardModel extends AbstractModel implements PropertyChangeListener
{      
    private final BoxAndWhiskersDestination destination;

    private SampleSelectionModel keySelectionModel;

    private boolean finishEnabled;

    public KnownSamplesBoxSampleWizardModel(BoxAndWhiskersDestination destination, 
            SampleSelectionModel keySelectionModel)
    {
        this.destination = destination;
        this.keySelectionModel = keySelectionModel;
        setKeySelectionModel(keySelectionModel);

        boolean finishEnabledNew = keySelectionModel.isFinishEnabled();
        setFinishEnabled(finishEnabledNew);
    }

    public void setKeySelectionModel(SampleSelectionModel keySelectionModel)
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();
        Object source = evt.getSource();

        boolean currentPageEvent = (source == keySelectionModel);

        if(!currentPageEvent)
        {
            return;
        }

        if(FINISH_ENABLED.equals(property))
        {
            boolean finishEnabledNew = (boolean)evt.getNewValue();
            setFinishEnabled(finishEnabledNew);                   
        }
    }

    public boolean isBackEnabled()
    {
        return false;
    }

    public boolean isNextEnabled()
    {
        return false;
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
        return keySelectionModel;
    }

    public void next()
    {}

    public void back()
    {}

    public void finish()
    {
        drawAndShowBoxPlots();
    }

    public void cancel()
    {}

    private void drawAndShowBoxPlots()
    {   
        Map<String, Map<String, QuantitativeSample>> includedSamples = keySelectionModel.getIncludedSamples();

        String name = "Box and whisker plots";
        File defaultOutputLocation = keySelectionModel.getDefaultOutputLocation();

        //ugly hack, but works
        MetaMap<String, Object, QuantitativeSample> m = new MetaMap<>();
        m.putAll(includedSamples);

        destination.publishBoxPlots(defaultOutputLocation, name, name, m.getMapCopy(), true);
    }
}
