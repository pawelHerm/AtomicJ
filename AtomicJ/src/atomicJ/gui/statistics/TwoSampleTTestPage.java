
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

import static atomicJ.gui.WizardModelProperties.FINISH_ENABLED;
import static atomicJ.gui.WizardModelProperties.WIZARD_PAGE;
import static atomicJ.gui.statistics.InferenceModelProperties.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import javax.swing.JPanel;

import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.gui.UserCommunicableException;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.statistics.TwoSampleTTest;


public class TwoSampleTTestPage <E extends Processed1DPack<E,?>> extends StatisticalTestPage implements PropertyChangeListener
{
    private static final String NAME = "t-test for differences in sample means";
    private static final String TASK = "Specify parameters of the two - sample t-test";

    private final TwoSampleTTestModel<E> pageModel;
    private final TwoSampleTTestView<E> pageView;
    private final RichWizardPage previousPage;
    private SimpleWizard wizard;

    public TwoSampleTTestPage(Collection<ProcessedPackFunction<? super E>> availableFunctions, List<Batch<E>> availableData, RichWizardPage previousPage)
    {
        pageModel = new TwoSampleTTestModel<>(availableFunctions, availableData);
        pageView = new TwoSampleTTestView<>(pageModel);
        pageModel.addPropertyChangeListener(this);
        this.previousPage = previousPage;
    }

    @Override
    public void setWizard(SimpleWizard wizard)
    {
        this.wizard = wizard;
        pageView.setWizard(wizard);
    }

    protected TwoSampleTTestModel<E> getPageModel()
    {
        return pageModel;
    }

    @Override
    public JPanel getPageView()
    {
        return pageView;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public String getName() 
    {
        return NAME;
    }

    @Override
    public String getTaskDescription() 
    {
        return TASK;
    }

    @Override
    public void back() 
    {       
        firePropertyChange(WIZARD_PAGE, this, previousPage);
    }

    @Override
    public void next() 
    {       
    }

    @Override
    public void skip() 
    {       
    }

    @Override
    public void finish() 
    {
        try 
        {
            TwoSampleTTest test = pageModel.run();
            File outputDirectory = pageModel.getDefaultOutputDirectory();
            DescriptiveStatistics stats1 = test.getFirstSampleStatistics();
            DescriptiveStatistics stats2 = test.getSecondSampleStatistics();

            Map<String, Object[]> sampleData = new LinkedHashMap<>();
            Map<String, Object> testData = new LinkedHashMap<>();

            sampleData.put("Name", new Object[] {stats1.getSampleName(), stats2.getSampleName()});
            sampleData.put("Size", new Object[] {stats1.getSize(), stats2.getSize()});
            sampleData.put("Mean", new Object[] {stats1.getArithmeticMean(), stats2.getArithmeticMean()});
            sampleData.put("Variance", new Object[] {stats1.getVariance(), stats2.getVariance()});

            testData.put("Test", test.getName());
            testData.put("Two - tailed", test.isTwoTailed());
            testData.put("Equal variances", test.isVariancesAssuemdEqual());
            testData.put("Significance level", test.getSignificanceLevel());
            testData.put("Difference of means", test.getMeanDifference());
            testData.put("Confidence interval min", test.getConfidanceIntervalLowerLimit());
            testData.put("Confidence interval max", test.getConfidanceIntervalUpperLimit());
            testData.put("P value", test.getPValue());
            testData.put("Difference significant", test.isNullHypothesisRejected());        

            InferencesTableModel tableModel = new InferencesTableModel(sampleData, testData, 2, outputDirectory);
            InferencesTable table = new InferencesTable(tableModel);

            InferenceTableDialog dialog = new InferenceTableDialog(wizard.getOwner(), table);
            dialog.setVisible(true);

            wizard.setVisible(false);
        } 
        catch (UserCommunicableException e) 
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isBackEnabled() 
    {
        return true;
    }

    @Override
    public boolean isNextEnabled() 
    {
        return false;
    }

    @Override
    public boolean isSkipEnabled() 
    {
        return false;
    }

    @Override
    public boolean isFinishEnabled() 
    {
        return pageModel.isInputProvided();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if(INPUT_PROVIDED.equals(name))
        {
            firePropertyChange(FINISH_ENABLED, evt.getOldValue(), evt.getNewValue());
        }
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
