package com.github.btnguyen2k.mus.samples.petstore.bo;

import com.github.ddth.dao.utils.DaoResult;

import java.util.Collection;

/**
 * API to access pet storage.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-2.0.r3
 */
public interface IPetDao {
    /**
     * Get a pet by id.
     *
     * @param id
     * @return
     */
    PetBo getPet(String id);

    /**
     * Get all available pets.
     *
     * @return
     */
    Collection<PetBo> getAllPets();

    /**
     * Create a new pet.
     *
     * @param pet
     * @return
     */
    DaoResult create(PetBo pet);

    /**
     * Delete an existing pet.
     *
     * @param pet
     * @return
     */
    DaoResult delete(PetBo pet);

    /**
     * Update an existing pet.
     *
     * @param pet
     * @return
     */
    DaoResult update(PetBo pet);
}
