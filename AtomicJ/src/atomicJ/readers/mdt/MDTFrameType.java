package atomicJ.readers.mdt;

import atomicJ.data.ChannelFilter;

public enum MDTFrameType
{
    SCANNED_DATA_FRAME_TYPE(ScannedDataFrameReader.FRAME_TYPE) {
        @Override
        public MDTFrameReader getFrameReader(ChannelFilter filter) 
        {
            return new ScannedDataFrameReader(filter);
        }

        @Override
        public boolean canContainImageSources() {
            return true;
        }

        @Override
        public boolean canContainSpectroscopySources() {
            return false;
        }

        @Override
        public boolean canContainAnySources() {
            return true;
        }
    }, 

    MDA_DATA_FRAME_TYPE(MDAFrameGeneralReader.FRAME_TYPE) {
        @Override
        public MDTFrameReader getFrameReader(ChannelFilter filter) 
        {
            return new MDAFrameGeneralReader(filter);
        }

        @Override
        public boolean canContainImageSources() {
            return true;
        }

        @Override
        public boolean canContainSpectroscopySources() {
            return false;
        }

        @Override
        public boolean canContainAnySources() {
            return true;
        }
    },

    COMP_DATA_FRAME(CompDataFrameReader.FRAME_TYPE) 
    {
        @Override
        public MDTFrameReader getFrameReader(ChannelFilter filter) {
            return new CompDataFrameReader(filter);
        }

        @Override
        public boolean canContainImageSources() {
            return false;
        }

        @Override
        public boolean canContainSpectroscopySources() {
            return true;
        }

        @Override
        public boolean canContainAnySources() {
            return true;
        }
    },
    CURVES_DATA_FRAME_TYPE(CurvesDataFrameReader.FRAME_TYPE) {
        @Override
        public MDTFrameReader getFrameReader(ChannelFilter filter) 
        {
            return new CurvesDataFrameReader(filter);
        }

        @Override
        public boolean canContainImageSources() {
            return false;
        }

        @Override
        public boolean canContainSpectroscopySources() {
            return true;
        }

        @Override
        public boolean canContainAnySources() {
            return true;
        }
    }, 
    UNRECOGNIZED(UnrecognizedDataFrame.FRAME_TYPE) 
    {
        @Override
        public MDTFrameReader getFrameReader(ChannelFilter filter) {
            return new UnrecognizedDataFrame();
        }

        @Override
        public boolean canContainImageSources() {
            return false;
        }

        @Override
        public boolean canContainSpectroscopySources() {
            return false;
        }

        @Override
        public boolean canContainAnySources() {
            return false;
        }
    };

    private final int code;

    MDTFrameType(int code)
    {
        this.code = code;
    }

    public abstract boolean canContainImageSources();
    public abstract boolean canContainSpectroscopySources();
    public abstract boolean canContainAnySources();

    public abstract MDTFrameReader getFrameReader(ChannelFilter filter);

    public static MDTFrameType getMDTFrameType(int code)
    {
        for(MDTFrameType frameType : MDTFrameType.values())
        {
            if(frameType.code == code)
            {
                return frameType;
            }
        }

        return UNRECOGNIZED;
    }
}