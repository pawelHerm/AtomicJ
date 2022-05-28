package atomicJ.utilities;

/*
 * #%L
 * BSD implementations of Bio-Formats readers and writers
 * %%
 * Copyright (C) 2005 - 2014 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */


import java.awt.Component;
import java.awt.Container;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import loci.common.DataTools;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.MetadataTools;
import loci.formats.gui.Index16ColorModel;
import loci.formats.gui.SignedByteBuffer;
import loci.formats.gui.SignedColorModel;
import loci.formats.gui.SignedShortBuffer;
import loci.formats.gui.TwoChannelColorSpace;
import loci.formats.gui.UnsignedIntBuffer;
import loci.formats.gui.UnsignedIntColorModel;
import loci.formats.meta.MetadataRetrieve;
import ome.xml.model.primitives.PositiveInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class with convenience methods for manipulating images
 * in {@link java.awt.image.BufferedImage} form.
 *
 * To work with images in primitive array form,
 * use the {@link loci.formats.ImageTools} class.
 *
 * Much code was stolen and adapted from
 * <a href="http://forum.java.sun.com/thread.jspa?threadID=522483">
 * DrLaszloJamf's posts</a>
 * on the Java forums.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/gui/AWTImageTools.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/gui/AWTImageTools.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public final class AWTImageTools2 {

    // -- Constants --

    /** ImageObserver for working with AWT images. */
    protected static final Component OBS = new Container();

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AWTImageTools2.class);

    // -- Constructor --

    private AWTImageTools2() { }

    // -- Image construction - from 1D (single channel) data arrays --

    /**
     * Creates an image from the given single-channel byte data.
     *
     * @param data Array containing image data.
     * @param w Width of image plane.
     * @param h Height of image plane.
     * @param signed Whether the byte values should be treated as signed
     *   (-128 to 127) instead of unsigned (0 to 255).
     */
    public static BufferedImage makeImage(byte[] data,
            int w, int h, boolean signed)
    {
        return makeImage(new byte[][] {data}, w, h, signed);
    }

    /**
     * Creates an image from the given single-channel short data.
     *
     * @param data Array containing image data.
     * @param w Width of image plane.
     * @param h Height of image plane.
     * @param signed Whether the short values should be treated as signed
     *   (-32768 to 32767) instead of unsigned (0 to 65535).
     */
    public static BufferedImage makeImage(short[] data,
            int w, int h, boolean signed)
    {
        return makeImage(new short[][] {data}, w, h, signed);
    }

    /**
     * Creates an image from the given single-channel int data.
     *
     * @param data Array containing image data.
     * @param w Width of image plane.
     * @param h Height of image plane.
     * @param signed Whether the int values should be treated as signed
     *   (-2^31 to 2^31-1) instead of unsigned (0 to 2^32-1).
     */
    public static BufferedImage makeImage(int[] data,
            int w, int h, boolean signed)
    {
        return makeImage(new int[][] {data}, w, h, signed);
    }

    /**
     * Creates an image from the given single-channel float data.
     *
     * @param data Array containing image data.
     * @param w Width of image plane.
     * @param h Height of image plane.
     */
    public static BufferedImage makeImage(float[] data, int w, int h) {
        return makeImage(new float[][] {data}, w, h);
    }

    /**
     * Creates an image from the given single-channel double data.
     *
     * @param data Array containing image data.
     * @param w Width of image plane.
     * @param h Height of image plane.
     */
    public static BufferedImage makeImage(double[] data, int w, int h) {
        return makeImage(new double[][] {data}, w, h);
    }

    // -- Image construction - from 1D (interleaved or banded) data arrays --

    /**
     * Creates an image from the given byte data.
     *
     * @param data Array containing image data.
     * @param w Width of image plane.
     * @param h Height of image plane.
     * @param c Number of channels.
     * @param interleaved If set, the channels are assumed to be interleaved;
     *   otherwise they are assumed to be sequential.
     *   For example, for RGB data, the pattern "RGBRGBRGB..." is interleaved,
     *   while "RRR...GGG...BBB..." is sequential.
     * @param signed Whether the byte values should be treated as signed
     *   (-128 to 127) instead of unsigned (0 to 255).
     */
    public static BufferedImage makeImage(byte[] data,
            int w, int h, int c, boolean interleaved, boolean signed)
    {
        if (c == 1) return makeImage(data, w, h, signed);
        if (c > 2) return makeRGBImage(data, c, w, h, interleaved);
        int dataType;
        DataBuffer buffer;
        dataType = DataBuffer.TYPE_BYTE;
        if (signed) {
            buffer = new SignedByteBuffer(data, c * w * h);
        }
        else {
            buffer = new DataBufferByte(data, c * w * h);
        }
        return constructImage(c, dataType, w, h, interleaved, false, buffer);
    }

    /**
     * Creates an image from the given short data.
     *
     * @param data Array containing image data.
     * @param w Width of image plane.
     * @param h Height of image plane.
     * @param c Number of channels.
     * @param interleaved If set, the channels are assumed to be interleaved;
     *   otherwise they are assumed to be sequential.
     *   For example, for RGB data, the pattern "RGBRGBRGB..." is interleaved,
     *   while "RRR...GGG...BBB..." is sequential.
     * @param signed Whether the short values should be treated as signed
     *   (-32768 to 32767) instead of unsigned (0 to 65535).
     */
    public static BufferedImage makeImage(short[] data,
            int w, int h, int c, boolean interleaved, boolean signed)
    {
        if (c == 1) return makeImage(data, w, h, signed);
        int dataType;
        DataBuffer buffer;
        if (signed) {
            dataType = DataBuffer.TYPE_SHORT;
            buffer = new SignedShortBuffer(data, c * w * h);
        }
        else {
            dataType = DataBuffer.TYPE_USHORT;
            buffer = new DataBufferUShort(data, c * w * h);
        }
        return constructImage(c, dataType, w, h, interleaved, false, buffer);
    }

    /**
     * Creates an image from the given int data.
     *
     * @param data Array containing image data.
     * @param w Width of image plane.
     * @param h Height of image plane.
     * @param c Number of channels.
     * @param interleaved If set, the channels are assumed to be interleaved;
     *   otherwise they are assumed to be sequential.
     *   For example, for RGB data, the pattern "RGBRGBRGB..." is interleaved,
     *   while "RRR...GGG...BBB..." is sequential.
     * @param signed Whether the int values should be treated as signed
     *   (-2^31 to 2^31-1) instead of unsigned (0 to 2^32-1).
     */
    public static BufferedImage makeImage(int[] data,
            int w, int h, int c, boolean interleaved, boolean signed)
    {
        if (c == 1) return makeImage(data, w, h, signed);
        int dataType = DataBuffer.TYPE_INT;
        DataBuffer buffer;
        if (signed) {
            buffer = new DataBufferInt(data, c * w * h);
        }
        else {
            buffer = new UnsignedIntBuffer(data, c * w * h);
        }
        return constructImage(c, dataType, w, h, interleaved, false, buffer);
    }

    /**
     * Creates an image from the given float data.
     *
     * @param data Array containing image data.
     * @param w Width of image plane.
     * @param h Height of image plane.
     * @param c Number of channels.
     * @param interleaved If set, the channels are assumed to be interleaved;
     *   otherwise they are assumed to be sequential.
     *   For example, for RGB data, the pattern "RGBRGBRGB..." is interleaved,
     *   while "RRR...GGG...BBB..." is sequential.
     */
    public static BufferedImage makeImage(float[] data,
            int w, int h, int c, boolean interleaved)
    {
        if (c == 1) return makeImage(data, w, h);
        int dataType = DataBuffer.TYPE_FLOAT;
        DataBuffer buffer = new DataBufferFloat(data, c * w * h);
        return constructImage(c, dataType, w, h, interleaved, false, buffer);
    }

    /**
     * Creates an image from the given double data.
     *
     * @param data Array containing image data.
     * @param w Width of image plane.
     * @param h Height of image plane.
     * @param c Number of channels.
     * @param interleaved If set, the channels are assumed to be interleaved;
     *   otherwise they are assumed to be sequential.
     *   For example, for RGB data, the pattern "RGBRGBRGB..." is interleaved,
     *   while "RRR...GGG...BBB..." is sequential.
     */
    public static BufferedImage makeImage(double[] data,
            int w, int h, int c, boolean interleaved)
    {
        if (c == 1) return makeImage(data, w, h);
        int dataType = DataBuffer.TYPE_DOUBLE;
        DataBuffer buffer = new DataBufferDouble(data, c * w * h);
        return constructImage(c, dataType, w, h, interleaved, false, buffer);
    }

    // -- Image construction - from 2D (banded) data arrays --

    /**
     * Creates an image from the given byte data.
     *
     * @param data Array containing image data.
     *   It is assumed that each channel corresponds to one element of the array.
     *   For example, for RGB data, data[0] is R, data[1] is G, and data[2] is B.
     * @param w Width of image plane.
     * @param h Height of image plane.
     * @param signed Whether the byte values should be treated as signed
     *   (-128 to 127) instead of unsigned (0 to 255).
     */
    public static BufferedImage makeImage(byte[][] data,
            int w, int h, boolean signed)
    {
        if (data.length > 2) return makeRGBImage(data, w, h);
        int dataType;
        DataBuffer buffer;
        dataType = DataBuffer.TYPE_BYTE;
        if (signed) {
            buffer = new SignedByteBuffer(data, data[0].length);
        }
        else {
            buffer = new DataBufferByte(data, data[0].length);
        }
        return constructImage(data.length, dataType, w, h, false, true, buffer);
    }

    /**
     * Creates an image from the given short data.
     *
     * @param data Array containing image data.
     *   It is assumed that each channel corresponds to one element of the array.
     *   For example, for RGB data, data[0] is R, data[1] is G, and data[2] is B.
     * @param w Width of image plane.
     * @param h Height of image plane.
     * @param signed Whether the short values should be treated as signed
     *   (-32768 to 32767) instead of unsigned (0 to 65535).
     */
    public static BufferedImage makeImage(short[][] data,
            int w, int h, boolean signed)
    {
        int dataType;
        DataBuffer buffer;
        if (signed) {
            dataType = DataBuffer.TYPE_SHORT;
            buffer = new SignedShortBuffer(data, data[0].length);
        }
        else {
            dataType = DataBuffer.TYPE_USHORT;
            buffer = new DataBufferUShort(data, data[0].length);
        }
        return constructImage(data.length, dataType, w, h, false, true, buffer);
    }

    /**
     * Creates an image from the given int data.
     *
     * @param data Array containing image data.
     *   It is assumed that each channel corresponds to one element of the array.
     *   For example, for RGB data, data[0] is R, data[1] is G, and data[2] is B.
     * @param w Width of image plane.
     * @param h Height of image plane.
     * @param signed Whether the int values should be treated as signed
     *   (-2^31 to 2^31-1) instead of unsigned (0 to 2^32-1).
     */
    public static BufferedImage makeImage(int[][] data,
            int w, int h, boolean signed)
    {
        int dataType = DataBuffer.TYPE_INT;
        DataBuffer buffer;
        if (signed) {
            buffer = new DataBufferInt(data, data[0].length);
        }
        else {
            buffer = new UnsignedIntBuffer(data, data[0].length);
        }
        return constructImage(data.length, dataType, w, h, false, true, buffer);
    }

    /**
     * Creates an image from the given single-precision floating point data.
     *
     * @param data Array containing image data.
     *   It is assumed that each channel corresponds to one element of the array.
     *   For example, for RGB data, data[0] is R, data[1] is G, and data[2] is B.
     * @param w Width of image plane.
     * @param h Height of image plane.
     */
    public static BufferedImage makeImage(float[][] data, int w, int h) {
        int dataType = DataBuffer.TYPE_FLOAT;
        DataBuffer buffer = new DataBufferFloat(data, data[0].length);
        return constructImage(data.length, dataType, w, h, false, true, buffer);
    }

    /**
     * Creates an image from the given double-precision floating point data.
     *
     * @param data Array containing image data.
     *   It is assumed that each channel corresponds to one element of the array.
     *   For example, for RGB data, data[0] is R, data[1] is G, and data[2] is B.
     * @param w Width of image plane.
     * @param h Height of image plane.
     */
    public static BufferedImage makeImage(double[][] data, int w, int h) {
        int dataType = DataBuffer.TYPE_DOUBLE;
        DataBuffer buffer = new DataBufferDouble(data, data[0].length);
        return constructImage(data.length, dataType, w, h, false, true, buffer);
    }

    // -- Image construction - with type conversion --

    /**
     * Creates an image from the given raw byte array, obtaining the
     * dimensional parameters from the specified metadata object.
     *
     * @param data Array containing image data.
     * @param interleaved If set, the channels are assumed to be interleaved;
     *   otherwise they are assumed to be sequential.
     *   For example, for RGB data, the pattern "RGBRGBRGB..." is interleaved,
     *   while "RRR...GGG...BBB..." is sequential.
     * @param meta Metadata object containing dimensional parameters.
     * @param series Relevant image series number of metadata object.
     */
    public static BufferedImage makeImage(byte[] data, boolean interleaved,
            MetadataRetrieve meta, int series) throws FormatException
    {
        MetadataTools.verifyMinimumPopulated(meta, series);
        int width = meta.getPixelsSizeX(series).getValue().intValue();
        int height = meta.getPixelsSizeY(series).getValue().intValue();
        String pixelType = meta.getPixelsType(series).toString();
        int type = FormatTools.pixelTypeFromString(pixelType);
        PositiveInteger nChannels = meta.getChannelSamplesPerPixel(series, 0);
        if (nChannels == null) {
            LOGGER.warn("SamplesPerPixel is null; it is assumed to be 1.");
        }
        int channels = nChannels == null ? 1 : nChannels.getValue();
        boolean littleEndian =
                !meta.getPixelsBinDataBigEndian(series, 0).booleanValue();
        return makeImage(data, width, height, channels,
                interleaved, FormatTools.getBytesPerPixel(type),
                FormatTools.isFloatingPoint(type), littleEndian,
                FormatTools.isSigned(type));
    }

    /**
     * Creates an image from the given raw byte array,
     * performing any necessary type conversions.
     *
     * @param data Array containing image data.
     * @param w Width of image plane.
     * @param h Height of image plane.
     * @param c Number of channels.
     * @param interleaved If set, the channels are assumed to be interleaved;
     *   otherwise they are assumed to be sequential.
     *   For example, for RGB data, the pattern "RGBRGBRGB..." is interleaved,
     *   while "RRR...GGG...BBB..." is sequential.
     * @param bpp Denotes the number of bytes in the returned primitive type
     *   (e.g. if bpp == 2, we should return an array of type short).
     * @param fp If set and bpp == 4 or bpp == 8, then return floats or doubles.
     * @param little Whether byte array is in little-endian order.
     * @param signed Whether the data values should be treated as signed
     *   instead of unsigned.
     */
    public static BufferedImage makeImage(byte[] data, int w, int h, int c,
            boolean interleaved, int bpp, boolean fp, boolean little, boolean signed)
    {
        Object pixels = DataTools.makeDataArray(data,
                bpp % 3 == 0 ? bpp / 3 : bpp, fp, little);

        if (pixels instanceof byte[]) {
            return makeImage((byte[]) pixels, w, h, c, interleaved, signed);
        }
        else if (pixels instanceof short[]) {
            return makeImage((short[]) pixels, w, h, c, interleaved, signed);
        }
        else if (pixels instanceof int[]) {
            return makeImage((int[]) pixels, w, h, c, interleaved, signed);
        }
        else if (pixels instanceof float[]) {
            return makeImage((float[]) pixels, w, h, c, interleaved);
        }
        else if (pixels instanceof double[]) {
            return makeImage((double[]) pixels, w, h, c, interleaved);
        }
        return null;
    }

    /**
     * Creates an image from the given raw byte array,
     * performing any necessary type conversions.
     *
     * @param data Array containing image data, one channel per element.
     * @param w Width of image plane.
     * @param h Height of image plane.
     * @param bpp Denotes the number of bytes in the returned primitive type
     *   (e.g. if bpp == 2, we should return an array of type short).
     * @param fp If set and bpp == 4 or bpp == 8, then return floats or doubles.
     * @param little Whether byte array is in little-endian order.
     * @param signed Whether the data values should be treated as signed
     *   instead of unsigned.
     */
    public static BufferedImage makeImage(byte[][] data,
            int w, int h, int bpp, boolean fp, boolean little, boolean signed)
    {
        int c = data.length;
        Object v = null;
        for (int i=0; i<c; i++) {
            Object pixels = DataTools.makeDataArray(data[i],
                    bpp % 3 == 0 ? bpp / 3 : bpp, fp, little);
            if (pixels instanceof byte[]) {
                if (v == null) v = new byte[c][];
                ((byte[][]) v)[i] = (byte[]) pixels;
            }
            else if (pixels instanceof short[]) {
                if (v == null) v = new short[c][];
                ((short[][]) v)[i] = (short[]) pixels;
            }
            else if (pixels instanceof int[]) {
                if (v == null) v = new int[c][];
                ((int[][]) v)[i] = (int[]) pixels;
            }
            else if (pixels instanceof float[]) {
                if (v == null) v = new float[c][];
                ((float[][]) v)[i] = (float[]) pixels;
            }
            else if (pixels instanceof double[]) {
                if (v == null) v = new double[c][];
                ((double[][]) v)[i] = (double[]) pixels;
            }
        }
        if (v instanceof byte[][]) {
            return makeImage((byte[][]) v, w, h, signed);
        }
        else if (v instanceof short[][]) {
            return makeImage((short[][]) v, w, h, signed);
        }
        else if (v instanceof int[][]) {
            return makeImage((int[][]) v, w, h, signed);
        }
        else if (v instanceof float[][]) {
            return makeImage((float[][]) v, w, h);
        }
        else if (v instanceof double[][]) {
            return makeImage((double[][]) v, w, h);
        }
        return null;
    }

    public static BufferedImage makeRGBImage(byte[] data, int c, int w, int h,
            boolean interleaved)
    {
        int cc = Math.min(c, 4); // throw away channels beyond 4
        int[] buf = new int[data.length / c];
        int nBits = (cc - 1) * 8;

        for (int i=0; i<buf.length; i++) {
            for (int q=0; q<cc; q++) {
                if (interleaved) {
                    buf[i] |= ((data[i*c + q] & 0xff) << (nBits - q*8));
                }
                else {
                    buf[i] |= ((data[q*buf.length + i] & 0xff) << (nBits - q*8));
                }
            }
        }

        DataBuffer buffer = new DataBufferInt(buf, buf.length);
        return constructImage(cc, DataBuffer.TYPE_INT, w, h, false, false, buffer);
    }

    public static BufferedImage makeRGBImage(byte[][] data, int w, int h) {
        int[] buf = new int[data[0].length];
        int nBits = (data.length - 1) * 8;

        for (int i=0; i<buf.length; i++) {
            for (int q=0; q<data.length; q++) {
                buf[i] |= ((data[q][i] & 0xff) << (nBits - q*8));
            }
        }

        DataBuffer buffer = new DataBufferInt(buf, buf.length);
        return constructImage(data.length, DataBuffer.TYPE_INT, w, h, false,
                false, buffer);
    }


    /** Creates an image with the given DataBuffer. */
    public static BufferedImage constructImage(int c, int type, int w,
            int h, boolean interleaved, boolean banded, DataBuffer buffer)
    {
        return constructImage(c, type, w, h, interleaved, banded, buffer, null);
    }

    /** Creates an image with the given DataBuffer. */
    public static BufferedImage constructImage(int c, int type, int w,
            int h, boolean interleaved, boolean banded, DataBuffer buffer,
            ColorModel colorModel)
    {
        if (c > 4) {
            throw new IllegalArgumentException(
                    "Cannot construct image with " + c + " channels");
        }
        if (colorModel == null || colorModel instanceof DirectColorModel) {
            colorModel = makeColorModel(c, type);
            if (colorModel == null) return null;
            if (buffer instanceof UnsignedIntBuffer) {
                try {
                    colorModel = new UnsignedIntColorModel(32, type, c);
                }
                catch (IOException e) {
                    return null;
                }
            }
        }

        SampleModel model;
        if (c > 2 && type == DataBuffer.TYPE_INT && buffer.getNumBanks() == 1 &&
                !(buffer instanceof UnsignedIntBuffer))
        {
            int[] bitMasks = new int[c];
            for (int i=0; i<c; i++) {
                bitMasks[i] = 0xff << ((c - i - 1) * 8);
            }
            model =
                    new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, w, h, bitMasks);
        }
        else if (banded) model = new BandedSampleModel(type, w, h, c);
        else if (interleaved) {
            int[] bandOffsets = new int[c];
            for (int i=0; i<c; i++) bandOffsets[i] = i;
            model = new PixelInterleavedSampleModel(type,
                    w, h, c, c * w, bandOffsets);
        }
        else {
            int[] bandOffsets = new int[c];
            for (int i=0; i<c; i++) bandOffsets[i] = i * w * h;
            model = new ComponentSampleModel(type, w, h, 1, w, bandOffsets);
        }

        WritableRaster raster = Raster.createWritableRaster(model, buffer, null);

        BufferedImage b = null;

        if (c == 1 && type == DataBuffer.TYPE_BYTE &&
                !(buffer instanceof SignedByteBuffer))
        {
            if (colorModel instanceof IndexColorModel) {
                b = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED);
            }
            else {
                b = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
            }
            b.setData(raster);
        }
        else if (c == 1 && type == DataBuffer.TYPE_USHORT) {
            if (!(colorModel instanceof IndexColorModel)) {
                b = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY);
                b.setData(raster);
            }
        }
        else if (c > 2 && type == DataBuffer.TYPE_INT && buffer.getNumBanks() == 1
                && !(buffer instanceof UnsignedIntBuffer))
        {
            if (c == 3) {
                b = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            }
            else if (c == 4) {
                b = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            }

            if (b != null) b.setData(raster);
        }

        if (b == null) b = new BufferedImage(colorModel, raster, false, null);

        return b;
    }

    // -- Color model --

    /** Gets a color space for the given number of color components. */
    public static ColorSpace makeColorSpace(int c) {
        int type;
        switch (c) {
        case 1:
            type = ColorSpace.CS_GRAY;
            break;
        case 2:
            type = TwoChannelColorSpace.CS_2C;
            break;
        case 3:
            type = ColorSpace.CS_sRGB;
            break;
        case 4:
            type = ColorSpace.CS_sRGB;
            break;
        default:
            return null;
        }
        return TwoChannelColorSpace.getInstance(type);
    }

    /** Gets a color model for the given number of color components. */
    public static ColorModel makeColorModel(int c, int dataType) {
        ColorSpace cs = makeColorSpace(c);
        return cs == null ? null : new ComponentColorModel(cs,
                c == 4, false, Transparency.TRANSLUCENT, dataType);
    }

    /**
     * Creates an image from the given byte array, using the given
     * IFormatReader to retrieve additional information.
     */
    public static BufferedImage openImage(byte[] buf, IFormatReader r,
            int w, int h) throws FormatException, IOException
    {

        int pixelType = r.getPixelType();
        boolean little = r.isLittleEndian();
        boolean normal = r.isNormalized();
        int rgbChanCount = r.getRGBChannelCount();
        boolean interleaved = r.isInterleaved();
        boolean indexed = r.isIndexed();

        if (pixelType == FormatTools.FLOAT) {
            float[] f = (float[]) DataTools.makeDataArray(buf, 4, true, little);
            if (normal) f = DataTools.normalizeFloats(f);
            return makeImage(f, w, h, rgbChanCount, interleaved);
        }
        else if (pixelType == FormatTools.DOUBLE) {
            double[] d = (double[]) DataTools.makeDataArray(buf, 8, true, little);
            if (normal) d = DataTools.normalizeDoubles(d);
            return makeImage(d, w, h, rgbChanCount, interleaved);
        }

        boolean signed = FormatTools.isSigned(pixelType);
        ColorModel model = null;

        if (signed) {
            if (pixelType == FormatTools.INT8) {
                model = new SignedColorModel(8, DataBuffer.TYPE_BYTE, rgbChanCount);
            }
            else if (pixelType == FormatTools.INT16) {
                model = new SignedColorModel(16, DataBuffer.TYPE_SHORT, rgbChanCount);
            }
            else if (pixelType == FormatTools.INT32) {
                model = new SignedColorModel(32, DataBuffer.TYPE_INT, rgbChanCount);
            }
        }

        int bpp = FormatTools.getBytesPerPixel(pixelType);
        BufferedImage b = makeImage(buf, w, h, rgbChanCount,
                interleaved, bpp, false, little, signed);
        if (b == null) {
            throw new FormatException("Could not construct BufferedImage");
        }

        if (indexed && rgbChanCount == 1) {
            if (pixelType == FormatTools.UINT8 || pixelType == FormatTools.INT8) {
                byte[][] table = r.get8BitLookupTable();
                if (table != null && table.length > 0 && table[0] != null) {
                    int len = table[0].length;
                    byte[] dummy = table.length < 3 ? new byte[len] : null;
                    byte[] red = table.length >= 1 ? table[0] : dummy;
                    byte[] green = table.length >= 2 ? table[1] : dummy;
                    byte[] blue = table.length >= 3 ? table[2] : dummy;
                    model = new IndexColorModel(8, len, red, green, blue);
                }
            }
            else if (pixelType == FormatTools.UINT16 ||
                    pixelType == FormatTools.INT16)
            {
                short[][] table = r.get16BitLookupTable();
                if (table != null && table.length > 0 && table[0] != null) {
                    model = new Index16ColorModel(16, table[0].length, table,
                            r.isLittleEndian());
                }
            }
        }

        if (model != null) {
            WritableRaster raster = Raster.createWritableRaster(b.getSampleModel(),
                    b.getRaster().getDataBuffer(), null);
            b = new BufferedImage(model, raster, false, null);
        }

        return b;
    }

}