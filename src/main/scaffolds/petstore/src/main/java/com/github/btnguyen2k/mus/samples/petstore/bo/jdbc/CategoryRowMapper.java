package com.github.btnguyen2k.mus.samples.petstore.bo.jdbc;

import com.github.btnguyen2k.mus.samples.petstore.bo.CategoryBo;
import com.github.ddth.dao.jdbc.annotations.AnnotatedGenericRowMapper;
import com.github.ddth.dao.jdbc.annotations.ColumnAttribute;
import com.github.ddth.dao.jdbc.annotations.PrimaryKeyColumns;
import com.github.ddth.dao.jdbc.annotations.UpdateColumns;

/**
 * {@link com.github.ddth.dao.jdbc.IRowMapper} implementation that maps {@link java.sql.ResultSet} to {@link CategoryBo}
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-2.0.r3
 */
@ColumnAttribute(column = "cid", attr = "id", attrClass = String.class)
@ColumnAttribute(column = "cdata", attr = "data", attrClass = String.class)
@PrimaryKeyColumns({ "cid" })
@UpdateColumns({ "cdata" })
public class CategoryRowMapper extends AnnotatedGenericRowMapper<CategoryBo> {
    public final static String TABLE_NAME = "tbl_category";
    public final static CategoryRowMapper INSTANCE = new CategoryRowMapper();
}
