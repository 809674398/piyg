app.controller("cartController",function ($scope, cartService) {

    //查询购物车列表
    $scope.findCartList=function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList=response;
               $scope.totalValue = cartService.sum($scope.cartList);
            }
        )
    }

    //增加减少购物车商品数量
    $scope.addGoodsToCartList=function (itemId, num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {
                if (response.success){
                    $scope.findCartList();//刷新列表
                }else{
                    alert(response.message)
                }
            }
        )
    }
    //查询地址列表
    $scope.findAddressByUserId=function () {
        cartService.findAddressByUserId().success(
            function (response) {
                $scope.addressList=response;
                for(var i = 0;i<$scope.addressList.length;i++){
                    if ($scope.addressList[i].isDefault==1){
                        $scope.address=$scope.addressList[i];
                        break;
                    }
                }
            }
        )
    }
    //选择地址
    $scope.selectAddress=function (address) {
        $scope.address=address;
    }
    //判断是否为选择的地址的方法
    $scope.selectedAddress=function (address) {
        if(address==$scope.address){
            return true;
        }else {
            return false;
        }
    }
    //定义订单对象
    $scope.order={paymentType:'1'};
    //选择支付方式
    $scope.selectPayment=function (type) {
        $scope.order.paymentType=type;
    }
    //提交订单
    $scope.submitOrder=function () {
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//电话
        $scope.order.receiver=$scope.address.contact;//联系人
        cartService.submitOrder($scope.order).success(
            function (response) {
                if(response.success){//如果提交订单成功
                    //页面跳转
                    if ($scope.order.paymentType=='1'){
                        location.href="pay.html";
                    }else {
                        location.href="paysuccess.html";
                    }
                }else {
                    alert(response.message)
                }
            }
        )
    }

});