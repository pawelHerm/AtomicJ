package atomicJ.gui.histogram;

import java.util.List;

import atomicJ.data.SampleCollection;
import atomicJ.gui.selection.multiple.MultipleSelectionWizardPageModel;
import atomicJ.gui.selection.multiple.SampleSelectionModel;

public class HistogramWizardModelSingleSelection extends HistogramWizardModel
{ 
    private final SampleSelectionModel selectionModel;

    public HistogramWizardModelSingleSelection(HistogramDestination destination,List<SampleCollection> sampleCollections)
    {
        this(destination, sampleCollections, true);
    }

    public HistogramWizardModelSingleSelection(HistogramDestination destination,List<SampleCollection> sampleCollections,
            boolean includeCollectionNameInTask)
    {
        super(destination, sampleCollections, includeCollectionNameInTask);

        this.selectionModel = buildSampleSelectionModel(sampleCollections);
        setCurrentPageModel(selectionModel);

    }
    private SampleSelectionModel buildSampleSelectionModel(List<SampleCollection> sampleCollections)
    {
        return new SampleSelectionModel(sampleCollections, "<html>Which datasets would you like to plot?</html>", true, false);
    }

    @Override
    public MultipleSelectionWizardPageModel<String> getSelectionModel()
    {
        return selectionModel;
    }
}
