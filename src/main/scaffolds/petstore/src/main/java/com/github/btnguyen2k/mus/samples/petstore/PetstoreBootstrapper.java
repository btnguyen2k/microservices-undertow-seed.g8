package com.github.btnguyen2k.mus.samples.petstore;

import com.github.btnguyen2k.mus.samples.petstore.bo.jdbc.CategoryRowMapper;
import com.github.btnguyen2k.mus.samples.petstore.bo.jdbc.PetRowMapper;
import com.github.btnguyen2k.mus.utils.SpringBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Bootstrapper for Petstore demo.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r3
 */
public class PetstoreBootstrapper implements Runnable {
    private final Logger LOGGER = LoggerFactory.getLogger(PetstoreBootstrapper.class);

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        try (ResultSet rs = conn.getMetaData()
                .getTables(null, null, tableName.toUpperCase(), new String[] { "TABLE" })) {
            return rs.next();
        }
    }

    @Override
    public void run() {
        DataSource ds = SpringBeanUtils.getBean("DATASOURCE_PETSTORE", DataSource.class);
        try (Connection conn = ds.getConnection()) {
            if (!tableExists(conn, CategoryRowMapper.TABLE_NAME)) {
                LOGGER.warn("Creating table [" + CategoryRowMapper.TABLE_NAME + "]...");
                conn.createStatement().execute("CREATE TABLE " + CategoryRowMapper.TABLE_NAME
                        + "(cid VARCHAR(64), cdata CLOB, PRIMARY KEY (cid))");
            }

            if (!tableExists(conn, PetRowMapper.TABLE_NAME)) {
                LOGGER.warn("Creating table [" + PetRowMapper.TABLE_NAME + "]...");
                conn.createStatement().execute(
                        "CREATE TABLE " + PetRowMapper.TABLE_NAME + "(pid VARCHAR(64), pdata CLOB, PRIMARY KEY (pid))");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
