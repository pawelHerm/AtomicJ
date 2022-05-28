package atomicJ.readers.mdt;

import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.DataStorageDirection;
import atomicJ.readers.ArrayStorageType;

public enum HybridScanDirection
{

    //T - DataStorageDirection.REVERSED
    //B - DataStorageDirection.STRAIGHT

    HLT(0, ArrayStorageType.ROW_BY_ROW, DataStorageDirection.STRAIGHT, DataStorageDirection.REVERSED), 
    HLB(1, ArrayStorageType.ROW_BY_ROW, DataStorageDirection.STRAIGHT, DataStorageDirection.STRAIGHT),
    HRT(2, ArrayStorageType.ROW_BY_ROW, DataStorageDirection.REVERSED, DataStorageDirection.REVERSED),
    HRB(3, ArrayStorageType.ROW_BY_ROW, DataStorageDirection.REVERSED, DataStorageDirection.STRAIGHT),
    VLT(4, ArrayStorageType.COLUMN_BY_COLUMN, DataStorageDirection.STRAIGHT, DataStorageDirection.REVERSED), 
    VLB(5, ArrayStorageType.COLUMN_BY_COLUMN, DataStorageDirection.STRAIGHT, DataStorageDirection.STRAIGHT), 
    VRT(6, ArrayStorageType.COLUMN_BY_COLUMN, DataStorageDirection.REVERSED, DataStorageDirection.REVERSED), 
    VRB(7, ArrayStorageType.COLUMN_BY_COLUMN, DataStorageDirection.REVERSED, DataStorageDirection.STRAIGHT);

    private final int code;
    private final ArrayStorageType dataStorageType;
    private final DataStorageDirection rowStorageDirection;
    private final DataStorageDirection columnStorageDirection;

    private HybridScanDirection(int code, ArrayStorageType dataStorageType, DataStorageDirection rowStorageDirection, DataStorageDirection columnStorageDirection)
    {
        this.code = code;
        this.dataStorageType = dataStorageType;
        this.rowStorageDirection = rowStorageDirection;
        this.columnStorageDirection = columnStorageDirection;
    }

    public ArrayStorageType getStorageType()
    {
        return dataStorageType;
    }

    public DataStorageDirection getBetweenVectorsDirection()
    {
        DataStorageDirection storageDirection = ArrayStorageType.ROW_BY_ROW.equals(dataStorageType) ? columnStorageDirection : rowStorageDirection;
        return storageDirection;
    }

    public DataStorageDirection getiInsideVectorDirection()
    {
        DataStorageDirection storageDirection = ArrayStorageType.ROW_BY_ROW.equals(dataStorageType) ? rowStorageDirection : columnStorageDirection;
        return storageDirection;
    }

    public static HybridScanDirection getHybriScanDirection(int code) throws UserCommunicableException
    {
        for(HybridScanDirection scanDirection : HybridScanDirection.values())
        {
            if(scanDirection.code == code)
            {
                return scanDirection;
            }
        }

        throw new UserCommunicableException("No scan direction known for code " + code);
    }
}