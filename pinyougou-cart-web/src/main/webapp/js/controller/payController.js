app.controller('payController',function ($scope,$location, payService) {

    //生成二维码
        $scope.createNative=function () {
            payService.createNative().success(
                function (response) {
                    $scope.money=(response.total_fee/100).toFixed(2);//金额
                    $scope.out_trade_no= response.out_trade_no;//订单号
                    //二维码
                    var qr = new QRious(
                        {
                            element:document.getElementById('qrious'),
                            size:250,
                            value:response.code_url,
                            level:'H'
                        }

                    );
                    queryPayStatus();//调用查询
                }
            )
        }
        //查询支付结果
        queryPayStatus=function () {
            payService.queryPayStatus($scope.out_trade_no).success(
                function (response) {
                    if (response.success){
                        location.href="paysuccess.html#?money="+$scope.money;
                    }else{
                        if (response.message=="支付超时!"){//重新打开网页
                            $scope.createNative();//调用方法重新生成二维码
                        }else{
                            location.href="payfail.html";
                        }
                    }
                }
            )
        }
        //获取金额

        $scope.getMoney=function () {
              return $location.search()['money'];
        }
})