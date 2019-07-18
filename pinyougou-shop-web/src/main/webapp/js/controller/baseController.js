//声明controller
app.controller("baseController",function ($scope) {
//分页控件配置
$scope.paginationConf = {
    currentPage: 1,
    totalItems: 10,
    itemsPerPage: 10,
    perPageOptions: [10, 20, 30, 40, 50],
    onChange: function(){
        $scope.reloadList();
    }
};
//抽取的刷新页面方法
$scope.reloadList=function () {
    $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage)
};

//定义一个变量数组,用来接收复选框选中的id数组
$scope.selectIds=[];
//定义接收id的方法
$scope.updateSelected=function (id,$event) {
    if($event.target.checked){
        $scope.selectIds.push(id);
    }else {
        var index= $scope.selectIds.indexOf(id);
        $scope.selectIds.splice(index,1);
    }
};
//定义优化数据格式的方法
    $scope.jsonToString=function (jsonString,key) {
         var json= JSON.parse(jsonString);
         var value = "";
         for(var i = 0;i<json.length;i++){
             if(i>0){
                 value+=",";
             }
             value+=json[i][key];
         }
         return value;
    }

    //在list集合中根据某key的值查询对象
    $scope.searchObjectByKey=function(list,key,keyValue){

        for(var i=0;i<list.length;i++){
            if(list[i][key]==keyValue){
                return list[i];
            }
        }
        return null;
    }


});