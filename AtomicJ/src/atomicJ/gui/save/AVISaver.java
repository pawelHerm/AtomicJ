package atomicJ.gui.save;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;

import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.Channel2DChart;
import atomicJ.utilities.IOUtilities;



public class AVISaver extends BasicImageFormatSaver implements ZippableFrameFormatSaver
{

    private final AVICompression  biCompression;  //compression type (0, 'JPEG, 'PNG')
    private final int firstFrame;
    private final int lastFrame;
    private final int frameRate;
    private final boolean reverse;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private static final String EXT = ".avi";


    public AVISaver(Rectangle2D chartInitialArea, int width, int height, int firstFrame, int lastFrame, int frameRate, boolean reverse, boolean saveDataArea, AVICompression compression, int jpegQuality)
    {
        super(chartInitialArea, width, height, saveDataArea);
        this.biCompression = compression;
        this.firstFrame = firstFrame;
        this.lastFrame = lastFrame;
        this.frameRate = frameRate;
        this.reverse = reverse;
    }

    @Override
    public void saveAsZip(final JFreeChart freeChart, final File path, final String entryName, ChartRenderingInfo info)
    {
        if(!(freeChart instanceof Channel2DChart<?>))
        {
            return;
        }

        final File tempFile;
        try {
            tempFile = File.createTempFile("stackTempAtomicJ", EXT);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return;
        }
        tempFile.deleteOnExit();

        Channel2DChart<?> densityChart = (Channel2DChart<?>)freeChart;

        AVISavingTask task = new AVISavingTask(AtomicJ.getApplicationFrame(), densityChart, 
                tempFile, biCompression, firstFrame, lastFrame, frameRate, reverse);


        PropertyChangeListener taskStateListener = new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                if(SwingWorker.StateValue.DONE.equals(evt.getNewValue())) 
                {
                    try
                    {
                        IOUtilities.zip(path, tempFile, entryName);     
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    finally
                    {
                        tempFile.delete();
                    }
                }
            }          
        };

        task.addPropertyChangeListener("state", taskStateListener);
        task.execute();

    }



    /** Writes an stack as AVI file. 
     * @throws InterruptedException */
    @Override
    public void saveChart(JFreeChart freeChart, File path, ChartRenderingInfo info)
            throws IOException 
    {     

        if(! (freeChart instanceof Channel2DChart<?>))
        {
            return;
        }

        AVISavingTask task = new AVISavingTask(AtomicJ.getApplicationFrame(), (Channel2DChart<?>)freeChart, 
                path, biCompression, firstFrame, lastFrame, frameRate, reverse);
        task.execute();
    }


    @Override
    public void writeChartToStream(JFreeChart chart, OutputStream out)
            throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeMovieFrameToStream(Channel2DChart<?> chart, int frame,
            OutputStream out) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getExtension() 
    {
        return EXT;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        support.removePropertyChangeListener(listener);
    }

    private class AVISavingTask extends MonitoredSwingWorker<Void, Void> 
    {
        /*
         * THIS class is a moodification of a plugin for ImageJ, written by William Gandler and Wayne Rasband
         * (http://rsbweb.nih.gov/ij/plugins/avi.html). Pawel Hermanowicz introduced changes which allowed to save
         * AtomicJ stacks, in a task run in a separate thread
         */

        /*
        This plugin implements the File/Save As/AVI command.
        Supported formats:
          Uncompressed 8-bit (gray or indexed color), 24-bit (RGB)
          JPEG and PNG compression
          16-bit and 32-bit (float) images are converted to 8-bit
        The plugin is based on the FileAvi class written by William Gandler,
        part of Matthew J. McAuliffe's MIPAV program, available from
        http://mipav.cit.nih.gov/.
        2008-06-05: Support for jpeg and png-compressed output and
        composite images by Michael Schmid.
         */

        //four-character codes for compression
        // Note: byte sequence in four-cc is reversed - ints in Intel (little endian) byte order.
        // Note that compression codes BI_JPEG=4 and BI_PNG=5 are not understood by avi players
        // (even not by MediaPlayer, even though these codes are specified by Microsoft).
        private final static int FOURCC_00db = 0x62643030;    //'00db' uncompressed frame
        private final static int FOURCC_00dc = 0x63643030;    //'00dc' compressed frame

        private int xDim,yDim;      //image size
        private int zDim;           //number of movie frames (stack size)
        private int bytesPerPixel;  //8 or 24
        private int frameDataSize;  //in bytes (uncompressed)

        private int linePad;        //padding of data lines in bytes to reach 4*n length
        private byte[] bufferWrite;    //output buffer for image data
        private OutputStream  raOutputStream; //output stream for writing compressed images
        private final long[]  sizePointers =  //a stack of the pointers to the chunk sizes (pointers are
                new long[5];//  remembered to write the sizes later, when they are known)
        private int stackPointer;   //points to first free position in sizePointers stack

        private final Channel2DChart<?> chart;
        private RandomAccessFile raFile;

        private final AVICompression  biCompression;  //compression type (0, 'JPEG, 'PNG')

        private final int firstFrame;
        private final int lastFrame;
        private final int frameRate;
        private final boolean reverse;

        private int savedFrameCount = 0;

        private final File file;
        private final Component parent;

        public AVISavingTask(Component parent, Channel2DChart<?> chart, File file, AVICompression biCompression,
                int firstFrame, int lastFrame, int frameRate, boolean reverse)
        {
            super(parent, "Saving in progress", "Saved", (lastFrame - firstFrame + 1));

            this.parent = parent;
            this.chart = chart;
            this.file = file;

            this.firstFrame = firstFrame;
            this.lastFrame = lastFrame;
            this.frameRate = frameRate;
            this.reverse = reverse;
            this.biCompression = biCompression;
        }

        @Override
        public Void doInBackground() throws IOException
        {
            boolean uncompressed = AVICompression.NO_COMPRESSION.equals(biCompression);
            raFile = new RandomAccessFile(file, "rw");
            raFile.setLength(0);

            //  G e t   s t a c k   p r o p e r t i e s
            zDim = (lastFrame - firstFrame + 1);
            yDim = (int) Math.rint(getHeight());
            xDim = (int) Math.rint(getWidth());

            bytesPerPixel = 3;  //color and JPEG-compressed files

            linePad = 0;
            int minLineLength = bytesPerPixel*xDim;
            if (uncompressed && minLineLength%4!=0)
                linePad = 4 - minLineLength%4; //uncompressed lines written must be a multiple of 4 bytes
            frameDataSize = (bytesPerPixel*xDim+linePad)*yDim;

            writeAVIFIleHeader();

            //  W r i t e   s t r e a m   i n f o r m a t i o n
            long moviPointer = writeStreamInformation(uncompressed);

            //  P r e p a r e   f o r   w r i t i n g   d a t a
            if (uncompressed)
            {
                bufferWrite = new byte[frameDataSize];
            }
            else
            {
                raOutputStream = new RandomAccessFileOutputStream(raFile); //needed for writing compressed formats
            }
            int dataSignature = uncompressed ? FOURCC_00db : FOURCC_00dc;
            int maxChunkLength = 0;                 // needed for dwSuggestedBufferSize
            int[] dataChunkOffset = new int[zDim];  // remember chunk positions...
            int[] dataChunkLength = new int[zDim];  // ... and sizes for the index

            //  W r i t e   f r a m e   d a t a
            for (int z=0; z<zDim; z++) 
            {       

                if(!isCancelled())
                {
                    int chunkPointer = (int)raFile.getFilePointer();
                    writeInt(dataSignature);        // start writing chunk: '00db' or '00dc'
                    chunkSizeHere();                // size of '00db' or '00dc' chunk (nesting level 2)

                    int i = reverse ? zDim - z  - 1: z;
                    int j = reverse ? i - firstFrame : i + firstFrame;


                    if (uncompressed) 
                    {
                        writeRGBFrame(chart, j);
                    }
                    else
                    {
                        writeCompressedFrame(chart, j);
                    }

                    dataChunkOffset[i] = (int)(chunkPointer - moviPointer);
                    dataChunkLength[i] = (int)(raFile.getFilePointer() - chunkPointer - 8); //size excludes '00db' and size fields
                    if (maxChunkLength < dataChunkLength[i]) maxChunkLength = dataChunkLength[i];
                    chunkEndWriteSize();            // '00db' or '00dc' chunk finished (nesting level 2)

                    this.savedFrameCount = z + 1;
                    setStep(this.savedFrameCount);
                }
                else
                {
                    break;
                }

            }
            chunkEndWriteSize();                // LIST 'movi' finished (nesting level 1)

            //  W r i t e   I n d e x
            writeString("idx1");    // Write the idx1 chunk
            chunkSizeHere();        // size of 'idx1' chunk (nesting level 1)
            for (int z = 0; z < zDim; z++) 
            {
                int i = reverse ? zDim - z - 1: z;

                writeInt(dataSignature);// ckid field: '00db' or '00dc'
                writeInt(0x10);     // flags: select AVIIF_KEYFRAME
                // AVIIF_KEYFRAME 0x00000010
                // The flag indicates key frames in the video sequence.
                // Key frames do not need previous video information to be decompressed.
                // AVIIF_NOTIME 0x00000100 The CHUNK does not influence video timing (for
                //   example a palette change CHUNK).
                // AVIIF_LIST 0x00000001 marks a LIST CHUNK.
                // AVIIF_TWOCC 2L
                // AVIIF_COMPUSE 0x0FFF0000 These bits are for compressor use.
                writeInt(dataChunkOffset[i]); // offset to the chunk
                // offset can be relative to file start or 'movi'
                writeInt(dataChunkLength[i]); // length of the chunk.
            }  // for (z = 0; z < zDim; z++)
            chunkEndWriteSize();    // 'idx1' finished (nesting level 1)
            chunkEndWriteSize();    // 'RIFF' File finished (nesting level 0)
            raFile.close();

            return null;
        }	

        private void writeAVIFIleHeader() throws IOException
        {           
            //  W r i t e   A V I   f i l e   h e a d e r
            int microSecPerFrame = (int)Math.round((1.0/frameRate)*1.0e6);

            writeString("RIFF");    // signature
            chunkSizeHere();        // size of file (nesting level 0)
            writeString("AVI ");    // RIFF type
            writeString("LIST");    // first LIST chunk, which contains information on data decoding
            chunkSizeHere();        // size of LIST (nesting level 1)
            writeString("hdrl");    // LIST chunk type
            writeString("avih");    // Write the avih sub-CHUNK
            writeInt(0x38);         // length of the avih sub-CHUNK (38H) not including the
            // the first 8 bytes for avihSignature and the length
            writeInt(microSecPerFrame); // dwMicroSecPerFrame - Write the microseconds per frame
            writeInt(0);            // dwMaxBytesPerSec (maximum data rate of the file in bytes per second)
            writeInt(0);            // dwReserved1 - Reserved1 field set to zero
            writeInt(0x10);         // dwFlags - just set the bit for AVIF_HASINDEX
            //   10H AVIF_HASINDEX: The AVI file has an idx1 chunk containing
            //   an index at the end of the file.  For good performance, all
            //   AVI files should contain an index.
            writeInt(zDim);         // dwTotalFrames - total frame number
            writeInt(0);            // dwInitialFrames -Initial frame for interleaved files.
            // Noninterleaved files should specify 0.
            writeInt(1);            // dwStreams - number of streams in the file - here 1 video and zero audio.
            writeInt(0);      // dwSuggestedBufferSize 
            writeInt(xDim);         // dwWidth - image width in pixels
            writeInt(yDim);         // dwHeight - image height in pixels
            writeInt(0);            // dwReserved[4]
            writeInt(0);
            writeInt(0);
            writeInt(0);
        }

        private long writeStreamInformation(boolean uncompressed) throws IOException
        {
            writeString("LIST");    // List of stream headers

            chunkSizeHere();        // size of LIST (nesting level 2)
            writeString("strl");    // LIST chunk type: stream list
            writeString("strh");    // stream header 
            writeInt(56);           // Write the length of the strh sub-CHUNK
            writeString("vids");    // fccType - type of data stream - here 'vids' for video stream
            writeString("DIB ");    // 'DIB ' for Microsoft Device Independent Bitmap.
            writeInt(0);            // dwFlags
            writeInt(0);            // wPriority, wLanguage
            writeInt(0);            // dwInitialFrames
            writeInt(1);            // dwScale
            writeInt(frameRate); //  dwRate - frame rate for video streams
            writeInt(0);            // dwStart - this field is usually set to zero
            writeInt(zDim);         // dwLength - playing time of AVI file as defined by scale and rate
            // Set equal to the number of frames
            writeInt(0);            // dwSuggestedBufferSize for reading the stream.
            // Typically, this contains a value corresponding to the largest chunk
            // in a stream.
            writeInt(-1);           // dwQuality - encoding quality given by an integer between
            // 0 and 10,000.  If set to -1, drivers use the default
            // quality value.
            writeInt(0);            // dwSampleSize. 0 means that each frame is in its own chunk
            writeShort((short)0);   // left of rcFrame if stream has a different size than dwWidth*dwHeight(unused)
            writeShort((short)0);   // top
            writeShort((short)0);   // right
            writeShort((short)0);   // bottom

            // end of 'strh' chunk, stream format follows
            writeString("strf");    // stream format chunk
            chunkSizeHere();        // size of 'strf' chunk (nesting level 3)
            writeInt(40);           // biSize - Write header size of BITMAPINFO header structure
            // Applications should use this size to determine which BITMAPINFO header structure is
            // being used.  This size includes this biSize field.
            writeInt(xDim);         // biWidth - width in pixels
            writeInt(yDim);         // biHeight - image height in pixels. (May be negative for uncompressed
            // video to indicate vertical flip).
            writeShort(1);          // biPlanes - number of color planes in which the data is stored
            writeShort((short)(8*bytesPerPixel)); // biBitCount - number of bits per pixel #
            writeInt(biCompression.getCompressionCode()); // biCompression - type of compression used (uncompressed: NO_COMPRESSION=0)
            int biSizeImage =       // Image Buffer. Quicktime needs 3 bytes also for 8-bit png
                    uncompressed ? 0:xDim*yDim*bytesPerPixel;
            writeInt(biSizeImage);  // biSizeImage (buffer size for decompressed mage) may be 0 for uncompressed data
            writeInt(0);            // biXPelsPerMeter - horizontal resolution in pixels per meter
            writeInt(0);            // biYPelsPerMeter - vertical resolution in pixels per meter
            writeInt(0); // biClrUsed (color table size; for 8-bit only)
            writeInt(0);            // biClrImportant - specifies that the first x colors of the color table
            // are important to the DIB.  If the rest of the colors are not available,
            // the image still retains its meaning in an acceptable manner.  When this
            // field is set to zero, all the colors are important, or, rather, their
            // relative importance has not been computed.


            chunkEndWriteSize();    //'strf' chunk finished (nesting level 3)

            writeString("strn");    // Use 'strn' to provide a zero terminated text string describing the stream
            writeInt(16);           // length of the strn sub-CHUNK (must be even)
            writeString("ImageJ AVI     \0"); //must be 16 bytes as given above (including the terminating 0 byte)

            chunkEndWriteSize();    // LIST 'strl' finished (nesting level 2)
            chunkEndWriteSize();    // LIST 'hdrl' finished (nesting level 1)


            writeString("JUNK");    // write a JUNK chunk for padding
            chunkSizeHere();        // size of 'strf' chunk (nesting level 1)
            raFile.seek(4096/*2048*/);      // we continue here
            chunkEndWriteSize();    // 'JUNK' finished (nesting level 1)

            writeString("LIST");    // the second LIST chunk, which contains the actual data
            chunkSizeHere();        // size of LIST (nesting level 1)
            long moviPointer = raFile.getFilePointer();
            writeString("movi");    // Write LIST type 'movi'

            return moviPointer;
        }


        /** Reserve space to write the size of chunk and remember the position
         *  for a later call to chunkEndWriteSize().
         *  Several levels of chunkSizeHere() and chunkEndWriteSize() may be nested.
         */
        private void chunkSizeHere() throws IOException {
            sizePointers[stackPointer] = raFile.getFilePointer();

            writeInt(0);    //for now, write 0 to reserve space for "size" item
            stackPointer++;
        }

        /** At the end of a chunk, calculate its size and write it to the
         *  position remembered previously. Also pads to 2-byte boundaries.
         */
        private void chunkEndWriteSize() throws IOException {
            stackPointer--;
            long position = raFile.getFilePointer();
            raFile.seek(sizePointers[stackPointer]);
            writeInt((int)(position - (sizePointers[stackPointer]+4)));

            raFile.seek(((position+1)/2)*2);    //pad to 2-byte boundary
            //IJ.log("chunk at 0x"+Long.toHexString(sizePointers[stackPointer]-4)+"-0x"+Long.toHexString(position));
        }

        //I removed writeByteFrame method, as it was unnecessary for us

        //I introduced changes to writeRGBFRame methods, among others, changed its signature, so that now
        //I can use it to write frame of Channel2DChart
        /** Write RGB data. Each 3-byte triplet in the bitmap array represents
         *  blue, green, and red, respectively, for a pixel.  The color bytes are
         *  in reverse order (Windows convention). Lines are padded to a length
         *  that is a multiple of 4 bytes. */
        private void writeRGBFrame(Channel2DChart<?> chart, int frame) throws IOException 
        {
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();	
            paintOnGraphicsDevice(chart, frame, g2);

            g2.dispose();


            int[] pixels = getPixels(image);
            int width = image.getWidth();
            int height = image.getHeight();
            int c, offset, index = 0;
            for (int y=height-1; y>=0; y--) {
                offset = y*width;
                for (int x=0; x<width; x++) {
                    c = pixels[offset++];
                    bufferWrite[index++] = (byte)(c&0xff); // blue
                    bufferWrite[index++] = (byte)((c&0xff00)>>8); //green
                    bufferWrite[index++] = (byte)((c&0xff0000)>>16); // red
                }
                for (int i = 0; i<linePad; i++)
                    bufferWrite[index++] = (byte)0;
            }
            raFile.write(bufferWrite);
        }

        /** Write a frame as jpeg- or png-compressed image */
        private void writeCompressedFrame(Channel2DChart<?> chart, int frame) throws IOException 
        {
            //IJ.log("BufferdImage Type="+bufferedImage.getType()); // 1=RGB, 13=indexed
            if (biCompression.equals(AVICompression.JPEG_COMPRESSION))
            {
                BufferedImage bi = getBufferedImage(chart, frame);
                ImageIO.write(bi, "jpeg", raOutputStream);
            } 
            else 
            { 
                BufferedImage bi = getBufferedImage(chart, frame);
                ImageIO.write(bi, "png", raOutputStream);
            }
        }

        private BufferedImage getBufferedImage(Channel2DChart<?> chart, int frame) 
        {
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();		
            paintOnGraphicsDevice(chart, frame, g2);
            g2.dispose();

            return image;
        }

        //I removed writeLUT and getFrameRate methods, as they were not ecessary
        //added getPixels method
        private int[] getPixels(BufferedImage image)
        {	    	
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            int[] pixels = new int[width * height];
            PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
            try {
                pg.grabPixels();
            } catch (InterruptedException e){}

            return pixels;
        }

        private void writeString(String s) throws IOException {
            byte[] bytes =  s.getBytes("UTF8");
            raFile.write(bytes);
        }

        /** Write 4-byte int with Intel (little-endian) byte order
         * (note: RandomAccessFile.writeInt has other byte order than AVI) */
        private void writeInt(int v) throws IOException {
            raFile.write(v & 0xFF);
            raFile.write((v >>>  8) & 0xFF);
            raFile.write((v >>> 16) & 0xFF);
            raFile.write((v >>> 24) & 0xFF);
        }

        /** Write 2-byte short with Intel (little-endian) byte order
         * (note: RandomAccessFile.writeShort has other byte order than AVI) */
        private void writeShort(int v) throws IOException {
            raFile.write(v & 0xFF);
            raFile.write((v >>> 8) & 0xFF);
        }

        @Override
        protected void done()
        {
            super.done();

            if(isCancelled())
            {
                JOptionPane.showMessageDialog(parent, "Saving terminatedSaved " + 
                        savedFrameCount + " frames", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
            }
        }

        @Override
        public void cancelAllTasks() 
        {
            cancel(false);
        }
    }
}
