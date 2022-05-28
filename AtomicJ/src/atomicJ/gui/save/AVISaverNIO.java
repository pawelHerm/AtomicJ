package atomicJ.gui.save;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;

import atomicJ.analysis.MonitoredSwingWorker;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.Channel2DChart;
import atomicJ.utilities.IOUtilities;



public class AVISaverNIO extends BasicImageFormatSaver implements ZippableFrameFormatSaver
{
    private static final String CHARSET_NAME = StandardCharsets.UTF_8.name();
    private final AVICompression  biCompression;  //compression type (0, 'JPEG, 'PNG')
    private final int firstFrame;
    private final int lastFrame;
    private final int frameRate;
    private final boolean reverse;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private static final String EXT = ".avi";


    public AVISaverNIO(Rectangle2D chartInitialArea, int width, int height, int firstFrame, int lastFrame, int frameRate, boolean reverse, boolean saveDataArea, AVICompression compression, int jpegQuality)
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


    //    private void saveAsZipJava11(final JFreeChart freeChart, final File path, final String entryName, ChartRenderingInfo info)
    //    {
    //        Map<String, String> env = new HashMap<>(); 
    //        env.put("create", "true");
    //        
    //        URI uri = URI.create("jar:" + f.toURI());
    //        
    //        try
    //        {
    //            FileSystem fs = FileSystems.newFileSystem(uri, env);
    //            Channel2DChart<?> densityChart = (Channel2DChart<?>)freeChart;
    //
    //            AVISavingTask task = new AVISavingTask(AtomicJ.currentFrame, densityChart, 
    //                    fs.getPath(entryName), biCompression, firstFrame, lastFrame, frameRate, reverse);
    //            
    //            PropertyChangeListener taskStateListener = new PropertyChangeListener()
    //            {
    //                @Override
    //                public void propertyChange(PropertyChangeEvent evt) 
    //                {
    //                    if(SwingWorker.StateValue.DONE.equals(evt.getNewValue())) 
    //                    {
    //                       try {
    //                        fs.close();
    //                    } catch (IOException e) {
    //                        // TODO Auto-generated catch block
    //                        e.printStackTrace();
    //                    }
    //                    }
    //                }          
    //            };
    //
    //            task.addPropertyChangeListener("state", taskStateListener);
    //            task.execute();
    //        } catch (IOException e2) {
    //            // TODO Auto-generated catch block
    //            e2.printStackTrace();
    //        }
    //    }


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
         * THIS class is a modification of a plugin for ImageJ, written by William Gandler and Wayne Rasband
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
        private final long[]  sizePointers =  //a stack of the pointers to the chunk sizes (pointers are
                new long[5];//  remembered to write the sizes later, when they are known)
        private int stackPointer;   //points to first free position in sizePointers stack

        private final Channel2DChart<?> chart;
        private SeekableByteChannel channel;

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
            channel = Files.newByteChannel(this.file.toPath(), EnumSet.of(StandardOpenOption.CREATE, 
                    StandardOpenOption.WRITE));

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

            //  W r i t e   f r a m e   d a t a
            //  W r i t e   I n d e x
            writeFrameDataAndIndex(uncompressed, moviPointer);
            channel.close();

            return null;
        }	

        private void writeAVIFIleHeader() throws IOException
        {            
            int headerSize = 88;
            ByteBuffer bufferHeader = ByteBuffer.allocate(headerSize).order(ByteOrder.LITTLE_ENDIAN);

            long position = channel.position();
            //  W r i t e   A V I   f i l e   h e a d e r
            bufferHeader.put("RIFF".getBytes(CHARSET_NAME));    // signature, 4 bytes
            chunkSizeHere(bufferHeader, position);        // 4 bytes, size of file (nesting level 0)

            bufferHeader.put("AVI ".getBytes(CHARSET_NAME));    // RIFF type, 4 bytes
            bufferHeader.put("LIST".getBytes(CHARSET_NAME));    // 4 bytes, first LIST chunk, which contains information on data decoding
            chunkSizeHere(bufferHeader, position);        //4 bytes,  size of LIST (nesting level 1)
            bufferHeader.put("hdrl".getBytes(CHARSET_NAME));    // 4 bytes, LIST chunk type
            bufferHeader.put("avih".getBytes(CHARSET_NAME));    // 4bytes, Write the avih sub-CHUNK
            bufferHeader.putInt(0x38);         //4 bytes, length of the avih sub-CHUNK (38H) not including the
            // the first 8 bytes for avihSignature and the length
            int microSecPerFrame = (int)Math.round((1.0/frameRate)*1.0e6);
            bufferHeader.putInt(microSecPerFrame); // dwMicroSecPerFrame - Write the microseconds per frame
            bufferHeader.putInt(0);            // dwMaxBytesPerSec (maximum data rate of the file in bytes per second)
            bufferHeader.putInt(0);            // dwReserved1 - Reserved1 field set to zero
            bufferHeader.putInt(0x10);         // dwFlags - just set the bit for AVIF_HASINDEX
            //   10H AVIF_HASINDEX: The AVI file has an idx1 chunk containing
            //   an index at the end of the file.  For good performance, all
            //   AVI files should contain an index.
            bufferHeader.putInt(zDim);         // dwTotalFrames - total frame number
            bufferHeader.putInt(0);            // dwInitialFrames -Initial frame for interleaved files.
            // Noninterleaved files should specify 0.
            bufferHeader.putInt(1);            // dwStreams - number of streams in the file - here 1 video and zero audio.
            bufferHeader.putInt(0);      // dwSuggestedBufferSize 
            bufferHeader.putInt(xDim);         // dwWidth - image width in pixels
            bufferHeader.putInt(yDim);         // dwHeight - image height in pixels
            bufferHeader.putInt(0);            // dwReserved[4]
            bufferHeader.putInt(0);
            bufferHeader.putInt(0);
            bufferHeader.putInt(0);

            bufferHeader.flip();

            channel.write(bufferHeader);

        }

        private long writeStreamInformation(boolean uncompressed) throws IOException
        {
            int streamFirstBufferSize = 124;
            ByteBuffer streamHeader = ByteBuffer.allocate(streamFirstBufferSize).order(ByteOrder.LITTLE_ENDIAN);

            long position = channel.position();
            streamHeader.put("LIST".getBytes(CHARSET_NAME));    // List of stream headers

            chunkSizeHere(streamHeader, position);        // size of LIST (nesting level 2)
            streamHeader.put("strl".getBytes(CHARSET_NAME));    // LIST chunk type: stream list
            streamHeader.put("strh".getBytes(CHARSET_NAME));    // stream header 
            streamHeader.putInt(56);           // Write the length of the strh sub-CHUNK
            streamHeader.put("vids".getBytes(CHARSET_NAME));    // fccType - type of data stream - here 'vids' for video stream
            streamHeader.put("DIB ".getBytes(CHARSET_NAME));    // 'DIB ' for Microsoft Device Independent Bitmap.
            streamHeader.putInt(0);            // dwFlags
            streamHeader.putInt(0);            // wPriority, wLanguage
            streamHeader.putInt(0);            // dwInitialFrames
            streamHeader.putInt(1);            // dwScale
            streamHeader.putInt(frameRate); //  dwRate - frame rate for video streams
            streamHeader.putInt(0);            // dwStart - this field is usually set to zero
            streamHeader.putInt(zDim);         // dwLength - playing time of AVI file as defined by scale and rate
            // Set equal to the number of frames
            streamHeader.putInt(0);            // dwSuggestedBufferSize for reading the stream.
            // Typically, this contains a value corresponding to the largest chunk
            // in a stream.
            streamHeader.putInt(-1);           // dwQuality - encoding quality given by an integer between
            // 0 and 10,000.  If set to -1, drivers use the default
            // quality value.
            streamHeader.putInt(0);            // dwSampleSize. 0 means that each frame is in its own chunk
            streamHeader.putShort((short)0);   // left of rcFrame if stream has a different size than dwWidth*dwHeight(unused)
            streamHeader.putShort((short)0);   // top
            streamHeader.putShort((short)0);   // right
            streamHeader.putShort((short)0);   // bottom
            // end of 'strh' chunk, stream format follows
            streamHeader.put("strf".getBytes(CHARSET_NAME));    // stream format chunk

            chunkSizeHere(streamHeader, position);        // size of 'strf' chunk (nesting level 3)
            streamHeader.putInt(40);           // biSize - Write header size of BITMAPINFO header structure
            // Applications should use this size to determine which BITMAPINFO header structure is
            // being used.  This size includes this biSize field.
            streamHeader.putInt(xDim);         // biWidth - width in pixels
            streamHeader.putInt(yDim);         // biHeight - image height in pixels. (May be negative for uncompressed
            // video to indicate vertical flip).
            streamHeader.putShort((short)1);          // biPlanes - number of color planes in which the data is stored
            streamHeader.putShort((short)(8*bytesPerPixel)); // biBitCount - number of bits per pixel #
            streamHeader.putInt(biCompression.getCompressionCode()); // biCompression - type of compression used (uncompressed: NO_COMPRESSION=0)
            int biSizeImage =       // Image Buffer. Quicktime needs 3 bytes also for 8-bit png
                    uncompressed ? 0:xDim*yDim*bytesPerPixel;
            streamHeader.putInt(biSizeImage);  // biSizeImage (buffer size for decompressed mage) may be 0 for uncompressed data
            streamHeader.putInt(0);            // biXPelsPerMeter - horizontal resolution in pixels per meter
            streamHeader.putInt(0);            // biYPelsPerMeter - vertical resolution in pixels per meter
            streamHeader.putInt(0); // biClrUsed (color table size; for 8-bit only)
            streamHeader.putInt(0);            // biClrImportant - specifies that the first x colors of the color table
            // are important to the DIB.  If the rest of the colors are not available,
            // the image still retains its meaning in an acceptable manner.  When this
            // field is set to zero, all the colors are important, or, rather, their
            // relative importance has not been computed.

            streamHeader.flip();
            channel.write(streamHeader);


            //dot¹d dobrze
            chunkEndWriteSize();    //'strf' chunk finished (nesting level 3)

            ByteBuffer secondStreamBuffer = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);

            secondStreamBuffer.put("strn".getBytes(CHARSET_NAME));    // Use 'strn' to provide a zero terminated text string describing the stream
            secondStreamBuffer.putInt(16);           // length of the strn sub-CHUNK (must be even)
            secondStreamBuffer.put("AtomicJ AVI    \0".getBytes(CHARSET_NAME)); //must be 16 bytes as given above (including the terminating 0 byte)

            secondStreamBuffer.flip();
            channel.write(secondStreamBuffer);

            chunkEndWriteSize();    // LIST 'strl' finished (nesting level 2)
            chunkEndWriteSize();    // LIST 'hdrl' finished (nesting level 1)


            ByteBuffer thridStreamBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);

            thridStreamBuffer.put("JUNK".getBytes(CHARSET_NAME));    // write a JUNK chunk for padding
            chunkSizeHere(thridStreamBuffer, channel.position());        // size of 'strf' chunk (nesting level 1)

            thridStreamBuffer.flip();
            channel.write(thridStreamBuffer);

            channel.position(4096/*2048*/);      // we continue here
            chunkEndWriteSize();    // 'JUNK' finished (nesting level 1)

            ByteBuffer fourthStreamBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);

            fourthStreamBuffer.put("LIST".getBytes(CHARSET_NAME));    // the second LIST chunk, which contains the actual data
            chunkSizeHere(fourthStreamBuffer, channel.position());        // size of LIST (nesting level 1)

            fourthStreamBuffer.flip();
            channel.write(fourthStreamBuffer);

            long moviPointer = channel.position();
            channel.write(ByteBuffer.wrap("movi".getBytes(CHARSET_NAME)));// Write LIST type 'movi'

            return moviPointer;
        }

        private void writeFrameDataAndIndex(boolean uncompressed, long moviPointer) throws IOException
        {
            ByteBuffer bufferWrite = null;
            if (uncompressed)
            {
                bufferWrite = ByteBuffer.allocate(frameDataSize);
            }

            int dataSignature = uncompressed ? FOURCC_00db : FOURCC_00dc;
            int maxChunkLength = 0;                 // needed for dwSuggestedBufferSize
            int[] dataChunkOffset = new int[zDim];  // remember chunk positions...
            int[] dataChunkLength = new int[zDim];  // ... and sizes for the index

            for (int z=0; z<zDim; z++) 
            {       

                if(!isCancelled())
                {
                    int chunkPointer = (int)channel.position();
                    ByteBuffer signatureBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
                    signatureBuffer.putInt(dataSignature);        // start writing chunk: '00db' or '00dc'
                    chunkSizeHere(signatureBuffer, channel.position());                // size of '00db' or '00dc' chunk (nesting level 2)

                    signatureBuffer.flip();
                    channel.write(signatureBuffer);

                    int i = reverse ? zDim - z  - 1: z;
                    int j = reverse ? i - firstFrame : i + firstFrame;


                    if (uncompressed) 
                    {
                        writeRGBFrame(chart, bufferWrite, j);
                    }
                    else
                    {
                        writeCompressedFrame(chart, j);
                    }

                    dataChunkOffset[i] = (int)(chunkPointer - moviPointer);
                    dataChunkLength[i] = (int)(channel.position() - chunkPointer - 8); //size excludes '00db' and size fields
                    if (maxChunkLength < dataChunkLength[i]) {maxChunkLength = dataChunkLength[i];}
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

            ByteBuffer bufferIndex = ByteBuffer.allocate(8 + 16*zDim).order(ByteOrder.LITTLE_ENDIAN);

            bufferIndex.put("idx1".getBytes(CHARSET_NAME));    // Write the idx1 chunk
            chunkSizeHere(bufferIndex, channel.position());        // size of 'idx1' chunk (nesting level 1)
            for (int z = 0; z < zDim; z++) 
            {
                int i = reverse ? zDim - z - 1: z;

                bufferIndex.putInt(dataSignature);// ckid field: '00db' or '00dc'
                bufferIndex.putInt(0x10);     // flags: select AVIIF_KEYFRAME
                // AVIIF_KEYFRAME 0x00000010
                // The flag indicates key frames in the video sequence.
                // Key frames do not need previous video information to be decompressed.
                // AVIIF_NOTIME 0x00000100 The CHUNK does not influence video timing (for
                //   example a palette change CHUNK).
                // AVIIF_LIST 0x00000001 marks a LIST CHUNK.
                // AVIIF_TWOCC 2L
                // AVIIF_COMPUSE 0x0FFF0000 These bits are for compressor use.
                bufferIndex.putInt(dataChunkOffset[i]); // offset to the chunk
                // offset can be relative to file start or 'movi'
                bufferIndex.putInt(dataChunkLength[i]); // length of the chunk.
            }  // for (z = 0; z < zDim; z++)

            bufferIndex.flip();
            channel.write(bufferIndex);

            chunkEndWriteSize();    // 'idx1' finished (nesting level 1)
            chunkEndWriteSize();    // 'RIFF' File finished (nesting level 0)
        }

        private void chunkSizeHere(ByteBuffer byteBuffer, long channelPositon) throws IOException {
            sizePointers[stackPointer] = channelPositon + byteBuffer.position();

            byteBuffer.putInt(0);    //for now, write 0 to reserve space for "size" item
            stackPointer++;
        }

        /** At the end of a chunk, calculate its size and write it to the
         *  position remembered previously. Also pads to 2-byte boundaries.
         */
        private void chunkEndWriteSize() throws IOException {
            stackPointer--;
            long position = channel.position();
            channel.position((int)sizePointers[stackPointer]);
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt((int)(position - (sizePointers[stackPointer]+4)));
            buffer.flip();
            channel.write(buffer);

            channel.position((int)((position+1)/2)*2);    //pad to 2-byte boundary
            //IJ.log("chunk at 0x"+Long.toHexString(sizePointers[stackPointer]-4)+"-0x"+Long.toHexString(position));
        }

        //I removed writeByteFrame method, as it was unnecessary for us

        //I introduced changes to writeRGBFRame methods, among others, changed its signature, so that now
        //I can use it to write frame of Channel2DChart
        /** Write RGB data. Each 3-byte triplet in the bitmap array represents
         *  blue, green, and red, respectively, for a pixel.  The color bytes are
         *  in reverse order (Windows convention). Lines are padded to a length
         *  that is a multiple of 4 bytes. */
        private void writeRGBFrame(Channel2DChart<?> chart, ByteBuffer bufferWrite, int frame) throws IOException 
        {
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();	
            paintOnGraphicsDevice(chart, frame, g2);

            g2.dispose();

            int[] pixels = getPixels(image);            

            int width = image.getWidth();
            int height = image.getHeight();
            int c, offset;

            for (int y=height-1; y>=0; y--) {
                offset = y*width;
                for (int x=0; x<width; x++) {
                    c = pixels[offset++];
                    bufferWrite.put((byte)(c&0xff)); //blue
                    bufferWrite.put((byte)((c&0xff00)>>8));//green
                    bufferWrite.put((byte)((c&0xff0000)>>16)); // red
                }
                for (int i = 0; i<linePad; i++)
                    bufferWrite.put((byte)0);
            }
            bufferWrite.flip();            
            channel.write(bufferWrite);          
            bufferWrite.clear();
        }

        /** Write a frame as jpeg- or png-compressed image */
        private void writeCompressedFrame(Channel2DChart<?> chart, int frame) throws IOException 
        {
            OutputStream raOutputStream = Channels.newOutputStream(channel);
            BufferedImage bi = getBufferedImage(chart, frame);

            if (biCompression.equals(AVICompression.JPEG_COMPRESSION))
            {
                ImageIO.write(bi, "jpeg", raOutputStream);
            } 
            else 
            { 
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

        //I removed writeLUT and getFrameRate methods, as they were not necessary
        //added getPixels method
        private int[] getPixels(BufferedImage image)
        {	    	

            //https://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image
            //https://stackoverflow.com/questions/29301838/converting-bufferedimage-to-bytebuffer
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            int[] pixels = new int[width * height];
            PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
            try {
                pg.grabPixels();
            } catch (InterruptedException e){}

            return pixels;
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
