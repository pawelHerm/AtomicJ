
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

package atomicJ.readers.nanopuller;

import atomicJ.gui.AbstractModel;
import atomicJ.gui.UserDialogDecisionState;

public class NanopullerFileStructureModel extends AbstractModel
{
    private static final String TASK_NAME = "Nanopuller file structure";
    private static final String TASK_DESCRIPTION_BASE = "Specify how to interpret Nanopuller file ";

    public static final String FORCE_INCREASE_DIRECTION = "ForceIncreaseDirection";
    public static final String PIEZO_POSITION_INCREASE_DIRECTION = "PiezoPositionIncreaseDirection";

    public static final String APPLY_ENABLED = "ApplyEnabled";
    public static final String APPLY_TO_ALL_ENABLED = "ApplyToAllEnabled";

    private final String taskDescription;

    private QuantityIncreaseDirection forceIncreaseDirection;
    private QuantityIncreaseDirection piezoPositionIncreaseDirection;

    private boolean applyEnabled;
    private boolean applyToAllEnabled;

    private UserDialogDecisionState userDecision = UserDialogDecisionState.UNDECIDED;

    private final boolean multipleCurvesStillToAnalyse;

    public NanopullerFileStructureModel(QuantityIncreaseDirection forceIncreaseDirection, QuantityIncreaseDirection piezoPositionIncreaseDirection, String fileName, boolean multipleCurveStillToAnalyse)
    {
        this.forceIncreaseDirection = forceIncreaseDirection;
        this.piezoPositionIncreaseDirection = piezoPositionIncreaseDirection;
        this.multipleCurvesStillToAnalyse = multipleCurveStillToAnalyse;
        this.applyEnabled = checkIfApplyEnabled();
        this.applyToAllEnabled = checkIfApplyToAllEnabled();
        this.taskDescription = "<html>"+TASK_DESCRIPTION_BASE + "<i>"+fileName+"</i>" +"</html>";
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

    public QuantityIncreaseDirection getForceIncreaseDirection()
    {
        return forceIncreaseDirection;
    }

    public void setForceIncreaseDirection(QuantityIncreaseDirection forceIncreaseDirectionNew)
    {
        QuantityIncreaseDirection forceIncreaseDirectionOld = this.forceIncreaseDirection;
        this.forceIncreaseDirection = forceIncreaseDirectionNew;

        firePropertyChange(FORCE_INCREASE_DIRECTION, forceIncreaseDirectionOld, forceIncreaseDirectionNew);

        checkIfApplyEnabled();
        checkIfApplyToAllEnabled();
    }

    public QuantityIncreaseDirection getPiezoPositionIncreaseDirection()
    {
        return piezoPositionIncreaseDirection;
    }

    public void setPiezoPositionIncreaseDirection(QuantityIncreaseDirection piezoPositionIncreaseDirectionNew)
    {
        QuantityIncreaseDirection piezoPositionIncreaseDirectionOld = this.piezoPositionIncreaseDirection;
        this.piezoPositionIncreaseDirection = piezoPositionIncreaseDirectionNew;

        firePropertyChange(PIEZO_POSITION_INCREASE_DIRECTION, piezoPositionIncreaseDirectionOld, piezoPositionIncreaseDirectionNew);

        checkIfApplyEnabled();
        checkIfApplyToAllEnabled();
    }

    public boolean isApplyEnabled()
    {
        return applyEnabled;
    }

    private boolean checkIfApplyEnabled()
    {
        boolean applyEnabledNew = (this.piezoPositionIncreaseDirection != null && forceIncreaseDirection != null);

        boolean applyEnabledOld = this.applyEnabled;
        this.applyEnabled = applyEnabledNew;

        firePropertyChange(APPLY_ENABLED, applyEnabledOld, applyEnabledNew);

        return applyEnabledNew;
    }

    public boolean isApplyToAllEnabled()
    {
        return applyToAllEnabled;
    }

    private boolean checkIfApplyToAllEnabled()
    {
        boolean applyToAllEnabledNew = multipleCurvesStillToAnalyse && (this.piezoPositionIncreaseDirection != null && forceIncreaseDirection != null);

        boolean applyToAllEnabledOld = this.applyToAllEnabled;
        this.applyToAllEnabled = applyToAllEnabledNew;

        firePropertyChange(APPLY_TO_ALL_ENABLED, applyToAllEnabledOld, applyToAllEnabledNew);

        return applyToAllEnabledNew;
    }

    public NanopullerFileStructure getNanopullerFileStructure()
    {
        return new NanopullerFileStructure(forceIncreaseDirection, piezoPositionIncreaseDirection);
    }

    //immutable classs
    public static class NanopullerFileStructure
    {
        private final QuantityIncreaseDirection forceIncreaseDirection;
        private final QuantityIncreaseDirection piezoPositionIncreaseDirection;

        public NanopullerFileStructure(QuantityIncreaseDirection forceIncreaseDirection, QuantityIncreaseDirection piezoPositionIncreaseDirection)
        {
            this.forceIncreaseDirection = forceIncreaseDirection;
            this.piezoPositionIncreaseDirection = piezoPositionIncreaseDirection;
        }

        public QuantityIncreaseDirection getPiezoPositionIncreaseDirection()
        {
            return piezoPositionIncreaseDirection;
        }

        public QuantityIncreaseDirection getForceIncreaseDirection()
        {
            return forceIncreaseDirection;
        }
    }

    public static enum QuantityIncreaseDirection
    {
        TOWARDS_SAMPLE("Towards sample"), AWARDS_FROM_SAMPLE("Away from sample");

        private final String name;

        private QuantityIncreaseDirection(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
