package com.github.btnguyen2k.mus.samples.petstore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PetStore model: base class for models.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r3
 */
public class BaseModel {
    private String id;

    public BaseModel() {
        this(null);
    }

    public BaseModel(String id) {
        setId(id != null ? id : UUID.randomUUID().toString());
    }

    /*----------------------------------------------------------------------*/

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        return map;
    }

    public String getId() {
        return id;
    }

    public BaseModel setId(String id) {
        this.id = id != null ? id.trim().toLowerCase() : null;
        return this;
    }
}
