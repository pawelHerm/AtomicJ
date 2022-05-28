package atomicJ.gui.results;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataTypeModel 
{
    private final List<DataType> dataTypes = new ArrayList<>();

    public DataTypeModel(DataType... dataTypes)
    {
        this.dataTypes.addAll(Arrays.asList(dataTypes));
    }
}
