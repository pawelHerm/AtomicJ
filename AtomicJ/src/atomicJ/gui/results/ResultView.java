
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

package atomicJ.gui.results;

import static atomicJ.gui.PreferenceKeys.WINDOW_HEIGHT;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_X;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_Y;
import static atomicJ.gui.PreferenceKeys.WINDOW_WIDTH;


import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.*;

import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.analysis.ResultDestinationBasic;
import atomicJ.data.SampleCollection;
import atomicJ.data.units.Quantity;
import atomicJ.gui.BasicViewListener;
import atomicJ.gui.ColumnVisibilityDialog;
import atomicJ.gui.CustomizableXYBaseChart;
import atomicJ.gui.CustomizeResultTableDialog;
import atomicJ.gui.DataViewListener;
import atomicJ.gui.DataViewSupport;
import atomicJ.gui.NumericalFormatDialog;
import atomicJ.gui.NumericalTableExporter;
import atomicJ.gui.TextFileChooser;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.boxplots.BasicBoxPlotPanel;
import atomicJ.gui.boxplots.BoxAndWhiskerWizard;
import atomicJ.gui.boxplots.BoxAndWhiskerXYPlot;
import atomicJ.gui.boxplots.BoxPlotSimpleView;
import atomicJ.gui.histogram.HistogramWizard;
import atomicJ.gui.histogram.HistogramWizardModel;
import atomicJ.gui.histogram.HistogramWizardModelDoubleSelection;
import atomicJ.gui.statistics.InferenceSelectionPage;
import atomicJ.gui.statistics.RichWizardPage;
import atomicJ.gui.statistics.SimpleWizard;
import atomicJ.gui.statistics.SimpleWizardModel;
import atomicJ.gui.statistics.StatisticsView;
import atomicJ.gui.statistics.StatisticsTable;
import atomicJ.gui.statistics.StatisticsTableModel;
import atomicJ.gui.units.UnitSelectionDialog;
import atomicJ.sources.Channel1DSource;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.GUIUtilities;

public class ResultView <S extends Channel1DSource<?>, E extends Processed1DPack<E,S>>
{
    private static final int DEFAULT_HEIGHT = Math.round(3*Toolkit.getDefaultToolkit().getScreenSize().height/5);
    private static final int DEFAULT_WIDTH = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/2);
    private static final int DEFAULT_LOCATION_X = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/2);

    private final Preferences pref = Preferences.userRoot().node(getClass().getName());

    private HistogramWizard histogramsWizard;
    private BoxAndWhiskerWizard<E> boxPlotWizard;

    private final Action recalculateAction = new RecalculateAction();

    private final Action showHistogramsAction = new ShowHistogramsAction();
    private final Action statisticsAction = new DescriptiveStatisticsAction();
    private final Action addHistogramAction = new PooledHistogramAction();
    private final Action addMultipleHistogramsAction = new BatchHistogramsAction();
    private final Action addBoxAndWhiskerPlotsAction = new AddBoxAndWhiskerPlotsAction();
    private final Action showBoxAndWhiskerPlotsAction = new ShowBoxAndWhiskerPlotsAction();
    private final Action inferenceAction = new InferenceAction();

    private final Action formatAction = new FormatAction();
    private final Action visibilityAction = new ChangeVisibilityAction();
    private final Action selectUnitsAction = new SelectUnitsAction();
    private final Action clearAction = new ClearAction();
    private final Action saveAction = new SaveAction();
    private final Action printAction = new PrintAction();
    private final Action closeAction = new CloseAction();

    private final JMenu menuCurves;

    private final JToolBar toolBar;

    private final TextFileChooser fileChooser = new TextFileChooser();

    private final StatisticsView resultStatisticsDialog;

    private final NumericalFormatDialog customizeDialog;
    private final ColumnVisibilityDialog columnVisiblityDialog;

    private final JDialog viewDialog;
    private final BoxPlotSimpleView<BasicBoxPlotPanel<CustomizableXYBaseChart<BoxAndWhiskerXYPlot>>> boxPlotDialog;
    private final RecalculateResultsDialog<RecalculateResultsModel<S,E>> recalculateDialog;

    private final ResultDestinationBasic<S, E> resultDestination;
    private final ResultTable<S,E,?> table;

    private final UnitSelectionDialog unitSelectionDialog;
    private final DataViewSupport dataViewSupport = new DataViewSupport();

    public ResultView(ResultTable<S,E,?> table, ResultDestinationBasic<S, E> parent)
    {
        this.viewDialog = new JDialog(parent.getPublicationSite(),"Processing results",false);

        this.boxPlotDialog = new BoxPlotSimpleView<>(viewDialog, new BasicBoxPlotPanel.BasicBoxPlotPanelFactory(), "Box plots");
        this.recalculateDialog = new RecalculateResultsDialog<>(viewDialog, false);

        this.resultDestination = parent;   
        this.table = table;
        this.customizeDialog = new CustomizeResultTableDialog(viewDialog, table, table.getPreferences());
        this.columnVisiblityDialog = ColumnVisibilityDialog.getDialog(viewDialog, table, table.getColumnShortNames());
        this.unitSelectionDialog = new UnitSelectionDialog(viewDialog, table.getUnitSelectionPanel());

        this.resultStatisticsDialog = buildStatisticsDialog();

        boolean boxPlotsPresent = !boxPlotDialog.isEmpty();

        showHistogramsAction.setEnabled(false);
        showBoxAndWhiskerPlotsAction.setEnabled(boxPlotsPresent);

        initPropertyChangeListeners();
        checkIfEmpty();

        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = buildMenuFile();
        this.menuCurves = buildMenuCurves();
        JMenu menuHistograms = buildMenuHistograms();
        JMenu menuBoxPlots = buildMenuBoxPlots();
        JMenu menuStatistics = buildMenuStatistics();
        JMenu menuCustomize = buildMenuCustomize();

        menuBar.add(menuFile);
        menuBar.add(menuCurves);
        menuBar.add(menuHistograms);
        menuBar.add(menuBoxPlots);
        menuBar.add(menuStatistics);
        menuBar.add(menuCustomize); 

        menuBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredSoftBevelBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));

        this.toolBar = buildToolBar();        
        JScrollPane resultPane = buildResultPane();
        JPanel buttonsPanel = buildButtonPanel();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(resultPane, BorderLayout.CENTER);
        mainPanel.add(menuBar, BorderLayout.NORTH);

        viewDialog.setLayout(new BorderLayout(1,5));

        viewDialog.add(mainPanel,BorderLayout.CENTER);
        viewDialog.add(toolBar, BorderLayout.WEST);
        viewDialog.add(buttonsPanel, BorderLayout.SOUTH);

        initPackFunctionListener();

        int height = pref.getInt(WINDOW_HEIGHT,DEFAULT_HEIGHT);
        int width = pref.getInt(WINDOW_WIDTH,DEFAULT_WIDTH);
        int locationX =  pref.getInt(WINDOW_LOCATION_X, DEFAULT_LOCATION_X);
        int locationY =  pref.getInt(WINDOW_LOCATION_Y, 0);

        if(GUIUtilities.areWindowSizeAndLocationWellSpecified(width, height, locationX, locationY))
        {
            viewDialog.setSize(width,height);
            viewDialog.setLocation(locationX,locationY);
        }
        else
        {
            viewDialog.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            viewDialog.setLocation(DEFAULT_LOCATION_X, 0);
        } 

        viewDialog.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent evt)
            {           
                pref.putInt(WINDOW_HEIGHT, viewDialog.getHeight());
                pref.putInt(WINDOW_WIDTH, viewDialog.getWidth());
                pref.putInt(WINDOW_LOCATION_X, (int) viewDialog.getLocation().getX());           
                pref.putInt(WINDOW_LOCATION_Y, (int) viewDialog.getLocation().getY());

                dataViewSupport.fireDataViewVisiblityChanged(false);
            }

            @Override
            public void componentShown(ComponentEvent evt)
            {
                dataViewSupport.fireDataViewVisiblityChanged(true);
            }
        });
    }


    public void addDataViewListener(DataViewListener listener)
    {
        dataViewSupport.addDataViewListener(listener);
    }

    public void removeDataViewListener(DataViewListener listener)
    {
        dataViewSupport.removeDataViewListener(listener);
    }

    protected void fireDataAvailabilityChanged(boolean dataAvailableNew)
    {
        dataViewSupport.fireDataAvailabilityChanged(dataAvailableNew);
    }


    public void addStatisticsDataViewListener(DataViewListener listener)
    {
        resultStatisticsDialog.addDataViewListener(listener);
    }

    public void removeStatisticsDataViewListener(DataViewListener listener)
    {
        resultStatisticsDialog.removeDataViewListener(listener);
    }

    public boolean isVisible()
    {
        return viewDialog.isVisible();
    }

    public void setVisible(boolean visible)
    {
        viewDialog.setVisible(visible);
    }

    protected void addNewCurvesMenAction(Action action)
    {
        this.menuCurves.add(new JMenuItem(action));
    }

    protected void addNewToolbarAction(Action action, ImageIcon disabledIcon)
    {
        JButton button = new JButton(action);
        button.setDisabledIcon(disabledIcon);

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) button.getMaximumSize().getHeight()));

        toolBar.add(button);
    }

    public void showStatistics(boolean b)
    {
        viewDialog.setVisible(b);
        resultStatisticsDialog.setVisible(b);
    }

    public boolean isStatisticsDisplayed()
    {
        return resultStatisticsDialog.isVisible();
    }

    private StatisticsView buildStatisticsDialog()
    {
        Map<String, StatisticsTable> tables = table.buildStatisticsTables();

        StatisticsView dialog = new StatisticsView(viewDialog, tables, "Live results statistics", false);
        return dialog;
    } 

    private void initPackFunctionListener()
    {
        table.addPackFunctionListener(new PackFunctionListener<E>() {
            @Override
            public void packFunctionAdded(ProcessedPackFunction<? super E> f) 
            {
                Quantity quantity = f.getEvaluatedQuantity();
                String name = quantity.getName();

                StatisticsTableModel model = table.buildStatisticsModel(f);

                resultStatisticsDialog.addOrReplaceTableModel(model, name);                  
                columnVisiblityDialog.addNewColumn(new IdentityTag(name));
            }
        });
    }

    public void publishResults(Collection<Batch<E>> results) 
    {
        table.addProcessedBatches(results);
    }

    public void removeProcessedPacks(List<E> packs)
    {
        table.removeProcessedPacks(packs);
    }

    public void removeSources(List<S> sources)
    {
        table.removeSources(sources);
    }

    private JScrollPane buildResultPane()
    {
        JScrollPane resultPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS); 
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),resultPane.getBorder()));

        return resultPane;
    }

    private JMenu buildMenuFile()
    {
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(KeyEvent.VK_F);

        JMenuItem itemSave = new JMenuItem(saveAction);
        JMenuItem itemPrint = new JMenuItem(printAction);
        JMenuItem itemClear = new JMenuItem(clearAction);
        JMenuItem itemClose = new JMenuItem(closeAction);

        menuFile.add(itemSave);
        menuFile.add(itemPrint);
        menuFile.addSeparator();

        menuFile.add(itemClear);
        menuFile.addSeparator();

        menuFile.add(itemClose);

        return menuFile;
    }

    private JMenu buildMenuCurves()
    {
        JMenu menuCurves = new JMenu("Curves");

        menuCurves.add(new JMenuItem(recalculateAction));
        return menuCurves;
    }

    private JMenu buildMenuHistograms()
    {        
        JMenu menuHistograms = new JMenu("Histograms");
        menuHistograms.setMnemonic(KeyEvent.VK_H);

        menuHistograms.add(new JMenuItem(showHistogramsAction));
        menuHistograms.add(new JMenuItem(addHistogramAction));
        menuHistograms.add(new JMenuItem(addMultipleHistogramsAction));

        return menuHistograms;
    }

    private JMenu buildMenuBoxPlots()
    {
        JMenu menuBoxPlots = new JMenu("Box plots");

        menuBoxPlots.add(new JMenuItem(showBoxAndWhiskerPlotsAction));
        menuBoxPlots.add(new JMenuItem(addBoxAndWhiskerPlotsAction));

        return menuBoxPlots;
    }

    private JMenu buildMenuStatistics()
    {
        JMenu menuStatistics = new JMenu("Statistics");
        menuStatistics.setMnemonic(KeyEvent.VK_T);

        menuStatistics.add(new JMenuItem(statisticsAction));
        menuStatistics.add(new JMenuItem(inferenceAction));

        return menuStatistics;
    }

    private JMenu buildMenuCustomize()
    {
        JMenu menuCustomize = new JMenu("Customize");
        menuCustomize.setMnemonic(KeyEvent.VK_U);

        JMenuItem itemFormat = new JMenuItem(formatAction);
        JMenuItem itemUnits = new JMenuItem(selectUnitsAction);
        JMenuItem itemColumnVsibility = new JMenuItem(visibilityAction);

        menuCustomize.add(itemFormat);
        menuCustomize.add(itemUnits);
        menuCustomize.add(itemColumnVsibility);

        return menuCustomize;
    }

    private JToolBar buildToolBar()
    {
        JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);  
        toolBar.setBorder(BorderFactory.createLoweredBevelBorder());
        toolBar.setFloatable(false);
        toolBar.setAlignmentX(SwingConstants.LEFT);

        JButton buttonShowHistograms = new JButton(showHistogramsAction);
        JButton buttonAddHistogram = new JButton(addHistogramAction);
        JButton buttonAddMultipleHistograms = new JButton(addMultipleHistogramsAction);
        JButton buttonAddBoxAndWhiskerPlot = new JButton(addBoxAndWhiskerPlotsAction);
        JButton buttonShowBoxAndWhiskerPlot = new JButton(showBoxAndWhiskerPlotsAction);
        JButton buttonShowStatistics = new JButton(statisticsAction);
        JButton buttonInference = new JButton(inferenceAction);
        JButton buttonRecalculate = new JButton(recalculateAction);

        ImageIcon iconRecalculateDisabled = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Resources/RecalculateCurvesForDisabled.png"));
        buttonRecalculate.setDisabledIcon(new ImageIcon(GrayFilter.createDisabledImage(iconRecalculateDisabled.getImage())));

        buttonAddHistogram.setHorizontalAlignment(SwingConstants.LEFT);
        buttonAddMultipleHistograms.setHorizontalAlignment(SwingConstants.LEFT);
        buttonAddBoxAndWhiskerPlot.setHorizontalAlignment(SwingConstants.LEFT);
        buttonShowBoxAndWhiskerPlot.setHorizontalAlignment(SwingConstants.LEFT);
        buttonShowStatistics.setHorizontalAlignment(SwingConstants.LEFT);
        buttonShowHistograms.setHorizontalAlignment(SwingConstants.LEFT);
        buttonInference.setHorizontalAlignment(SwingConstants.LEFT);
        buttonRecalculate.setHorizontalAlignment(SwingConstants.LEFT);

        toolBar.add(buttonShowHistograms);
        toolBar.add(buttonAddHistogram);
        toolBar.add(buttonAddMultipleHistograms);
        toolBar.add(buttonShowBoxAndWhiskerPlot);
        toolBar.add(buttonAddBoxAndWhiskerPlot);
        toolBar.add(buttonShowStatistics);
        toolBar.add(buttonInference);
        toolBar.add(buttonRecalculate);

        for(Component c: toolBar.getComponents())
        {
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int) c.getMaximumSize().getHeight()));
        }

        return toolBar;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonGroupResultsContainer = new JPanel();

        JPanel buttonGroupResults = new JPanel(new GridLayout(1, 0, 5, 5));

        JButton buttonClose = new JButton(closeAction);
        JButton buttonClear = new JButton(clearAction);
        JButton buttonShowAll = new JButton(saveAction);
        JButton buttonPrint = new JButton(printAction);

        buttonGroupResults.add(buttonClear);
        buttonGroupResults.add(buttonShowAll);
        buttonGroupResults.add(buttonPrint);
        buttonGroupResults.add(buttonClose);
        buttonGroupResultsContainer.add(buttonGroupResults);
        buttonGroupResultsContainer.setBorder(BorderFactory.createRaisedBevelBorder());

        return buttonGroupResultsContainer;
    }

    public void setHistogramsAvailable(boolean available)
    {
        showHistogramsAction.setEnabled(available);
    }

    public void addHistograms(SampleCollection sampleCollection)
    {
        HistogramWizardModel model = new HistogramWizardModelDoubleSelection(resultDestination.getCalculationHistogramDestination(), Collections.singletonList(sampleCollection), true);
        initHistogramWizard(model);
    }

    public void addHistograms(List<SampleCollection> sampleCollections)
    {
        HistogramWizardModel model = new HistogramWizardModelDoubleSelection(resultDestination.getCalculationHistogramDestination(), sampleCollections, true);
        initHistogramWizard(model);     
    }

    private void initHistogramWizard(HistogramWizardModel model)
    {
        if(histogramsWizard == null)
        {
            histogramsWizard = new HistogramWizard(model);
        }
        else
        {
            histogramsWizard.setWizardModel(model);
        }
        histogramsWizard.showDialog();
    }

    public void buildBoxPlots()
    {
        if(boxPlotWizard == null)
        {
            boxPlotWizard = new BoxAndWhiskerWizard<>(boxPlotDialog);
        }

        boxPlotWizard.showDialog(table.getPackFunctions(), table.getBatches());
    }

    public void showBoxPlots()
    {
        boxPlotDialog.showBoxPlots(true);
    }

    public void showTemporaryStatisticsDialog(Map<String,StatisticsTable> tables, String title)
    {
        StatisticsView dialog = new StatisticsView(viewDialog, tables, title, true);
        dialog.setVisible(true);
    }

    public ResultTable<S,E,?> getResultTable()
    {
        return table;
    }

    public void saveResultTable()
    {
        if(table.isEmpty())
        {
            return;
        }

        File path = table.getDefaultOutputDirectory();

        fileChooser.setCurrentDirectory(path);
        int op = fileChooser.showSaveDialog(viewDialog.getParent());

        if(op == JFileChooser.APPROVE_OPTION)
        {   
            try 
            {                
                NumericalTableExporter exporter = new NumericalTableExporter(); 
                File selectedFile = fileChooser.getSelectedFile();                
                exporter.export(table, selectedFile, table.getDecimalFormat(), fileChooser.getSelectedExtensions());                         
                table.setSaved(true);
            } 
            catch (IOException ex) 
            {
                JOptionPane.showMessageDialog(viewDialog, "Error encountered while saving", "", JOptionPane.ERROR_MESSAGE);

            }
        }   
    }

    public void selectSources(List<S> sources) throws UserCommunicableException
    {
        table.selectSources(sources);
    }

    private void checkIfEmpty()
    {
        int packCount = table.getPackCount();
        int batchCount = table.getBatchCount();
        boolean tableNonEmpty = (packCount > 0);
        boolean tableMoreThanSingle = (packCount > 1);
        boolean enableAddMultipleHistograms = tableMoreThanSingle && (packCount > batchCount);

        recalculateAction.setEnabled(tableNonEmpty);
        clearAction.setEnabled(tableNonEmpty);
        saveAction.setEnabled(tableNonEmpty);
        printAction.setEnabled(tableNonEmpty);
        formatAction.setEnabled(tableNonEmpty);
        statisticsAction.setEnabled(tableNonEmpty);
        addBoxAndWhiskerPlotsAction.setEnabled(tableMoreThanSingle);
        addHistogramAction.setEnabled(tableMoreThanSingle);
        inferenceAction.setEnabled(tableMoreThanSingle);
        addMultipleHistogramsAction.setEnabled(enableAddMultipleHistograms);
    }

    private void showRecalculateDialog()
    {        
        recalculateDialog.showDialog(table.getRecalculateModel());
    }

    public void showRecalculateDialog(RecalculateResultsModel<S, E> model)
    {
        recalculateDialog.showDialog(model);
    }

    private void initPropertyChangeListeners()
    {
        table.addPropertyChangeListener(ResultDataModel.PACK_COUNT, new PropertyChangeListener() 
        {     
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                checkIfEmpty();                            
            }
        });

        table.addPropertyChangeListener(ResultDataModel.RESULTS_EMPTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {                
                dataViewSupport.fireDataAvailabilityChanged(!(Boolean)evt.getNewValue());
            }
        });

        boxPlotDialog.addDataViewListener(new BasicViewListener(showBoxAndWhiskerPlotsAction));
    }

    private class ClearAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;
        public ClearAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME, "Clear");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {

            if(table.isEmpty())
            {
                return;
            }

            if(!table.areChangesSaved())
            {
                int result = JOptionPane.showConfirmDialog(viewDialog,"Do you want to save results?","AtomicJ",JOptionPane.YES_NO_CANCEL_OPTION);
                switch(result)
                {
                case JOptionPane.YES_OPTION:
                    saveResultTable();
                    table.clearData();
                    return;
                case JOptionPane.NO_OPTION:
                    table.clearData();
                    return;
                case JOptionPane.CANCEL_OPTION:
                    return;
                }
            }
            else
            {
                table.clearData();
            }
            viewDialog.validate();       
        }
    }

    private class SaveAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME, "Save");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {           
            saveResultTable();
        }
    }

    private class PrintAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public PrintAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(NAME, "Print");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            try 
            {
                table.print();
            } 
            catch (PrinterException pe) 
            {
                JOptionPane.showMessageDialog(viewDialog, pe.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class CloseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;
        public CloseAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME, "Close");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            resultDestination.showCalculations(false);
        }
    }

    private class PooledHistogramAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public PooledHistogramAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/HistogramAdd.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(NAME,"Pooled histogram");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            addHistograms(table.getSampleCollectionForPooledBatches());
        }
    }

    private class BatchHistogramsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public BatchHistogramsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/HistogramAddMultiple.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(NAME,"Batch histograms");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            addHistograms(table.getSampleCollectionsForSeparateBatches());          
        }
    }

    private class AddBoxAndWhiskerPlotsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public AddBoxAndWhiskerPlotsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/AddBoxWhisker.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_X);
            putValue(NAME, "Add box plots");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            buildBoxPlots();           
        }
    }

    private class ShowBoxAndWhiskerPlotsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowBoxAndWhiskerPlotsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/BoxWhisker.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(NAME,"Show box plots");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showBoxPlots();           
        }
    }

    private class FormatAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FormatAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
            putValue(NAME,"Format data");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            customizeDialog.setVisible(true);
        }
    }

    private class ChangeVisibilityAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ChangeVisibilityAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_V);
            putValue(NAME,"Column visibility");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            columnVisiblityDialog.setVisible(true);
        }
    }

    private class InferenceAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public InferenceAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/Inference.png"));

            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
            putValue(LARGE_ICON_KEY, icon);

            putValue(MNEMONIC_KEY, KeyEvent.VK_I);
            putValue(NAME,"Inference");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            List<RichWizardPage> allPageModels = new ArrayList<>();
            InferenceSelectionPage selectionPageModel = new InferenceSelectionPage(table.getPackFunctions(), table.getBatches());
            allPageModels.add(selectionPageModel);
            allPageModels.addAll(selectionPageModel.getAllTestPages());

            SimpleWizard wizard = new SimpleWizard(new SimpleWizardModel(selectionPageModel, allPageModels), "Statistical inference", viewDialog);
            wizard.setVisible(true);
        }
    }

    private class ShowHistogramsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowHistogramsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/Histogram.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_W);
            putValue(NAME,"Show histograms");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            resultDestination.showCalculationsHistograms(true);
        }
    }

    private class DescriptiveStatisticsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public DescriptiveStatisticsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/Sigma.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_D);
            putValue(NAME,"Descitptive statistics");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            resultStatisticsDialog.setVisible(true);
        }
    }

    private class RecalculateAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public RecalculateAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/RecalculateCurves.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Recalculate");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            showRecalculateDialog();
        }
    }

    private class SelectUnitsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SelectUnitsAction() {
            putValue(NAME, "Select units");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            unitSelectionDialog.setVisible(true);
        }
    }
}
