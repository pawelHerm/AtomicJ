package atomicJ.gui.imageProcessing;

import java.util.ArrayList;
import java.util.List;

public class KernelElementValueEvent 
{
    private final Object source;

    private final List<KernelElementChange> elementChanges = new ArrayList<>();

    public KernelElementValueEvent(Object source, KernelElementChange elementChanges)
    {
        this.source = source;

        this.elementChanges.add(elementChanges);
    }

    public KernelElementValueEvent(Object source, List<KernelElementChange> elementChanges)
    {
        this.source = source;

        this.elementChanges.addAll(elementChanges);
    }

    public Object getSource()
    {
        return source;
    }

    public List<KernelElementChange> getElementChanges()
    {
        List<KernelElementChange> copy = new ArrayList<>(elementChanges);

        return copy;
    }

    public static class KernelElementChange
    {
        private final int row;
        private final int column;
        private final double valueOld;
        private final double valueNew;

        public KernelElementChange(int row, int column, double valueOld, double valueNew)
        {
            this.row = row;
            this.column = column;
            this.valueOld = valueOld;
            this.valueNew = valueNew;
        }

        public int getRow()
        {
            return row;
        }

        public int getColumn()
        {
            return column;
        }

        public double getValueOld()
        {
            return valueOld;
        }

        public double getValueNew()
        {
            return valueNew;
        }
    }
}
