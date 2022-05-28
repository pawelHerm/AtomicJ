package atomicJ.gui.rois;

import org.jfree.util.ObjectUtilities;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.Channel2DSupervisor;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.generalProcessing.OperationModel;
import atomicJ.utilities.GeometryUtilities;


public class WandROIModel extends OperationModel
{
    private static final double TOLERANCE = 1e-10;

    public static final String MAX_DIFFERENCE = "MaxDiffernce";
    public static final String MIN_DIFFERENCE = "MinDifference";
    public static final String DIFFERENCE_UNIT = "DifferenceUnit";

    public static final String INTERRUPT = "Interrupt";
    public static final String FILL_HOLES = "FillHoles";

    private double minDifference = -1;
    private double maxDifference = 1;
    private PrefixedUnit differenceUnit;

    private boolean fillHoles = false;

    private final Channel2DSupervisor supervisor;

    public WandROIModel(Channel2DSupervisor supervisor)
    {
        super(supervisor.getDrawableROIs(), supervisor.getROIUnion());
        this.supervisor = supervisor;
    }

    public double getMinDifference()
    {
        return minDifference;
    }

    public void setMinDifference(double minDifferenceNew)
    {      
        if(!GeometryUtilities.almostEqual(this.minDifference, minDifferenceNew, TOLERANCE))
        {
            double minDifferenceOld = this.minDifference;
            this.minDifference = minDifferenceNew;

            firePropertyChange(MIN_DIFFERENCE, minDifferenceOld, minDifferenceNew);

            checkIfApplyEnabled();
        }
    }

    public double getMaxDifference()
    {
        return maxDifference;
    }

    public void setMaxDifference(double maxDifferenceNew)
    {
        if(!GeometryUtilities.almostEqual(this.maxDifference, maxDifferenceNew, TOLERANCE))
        {
            double maxDifferenceOld = this.maxDifference;
            this.maxDifference = maxDifferenceNew;

            firePropertyChange(MAX_DIFFERENCE, maxDifferenceOld, maxDifferenceNew);

            checkIfApplyEnabled();
        }
    }

    public PrefixedUnit getDifferenceUnit()
    {
        return differenceUnit;
    }

    public void setUnitDifference(PrefixedUnit differenceUnitNew)
    {
        if(!ObjectUtilities.equal(differenceUnit, differenceUnitNew))
        {
            PrefixedUnit differenceUnitOld = this.differenceUnit;
            this.differenceUnit = differenceUnitNew;

            firePropertyChange(DIFFERENCE_UNIT, differenceUnitOld, differenceUnitNew);

            checkIfApplyEnabled();
        }
    }


    public boolean isFillHoles()
    {
        return fillHoles;
    }

    public void setFillHoles(boolean fillHolesNew)
    {
        if(this.fillHoles != fillHolesNew)
        {
            boolean fillHolesOld = this.fillHoles;
            this.fillHoles = fillHolesNew;

            firePropertyChange(FILL_HOLES, fillHolesOld, fillHolesNew);
        }
    }


    @Override
    protected boolean calculateApplyEnabled()
    {
        boolean applyEnabled = super.calculateApplyEnabled() &&(!Double.isNaN(minDifference)
                && !Double.isNaN(maxDifference) && (differenceUnit != null));

        return applyEnabled;
    } 

    public WandContourTracer getContourTracer()
    {
        WandContourTracer tracer = new WandContourTracer(minDifference, maxDifference, getROIPosition(), supervisor.getROIUnion(), differenceUnit);

        return tracer;
    }

    @Override
    public void operationFinished()
    {
        super.operationFinished();
        supervisor.setMode(MouseInputModeStandard.POLYGON_ROI);
    }
}
