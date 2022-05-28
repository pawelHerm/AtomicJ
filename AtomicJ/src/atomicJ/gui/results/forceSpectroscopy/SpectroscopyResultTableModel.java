
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

package atomicJ.gui.results.forceSpectroscopy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import atomicJ.analysis.*;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.Units;
import atomicJ.gui.results.ResultTableModel;
import atomicJ.gui.units.StandardUnitSource;
import atomicJ.sources.IdentityTag;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.statistics.DescriptiveStatistics;


public class SpectroscopyResultTableModel extends ResultTableModel<SimpleSpectroscopySource, ProcessedSpectroscopyPack> 
{
    private static final long serialVersionUID = 1L;

    private static final int SPECIAL_COLUMN_INSERTION_INDEX = 7;
    private static final int STANDARD_COLUMN_COUNT = 11;

    private final List<IdentityTag> columnIds = new ArrayList<>(Arrays.asList( new IdentityTag("Source name"),
            new IdentityTag("Young's modulus"),
            new IdentityTag("Contact X"),
            new IdentityTag("Contact Y"),
            new IdentityTag("Transition indent"),
            new IdentityTag("Transition force"),
            new IdentityTag("Deformation"),
            new IdentityTag("Spring const"),
            new IdentityTag("Sensitivity"),
            new IdentityTag("Tip geometry"),
            new IdentityTag("Batch")));

    private final List<PrefixedUnit> dataUnits = new ArrayList<>(Arrays.<PrefixedUnit>asList(null, 

            Units.KILO_PASCAL_UNIT, Units.MICRO_METER_UNIT,
            Units.NANO_NEWTON_UNIT, Units.MICRO_METER_UNIT,
            Units.NANO_NEWTON_UNIT, Units.MICRO_METER_UNIT,
            Units.NEWTON_PER_METER,
            Units.MICRO_METER_PER_VOLT_UNIT, null, null));

    private final StandardUnitSource unitSource;


    public SpectroscopyResultTableModel()
    {
        this(new SpectroscopyResultDataModel());
    }

    public SpectroscopyResultTableModel(SpectroscopyResultDataModel dataModel)
    {   
        super(dataModel, new SpectroscopyRecalculateResultsModel(dataModel),STANDARD_COLUMN_COUNT, SPECIAL_COLUMN_INSERTION_INDEX);

        this.unitSource = buildUnitSource(columnIds, dataUnits);
    }

    public boolean containsPacksFromMap(boolean selected)   
    {
        SpectroscopyResultDataModel dataModel = (SpectroscopyResultDataModel) getDataModel();
        return dataModel.containsPacksFromMap(selected);
    }

    @Override
    public StandardUnitSource getUnitSource()
    {
        return unitSource;
    }

    @Override
    protected void addUnitSourceGroup(IdentityTag group, PrefixedUnit selectedUnit, List<PrefixedUnit> units)
    {
        unitSource.addUnitGroup(group, selectedUnit, units);
    }

    @Override
    public List<IdentityTag> getColumnShortNames()
    {
        return new ArrayList<>(columnIds);
    }

    @Override
    public int getModelIndex(IdentityTag tag)
    {
        return columnIds.indexOf(tag);
    }

    @Override
    public int getColumnIndex(IdentityTag columnId)
    {      
        int index = columnIds.indexOf(columnId);
        return index;
    }

    @Override
    public PrefixedUnit getDataUnit(IdentityTag columnId)
    {
        int index = columnIds.indexOf(columnId);
        return dataUnits.get(index);
    }

    @Override
    public List<PrefixedUnit> getDataUnits()
    {
        return new ArrayList<>(dataUnits);
    }

    @Override
    public List<PrefixedUnit> getDisplayedUnits()
    {
        List<PrefixedUnit> units = new ArrayList<>(Collections.<PrefixedUnit>nCopies(columnIds.size(), null));

        for(Entry<IdentityTag, PrefixedUnit> entry : unitSource.getSelectedUnits().entrySet())
        {
            int index = columnIds.indexOf(entry.getKey());
            units.set(index, entry.getValue());
        }

        return units;
    }

    @Override
    public String getColumnName(int index)
    {
        IdentityTag id = columnIds.get(index);
        PrefixedUnit unit = unitSource.getSelectedUnit(id);
        String name = (unit != null) ? id.getLabel() + " (" + unit.getFullName() + ")" : id.getLabel();
        return name;
    }

    @Override
    protected void updateDefaultPrefices()
    {  
        for(int i = 0; i<columnIds.size(); i++)
        {
            PrefixedUnit dataUnit = dataUnits.get(i);
            if(dataUnit != null)
            {
                unitSource.setDefaultUnit(columnIds.get(i), getDefultUnit(i));
            }
        }
    }

    @Override
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
    protected PrefixedUnit getDataUnit(int columnIndex)
    {
        return dataUnits.get(columnIndex);
    }

    @Override
    protected void registerAddedColumnIdAndDataUnit(IdentityTag columnId,PrefixedUnit dataUnit)
    {
        columnIds.add(columnId);
        dataUnits.add(dataUnit);
    }

    @Override
    protected void readInBasicColumns(Object[] row, ProcessedSpectroscopyPack pack, String batchName)
    {
        NumericalSpectroscopyProcessingResults results = pack.getResults();
        ProcessingSettings settings = pack.getProcessingSettings();

        row[0] = pack;       
        row[1] = results.getYoungModulus();
        row[2] = results.getContactDisplacement();
        row[3] = results.getContactForce();
        row[4] = results.getTransitionIndentation();
        row[5] = results.getTransitionForce();
        row[6] = results.getMaximalDefomation();
        row[7] = settings.getSpringConstant();
        row[8] = settings.getSensitivity();
        row[9] = settings.getContactModel().getName();  
        row[10] = batchName;
    }

}