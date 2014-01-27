package net.kkolyan.jhole2.log;

import com.sun.org.omg.CORBA.OperationDescription;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author nplekhanov
 */
public class H2ApplicationLogger implements ApplicationLogger {
    private JdbcTemplate jdbcTemplate;
    private Long runtimeId;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public H2ApplicationLogger() {
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:jhole2_log");
        ds.setUsername("sa");
        ds.setPassword("sa");
        jdbcTemplate = new JdbcTemplate(ds);
        try {
            for (String statement: new String(IOUtils.readBytesAndClose(getClass().getClassLoader().getResourceAsStream("log_ddl.sql"), -1)).split(";")) {
                if (statement.trim().isEmpty()) {
                    continue;
                }
                jdbcTemplate.execute(statement);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        runtimeId = jdbcTemplate.queryForObject("select run_id_seq.nextval from dual", Long.class);
    }

    @Override
    public ConnectionLogger logConnection(String desc) {
        long connId = jdbcTemplate.queryForObject("select con_id_seq.nextval from dual", Long.class);
        jdbcTemplate.update("insert into con (id, time, desc, rt_id) values(?, CURRENT_TIMESTAMP(), ?, ?)", connId, desc, runtimeId);
        return new ConnectionLoggerImpl(connId);
    }

    public class ConnectionLoggerImpl implements ConnectionLogger {

        private long connId;

        private ConnectionLoggerImpl(long connId) {
            this.connId = connId;
        }

        @Override
        public void logClose(String direction) {
            jdbcTemplate.update("insert into con_close (con_id, time, dir) values (?, CURRENT_TIMESTAMP(), ?)", connId, direction);
        }

        @Override
        public void logTransfer(byte[] data, String direction) {
            jdbcTemplate.update("insert into tf (id, time, bytes, con_id, dir) values " +
                    "(tf_id_seq.nextval, CURRENT_TIMESTAMP(), ?, ?, ?)",
                    data, connId, direction);
        }

        @Override
        public void logEof(String direction) {
            jdbcTemplate.update("insert into con_eof (con_id, dir, time) values " +
                    "(?, ?, CURRENT_TIMESTAMP())",
                    connId, direction);
        }

        @Override
        public void logException(Exception ex) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(bos, true));
            jdbcTemplate.update("insert into con_exc (id, con_id, time, type, message, details) values " +
                    "(con_exc_id_seq.nextval, ?, CURRENT_TIMESTAMP(), ?, ?, ?)",
                    connId, ex.getClass().getName(), ex.getMessage(), bos.toString());
        }
    }

//    public QueryResult query(String query) {
//        return query(query, new Object[0]);
//    }

    public QueryResult query(String query, Object... args) {
        QueryResult result;
        try {
            result = jdbcTemplate.query(query, args, new ResultSetExtractor<QueryResult>() {
                @Override
                public QueryResult extractData(ResultSet rs) throws SQLException, DataAccessException {
                    List<List<?>> rows = new ArrayList<List<?>>();
                    List<String> columns = new ArrayList<String>();
                    int colCount = rs.getMetaData().getColumnCount();
                    for (int i = 0; i < colCount; i++) {
                        columns.add(rs.getMetaData().getColumnLabel(i + 1));
                    }
                    while (rs.next()) {
                        List<Object> row = new ArrayList<Object>();
                        for (int i = 0; i < colCount; i++) {
                            Object value = rs.getObject(i + 1);
                            row.add(value);
                        }
                        rows.add(row);
                    }
                    return new QueryResult(columns, rows);
                }
            });
        } catch (DataAccessException e) {
            logger.warn(e.toString(), e);
            result = new QueryResult(e);
        }
        String resultDetails;
        if (result.getException() == null) {
            resultDetails = result.rows.size() + "";
        } else {
            resultDetails = result.getException().toString();
        }
        jdbcTemplate.update("insert into q_hist (id, time, q, res) values " +
                "(q_hist_id_seq.nextval, CURRENT_TIMESTAMP(), ?, ?)",
                query, resultDetails);

        return result;
    }

    public static class QueryResult {
        private List<List<?>> rows;
        private List<String> columns;
        private DataAccessException exception;

        public QueryResult(DataAccessException exception) {
            this.exception = exception;
        }

        public DataAccessException getException() {
            return exception;
        }

        public QueryResult(List<String> columns, List<List<?>> rows) {
            this.columns = columns;
            this.rows = rows;
        }

        public List<List<?>> getRows() {
            return rows;
        }

        public List<String> getColumns() {
            return columns;
        }
    }
}
