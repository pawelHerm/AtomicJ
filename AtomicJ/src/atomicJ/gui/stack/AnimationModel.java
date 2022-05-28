package atomicJ.gui.stack;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import atomicJ.data.Channel2D;
import atomicJ.data.Coordinate4D;
import atomicJ.data.DataAxis1D;
import atomicJ.data.Grid1D;
import atomicJ.data.units.DimensionlessQuantity;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.SIPrefix;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.Units;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.selection.multiple.BasicMultipleSelectionModel;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.MultiMap;
import atomicJ.utilities.Validation;

public class AnimationModel extends AbstractModel implements PropertyChangeListener
{
    private static final String TASK_NAME = "Movie";
    private static final String TASK_DESCRIPTION = "<html>Specify how to create a movie</html>";

    public static final String STACK_START = "StackStart";
    public static final String STACK_END = "StackEnd";
    public static final String STACK_STEP_SIZE = "StackStepSize";

    public static final String STACKING_QUANTITY_NAME = "StackingQuantityName";
    public static final String STACKING_QUANTITY_PREFIX = "StackingQuantityPrefix";
    public static final String STACKING_QUANTITY_UNIT_NAME = "StackingQuantityUnitName";

    public static final String USER_QUANTITY_SPECIFICATION_ENABLED = "UserQuantitySpecificationEnabled";
    public static final String USE_DEFAULT_STACKING_QUATITY = "UseDefaultStackingQuantity";
    public static final String FINISH_ENABLED = "FinishEnabled";

    private final int frameCount;

    private double stackMinimum = Double.NaN;
    private double stackMaximum = Double.NaN;
    private double stepSize = Double.NaN;

    private String stackingQuantityName = "";
    private SIPrefix stackingQuantityPrefix = SIPrefix.u;
    private String stackingQuantityUnitName = "m";

    private final MultiMap<String, Channel2D> channelsToAnimate;
    private final MultiMap<String, DataAxis1D> defaultAxes;

    private final MetaMap<TypeCollection, Quantity, DataAxis1D> availableDefaultAxes;
    private final Map<TypeCollection, Quantity> selectedDefaultAxes;

    private boolean userQuantitySpecificationEnabled;
    private boolean useDefaultStackingQuantity = false;
    private boolean finishEnabled;

    private static final PrefixedUnit[] units = new PrefixedUnit[] {Units.METER_UNIT, Units.MILI_METER_UNIT, Units.MICRO_METER_UNIT,
            Units.NANO_METER_UNIT, Units.PICO_METER_UNIT};

    private static final String[] unitBareNames = new String[] {"", "m","V","N","Pa","g","s","A","Hz","J","eV",
            "W","C","F","deg","rad","Arb"};

    private final BasicMultipleSelectionModel<String> selectionModel;

    private boolean approved;

    public AnimationModel(int frameCount, MultiMap<String, Channel2D> channelsToAnimate)
    {
        Validation.requireNonNullAndNonEmptyParameterName(channelsToAnimate, "channelsToAnimate");

        List<String> types = new ArrayList<>(channelsToAnimate.keySet());

        this.selectionModel = new BasicMultipleSelectionModel<>(types, "Channels to animate");
        this.selectionModel.setSelected(types.iterator().next(), true);
        this.selectionModel.addPropertyChangeListener(this);

        this.channelsToAnimate = channelsToAnimate;
        this.frameCount = frameCount;
        this.defaultAxes = getDefaultAxesForAnimation(channelsToAnimate);
        this.availableDefaultAxes = buildAvailableDefaultAxes();
        this.selectedDefaultAxes = new LinkedHashMap<>();

        for(Entry<TypeCollection, Map<Quantity, DataAxis1D>> entry : availableDefaultAxes.entrySet())
        {
            selectedDefaultAxes.put(entry.getKey(), entry.getValue().keySet().iterator().next());
        }

        this.userQuantitySpecificationEnabled = calculateUserQuantitySpecificationEnabled();
        this.finishEnabled = checkIfFinishEnabled();
    }

    public boolean isUseDefaultStackingQuantity()
    {
        return useDefaultStackingQuantity;
    }

    public void setUseDefaultStackingQuantity(boolean useDefaultStackingQuantityNew)
    {
        boolean useDefaultStackingQuantityOld = this.useDefaultStackingQuantity;
        this.useDefaultStackingQuantity = useDefaultStackingQuantityNew;

        checkIfUserQuantitySpecificationEnabled();
        checkIfFinishEnabled();

        firePropertyChange(USE_DEFAULT_STACKING_QUATITY, useDefaultStackingQuantityOld, useDefaultStackingQuantityNew);
    }


    private MetaMap<TypeCollection, Quantity, DataAxis1D> buildAvailableDefaultAxes()
    {
        MetaMap<TypeCollection, Quantity, DataAxis1D> map = new MetaMap<>();

        if(defaultAxes.isEmpty())
        {
            return map;
        }       

        if(isDefaultAxesEqualForEachType())
        {
            String stringRepresentation = isDefaultAxesKnownForEachType() ? "All" : null;
            TypeCollection id = new TypeCollection(defaultAxes.keySet(), stringRepresentation);
            List<DataAxis1D> axes = defaultAxes.values().iterator().next();
            for(DataAxis1D axis : axes)
            {
                map.put(id, axis.getQuantity(),axis);
            }
        }
        else
        {
            for(Entry<String, List<DataAxis1D>> entry : defaultAxes.entrySet())
            {
                TypeCollection id = new TypeCollection(Collections.singleton(entry.getKey()));
                List<DataAxis1D> axes = entry.getValue();
                for(DataAxis1D axis : axes)
                {
                    map.put(id, axis.getQuantity(),axis);
                }
            }
        }

        return map;
    }

    private boolean isDefaultAxesEqualForEachType()
    {       
        Collection<List<DataAxis1D>> axes = defaultAxes.values();
        boolean allTheSame = ArrayUtilities.isConstant(axes);

        return allTheSame;
    }

    private boolean isDefaultAxesKnownForEachType()
    {
        Collection<String> typesWithAxes = defaultAxes.keySet();
        Collection<String> allTypes = channelsToAnimate.keySet();

        boolean knownForAll = Objects.equals(typesWithAxes, allTypes);

        return knownForAll;
    }

    public boolean isUserQuantitySpecificationEnabled()
    {
        return userQuantitySpecificationEnabled;
    }

    private void checkIfUserQuantitySpecificationEnabled()
    {
        boolean userQuantitySpecificationEnabledOld = this.userQuantitySpecificationEnabled;
        this.userQuantitySpecificationEnabled = calculateUserQuantitySpecificationEnabled();

        if(userQuantitySpecificationEnabledOld != this.userQuantitySpecificationEnabled)
        {
            firePropertyChange(USER_QUANTITY_SPECIFICATION_ENABLED, userQuantitySpecificationEnabledOld, this.userQuantitySpecificationEnabled);
        }
    }

    private boolean calculateUserQuantitySpecificationEnabled()
    {
        if(!useDefaultStackingQuantity)
        {
            return true;
        }

        List<String> selectedTypes = getSelectedStackTypes();
        for(String type : selectedTypes)
        {
            if(defaultAxes.isEmpty(type))
            {
                return true;
            }

        }

        return false;
    }

    public MultiMap<TypeCollection, Quantity> getAvailableDefaultQuantities()
    {
        MultiMap<TypeCollection, Quantity> quantities = new MultiMap<>();

        for(Entry<TypeCollection, Map<Quantity, DataAxis1D>> entry : availableDefaultAxes.entrySet())
        {
            quantities.putAll(entry.getKey(), new ArrayList<>(entry.getValue().keySet()));
        }

        return quantities;
    }

    public void setSelectedDefaultQuantity(TypeCollection types, Quantity axis)
    {
        selectedDefaultAxes.put(types, axis);
        checkIfFinishEnabled();
    }

    public Quantity getSelectedDefaultQuantity(TypeCollection types)
    {
        return selectedDefaultAxes.get(types);
    }

    public DataAxis1D getStackAxis(String type)
    {
        if(useDefaultStackingQuantity)
        {
            for(Entry<TypeCollection, Quantity> entry : selectedDefaultAxes.entrySet())
            {
                TypeCollection types = entry.getKey();
                if(types.contains(type))
                {
                    return availableDefaultAxes.get(types, entry.getValue());
                }
            }
        }

        return new Grid1D(stepSize, stackMinimum, frameCount, getStackingQuantity());
    }

    public String getTaskName()
    {
        return TASK_NAME;
    }

    public String getTaskDescription()
    {
        return TASK_DESCRIPTION;
    }

    public BasicMultipleSelectionModel<String> getSelectionModel()
    {
        return selectionModel;
    }

    public int getFrameCount()
    {
        return frameCount;
    }

    public boolean isApproved()
    {
        return approved;
    }

    public void finish()
    {
        this.approved = true;
    }

    public void cancel()
    {
        this.approved = false;
    }

    public List<String> getTypes()
    {
        return new ArrayList<>(channelsToAnimate.keySet());
    }

    private List<String> getSelectedStackTypes()
    {
        return new ArrayList<>(selectionModel.getSelectedKeys());
    }

    public MultiMap<String, Channel2D> getSelectedChannels()
    {
        MultiMap<String, Channel2D> selectedChannels = new MultiMap<>();

        Set<String> selectedKeys = selectionModel.getSelectedKeys();

        for(String type : selectedKeys)
        {
            selectedChannels.putAll(type, channelsToAnimate.get(type));
        }

        return selectedChannels;
    }

    public String getStackingQuantityName()
    {
        return stackingQuantityName;
    }

    public void setStackingQuantityName(String stackingQuantityNameNew)
    { 
        String stackingQuantityNameOld = this.stackingQuantityName;
        this.stackingQuantityName = stackingQuantityNameNew;

        firePropertyChange(STACKING_QUANTITY_NAME, stackingQuantityNameOld, stackingQuantityNameNew);
        checkIfFinishEnabled();
    }

    public SIPrefix getStackingQuantityPrefix()
    {
        return stackingQuantityPrefix;
    }

    public void setStackingQuantityPrefix(SIPrefix stackingQuantityPrefixNew)
    {
        SIPrefix stackingQuantityPrefixOld = this.stackingQuantityPrefix;
        this.stackingQuantityPrefix = stackingQuantityPrefixNew;

        firePropertyChange(STACKING_QUANTITY_PREFIX, stackingQuantityPrefixOld, stackingQuantityPrefixNew);
        checkIfFinishEnabled();
    }

    public String getStackingQuantityUnitName()
    {
        return stackingQuantityUnitName;
    }

    public void setStackingQuantityUnitName(String stackingQuantityUnitNameNew)
    {
        String stackingQuantityUnitNameOld = this.stackingQuantityUnitName;
        this.stackingQuantityUnitName = stackingQuantityUnitNameNew;

        firePropertyChange(STACKING_QUANTITY_UNIT_NAME, stackingQuantityUnitNameOld, stackingQuantityUnitNameNew);
        checkIfFinishEnabled();
    }

    public Quantity getStackingQuantity()
    {
        Quantity combinedZQuantity = stackingQuantityUnitName.isEmpty() ?new DimensionlessQuantity(stackingQuantityName, new SimplePrefixedUnit("",SIPrefix.Empty, 0)):new UnitQuantity(stackingQuantityName, new SimplePrefixedUnit(stackingQuantityUnitName, stackingQuantityPrefix));

        return combinedZQuantity;
    }

    public double getStackMinimum()
    {
        return stackMinimum;
    }

    public void setStackMinimum(double stackMinimumNew)
    {
        double stackMinimumOld = this.stackMinimum;
        this.stackMinimum = stackMinimumNew;

        firePropertyChange(STACK_START, stackMinimumOld, stackMinimumNew);

        checkIfFinishEnabled();
    }

    public void specifyStackMinimum(double stackMinimumNew)
    {
        setStackMinimum(stackMinimumNew);      
        setStackStepSize(calculateStepAnew());
    }

    public double getStackMaximum()
    {
        return stackMaximum;
    }

    private void setStackMaximum(double stackMaximumNew)
    {
        double stackMaximumOld = this.stackMaximum;
        this.stackMaximum = stackMaximumNew;

        firePropertyChange(STACK_END, stackMaximumOld, stackMaximumNew);

        checkIfFinishEnabled();
    }

    public void specifyStackMaximum(double stackMaximumNew)
    {
        setStackMaximum(stackMaximumNew);

        double stepNew = calculateStepAnew();
        setStackStepSize(stepNew);
    }

    public double getStackStepSize()
    {
        return stepSize;
    }

    public void specifyStackStepSize(double stepSizeNew)
    {
        setStackStepSize(stepSizeNew);
        setStackMaximum(calculateStackMaximumAnew());
    }

    private void setStackStepSize(double stepSizeNew)
    {
        double stepSizeOld = this.stepSize;
        this.stepSize = stepSizeNew;

        firePropertyChange(STACK_STEP_SIZE, stepSizeOld, stepSizeNew);

        checkIfFinishEnabled();
    }

    private double calculateStepAnew()
    {
        double step = (stackMaximum - stackMinimum)/(frameCount - 1.); 

        return step;
    }

    private double calculateStackMaximumAnew()
    {
        double stackMaximum = stepSize*(frameCount - 1) + stackMinimum;
        return stackMaximum;
    }

    public static PrefixedUnit[] getSIUnits()
    {
        return units;
    }

    public static String[] getZQuantityUnitNames()
    {
        return unitBareNames;
    }

    public boolean isFinishEnabled()
    {
        return finishEnabled;
    }

    private boolean checkIfFinishEnabled()
    {
        boolean finishEnabledNew = selectionModel.isFinishEnabled();

        if(useDefaultStackingQuantity)
        {
            for(String type : getSelectedStackTypes())
            {
                finishEnabledNew = defaultAxes.containsKey(type) ? (finishEnabledNew && (getStackAxis(type) != null)) : (finishEnabledNew && isUserDefinedAxisSpecified());
            }
        }
        else
        {
            finishEnabledNew = finishEnabledNew && isUserDefinedAxisSpecified();
        }

        boolean finishEnabledOld = this.finishEnabled;
        this.finishEnabled = finishEnabledNew;

        firePropertyChange(FINISH_ENABLED, finishEnabledOld, finishEnabledNew);

        return finishEnabledNew;
    }

    private boolean isUserDefinedAxisSpecified()
    {
        boolean specified = !Double.isNaN(stackMinimum) && !Double.isNaN(stackMaximum) 
                && stackingQuantityName != null && !stackingQuantityName.isEmpty();

        return specified;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if(BasicMultipleSelectionModel.KEY_SELECTION_CAN_BE_FINISHED.equals(property))
        {
            checkIfUserQuantitySpecificationEnabled();
            checkIfFinishEnabled();
        }
    }

    public static MultiMap<String, DataAxis1D> getDefaultAxesForAnimation(MultiMap<String, Channel2D> channelMap)
    {
        MultiMap<String, DataAxis1D> axes = new MultiMap<>();

        for(Entry<String, List<Channel2D>> entry : channelMap.entrySet())
        {
            String type = entry.getKey();
            List<Channel2D> channels = entry.getValue();
            List<Coordinate4D> coordinates = Coordinate4D.getCoordinates(channels);

            List<DataAxis1D> axesForType = Coordinate4D.getAxesWithoutTies(coordinates);
            axes.putAll(type, axesForType);
        }

        return axes;
    }

    public static class TypeCollection
    {
        private final String stringRepresentation;
        private final List<String> types;

        private TypeCollection(Collection<String> types)
        {
            this(types, null);
        }

        private TypeCollection(Collection<String> types, String stringRepresentation)
        {
            this.types = new ArrayList<>(types);
            this.stringRepresentation = stringRepresentation;
        }

        public boolean contains(Object element)
        {
            return types.contains(element);
        }

        private String buildDefaultString()
        {
            StringBuffer buff = new StringBuffer();

            for(String type : types)
            {
                buff.append(" ").append(type);
            }

            return buff.toString();
        }

        @Override
        public String toString()
        {
            String s = (stringRepresentation != null) ? stringRepresentation : buildDefaultString();

            return s;
        }

        @Override
        public int hashCode()
        {
            return types.hashCode();
        }

        @Override
        public boolean equals(Object other)
        {
            return types.equals(other);
        }

        public List<String> getTypes()
        {
            return Collections.unmodifiableList(types);
        }
    }
}
