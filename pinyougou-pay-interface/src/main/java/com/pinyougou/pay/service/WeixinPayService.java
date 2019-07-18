package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {

    /**
     * 生成微信支付二维码的方法
     */
    public Map createNative(String out_trade_no,String total_fee);
    /**
     * 查询订单支付状态
     */
    public Map queryNativeStatus(String out_trade_no);
    /**
     * 关闭订单
     */
    public Map closePay(String out_trade_no);
}
