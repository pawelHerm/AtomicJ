package atomicJ.gui.save;

import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.commons.compress.compressors.CompressorStreamFactory;

public enum ArchiveType 
{
    ZIP("ZIP", "zip") 
    {
        @Override
        public SwingWorker<?, ?> getSavingTask(List<StreamSavable> savables, File path, Component parent) 
        {
            return ZipSavingTaskApache.getSavingTask(savables, path, parent);
        }
    },
    SEVEN_Z("7z", "7z") 
    {
        @Override
        public SwingWorker<?, ?> getSavingTask(List<StreamSavable> savables, File path, Component parent) 
        {
            return SevenZSavingTask.getSavingTask(savables, path, parent);
        }
    }, 
    TAR("tar", "tar") 
    {
        @Override
        public SwingWorker<?, ?> getSavingTask(List<StreamSavable> savables, File path, Component parent) {
            return TarSavingTask.getSavingTask(savables, path, parent);
        }
    }, 
    TAR_GZIP("tar.gz", "tar.gz") 
    {
        @Override
        public SwingWorker<?, ?> getSavingTask(List<StreamSavable> savables, File path, Component parent) 
        {
            return TarBallSavingTask.getSavingTask(CompressorStreamFactory.GZIP, savables, path, parent);
        }
    }, 
    TAR_BZIP2("tar.bz2", "tar.bz2") 
    {
        @Override
        public SwingWorker<?, ?> getSavingTask(List<StreamSavable> savables, File path, Component parent)
        {
            return TarBallSavingTask.getSavingTask(CompressorStreamFactory.BZIP2, savables, path, parent);
        }
    },
    TAR_XZ("tar.xz", "tar.xz") 
    {
        @Override
        public SwingWorker<?, ?> getSavingTask(List<StreamSavable> savables, File path, Component parent)
        {
            return TarBallSavingTask.getSavingTask(CompressorStreamFactory.XZ, savables, path, parent);
        }
    };

    private final String prettyName;
    private final String extension;

    private ArchiveType(String prettyName, String extension)
    {
        this.prettyName = prettyName;
        this.extension = extension;
    }

    public String getExtension()
    {
        return extension;
    }

    public abstract SwingWorker<?, ?> getSavingTask(List<StreamSavable> savables, File path, Component parent);


    @Override
    public String toString()
    {
        return prettyName;
    }
}
