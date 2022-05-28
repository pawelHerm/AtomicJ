package atomicJ.data;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class ChannelDomainIdentifier
{
    private static final AtomicLong domainTag = new AtomicLong();

    private final double dataDensity;
    private final Object domainKey;

    public ChannelDomainIdentifier(double dataDensity, Object domainKey)
    {
        this.domainKey = domainKey;
        this.dataDensity = dataDensity;
    }     

    public double getDataDensity()
    {
        return dataDensity;
    }

    public static synchronized long getNewDomainKey()
    {
        return domainTag.incrementAndGet();
    }

    @Override
    public int hashCode()
    {
        int hashCode = Objects.hashCode(domainKey);
        return hashCode;
    }

    public boolean equal(Object that)
    {
        if(that instanceof ChannelDomainIdentifier)
        {
            ChannelDomainIdentifier thatDomain = (ChannelDomainIdentifier)that;

            if(this.domainKey.equals(thatDomain.domainKey))
            {
                return false;
            }

            return true;
        }

        return false;
    }
}