package com.mall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.dao.CartMapper;
import com.mall.dao.ProductMapper;
import com.mall.pojo.Cart;
import com.mall.pojo.Product;
import com.mall.service.ICartService;
import com.mall.util.BigDecimalUtil;
import com.mall.util.PropertiesUtil;
import com.mall.vo.CartProductVo;
import com.mall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {
    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    private boolean getAllCheckedStatus ( Integer userId ) {
        if (userId == null) {
            return false;
        }
        return cartMapper.SelectCartProductCheckedStatusByUserId (userId) == 0;
    }

    //获取购物车中的数据，同时组装好购物车数据例如图片的http地址等信息,判断购物车里的数量是否需要限制，并把限制数量是否成功返回给前端
    private CartVo getCartVoLimit ( Integer userId ) {
        CartVo cartVo = new CartVo ();
        //获取用户的购物车清单
        List<Cart> cartList = cartMapper.selectCartByUserId (userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList ();
        //总价
        BigDecimal cartTotalPrice = new BigDecimal ("0");
        //如果有产品
        if (CollectionUtils.isNotEmpty (cartList)) {
            for (Cart cartItem : cartList) {
                CartProductVo cartProductVo = new CartProductVo ();
                //组装cartProductVo
                cartProductVo.setId (cartItem.getId ());
                cartProductVo.setUserId (cartItem.getUserId ());
                cartProductVo.setProductId (cartItem.getProductId ());
                //获取产品
                Product product = productMapper.selectByPrimaryKey (cartItem.getProductId ());
                if (product != null) {
                    //继续组装cartProductVo
                    cartProductVo.setProductName (product.getName ());
                    cartProductVo.setProductMainImage (product.getMainImage ());
                    cartProductVo.setProductPrice (product.getPrice ());
                    cartProductVo.setProductStatus (product.getStatus ());
                    cartProductVo.setProductStock (product.getStock ());
                    cartProductVo.setProductSubtitle (product.getSubtitle ());
                    //判断库存
                    //初始化购买限制数量
                    int buyLimitCount = 0;
                    //库存充足，不用限制
                    if (product.getStock () >= cartItem.getQuantity ()) {
                        buyLimitCount = cartItem.getQuantity ();//赋值为要买的数量
                        cartProductVo.setLimitQuantity (Const.Cart.LIMIT_NUM_SUCCESS);
                    }
                    //库存不足
                    else {
                        buyLimitCount = product.getStock ();
                        cartProductVo.setLimitQuantity (Const.Cart.LIMIT_NUM_FAIL);
                        //更新该购物车中的有效库存
                        Cart newCart = new Cart ();
                        newCart.setId (cartItem.getId ());
                        newCart.setQuantity (buyLimitCount);
                        //只更新quantity字段
                        cartMapper.updateByPrimaryKeySelective (newCart);
                    }
                    //继续组装cartProductVo
                    cartProductVo.setQuantity (buyLimitCount);
                    //计算该商品总价
                    cartProductVo.setProductTotalPrice (BigDecimalUtil.mul (product.getPrice ().doubleValue (), cartProductVo.getQuantity ()));
                    cartProductVo.setProductChecked (cartItem.getChecked ());

                }
                //如果该商品为选中状态，计入商品总价cartTotalPrice
                if (cartItem.getChecked () == Const.Cart.CHECKED) {
                    cartTotalPrice = BigDecimalUtil.add (cartTotalPrice.doubleValue (), cartProductVo.getProductTotalPrice ().doubleValue ());
                }
                //将组装好的cartProductVo存入cartProductVoList
                cartProductVoList.add (cartProductVo);
            }

        }
        cartVo.setCartTotalPrice (cartTotalPrice);
        cartVo.setCartProductVoList (cartProductVoList);
        cartVo.setAllChecked (this.getAllCheckedStatus (userId));
        cartVo.setImageHost (PropertiesUtil.getProperty ("ftp.server.http.prefix"));
        return cartVo;
    }

    public ServerResponse<CartVo> list ( Integer userId ) {
        CartVo cartVo = this.getCartVoLimit (userId);
        return ServerResponse.createBySuccess (cartVo);

    }

    public ServerResponse<CartVo> add (Integer userId, Integer productId, Integer count ) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.ILLEGAL_ARGUMENT.getCode (), ResponseCode.ILLEGAL_ARGUMENT.getDesc ());
        }
        //获取该用户的该产品购物车
        Cart cart = cartMapper.selectCartByUserIdProductId (userId, productId);
        //产品不在该用户购物车中
        if (cart == null) {
            //添加该产品
            Cart cartItem = new Cart ();
            cartItem.setChecked (Const.Cart.CHECKED);
            cartItem.setProductId (productId);
            cartItem.setUserId (userId);
            cartItem.setQuantity (count);
            cartMapper.insert (cartItem);
        } else {
            //产品已经在该用户购物车中
            count += cart.getQuantity ();
            //更新该产品数量
            cart.setQuantity (count);
            cartMapper.updateByPrimaryKeySelective (cart);
        }
        return this.list (userId);

    }

    public ServerResponse<CartVo> update ( Integer userId, Integer productId, Integer count ) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.ILLEGAL_ARGUMENT.getCode (), ResponseCode.ILLEGAL_ARGUMENT.getDesc ());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId (userId, productId);
        if (cart != null) {
            cart.setQuantity (count);
        }
        cartMapper.updateByPrimaryKeySelective (cart);
        return this.list (userId);
    }

    public ServerResponse<CartVo> deleteProduct ( Integer userId, String productIds ) {
        //分割productIds->("12","17")
        List<String> productList = Splitter.on (",").splitToList (productIds);
        if (CollectionUtils.isEmpty (productList)) {
            return ServerResponse.createByErrorCodeMessage (ResponseCode.ILLEGAL_ARGUMENT.getCode (), ResponseCode.ILLEGAL_ARGUMENT.getDesc ());
        }
        cartMapper.deleteByUserIdProductIds (userId, productList);
        return this.list (userId);

    }

    public ServerResponse<CartVo> selecteOrUnSelect ( Integer userId, Integer checked, Integer productId ) {
        cartMapper.checkedOrUnCheckedProduct (userId, checked, productId);
        return this.list (userId);
    }

    public ServerResponse<Integer> getCartProductCount ( Integer userId ) {
        if (userId == null) {
            return ServerResponse.createBySuccess (0);
        }
        return ServerResponse.createBySuccess (cartMapper.selectCartProductCount (userId));
    }

}
