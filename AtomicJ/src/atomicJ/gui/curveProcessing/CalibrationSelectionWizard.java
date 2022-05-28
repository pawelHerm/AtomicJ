package atomicJ.gui.curveProcessing;

import atomicJ.analysis.PreviewDestination;
import atomicJ.gui.experimental.ExperimentalWizard;
import atomicJ.gui.rois.ROIWizardReceiver;

public class CalibrationSelectionWizard extends ExperimentalWizard
{
    private static final long serialVersionUID = 1L;

    public CalibrationSelectionWizard(String title, PreviewDestination parent, ROIWizardReceiver roiImageReceiver)
    {
        super(title, parent, roiImageReceiver, new CalibrationSelectionWizardModel(parent, roiImageReceiver));
    }
}
