
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

package atomicJ.gui.curveProcessing;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;

import javax.swing.*;

import atomicJ.analysis.*;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.ConcurrentPointSelectionChartRenderingTask;
import atomicJ.gui.ItemList;
import atomicJ.gui.ManualEstimatorType;
import atomicJ.gui.NumericalField;
import atomicJ.gui.PointSelectionChart;
import atomicJ.gui.PointSelectionPanel;
import atomicJ.gui.ProcessablePackListCellRenderer;
import atomicJ.gui.SelectionDialog;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.GUIUtilities;
import atomicJ.utilities.Validation;

import static atomicJ.gui.ManualEstimatorType.*;
import static atomicJ.gui.PreferenceKeys.WINDOW_HEIGHT;
import static atomicJ.gui.PreferenceKeys.WINDOW_WIDTH;
import static atomicJ.gui.curveProcessing.ContactSelectionModel.*;

public class ContactSelectionDialog extends JDialog implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_HEIGHT = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/2);
    private static final int DEFAULT_WIDTH = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/2);

    private final Preferences pref = Preferences.userRoot().node(getClass().getName());

    private final FitPreviewAction previewAction = new FitPreviewAction();
    private final ApplyAction applyAction = new ApplyAction();
    private final ApplyToAllAction applyToAllAction = new ApplyToAllAction();
    private final SkipAction skipAction = new SkipAction();
    private final FinishAction finishAction = new FinishAction();
    private final CancelAction cancelAction = new CancelAction();
    private final ItemList<ProcessableSpectroscopyPack> packsList = new ItemList<>();       

    private final JButton butonLivePreview = new JButton(previewAction);

    private final ButtonGroup radioButtons = new ButtonGroup();     
    private final JRadioButton buttonUseX = new JRadioButton("Use X", false);
    private final JRadioButton buttonUseY = new JRadioButton("Use Y", false);
    private final JRadioButton buttonUseBoth = new JRadioButton("Use both", false);

    private final NumericalField fieldSelectionX = new NumericalField();
    private final NumericalField fieldSelectionY = new NumericalField();

    private PrefixedUnit xUnit;
    private PrefixedUnit yUnit;
    private final JLabel labelUnitX = new JLabel();
    private final JLabel labelUnitY = new JLabel();

    private final PointSelectionPanel selectionPanel = new PointSelectionPanel();

    private final SelectionDialog<ProcessableSpectroscopyPack> sourceSelectionDialog  = new SelectionDialog<>(this);
    private final LivePreviewDialog previewDialog = new LivePreviewDialog(this, "Live preview", false);
    private ContactSelectionModel model;


    public ContactSelectionDialog(SpectroscopyResultDestination destination)
    {
        super(destination.getPublicationSite(),"Contact point selection ",ModalityType.APPLICATION_MODAL);

        Container content = getContentPane();
        content.setLayout(new BorderLayout());

        JPanel contactPanel = buildContactPointPanel();
        JPanel buttonsPanel = buildButtonsPanel();
        SubPanel sourcePanel = buildActiveSourcePanel();

        content.add(sourcePanel, BorderLayout.NORTH);
        content.add(contactPanel,BorderLayout.CENTER);
        content.add(buttonsPanel,BorderLayout.SOUTH);

        initComponentListener();
        initWindowListener();
        initFieldSelectionListeners();
        initItemListeners();


        int width = pref.getInt(WINDOW_WIDTH,DEFAULT_WIDTH);
        int height = pref.getInt(WINDOW_HEIGHT,DEFAULT_HEIGHT);

        if(GUIUtilities.isWindowSizeWellSpecified(width, height))
        {
            setSize(width,height);
        }
        else
        {
            setSize(DEFAULT_WIDTH,DEFAULT_HEIGHT);
        }

        setLocationRelativeTo(destination.getPublicationSite());
    }

    private void initWindowListener()
    {
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if(model != null)
                {
                    cancel();
                }
            }
        });
    }

    private void initComponentListener()
    {
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent evt)
            {
                pref.putInt(WINDOW_HEIGHT, getHeight());
                pref.putInt(WINDOW_WIDTH, getWidth());
            }
        });
    }

    private void initItemListeners()
    {
        buttonUseX.addItemListener(new ItemListener() 
        {         
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    model.setEstimatorType(ORDINATE_ESTIMATOR);
                }
            }
        });     
        buttonUseY.addItemListener(new ItemListener()
        {          
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    model.setEstimatorType(ABSCISSA_ESTIMATOR);
                }
            }
        });     
        buttonUseBoth.addItemListener(new ItemListener() 
        {          
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    model.setEstimatorType(POINT_ESTIMATOR);
                }
            }
        });  
    }

    private void initFieldSelectionListeners()
    {
        fieldSelectionX.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener() 
        {           
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                double x = ((Number)evt.getNewValue()).doubleValue();
                selectionPanel.setSelectionX(x);
            }
        });

        fieldSelectionY.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener()
        {           
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                double y = ((Number)evt.getNewValue()).doubleValue();
                selectionPanel.setSelectionY(y);
            }
        });
    }

    public List<ProcessableSpectroscopyPack> setModelAndShow(ContactSelectionModel model)
    {
        Validation.requireNonNullParameterName(model, "model");

        ContactSelectionModel oldModel = model;
        if(oldModel != null)
        {
            oldModel.removePropertyChangeListener(this);
        }

        this.model = model;
        this.model.addPropertyChangeListener(this);

        pullModelProperties();  
        setVisible(true);

        return model.getFinishedProcessablePacks();
    }

    private void pullModelProperties()
    {
        final ConcurrentPointSelectionChartRenderingTask task = new ConcurrentPointSelectionChartRenderingTask(model.getPacks(), this);

        PropertyChangeListener taskListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                if(SwingWorker.StateValue.DONE.equals(evt.getNewValue())) 
                {
                    boolean cancelled = task.isCancelled();

                    if(!cancelled)
                    {
                        Map<ProcessableSpectroscopyPack, PointSelectionChart> charts = task.getCharts(); 
                        pullModelProperties(new ArrayList<>(charts.values()));
                    }
                    else
                    {
                        cancel();
                    }
                }
            }
        };

        task.addPropertyChangeListener("state", taskListener);      
        task.execute();
    }


    private void pullModelProperties(List<PointSelectionChart> charts)
    {       
        List<ProcessableSpectroscopyPack> sources = model.getPacks();
        ProcessableSpectroscopyPack currentSource = model.getCurrentPack();
        int currentIndex = model.getCurrentPackIndex();

        UnitExpression selectionX = model.getX();
        UnitExpression selectionY = model.getY();

        double selectionXValue = (selectionX != null) ? selectionX.getValue() : Double.NaN;
        double selectionYValue = (selectionX != null) ? selectionY.getValue() : Double.NaN;

        this.xUnit = selectionX.getUnit();
        this.yUnit = selectionY.getUnit();

        ManualEstimatorType estimatorType = model.getEstimatorType();

        selectionPanel.setCharts(charts);      
        selectionPanel.setSelectedChart(currentIndex);

        selectionPanel.setSelection(selectionXValue, selectionYValue);

        fieldSelectionX.setValue(selectionXValue);
        fieldSelectionY.setValue(selectionYValue);

        labelUnitX.setText(xUnit.getFullName());
        labelUnitY.setText(yUnit.getFullName());

        setManualEstimatorType(estimatorType);

        packsList.setItems(sources);
        packsList.setSelectedValue(currentSource,true);
        packsList.revalidate();


        boolean finishEnabled = model.isFinishEnabled();
        boolean applyEnabled = model.isApplyEnabled();
        boolean applyToAllEnabled = model.isApplyToAllEnabled();
        boolean skipEnabled = model.isSkipEnabled();

        previewAction.setEnabled(applyEnabled);
        finishAction.setEnabled(finishEnabled);
        applyAction.setEnabled(applyEnabled);
        applyToAllAction.setEnabled(applyToAllEnabled);
        skipAction.setEnabled(skipEnabled);
    }

    private SubPanel buildActiveSourcePanel()
    {
        SubPanel activeSourcePanel = new SubPanel();

        packsList.setEnabled(false);
        packsList.setCellRenderer(new BoldProcessablePackListCellRenderer());
        packsList.setBackground(UIManager.getColor("Label.background"));            
        packsList.setVisibleRowCount(4);

        JScrollPane scrollPane  = new JScrollPane(packsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);     
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JButton selectButton = new JButton(new SelectAction());

        activeSourcePanel.addComponent(selectButton, 0,0,1,1,GridBagConstraints.CENTER, GridBagConstraints.NONE,0,1, new Insets(3,10,3,10));
        activeSourcePanel.addComponent(scrollPane, 1,0,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH,1,1 );

        activeSourcePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3),"Sources")));  

        return activeSourcePanel;
    }

    private JPanel buildButtonsPanel()
    {
        JButton buttonApply = new JButton(applyAction);
        JButton buttonApplytoAll = new JButton(applyToAllAction);
        JButton buttonSkip = new JButton(skipAction);
        JButton buttonFinish = new JButton(finishAction);
        JButton buttonCancel = new JButton(cancelAction);   

        JPanel innerButtons = new JPanel(new GridLayout(1, 0, 10, 10));
        innerButtons.add(buttonApply);
        innerButtons.add(buttonApplytoAll);
        innerButtons.add(buttonSkip);
        innerButtons.add(buttonFinish);
        innerButtons.add(buttonCancel);

        innerButtons.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(innerButtons);
        buttonsPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        return buttonsPanel;
    }

    private JPanel buildContactPointPanel()
    {
        JPanel contactPointPanel = new JPanel(new BorderLayout());

        selectionPanel.addPropertyChangeListener(this);
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,3));

        radioButtons.add(buttonUseX);
        radioButtons.add(buttonUseY);
        radioButtons.add(buttonUseBoth);
        radioButtons.setSelected(buttonUseBoth.getModel(), true);

        JLabel labelX = new JLabel("X:");
        JLabel labelY = new JLabel("Y:");

        Box controlsContainer = Box.createVerticalBox();

        JPanel panelPreview = new JPanel();
        JPanel panelRadio = new JPanel(new GridLayout(0,1,10,10));
        SubPanel panelFields = new SubPanel();

        panelPreview.add(butonLivePreview);

        panelFields.addComponent(labelX, 0,0,1,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,1,0);
        panelFields.addComponent(labelY, 0,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,1,0);
        panelFields.addComponent(fieldSelectionX, 1,0,1,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,1,0);
        panelFields.addComponent(fieldSelectionY, 1,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,1,0);       
        panelFields.addComponent(labelUnitX, 2,0,1,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,1,0);
        panelFields.addComponent(labelUnitY, 2,1,1,1,GridBagConstraints.CENTER,GridBagConstraints.NONE,1,0);       

        panelRadio.add(buttonUseX);
        panelRadio.add(buttonUseY);
        panelRadio.add(buttonUseBoth);

        panelPreview.setBorder(BorderFactory.createEmptyBorder(10,3,10,5));
        panelFields.setBorder(BorderFactory.createEmptyBorder(10,3,10,5));
        panelRadio.setBorder(BorderFactory.createEmptyBorder(15,3,10,5));

        controlsContainer.add(panelPreview);
        controlsContainer.add(panelFields);
        controlsContainer.add(panelRadio);
        controlsContainer.add(Box.createVerticalStrut(60));

        JPanel panelEast = new JPanel(new BorderLayout());
        panelEast.add(controlsContainer,BorderLayout.SOUTH);

        contactPointPanel.add(panelEast,BorderLayout.EAST);
        contactPointPanel.add(selectionPanel, BorderLayout.CENTER);

        return contactPointPanel;
    }   

    private ManualEstimatorType getSelectedEstimator()
    {
        ManualEstimatorType estimatorType = null;

        if(buttonUseX.isSelected())
        {
            estimatorType = ORDINATE_ESTIMATOR;
        }
        else if(buttonUseY.isSelected())
        {
            estimatorType = ABSCISSA_ESTIMATOR;
        }
        else if(buttonUseBoth.isSelected())
        {
            estimatorType = POINT_ESTIMATOR;
        }

        return estimatorType;
    }

    private void setManualEstimatorType(ManualEstimatorType newVal)
    {
        radioButtons.clearSelection();
        if(ManualEstimatorType.ORDINATE_ESTIMATOR.equals(newVal))
        {
            radioButtons.setSelected(buttonUseX.getModel(), true);
            fieldSelectionX.setEnabled(true);
            fieldSelectionY.setEnabled(false);
        }
        else if(ManualEstimatorType.ABSCISSA_ESTIMATOR.equals(newVal))
        {
            radioButtons.setSelected(buttonUseY.getModel(), true);
            fieldSelectionX.setEnabled(false);
            fieldSelectionY.setEnabled(true);
        }
        else if(ManualEstimatorType.POINT_ESTIMATOR.equals(newVal))
        {
            radioButtons.setSelected(buttonUseBoth.getModel(), true);
            fieldSelectionX.setEnabled(true);
            fieldSelectionY.setEnabled(true);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if(APPLY_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = applyAction.isEnabled();
            if(newVal != oldVal)
            {
                applyAction.setEnabled(newVal);
                previewAction.setEnabled(newVal);
            }
        }
        else if(APPLY_TO_ALL_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = applyToAllAction.isEnabled();
            if(newVal != oldVal)
            {
                applyToAllAction.setEnabled(newVal);
            }
        }
        else if(SKIP_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = skipAction.isEnabled();
            if(newVal != oldVal)
            {
                skipAction.setEnabled(newVal);
            }
        }
        else if(FINISH_ENABLED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = finishAction.isEnabled();
            if(newVal != oldVal)
            {
                finishAction.setEnabled(newVal);
            }
        }
        else if(ESTIMATOR_TYPE.equals(property))
        {
            ManualEstimatorType newVal = (ManualEstimatorType)evt.getNewValue();
            ManualEstimatorType oldVal = getSelectedEstimator();

            if(!Objects.equals(oldVal, newVal))
            {
                setManualEstimatorType(newVal);
                updatePreviewDialog();
            }            
        }
        else if(CURRENT_PACK.equals(property))
        {
            ProcessableSpectroscopyPack newVal = (ProcessableSpectroscopyPack)evt.getNewValue();
            ProcessableSpectroscopyPack oldVal = packsList.getSelectedValue();

            if(!Objects.equals(newVal, oldVal))
            {
                packsList.setSelectedValue(newVal,true);
                packsList.revalidate();
                updatePreviewDialog();
            }
        }
        else if(PACKS_STILL_TO_PROCESS.equals(property))
        {
            @SuppressWarnings("unchecked")
            List<ProcessableSpectroscopyPack> newVal = (List<ProcessableSpectroscopyPack>)evt.getNewValue();
            List<ProcessableSpectroscopyPack> oldVal = packsList.getItems();

            if(!Objects.equals(oldVal,newVal))
            {
                packsList.setItems(newVal);
                packsList.revalidate();
            }
        }
        else if(CURRENT_PACK_INDEX.equals(property))
        {
            int newVal = (Integer)evt.getNewValue();
            int oldVal = selectionPanel.getSelectedChartIndex();

            if(oldVal != newVal)
            {
                selectionPanel.setSelectedChart(newVal);
            }
        }
        else if(SELECTED_X.equals(property))
        {
            UnitExpression newVal = (UnitExpression)evt.getNewValue();
            UnitExpression oldVal = new UnitExpression(fieldSelectionX.getValue().doubleValue(), xUnit);

            if(!UnitExpression.equalUpToPrefices(oldVal, newVal))
            {
                double val = (newVal != null) ? newVal.derive(xUnit).getValue() : Double.NaN;
                fieldSelectionX.setValue(val);
                selectionPanel.setSelectionX(val);

                updatePreviewDialog();
            }           
        }
        else if(SELECTED_Y.equals(property))
        {
            UnitExpression newVal = (UnitExpression)evt.getNewValue();
            UnitExpression oldVal = new UnitExpression(fieldSelectionY.getValue().doubleValue(), yUnit);

            if(!UnitExpression.equalUpToPrefices(oldVal, newVal))
            {
                double val = (newVal != null) ? newVal.derive(yUnit).getValue() : Double.NaN;
                fieldSelectionY.setValue(val);
                selectionPanel.setSelectionY(val);

                updatePreviewDialog();
            }            
        }
        else if(PointSelectionChart.USERS_POINT_SELECTION.equals(property))
        {
            Point2D newVal = (Point2D)evt.getNewValue();

            double x = newVal.getX();
            double y = newVal.getY();

            model.setX(new UnitExpression(x, xUnit));
            model.setY(new UnitExpression(y, yUnit));
        }
    }

    private void updatePreviewDialog()
    {
        if(previewDialog.isVisible())
        {
            LivePreviewPack pack = model.getLivePreviewPack();          

            previewDialog.setLivePreviewPack(pack);
        }
    }

    private class FitPreviewAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FitPreviewAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
            putValue(NAME,"Live preview");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            LivePreviewPack pack = model.getLivePreviewPack();          
            previewDialog.setLivePreviewPack(pack);

            previewDialog.setVisible(true);
        };
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
            model.apply();
        };
    }

    private class ApplyToAllAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ApplyToAllAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_T);
            putValue(NAME,"Apply to all");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            model.applyToAll();
        };
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
            model.skip();
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
            previewDialog.setVisible(false);
            previewDialog.clear();
            setVisible(false);
        };
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
            cancel();
            setVisible(false);
        };
    }

    private void cancel()
    {
        model.cancel();
        previewDialog.setVisible(false);
        previewDialog.clear();
    }

    private class SelectAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;
        public SelectAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME,"Select");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            sourceSelectionDialog.setListElements(packsList.getItems());
            sourceSelectionDialog.setVisible(true);
            ProcessableSpectroscopyPack selectedSource = sourceSelectionDialog.getSelectedSource();

            if(selectedSource != null)
            {       
                model.setCurrentPack(selectedSource);
            }       
        };
    }

    private static class BoldProcessablePackListCellRenderer extends ProcessablePackListCellRenderer
    {
        private static final long serialVersionUID = 1L;

        public BoldProcessablePackListCellRenderer() 
        {
            super(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ProcessableSpectroscopyPack> list, ProcessableSpectroscopyPack value, int index, boolean isSelected, boolean cellHasFocus)
        {
            Font font = isSelected ? getFont().deriveFont(Font.BOLD) : getFont().deriveFont(Font.PLAIN);
            setFont(font);

            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
