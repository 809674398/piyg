package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojoGroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;


import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private TbPayLogMapper payLogMapper;


	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//从redis中取出购物车列表
	List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		//创建订单Id集合
	List<String> orderIdList=new ArrayList<>();
		//定义订单总金额
		double too_money=0;
		//循环购物车列表添加订单
	for (Cart cart :cartList){
		System.out.println("====================商家id为==============="+cart.getSellerId());
		//添加订单主表数据
		TbOrder tbOrder = new TbOrder();
		long orderId = idWorker.nextId();
		System.out.println("==============订单标号为:================"+orderId);
		tbOrder.setOrderId(orderId);
		tbOrder.setPaymentType(order.getPaymentType());//支付类型
		tbOrder.setStatus("1");//未付款
		tbOrder.setCreateTime( new Date());//下单时间
		tbOrder.setUpdateTime(new Date());//更新时间
		tbOrder.setUserId(order.getUserId());//用户名
		tbOrder.setReceiver(order.getReceiver());//收货人
		tbOrder.setReceiverMobile(order.getReceiverMobile());//收货人电话
		tbOrder.setReceiverAreaName(order.getReceiverAreaName());//收货人地址
		tbOrder.setSourceType(order.getSourceType());//订单来源
		tbOrder.setSellerId(cart.getSellerId());//商家id

		double money = 0;
		//添加订单从表明细
		for (TbOrderItem orderItem:cart.getOrderItemList()){
			orderItem.setId(idWorker.nextId());//主键
			orderItem.setOrderId(orderId);//订单编号
			orderItem.setSellerId(cart.getSellerId());//商家Id
			orderItemMapper.insert(orderItem);
			BigDecimal totalFee = orderItem.getTotalFee();
			money+=totalFee.doubleValue();
		}
		tbOrder.setPayment(new BigDecimal(money));
		orderMapper.insert(tbOrder);
		orderIdList.add(orderId+"");
		too_money+=money;
	}
	//添加支付日志,条件是支付类型是微信支付
		if ("1".equals(order.getPaymentType())){
			TbPayLog payLog = new TbPayLog();
			payLog.setCreateTime(new Date());//创建时间
			payLog.setUserId(order.getUserId());//用户id
			payLog.setOutTradeNo(idWorker.nextId()+"");//支付订单号
			payLog.setOrderList(orderIdList.toString().replace("[","").replace("]",""));//订单id列表
			payLog.setTotalFee((long)(too_money*100));//总金额
			payLog.setTradeState("0");//支付状态
			payLog.setPayType("1");//微信支付
			payLogMapper.insert(payLog);
			//放入缓存
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
		}



	//清除redis中的购物车
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public TbPayLog findPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//1.修改支付日志状态及相关字段
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());//支付时间
		payLog.setTradeState("1");//支付状态已支付
		payLog.setTransactionId(transaction_id);//微信交易流水号
		payLogMapper.updateByPrimaryKey(payLog);
		//2.修改订单表的状态
		String orderList = payLog.getOrderList();
		String[] orderIds = orderList.split(",");
		for (String orderId:orderIds){
			TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
			tbOrder.setStatus("2");
			tbOrder.setPaymentTime(new Date());
			orderMapper.updateByPrimaryKey(tbOrder);
		}
		//3.清空支付日志缓存
		redisTemplate.boundHashOps("payLog").get(payLog.getUserId());
	}

}
