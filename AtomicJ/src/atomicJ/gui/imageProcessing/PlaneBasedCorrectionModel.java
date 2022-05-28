
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

package atomicJ.gui.imageProcessing;

import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.units.UnitExpression;
import atomicJ.functions.MultivariateLinearFunction;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.ResourceView;
import atomicJ.utilities.GeometryUtilities;

public abstract class PlaneBasedCorrectionModel extends ImageBatchROIProcessingModel
{
    private static final double TOLERANCE = 1e-10;

    public static final String INTERCEPT = "Intercept";
    public static final String X_COEFFICIENT = "XCoefficient";
    public static final String Y_COEFFICIENT = "YCoefficient";
    public static final String Z_COEFFICIENT = "ZCoefficient";

    private double xCoeff = 0;
    private double yCoeff = 0;
    private double zCoeff = 0;
    private UnitExpression intercept;

    public PlaneBasedCorrectionModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter, boolean modifyAllTypes, boolean previewUnabledByDefault)
    {
        super(manager, channelFilter, modifyAllTypes, previewUnabledByDefault);

        this.intercept = new UnitExpression(0, getValueAxisDisplayedUnit());
    }

    public UnitExpression getIntercept()
    {
        return intercept;
    }

    public void setIntercept(UnitExpression interceptNew)
    {
        if(!GeometryUtilities.almostEqual(this.intercept, interceptNew, TOLERANCE))
        {
            UnitExpression interceptOld = this.intercept;
            this.intercept = interceptNew;

            firePropertyChange(INTERCEPT, interceptOld, interceptNew); 

            checkIfApplyEnabled();
            updatePreview();
        }    
    }

    public double getXCoefficient()
    {
        return xCoeff;
    }

    public void setXCoefficient(double xCoeffNew)
    {
        if(Double.compare(this.xCoeff, xCoeffNew) != 0)
        {
            double xCoeffOld = this.xCoeff;
            this.xCoeff = xCoeffNew;

            firePropertyChange(X_COEFFICIENT, xCoeffOld, xCoeffNew);   

            checkIfApplyEnabled();
            updatePreview();
        }    
    }

    public double getYCoefficient()
    {
        return yCoeff;
    }

    public void setYCoefficient(double yCoeffNew)
    {
        if(Double.compare(this.yCoeff, yCoeffNew) != 0)
        {
            double yCoeffOld = this.yCoeff;
            this.yCoeff = yCoeffNew;

            firePropertyChange(Y_COEFFICIENT, yCoeffOld, yCoeffNew);  

            checkIfApplyEnabled();
            updatePreview();
        }
    }

    public double getZCoefficient()
    {
        return zCoeff;
    }

    public void setZCoefficient(double zCoeffNew)
    {
        if(Double.compare(this.zCoeff, zCoeffNew) != 0)
        {
            double zCoeffOld = this.zCoeff;
            this.zCoeff = zCoeffNew;

            firePropertyChange(Z_COEFFICIENT, zCoeffOld, zCoeffNew); 

            checkIfApplyEnabled();
            updatePreview();
        }
    }

    protected MultivariateLinearFunction getFunction()
    {
        double[] slopes = new double[] {xCoeff, yCoeff, zCoeff};

        double interceptVaueDataUnits = intercept.derive(getDataUnit()).getValue();
        MultivariateLinearFunction f = new MultivariateLinearFunction(slopes, interceptVaueDataUnits);

        return f;
    }

    @Override
    protected boolean calculateApplyEnabled()
    {
        boolean applyEnabled = super.calculateApplyEnabled() && !Double.isNaN(xCoeff) && 
                !Double.isNaN(yCoeff) && !Double.isNaN(zCoeff) && (intercept != null) && !Double.isNaN(intercept.getValue());

        return applyEnabled;
    }
}
