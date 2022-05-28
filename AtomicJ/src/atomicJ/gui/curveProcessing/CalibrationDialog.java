
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

package atomicJ.gui.curveProcessing;

import static atomicJ.gui.PreferenceKeys.*;
import static atomicJ.gui.curveProcessing.CalibrationModel.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import javax.swing.*;

import org.jfree.data.Range;

import atomicJ.analysis.ForceCurveBranch;
import atomicJ.analysis.PhotodiodeSignalType;
import atomicJ.data.Channel1D;
import atomicJ.data.SpectroscopyCurve;
import atomicJ.gui.ItemList;
import atomicJ.gui.RawCurvePlotFactory;
import atomicJ.gui.SourceFileChooser;
import atomicJ.gui.SourceListCellRenderer;
import atomicJ.gui.SubPanel;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.experimental.ExperimentalWizard;
import atomicJ.readers.ConcurrentReadingTask;
import atomicJ.readers.SpectroscopyReadingModel;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.utilities.GUIUtilities;
import atomicJ.sources.ChannelSource;


public class CalibrationDialog extends JDialog implements PropertyChangeListener, CalibrationSupervisor
{
    private static final long serialVersionUID = 1L;

    private static final Preferences PREF = Preferences.userRoot().node(CalibrationDialog.class.getName());

    private static final int DEFAULT_HEIGHT = (int)Math.round(0.4*Toolkit.getDefaultToolkit().getScreenSize().height);
    private static final int DEFAULT_WIDTH = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/2);

    private static final int DEFAULT_LOCATION_X = Math.round((Toolkit.getDefaultToolkit().getScreenSize().width - DEFAULT_WIDTH)/2);
    private static final int DEFAULT_LOCATION_Y = Math.round((Toolkit.getDefaultToolkit().getScreenSize().height - DEFAULT_HEIGHT)/2);

    private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);

    private final ApplyAction applyAction = new ApplyAction();
    private final SkipAction nextAction = new SkipAction();
    private final ClearAction clearAction = new ClearAction();
    private final CancelAction cancelAction = new CancelAction();
    private final FinishAction finishAction = new FinishAction();

    private final FilterSourcesAction substrateSelectionAction = new FilterSourcesAction();

    private final ItemList<SimpleSpectroscopySource> sourceList = new ItemList<>();

    private final JLabel labelLastResult = new JLabel("Current");
    private final JLabel labelMean = new JLabel("Mean");
    private final JLabel labelCount = new JLabel("Count");

    private final JLabel fieldLastResult = new JLabel();
    private final JLabel fieldMean = new JLabel();
    private final JLabel fieldCount = new JLabel();
    private final JComboBox<ForceCurveBranch> comboCurveBranch = new JComboBox<>(ForceCurveBranch.values());

    private final CalibrationPanel calibrationPanel = new CalibrationPanel();

    private final ExperimentalWizard substrateSelectionWizard;

    private ProcessingModel processingModel;
    private final SourceFileChooser<SimpleSpectroscopySource> chooser = new SourceFileChooser<>(SpectroscopyReadingModel.getInstance(),PREF);

    private final CalibrationModel calibrationModel = new CalibrationModel();

    public CalibrationDialog(ProcessingModel processingModel)
    {
        super(processingModel.getPublicationSite(), "Calibration", ModalityType.MODELESS);	
        calibrationModel.addPropertyChangeListener(this);

        calibrationPanel.setCalibrationSupervisor(this);

        pullModelProperties();
        setModel(processingModel);		

        this.substrateSelectionWizard = new CalibrationSelectionWizard("Calibration substrate assistant", processingModel.getPreviewDestination(), calibrationModel);

        numberFormat.setMaximumFractionDigits(4);
        numberFormat.setMinimumFractionDigits(4);

        Container content = getContentPane();
        content.setLayout(new BorderLayout());

        SubPanel sourcePanel = buildActiveSourcePanel();
        JPanel mainPanel = buildMainPanel();
        JPanel buttonPanel = buildButtonPanel();

        content.add(mainPanel, BorderLayout.CENTER);
        content.add(sourcePanel, BorderLayout.NORTH);		
        content.add(buttonPanel,BorderLayout.SOUTH);

        int height = PREF.getInt(WINDOW_HEIGHT,DEFAULT_HEIGHT);
        int width = PREF.getInt(WINDOW_WIDTH,DEFAULT_WIDTH);
        int locationX = PREF.getInt(WINDOW_LOCATION_X, DEFAULT_LOCATION_X);
        int locationY = PREF.getInt(WINDOW_LOCATION_Y, DEFAULT_LOCATION_Y);

        if(GUIUtilities.areWindowSizeAndLocationWellSpecified(width, height, locationX, locationY))
        { 
            setSize(width,height);
            setLocation(locationX,locationY);
        }
        else
        {
            setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            setLocation(DEFAULT_LOCATION_X, DEFAULT_LOCATION_Y);
        }

        initComponentListener();
        initItemListener();

        setResultsVisible(false);
    }

    private void initComponentListener()
    {
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent evt)
            {
                calibrationModel.clear();

                PREF.putInt(WINDOW_HEIGHT, getHeight());
                PREF.putInt(WINDOW_WIDTH, getWidth());
                PREF.putInt(WINDOW_LOCATION_X, (int) getLocation().getX());         
                PREF.putInt(WINDOW_LOCATION_Y, (int) getLocation().getY());
            }
        });
    }

    private void initItemListener()
    {
        comboCurveBranch.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                calibrationModel.setForceCurveBranch((ForceCurveBranch) comboCurveBranch.getSelectedItem());
            }
        });
    }

    private void setResultsVisible(boolean visible)
    {        
        labelLastResult.setVisible(visible);
        labelCount.setVisible(visible);
        labelMean.setVisible(visible);

        fieldCount.setVisible(visible);
        fieldMean.setVisible(visible);
        fieldLastResult.setVisible(visible);
    }


    private void pullModelProperties()
    {
        List<SimpleSpectroscopySource> sources = calibrationModel.getSources();
        SimpleSpectroscopySource currentSource = calibrationModel.getCurrentSource();

        boolean applyEnabled = calibrationModel.isApplyEnabled();
        boolean nextEnabled = calibrationModel.isNextEnabled();
        boolean finishEnabled = calibrationModel.isFinishEnabled();
        boolean clearEnabled = calibrationModel.isClearEnabled();
        boolean substrateSelectionEnabled = calibrationModel.isSubstrateSelectionEnabled();

        double meanSensitivity = calibrationModel.getMeanSensitivity();
        double currentSensitivity = calibrationModel.getCurrentSensitivity();
        int sensitivityCount = calibrationModel.getSensitivityMeansurementCount();

        setSources(sources);
        selectSource(currentSource);

        applyAction.setEnabled(applyEnabled);
        nextAction.setEnabled(nextEnabled);
        finishAction.setEnabled(finishEnabled);
        clearAction.setEnabled(clearEnabled);
        substrateSelectionAction.setEnabled(substrateSelectionEnabled);

        String meanSensitivyText = Double.isNaN(meanSensitivity)? "" : numberFormat.format(meanSensitivity);
        String lastSensitivyText = Double.isNaN(currentSensitivity)? "" : numberFormat.format(currentSensitivity);
        String sensitivityCountText = Integer.toString(sensitivityCount);

        ForceCurveBranch forceCurveBranch = calibrationModel.getForceCurveBranch();
        Set<ForceCurveBranch> availableForceCurveBranches = calibrationModel.getAvailableBranches();

        fieldMean.setText(meanSensitivyText);
        fieldLastResult.setText(lastSensitivyText);
        fieldCount.setText(sensitivityCountText);

        comboCurveBranch.setModel(new DefaultComboBoxModel<>(availableForceCurveBranches.toArray(new ForceCurveBranch[] {})));
        comboCurveBranch.setSelectedItem(forceCurveBranch);
    }

    public void setModel(ProcessingModel model)
    {
        this.processingModel = model;
    }

    public void setChooserSelectedFile(File file)
    {
        chooser.setSelectedFile(file);
    }

    public void selectSource(SimpleSpectroscopySource source)
    {		
        if(source != null)
        {
            try {
                SpectroscopyCurve<Channel1D> afmCurve = source.getRecordedPhotodiodeCurve(Double.NaN, Double.NaN);
                CalibrationChart selectedChart = calibrationPanel.getSelectedChart();

                if(selectedChart == null)
                {
                    CalibrationChart chartNew = new CalibrationChart(RawCurvePlotFactory.getInstance().getPlot(afmCurve));
                    calibrationPanel.setChart(chartNew);   
                }
                else
                {
                    selectedChart.setDataTo(afmCurve.getChannels());
                }

                sourceList.setSelectedValue(source,true);

                setResultsVisible(true);
            } catch (UserCommunicableException e) 
            {
                e.printStackTrace();
            }

        }
        else
        {
            calibrationPanel.clear();			
            sourceList.clearSelection();
            //            setResultsVisible(false);
        }
    }

    public void setSources(List<SimpleSpectroscopySource> sources)
    {
        sourceList.setItems(sources);
        revalidate();
    }

    private SubPanel buildActiveSourcePanel()
    {	
        SubPanel sourcePanel = new SubPanel();

        sourceList.setEnabled(false);
        sourceList.setCellRenderer(new BoldSourceListCellRenderer(true));
        sourceList.setBackground(UIManager.getColor("Label.background"));			
        sourceList.setVisibleRowCount(4);

        JScrollPane scrollPane  = new JScrollPane(sourceList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);		
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel controlPanel = buildControlPanel();

        sourcePanel.addComponent(controlPanel, 0,0,1,1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 1, new Insets(3,10,3,10));
        sourcePanel.addComponent(scrollPane, 1,0,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);

        sourcePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(25, 8, 8, 8),"Sources")));	

        return sourcePanel;
    }

    private JPanel buildControlPanel()
    {
        JPanel panelControl = new JPanel(); 

        JButton buttonAddSources = new JButton(new AddSourceAction());
        JButton buttonSubstrate = new JButton(substrateSelectionAction);
        JButton buttonClear = new JButton(clearAction);

        GroupLayout layout = new GroupLayout(panelControl);
        layout.setHonorsVisibility(true);
        panelControl.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)

                .addComponent(buttonAddSources).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSubstrate).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonClear)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        layout.setHorizontalGroup(layout.createParallelGroup()

                .addComponent(buttonAddSources)
                .addComponent(buttonSubstrate)
                .addComponent(buttonClear));

        layout.linkSize(buttonAddSources, buttonSubstrate, buttonClear);

        return panelControl;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        JButton buttonAdd = new JButton(applyAction);
        JButton buttonNext = new JButton(nextAction);
        JButton buttonCancel = new JButton(cancelAction);
        JButton buttonFinish = new JButton(finishAction);

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonAdd).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonNext).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonFinish).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 30, GroupLayout.PREFERRED_SIZE)
                .addComponent(buttonCancel).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonAdd)
                .addComponent(buttonNext)
                .addComponent(buttonFinish)
                .addComponent(buttonCancel));

        layout.linkSize(buttonAdd, buttonNext, buttonFinish, buttonCancel);

        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
    }

    private JPanel buildMainPanel()
    {		
        JPanel mainPanel = new JPanel();

        mainPanel.setLayout(new BorderLayout());
        calibrationPanel.setBorder(BorderFactory.createEmptyBorder(12,8,5,5));

        JPanel panelStatistics = buildPanelStatistics();

        mainPanel.add(calibrationPanel, BorderLayout.CENTER);
        mainPanel.add(panelStatistics,BorderLayout.EAST);

        return mainPanel;		
    }

    private JPanel buildPanelStatistics()
    {
        SubPanel inner = new SubPanel();

        inner.addComponent(labelLastResult, 0,0,1,1,GridBagConstraints.EAST,GridBagConstraints.NONE,1,0);
        inner.addComponent(labelMean, 0,1,1,1,GridBagConstraints.EAST,GridBagConstraints.NONE,1,0);
        inner.addComponent(labelCount, 0,2,1,1,GridBagConstraints.EAST,GridBagConstraints.NONE,1,0);
        inner.addComponent(new JLabel("Branch"), 0,3,1,1,GridBagConstraints.EAST,GridBagConstraints.NONE,1,0);

        inner.addComponent(fieldLastResult, 1,0,1,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,1,0,new Insets(6,3,6,3));
        inner.addComponent(fieldMean, 1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,1,0,new Insets(6,3,6,3));
        inner.addComponent(fieldCount, 1,2,1,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,1,0,new Insets(6,3,6,3));
        inner.addComponent(comboCurveBranch, 1,3,1,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,1,0,new Insets(6,3,6,3));

        inner.setBorder(BorderFactory.createEmptyBorder(10,0,10,5));

        Box outer = Box.createVerticalBox();
        outer.add(inner);
        outer.add(Box.createVerticalStrut(120));

        JPanel panelStatistics = new JPanel(new BorderLayout());       
        panelStatistics.add(outer,BorderLayout.SOUTH);
        panelStatistics.setBorder(BorderFactory.createEmptyBorder(5,5,10,5));

        return panelStatistics;
    }

    private static final class BoldSourceListCellRenderer extends SourceListCellRenderer 
    {
        private static final long serialVersionUID = 1L;

        private BoldSourceListCellRenderer(boolean useLongName) {
            super(useLongName);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ChannelSource> list, ChannelSource value, int index, boolean isSelected, boolean cellHasFocus)
        {
            Font font = isSelected ? getFont().deriveFont(Font.BOLD) : getFont().deriveFont(Font.PLAIN);
            setFont(font);

            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    public void showDialog(PhotodiodeSignalType photodiodeSignalType)
    {
        calibrationModel.setPhotodiodeSignalType(photodiodeSignalType);
        setVisible(true);
    }

    private class AddSourceAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public AddSourceAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_D);
            putValue(NAME,"Add");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {			
            final ConcurrentReadingTask<SimpleSpectroscopySource> task = chooser.chooseSources(CalibrationDialog.this);
            if(task == null)
            {
                return;
            }

            PropertyChangeListener stateChangesListener = new PropertyChangeListener()
            {
                @Override
                public void propertyChange(PropertyChangeEvent evt) 
                {                   
                    if(SwingWorker.StateValue.DONE.equals(evt.getNewValue())) 
                    {
                        try 
                        {
                            List<SimpleSpectroscopySource> sources = task.get();
                            boolean cancelled = task.isCancelled();

                            if(cancelled || sources == null)
                            {
                                JOptionPane.showMessageDialog(CalibrationDialog.this, "Reading terminated", "", JOptionPane.INFORMATION_MESSAGE);
                            }
                            else
                            {
                                int failures = task.getFailuresCount();

                                if(failures > 0)
                                {
                                    JOptionPane.showMessageDialog(CalibrationDialog.this, "Errors occured during reading of " + failures + " files", "", JOptionPane.ERROR_MESSAGE);
                                }

                                calibrationModel.addSources(sources);
                            }
                        }
                        catch (InterruptedException | ExecutionException e) 
                        {
                            e.printStackTrace();
                        } 
                    }                   
                }

            };

            task.getPropertyChangeSupport().addPropertyChangeListener("state", stateChangesListener);
            task.execute();
        }
    }

    private class CancelAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CancelAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME,"Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            setVisible(false);
        };
    }

    private class ClearAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ClearAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
            putValue(NAME,"Clear");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            calibrationModel.clear();
        };
    }

    private class FinishAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FinishAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
            putValue(NAME,"Finish");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            double meanSensitivity = calibrationModel.getMeanSensitivity();
            PhotodiodeSignalType photodiodeSignalType = calibrationModel.getPhotodiodeSignalType();

            processingModel.setSensitivity(photodiodeSignalType, meanSensitivity);
            calibrationModel.clear();

            CalibrationDialog.this.setVisible(false);		
        }
    }


    private class SkipAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SkipAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME,"Skip");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            calibrationModel.next();
        }
    }

    private class ApplyAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ApplyAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);
            putValue(NAME,"Apply");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {            
            calibrationModel.apply();            
        };
    }


    private class FilterSourcesAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FilterSourcesAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(NAME,"Substrate");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            filterSources();
        };
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if(NEXT_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            nextAction.setEnabled(newVal);
        }
        else if(APPLY_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            applyAction.setEnabled(newVal);
        }
        else if(CLEAR_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            clearAction.setEnabled(newVal);
        }	
        else if(FINISH_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            finishAction.setEnabled(newVal);
        }
        else if(FORCE_CURVE_BRANCH.equals(property))
        {
            ForceCurveBranch branchNew = (ForceCurveBranch)evt.getNewValue();
            comboCurveBranch.setSelectedItem(branchNew);
        }
        else if(AVAILABLE_FORCE_CURVE_BRANCHES.equals(property))
        {
            Set<ForceCurveBranch> branchesNew = (Set<ForceCurveBranch>)evt.getNewValue();
            comboCurveBranch.setModel(new DefaultComboBoxModel<>(branchesNew.toArray(new ForceCurveBranch[] {})));
            comboCurveBranch.setSelectedItem(calibrationModel.getForceCurveBranch());
        }
        else if(MEAN_SENSITIVITY.equals(property))
        {
            double newVal = ((Number)evt.getNewValue()).doubleValue();
            String meanSensitivyText = Double.isNaN(newVal)? "":numberFormat.format(newVal);

            fieldMean.setText(meanSensitivyText);
        }
        else if(LAST_SENSITIVITY_MEASUREMENT.equals(property))
        {
            double newVal = ((Number)evt.getNewValue()).doubleValue();
            String lastSensitivyText = Double.isNaN(newVal)? "":numberFormat.format(newVal);

            fieldLastResult.setText(lastSensitivyText);
        }
        else if(SENSITIVITY_MEASUREMENT_COUNT.equals(property))
        {
            int newVal = ((Number)evt.getNewValue()).intValue();
            String sensitivityCountText = Integer.toString(newVal);

            fieldCount.setText(sensitivityCountText);
        }
        else if(CURRENT_RANGE.equals(property))
        {
            Range rangeNew = (Range)evt.getNewValue();
            calibrationPanel.setRange(rangeNew);
        }
        else if(CURRENT_SOURCE.equals(property))
        {
            SimpleSpectroscopySource source = (SimpleSpectroscopySource)evt.getNewValue();
            selectSource(source);
        }
        else if (SUBSTRATE_SELECTION_ENABLED.equals(property))
        {
            boolean enabled = (boolean)evt.getNewValue();
            substrateSelectionAction.setEnabled(enabled);
        }
        else if(CALIBRATION_SOURCES.equals(property))		
        {
            @SuppressWarnings("unchecked")
            List<SimpleSpectroscopySource> sources = (List<SimpleSpectroscopySource>)evt.getNewValue();
            setSources(sources);

            SimpleSpectroscopySource currentSource = calibrationModel.getCurrentSource();
            selectSource(currentSource);
        }
    }

    public void filterSources() 
    {
        substrateSelectionWizard.showDialog(calibrationModel.getCommonSourceDirectory());             
    }

    @Override
    public Range getRange()
    {
        return calibrationModel.getRange();
    }

    @Override
    public void setRange(Range range) 
    {
        calibrationModel.setRange(range);
    }

    @Override
    public void requestLowerRangeBound(double lowerBound)
    {
        calibrationModel.requestLowerRangeBound(lowerBound);
    }

    @Override
    public void requestUpperRangeBound(double upperBound)
    {
        calibrationModel.requestUpperRangeBound(upperBound);
    }

    @Override
    public Range getCurrentMaximumRange() 
    {
        return calibrationModel.calculateCurrentMaximumRange();
    }

    @Override
    public void requestCursorChange(Cursor cursor) {
        calibrationPanel.setCursor(cursor);
    }
}

