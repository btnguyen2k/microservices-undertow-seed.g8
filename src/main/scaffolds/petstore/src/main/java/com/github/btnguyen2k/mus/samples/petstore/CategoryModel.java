package com.github.btnguyen2k.mus.samples.petstore;

/**
 * PetStore model: Category.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r3
 */
public class CategoryModel extends BaseModel {
    private String name;

    public CategoryModel(String name) {
        this(null, name);
    }

    public CategoryModel(String id, String name) {
        super(id);
        setName(name);
    }

    public String getName() {
        return name;
    }

    public CategoryModel setName(String name) {
        this.name = name != null ? name.trim() : null;
        return this;
    }
}
