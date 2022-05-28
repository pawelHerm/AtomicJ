package atomicJ.gui.curveProcessing;

public interface CroppingReceiver
{
    public void setLeftCropping(double t);
    public void setRightCropping(double t);
    public void setLowerCropping(double t);
    public void setUpperCropping(double t);

    public static class DummyCroppingReceiver implements CroppingReceiver
    {
        private static DummyCroppingReceiver INSTANCE = new DummyCroppingReceiver();
        private DummyCroppingReceiver() {};

        public static DummyCroppingReceiver getInstance()
        {
            return INSTANCE;
        }

        @Override
        public void setLeftCropping(double t) {}

        @Override
        public void setRightCropping(double t) {}

        @Override
        public void setLowerCropping(double t) {}

        @Override
        public void setUpperCropping(double t) {}       
    }
}