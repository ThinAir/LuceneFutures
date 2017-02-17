import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by josh.hight on 10/18/16.
 */
public class HashStreamWithFuture extends InputStream
{
    InputStream m_source;
    MessageDigest md5;

    public HashStreamWithFuture(InputStream source) throws NoSuchAlgorithmException
    {
        m_source = source;
        md5 = MessageDigest.getInstance("md5");
    }


    @Override
    public int read() throws IOException
    {
        int retVal = m_source.read();

        if (retVal >= 0)
        {
            md5.update((byte) retVal);
        }

        return retVal;
    }

    public byte[] getHash()
    {
        return md5.digest();
    }

    Future<String> getHashFuture()
    {
        return new HashFuture();
    }

    private class HashFuture implements Future<String>
    {

        @Override
        public boolean cancel(boolean mayInterruptIfRunning)
        {
            return false;
        }

        @Override
        public boolean isCancelled()
        {
            return false;
        }

        @Override
        public boolean isDone()
        {
            return false;
        }

        //we only really care about this method
        @Override
        public String get() throws InterruptedException, ExecutionException
        {
            return SingleRead.hashToString(getHash());
        }

        //but this one is trivial to implement, so why not
        @Override
        public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
        {
            return get();
        }
    }

}
