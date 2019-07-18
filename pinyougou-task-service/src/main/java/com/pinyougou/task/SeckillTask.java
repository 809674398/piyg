package com.pinyougou.task;

import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void refreshSeckillGoods(){
        System.out.println("执行了增量秒杀商品更新任务调度:"+new Date());
        List goodsIdList =new ArrayList( redisTemplate.boundHashOps("seckillGoods").keys());
        System.out.println(goodsIdList);
        //从数据库中查询
        TbSeckillGoodsExample example=new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//状态在售
        criteria.andStockCountGreaterThan(0);//库存数大于0
        criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间
        criteria.andEndTimeGreaterThanOrEqualTo(new Date());//结束时间
        if (goodsIdList.size()>0){//缓存中存在商品
            criteria.andIdNotIn(goodsIdList);//排除缓存中的已经存在的id集合
        }
      List<TbSeckillGoods>  seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        //把数据遍历放入缓存
        for (TbSeckillGoods seckillGoods:seckillGoodsList){
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(),seckillGoods);
            System.out.println("增量更新的商品id为:"+seckillGoods.getId());

        }
        System.out.println("---------------zzzzzzzzzzzzz----------------");

    }
    @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods(){
        //查询出缓存数据库中的数据,比较时间
        List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();
        System.out.println("执行了清除秒杀商品的任务:"+new Date());
        //和当前的时间进行比较
        for (TbSeckillGoods seckillGood:seckillGoods){
            if (seckillGood.getEndTime().getTime()<new Date().getTime()){
                //同步到数据库
                seckillGoodsMapper.updateByPrimaryKey(seckillGood);
                //清除缓存中过期的秒杀商品
                redisTemplate.boundHashOps("seckillGoods").delete(seckillGood.getId());
                System.out.println("秒杀商品:"+seckillGood.getId()+"已过期");
            }
        }
        System.out.println("执行了清除秒杀商品的任务...end");
    }

}
