package com.github.btnguyen2k.mus.samples.petstore;

import java.util.HashMap;
import java.util.Map;

/**
 * PetStore model: Pet.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r3
 */
public class PetModel extends BaseModel {
    public enum PetStatus {
        AVAILABLE(0), PENDING(1), SOLD(2);

        private int value;

        PetStatus(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    private String name;
    private CategoryModel category;
    private PetStatus status = PetStatus.AVAILABLE;

    public PetModel(String name) {
        this(null, name);
    }

    public PetModel(String id, String name) {
        super(id);
        setName(name);
    }

    /*----------------------------------------------------------------------*/

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>(super.toMap());
        map.put("name", name);
        return map;
    }

    public String getName() {
        return name;
    }

    public PetModel setName(String name) {
        this.name = name != null ? name.trim() : null;
        return this;
    }

    public CategoryModel getCategory() {
        return category;
    }

    public PetModel setCategory(CategoryModel category) {
        this.category = category;
        return this;
    }

    public PetStatus getStatus() {
        return status;
    }

    public PetModel setStatus(PetStatus status) {
        this.status = status;
        return this;
    }
}
