package com.github.btnguyen2k.mus.samples.petstore.bo;

import com.github.btnguyen2k.mus.utils.SpringBeanUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * PetStore model: Pet.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r3
 */
public class PetBo extends BaseBo {
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

    private CategoryBo category;
    private PetStatus status;

    public PetBo() {
    }

    public PetBo(String name) {
        this(null, name);
    }

    public PetBo(String id, String name) {
        super(id);
        setName(name);
        setStatus(PetStatus.AVAILABLE);
    }

    /*----------------------------------------------------------------------*/
    @Override
    protected void parseData() {
        super.parseData();
        try {
            this.status = PetStatus.valueOf(getDataAttr("status", String.class));
        } catch (IllegalArgumentException e) {
            this.status = null;
        }
        ICategoryDao catDao = SpringBeanUtils.getBean(ICategoryDao.class);
        this.category = catDao.getCategory(getDataAttr("category", String.class));
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>(super.toMap());
        map.put("category", category != null ? category.toMap() : null);
        return map;
    }

    public String getName() {
        return getDataAttr("name", String.class);
    }

    public PetBo setName(String name) {
        setDataAttr("name", name != null ? name.trim() : null);
        return this;
    }

    public CategoryBo getCategory() {
        return category;
    }

    public PetBo setCategory(CategoryBo category) {
        this.category = category;
        setDataAttr("category", category != null ? category.getId() : null);
        return this;
    }

    public PetStatus getStatus() {
        return status;
    }

    public PetBo setStatus(PetStatus status) {
        this.status = status;
        setDataAttr("status", status != null ? status.name() : null);
        return this;
    }
}
