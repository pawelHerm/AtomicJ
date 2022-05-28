
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

package atomicJ.analysis;

import atomicJ.analysis.indentation.AdhesiveEnergyEstimationMethod;
import atomicJ.analysis.indentation.ContactModel;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.NullCurveTransformation;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;

public final class ProcessingSettings 
{	
    private final boolean contactAutomatic;
    private final double loadLimit;	
    private final double indentationLimit;	
    private final double fitIndentationLimit;
    private final double fitZMinimum;
    private final double fitZMaximum;
    private final double sensitivity;
    private final double springConstant;
    private final boolean calculateRSquared;
    private final boolean calculateAdhesionForce;
    private final boolean smoothed;
    private final boolean trimmed;
    private final Channel1DDataTransformation trimmer;
    private final Channel1DDataTransformation smoother;
    private final ContactModel contactModel;
    private final ContactEstimationGuide contactEstimationGuide;
    private final RegressionStrategy regressionStrategy;
    private final ForceCurveBranch fittedBranch;
    private final AdhesiveEnergyEstimationMethod adhesiveEnergyEstimationMethod;

    //immutable
    private ProcessingSettings(Builder builder)
    {
        this.contactAutomatic = builder.automatic;
        this.springConstant = builder.springConstant;
        this.sensitivity = builder.sensitivity;
        this.loadLimit = builder.loadLimit;
        this.indentationLimit = builder.indentationLimit;
        this.fitIndentationLimit = builder.fitIndentationLimit;
        this.fitZMinimum = builder.fitZMinimum;
        this.fitZMaximum = builder.fitZMaximum;
        this.calculateRSquared = builder.calculateRSquared;
        this.calculateAdhesionForce = builder.calculateAdhesionForce;
        this.smoothed = builder.smoothed;
        this.trimmed = builder.trimmed;
        this.trimmer = builder.trimmer;
        this.smoother = builder.smoother;
        this.contactModel = builder.contactModel;
        this.contactEstimationGuide = builder.contactEstimationGuide;
        this.regressionStrategy = builder.regressionStrategy;
        this.fittedBranch = builder.fittedBranch;
        this.adhesiveEnergyEstimationMethod = builder.adhesiveEnergyEstimationMethod;
    }

    public double getSpringConstant()
    {
        return springConstant;
    }

    public UnitExpression getSpringConstantWithUnit()
    {
        UnitExpression springConstantExpression = new UnitExpression(springConstant, Units.NEWTON_PER_METER);
        return springConstantExpression;
    }

    public double getSensitivity()
    {
        return sensitivity;
    }

    public double getLoadLimit()
    {
        return loadLimit;
    }

    public double getFitIndentationLimit()
    {
        return fitIndentationLimit;
    }

    public double getFitZMinimum()
    {
        return fitZMinimum;
    }

    public double getFitZMaximum()
    {
        return fitZMaximum;
    }

    public double getIndentationLimit()
    {
        return indentationLimit;
    }

    public boolean isCalculateRSquared()
    {
        return calculateRSquared;
    }

    public boolean isCalculateAdhesionForce()
    {
        return calculateAdhesionForce;
    }

    public boolean areDataSmoothed()
    {
        return smoothed;
    }	

    public boolean areDataTrimmed()
    {
        return trimmed;
    }

    public boolean isAutomatic()
    {
        return contactAutomatic;
    }	

    public ContactModel getContactModel()
    {
        return contactModel;
    }	

    public ContactEstimationGuide getContactEstimationGuide()
    {
        return contactEstimationGuide;
    }

    public RegressionStrategy getRegressionStrategy()
    {
        return regressionStrategy;
    }	

    public ForceCurveBranch getFittedBranch()
    {
        return fittedBranch;
    }

    public AdhesiveEnergyEstimationMethod getAdhesiveEnergyEstimationMethod()
    {
        return adhesiveEnergyEstimationMethod;
    }

    public Channel1DDataTransformation getSmoother()
    {
        return smoother;
    }

    public Channel1DDataTransformation getTrimmer()
    {
        return trimmer;
    }


    public static class Builder
    {
        private final boolean automatic;
        private final double springConstant;
        private final double sensitivity;
        private final ContactModel contactModel;
        private final ContactEstimationGuide contactEstimationGuide;
        private final RegressionStrategy regressionStrategy;
        private final ForceCurveBranch fittedBranch;
        private final AdhesiveEnergyEstimationMethod adhesiveEnergyEstimationMethod;
        private boolean calculateRSquared;
        private boolean calculateAdhesionForce;
        private boolean smoothed = false;
        private boolean trimmed = false;
        private double loadLimit = Double.POSITIVE_INFINITY;
        private double indentationLimit = Double.POSITIVE_INFINITY;
        private double fitIndentationLimit = Double.POSITIVE_INFINITY;
        private double fitZMinimum = Double.NEGATIVE_INFINITY;
        private double fitZMaximum = Double.POSITIVE_INFINITY;
        private Channel1DDataTransformation trimmer = NullCurveTransformation.getInstance();
        private Channel1DDataTransformation smoother = NullCurveTransformation.getInstance();

        public Builder(ContactModel contactModel, ContactEstimationGuide contactEstimationGuide, RegressionStrategy strategy, ForceCurveBranch fittedBranch, AdhesiveEnergyEstimationMethod adhesiveEnergyEstimationMethod, boolean automatic, double k, double sens)
        {
            this.contactModel = contactModel;
            this.contactEstimationGuide = contactEstimationGuide;
            this.regressionStrategy = strategy;
            this.fittedBranch = fittedBranch;
            this.adhesiveEnergyEstimationMethod = adhesiveEnergyEstimationMethod;
            this.automatic = automatic;
            this.springConstant = k;
            this.sensitivity = sens;
        }

        public Builder smoothed(boolean s){this.smoothed = s; return this;}

        public Builder smoother(Channel1DDataTransformation s){this.smoother = s; return this;}

        public Builder loadLimit(double m){this.loadLimit = m; return this;}

        public Builder indentationLimit(double m){this.indentationLimit = m; return this;}

        public Builder calculateRSquared(boolean b){this.calculateRSquared = b; return this;}

        public Builder calculateAdhesionForce(boolean b){this.calculateAdhesionForce = b; return this;}

        public Builder trimmed(boolean t){this.trimmed = t; return this;}

        public Builder trimmer(Channel1DDataTransformation t){this.trimmer = t; return this;}

        public Builder fitIndentationLimit(double fitIndentationLimit){this.fitIndentationLimit = fitIndentationLimit; return this;};

        public Builder fitZMinimum(double fitZMinimum){this.fitZMinimum = fitZMinimum; return this;};

        public Builder fitZMaximum(double fitZMaximum){this.fitZMaximum = fitZMaximum; return this;};

        public ProcessingSettings build()
        {
            return new ProcessingSettings(this);
        }
    }
}
