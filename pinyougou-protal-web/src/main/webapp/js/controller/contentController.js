app.controller('contentController',function ($scope,contentService) {
    $scope.categoryId=[];
    //根据id查询分类广告
    $scope.findByCategoryId=function (id) {
        contentService.findByCategoryId(id).success(
            function (response) {
                $scope.categoryId[id]=response;
            }
        )
    }
    //跳转页面,传递关键字
    $scope.search=function () {
        location.href='http://localhost:9104/search.html#?keywords='+$scope.keywords;
    }
});