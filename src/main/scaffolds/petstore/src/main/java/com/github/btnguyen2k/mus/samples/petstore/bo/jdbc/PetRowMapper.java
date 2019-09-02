package com.github.btnguyen2k.mus.samples.petstore.bo.jdbc;

import com.github.btnguyen2k.mus.samples.petstore.bo.PetBo;
import com.github.ddth.dao.jdbc.annotations.AnnotatedGenericRowMapper;
import com.github.ddth.dao.jdbc.annotations.ColumnAttribute;
import com.github.ddth.dao.jdbc.annotations.PrimaryKeyColumns;
import com.github.ddth.dao.jdbc.annotations.UpdateColumns;

/**
 * {@link com.github.ddth.dao.jdbc.IRowMapper} implementation that maps {@link java.sql.ResultSet} to {@link PetBo}
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-2.0.r3
 */
@ColumnAttribute(column = "pid", attr = "id", attrClass = String.class)
@ColumnAttribute(column = "pdata", attr = "data", attrClass = String.class)
@PrimaryKeyColumns({ "pid" })
@UpdateColumns({ "pdata" })
public class PetRowMapper extends AnnotatedGenericRowMapper<PetBo> {
    public final static String TABLE_NAME = "tbl_pet";
    public final static PetRowMapper INSTANCE = new PetRowMapper();
}
