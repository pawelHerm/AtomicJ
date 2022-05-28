package atomicJ.gui.save;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class RandomAccessFileOutputStream extends OutputStream 
{
    private final RandomAccessFile randomAcessFile;

    public RandomAccessFileOutputStream(RandomAccessFile randomAcessFile) 
    {
        this.randomAcessFile = randomAcessFile;
    }

    @Override
    public void write (int b) throws IOException 
    {
        randomAcessFile.writeByte(b); 
    }
    @Override
    public void write (byte[] b) throws IOException 
    {
        randomAcessFile.write(b);
    }
    @Override
    public void write (byte[] b, int off, int len) throws IOException 
    {
        randomAcessFile.write(b, off, len);
    }
}