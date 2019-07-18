 //控制层 
app.controller('userController' ,function($scope,userService){
	//注册方法
	$scope.reg=function () {
		if ($scope.password != $scope.entity.password){
            $scope.entity.password="";
            $scope.password="";
			alert("两次输入的密码不一致,请重试");
			return;
		}
		userService.add($scope.entity,$scope.code).success(
			function (response) {
				alert(response.message)
            }
		)
    }
    //发送验证码
	$scope.sendCode=function () {
		if ($scope.entity.phone==null || $scope.entity.phone==''){
			alert("请输入手机号");
			return;
		}
		userService.sendCode($scope.entity.phone).success(
			function (response) {
				alert(response.message)
            }
		)
    }

    
});	
