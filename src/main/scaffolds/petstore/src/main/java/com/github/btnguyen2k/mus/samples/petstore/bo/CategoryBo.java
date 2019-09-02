package com.github.btnguyen2k.mus.samples.petstore.bo;

/**
 * PetStore model: Category.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r3
 */
public class CategoryBo extends BaseBo {
    public CategoryBo() {
    }

    public CategoryBo(String name) {
        this(null, name);
    }

    public CategoryBo(String id, String name) {
        super(id);
        setName(name);
    }

    public String getName() {
        return getDataAttr("name", String.class);
    }

    public CategoryBo setName(String name) {
        setDataAttr("name", name != null ? name.trim() : null);
        return this;
    }
}
