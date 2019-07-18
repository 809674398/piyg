package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.search.service.ItemSearchService;
import com.pinyougou.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    /**
     * 根据条件查询的方法
     * @param searchMap
     * @return
     */
    @Override
    public Map search(Map searchMap) {
        Map map=new HashMap();

        //1.查询列表
        map.putAll(searchList(searchMap));
        //2.分组查询 商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);

        //3.查询品牌和规格列表
        String category= (String) searchMap.get("category");
        if(!category.equals("")){
            map.putAll(searchBrandAndSpecList(category));
        }else{
            if(categoryList.size()>0){
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }

        return map;
    }

    /**
     * 导入数据到solr索引库的方法
     * @param list
     */
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }
    /**
     * 根据Id删除solr索引库的方法
     */
    @Override
    public void deleteByGoodsIds(List goodsIds) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


    /**
     * 查询列表
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap){
        Map map = new HashMap();
        //过滤关键字中的空格
        String keywords = (String) searchMap.get("keywords");
       searchMap.put("keywords",keywords.replace(" ",""));
        //高亮显示
        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//设置高亮域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix("</em>");//高亮后缀
        query.setHighlightOptions(highlightOptions);//设置高亮选项
        //1.1根据条件查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //1.2 按商品分类过滤
        if(!"".equals(searchMap.get("category"))  )	{//如果用户选择了分类
            FilterQuery filterQuery=new SimpleFilterQuery();
            Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.3 按品牌过滤
        if(!"".equals(searchMap.get("brand"))  )	{//如果用户选择了品牌
            FilterQuery filterQuery=new SimpleFilterQuery();
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.4 按规格过滤
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap= (Map<String, String>) searchMap.get("spec");
            for(String key :specMap.keySet()){

                FilterQuery filterQuery=new SimpleFilterQuery();
                Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key)  );
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);

            }
        }
        //1.5按价格过滤
        if (!"".equals(searchMap.get("price"))){
            String priceStr = (String) searchMap.get("price");
            String[] price = priceStr.split("-");
            if (!price[0].equals("0")){
                FilterQuery filterQuery=new SimpleFilterQuery();
                Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")){
                FilterQuery filterQuery=new SimpleFilterQuery();
                Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //1.6按页码和每页条数过滤
        Integer pageNum = (Integer) searchMap.get("pageNum");//当前页数
        if (pageNum==null){
            pageNum = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");//每页条数
        if (pageSize==null){
            pageSize=20;
        }
        query.setOffset((pageNum-1)*pageSize);//起始索引
        query.setRows(pageSize);//每页显示条数
        //1.7排序查询
        String sortField = (String) searchMap.get("sortField");//排序字段
        String sortValue = (String) searchMap.get("sort");//排序方式
        if (sortValue!=null && !sortValue.equals("")){
            if (sortValue.equals("ASC")){//升序排序
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")){//倒序排序
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }



        //*********************高亮页记录*************************
        HighlightPage<TbItem> pages = solrTemplate.queryForHighlightPage(query, TbItem.class);
        List<HighlightEntry<TbItem>> h = pages.getHighlighted();
        //循环遍历高亮入口集合
        for (HighlightEntry<TbItem> highlightEntry : h){
            TbItem entity = highlightEntry.getEntity();//获取原实体类
            if (highlightEntry.getHighlights().size()>0 && highlightEntry.getHighlights().get(0).getSnipplets().size()>0){
                //设置高亮的结果偶
                entity.setTitle(highlightEntry.getHighlights().get(0).getSnipplets().get(0));
            }
        }

        map.put("rows",pages.getContent());
        map.put("totalPages",pages.getTotalPages());//需要传给前端总页数
        map.put("total",pages.getTotalElements());//传给前端总记录数
        return map;
    }
        /**
         * 分组查询商品分类列表
         */
        private List<String> searchCategoryList(Map searchMap){
                List<String> list = new ArrayList();
            Query query =  new SimpleQuery("*:*");
            //根据关键字查询
            Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));//where
            query.addCriteria(criteria);
            //设置分组选项
            GroupOptions groupOptions = new GroupOptions();
            groupOptions.addGroupByField("item_category");//group by
            query.setGroupOptions(groupOptions);
            //获取分组页
            GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
            //获取分组结果
            GroupResult<TbItem> result = groupPage.getGroupResult("item_category");
            //获取分组入口页
            Page<GroupEntry<TbItem>> entries = result.getGroupEntries();
            //获取分组入口集合
            List<GroupEntry<TbItem>> entryList = entries.getContent();
            for (GroupEntry<TbItem> entry : entryList) {
                list.add(entry.getGroupValue());
            }
            return list;
        }
        @Autowired
        private RedisTemplate redisTemplate;
    /**
     * 查询品牌列表和规格
     */
        private Map searchBrandAndSpecList(String categoryId){
            Map map = new HashMap();
            //先根据分类名称查询分类id
            Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(categoryId);
          if (typeId!= null){
              //根据分类Id查询品牌
              List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
              map.put("brandList",brandList);
              //根据分类id查询规格
              List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
              map.put("specList",specList);
              map.put("specList",specList);
          }
            return  map;
        }
}
