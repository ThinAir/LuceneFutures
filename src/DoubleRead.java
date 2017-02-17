import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.UAX29URLEmailAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.parser.ParsingReader;

import java.io.*;
import java.nio.file.Paths;
import java.security.MessageDigest;

/**
 * Created by josh.hight on 10/18/16.
 */
public class DoubleRead
{
    private static String md5Field = "md5";
    private static String contentsField = "contents";

    private static final char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'};
    public static void main(String[] args)
    {
        Document docTest = new Document();
        File test = new File("./test.txt");


        byte[] hash = null;

        //First we read through the file once to generate the hash
        try( InputStream first = new FileInputStream(test))
        {
            byte[] buffer = new byte[0x100];
            MessageDigest md5 = MessageDigest.getInstance("md5");

            while (first.read(buffer) > 0)
            {
                md5.update(buffer);
            }

            hash = md5.digest();
        }
        catch (Exception e)
        {
            System.out.println("This is demo code, why are we getting an exception?");
        }

        //Then we generate the standard string encoding of the hash
        String strHash = hashToString(hash);

        //We add a TextField containing that hash string to the Document
        TextField hashField = new TextField("md5", strHash, Field.Store.YES);
        docTest.add(hashField);


        //Then we open another InputStream, wrap it in a Tika ParsingReader
        //and hand it over to Lucene to be indexed
        try(InputStream second = new FileInputStream(test))
        {
            ParsingReader reader = new ParsingReader(second);
            TextField contents = new TextField("contents", reader);
            docTest.add(contents);

            //Standard Lucene boilerplate
            IndexWriterConfig m_writerConfig = new IndexWriterConfig(new StandardAnalyzer());
            m_writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            Directory dir = FSDirectory.open(Paths.get("./luceneIndex"));
            IndexWriter writer = new IndexWriter(dir, m_writerConfig);

            //Then we tell Lucene to index that Document
            writer.addDocument(docTest);

        }
        catch (Exception e)
        {
            System.out.println("Oh no my sample code isn't running :(");
        }

    }

    //Because Java doesn't support unsigned bytes
    // we use a quick and dirty little method to
    //generate a standard hash string from a byte[]
    static String hashToString(byte[] hash)
    {
        StringBuilder toString = new StringBuilder(52);

        for (byte i : hash)
        {
            toString.append(hexChars[(i >> 4) & 0xf]);
            toString.append(hexChars[i & 0xf]);
        }

        return toString.toString();
    }
}
