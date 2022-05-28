
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

package atomicJ.gui;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.table.TableModel;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.units.StandardUnitSource;
import atomicJ.sources.IdentityTag;

public interface NumericalTableModel extends TableModel
{    
    public List<IdentityTag> getColumnShortNames();
    public File getDefaultOutputDirectory();
    public boolean isEmpty();

    public StandardUnitSource getUnitSource();
    public int getColumnIndex(IdentityTag columnId);
    public PrefixedUnit getDataUnit(IdentityTag columnId);
    public List<PrefixedUnit> getDataUnits();
    public List<PrefixedUnit> getDisplayedUnits();

    public void addPropertyChangeListener(PropertyChangeListener listener);
    public void removePropertyChangeListener(PropertyChangeListener listener);
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue);
}
