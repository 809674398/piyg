package com.pinyougou.cart.service;

import com.pinyougou.pojoGroup.Cart;

import java.util.List;

public interface CartService {
    /**
     * 添加商品到购物车列表
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);

    /**
     * 从redis中查询购物车列表
     */
    public List<Cart> findCartListFromRedis(String name);

    /**
     *向redis中添加购物车列表
     */
    public void saveCartListToRedis(String name,List<Cart> cartList);

    /**
     * 合并购物车列表
     */
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
