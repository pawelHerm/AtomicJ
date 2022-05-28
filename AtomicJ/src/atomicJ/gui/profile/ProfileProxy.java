package atomicJ.gui.profile;

import java.io.Serializable;


public interface ProfileProxy extends Serializable
{
    public Profile recreateOriginalObject(ProfileStyle style, Integer key);
}
