
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


import java.awt.Rectangle;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;

import atomicJ.analysis.Batch;
import atomicJ.analysis.BatchUtilities;
import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.analysis.ResultDestinationBasic;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardMapSampleCollection;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.Quantity;
import atomicJ.gui.StandardNumericalTable;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.statistics.ResultStatisticsModel;
import atomicJ.gui.statistics.StatisticsTable;
import atomicJ.gui.statistics.StatisticsTableModel;
import atomicJ.sources.Channel1DSource;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.MultiMap;
import atomicJ.utilities.Validation;


public class ResultTable <S extends Channel1DSource<?>, E extends Processed1DPack<E,S>, V extends  ResultDestinationBasic<S,E>> extends StandardNumericalTable
{   
    private static final long serialVersionUID = 1L;

    private final V resultDestination;
    private final PackFunctionListenerOfModel packFunctionListenerOfModel = new PackFunctionListenerOfModel();
    private final PropertyChangeListener dataPropertyChangeListener = new DataModelPropertyChangeListener();

    private ResultDataModel<S,E> dataModel;
    private final ProcessedPackCellRenderer packRenderer = new ProcessedPackCellRenderer(getPreferences());

    private final List<PackFunctionListener<E>> packFunctionListeners = new CopyOnWriteArrayList<>();

    //to create a pop-up menu, inheriting classes should call initMouseListener(buildPopupMenu());

    public ResultTable(ResultTableModel<S,E> tableModel, V resultDestination)
    {
        super(tableModel, true, false);

        this.resultDestination = resultDestination;
        this.dataModel = tableModel.getDataModel();
        this.dataModel.addPropertyChangeListener(dataPropertyChangeListener);

        setDefaultRenderer(Processed1DPack.class, packRenderer);

        initKeyListener();
        initListSelectionListeners();
        initPackFunctionListener();
    }

    public List<E> getProcessedPacks()
    {
        List<E> packs = dataModel.getProcessedPacks();
        return packs;
    }

    public int getPackCount()
    {
        int packCount = dataModel.getPackCount();
        return packCount;
    }

    public List<Batch<E>> getBatches()
    {
        return dataModel.getBatches();
    }

    public int getBatchCount()
    {
        int batchCount = dataModel.getBatchCount();
        return batchCount;
    }

    public void addProcessedBatches(Collection<Batch<E>> results) 
    {
        dataModel.addProcessedBatches(results);
    }

    public void removeProcessedPacks(List<E> packs)
    {
        dataModel.removeProcessedPacks(packs);
    }

    public void removeSources(List<S> sources)
    {
        dataModel.removeProcessedPacks(dataModel.findProcessedPack(sources));
    }

    public Set<ProcessedPackFunction<? super E>> getPackFunctions()
    {
        return dataModel.getPackFunctions();
    }

    protected V getResultDestination()
    {
        return resultDestination;
    }

    private void initPackFunctionListener()
    {
        dataModel.addPackFunctionListener(packFunctionListenerOfModel);
    }

    public StatisticsTableModel buildStatisticsModel(ProcessedPackFunction<? super E> f)
    {
        Quantity quantity = f.getEvaluatedQuantity();
        PrefixedUnit unit = quantity.getUnit();

        StatisticsTableModel model = new ResultStatisticsModel<S,E>(dataModel, f, unit);
        return model;
    }

    private void reorderView()
    {
        ResultTableModel<S,E> model = getModel();

        for(IdentityTag columnTag : model.getSpecialColumns())
        {
            int desiredIndex = model.getDesiredSpecialColumnViewIndex(columnTag);
            int currentIndex = model.getColumnIndex(columnTag);

            if(desiredIndex > -1)
            {
                moveColumn(currentIndex, desiredIndex);
            }
        }
    }

    public void setUseLongSourceName(boolean u)
    {
        packRenderer.setUseLongName(u);
        resizeAndRepaint();
    }   

    public boolean areChangesSaved()
    {
        ResultTableModel<S,E> model = getModel();
        return model.areChangesSaved();
    }

    @Override
    public void setSaved(boolean saved)
    {
        ResultTableModel<S,E> model = getModel();
        model.setSaved(saved);
    }

    public void clearData()
    {
        dataModel.clear();
    }

    public RecalculateResultsModel<S, E> getRecalculateModel()
    {
        ResultTableModel<S,E> model = (ResultTableModel<S,E>)super.getModel();

        return model.getRecalculateModel();
    }

    @Override
    public ResultTableModel<S,E> getModel()
    {
        ResultTableModel<S,E> model = (ResultTableModel<S,E>)super.getModel();
        return model;
    }

    @Override
    public void setModel(TableModel model) 
    {
        Validation.requireNonNull(model, "Null 'model' argument");

        if(!(model instanceof ResultTableModel))
        {
            throw new IllegalArgumentException("The type of 'model' must be ResultsTableModel");
        }

        if(this.dataModel != null)
        {
            this.dataModel.removePackFunctionListener(packFunctionListenerOfModel);
            this.dataModel.removePropertyChangeListener(dataPropertyChangeListener);
        }

        ResultTableModel<S,E> resultTableModel = (ResultTableModel<S,E>)model;
        this.dataModel = resultTableModel.getDataModel();
        if(packFunctionListenerOfModel != null)
        {
            this.dataModel.addPackFunctionListener(packFunctionListenerOfModel);
        }
        if(dataPropertyChangeListener != null)
        {
            this.dataModel.addPropertyChangeListener(dataPropertyChangeListener);
        }

        super.setModel(model);
    };

    public void selectSources(List<S> sources) throws UserCommunicableException
    {
        ResultTableModel<S,E> model = getModel();

        int failures = 0;

        clearSelection();

        for(S source: sources)
        {
            int rowIndex = model.findContainingRow(source);

            if(rowIndex <0)
            {
                continue;
            }

            int i = convertRowIndexToView(rowIndex);            
            if(i>-1)
            {
                addRowSelectionInterval(i,i);
                Rectangle rect = getCellRect(i, 0, true);
                scrollRectToVisible(rect);
            }
            else
            {
                failures++;
            }
        }       
        if(failures>0)
        {
            throw new UserCommunicableException("Unable to find " + failures + " of the requested results");
        }
    }

    public List<E> getSelectedPacks()
    {
        ResultTableModel<S,E> model = getModel();

        List<E> packs = new ArrayList<>();

        int[] selectionsView = getSelectedRows();
        int n = selectionsView.length;
        int[] selectionsModel = new int[n];
        for(int i = 0; i<n;i++)
        {
            selectionsModel[i] = convertRowIndexToModel(selectionsView[i]);;
        }
        Arrays.sort(selectionsModel);
        for(int i = n -1;i>=0;i--)
        {
            int s = selectionsModel[i];
            E pack = model.getPack(s);
            packs.add(pack);
        }
        Collections.reverse(packs);
        return packs;
    }

    public Map<String, StatisticsTable> buildStatisticsTables()
    {
        Map<String, StatisticsTable> tables = new LinkedHashMap<>();

        for(ProcessedPackFunction<? super E> f : dataModel.getPackFunctions())
        {
            Quantity quantity = f.getEvaluatedQuantity();
            PrefixedUnit unit = quantity.getUnit();
            String name = quantity.getName();

            StatisticsTable table = new StatisticsTable(new ResultStatisticsModel<S,E>(dataModel, f, unit));
            tables.put(name, table);
        }

        return tables;
    }

    private void showSelectedPacksOnHistogram(List<E> packs)
    {
        resultDestination.requestAdditionOfCalculationHistograms(getSampleCollectionForPooledBatches(packs));
    }

    public SampleCollection getSampleCollectionForPooledBatches()
    {
        return getSampleCollectionForPooledBatches(dataModel.getProcessedPacks());
    }

    public SampleCollection getSampleCollectionForPooledBatches(List<E> packs)
    {
        SampleCollection collection = new StandardMapSampleCollection<>(dataModel.getPackFunctions(), packs, "pooled batches");
        return collection;
    }

    public List<SampleCollection> getSampleCollectionsForSeparateBatches()
    {
        MultiMap<IdentityTag, E> batchPacksMap = BatchUtilities.segregateIntoBatches(dataModel.getProcessedPacks());

        List<SampleCollection> sampleCollections = new ArrayList<>();
        for(Entry<IdentityTag, List<E>> entry: batchPacksMap.entrySet())
        {               
            SampleCollection collection = new StandardMapSampleCollection<>(dataModel.getPackFunctions(), entry.getValue(), "batch " + entry.getKey().getLabel());
            sampleCollections.add(collection); 
        }

        return sampleCollections;
    }

    private List<SampleCollection> getSampleCollections(List<E> packs, String sampleName)
    {
        SampleCollection sampleCollection = new StandardMapSampleCollection<>(dataModel.getPackFunctions(), packs, sampleName);
        List<SampleCollection> sampleCollections = new ArrayList<>();
        sampleCollections.add(sampleCollection);
        return sampleCollections;
    }       

    private void showStatisticsForPacks(List<E> packs, String sampleName)
    {
        List<SampleCollection> sampleCollections = getSampleCollections(packs, sampleName);     
        Map<String, StatisticsTable> tables = StatisticsTable.getStatisticsTables(sampleCollections, "");

        resultDestination.showTemporaryCalculationStatisticsDialog(tables, "Selection statistics");
    }

    private void jumpToChart(E pack)
    {
        try
        {
            resultDestination.showFigures(pack);
        }
        catch(UserCommunicableException e)
        {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), e.getMessage(), "", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void initKeyListener()
    {
        addKeyListener(new KeyAdapter() 
        {
            @Override
            public void keyPressed(KeyEvent e) 
            {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                {
                    List<E> packs = getSelectedPacks();
                    dataModel.removeProcessedPacks(packs);
                }
            }
        });
    }

    private void initListSelectionListeners()
    {
        getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e) 
            {
                if(!e.getValueIsAdjusting())
                {
                    List<E> selectedPacks = getSelectedPacks();
                    dataModel.setSelectedPacks(selectedPacks);               
                }

                runAdditionResponsesToSelectionChange(e);
            } 
        });    
    }

    protected void runAdditionResponsesToSelectionChange(ListSelectionEvent e){};

    protected JPopupMenu buildPopupMenu() 
    {
        final JPopupMenu popup = buildCopyAndSelectAllPopupMenu();

        popup.addSeparator();

        JMenuItem itemJump = new JMenuItem("Find graphs");
        itemJump.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                jumpToChart(getSelectedPacks().get(0));
            }
        });
        popup.add(itemJump);

        //  popup.add(markOnMapItem);

        JMenuItem itemRecalculate = new JMenuItem("Recalculate");
        itemRecalculate.addActionListener(new ActionListener() 
        {         
            @Override
            public void actionPerformed(ActionEvent event) 
            {
                resultDestination.showRecalculationDialog(getRecalculateModel());
            }
        });
        popup.add(itemRecalculate);

        JMenuItem itemHistogram = new JMenuItem("Selection histogram");
        itemHistogram.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                showSelectedPacksOnHistogram(getSelectedPacks());
            }
        });
        popup.add(itemHistogram);

        JMenuItem itemStatistics = new JMenuItem("Selection statistics");
        itemStatistics.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                showStatisticsForPacks(getSelectedPacks(), "Selection");
            }
        });
        popup.add(itemStatistics);

        popup.add(new JSeparator());

        JMenuItem itemDelete = new JMenuItem("Delete");
        itemDelete.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                dataModel.removeProcessedPacks(getSelectedPacks());
            }
        });
        popup.add(itemDelete);

        return popup;      
    }

    private class PackFunctionListenerOfModel implements PackFunctionListener<E>
    {
        @Override
        public void packFunctionAdded(ProcessedPackFunction<? super E> f) 
        {
            //for some reason (why, I do not know) we need to reorder 
            //all additional columns, every time one is added
            reorderView();

            firePackFunctionAdded(f);
        }
    }


    public void addPackFunctionListener(PackFunctionListener<E> listener)
    {
        packFunctionListeners.add(listener);
    }

    public void removePackFunctionListener(PackFunctionListener<E> listener)
    {
        packFunctionListeners.remove(listener);
    }

    protected void firePackFunctionAdded(ProcessedPackFunction<? super E> function)
    {
        for(PackFunctionListener<E> listener : packFunctionListeners)
        {
            listener.packFunctionAdded(function);
        }
    }

    private class DataModelPropertyChangeListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt) 
        {           
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }
}

