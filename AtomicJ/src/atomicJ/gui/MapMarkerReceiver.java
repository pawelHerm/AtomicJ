package atomicJ.gui;

import java.util.Map;

public interface MapMarkerReceiver 
{    
    public void setMapMarkers(Map<Object, MapMarker> mapMarkers);
    public void addOrReplaceMapMarker(MapMarker mapMarker);
    public void removeMapMarker(MapMarker mapMarker);
}
