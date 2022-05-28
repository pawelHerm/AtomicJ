package atomicJ.gui.histogram;

import java.util.ArrayList;
import java.util.List;

import atomicJ.data.SampleCollection;
import atomicJ.gui.selection.multiple.CompositeSelectionWizardPageModel;
import atomicJ.gui.selection.multiple.MultipleSelectionWizardPageModel;
import atomicJ.gui.selection.multiple.SampleCollectionSelectionModel;
import atomicJ.gui.selection.multiple.SampleSelectionModel;

public class HistogramWizardModelDoubleSelection extends HistogramWizardModel
{ 
    private final CompositeSelectionWizardPageModel<String> selectionModel;

    public HistogramWizardModelDoubleSelection(HistogramDestination destination,List<SampleCollection> sampleCollections)
    {
        this(destination, sampleCollections, true);
    }

    public HistogramWizardModelDoubleSelection(HistogramDestination destination,List<SampleCollection> sampleCollections,
            boolean includeCollectionNameInTask)
    {
        super(destination, sampleCollections, includeCollectionNameInTask);

        this.selectionModel = buildSampleSelectionModel(sampleCollections);
        setCurrentPageModel(selectionModel);
    }

    private CompositeSelectionWizardPageModel<String> buildSampleSelectionModel(List<SampleCollection> sampleCollections)
    {
        List<MultipleSelectionWizardPageModel<String>> models = new ArrayList<>();

        models.add(new SampleCollectionSelectionModel(sampleCollections, "<html>Which datasets would you like to plot?</html>", false, false));
        models.add(new SampleSelectionModel(sampleCollections, "<html>Which datasets would you like to plot?</html>", true, false));

        return new CompositeSelectionWizardPageModel<>(models);
    }

    @Override
    public MultipleSelectionWizardPageModel<String> getSelectionModel()
    {
        return selectionModel;
    }
}
