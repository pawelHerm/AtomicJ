
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2018 by Pawe³ Hermanowicz
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

package atomicJ.readers.nanoscope;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import atomicJ.gui.AbstractModel;
import atomicJ.gui.UserDialogDecisionState;

public class NanoscopeFileStructureModel extends AbstractModel
{
    private static final Preferences NANOSCOPE_FILE_PREFERENCES = Preferences.userNodeForPackage(NanoscopeFileStructureModel.class).node(NanoscopeFileStructureModel.class.getName());
    private static final String TASK_NAME = "Nanoscope file structure";

    public static final String USE_HEIGHT_SENSOR_DATA_WHENEVER_AVAILABLE = "UseHeightSensorDataWheneverAvailable";
    public static final String PIEZO_POSITION_INCREASE_DIRECTION = "PiezoPositionIncreaseDirection";

    public static final String APPLY_ENABLED = "ApplyEnabled";

    private final String taskDescription = "Specify how to interpret Nanoscope files";

    private boolean useHeightSensorDataWheneverAvailable;

    private boolean applyEnabled;

    private UserDialogDecisionState userDecision = UserDialogDecisionState.UNDECIDED;

    public NanoscopeFileStructureModel()
    {
        this.useHeightSensorDataWheneverAvailable = NANOSCOPE_FILE_PREFERENCES.getBoolean(USE_HEIGHT_SENSOR_DATA_WHENEVER_AVAILABLE, true);

        this.applyEnabled = checkIfApplyEnabled();
    }

    public String getTaskName()
    {
        return TASK_NAME;
    }

    public String getTaskDescription()
    {
        return taskDescription;
    }

    public void apply()
    {
        this.userDecision = UserDialogDecisionState.APPLY;
    }

    public void applyToAll()
    {
        this.userDecision = UserDialogDecisionState.APPLY_TO_ALL;
    }

    public void cancel()
    {
        this.userDecision = UserDialogDecisionState.CANCEL;
    }

    public UserDialogDecisionState getUserDialogDecisionState()
    {
        return userDecision;
    }

    public boolean isUseHeightSensorDataWheneverAvailable()
    {
        return useHeightSensorDataWheneverAvailable;
    }

    public void setUseHeightSensorDataWheneverAvailable(boolean useHeightSensorDataWheneverAvailableNew)
    {
        boolean useHeightSensorDataWheneverAvailableOld = this.useHeightSensorDataWheneverAvailable;
        this.useHeightSensorDataWheneverAvailable = useHeightSensorDataWheneverAvailableNew;

        firePropertyChange(USE_HEIGHT_SENSOR_DATA_WHENEVER_AVAILABLE, useHeightSensorDataWheneverAvailableOld, useHeightSensorDataWheneverAvailableNew);

        NANOSCOPE_FILE_PREFERENCES.putBoolean(USE_HEIGHT_SENSOR_DATA_WHENEVER_AVAILABLE, this.useHeightSensorDataWheneverAvailable);

        try
        {
            NANOSCOPE_FILE_PREFERENCES.flush();
        } catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }

        checkIfApplyEnabled();
    }


    public boolean isApplyEnabled()
    {
        return applyEnabled;
    }

    //it does nothing at this point, it never fires events, but I keep it as it may come in handy in the future
    private boolean checkIfApplyEnabled()
    {
        boolean applyEnabledNew = true;

        boolean applyEnabledOld = this.applyEnabled;
        this.applyEnabled = applyEnabledNew;

        firePropertyChange(APPLY_ENABLED, applyEnabledOld, applyEnabledNew);

        return applyEnabledNew;
    }

    public NanoscopePreferences getNanoscopePreferences()
    {
        return new NanoscopePreferences(useHeightSensorDataWheneverAvailable);
    }

    //immutable classs
    public static class NanoscopePreferences
    {
        private final boolean useHeightSensorDataWheneverAvailable;

        public NanoscopePreferences(boolean useHeightSensorDataWheneverAvailable)
        {
            this.useHeightSensorDataWheneverAvailable = useHeightSensorDataWheneverAvailable;
        }

        public boolean isUseHeightSensorDataWheneverAvailable()
        {
            return useHeightSensorDataWheneverAvailable;
        }
    }
}
