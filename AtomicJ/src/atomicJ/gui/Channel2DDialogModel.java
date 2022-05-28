package atomicJ.gui;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.SampleCollection;
import atomicJ.data.StandardSampleCollection;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.stack.AnimationModel;
import atomicJ.resources.Channel2DResource;
import atomicJ.utilities.MultiMap;

public class Channel2DDialogModel<R extends Channel2DResource> extends ChannelResourceDialogModel<Channel2D,Channel2DData,String,R>
{     
    public Map<Object, ROI> getROIs()
    {
        R selectedResource = getSelectedResource();

        Map<Object, ROI> rois = new LinkedHashMap<>();
        if(selectedResource != null)
        {
            rois.putAll(selectedResource.getROIs());
        }

        return rois;
    }

    public Map<Object, ROI> getAvailableROIs()
    {
        Map<Object, ROI> rois = new LinkedHashMap<>();

        ROI roiUnion = getROIUnion();
        rois.put(roiUnion.getKey(), roiUnion);
        rois.putAll(getROIs());

        return rois;
    }

    public List<SampleCollection> getROIUnionSampleCollections()
    {
        R selectedResource = getSelectedResource();
        Map<String, QuantitativeSample> unionROISamples = selectedResource.getROIUnionSamples();

        SampleCollection collection = new StandardSampleCollection(unionROISamples, selectedResource.getShortName(), selectedResource.getShortName(), selectedResource.getDefaultOutputLocation());
        List<SampleCollection> sampleCollections = Collections.singletonList(collection);

        return sampleCollections;   
    }



    public MultiMap<String, Channel2D> getChannelsForAnimation()
    {
        MultiMap<String, Channel2D> multiMap = new MultiMap<>();

        for(R resource : getResources())
        {
            for(String type : resource.getAllTypes())
            {
                Collection<Channel2D> channels = resource.getChannels(type).values();

                for(Channel2D ch : channels)
                {
                    if(Objects.equals(ch.getIdentifier(), type))
                    {
                        multiMap.put(type, ch);
                    }
                }
            }
        }

        return multiMap;
    }

    public AnimationModel getAnimationModel()
    {           
        int frameCount = getResourceCount();

        MultiMap<String, Channel2D> allChannels = getChannelsForAnimation();

        AnimationModel model = new AnimationModel(frameCount, allChannels);  

        return model;
    }
}
