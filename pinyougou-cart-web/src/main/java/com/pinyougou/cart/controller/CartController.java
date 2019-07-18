package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojoGroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletResponse response;

    /**
     * 查询购物车列表
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录的用户为:"+name);
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        //未登录,从cookie中查
        System.out.println("从cookie中查");
        if (cartListStr == null || cartListStr.equals("")){
            cartListStr = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListStr, Cart.class);
        if (name.equals("anonymousUser")){

            return cartList_cookie;

        }else {
            //已登录,从redis中查
            System.out.println("从redis中查");
            List<Cart> cartList_redis = cartService.findCartListFromRedis(name);
            if (cartList_cookie.size()>0){//如果本地购物车中有数据
                //合并redis和cookie的购物车列表
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
                //将合并后的购物车存入redis
                cartService.saveCartListToRedis(name,cartList_redis);
                //清空本地购物车
                CookieUtil.deleteCookie(request,response,"cartList");
            }

            return cartList_redis;
        }
    }

    /**
     * 添加商品到购物车列表并存入cookie
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId,Integer num){

        try {
            List<Cart> cartList = findCartList();
            cartList =  cartService.addGoodsToCartList(cartList,itemId,num);
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("当前登录的用户为:"+name);
            if (name.equals("anonymousUser")){
            //未登录,向cookie中添加购物车
                String cartListStr = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request,response,"cartList",cartListStr,3600*24,"UTF-8");
            }else {
                //已登录,向redis中添加
                cartService.saveCartListToRedis(name,cartList);
            }

            return new Result(true,"添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加购物车失败");
        }

    }
}
