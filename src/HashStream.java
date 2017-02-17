import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by josh.hight on 10/18/16.
 */
public class HashStream extends InputStream
{
    InputStream m_source;
    MessageDigest md5;

    public HashStream(InputStream source) throws NoSuchAlgorithmException
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
}
