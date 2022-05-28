package atomicJ.gui.rois.line;

import java.io.Serializable;

import atomicJ.gui.profile.ProfileStyle;


public interface ROICurveSerializationProxy extends Serializable
{
    public ROICurve recreateOriginalObject(ProfileStyle style, Integer key);
}
