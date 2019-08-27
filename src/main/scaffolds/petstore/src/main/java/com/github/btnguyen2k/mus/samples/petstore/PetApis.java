package com.github.btnguyen2k.mus.samples.petstore;

import com.github.ddth.recipes.apiservice.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PetStore: Pet APIs.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r3
 */
public class PetApis {
    private static Map<String, PetModel> petStore = new HashMap<>();

    public static class ListAllPets implements IApiHandler {
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            List<Object> allPets = new ArrayList<>();
            petStore.forEach((id, pet) -> allPets.add(pet.toMap()));
            return ApiResult.resultOk(allPets);
        }
    }

    public static class CreatePet implements IApiHandler {
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            String name = params.getParam("name", String.class);
            if (StringUtils.isBlank(name)) {
                return new ApiResult(ApiResult.STATUS_ERROR_CLIENT, "Pet name must not be empty.");
            }
            PetModel pet = new PetModel(null, name);
            petStore.put(pet.getId(), pet);
            return ApiResult.resultOk(pet.toMap());
        }
    }

    public static class GetPet implements IApiHandler {
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            String id = params.getParam("id", String.class);
            PetModel pet = petStore.get(id);
            return pet != null ? ApiResult.resultOk(pet) : ApiResult.DEFAULT_RESULT_NOT_FOUND;
        }
    }

    public static class DeletePet implements IApiHandler {
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            String id = params.getParam("id", String.class);
            PetModel pet = petStore.remove(id);
            return pet != null ? ApiResult.resultOk(pet) : ApiResult.DEFAULT_RESULT_NOT_FOUND;
        }
    }

    public static class UpdatePet implements IApiHandler {
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            String id = params.getParam("id", String.class);
            PetModel pet = petStore.get(id);
            if (pet == null) {
                return ApiResult.DEFAULT_RESULT_NOT_FOUND;
            }
            String name = params.getParam("name", String.class);
            if (StringUtils.isBlank(name)) {
                return new ApiResult(ApiResult.STATUS_ERROR_CLIENT, "Pet name must not be empty.");
            }
            String status = params.getParam("status", String.class);
            PetModel.PetStatus petStatus = null;
            if (!StringUtils.isBlank(status)) {
                petStatus = PetModel.PetStatus.valueOf(status);
                if (petStatus == null) {
                    return new ApiResult(ApiResult.STATUS_ERROR_CLIENT, "Invalid status.");
                }
            }
            pet.setName(name);
            if (petStatus != null) {
                pet.setStatus(petStatus);
            }
            petStore.put(pet.getId(), pet);
            return ApiResult.resultOk(pet.toMap());
        }
    }
}
