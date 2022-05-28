package atomicJ.gui;

import java.awt.Cursor;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.plot.PlotOrientation;

import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.profile.KnobSpecification;
import atomicJ.gui.profile.Profile;
import atomicJ.gui.profile.ProfileFreeHand;
import atomicJ.gui.profile.ProfileLine;
import atomicJ.gui.profile.AnnotationModificationOperation;
import atomicJ.gui.profile.ProfilePolyLine;

class ProfileManager implements MouseInputResponse
{
    private final Channel2DChart<?> densityChart;

    private final Map<Object, Profile> profiles = new LinkedHashMap<>();
    private Profile profileUnderConstruction;   
    private int currentProfileIndex = 1;

    private KnobSpecification caughtProfileKnob;
    private Profile caughtProfile;
    private AnnotationModificationOperation currentModificationOperation;
    private boolean profilesVisible = false;

    ProfileManager(Channel2DChart<?> densityChart) {
        this.densityChart = densityChart;
    }

    @Override
    public void mousePressed(CustomChartMouseEvent event) 
    {
        if(!this.densityChart.isProfileMode(MouseInputType.PRESSED))
        {
            return;
        }

        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();
        Rectangle2D dataArea = event.getDataRectangle(0.005);  

        Profile clickedProfile = getProfileForPoint(dataPoint);
        boolean isEmptySpace = (clickedProfile == null); 

        if(isEmptySpace && (profileUnderConstruction != null))
        {
            profileUnderConstruction.mousePressedDuringConstruction(dataPoint.getX(), dataPoint.getY(), event.getModifierKeys());
        }

        caughtProfileKnob = getProfileKnob(java2DPoint);

        if(caughtProfileKnob == null && !isProfileUnderConstruction())
        {           
            currentModificationOperation = null;
            caughtProfile = null;

            ListIterator<Profile> it = new ArrayList<>(profiles.values()).listIterator(profiles.size());
            while(it.hasPrevious())
            {           
                Profile profile = it.previous();
                AnnotationAnchorSigned anchor = profile.getCaughtAnchor(java2DPoint, dataPoint, dataArea);
                if(anchor != null)
                {
                    caughtProfile = profile;
                    currentModificationOperation = new AnnotationModificationOperation(anchor, dataPoint, dataPoint);

                    requestCursorCompatibleWithAnchor(anchor);
                    break;
                }           
            }     
        }        
    }

    private void requestCursorCompatibleWithAnchor(AnnotationAnchorSigned anchor)
    {
        PlotOrientation orientation = this.densityChart.getCustomizablePlot().getOrientation();
        boolean isVertical = (orientation == PlotOrientation.VERTICAL);
        Cursor cursor = anchor.getCoreAnchor().getCursor(isVertical);
        this.densityChart.supervisor.requestCursorChange(cursor);
    }

    @Override
    public void mouseReleased(CustomChartMouseEvent event) 
    {
        if(!this.densityChart.isProfileMode(MouseInputType.RELEASED))
        {
            return;
        }

        this.currentModificationOperation = null;
        this.caughtProfileKnob = null;
        this.caughtProfile = null;
    }

    @Override
    public void mouseDragged(CustomChartMouseEvent event) 
    {
        if(!this.densityChart.isProfileMode(MouseInputType.DRAGGED))
        {
            return;
        }

        if(event.isConsumed(CustomChartMouseEvent.MOUSE_DRAGGED_CONSUMED))
        {
            return;
        }

        Point2D dataPoint = event.getDataPoint();

        if(caughtProfileKnob != null)
        {
            caughtProfileKnobResponseToMouseDragged(dataPoint);
        }
        else if(caughtProfile != null)
        {                
            this.currentModificationOperation = caughtProfile.setPosition(currentModificationOperation.getAnchor(), currentModificationOperation.getPressedPoint(), currentModificationOperation.getEndPoint(), dataPoint, event.getModifierKeys());
            this.densityChart.supervisor.addOrReplaceProfile(caughtProfile); 
        }    
    }

    @Override
    public void mouseMoved(CustomChartMouseEvent event) 
    {        
        if(!this.densityChart.isProfileMode(MouseInputType.MOVED))
        {
            return;
        }

        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();
        Rectangle2D dataArea = event.getDataRectangle(0.005);  

        KnobSpecification profileKnob = getProfileKnob(java2DPoint);

        if(profileKnob != null)
        {
            this.densityChart.supervisor.requestCursorChange(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        else
        {
            this.densityChart.supervisor.requestCursorChange(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

            if(isProfileUnderConstruction())
            {
                profileUnderConstruction.mouseMovedDuringConstruction(dataPoint.getX(), dataPoint.getY(), event.getModifierKeys());  
            }
            else 
            {       
                if(event.isConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED))
                {
                    return;
                }

                ListIterator<Profile> it = new ArrayList<>(profiles.values()).listIterator(profiles.size());
                while(it.hasPrevious())
                {
                    Profile profile = it.previous();
                    AnnotationAnchorSigned anchor = profile.getCaughtAnchor(java2DPoint, dataPoint, dataArea);
                    if(anchor != null)
                    {                             
                        requestCursorCompatibleWithAnchor(anchor);
                        event.setConsumed(CustomChartMouseEvent.CURSOR_CHANGE_CONSUMED, true);
                        break;
                    }           
                }
            }
        }      
    }

    @Override
    public void mouseClicked(CustomChartMouseEvent event) 
    {
        if(!this.densityChart.isProfileMode(MouseInputType.CLICKED))
        {
            return;
        }

        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();

        KnobSpecification knob = getProfileKnob(java2DPoint);
        boolean knobCaught = (knob != null);

        if(event.isMultiple())
        {
            if(knobCaught)
            {
                this.densityChart.supervisor.removeProfileKnob(knob.getKey(), knob.getPosition());
            }
            else
            {
                removeProfile(dataPoint);
            }
        }
        else if(!knobCaught)
        {
            if(event.isLeft())
            {                           
                handleLeftSingleClick(event);
            }
            else 
            {    
                handleRightSingleClick(event);  
            }
        } 
    }

    private void handleLeftSingleClick(CustomChartMouseEvent event)
    {
        Point2D java2DPoint = event.getJava2DPoint();
        Point2D dataPoint = event.getDataPoint();
        Rectangle2D dataArea = event.getDataRectangle(0.005);  

        Profile clickedProfile = getProfileForPoint(dataPoint);
        boolean isEmptySpace = (clickedProfile == null);  

        if(isEmptySpace)
        {
            if(!isComplexProfileUnderConstruction())
            {
                if(!isProfileUnderConstruction())
                {
                    beginNewProfile(dataPoint);
                }
                else 
                {
                    finishProfile();
                } 
            }
        }
        else
        {
            boolean isHighlighted = clickedProfile.isHighlighted();

            Set<ModifierKey> modifierKeys = event.getModifierKeys();

            boolean reshaped = clickedProfile.reshapeInResponseToMouseClick(modifierKeys, java2DPoint, dataPoint, dataArea);

            if(reshaped)
            {
                this.densityChart.supervisor.addOrReplaceProfile(clickedProfile);
            }
            else
            {
                clickedProfile.setHighlighted(!isHighlighted);
            }           
        }
    }

    private void handleRightSingleClick(CustomChartMouseEvent event)
    {
        Point2D dataPoint = event.getDataPoint();

        Profile clickedProfile = getProfileForPoint(dataPoint);
        boolean isEmptySpace = (clickedProfile == null); 

        if(!isEmptySpace && clickedProfile.isFinished())
        {    
            this.densityChart.supervisor.addProfileKnob(clickedProfile.getKey(),clickedProfile.getCorrespondingKnobPosition(dataPoint));
        }       
        else if(isProfileUnderConstruction())
        {
            finishProfile();
        }
    }

    @Override
    public boolean isRightClickReserved(Rectangle2D dataRectangle, Point2D dataPoint)
    {
        boolean reserved = (getProfileForPoint(dataPoint) != null);

        if(isProfileUnderConstruction())
        {
            reserved = reserved || profileUnderConstruction.isBoundaryClicked(dataRectangle);
        }

        return reserved;
    }


    @Override
    public boolean isChartElementCaught() 
    {
        boolean caught = (caughtProfile != null) || (caughtProfileKnob != null);
        return caught;
    }

    public void removeProfile(Profile profile)
    {
        Object key = profile.getKey();
        Profile oldProfile = profiles.remove(key);

        if(oldProfile != null)
        {
            Channel2DPlot plot = this.densityChart.getCustomizablePlot();
            plot.removeProfile(oldProfile);
        }
    }

    private void beginNewProfile(Point2D anchor)
    {      
        if(this.densityChart.getMode().equals(MouseInputModeStandard.PROFILE_LINE))
        {
            profileUnderConstruction = new ProfileLine(anchor, anchor, currentProfileIndex, this.densityChart.profileStyle);

            Channel2DPlot plot = this.densityChart.getCustomizablePlot();
            plot.addOrReplaceProfile(profileUnderConstruction);
        }
        else if(this.densityChart.getMode().equals(MouseInputModeStandard.PROFILE_POLYLINE))
        {
            profileUnderConstruction = new ProfilePolyLine(anchor, currentProfileIndex, this.densityChart.profileStyle);

            Channel2DPlot plot = this.densityChart.getCustomizablePlot();
            plot.addOrReplaceProfile(profileUnderConstruction);
        }
        else if(this.densityChart.getMode().equals(MouseInputModeStandard.PROFILE_FREEHAND))
        {
            profileUnderConstruction = new ProfileFreeHand(anchor, currentProfileIndex, this.densityChart.profileStyle);

            Channel2DPlot plot = this.densityChart.getCustomizablePlot();
            plot.addOrReplaceProfile(profileUnderConstruction);
        }
    }

    private void finishProfile()
    {   
        if(isProfileUnderConstruction())
        {
            this.profileUnderConstruction.setFinished(true);                         
            this.densityChart.supervisor.addOrReplaceProfile(profileUnderConstruction);           
            this.profileUnderConstruction = null;
        }   
    }

    public Profile getProfileForPoint(Point2D p)
    {
        Rectangle2D dataArea = this.densityChart.getDataSquare(p, 0.005);  

        for(Profile profile: profiles.values())
        {       
            boolean isClicked = profile.isClicked(dataArea);
            if(isClicked)
            {
                return profile;
            }
        }

        return null;        
    }

    public boolean isProfileUnderConstruction()
    {
        return profileUnderConstruction != null;
    }

    private boolean isComplexProfileUnderConstruction()
    {
        return profileUnderConstruction != null && profileUnderConstruction.isComplex();
    }

    public boolean isComplexElementUnderConstruction()
    {
        return isComplexProfileUnderConstruction();
    }

    private void removeProfile(Point2D p)
    {
        Profile clickedProfile = getProfileForPoint(p);
        if(clickedProfile != null)
        {
            this.densityChart.supervisor.removeProfile(clickedProfile);
        }       
    }

    public void setProfiles(Map<Object, Profile> profilesNew)
    {       
        if(!profiles.equals(profilesNew))
        {
            Channel2DPlot plot = this.densityChart.getCustomizablePlot();

            for(Profile oldProfile: profiles.values())
            {
                plot.removeProfile(oldProfile, false);
            }

            profiles.clear();

            for(Profile newProfile: profilesNew.values())
            {
                Profile profileCopy = newProfile.copy(this.densityChart.profileStyle);
                profiles.put(profileCopy.getKey(), profileCopy);

                plot.addOrReplaceProfile(profileCopy);

                currentProfileIndex = Math.max(currentProfileIndex, newProfile.getKey());
                currentProfileIndex++;
            }

            this.densityChart.fireChartChanged();
        }
    }

    public void addOrReplaceProfile(Profile profile)
    {
        Object key = profile.getKey();
        Profile oldProfile = profiles.get(key);

        if(oldProfile != null)
        {
            Channel2DPlot plot = this.densityChart.getCustomizablePlot();
            plot.removeProfile(oldProfile);
        }
        else
        {
            currentProfileIndex = Math.max(currentProfileIndex, profile.getKey());
            currentProfileIndex++;
        }
        Profile profileCopy = profile.copy(this.densityChart.profileStyle);
        profiles.put(key, profileCopy);

        Channel2DPlot plot = this.densityChart.getCustomizablePlot();
        plot.addOrReplaceProfile(profileCopy);
    }


    public void setProfilesVisible(boolean visibleNew)
    {
        if(profilesVisible != visibleNew)
        {
            profilesVisible = visibleNew;           
            for(Profile profile: profiles.values())
            {
                profile.setVisible(profilesVisible);
            }
        }
    }

    public void setProfileKnobPositions(Object profileKey, List<Double> knobrPositions)
    {
        Profile profile = profiles.get(profileKey);

        if(profile != null)
        {
            profile.setKnobPositions(knobrPositions);
        }
    }

    public void moveProfileKnob(Object profileKey, int knobIndex, double knobNewPosition)
    {
        Profile profile = profiles.get(profileKey);

        if(profile != null)
        {
            profile.moveKnob(knobIndex, knobNewPosition);
        }
    }

    public void addProfileKnob(Object profileKey, double knobPosition)
    {
        Profile profile = profiles.get(profileKey);

        if(profile != null)
        {
            profile.addKnob(knobPosition);
        }
    }

    public void removeProfileKnob(Object profileKey, double knobPosition)
    {
        //think what with caught marker. It should be updated, although
        //not updating it is unlikely to cause any problems

        Profile profile = profiles.get(profileKey);

        if(profile != null)
        {
            profile.removeKnob(knobPosition);
        }
    }

    private void caughtProfileKnobResponseToMouseDragged(Point2D dataPoint)
    {
        if(caughtProfileKnob != null)
        {
            int knobIndex = caughtProfileKnob.getKnobIndex();
            Object profileKey = caughtProfileKnob.getKey();
            Profile profile = profiles.get(profileKey);

            double knobNewPosition = profile.getCorrespondingKnobPosition(dataPoint);

            this.densityChart.supervisor.moveProfileKnob(profileKey, knobIndex, knobNewPosition);
        }
    }

    public int getProfileCount()
    {
        return profiles.size();
    }

    public int getCurrentProfileIndex()
    {
        return currentProfileIndex;
    }

    private KnobSpecification getProfileKnob(Point2D java2DPoint)
    {
        KnobSpecification knobSpecification = null;
        for(Profile profile : profiles.values())
        {
            knobSpecification = profile.getCaughtKnob(java2DPoint);
            if(knobSpecification != null)
            {
                break;
            }                   
        }

        return knobSpecification;
    }
}