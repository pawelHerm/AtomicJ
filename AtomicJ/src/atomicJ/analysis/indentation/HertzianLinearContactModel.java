
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

package atomicJ.analysis.indentation;

import java.awt.geom.Point2D;

import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.data.Channel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.Quantities;
import atomicJ.statistics.FittedLinearUnivariateFunction;
import atomicJ.statistics.LinearRegressionEsimator;


public abstract class HertzianLinearContactModel extends HertzianContactModel
{
    public HertzianLinearContactModel(PrecontactInteractionsModel precontactModel)
    {
        super(precontactModel);
    }

    protected abstract LinearRegressionEsimator getLinearRegression(double[][] forceIndentation, RegressionStrategy regressionStrategy, Point2D recordingPoint);   
    protected abstract FittedLinearUnivariateFunction getFittedFunction(double[][] forceIndentation, RegressionStrategy regressionStrategy, Point2D recordingPoint);   
    protected abstract FittedLinearUnivariateFunction getFittedFunction(double[] forceIndentationYs, double[] forceIndentationXs, RegressionStrategy regressionStrategy, Point2D recordingPoint);   

    public static abstract class HertzianLinearFit <E extends HertzianLinearContactModel> extends HertzianFit <E>
    {
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation;
        private final Point1DData forceIndentationTransitionPoint;
        private final FittedLinearUnivariateFunction fittedFunction;

        public HertzianLinearFit(E indentationModel, Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
        {         
            super(indentationModel, deflectionChannel, contactPoint, recordingPoint, processingSettings);

            this.annotatedForceIndentation = getAnnotatedForceIndentation();
            double[] fittableIndentationValues = annotatedForceIndentation.getFittableIndentationValues();
            double[] fittableForceValues = annotatedForceIndentation.getFittableForceValues();

            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            this.fittedFunction = indentationModel.getFittedFunction(fittableForceValues, fittableIndentationValues, regressionStrategy, recordingPoint);

            double[] forceIndentTransitionPointData = regressionStrategy.getLastCoveredPoint(fittableForceValues, fittableIndentationValues, fittedFunction); 
            this.forceIndentationTransitionPoint = new Point1DData(forceIndentTransitionPointData[0], forceIndentTransitionPointData[1], Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS);
        }

        @Override
        protected FittedLinearUnivariateFunction getFittedFunction()
        {
            return fittedFunction;
        }

        @Override
        public Point1DData getForceIndentationTransitionPoint()
        {
            return forceIndentationTransitionPoint;
        }
    }
}