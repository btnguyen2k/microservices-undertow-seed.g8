package com.github.btnguyen2k.mus.samples.petstore;

import com.github.btnguyen2k.mus.samples.petstore.bo.CategoryBo;
import com.github.btnguyen2k.mus.samples.petstore.bo.ICategoryDao;
import com.github.btnguyen2k.mus.samples.petstore.bo.IPetDao;
import com.github.btnguyen2k.mus.samples.petstore.bo.PetBo;
import com.github.btnguyen2k.mus.utils.SpringBeanUtils;
import com.github.ddth.dao.utils.DaoResult;
import com.github.ddth.recipes.apiservice.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PetStore: Pet APIs.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since template-v2.0.r3
 */
public class PetstoreApis {
    public static class ListAllCategories implements IApiHandler {
        @Operation(operationId = "listCategories", summary = "Return all categories.", method = "get:/api/petstore/categories", tags = {
                "petstore" })
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            ICategoryDao dao = SpringBeanUtils.getBean(ICategoryDao.class);
            List<Object> allCategories = dao.getAllCategories().stream().map(bo -> bo.toMap())
                    .collect(Collectors.toCollection(() -> new ArrayList<>()));
            return ApiResult.resultOk(allCategories);
        }
    }

    public static class CreateCategory implements IApiHandler {
        @Operation(operationId = "createCategory", summary = "Create a new category.", method = "post:/api/petstore/category", tags = {
                "petstore" }, parameters = {
                @Parameter(name = "name", description = "Category's name", required = true, in = ParameterIn.DEFAULT, schema = @Schema(type = "string")) })
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            String name = params.getParam("name", String.class);
            if (StringUtils.isBlank(name)) {
                return new ApiResult(ApiResult.STATUS_ERROR_CLIENT, "Category name must not be empty.");
            }
            ICategoryDao dao = SpringBeanUtils.getBean(ICategoryDao.class);
            CategoryBo bo = new CategoryBo(name);
            DaoResult result = dao.create(bo);
            return result.getStatus() == DaoResult.DaoOperationStatus.SUCCESSFUL ?
                    ApiResult.resultOk(bo.toMap()) :
                    new ApiResult(ApiResult.STATUS_ERROR_SERVER, result.toString());
        }
    }

    public static class GetCategory implements IApiHandler {
        @Operation(operationId = "getCategory", summary = "Get an existing category's info.", method = "get:/api/petstore/category/{id}", tags = {
                "petstore" }, parameters = {
                @Parameter(name = "id", description = "Category's unique id", required = true, in = ParameterIn.PATH, schema = @Schema(type = "string")) })
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            String id = params.getParam("id", String.class);
            ICategoryDao dao = SpringBeanUtils.getBean(ICategoryDao.class);
            CategoryBo bo = dao.getCategory(id);
            return bo != null ? ApiResult.resultOk(bo.toMap()) : ApiResult.DEFAULT_RESULT_NOT_FOUND;
        }
    }

    public static class DeleteCategory implements IApiHandler {
        @Operation(operationId = "deleteCategory", summary = "Delete an existing category.", method = "delete:/api/petstore/category/{id}", tags = {
                "petstore" }, parameters = {
                @Parameter(name = "id", description = "Category's unique id", required = true, in = ParameterIn.PATH, schema = @Schema(type = "string")) })
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            String id = params.getParam("id", String.class);
            ICategoryDao dao = SpringBeanUtils.getBean(ICategoryDao.class);
            CategoryBo bo = dao.getCategory(id);
            if (bo == null) {
                return ApiResult.DEFAULT_RESULT_NOT_FOUND;
            }
            DaoResult result = dao.delete(bo);
            return result.getStatus() == DaoResult.DaoOperationStatus.SUCCESSFUL ?
                    ApiResult.resultOk(bo.toMap()) :
                    new ApiResult(ApiResult.STATUS_ERROR_SERVER, result.toString());
        }
    }

    public static class UpdateCategory implements IApiHandler {
        @Operation(operationId = "updateCategory", summary = "Update an existing category.", method = "put:/api/petstore/category/{id}", tags = {
                "petstore" }, parameters = {
                @Parameter(name = "id", description = "Category's unique id", required = true, in = ParameterIn.PATH, schema = @Schema(type = "string")),
                @Parameter(name = "name", description = "Category's name", in = ParameterIn.DEFAULT, schema = @Schema(type = "string")) })
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            String id = params.getParam("id", String.class);
            ICategoryDao dao = SpringBeanUtils.getBean(ICategoryDao.class);
            CategoryBo bo = dao.getCategory(id);
            if (bo == null) {
                return ApiResult.DEFAULT_RESULT_NOT_FOUND;
            }
            String name = params.getParam("name", String.class);
            if (StringUtils.isBlank(name)) {
                return new ApiResult(ApiResult.STATUS_ERROR_CLIENT, "Category name must not be empty.");
            }
            bo.setName(name);
            DaoResult result = dao.update(bo);
            return result.getStatus() == DaoResult.DaoOperationStatus.SUCCESSFUL ?
                    ApiResult.resultOk(bo.toMap()) :
                    new ApiResult(ApiResult.STATUS_ERROR_SERVER, result.toString());
        }
    }

    /*----------------------------------------------------------------------*/

    public static class ListAllPets implements IApiHandler {
        @Operation(operationId = "listPets", summary = "Return all pets.", method = "get:/api/petstore/pets", tags = {
                "petstore" })
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            IPetDao dao = SpringBeanUtils.getBean(IPetDao.class);
            List<Object> allCategories = dao.getAllPets().stream().map(bo -> bo.toMap())
                    .collect(Collectors.toCollection(() -> new ArrayList<>()));
            return ApiResult.resultOk(allCategories);
        }
    }

    public static class CreatePet implements IApiHandler {
        @Operation(operationId = "createPet", summary = "Create a new pet.", method = "post:/api/petstore/pet", tags = {
                "petstore" }, parameters = {
                @Parameter(name = "name", description = "Pet's name", required = true, in = ParameterIn.DEFAULT, schema = @Schema(type = "string")),
                @Parameter(name = "category", description = "Pet's category-id", in = ParameterIn.DEFAULT, schema = @Schema(type = "string")) })
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            String name = params.getParam("name", String.class);
            if (StringUtils.isBlank(name)) {
                return new ApiResult(ApiResult.STATUS_ERROR_CLIENT, "Pet name must not be empty.");
            }
            String catId = params.getParam("category", String.class);
            CategoryBo cat = null;
            if (!StringUtils.isBlank(catId)) {
                ICategoryDao catDao = SpringBeanUtils.getBean(ICategoryDao.class);
                cat = catDao.getCategory(catId);
                if (cat == null) {
                    return new ApiResult(ApiResult.STATUS_ERROR_CLIENT, "Category [" + catId + "] does not exist.");
                }
            }

            IPetDao dao = SpringBeanUtils.getBean(IPetDao.class);
            PetBo bo = new PetBo(name);
            if (cat != null) {
                bo.setCategory(cat);
            }
            DaoResult result = dao.create(bo);
            return result.getStatus() == DaoResult.DaoOperationStatus.SUCCESSFUL ?
                    ApiResult.resultOk(bo.toMap()) :
                    new ApiResult(ApiResult.STATUS_ERROR_SERVER, result.toString());
        }
    }

    public static class GetPet implements IApiHandler {
        @Operation(operationId = "getPet", summary = "Get an existing pet's info.", method = "get:/api/petstore/pet/{id}", tags = {
                "petstore" }, parameters = {
                @Parameter(name = "id", description = "Pet's unique id", required = true, in = ParameterIn.PATH, schema = @Schema(type = "string")) })
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            String id = params.getParam("id", String.class);
            IPetDao dao = SpringBeanUtils.getBean(IPetDao.class);
            PetBo bo = dao.getPet(id);
            return bo != null ? ApiResult.resultOk(bo.toMap()) : ApiResult.DEFAULT_RESULT_NOT_FOUND;
        }
    }

    public static class DeletePet implements IApiHandler {
        @Operation(operationId = "deletePet", summary = "Delete an existing pet.", method = "delete:/api/petstore/pet/{id}", tags = {
                "petstore" }, parameters = {
                @Parameter(name = "id", description = "Pet's unique id", required = true, in = ParameterIn.PATH, schema = @Schema(type = "string")) })
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            String id = params.getParam("id", String.class);
            IPetDao dao = SpringBeanUtils.getBean(IPetDao.class);
            PetBo bo = dao.getPet(id);
            if (bo == null) {
                return ApiResult.DEFAULT_RESULT_NOT_FOUND;
            }
            DaoResult result = dao.delete(bo);
            return result.getStatus() == DaoResult.DaoOperationStatus.SUCCESSFUL ?
                    ApiResult.resultOk(bo.toMap()) :
                    new ApiResult(ApiResult.STATUS_ERROR_SERVER, result.toString());
        }
    }

    public static class UpdatePet implements IApiHandler {
        @Operation(operationId = "updatePet", summary = "Update an existing pet.", method = "put:/api/petstore/pet/{id}", tags = {
                "petstore" }, parameters = {
                @Parameter(name = "id", description = "Pet's unique id", required = true, in = ParameterIn.PATH, schema = @Schema(type = "string")),
                @Parameter(name = "name", description = "Pet's name", in = ParameterIn.DEFAULT, schema = @Schema(type = "string")),
                @Parameter(name = "category", description = "Pet's category-id", in = ParameterIn.DEFAULT, schema = @Schema(type = "string")),
                @Parameter(name = "status", description = "Pet's status", in = ParameterIn.DEFAULT, schema = @Schema(type = "string", allowableValues = {
                        "AVAILABLE", "PENDING", "SOLD" })) })
        @Override
        public ApiResult handle(ApiContext context, ApiAuth auth, ApiParams params) throws Exception {
            String id = params.getParam("id", String.class);
            IPetDao dao = SpringBeanUtils.getBean(IPetDao.class);
            PetBo bo = dao.getPet(id);
            if (bo == null) {
                return ApiResult.DEFAULT_RESULT_NOT_FOUND;
            }

            String name = params.getParam("name", String.class);
            String status = params.getParam("status", String.class);
            PetBo.PetStatus petStatus = null;
            if (!StringUtils.isBlank(status)) {
                try {
                    petStatus = PetBo.PetStatus.valueOf(status);
                } catch (IllegalArgumentException e) {
                    petStatus = null;
                }
                if (petStatus == null) {
                    return new ApiResult(ApiResult.STATUS_ERROR_CLIENT, "Invalid pet status.");
                }
            }
            String catId = params.getParam("category", String.class);
            CategoryBo cat = null;
            if (!StringUtils.isBlank(catId)) {
                ICategoryDao catDao = SpringBeanUtils.getBean(ICategoryDao.class);
                cat = catDao.getCategory(catId);
                if (cat == null) {
                    return new ApiResult(ApiResult.STATUS_ERROR_CLIENT, "Category [" + catId + "] does not exist.");
                }
            }

            if (!StringUtils.isBlank(name)) {
                bo.setName(name);
            }
            if (petStatus != null) {
                bo.setStatus(petStatus);
            }
            if (cat != null) {
                bo.setCategory(cat);
            }

            DaoResult result = dao.update(bo);
            return result.getStatus() == DaoResult.DaoOperationStatus.SUCCESSFUL ?
                    ApiResult.resultOk(bo.toMap()) :
                    new ApiResult(ApiResult.STATUS_ERROR_SERVER, result.toString());
        }
    }
}
