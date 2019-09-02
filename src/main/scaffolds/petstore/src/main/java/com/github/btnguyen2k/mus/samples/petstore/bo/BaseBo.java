package com.github.btnguyen2k.mus.samples.petstore.bo;

import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.dao.BaseDataJsonFieldBo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PetStore model: base class for models.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r3
 */
public class BaseBo extends BaseDataJsonFieldBo {
    private String id;

    public BaseBo() {
        this(null);
    }

    public BaseBo(String id) {
        setId(id != null ? id : UUID.randomUUID().toString());
    }

    /*----------------------------------------------------------------------*/

    public Map<String, Object> toMap() {
        Map<String, Object> result = SerializationUtils.fromJson(getDataAttrs(), Map.class);
        if (result == null) {
            result = new HashMap<>();
        }
        result.put("id", getId());
        return result;
    }

    public String getId() {
        return id;
    }

    public BaseBo setId(String id) {
        this.id = id != null ? id.trim().toLowerCase() : null;
        return this;
    }
}
