package atomicJ.gui.profile;

import java.util.List;
import java.util.Map;


public interface ProfileReceiver 
{    
    public void setProfiles(Map<Object, Profile> profiles);
    public void addOrReplaceProfile(Profile profile);
    public void removeProfile(Profile profile);

    public void addProfileKnob(Object profileKey, double knobPosition);
    public void moveProfileKnob(Object profileKey, int knobIndex, double knobPositionNew);
    public void removeProfileKnob(Object profileKey, double knobPosition);
    public void setProfileKnobPositions(Object resource, Object profileKey, List<Double> knobPositions);
}
