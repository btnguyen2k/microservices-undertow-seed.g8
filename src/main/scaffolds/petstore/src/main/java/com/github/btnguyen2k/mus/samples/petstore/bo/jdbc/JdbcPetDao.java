package com.github.btnguyen2k.mus.samples.petstore.bo.jdbc;

import com.github.btnguyen2k.mus.samples.petstore.bo.IPetDao;
import com.github.btnguyen2k.mus.samples.petstore.bo.PetBo;
import com.github.ddth.dao.BoId;
import com.github.ddth.dao.jdbc.GenericBoJdbcDao;
import com.github.ddth.dao.utils.DaoResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * JDBC-implementation of {@link IPetDao}.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-2.0.r3
 */
public class JdbcPetDao extends GenericBoJdbcDao<PetBo> implements IPetDao {
    @Override
    public JdbcPetDao init() {
        if (getRowMapper() == null) {
            setRowMapper(PetRowMapper.INSTANCE);
        }
        if (getTableName() == null) {
            setTableName(PetRowMapper.TABLE_NAME);
        }
        super.init();
        return this;
    }

    @Override
    public PetBo getPet(String id) {
        return get(new BoId(id));
    }

    @Override
    public Collection<PetBo> getAllPets() {
        return getAll().collect(Collectors.toCollection(() -> new ArrayList<>()));
    }

    @Override
    public DaoResult create(PetBo pet) {
        return super.create(pet);
    }

    @Override
    public DaoResult delete(PetBo pet) {
        return super.delete(pet);
    }

    @Override
    public DaoResult update(PetBo pet) {
        return super.update(pet);
    }
}
