package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;


import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbSeckillGoodsMapper goodsMapper;

	@Autowired
	private IdWorker idWorker;


	/**
	 * 秒杀下单
	 * @param goodsId
	 * @param userId
	 */
	@Override
	public void submitOrder(Long goodsId, String userId) {
		//1.根据goodsid查询商品
	TbSeckillGoods goods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(goodsId);
		if (goods.getStockCount()<=0){
		  throw new RuntimeException("商品被抢光了");
 		}
 		if (goods==null){
			throw new RuntimeException("商品不存在");
		}
		//2.减少商品库存
		goods.setStockCount(goods.getStockCount()-1);//减库存
		redisTemplate.boundHashOps("seckillGoods").put(goodsId,goods);//更新到缓存
		System.out.println("更新商品到缓存");
		if (goods.getStockCount()==0){//更新到数据库,删除缓存
			goodsMapper.updateByPrimaryKey(goods);
			redisTemplate.boundHashOps("seckillGoods").delete(goodsId);
		}
		//3.存储订单到缓存
		TbSeckillOrder order = new TbSeckillOrder();
		order.setId(idWorker.nextId());
		order.setSeckillId(goodsId);
		order.setMoney(goods.getCostPrice());
		order.setUserId(userId);
		order.setSellerId(goods.getSellerId());
		order.setCreateTime(new Date());
		order.setStatus("0");
		redisTemplate.boundHashOps("seckillOrder").put(userId,order);
		System.out.println("存储订单到缓存");

	}

    /**
     * 查询秒杀订单
     * @param userId
     * @return
     */
    @Override
    public TbSeckillOrder searchSeckillOrderFromRedis(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    /**
     * 保存订单到数据库
     * @param userId
     * @param orderId
     * @param transactionId
     */
    @Override
    public void saveOrderFromRedisToDB(String userId, Long orderId, String transactionId) {
        //1查询订单
        TbSeckillOrder order = searchSeckillOrderFromRedis(userId);
        if (!order.getId().equals(orderId)){
            throw new RuntimeException("订单出错");
        }
        if (order==null){
            throw new RuntimeException("订单不存在");
        }
        //2.给订单赋值
        order.setPayTime(new Date());//支付日期
        order.setTransactionId(transactionId);//微信流水号
        order.setStatus("1");//已支付
        //3.存入数据库
        seckillOrderMapper.insert(order);
        //4.删除缓存
        redisTemplate.boundHashOps("seckillOrder").delete(userId);

    }

	/**
	 * 从缓存中删除订单
	 * @param userId
	 * @param orderId
	 */
	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		//1.从缓存中查询订单
		TbSeckillOrder order = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (order != null){
			//2.删除redis中的订单
			redisTemplate.boundHashOps("seckillOrder").delete(userId);

			//3.库存回退
			TbSeckillGoods goods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(order.getSeckillId());
			if (goods!=null){
				goods.setStockCount(goods.getStockCount()+1);//库存
				redisTemplate.boundHashOps("seckillGoods").put(order.getSeckillId(),goods);//回退
			}else {
				goods = new TbSeckillGoods();
				goods.setId(order.getSeckillId());
				goods.setStockCount(1);
				//省略
				redisTemplate.boundHashOps("seckillGoods").put(order.getSeckillId(),goods);//回退
			}
			System.out.println("订单取消:"+orderId);
		}
	}

}
