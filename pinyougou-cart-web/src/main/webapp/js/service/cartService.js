app.service("cartService",function ($http) {

    //查询购物车列表
    this.findCartList=function () {
        return $http.get("cart/findCartList.do")
    }

    //增加减少购物车商品数量
    this.addGoodsToCartList=function (itemId, num) {
        return $http.get("cart/addGoodsToCartList.do?itemId="+itemId+"&num="+num);
    }

    //求合计数
    this.sum=function (cartList) {
        var totalValue={totalNum:0,totalPrice:0};
        for(var i = 0;i <cartList.length;i++){
            var orderItemList = cartList[i].orderItemList;
            for (var j = 0; j<orderItemList.length;j++){
                var orderItem =  orderItemList[j];
                totalValue.totalNum += orderItem.num;
                totalValue.totalPrice += orderItem.totalFee;
            }
        }
        return totalValue;
    }
    //查询地址列表
    this.findAddressByUserId=function () {
        return $http.get("address/findAddressByUserId.do")
    }
    //提交订单列表
    this.submitOrder=function (order) {
        return $http.post('order/add.do',order)
    }
})