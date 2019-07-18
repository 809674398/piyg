package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private SeckillOrderService orderService;

    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        //获取当前userId
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //先从缓存中取出数据
        TbSeckillOrder order = orderService.searchSeckillOrderFromRedis(userId);
        if (order!=null){
            return weixinPayService.createNative(order.getId()+"",(long)(order.getMoney().doubleValue()*100)+"");
        }else {
            return new HashMap();
        }

    }

    /**
     * 查询支付结果
     */

    @RequestMapping("/queryNativeStatus")
    public Result queryNativeStatus(String out_trade_no){
        //获取当前userId
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result = null;
        int x = 0;
        while (true){
            Map<String,String> map = weixinPayService.queryNativeStatus(out_trade_no);
            if (map==null){
                result=  new Result(false,"支付错误,请重新支付");
                break;
            }
            if (map.get("trade_state").equals("SUCCESS")){
                //支付成功
                result=  new Result(true,"支付成功!");
               //调用方法保存订单
                orderService.saveOrderFromRedisToDB(userId,Long.valueOf(out_trade_no),map.get("transaction_id"));
                break;
            }
            try {//3秒调用一次
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x >= 100){
                result=  new Result(false,"支付超时!");
                Map resultMap = weixinPayService.closePay(out_trade_no);
                if (resultMap!= null && "FAIL".equals(resultMap.get("return_code"))){
                //关闭成功
                    if ("ORDERPAID".equals(resultMap.get("err_code"))){
                        //用户已支付
                        result=  new Result(true,"支付成功!");
                        //调用方法保存订单
                        orderService.saveOrderFromRedisToDB(userId,Long.valueOf(out_trade_no),map.get("transaction_id"));
                    }
                }
                if (result.getSuccess()==false){
                    orderService.deleteOrderFromRedis(userId,Long.valueOf(out_trade_no));
                }
                break;
            }

        }
            return result;
    }
}
