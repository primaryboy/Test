
package com.mall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mall.common.ServerResponse;
import com.mall.dao.CategoryMapper;
import com.mall.pojo.Category;
import com.mall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    private Logger logger = LoggerFactory.getLogger (CategoryServiceImpl.class);
    //添加分类
    public ServerResponse addCategory (String categoryName, Integer parentId ) {
        //如果没有父类或者分类名为空
        if (parentId == null || StringUtils.isBlank (categoryName)) {
            return ServerResponse.createByErrorMessage ("品类参数错误，创建失败");
        }
        //这个时候就新建一个分类
        Category category = new Category ();
        category.setName (categoryName);
        category.setParentId (parentId);
        //表示该分类可用
        category.setStatus (true);
        //新建的分类插入到分类表中
        int resultCount = categoryMapper.insert (category);
        //判断是否添加成功
        if (resultCount > 0) {
            return ServerResponse.createBySuccess ("添加类别成功！");
        }
        return ServerResponse.createByErrorMessage ("添加类别失败，请重新添加！");
    }
    //更新商品类别名
    public ServerResponse updateCategoryName ( Integer categoryId, String categoryName ) {
        if (categoryId == null || StringUtils.isBlank (categoryName)) {
            return ServerResponse.createByErrorMessage ("商品类别相关参数错误，更新失败");
        }
        //对商品类别名进行更新
        Category category = new Category ();
        category.setId (categoryId);
        category.setName (categoryName);
        int resultCount = categoryMapper.updateByPrimaryKeySelective (category);
        //判断是否更新成功
        if (resultCount > 0) {
            return ServerResponse.createBySuccess ("更新商品类别名成功！");
        }
        return ServerResponse.createByErrorMessage ("更新商品类别名失败！");
    }
    //通过节点id获取子节点，平级
    public ServerResponse<List<Category>> getChildrenParallelCategory (Integer categoryId ) {
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId (categoryId);
        //null||empty
        if (CollectionUtils.isEmpty (categoryList)) {
            //即使是一个空的节点也不需要放回一个信息，在日志里面展现即可
            logger.info ("未找到当前分类的子类");
        }
        return ServerResponse.createBySuccess (categoryList);
    }


    public ServerResponse<List<Integer>> selectCategoryAndChildrenById (Integer categoryId ) {
        //初始化
        Set<Category> categorySet = Sets.newHashSet ();//使用guawa的set进行初始化
        //填充categorySet
        findChildCategory (categorySet, categoryId);
        List<Integer> categoryIdList = Lists.newArrayList ();
        //将子节点id填充到categoryIdList
        if (categoryId != null) {
            for (Category categoryItem : categorySet) {
                categoryIdList.add (categoryItem.getId ());
            }
        }
        return ServerResponse.createBySuccess (categoryIdList);
    }


    //递归（自己调自己）,查出子节点
    private Set<Category> findChildCategory ( Set<Category> categorySet, Integer categoryId ) {
        Category category = categoryMapper.selectByPrimaryKey (categoryId);
        if (category != null) {
            categorySet.add (category);
        }
        //查找子节点,递归算法(Mybatis中不会返回null）
        //退出的条件：查找出为null
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId (categoryId);//使用集合收取，使用的是mybatis返回集合：如果没有查到，不会返回一个null，所以不进行判断
        for (Category categoryItem : categoryList) {
            findChildCategory (categorySet, categoryItem.getId ());
        }
        return categorySet;
    }
}
