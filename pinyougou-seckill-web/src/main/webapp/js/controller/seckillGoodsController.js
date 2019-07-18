app.controller('seckillGoodsController' ,function($scope,$location,seckillGoodsService,$interval){
//读取列表数据绑定到表单中
    $scope.findList=function(){
        seckillGoodsService.findList().success(
            function(response){
                $scope.list=response;
            }
        );
    }
    //查询一个秒杀商品
    $scope.findOne=function () {
       var id = $location.search()['id'];
       seckillGoodsService.findOne(id).success(
           function (response) {
               $scope.seckillGood=response;

               //计算商品秒杀剩余秒数
               allsecond=Math.floor((new Date($scope.seckillGood.endTime).getTime()-new Date().getTime())/1000);
               time=$interval(function () {
                   allsecond=allsecond-1;
                   $scope.timeStr = convertTimeStr(allsecond);
                   if (allsecond<=0){
                       $interval.cancel(time);
                   }
               },1000)
           }
       )
    }


    //转换秒为日期类型
    convertTimeStr=function (allsecond) {
        var days=Math.floor(allsecond/(60*60*24)) //天数
        var hours= Math.floor((allsecond-days*60*60*24)/(60*60)) //小时数
        var minutes= Math.floor( (allsecond -days*60*60*24 - hours*60*60)/60 );//分钟数
        var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
        var timeStr="";
        if (days>0){
            timeStr=days+"天";
        }
        return timeStr+hours+"小时"+minutes+"分钟"+seconds+"秒";
    }

    $scope.submitOrder=function () {
        seckillGoodsService.submitOrder($scope.seckillGood.id).success(
            function (response) {
                if (response.success){
                    alert("抢购成功,请在5分钟之内支付订单")
                    location.href="pay.html"
                }else {
                    alert(response.message)
                }
            }
        )
    }


});