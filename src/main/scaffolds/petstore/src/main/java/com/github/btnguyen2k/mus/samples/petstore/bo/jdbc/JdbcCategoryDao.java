package com.github.btnguyen2k.mus.samples.petstore.bo.jdbc;

import com.github.btnguyen2k.mus.samples.petstore.bo.CategoryBo;
import com.github.btnguyen2k.mus.samples.petstore.bo.ICategoryDao;
import com.github.ddth.dao.BoId;
import com.github.ddth.dao.jdbc.GenericBoJdbcDao;
import com.github.ddth.dao.utils.DaoResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * JDBC-implementation of {@link ICategoryDao}.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-2.0.r3
 */
public class JdbcCategoryDao extends GenericBoJdbcDao<CategoryBo> implements ICategoryDao {
    @Override
    public JdbcCategoryDao init() {
        if (getRowMapper() == null) {
            setRowMapper(CategoryRowMapper.INSTANCE);
        }
        if (getTableName() == null) {
            setTableName(CategoryRowMapper.TABLE_NAME);
        }
        super.init();
        return this;
    }

    @Override
    public CategoryBo getCategory(String id) {
        return get(new BoId(id));
    }

    @Override
    public Collection<CategoryBo> getAllCategories() {
        return getAll().collect(Collectors.toCollection(() -> new ArrayList<>()));
    }

    @Override
    public DaoResult create(CategoryBo cat) {
        return super.create(cat);
    }

    @Override
    public DaoResult delete(CategoryBo cat) {
        return super.delete(cat);
    }

    @Override
    public DaoResult update(CategoryBo cat) {
        return super.update(cat);
    }
}
