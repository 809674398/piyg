app.controller("indexController",function ($scope,loginService) {
    //展示用户名
    $scope.showName=function () {
        loginService.showName().success(
            function (response) {
                $scope.loginName=response.loginName;
            }
        )
    }
})