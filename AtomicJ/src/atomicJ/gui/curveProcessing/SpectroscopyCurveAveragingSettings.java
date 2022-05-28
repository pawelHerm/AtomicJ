package atomicJ.gui.curveProcessing;

import atomicJ.curveProcessing.ErrorBarType;
import atomicJ.utilities.Validation;

public class SpectroscopyCurveAveragingSettings 
{
    private final int POINT_COUNT = 30;

    private final boolean showAveragedRecordedCurves;
    private final boolean showAveragedIndentationCurves;
    private final boolean showAveragedPointwiseModulusCurves;

    private final ErrorBarType averagedCurvesBarType;

    //immutable class
    public SpectroscopyCurveAveragingSettings(boolean showAveragedRecordedCurves, boolean showAveragedIndentationCurves, boolean showAveragedPointwiseModulusCurves, ErrorBarType averagedCurvesBarType)
    {
        this.showAveragedRecordedCurves = showAveragedRecordedCurves;
        this.showAveragedIndentationCurves = showAveragedIndentationCurves;
        this.showAveragedPointwiseModulusCurves = showAveragedPointwiseModulusCurves;

        this.averagedCurvesBarType = Validation.requireNonNullParameterName(averagedCurvesBarType, "averagedCurvesBarType");
    }

    public int getNoOfPointInAveragedRecordedCurve()
    {
        return POINT_COUNT;
    }

    public boolean isShowAveragedRecordedCurves()
    {
        return showAveragedRecordedCurves;
    }

    public int getNoOfPointInAveragedIndentationCurve()
    {
        return POINT_COUNT;
    }

    public boolean isShowAveragedIndentationCurves()
    {
        return showAveragedIndentationCurves;
    }

    public int getNoOfPointInAveragedPointwiseModulusCurve()
    {
        return POINT_COUNT;
    }    

    public boolean isShowAveragedPointwiseModulusCurves()
    {
        return showAveragedPointwiseModulusCurves;
    }

    public ErrorBarType getAveragedCurvesBarType()
    {
        return averagedCurvesBarType;
    }
}
