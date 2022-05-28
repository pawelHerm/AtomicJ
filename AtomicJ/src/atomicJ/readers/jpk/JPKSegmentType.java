package atomicJ.readers.jpk;

import atomicJ.analysis.SortedArrayOrder;

public enum JPKSegmentType
{
    extend(SortedArrayOrder.DESCENDING), retract(SortedArrayOrder.ASCENDING), pause(null);

    private final SortedArrayOrder order;

    JPKSegmentType(SortedArrayOrder order)
    {
        this.order = order;
    }

    public SortedArrayOrder getDefaultXOrder()
    {
        return order;
    }
}