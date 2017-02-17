import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.parser.ParsingReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.MessageDigest;

/**
 * Created by josh.hight on 10/18/16.
 */
public class SingleRead
{
    private static String md5Field = "md5";
    private static String contentsField = "contents";

    private static final char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'};
    public static void main(String[] args)
    {
        Document docTest = new Document();
        File test = new File("./test.txt");


        //Here we open exactly one InputStream, wrap it in our HashStream
        //and then wrap that HashStream in a Tika ParsingReader
        try(InputStream only = new FileInputStream(test))
        {
            //our fancy HashStream
            HashStreamWithFuture fancy = new HashStreamWithFuture(only);
            ParsingReader reader = new ParsingReader(fancy);
            TextField contents = new TextField("contents", reader);
            //Recall that Lucene indexes Fields in the same order
            //in which they are added so if we want the hash future
            //to be ready we need to make sure Lucene reads through
            //the contents of the file before it gets to the future
            //and therefore we must be sure to add the Field with
            //the Reader value before the Field with the future value
            docTest.add(contents);

            //Now we get the mystical substance that allows you to see into the future
            FutureTextField spice = new FutureTextField("md5", fancy.getHashFuture());
            //and add it to the document
            docTest.add(spice);

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
