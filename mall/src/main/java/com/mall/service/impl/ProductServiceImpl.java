package com.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.dao.CategoryMapper;
import com.mall.dao.ProductMapper;
import com.mall.pojo.Category;
import com.mall.pojo.Product;
import com.mall.service.ICategoryService;
import com.mall.service.IProductService;
import com.mall.util.DateTimeUtil;
import com.mall.util.PropertiesUtil;
import com.mall.vo.ProductDetailVo;
import com.mall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service("iProductService")
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    //更新（保存）商品
    public ServerResponse saveOrUpdateProduct ( Product product ) {
        if (product != null) {
            //对商品进行更新
            if (product.getId () != null) {
                int resultCount = productMapper.updateByPrimaryKey (product);
                if (resultCount > 0) {
                    return ServerResponse.createBySuccess ("更新商品成功");
                }
                return ServerResponse.createByErrorMessage ("更新商品失败");
            }
            //新增商品
            else {
                int resultCount = productMapper.insertSelective (product);
                if (resultCount > 0) {
                    return ServerResponse.createBySuccess ("新商品成功");
                }
                return ServerResponse.createByErrorMessage ("新增商品失败");
            }
        }
        return ServerResponse.createByErrorMessage ("新增/更新的的参数不正确");
    }

    //设置产品销售状态
    public ServerResponse<String> setSaleStatus ( Integer productId, Integer status ) {
        if (productId == null || status == null) {
            //表达参数错误
            return ServerResponse.createByErrorCodeMessage (ResponseCode.ILLEGAL_ARGUMENT.getCode (), ResponseCode.ILLEGAL_ARGUMENT.getDesc ());
        }
        Product product = new Product ();
        product.setId (productId);
        product.setStatus (status);
        //对商品状态进行更新（1-在售，2-下架，3-删除）
        int resultCount = productMapper.updateByPrimaryKeySelective (product);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess ("更新商品销售状态成功");
        }
        return ServerResponse.createByErrorMessage ("更新商品销售状态失败");
    }

    //管理商品
    public ServerResponse<ProductDetailVo> administerProductDetail (Integer productId ) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.ILLEGAL_ARGUMENT.getCode (), ResponseCode.ILLEGAL_ARGUMENT.getDesc ());
        }
        Product product = productMapper.selectByPrimaryKey (productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage ("产品已下架或者删除");
        }
        //返回VO对象（value object）承载对象各个值的对象
        ProductDetailVo productDetailVo = assembleProductDetailVo (product);
        return ServerResponse.createBySuccess (productDetailVo);
    }

    //组装ProductDetailVo
    private ProductDetailVo assembleProductDetailVo ( Product product ) {

        ProductDetailVo productDetailVo = new ProductDetailVo ();
        productDetailVo.setId (product.getId ());
        productDetailVo.setSubtitle (product.getSubtitle ());
        productDetailVo.setPrice (product.getPrice ());
        productDetailVo.setMainImage (product.getMainImage ());
        productDetailVo.setSubImages (product.getSubImages ());
        productDetailVo.setCategoryId (product.getCategoryId ());
        productDetailVo.setDetail (product.getDetail ());
        productDetailVo.setName (product.getName ());
        productDetailVo.setStatus (product.getStatus ());
        productDetailVo.setStock (product.getStock ());
        //imageHost同配置文件中获取，为了使配置和代码分离（热部署，建立配置中心，专门管理这些配置），这样就不需要url硬编码到项目中
        //如果图片修改了，只需要改property配置即可
        //去获取的时候就会把ftp.server.http.prefix的值直接填充到ImageHost里，如果我们忘记传则http://img.happymmall.com/保底，使得服务器能找到图片
        //productDetailVo.setImageHost (PropertiesUtil.getProperty ("ftp.server.http.prefix", "http://img.happymmall.com/"));

        //获取产品所在分类
        Category category = categoryMapper.selectByPrimaryKey (product.getCategoryId ());
        if (category == null) {
            //默认为根节点
            productDetailVo.setParentCategoryId (0);
        } else {
            productDetailVo.setParentCategoryId (category.getParentId ());
        }

        productDetailVo.setCreateTime (DateTimeUtil.dateToStr (product.getCreateTime ()));
        productDetailVo.setUpdateTime (DateTimeUtil.dateToStr (product.getUpdateTime ()));
        //返回
        return productDetailVo;
    }

    //获取商品列表（商品部分信息）
    public ServerResponse<PageInfo> getProductList (int pageNum, int pageSize ) {
        //设置startPage
        PageHelper.startPage (pageNum, pageSize);
        //填充sql查询逻辑
        List<Product> productList = productMapper.selectList ();
        List<ProductListVo> productListVoList = Lists.newArrayList ();
        for (Product productItem : productList) {
            //重组
            ProductListVo productListVo = assembleProductListVo (productItem);
            //存入list
            productListVoList.add (productListVo);
        }
        //pageHelper自动分页---收尾
        PageInfo pageResult = new PageInfo (productList);
//        productList->productListVoList
        //给前端展示不是整个product，所以进行重置
        pageResult.setList (productListVoList);
        return ServerResponse.createBySuccess (pageResult);
    }

    //组装ProductListVo
    private ProductListVo assembleProductListVo ( Product product ) {
        ProductListVo productListVo = new ProductListVo ();
        productListVo.setId (product.getId ());
        productListVo.setName (product.getName ());
        productListVo.setCategoryId (product.getCategoryId ());
        productListVo.setImageHost (PropertiesUtil.getProperty ("ftp.server.http.prefix"));
        productListVo.setMainImage (product.getMainImage ());
        productListVo.setPrice (product.getPrice ());
        productListVo.setSubtitle (product.getSubtitle ());
        productListVo.setStatus (product.getStatus ());
        return productListVo;
    }

    public ServerResponse<PageInfo> searchProduct ( String productName, Integer productId, int pageNum, int pageSize ) {
        //设置startPage
        PageHelper.startPage (pageNum, pageSize);
        if (StringUtils.isNotBlank (productName)) {
            //使用sql中的某个查询构建
            //添加通配符%
            productName = new StringBuilder ().append ("%").append (productName).append ("%").toString ();
        }
        //填充sql查询逻辑,productList是一个Page对象
        List<Product> productList = productMapper.selectByNameAndProductId (productName, productId);
        //把product转成productListVoList
        List<ProductListVo> productListVoList = Lists.newArrayList ();
        for (Product productItem : productList) {
            //重组
            ProductListVo productListVo = assembleProductListVo (productItem);
            //存入list
            productListVoList.add (productListVo);
        }
        //pageHelper自动分页---收尾
        PageInfo pageResult = new PageInfo (productList);
        //productList->productListVoList
        //给前端展示不是整个product，所以进行重置
        pageResult.setList (productListVoList);
        return ServerResponse.createBySuccess (pageResult);
    }

    //用户获取商品信息
    public ServerResponse<ProductDetailVo> getProductDetail ( Integer productId ) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.ILLEGAL_ARGUMENT.getCode (), ResponseCode.ILLEGAL_ARGUMENT.getDesc ());
        }
        Product product = productMapper.selectByPrimaryKey (productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage ("产品已下架或者删除");
        }
        if (product.getStatus () != Const.ProductStatusEnum.ON_SALE.getCode ()) {
            return ServerResponse.createByErrorMessage ("产品已下架或者删除");
        }
        //返回VO对象（value object）
        ProductDetailVo productDetailVo = assembleProductDetailVo (product);
        return ServerResponse.createBySuccess (productDetailVo);
    }
    //搜索商品列表
    public ServerResponse<PageInfo> getProductByKeywordCategory ( String keyword, Integer categoryId, int pageNum, int pageSize, String orderBy ) {
        //keword和categoryId都为空
        if (StringUtils.isBlank (keyword) && categoryId == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.ILLEGAL_ARGUMENT.getCode (), ResponseCode.ILLEGAL_ARGUMENT.getDesc ());
        }
        //通过类别获取
        //放置分类id
        List<Integer> categoryIdList = new ArrayList<Integer> ();
        //传入categoryId
        if (categoryId != null) {
            Category category = categoryMapper.selectByPrimaryKey (categoryId);
            //如果没有该分类，且keyword也没有（返回空结果集，不报错）
            if (category == null && StringUtils.isBlank (keyword)) {
                //分页
                PageHelper.startPage (pageNum, pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList ();
                PageInfo pageInfo = new PageInfo (productListVoList);
                //不需要再set这个集合
                return ServerResponse.createBySuccess (pageInfo);
            }
            //有该分类，递归获取所有子分类
            categoryIdList = iCategoryService.selectCategoryAndChildrenById (categoryId).getData ();
        }
        //搜索
        //传入keyword
        if (StringUtils.isNotBlank (keyword)) {
            //拼接字符串%keyword%
            keyword = new StringBuilder ().append ("%").append (keyword).append ("%").toString ();
        }
        //分页
        PageHelper.startPage (pageNum, pageSize);
        //排序
        if (StringUtils.isNotBlank (orderBy)) {
            //升序||降序
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains (orderBy)) {
                String[] oderByArray = orderBy.split ("_");
                //设置排序方式（按价格）
                PageHelper.orderBy (oderByArray[ 0 ] + " " + oderByArray[ 1 ]);
            }
        }
        //sql查询（keyword不能为" "，categoryIdList不能为empty
        List<Product> productList = productMapper.selectByNameAndCategoryIds (StringUtils.isBlank (keyword) ? null : keyword, categoryIdList.size () == 0 ? null : categoryIdList);
        List<ProductListVo> productListVoList = Lists.newArrayList ();
        //填充productListVoList
        for (Product product : productList) {
            ProductListVo productListVo = assembleProductListVo (product);
            productListVoList.add (productListVo);
        }
        PageInfo pageInfo = new PageInfo (productList);
        pageInfo.setList (productListVoList);
        return ServerResponse.createBySuccess (pageInfo);
    }

}
