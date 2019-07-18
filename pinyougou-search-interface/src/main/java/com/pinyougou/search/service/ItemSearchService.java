package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /**
     * 搜索的方法
     * @param searchMap
     * @return
     */
    public Map search(Map searchMap);
    /**
     * 导入数据到solr索引库的方法
     */
    public void importList(List list);
    /**
     * 根据Id删除solr索引库的方法
     */
    public void deleteByGoodsIds(List goodsIds);
}
