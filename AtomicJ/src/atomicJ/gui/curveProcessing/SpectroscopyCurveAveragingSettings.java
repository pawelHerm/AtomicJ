package atomicJ.gui.curveProcessing;

import atomicJ.curveProcessing.ErrorBarType;

public class SpectroscopyCurveAveragingSettings 
{
    public static final int DEFAULT_POINT_COUNT = 30;
    
    private boolean averagingEnabled;

    private final AveragingSettings recordedCurvesAveragingSettings;
    private final AveragingSettings indentationCurvesAveragingSettings;
    private final AveragingSettings pointwiseModulusCurvesAveragingSettings;

    //immutable class
    public SpectroscopyCurveAveragingSettings(boolean averagingEnabled, AveragingSettings recordedCurvesAveragingSettings, AveragingSettings indentationCurvesAveragingSettings, AveragingSettings pointwiseModulusCurvesAveragingSettings)
    {
        this.averagingEnabled = averagingEnabled;
        this.recordedCurvesAveragingSettings = recordedCurvesAveragingSettings;
        this.indentationCurvesAveragingSettings = indentationCurvesAveragingSettings;
        this.pointwiseModulusCurvesAveragingSettings = pointwiseModulusCurvesAveragingSettings;
    }

    public boolean isAveragingEnabled()
    {
        return averagingEnabled;
    }
    
    public AveragingSettings getRecordedCurveSettings()
    {
        return recordedCurvesAveragingSettings;
    }

    public AveragingSettings getIndentationSettings()
    {
        return indentationCurvesAveragingSettings;
    }

    public AveragingSettings getPointwiseModulusSettings()
    {
        return pointwiseModulusCurvesAveragingSettings;
    }

    public static class AveragingSettings
    {
        private final int pointCount;
        private final ErrorBarType barType;
        private final boolean show;

        public AveragingSettings(int pointCount, ErrorBarType barType, boolean show)
        {
            this.pointCount = pointCount;
            this.barType = barType;
            this.show = show;
        }

        public int getPointCount()
        {
            return pointCount;
        }

        public ErrorBarType getErrorBarType()
        {
            return barType;
        }

        public boolean isShown()
        {
            return show;
        }
    }
}
