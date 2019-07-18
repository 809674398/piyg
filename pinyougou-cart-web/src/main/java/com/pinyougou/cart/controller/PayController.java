package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private OrderService orderService;

    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        //获取当前userId
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //先从缓存中取出数据
        TbPayLog payLog = orderService.findPayLogFromRedis(userId);
        if (payLog!=null){
            return weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else {
            return new HashMap();
        }

    }

    /**
     * 查询支付结果
     */

    @RequestMapping("/queryNativeStatus")
    public Result queryNativeStatus(String out_trade_no){
        Result result = null;
        while (true){
            int x = 0;
            Map<String,String> map = weixinPayService.queryNativeStatus(out_trade_no);
            if (map==null){
                result=  new Result(false,"支付错误,请重新支付");
                break;
            }
            if (map.get("trade_state").equals("SUCCESS")){
                //支付成功
                result=  new Result(true,"支付成功!");
                orderService.updateOrderStatus(out_trade_no,map.get("transaction_id"));
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
                break;
            }

        }
            return result;
    }
}
