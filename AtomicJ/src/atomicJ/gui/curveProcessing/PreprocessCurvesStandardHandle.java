package atomicJ.gui.curveProcessing;

import java.awt.Window;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    private final SpectroscopyView spectroscopyView;

    public PreprocessCurvesStandardHandle(SpectroscopyView spectroscopyView)
    {
        this.spectroscopyView = spectroscopyView;
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
        return spectroscopyView.getAssociatedWindow();
    }

    @Override
    public void publishPreviewData(Map<SpectroscopyBasicResource, Map<String, ChannelChart<?>>> charts) 
    {             
        if(!charts.isEmpty())
        {
            int index = spectroscopyView.getResourceCount();
            
            spectroscopyView.addResources(charts);
            spectroscopyView.selectResource(index);
            spectroscopyView.setVisible(true);    
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
