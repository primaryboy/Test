package com.mall.service;

import com.mall.common.ServerResponse;
import com.mall.pojo.Category;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ICategoryService {
    ServerResponse addCategory (String categoryName, Integer parentId );
    ServerResponse updateCategoryName ( Integer categoryId, String categoryName );
    ServerResponse<List<Category>> getChildrenParallelCategory (Integer categoryId );

    ServerResponse<List<Integer>> selectCategoryAndChildrenById (Integer categoryId );
}
