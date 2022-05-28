package atomicJ.gui.curveProcessing;

import java.awt.Window;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import atomicJ.analysis.PreviewDestination;
import atomicJ.gui.ConcurrentPreviewTask;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.Channel2DChart;
import atomicJ.gui.SpectroscopyView;
import atomicJ.resources.SpectroscopyBasicResource;
import atomicJ.resources.StandardChannel2DResource;
import atomicJ.sources.SimpleSpectroscopySource;

public class PreprocessCurvesStandardHandle implements PreprocessCurvesHandler, PreviewDestination
{
    private final SpectroscopyView spectroscopyDialog;

    public PreprocessCurvesStandardHandle(SpectroscopyView spectroscopyDialog)
    {
        this.spectroscopyDialog = spectroscopyDialog;
    }

    @Override
    public void preprocess(List<SimpleSpectroscopySource> sources)
    {
        if(!sources.isEmpty())
        {               
            ConcurrentPreviewTask task = new ConcurrentPreviewTask(sources, this);          
            task.execute();
        }       
        else
        {
            JOptionPane.showMessageDialog(null, "No file to preprocess", "", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public Window getPublicationSite() 
    {
        return spectroscopyDialog.getAssociatedWindow();
    }

    @Override
    public void publishPreviewData(Map<SpectroscopyBasicResource, Map<String, ChannelChart<?>>> charts) {

        if(!charts.isEmpty())
        {
            spectroscopyDialog.addResources(charts);
            spectroscopyDialog.selectResource(0);
            spectroscopyDialog.setVisible(true);
        }
    }

    @Override
    public void publishPreviewed2DData(
            Map<StandardChannel2DResource, Map<String, Channel2DChart<?>>> chartMaps) {
        // TODO Auto-generated method stub

    }

    @Override
    public void requestPreviewEnd() {
        // TODO Auto-generated method stub

    }
}
