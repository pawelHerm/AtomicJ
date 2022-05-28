package atomicJ.gui;

import atomicJ.analysis.PreviewDestination;
import atomicJ.gui.experimental.ExperimentalWizard;
import atomicJ.gui.rois.ROIWizardReceiver;

public class TopographySelectionWizard extends ExperimentalWizard
{
    private static final long serialVersionUID = 1L;

    public TopographySelectionWizard(String title, PreviewDestination parent, ROIWizardReceiver roiImageReceiver)
    {
        super(title, parent, roiImageReceiver, new TopographySelectionWizardModel(parent, roiImageReceiver));
    }
}
