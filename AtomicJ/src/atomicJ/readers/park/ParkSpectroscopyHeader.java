package atomicJ.readers.park;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import atomicJ.data.Grid2D;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.StandardQuantityTypes;


public class ParkSpectroscopyHeader
{
    private static final int MAX_SPECTROSCOPY_CHANNEL_COUNT = 8;

    private final List<ParkSpectroscopyChannel> channels = new ArrayList<>(); //8*112 = 896
    private final int spectrSourcesCount; //900
    private final int average; //904
    private final int dataPoints; // Number of data values in a spectrum  //908
    private final int pointCount;  // Number spectroscopy points //912
    private final int drivingSourceIndex; //916
    private final double forwardPeriod;  // In seconds  //924
    private final double backwardPeriod; // In seconds  //932
    private final double forwardSpeed;   // Driving source unit/second //940
    private final double backwardSpeed;  // Driving source unit/second  //948
    private final boolean volumeImage; //952

    protected ParkSpectroscopyHeader(ByteBuffer buffer)
    {
        for(int i = 0; i<MAX_SPECTROSCOPY_CHANNEL_COUNT; i++)
        {
            channels.add(ParkSpectroscopyChannel.readIn(buffer, i));
        }

        this.spectrSourcesCount = buffer.getInt();


        this.average = buffer.getInt();
        this.dataPoints = buffer.getInt(); //data in a line
        this.pointCount = buffer.getInt(); //number of (x,y)'s
        this.drivingSourceIndex = buffer.getInt();


        this.forwardPeriod = buffer.getFloat();
        this.backwardPeriod = buffer.getFloat();
        this.forwardSpeed = buffer.getFloat();
        this.backwardSpeed = buffer.getFloat();
        this.volumeImage = (buffer.getInt() != 0);

        for(int i = 0; i<MAX_SPECTROSCOPY_CHANNEL_COUNT; i++)
        {
            channels.get(i).setOffset(buffer.getDouble());
        }

        for(int i = 0; i<MAX_SPECTROSCOPY_CHANNEL_COUNT; i++)
        {
            boolean logScale = (buffer.getInt() != 0);
            channels.get(i).setLogScale(logScale);
        }

        for(int i = 0; i<MAX_SPECTROSCOPY_CHANNEL_COUNT; i++)
        {
            boolean square = (buffer.getInt() != 0);
            channels.get(i).setSquare(square);
        }
    }

    public boolean isWellSpecifiedGridMap()
    {
        return false;
    }

    public Grid2D buildGrid()
    {
        return null;
    }

    public int getDataInLineCount()
    {
        return dataPoints;
    }

    public int getRecordingPointCount()
    {
        return pointCount;
    }

    public int getDrivingChannelIndex()
    {
        return drivingSourceIndex;
    }

    public ParkSpectroscopyChannel getDrivingChannel()
    {
        return channels.get(drivingSourceIndex);
    }

    public int getSpectroscopyChannelsCount()
    {
        return spectrSourcesCount;
    }

    public boolean isVolumeImage()
    {
        return volumeImage;
    }

    public boolean hasReferenceImage()
    {
        return false;
    }

    public double getSpringConstant()
    {
        return Double.NaN;
    }

    public double getSensitivity()
    {
        return Double.NaN;
    }

    public ParkSpectroscopyChannel getSpectroscopyChannel(int index)
    {
        if(index < 0 || index >= spectrSourcesCount)
        {
            throw new IllegalArgumentException("The specified channel index " + index + " is outside the range" );
        }

        return channels.get(index);
    }

    public List<ParkSpectroscopyChannel> getPotentialYChannels()
    {
        List<ParkSpectroscopyChannel> channelsY = new ArrayList<>();

        for(int i = 0; i<spectrSourcesCount; i++)
        {
            ParkSpectroscopyChannel channel = this.channels.get(i);
            if(channel.isYAxisSource())
            {
                channelsY.add(channel);
            }
        }

        return channelsY;
    }

    public ParkSpectroscopyChannel getForceDistanceYChannel()
    {
        for(int i = 0; i<spectrSourcesCount; i++)
        {
            ParkSpectroscopyChannel channel = this.channels.get(i);
            if(channel.isYAxisSource())
            {
                //ampere is the unit of the y-channel of I-V curves
                PrefixedUnit unit = channel.getUnit();
                if(StandardQuantityTypes.VOLTAGE.isCompatible(unit) || StandardQuantityTypes.LENGTH.isCompatible(unit) || StandardQuantityTypes.FORCE.isCompatible(unit))
                    return channel;
            }
        }

        return null;
    }

    public List<ParkSpectroscopyChannel> getSpectroscopyChannels()
    {
        return channels.subList(0, spectrSourcesCount);
    }

    /*
     * 
typedef struct {
    PSIASpectroscopyChannel channel[PSIA_MAX_SPECTRO_CHANNEL];
    gint spect_sources;      /* Number of spectro sources (channels?) 
    gint average;
    gint res;                /* Number of data values in a spectrum 
    gint npoints;            /* Number spectroscopy points 
    gint driving_source_index;
    gdouble forward_period;  /* In seconds 
    gdouble backward_period; /* In seconds 
    gdouble forward_speed;   /* Driving source unit/second 
    gdouble backward_speed;  /* Driving source unit/second 
    gboolean volume_image;
    gdouble offset[PSIA_MAX_SPECTRO_CHANNEL];
    gboolean log_scale[PSIA_MAX_SPECTRO_CHANNEL];
    gboolean square[PSIA_MAX_SPECTRO_CHANNEL];
    /* Version 2+ only
    gint spec_point_per_x;   /* If volume_image && !spec_point_per_x, then
                                points are arranged in a square matrix 
    gboolean has_reference_image;
    gdouble xreal;     /* Grid dimensions 
    gdouble yreal;
    gdouble xoff;
    gdouble yoff;
    gdouble force_constant;      /* F-D, in Newton/meter 
    gdouble sensitivity;         /* F-D, in Volt/micrometer 
    gdouble force_limit;         /* F-D, in Volts 
    gdouble time_interval;       /* F-D, in seconds? 
    gdouble max_voltage;         /* I-V, in Volts 
    gdouble min_voltage;         /* I-V, in Volts 
    gdouble start_voltage;       /* I-V, in Volts 
    gdouble end_voltage;         /* I-V, in Volts 
    gdouble delayed_start_time;  /* I-V, in seconds? 
    gboolean z_servo;            /* I-V
    gdouble data_gain;
    gchar *w_unit;
    gboolean use_extended_header;
    PSIASpectroType spec_type;
    gdouble reset_level;           /* Photo-current information 
    gdouble reset_duration;
    gdouble operation_level;
    gdouble operation_duration;
    gdouble time_before_reset;
    gdouble time_after_reset;
    gdouble time_before_light_on;
    gdouble time_light_duration;
    gint reserved[30];
} PSIASpectroscopyHeader;
     */

    public static ParkSpectroscopyHeader readIn(ByteBuffer buffer)
    {
        return new ParkSpectroscopyHeader(buffer);
    }
}
