package net.kkolyan.jhole2.log;

import net.kkolyan.jhole2.utils.StreamUtils;
import org.h2.api.AggregateFunction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author nplekhanov
 */
public class ConcatBlob implements AggregateFunction {
    private ByteArrayOutputStream buffer;
    @Override
    public void init(Connection conn) throws SQLException {
        buffer = new ByteArrayOutputStream();
    }

    @Override
    public int getType(int[] inputTypes) throws SQLException {
        return Types.VARCHAR;
    }

    @Override
    public void add(Object value) throws SQLException {
        InputStream data = (InputStream) value;
        try {
            StreamUtils.pump(data, buffer);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Object getResult() throws SQLException {
        try {
            return buffer.toString("utf8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
