//创建控制器
app.controller("brandController",function ($scope,brandService,$controller) {
    $controller('baseController',{$scope:$scope});
    //声明一个查询品牌列表的方法
    $scope.findAll=function () {
        brandService.findAll().success(function (response) {
            $scope.list = response;
        });
    };

    $scope.findPage=function (page,size) {
        brandService.findPage(page,size).success(
            function (response) {
                $scope.list = response.rows;//当前页数据
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        )
    };
    //新增品牌方法,更改品牌方法
    $scope.save=function () {
        //定义方法名
        var object = null;
        if($scope.entity.id!=null){
            object = brandService.update($scope.entity);
        }else {
            object = brandService.add($scope.entity);
        }
        object.success(
            function (response) {
                if(response.success){
                    $scope.reloadList();
                }else {
                    alert(response.message)
                }
            }
        )
    };
    //查询一个方法
    $scope.findOne=function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        )
    };

    //选中删除方法
    $scope.dele=function () {
        if(confirm("确定要删除吗")){
            brandService.dele($scope.selectIds).success(
                function (response) {
                    if(response.success){
                        $scope.reloadList();
                    }else {
                        alert(response.message);
                    }
                }
            );
        }
    };
    $scope.searchEntity={};
    //根据条件查询方法
    $scope.search=function (page,size) {
        brandService.search(page,size,$scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;//当前页数据
                $scope.paginationConf.totalItems = response.total;//查询到的总记录数
            }
        )
    }

});