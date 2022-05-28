package atomicJ.gui.rois;

import java.io.File;
import java.util.Map;

import atomicJ.data.Channel2D;
import atomicJ.data.QuantitativeSample;
import atomicJ.sources.ImageSource;


public interface ROISelectionResource 
{
    public Channel2D getSelectedChannel();
    public ImageSource getSelectedImage();
    public File getDefaultOutputLocation();
    public Map<Object, ROIDrawable> getROIs();
    public Map<String, String> getIdentifierUnitMap();
    public Map<String, Map<Object, QuantitativeSample>> getSamplesForROIs(boolean includeCoordinates);
    public Map<String, Map<Object, QuantitativeSample>>getSamples(boolean includeCoordinates);
    public Map<String, QuantitativeSample> getSamplesForROIUnion();
}
