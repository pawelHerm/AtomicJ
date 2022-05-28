
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

package atomicJ.gui.rois;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.table.DefaultTableModel;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.MinimalNumericalTable;
import atomicJ.gui.NumericalTableModel;
import atomicJ.gui.ShapeFactors;
import atomicJ.gui.units.StandardUnitSource;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.MultiMap;


public class ROIShapeFactorsTableModel extends DefaultTableModel implements NumericalTableModel
{
    private static final long serialVersionUID = 1L;

    private final PrefixedUnit unit;
    private final File defaultOutputFile;	

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private boolean empty = isEmpty();

    private final Map<Object, ROI> rois = new Hashtable<>();

    private static final List<IdentityTag> columnIds = Arrays.asList(
            new IdentityTag("ROI"),
            new IdentityTag("Area"),
            new IdentityTag("Centroid X"),
            new IdentityTag("Centroid Y"),
            new IdentityTag("Perimeter"),
            new IdentityTag("Box width"),
            new IdentityTag("Box height"),
            new IdentityTag("Feret min"),
            new IdentityTag("Feret max"),
            new IdentityTag("Circularity"));

    private final List<PrefixedUnit> dataUnits = new ArrayList<>();

    private final StandardUnitSource unitSource;

    public ROIShapeFactorsTableModel(PrefixedUnit unit)
    {
        this(new File(System.getProperty("user.home")), unit, unit);
    }

    public ROIShapeFactorsTableModel(File defaultOutputFile, PrefixedUnit dataUnit, PrefixedUnit displayedUnit)
    {	
        super(0, columnIds.size());
        this.unit = dataUnit;
        this.defaultOutputFile = defaultOutputFile;	

        dataUnits.addAll(Collections.<PrefixedUnit>nCopies(columnIds.size(), dataUnit));
        dataUnits.set(0, null); // ROI name has no unit
        dataUnits.set(1, dataUnit.power(2)); //area

        this.unitSource = buildUnitSource();
        unitSource.setUseDefaultUnits(true);

        unitSource.setDefaultUnit(columnIds.get(1), displayedUnit.power(2));
        for(int i = 2; i<columnIds.size();i++)
        {
            unitSource.setDefaultUnit(columnIds.get(i), displayedUnit);
        }

    }

    private StandardUnitSource buildUnitSource()
    {
        Map<IdentityTag, PrefixedUnit> selectedUnits = new LinkedHashMap<>();
        for(int i = 1; i<columnIds.size();i++)
        {
            selectedUnits.put(columnIds.get(i), dataUnits.get(i));
        }


        MultiMap<IdentityTag, PrefixedUnit> unitGroups = new MultiMap<>();

        List<PrefixedUnit> units = unit.deriveUnits();
        unitGroups.putAll(columnIds.get(1), unit.power(2).deriveUnits());
        for(int i = 2; i<columnIds.size();i++)
        {
            unitGroups.putAll(columnIds.get(i), units);
        }

        StandardUnitSource unitSource = new StandardUnitSource(unitGroups, selectedUnits);

        return unitSource;
    }



    public void addROI(ROI roi)
    {
        rois.put(roi.getKey(), roi);

        Object[] row = convertROIToRow(roi);
        addRow(row);
    }

    public void addOrUpdateROI(ROI roi)
    {	
        Object key = roi.getKey();

        boolean alreadyPresent = rois.containsKey(key);			
        rois.put(key, roi);

        Object[] row = convertROIToRow(roi);

        if(alreadyPresent)
        {
            replaceRow(row, roi.getKey());
        }
        else
        {
            addRow(row);
        }
    }

    public void addROIs(Map<Object, ? extends ROI> roisNew)
    {		   
        for(Entry<Object, ? extends ROI> entry: roisNew.entrySet())
        {
            Object key = entry.getKey();
            ROI roi = entry.getValue();

            boolean alreadyPresent = rois.containsKey(key);			
            this.rois.put(key, roi);

            Object[] row = convertROIToRow(roi);

            if(alreadyPresent)
            {
                replaceRow(row, key);
            }
            else
            {
                addRow(row);
            }
        }		
        checkIfEmpty();
    }

    public void setROIs(Map<Object, ? extends ROI> rois)
    {
        setRowCount(0);
        addROIs(rois);
    }

    public boolean replaceROI(ROI roi)
    {
        boolean tableChanged = false;

        Object roiKey = roi.getKey();
        boolean presentBefore = rois.containsKey(roiKey);			
        if(presentBefore)
        {
            Object[] row = convertROIToRow(roi);
            replaceRow(row, roiKey);
            tableChanged = true;
        }

        return tableChanged;
    }

    public void removeROI(ROI roi)
    {
        Object key = roi.getKey();
        int i = getSampleIndex(key);
        if(i>-1)
        {
            removeRow(i);
        }

        rois.remove(roi.getKey());
        setRowCount(rois.size());
        checkIfEmpty();
    }

    public void removeROIs(Collection<? extends ROI> rois) 
    {
        for(ROI roi: rois)
        {
            Object key = roi.getKey();
            int i = getSampleIndex(key);
            if(i>-1)
            {
                removeRow(i);
            }

            this.rois.remove(roi);
        }

        setRowCount(this.rois.size());
        checkIfEmpty();
    }

    public void replaceROILabel(Object roiKey, String labelNew)
    {
        int i = getSampleIndex(roiKey);
        if(i>-1)
        {
            setValueAt(new IdentityTag(roiKey, labelNew), i, 0);
        }
    }

    private void replaceRow(Object[] rowNew, Object roiKey)
    {
        int i = getSampleIndex(roiKey);
        if(i>-1)
        {		
            removeRow(i);
            insertRow(i, rowNew);
        }
        checkIfEmpty();
    }

    protected int getSampleIndex(Object roiKey)
    {
        int result = -1;
        Vector<?> data = getDataVector();
        int r = getRowCount();
        for(int i = 0;i<r;i++)
        {
            Vector<?> row = (Vector<?>)data.elementAt(i);
            IdentityTag p = (IdentityTag)row.elementAt(0);
            if(p.getKey().equals(roiKey))
            {
                result = i;
                break;
            }
        }
        return result;
    }

    @Override
    public File getDefaultOutputDirectory() 
    {
        return defaultOutputFile;
    }

    @Override
    public boolean isEmpty()
    {
        Vector<?> data = getDataVector();
        return data.isEmpty();
    }

    @Override
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int n)
    {
        Class<?> c = (n == 0) ? Integer.class : Double.class;
        return c;
    }

    @Override
    public List<IdentityTag> getColumnShortNames()
    {
        return new ArrayList<>(columnIds);
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
    public StandardUnitSource getUnitSource()
    {
        return unitSource;
    }

    @Override
    public String getColumnName(int index)
    {
        IdentityTag id = columnIds.get(index);
        PrefixedUnit unit = unitSource.getSelectedUnit(id);
        String name = (unit != null) ? id.getLabel() + " (" + unit.getFullName() + ")" : id.getLabel();
        return name;
    }

    public void clearData()
    {
        rois.clear();
        setRowCount(0);
    }

    protected Object[] convertROIToRow(ROI roi)
    {		
        ShapeFactors sf = roi.getShapeFactors(0.);

        Object[] row = new Object[10];

        row[0] = roi.getIdentityTag();
        row[1] = sf.getArea();
        row[2] = sf.getCentroidX();	
        row[3] = sf.getCentroidY();
        row[4] = sf.getPerimeter();
        row[5] = sf.getBoxWidth();
        row[6] = sf.getBoxHeight();
        row[7] = sf.getFeretMin();
        row[8] = sf.getFeretMax();
        row[9] = sf.getCircularity();

        return row;	
    }

    protected void checkIfEmpty()
    {
        boolean emptyOld = empty;
        boolean emptyNew = isEmpty();
        this.empty = emptyNew;

        firePropertyChange(MinimalNumericalTable.TABLE_EMPTY, emptyOld, emptyNew);
    }

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
}
