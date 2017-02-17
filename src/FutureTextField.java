import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

import java.io.Reader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Joshua Hight
 *         A custom Lucene field that accepts a Future String as its data
 *         so long as the Future will be complete by the time Lucene wants
 *         to index this field. It should be noted that Lucene indexes Fields
 *         in the same order they are added to a Document
 */
class FutureTextField extends Field
{
    private static final Logger m_logger = Logger.getLogger(FutureTextField.class.getName());


    private static final FieldType myType = new FieldType();

    static
    {
        myType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        myType.setTokenized(true);
        myType.setStored(true);
        myType.freeze();
    }

    private final String name;
    private final Future<String> future;
    private String present;

    FutureTextField(String fieldName, Future<String> fortune)
    {
        super(fieldName, myType);
        name = fieldName;
        future = fortune;
    }


    @Override
    public String name()
    {
        return name;
    }


    @Override
    public String stringValue()
    {
        if (present == null)
        {
            try
            {
                present = future.get();
            }
            catch (InterruptedException | ExecutionException e)
            {
                m_logger.log(Level.WARNING, "Futuretextfield encountered an exception trying to get its value", e);
                present = "";
            }
        }
        return present;
    }

    @Override
    public Reader readerValue()
    {
        return null;
    }

    @Override
    public Number numericValue()
    {
        return null;
    }
}
