
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

package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.*;

import atomicJ.analysis.*;
import atomicJ.data.SampleCollection;
import atomicJ.gui.curveProcessing.ContactSelectionDialog;
import atomicJ.gui.curveProcessing.CurveVisualizationHandler;
import atomicJ.gui.curveProcessing.CurveVisualizationReplaceHandler;
import atomicJ.gui.curveProcessing.CurveVisualizationReplotHandler;
import atomicJ.gui.curveProcessing.MapSourceHandler;
import atomicJ.gui.curveProcessing.MapSourceInPlaceRecalculationHandler;
import atomicJ.gui.curveProcessing.NumericalResultsHandler;
import atomicJ.gui.curveProcessing.NumericalResultsReplaceHandler;
import atomicJ.gui.curveProcessing.NumericalResultsSourceReplaceHandler;
import atomicJ.gui.curveProcessing.PreprocessCurvesHandler;
import atomicJ.gui.curveProcessing.PreprocessCurvesStandardHandle;
import atomicJ.gui.curveProcessing.ProcessingBatchModel;
import atomicJ.gui.curveProcessing.ProcessingModel;
import atomicJ.gui.curveProcessing.ProcessingWizard;
import atomicJ.gui.curveProcessing.SpectroscopyCurveAveragingHandler;
import atomicJ.gui.curveProcessing.CurveVisualizationStandardHandler;
import atomicJ.gui.curveProcessing.MapSourceStandardHandler;
import atomicJ.gui.curveProcessing.NumericalResultsStandardHandler;
import atomicJ.gui.histogram.HistogramDestination;
import atomicJ.gui.histogram.TransformableHistogramView;
import atomicJ.gui.results.RecalculateResultsModel;
import atomicJ.gui.results.ResultView;
import atomicJ.gui.results.ResultTable;
import atomicJ.gui.results.forceSpectroscopy.SpectroscopyResultView;
import atomicJ.gui.results.forceSpectroscopy.SpectroscopyResultTable;
import atomicJ.gui.results.forceSpectroscopy.SpectroscopyResultTableModel;
import atomicJ.gui.statistics.StatisticsTable;
import atomicJ.readers.general.GeneralSourceReadingModel;
import atomicJ.readers.nanoscope.NanoscopeFileStructureDialog;
import atomicJ.readers.nanoscope.NanoscopeFileStructureModel;
import atomicJ.readers.nanoscope.NanoscopeFileStructureModel.NanoscopePreferences;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.MapImageResource;
import atomicJ.resources.SpectroscopyBasicResource;
import atomicJ.resources.StandardChannel2DResource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.GUIUtilities;
import atomicJ.utilities.LinkRunner;
import atomicJ.utilities.LinkUtilities;


public class MainView implements SpectroscopyResultDestination, SpectroscopyProcessingOrigin, PreviewDestination, SpectroscopyPreferencesModel
{
    private static final String FIRST_PAPER_LINK = "http://dx.doi.org/10.1063/1.4881683";
    private static final String SECOND_PAPER_LINK = "https://doi.org/10.1016/j.ijmecsci.2020.106138";
    private static final Preferences PREF = Preferences.userNodeForPackage(MainView.class).node("MainFrame");
    private static final boolean DESKTOP_AVAILABLE = Desktop.isDesktopSupported()&&(!GraphicsEnvironment.isHeadless());

    private Desktop desktop;
    private boolean mailSupported;
    private boolean openSupported;

    private final Action processAction = new ProcessAction();
    private final Action openAction = new OpenAction();
    private final Action previewAction = new PreviewAction(); 
    private final Action graphsAction = new GraphicalResultsAction(); 
    private final Action mapsAction = new MapsAction(); 
    private final Action imagesAction = new ImagesAction();
    private final Action resultsAction = new NumericalResultsAction();
    private final Action statisticsAction = new StatisticsAction();
    private final Action showResultHistogramsAction = new ResultHistogramsAction();
    private final Action showMapHistogramsAction = new MapHistogramsAction();
    private final Action showImageHistogramsAction = new ImageHistogramsAction();
    private final Action parallelComputationPreferencesAction = new ParallelComputationPreferencesAction();
    private final Action nanoscopePreferencesAction = new NanoscopePreferencesAction();

    private final Action aboutAction = new AboutAction();
    private final Action manualAction = new ManualAction();
    private final Action questionAction = new QuestionAction();
    private final Action exitAction = new ExitAction();

    private final JFrame mainFrame = new JFrame();

    private final AboutDialog infoDialog = new AboutDialog(mainFrame);
    private final ResultView<SimpleSpectroscopySource, ProcessedSpectroscopyPack> resultsView = new SpectroscopyResultView(new SpectroscopyResultTable(new SpectroscopyResultTableModel(), this),this);
    private final Channel1DResultsView graphsView = new Channel1DResultsView(this);

    private final TransformableHistogramView resultsHistogramView = new TransformableHistogramView(mainFrame, "Result histograms");
    private final TransformableHistogramView mapHistogramView = new TransformableHistogramView(mainFrame, "Map histograms");
    private final TransformableHistogramView imageHistogramView = new TransformableHistogramView(mainFrame, "Image histograms");
    private final MapView mapView = new MapView(this);
    private final ImageView imageView = new ImageView(this);
    private final SpectroscopyView previewView = new SpectroscopyView(mainFrame);
    private final SpectroscopyView preprocessView = new SpectroscopyView(mainFrame, true);

    private final ContactSelectionDialog selectionDialog = new ContactSelectionDialog(this);

    private final FileOpeningWizard<ChannelSource> openingWizard = new FileOpeningWizard<>(new OpeningModelStandard<>(this), GeneralSourceReadingModel.getInstance());
    private ProcessingWizard processingWizard;

    private final GeneralPreferencesDialog parallelismPeferencesDialog = new GeneralPreferencesDialog(mainFrame, "Parallel computation preferences");
    private final NanoscopeFileStructureModel nanoscopePreferencesModel = new NanoscopeFileStructureModel();
    private final NanoscopeFileStructureDialog nanoscopePreferencesDialog = new NanoscopeFileStructureDialog(mainFrame, nanoscopePreferencesModel);
    private final ResultBatchesCoordinator resultBatchesCoordinator = new ResultBatchesCoordinator();


    public MainView()
    {		
        Image icon = Toolkit.getDefaultToolkit().getImage("Resources/Logo.png");
        mainFrame.setIconImage(icon);

        initDesktop();

        previewAction.setEnabled(false);
        graphsAction.setEnabled(false);
        mapsAction.setEnabled(false);
        imagesAction.setEnabled(false);
        resultsAction.setEnabled(false);
        statisticsAction.setEnabled(false);
        showResultHistogramsAction.setEnabled(false);
        showMapHistogramsAction.setEnabled(false);
        showImageHistogramsAction.setEnabled(false);

        manualAction.setEnabled(openSupported);
        questionAction.setEnabled(mailSupported);

        JMenuBar menuBar = buildMenuBar();
        mainFrame.setJMenuBar(menuBar);

        JToolBar toolBar = buildToolBar();
        mainFrame.add(toolBar, BorderLayout.PAGE_START);

        JPanel southOuterPanel = new JPanel(new BorderLayout());
        southOuterPanel.setBorder(BorderFactory.createEmptyBorder(0, 7, 7, 7));
        southOuterPanel.add(new MainPanel(), BorderLayout.CENTER);
        mainFrame.add(southOuterPanel, BorderLayout.CENTER);

        mainFrame.setTitle(AtomicJ.APPLICATION_NAME);

        initViewListeners();

        mainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                closeSafely();
            }
        });

        mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);	


        int width = PREF.getInt(WINDOW_WIDTH, -1);
        int height = PREF.getInt(WINDOW_HEIGHT, -1);

        int locationX = Math.max(0, PREF.getInt(WINDOW_LOCATION_X, 0));
        int locationY = Math.max(0, PREF.getInt(WINDOW_LOCATION_Y, 0));

        if(GUIUtilities.areWindowSizeAndLocationWellSpecified(width, height, locationX, locationY))
        {   
            mainFrame.setSize(width, height); 
            mainFrame.setLocation(locationX,locationY);
        }
        else 
        {
            mainFrame.setExtendedState(mainFrame.getExtendedState()|Frame.MAXIMIZED_BOTH );
        }		
    }	

    private JMenuBar buildMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createLoweredBevelBorder());
        JMenu menuFile = new JMenu("File");
        JMenu menuControls = new JMenu("Controls");
        JMenu menuHelp = new JMenu("Help");

        JMenuItem itemStartPreview = new JMenuItem(openAction);
        JMenuItem itemProcess = new JMenuItem(processAction);
        JMenuItem itemExit = new JMenuItem(exitAction);
        JMenuItem itemAbout = new JMenuItem(aboutAction);
        JMenuItem itemManual = new JMenuItem(manualAction);
        JMenuItem itemQuestion = new JMenuItem(questionAction);

        menuFile.add(itemStartPreview);
        menuFile.add(itemProcess);
        menuFile.add(new JSeparator());
        menuFile.add(itemExit);
        menuFile.setMnemonic(KeyEvent.VK_F);        

        JCheckBoxMenuItem itemImages = new JCheckBoxMenuItem(imagesAction);

        JCheckBoxMenuItem itemCharts = new JCheckBoxMenuItem(graphsAction);
        JCheckBoxMenuItem itemMaps = new JCheckBoxMenuItem(mapsAction);
        JCheckBoxMenuItem itemCalculations = new JCheckBoxMenuItem(resultsAction);
        JCheckBoxMenuItem itemPreview = new JCheckBoxMenuItem(previewAction);

        menuControls.add(itemPreview);
        menuControls.add(itemImages);
        menuControls.add(itemMaps);
        menuControls.add(itemCharts);
        menuControls.add(itemCalculations);

        JMenuItem itemParallelComputationPreferences = new JMenuItem(parallelComputationPreferencesAction);
        JMenuItem itemNanoscopePreferences = new JMenuItem(nanoscopePreferencesAction);

        menuControls.addSeparator();
        menuControls.add(itemParallelComputationPreferences);
        JMenu menuFileFormatPreferences = new JMenu("Format preferences");
        menuFileFormatPreferences.add(itemNanoscopePreferences);

        menuControls.add(menuFileFormatPreferences);
        menuControls.setMnemonic(KeyEvent.VK_C);

        menuHelp.add(itemAbout);    
        menuHelp.add(itemManual);
        menuHelp.add(itemQuestion);
        menuHelp.setMnemonic(KeyEvent.VK_H);

        menuBar.add(menuFile);
        menuBar.add(menuControls);
        menuBar.add(menuHelp);

        return menuBar;
    }

    private JToolBar buildToolBar()
    {
        JToolBar toolBar = new JToolBar(SwingConstants.HORIZONTAL);

        JButton buttonProcess = new JButton(processAction);
        JButton buttonStartPreview = new JButton(openAction);
        JButton buttonShowPreview = new JButton(previewAction);
        JButton buttonCharts = new JButton(graphsAction);
        JButton buttonResults = new JButton(resultsAction);
        JButton buttonImages = new JButton(imagesAction);
        JButton buttonMaps = new JButton(mapsAction);
        JButton buttonAbout = new JButton(aboutAction);

        buttonProcess.setHideActionText(true);
        buttonStartPreview.setHideActionText(true);
        buttonShowPreview.setHideActionText(true);
        buttonCharts.setHideActionText(true);
        buttonResults.setHideActionText(true);
        buttonImages.setHideActionText(true);
        buttonMaps.setHideActionText(true);
        buttonAbout.setHideActionText(true);

        toolBar.add(buttonProcess);
        toolBar.add(buttonStartPreview);
        toolBar.add(buttonShowPreview);
        toolBar.add(buttonCharts);
        toolBar.add(buttonResults);
        toolBar.add(buttonImages);
        toolBar.add(buttonMaps);
        toolBar.add(buttonAbout);

        for(Component c: toolBar.getComponents())
        {
            c.setMaximumSize(new Dimension((int) c.getMaximumSize().getWidth(),Integer.MAX_VALUE));
        }

        return toolBar;
    }


    @Override
    public JFrame getPublicationSite()
    {
        return mainFrame;
    }

    public void setVisible(boolean visible)
    {
        mainFrame.setVisible(visible);
    }

    @Override
    public void startPreview()
    {
        openingWizard.setVisible(true);
    }

    @Override
    public void startPreview(List<SimpleSpectroscopySource> sources) 
    {
        ConcurrentPreviewTask task = new ConcurrentPreviewTask(sources, this);          
        task.execute();        
    }

    @Override
    public void startPreviewForAllSources(List<ChannelSource> sources)
    {
        ConcurrentPreviewTask task = new ConcurrentPreviewTask(sources, this);			
        task.execute();
    }

    @Override
    public void requestPreviewEnd()
    {
        openingWizard.endPreview();
    }

    @Override
    public void publishPreviewData(Map<SpectroscopyBasicResource, Map<String,ChannelChart<?>>> charts)
    {
        if(!charts.isEmpty())
        {
            int previousCount = previewView.getResourceCount();
            previewView.addResources(charts);
            previewView.selectResource(previousCount);

            previewAction.putValue(Action.SELECTED_KEY, true);
            previewView.setVisible(true);
        }
    }

    @Override
    public void publishPreviewed2DData(Map<StandardChannel2DResource, Map<String, Channel2DChart<?>>> charts) 
    {
        if(!charts.isEmpty())
        {
            imageView.addCharts(charts, true);			
            imagesAction.putValue(Action.SELECTED_KEY, true);
            imageView.setVisible(true);
        }		
    }

    @Override
    public void withdrawPublication()
    {
        JOptionPane.showMessageDialog(mainFrame, "Computation terminated", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public ResultBatchesCoordinator getResultBatchesCoordinator()
    {
        return resultBatchesCoordinator;
    }

    @Override
    public void showFigures(boolean b)
    {
        graphsView.setVisible(b);
    }

    @Override
    public void showMaps(boolean b)
    {
        mapView.setVisible(b);  
    }

    @Override
    public void showImages(boolean b) 
    {
        imageView.setVisible(b);
    }

    @Override
    public void showCalculations(boolean b)
    {
        resultsView.setVisible(b);
    }

    @Override
    public void showCurves(boolean b)
    {
        previewView.setVisible(b);
    }

    @Override
    public void showCalculationsHistograms(boolean b)
    {
        resultsHistogramView.setVisible(b);
    }

    @Override
    public void showMapHistograms(boolean b) 
    {
        mapHistogramView.setVisible(b);
    }

    @Override
    public void showImageHistograms(boolean b) 
    {
        imageHistogramView.setVisible(b);
    }

    public void showPreferences()
    {
        parallelismPeferencesDialog.showDialog();
    }

    public void showNanoscopeFormatPreferences()
    {
        nanoscopePreferencesDialog.setVisible(true);
    }

    public TransformableHistogramView getCalculationHistogramDialog()
    {
        return resultsHistogramView;
    }

    @Override
    public  HistogramDestination getCalculationHistogramDestination()
    {
        return resultsHistogramView;
    }

    public TransformableHistogramView getMapHistogramDialog()
    {
        return mapHistogramView;
    }

    public TransformableHistogramView getImageHistogramDialog()
    {
        return imageHistogramView;
    }

    @Override
    public Channel1DResultsView getGraphicalResultsDialog()
    {
        return graphsView;
    }

    @Override
    public ResultView<SimpleSpectroscopySource, ProcessedSpectroscopyPack> getResultDialog()
    {
        return resultsView;
    }

    public MapView getMapDialog()
    {
        return mapView;
    }

    @Override
    public boolean isAnyMapResourceAvailable()
    {
        return !mapView.isEmpty();
    }

    @Override
    public List<MapImageResource> getMapResources()
    {
        return this.mapView.getResources();
    }

    public ImageView getImageDialog()
    {
        return imageView;
    }

    @Override
    public List<? extends Channel2DResource> getImageResources()
    {
        return this.imageView.getResources();
    }

    @Override
    public boolean areChannel2DResourcesAvailable()
    {
        return !imageView.isEmpty();
    }

    @Override
    public ContactSelectionDialog getContactPointSelectionDialog()
    {
        return selectionDialog;
    }

    @Override
    public void startProcessing()
    {
        startProcessing(Collections.emptyList(), resultBatchesCoordinator.getPublishedBatchCount());	
    }

    @Override
    public void startProcessing(int initialIndex)
    {
        startProcessing(Collections.emptyList(), initialIndex);
    }

    @Override
    public void startProcessing(List<SimpleSpectroscopySource> sources, int initialIndex)
    {
        ProcessingModel model = new ProcessingModel(this, this, getDefaultPreprocessCurvesHandler(), sources, initialIndex);

        processAction.setEnabled(false);
        createProcessingWizardIfNecessaryAndShow(model);
    }

    @Override
    public void startProcessing(List<ProcessingBatchModel> batches)
    {
        ProcessingModel model = new ProcessingModel(this, this, getDefaultPreprocessCurvesHandler(), batches, true);

        processAction.setEnabled(false);
        createProcessingWizardIfNecessaryAndShow(model);
    }

    @Override
    public void startProcessing(List<ProcessingBatchModel> batches, MapSourceHandler mapSourceHandler, CurveVisualizationHandler<VisualizableSpectroscopyPack> curveVisualizationHandler, NumericalResultsHandler<ProcessedSpectroscopyPack> numericalResultsHandler)
    {
        ProcessingModel model = new ProcessingModel(this, this, getDefaultPreprocessCurvesHandler(), batches, true);
        model.setMapSourceHandler(mapSourceHandler);
        model.setCurveVisualizationHandler(curveVisualizationHandler);
        model.setNumericalResultsHandler(numericalResultsHandler);

        processAction.setEnabled(false);
        createProcessingWizardIfNecessaryAndShow(model);
    }

    private void createProcessingWizardIfNecessaryAndShow(ProcessingModel model)
    {        
        if(this.processingWizard == null)
        {
            this.processingWizard = new ProcessingWizard(model);
        }
        else
        {
            this.processingWizard.setProcessingModel(model);
        }   
        this.processingWizard.setVisible(true);       
    }

    @Override
    public int recalculate(ProcessingBatchModel model, Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld)
    {
        NumericalResultsHandler<ProcessedSpectroscopyPack> resultsHandler = new NumericalResultsSourceReplaceHandler<>(resultsView, new ArrayList<>(sourcesNewVsOld.values()));
        CurveVisualizationHandler<VisualizableSpectroscopyPack> curveVisualizationHandler = new CurveVisualizationReplotHandler(graphsView, sourcesNewVsOld);
        MapSourceHandler mapSourceHandler = getRecalculationMapSourceHandler();

        ProcessingModel processingModel = new ProcessingModel(this, this, getDefaultPreprocessCurvesHandler(), Collections.singletonList(model), true);
        processingModel.setMapSourceHandler(mapSourceHandler);
        processingModel.setCurveVisualizationHandler(curveVisualizationHandler);
        processingModel.setNumericalResultsHandler(resultsHandler);

        int failuresCount = processingModel.processCurves();  

        return failuresCount;
    }

    @Override
    public void recalculate(List<ProcessingBatchModel> models, Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld, List<ProcessedSpectroscopyPack> packsToRemove, boolean deleteOldNumericalResults, boolean deleteOldCurveCharts, boolean modifyMapsInPlace)
    {
        NumericalResultsHandler<ProcessedSpectroscopyPack> resultsHandle = deleteOldNumericalResults ? new NumericalResultsReplaceHandler<>(resultsView, packsToRemove) : getDefaultNumericalResultsHandler();
        CurveVisualizationHandler<VisualizableSpectroscopyPack> curveVisualizationHandle = deleteOldCurveCharts ? new CurveVisualizationReplaceHandler(graphsView, sourcesNewVsOld) : getDefaultCurveVisualizationHandler();
        MapSourceHandler mapSourceHandler = modifyMapsInPlace ? getRecalculationMapSourceHandler() : getDefaultMapSourceHandler();

        startProcessing(models, mapSourceHandler, curveVisualizationHandle, resultsHandle);        
    }

    @Override
    public void recalculateSources(List<ProcessingBatchModel> models, Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld, List<SimpleSpectroscopySource> sourcesToRemove, boolean deleteOldNumericalResults, boolean deleteOldCurveCharts, boolean modifyMapsInPlace)
    {
        NumericalResultsHandler<ProcessedSpectroscopyPack> resultsHandle = deleteOldNumericalResults ? new NumericalResultsSourceReplaceHandler<>(resultsView, sourcesToRemove) : getDefaultNumericalResultsHandler();
        CurveVisualizationHandler<VisualizableSpectroscopyPack> curveVisualizationHandle = deleteOldCurveCharts ? new CurveVisualizationReplaceHandler(graphsView, sourcesNewVsOld) : getDefaultCurveVisualizationHandler();
        MapSourceHandler mapSourceHandle = modifyMapsInPlace ? getRecalculationMapSourceHandler() : getDefaultMapSourceHandler();

        startProcessing(models, mapSourceHandle, curveVisualizationHandle, resultsHandle);        
    }


    @Override
    public void endProcessing()
    {	
        //we set a new processing model to free memory,as the old ProcessingModel retains much memory, actually all the read in curves and the results of 
        //their processing
        processingWizard.setProcessingModel(new ProcessingModel(this, this, getDefaultPreprocessCurvesHandler()));
        processAction.setEnabled(true);
    }

    private void initDesktop()
    {
        if(DESKTOP_AVAILABLE)
        {
            desktop = Desktop.getDesktop();
            mailSupported = desktop.isSupported(Desktop.Action.MAIL);
            openSupported = desktop.isSupported(Desktop.Action.OPEN);
        }
    }

    private void closeSafely()
    {
        ResultTable<SimpleSpectroscopySource, ProcessedSpectroscopyPack,?> table = resultsView.getResultTable();

        if(!table.isEmpty() && !table.areChangesSaved())
        {
            final JOptionPane pane = new JOptionPane("Some results have not been saved. Do you want to save them now?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);

            JDialog dialog = pane.createDialog(mainFrame, AtomicJ.APPLICATION_NAME);
            dialog.addWindowListener(new WindowAdapter() 
            {           
                @Override
                public void windowClosing(WindowEvent evt) 
                {
                    pane.setValue(JOptionPane.CANCEL_OPTION);
                }
            });
            dialog.setContentPane(pane);
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            dialog.pack();

            dialog.setVisible(true);
            int result = ((Number) pane.getValue()).intValue();
            switch(result)
            {
            case JOptionPane.YES_OPTION: resultsView.saveResultTable(); break;
            case JOptionPane.NO_OPTION: break;
            case JOptionPane.CANCEL_OPTION: return;
            }
        }

        PREF.putInt(WINDOW_LOCATION_X, (int) Math.max(0,mainFrame.getLocation().getX()));			
        PREF.putInt(WINDOW_LOCATION_Y, (int)  Math.max(0,mainFrame.getLocation().getY()));	
        PREF.putInt(WINDOW_WIDTH, Math.max(10, mainFrame.getWidth()));
        PREF.putInt(WINDOW_HEIGHT, Math.max(10, mainFrame.getHeight()));

        mainFrame.dispose();
        System.exit(0);
    }

    private void initViewListeners()
    {
        previewView.addDataViewListener(new BasicViewListener(previewAction));      
        mapView.addDataViewListener(new BasicViewListener(mapsAction));
        mapHistogramView.addDataViewListener(new BasicViewListener(showMapHistogramsAction));

        imageView.addDataViewListener(new BasicViewListener(imagesAction));
        imageHistogramView.addDataViewListener(new BasicViewListener(showImageHistogramsAction));
        graphsView.addDataViewListener(new BasicViewListener(graphsAction));
        resultsView.addDataViewListener(new BasicViewListener(resultsAction));       
        resultsView.addStatisticsDataViewListener(new BasicViewListener(statisticsAction));

        resultsHistogramView.addDataViewListener(new DataViewListener()
        {           
            @Override
            public void dataViewVisibilityChanged(boolean visibleNew) 
            {
                showResultHistogramsAction.putValue(Action.SELECTED_KEY, visibleNew);         
            }

            @Override
            public void dataAvailabilityChanged(boolean availableNew)
            {
                showResultHistogramsAction.setEnabled(availableNew);
                resultsView.setHistogramsAvailable(availableNew);                   
            }
        });
    }

    private class StatisticsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public StatisticsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/Sigma.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(DISPLAYED_MNEMONIC_INDEX_KEY, 7);
            putValue(NAME,"Result statistics");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            resultsView.showStatistics(true);
            putValue(SELECTED_KEY, resultsView.isStatisticsDisplayed());
        }
    }

    private class ProcessAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ProcessAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/cogWheel.png"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(NAME,"Process");
            putValue(SHORT_DESCRIPTION,"Process force curves and maps");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            startProcessing();
        }
    }

    private class OpenAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public OpenAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/preview.png"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(NAME,"Open");
            putValue(SHORT_DESCRIPTION,"Open force curves and images");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            startPreview();
        }
    }


    private class PreviewAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public PreviewAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/showPreview.png"));


            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_V);
            putValue(NAME,"Force curves");
            putValue(SHORT_DESCRIPTION,"Previewed force curves");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showCurves(true);
            putValue(SELECTED_KEY, previewView.isVisible());
        }
    }

    private class GraphicalResultsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public GraphicalResultsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/graph.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_G);
            putValue(NAME,"Graphical results");
            putValue(SHORT_DESCRIPTION,"Graphical results");

        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showFigures(true);
            putValue(SELECTED_KEY, graphsView.isVisible());
        }
    }

    private class MapsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public MapsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/Map.png")); 

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_M);
            putValue(NAME,"Maps");
            putValue(SHORT_DESCRIPTION,"Show maps");

        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showMaps(true);
            putValue(SELECTED_KEY, mapView.isVisible());
        }
    }

    private class ImagesAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ImagesAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/Image.png")); 

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_I);
            putValue(NAME,"Images");
            putValue(SHORT_DESCRIPTION,"Show images");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showImages(true);
            putValue(SELECTED_KEY, imageView.isVisible());
        }
    }

    private class NumericalResultsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public NumericalResultsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/results.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_N);
            putValue(NAME,"Numerical results");
            putValue(SHORT_DESCRIPTION,"Show calculated results");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showCalculations(true);
            putValue(SELECTED_KEY, resultsView.isVisible());
        }
    }


    private class ResultHistogramsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResultHistogramsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/Histogram.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_H);
            putValue(NAME,"Result histograms");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showCalculationsHistograms(true);
            putValue(SELECTED_KEY, resultsHistogramView.isVisible());
        }
    }

    private class MapHistogramsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public MapHistogramsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/HistogramLarger.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(NAME,"Map histograms");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showMapHistograms(true);
            putValue(SELECTED_KEY, mapHistogramView.isVisible());
        }
    }

    private class ImageHistogramsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ImageHistogramsAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/Histogram.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_T);
            putValue(NAME,"Image histograms");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showImageHistograms(true);
            putValue(SELECTED_KEY, imageHistogramView.isVisible());
        }
    }

    private class ParallelComputationPreferencesAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ParallelComputationPreferencesAction()
        {    
            putValue(NAME,"Parallel computation");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            showPreferences();
        }
    }

    private class NanoscopePreferencesAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public NanoscopePreferencesAction()
        {    
            putValue(NAME,"Nanoscope");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            showNanoscopeFormatPreferences();
        }
    }

    private class AboutAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public AboutAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/about.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);
            putValue(NAME,"About");
            putValue(SHORT_DESCRIPTION,"About AtomicJ");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            infoDialog.setVisible(true);
        }
    }

    private class ManualAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ManualAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(NAME,"Manual");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(openSupported)
            {
                try {

                    File manualFile = new File(AtomicJ.MANUAL_FILE_NAME);
                    if (manualFile.exists()) 
                    {
                        desktop.open(manualFile);
                    } 
                    else 
                    {
                        JOptionPane.showMessageDialog(mainFrame, 
                                "The manual file was not found", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
                    }             
                } 
                catch (IOException ex) 
                {
                    JOptionPane.showMessageDialog(mainFrame, 
                            "The manual PDF file could not be opened.", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);                 
                }
            }
        }
    }

    private class QuestionAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public QuestionAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_Q);
            putValue(NAME,"Ask a question");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(mailSupported)
            {
                URI mailURI;
                try 
                {
                    mailURI = new URI("mailto", AtomicJ.CONTACT_MAIL + "?subject=" + AtomicJ.APPLICATION_NAME + " question", null);
                    desktop.mail(mailURI);
                }
                catch(IOException e)
                {
                    JOptionPane.showMessageDialog(mainFrame, "Error occured during launching the default mail client", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);

                }
                catch (URISyntaxException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ExitAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ExitAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_E);
            putValue(NAME,"Exit");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            closeSafely();
        }
    }


    @Override
    public boolean containsFiguresForSource(SimpleSpectroscopySource source)
    {
        return graphsView.containsChannelsFromSource(source);
    }

    @Override
    public void showFigures(SimpleSpectroscopySource source) throws UserCommunicableException 
    {
        graphsView.selectResourceContainingChannelsFrom(source);
        showFigures(true);           
    }

    @Override
    public void showFigures(ProcessedSpectroscopyPack processedPack) throws UserCommunicableException
    {
        if(graphsView.containsChannelsFromSource(processedPack.getSource()))
        {
            graphsView.selectResourceContainingChannelsFrom(processedPack.getSource());
        }
        else
        {
            List<VisualizableSpectroscopyPack> visualizablePacks = Collections.singletonList(processedPack.visualize(new VisualizationSettings()));
            getDefaultCurveVisualizationHandler().handlePublicationRequest(visualizablePacks);
        }
        showFigures(true);   
    }

    @Override
    public void showResults(SimpleSpectroscopySource source) throws UserCommunicableException 
    {
        resultsView.selectSources(Collections.singletonList(source));
        showCalculations(true);
    }

    @Override
    public void showResults(List<SimpleSpectroscopySource> sources) throws UserCommunicableException 
    {
        resultsView.selectSources(sources);
        showCalculations(true);
    }

    @Override
    public void markSourcePositions(List<SimpleSpectroscopySource> sources)
    {
        try
        {
            MapView mapDialog = getMapDialog();
            mapDialog.markSourcePositions(sources);
            showMaps(true);
        } 
        catch (UserCommunicableException e) 
        {
            JOptionPane.showMessageDialog(mainFrame, e.getMessage(), AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    @Override
    public void markSourcePositions(SimpleSpectroscopySource sources)
    {
        try
        {
            MapView mapDialog = getMapDialog();
            mapDialog.markSourcePosition(sources);
            showMaps(true);
        } 
        catch (UserCommunicableException e) 
        {
            JOptionPane.showMessageDialog(mainFrame, e.getMessage(), AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private static class LinkMouseListener extends MouseAdapter 
    {
        private final String link;

        public LinkMouseListener(String link)
        {
            this.link = link;
        }

        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            JLabel l = (JLabel) evt.getSource();
            try {
                URI uri = new java.net.URI(link);
                (new LinkRunner(uri)).execute();
            } catch (URISyntaxException use) {
                throw new AssertionError(use + ": " + l.getText()); //NOI18N
            }
        }
    }

    private static class MainPanel extends JPanel
    {
        private static final long serialVersionUID = 1L;

        private static final String CITATION_A = "<html>When AtomicJ contributes to a published work, please cite <br>" +
                "P. Hermanowicz et al., Rev. Sci. Instrum. 85, 063703 (2014) <br> " +
                "(<u>available here</u>). Thank you!</html>";
        private static final String CITATION_B = "<html>The Chebyshev - Lebedev formulae for load required to indent a thin sample were published in <br>" +
                "P. Hermanowicz, International Journal of Mechanical Sciences 85, 106138 (2021) <br> " +
                "(<u>available here</u>).</html>";

        private final Color background = new Color(100, 100, 100); 
        public MainPanel()
        {
            setLayout(new BorderLayout());

            setBorder(BorderFactory.createLineBorder(Color.black, 1));

            JPanel mainPanel = new JPanel();
            mainPanel.setBackground(background);

            add(mainPanel, BorderLayout.CENTER);

            JPanel panelCitationA = buildPanelCitation(CITATION_A, FIRST_PAPER_LINK);
            JPanel panelCitationB = buildPanelCitation(CITATION_B, SECOND_PAPER_LINK);

            SubPanel panelReferences = new SubPanel();
            panelReferences.addComponent(panelCitationA, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0,0,0,0));
            panelReferences.addComponent(panelCitationB, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(0,0,0,0));

            add(panelReferences, BorderLayout.SOUTH);
        }

        private JPanel buildPanelCitation(String text,String link)
        {
            JPanel panelCitation = new JPanel(new BorderLayout());
            panelCitation.setBackground(background);

            JLabel labelCitation = new JLabel();

            labelCitation.setOpaque(true);
            labelCitation.setBackground(background);
            labelCitation.setForeground(Color.white);
            labelCitation.setText(text);
            labelCitation.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));

            if (LinkUtilities.isBrowsingSupported()) {
                LinkUtilities.makeLinkable(labelCitation, new LinkMouseListener(link));
            }
            panelCitation.add(labelCitation, BorderLayout.WEST);

            return panelCitation;
        }
    }


    @Override
    public MapSourceHandler getDefaultMapSourceHandler() 
    {
        return new MapSourceStandardHandler(mapView);
    }

    @Override
    public MapSourceHandler getRecalculationMapSourceHandler()
    {
        return new MapSourceInPlaceRecalculationHandler(mapView);
    }

    @Override
    public CurveVisualizationHandler<VisualizableSpectroscopyPack> getDefaultCurveVisualizationHandler() 
    {
        return new CurveVisualizationStandardHandler(graphsView);
    }

    @Override
    public NumericalResultsHandler<ProcessedSpectroscopyPack> getDefaultNumericalResultsHandler()
    {
        return new NumericalResultsStandardHandler<>(resultsView);
    }

    @Override
    public SpectroscopyCurveAveragingHandler getDefaultCurveAveragingHandler()
    {
        return new SpectroscopyCurveAveragingHandler(graphsView);
    }

    public PreprocessCurvesHandler getDefaultPreprocessCurvesHandler()
    {
        return new PreprocessCurvesStandardHandle(preprocessView);
    }

    @Override
    public NanoscopePreferences getNanoscopePreferences()
    {
        return nanoscopePreferencesModel.getNanoscopePreferences();
    }

    @Override
    public void requestAdditionOfCalculationHistograms(SampleCollection samples) 
    {
        this.resultsView.addHistograms(samples);
    }

    @Override
    public void showTemporaryCalculationStatisticsDialog(Map<String, StatisticsTable> tables, String title) 
    {
        this.resultsView.showTemporaryStatisticsDialog(tables, "Selection statistics");
    }

    @Override
    public void showRecalculationDialog(RecalculateResultsModel<SimpleSpectroscopySource, ProcessedSpectroscopyPack> recalculateModel)
    {
        this.resultsView.showRecalculateDialog(recalculateModel);
    }
}