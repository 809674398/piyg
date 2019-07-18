package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojoGroup.Cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据skuId查询商品明细sku对象
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        //1.1判断该sku是否合法
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("商品状态不合法");
        }
        //2.根据sku对象查询商家id
        String sellerId = item.getSellerId();
        //3.根据商家id查询购物车列表
        Cart cart = searchCartListBySellerId(cartList, sellerId);
        //4.如果不存在该商品所在商家的购物车对象
        if (cart == null) {
            //4.1创建一个新的该商家的购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            List<TbOrderItem> orderItemList = new ArrayList<>();
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2将新的购物车对象添加到该商家的购物车列表中
            cartList.add(cart);
        } else {  //5.如果购物车对象中存在该商家的购物车
            //5.1判断该商家的购物车中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
            if(orderItem==null){
                //5.1  如果不存在  ，创建新的购物车明细对象，并添加到该购物车的明细列表中
                orderItem=createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);

            }else{
                //5.2 如果存在，在原有的数量上添加数量 ,并且更新金额
                orderItem.setNum(orderItem.getNum()+num);//更改数量
                //金额
                orderItem.setTotalFee(  new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum() )  );
                //当明细的数量小于等于0，移除此明细
                if(orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);
                }
                //当购物车的明细数量为0，在购物车列表中移除此购物车
                if(cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    /**
     * 根据商家id查询购物车列表中有没有该商家的购物车
     */
    private Cart searchCartListBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    /**
     *
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        //创建新的购物车明细对象
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        return orderItem;
    }

    /**
     * 查询商家的购物车明细里有没有传过来的商品
     */
    public TbOrderItem searchOrderItemByItemId(List<TbOrderItem> itemList, Long itemId) {
        for (TbOrderItem orderItem : itemList) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 从redis中查询购物车
     * @param name
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String name) {
        System.out.println("从redis中查询购物车数据:"+name);

        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(name);
        if (cartList==null){
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 向redis中添加购物车
     * @param name
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String name, List<Cart> cartList) {
        System.out.println("向redis中存入购物车数据:"+name);
        redisTemplate.boundHashOps("cartList").put(name,cartList);
    }

    /**
     * 合并cookie和redis的购物车列表
     * @param cartList1
     * @param cartList2
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for(Cart cart: cartList2){
            for(TbOrderItem orderItem:cart.getOrderItemList()){
                cartList1=
                        addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList1;
    }
}