package com.pinyougou.page.service;

public interface ItemPageService {

    /**
     * 生成商品详情页
     */
    public Boolean genItemHtml(Long goodsId);

    /**
     * 删除商品详情页
     */
    public Boolean deleteHtml(Long [] ids);
}
