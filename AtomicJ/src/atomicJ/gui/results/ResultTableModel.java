
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

package atomicJ.gui.results;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import atomicJ.analysis.*;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.Units;
import atomicJ.gui.FastDefaultTableModel;
import atomicJ.gui.NumericalTableModel;
import atomicJ.gui.units.StandardUnitSource;
import atomicJ.sources.Channel1DSource;
import atomicJ.sources.IdentityTag;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MultiMap;


public abstract class ResultTableModel <S extends Channel1DSource<?>, E extends Processed1DPack<E,S>> extends FastDefaultTableModel implements ResultDataListener<S,E>, NumericalTableModel
{
    private static final long serialVersionUID = 1L;

    private boolean saved;
    private final ResultDataModel<S,E> dataModel;

    private final int standardColumnCount;
    private final int specialColumnInsertionIndex;

    private final List<IdentityTag> specialColumnIDs = new ArrayList<>();

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private final RecalculateResultsModel<S,E> recaulculationModel;

    public ResultTableModel(int standardColumnCount, int specialColumnInsertionIndex)
    {
        this.standardColumnCount = standardColumnCount;
        this.specialColumnInsertionIndex = specialColumnInsertionIndex;
        this.dataModel = new ResultDataModel<>();
        this.recaulculationModel = new RecalculateResultsModel<>(dataModel);

        setColumnCount(standardColumnCount);
    }

    public ResultTableModel(ResultDataModel<S,E> dataModel, RecalculateResultsModel<S,E> recaulculationModel, int standardColumnCount, int specialColumnInsertionIndex)
    {   
        setColumnCount(standardColumnCount);

        this.standardColumnCount = standardColumnCount;
        this.specialColumnInsertionIndex = specialColumnInsertionIndex;
        this.dataModel = dataModel;
        this.recaulculationModel = recaulculationModel;

        dataModel.addResultModelListener(this);

        Object[][] rows = convertBatchesToRows(dataModel.getBatches());
        addRows(rows);
    }

    public ResultDataModel<S,E> getDataModel()
    {
        return dataModel;
    }

    public RecalculateResultsModel<S, E> getRecalculateModel()
    {
        return recaulculationModel;
    }

    public boolean areChangesSaved()
    {
        return saved;
    }

    public void setSaved(boolean savedNew)
    {
        this.saved = savedNew;
    }

    @Override
    public boolean isEmpty()
    {
        Vector<?> data = getDataVector();
        return data.isEmpty();
    }

    @Override
    public File getDefaultOutputDirectory()
    {
        File defaultOutput = dataModel.getDefaultOutputDirectory();
        return defaultOutput;
    }

    @Override
    public Class<?> getColumnClass(int n)
    {
        Class<?> columnClass = (n == 0) ? Processed1DPack.class : Double.class;
        return columnClass;
    }

    @Override
    public abstract List<IdentityTag> getColumnShortNames();

    public abstract int getModelIndex(IdentityTag tag);

    @Override
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }

    @Override
    public void batchesAdded(ResultDataEvent<S,E> event) 
    {
        List<Batch<E>> batches = event.getBatches();
        addBatches(batches);

        this.saved = false;
    }

    public void addColumn(IdentityTag columnId, String columnLongName, PrefixedUnit dataUnit)
    {
        specialColumnIDs.add(columnId);
        registerAddedColumnIdAndDataUnit(columnId, dataUnit);
        Object[] values = new Object[getRowCount()];
        Arrays.fill(values, null);

        super.addColumn(columnLongName, values);
    }

    protected abstract void registerAddedColumnIdAndDataUnit(IdentityTag columnId,PrefixedUnit dataUnit);

    @Override
    public void batchesRemoved(ResultDataEvent<S,E> event) 
    {
        List<E> packs = event.getPacks();
        int[] rows = new int[packs.size()];

        for(int i = 0; i<packs.size(); i++)
        {
            E pack = packs.get(i);
            int rowIndex = findContainingRow(pack);
            rows[i] = rowIndex;

        }

        removeRows(rows);
        this.saved = false;
    }


    @Override
    public void batchesCleared(ResultDataEvent<S,E> event)
    {
        clear();
    }


    @Override
    public void packsAdded(ResultDataEvent<S,E> event) 
    {
        List<E> packs = event.getPacks();
        Batch<E> batch = event.getBatches().get(0);
        String batchName = batch.getName();
        addPacks(packs, batchName);

        this.saved = false;
    }

    @Override
    public void packsRemoved(ResultDataEvent<S,E> event) 
    {
        List<E> packs = event.getPacks();
        int[] rows = new int[packs.size()];

        for(int i = 0; i<packs.size(); i++)
        {
            E pack = packs.get(i);
            int rowIndex = findContainingRow(pack);
            rows[i] = rowIndex;
        }

        removeRows(rows);

        this.saved = false;
    }

    public E getPack(int r)
    {
        Vector<?> data = getDataVector();

        Vector<?> row = (Vector<?>)data.elementAt(r);
        E pack = (E)row.elementAt(0);
        return pack;
    }

    public int findContainingRow(S source)
    {
        int result = -1;

        Vector<?> data = getDataVector();
        int r = getRowCount();
        for(int i = 0;i<r;i++)
        {
            Vector<?> row = (Vector<?>)data.elementAt(i);
            Processed1DPack<?,?> p = (Processed1DPack<?,?>)row.elementAt(0);
            if(p.getSource().equals(source))
            {
                result = i;
                break;
            }
        }
        return result;
    }

    public int findContainingRow(E pack)
    {
        int result = -1;

        Vector<?> data = getDataVector();
        int r = getRowCount();
        for(int i = 0;i<r;i++)
        {
            Vector<?> row = (Vector<?>)data.elementAt(i);
            Processed1DPack<?,?> p = (Processed1DPack<?,?>)row.elementAt(0);
            if(p.equals(pack))
            {
                result = i;
                break;
            }
        }
        return result;
    }

    private void addBatches(List<Batch<E>> batches)
    {
        Object[][] rows = convertBatchesToRows(batches);
        addRows(rows);
        updateDefaultPrefices();
    }

    private void addPacks(List<E> packs, String batchName)
    {
        int n = packs.size();
        Object[][] rows = new Object[n][];
        for(int i = 0; i<n; i++)
        {
            E pack = packs.get(i);
            Object[]  row = convertPackToRow(pack, batchName);
            rows[i] = row;
        }

        addRows(rows);
        updateDefaultPrefices();
    }

    private Object[][] convertBatchesToRows(List<Batch<E>> batches)
    {
        Object[][] allData = new Object[][] {};

        for(int i = 0;i<batches.size();i++)
        {
            Batch<E> batch = batches.get(i);
            Object[][] data = convertBatchToRows(batch);
            allData = ArrayUtilities.join(allData, data);
        }   
        return allData;
    }

    private Object[][] convertBatchToRows(Batch<E> batch)
    {
        String batchName = batch.getName();
        List<E> packs = batch.getPacks();

        int m = packs.size();
        Object[][] data = new Object[m][];

        for(int j = 0;j<m;j++)
        {
            E pack = packs.get(j);
            Object[] row = convertPackToRow(pack, batchName);
            data[j] = row;              
        }
        return data;
    }

    protected abstract void readInBasicColumns(Object[] row, E pack, String batchName);

    public int getDesiredSpecialColumnViewIndex(IdentityTag id)
    {
        int index = specialColumnIDs.contains(id) ? specialColumnIDs.indexOf(id) + specialColumnInsertionIndex : -1;
        return index;
    }

    public List<IdentityTag> getSpecialColumns()
    {
        return Collections.unmodifiableList(specialColumnIDs);
    }

    public int getSpecialColumnCount()
    {
        return specialColumnIDs.size();
    }

    private Object[] convertPackToRow(E pack, String batchName)
    {
        List<? extends ProcessedPackFunction<? super E>> specialFunctions =  pack.getSpecialFunctions();

        for(ProcessedPackFunction<? super E> f : specialFunctions)
        {
            String columnLongNameNew = f.getEvaluatedQuantity().getLabel();
            IdentityTag columnIdNew = new IdentityTag(f.getEvaluatedQuantity().getName());

            if(!specialColumnIDs.contains(columnIdNew))
            {
                Quantity quantity = f.getEvaluatedQuantity();
                PrefixedUnit unit = quantity.hasDimension() ? quantity.getUnit() : null;


                addColumn(columnIdNew, columnLongNameNew, unit);
                dataModel.addPackFunction(f);               

                addUnitSourceGroup(columnIdNew, unit, buildPreferredUnits(unit));
            }           
        }

        Object[] row = new Object[getColumnCount()]; 

        readInBasicColumns(row, pack, batchName);

        for(ProcessedPackFunction<? super E> f : specialFunctions)
        {
            double val = f.evaluate(pack);
            int index = specialColumnIDs.indexOf(new IdentityTag(f.getEvaluatedQuantity().getName()));
            row[standardColumnCount + index] = val;
        }

        return row;
    }

    private List<PrefixedUnit> buildPreferredUnits(PrefixedUnit unit)
    {
        if(unit == null)
        {
            return Collections.singletonList(unit);
        }

        List<PrefixedUnit> proposedUnits = new ArrayList<>();
        if(unit != null && unit.hasPrevious())
        {
            proposedUnits.add(unit.getPrevious());
        }
        proposedUnits.add(unit);
        if(unit != null && unit.hasNext())
        {
            proposedUnits.add(unit.getNext());
        }

        return proposedUnits;
    }

    protected abstract void addUnitSourceGroup(IdentityTag group, PrefixedUnit selectedUnit, List<PrefixedUnit> units);

    @Override
    public abstract int getColumnIndex(IdentityTag columnId);

    @Override
    public abstract PrefixedUnit getDataUnit(IdentityTag columnId);

    protected abstract PrefixedUnit getDataUnit(int columnIndex);

    protected PrefixedUnit getDefultUnit(int columnIndex)
    {
        int rowCount = getRowCount();
        double[] values = new double[rowCount];

        for(int i = 0; i<rowCount; i++)
        {
            Object value = getValueAt(i, columnIndex);
            if(value instanceof Number)
            {
                values[i] = Math.abs(((Number)value).doubleValue());   
            }
        }

        double median = DescriptiveStatistics.median(values);

        return getDataUnit(columnIndex).getPreferredCompatibleUnit(median);
    }

    @Override
    public abstract List<PrefixedUnit> getDataUnits();

    @Override
    public abstract List<PrefixedUnit> getDisplayedUnits();

    @Override
    public abstract String getColumnName(int index);

    protected abstract void updateDefaultPrefices();

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) 
    {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) 
    {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) 
    {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }   

    protected static StandardUnitSource buildUnitSource(List<IdentityTag> columnIds, List<PrefixedUnit> dataUnits)
    {
        Map<IdentityTag, PrefixedUnit> selectedUnits = new LinkedHashMap<>();
        MultiMap<IdentityTag, PrefixedUnit> unitGroups = new MultiMap<>();

        for(int i = 0; i<columnIds.size(); i++)
        {
            PrefixedUnit dataUnit = dataUnits.get(i);
            if(dataUnit != null)
            {
                selectedUnits.put(columnIds.get(i), dataUnit);
                unitGroups.putAll(columnIds.get(i), Units.getPreferredDerivedUnits(dataUnit));
            }
        }

        StandardUnitSource unitSource = new StandardUnitSource(unitGroups, selectedUnits);
        unitSource.setUseDefaultUnits(true);

        return unitSource;
    }
}