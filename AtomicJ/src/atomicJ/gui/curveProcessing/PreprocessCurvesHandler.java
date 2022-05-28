package atomicJ.gui.curveProcessing;

import java.util.List;

import atomicJ.sources.SimpleSpectroscopySource;

public interface PreprocessCurvesHandler 
{
    public void preprocess(List<SimpleSpectroscopySource> sources);
}
