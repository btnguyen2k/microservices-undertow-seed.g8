package com.github.btnguyen2k.mus.samples.petstore.bo;

import java.util.Collection;

import com.github.ddth.dao.utils.DaoResult;

/**
 * API to access user/usergroup storage.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-2.6.r5
 */
public interface ICategoryDao {
    /**
     * Get a category by id.
     *
     * @param id
     * @return
     */
    CategoryBo getCategory(String id);

    /**
     * Get all available categories.
     *
     * @return
     */
    Collection<CategoryBo> getAllCategories();

    /**
     * Create a new category.
     *
     * @param cat
     * @return
     */
    DaoResult create(CategoryBo cat);

    /**
     * Delete an existing category.
     *
     * @param cat
     * @return
     */
    DaoResult delete(CategoryBo cat);

    /**
     * Update an existing category.
     *
     * @param cat
     * @return
     */
    DaoResult update(CategoryBo cat);
}
