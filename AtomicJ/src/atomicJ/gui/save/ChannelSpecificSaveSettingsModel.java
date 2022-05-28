
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

package atomicJ.gui.save;

import static atomicJ.gui.save.SaveModelProperties.*;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.NameComponent;

public class ChannelSpecificSaveSettingsModel extends AbstractModel
{
    private final String key;

    private boolean save;
    private Object prefix;
    private Object root;
    private Object suffix;

    public ChannelSpecificSaveSettingsModel(String key)
    {
        this(key, NameComponent.PREFIX, NameComponent.ROOT, NameComponent.SUFFIX);
    }

    public ChannelSpecificSaveSettingsModel(String key, Object prefix, Object root, Object suffix)
    {
        this.key = key;

        this.save = true;
        this.prefix = prefix;
        this.root = root;
        this.suffix = suffix;
    }

    public String getKey()
    {
        return key;
    }

    public Object getPrefix()
    {
        return prefix;
    }

    public void setPrefix(Object prefixNew)
    {
        Object prefixOld = prefix;
        this.prefix = prefixNew;

        firePropertyChange(PREFIX, prefixOld, prefixNew);
    }

    public Object getRoot()
    {
        return root;
    }

    public void setRoot(Object rootNew)
    {
        Object rootOld = root;
        this.root = rootNew;

        firePropertyChange(ROOT, rootOld, rootNew);
    }

    public Object getSuffix()
    {
        return suffix;
    }

    public void setSuffix(Object suffixNew)
    {
        Object suffixOld = suffix;
        this.suffix = suffixNew;

        firePropertyChange(SUFFIX, suffixOld, suffixNew);
    }

    public boolean getSave()
    {
        return save;
    }

    public void setSave(boolean saveNew)
    {
        boolean saveOld = save;
        this.save = saveNew;

        firePropertyChange(SAVE_SERIES, saveOld, saveNew);
    }
}
