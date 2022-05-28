
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

package atomicJ.gui.statistics;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedPackFunction;



import static atomicJ.gui.WizardModelProperties.*;
import static atomicJ.gui.statistics.InferenceModelProperties.*;

public class InferenceSelectionPage extends RichWizardPage implements PropertyChangeListener
{   
    private static final String NAME = "Test selection";
    private static final String TASK = "Select the test you wish to perform";

    private final Map<String, StatisticalTestPage> tests = new LinkedHashMap<>();

    private final InferenceSelectionPageView selectionPage = new InferenceSelectionPageView();
    private StatisticalTestPage currentTest;

    public <E extends Processed1DPack<E,?>> InferenceSelectionPage(Collection<ProcessedPackFunction<? super E>> availableFunctions, List<Batch<E>> availableData)
    {
        this.currentTest = new OneSampleTTestPage<>(availableFunctions, availableData, this);
        tests.put(currentTest.getName(), currentTest);

        StatisticalTestPage secondTest = new TwoSampleTTestPage<>(availableFunctions, availableData,this);
        tests.put(secondTest.getName(), secondTest);
        selectionPage.setPageModel(this);
    }

    @Override
    public void setWizard(SimpleWizard wizard)
    {
        currentTest.setWizard(wizard);
    }

    public Map<String, StatisticalTestPage> getTests()
    {
        return tests;
    }

    public StatisticalTestPage getCurrentTestPage()
    {
        return currentTest;
    }

    public List<StatisticalTestPage> getAllTestPages()
    {
        return new ArrayList<>( tests.values());
    }

    public boolean setTest(String currentTestNewName)
    {
        if(tests.containsKey(currentTestNewName))
        {
            StatisticalTestPage currentTestOld = currentTest;
            this.currentTest = tests.get(currentTestNewName);

            currentTestOld.removePropertyChangeListener(this);
            currentTest.addPropertyChangeListener(this);

            firePropertyChange(SELECTED_TEST, currentTestOld.getName(), currentTest.getName());
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();
        if(INPUT_PROVIDED.equals(name))
        {
        }
        else
        {
            firePropertyChange(evt);
        }
    }

    @Override
    public String getTaskDescription() 
    {
        return TASK;
    }

    @Override
    public void back() 
    {       
    }

    @Override
    public void next() 
    {       
        firePropertyChange(WIZARD_PAGE, this, currentTest);
    }

    @Override
    public void skip() 
    {       
    }

    @Override
    public void finish() 
    {       
    }

    @Override
    public boolean isBackEnabled() 
    {
        return false;
    }

    @Override
    public boolean isNextEnabled() 
    {
        return true;
    }

    @Override
    public boolean isSkipEnabled() 
    {
        return false;
    }

    @Override
    public boolean isFinishEnabled() 
    {
        return false;
    }

    @Override
    public String getName() 
    {
        return NAME;
    }

    @Override
    public JPanel getPageView() 
    {
        return selectionPage;
    }

    @Override
    public String getTaskName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isFirst() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isLast() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNecessaryInputProvided() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void cancel() {
        // TODO Auto-generated method stub

    }
}
