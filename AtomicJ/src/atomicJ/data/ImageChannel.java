
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.data;

import atomicJ.data.units.Quantity;

public class ImageChannel extends Channel2DStandard
{
    private final boolean isTrace;
    private final String filter;

    private int duplicationCount = 0;

    public ImageChannel(double[][] gridData, Grid2D grid, Quantity zQuantity, String identifier, boolean isTrace)
    {
        this(new GridChannel2DData(gridData, grid, zQuantity), identifier, "", isTrace, "");        
    }

    public ImageChannel(double[][] gridData, Grid2D grid, Quantity zQuantity, String identifier, boolean isTrace, ChannelMetadata channelMetadata)
    {
        this(new GridChannel2DData(gridData, grid, zQuantity), identifier, "", isTrace, channelMetadata);        
    }

    public ImageChannel(Channel2DData channelData, String identifier, boolean isTrace)
    {
        this(channelData, identifier, "", isTrace, "");        
    }

    public ImageChannel(Channel2DData channelData, String identifier, String filter, boolean isTrace)
    {
        this(channelData, identifier, filter, isTrace, "");        
    }

    public ImageChannel(Channel2DData channelData, String identifier, String filter, boolean isTrace, String info)
    {
        super(channelData, info, identifier);

        this.filter = filter;
        this.isTrace = isTrace;
    }

    public ImageChannel(Channel2DData channelData, String identifier, String filter, boolean isTrace, ChannelMetadata channelMetadata)
    {
        super(channelData, "", identifier, channelMetadata);

        this.filter = filter;
        this.isTrace = isTrace;
    }

    public ImageChannel(ImageChannel channelOld) 
    {
        this(channelOld, channelOld.getIdentifier());
    }

    public ImageChannel(ImageChannel channelOld, String identifierNew)
    {
        super(channelOld, identifierNew);

        this.filter = channelOld.filter;
        this.isTrace = channelOld.isTrace;       
    }

    public ImageChannel(ImageChannelBuilder builder)
    {
        super(builder.buildData(), "", builder.identifier);
        this.filter = builder.filter;
        this.isTrace = builder.isTrace;
    }

    @Override
    public ImageChannel getCopy()
    {
        return new ImageChannel(this);
    }

    @Override
    public ImageChannel duplicate()
    {
        String identifierNew = getIdentifier() + " (" + String.valueOf(duplicationCount + 1) + ")";
        this.duplicationCount = duplicationCount + 1;

        return new ImageChannel(this, identifierNew);
    }

    @Override
    public ImageChannel duplicate(String identifierNew)
    {
        this.duplicationCount = duplicationCount + 1;
        return new ImageChannel(this, identifierNew);
    }

    public String getFilter()
    {
        return filter;
    }

    public boolean isTrace()
    {
        return isTrace;
    }

    public static class ImageChannelBuilder
    {
        private boolean isTrace;
        private Quantity quantity;
        private String filter = "";
        private String identifier;
        private double range;

        private Grid2D grid;
        private double[][] data;

        public ImageChannelBuilder setTrace(boolean isTrace)
        {
            this.isTrace = isTrace;
            return this;
        }

        public Quantity getQuantity()
        {
            return quantity;
        }

        public ImageChannelBuilder setZQuantity(Quantity quantity)
        {
            this.quantity = quantity;
            return this;
        }

        public String getFilter()
        {
            return filter;
        }

        public ImageChannelBuilder setFilter(String filter)
        {
            this.filter = filter;
            return this;
        }

        public String getIdentifier()
        {
            return identifier;
        }

        public ImageChannelBuilder setIdentifier(String identifier)
        {
            this.identifier = identifier;
            return this;
        }

        public double getRange()
        {
            return range;
        }

        public ImageChannelBuilder setRange(double range)
        {
            this.range = range;
            return this;
        }

        public Grid2D getGrid()
        {
            return grid;
        }

        public ImageChannelBuilder setGrid(Grid2D grid)
        {
            this.grid = grid;
            return this;
        }

        public double[][] getData()
        {
            return data;
        }

        public ImageChannelBuilder setData(double[][] data)
        {
            this.data = data;
            return this;
        }

        public boolean canBeBuilt()
        {
            boolean isFinished = (quantity!= null) &&
                    identifier!= null && grid!= null && data!= null;
            return isFinished;
        }

        public GridChannel2DData buildData()
        {
            return new GridChannel2DData(data, grid, quantity);
        }

        public ImageChannel build()
        {
            ImageChannel imageChannel = new ImageChannel(this);
            return imageChannel;
        }
    }
}