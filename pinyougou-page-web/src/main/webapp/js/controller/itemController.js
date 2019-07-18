app.controller("itemController",function ($scope,$http) {
	$scope.num=1;
	$scope.addNum=function(x){
	$scope.num+=x;
	if($scope.num<1){
	$scope.num=1;
	}
}
	$scope.specificationItems={};//记录用户选择的规格
	//用户选择规格
	$scope.selectSpecification=function(key,value){
		$scope.specificationItems[key]=value;	
			searchSku();
	}
	//判断规格是否被用户选择的方法
	$scope.isSelected=function(key,value){
		if($scope.specificationItems[key]==value){
			return true;
		}else{
			return false;
		}		
	}
	//当前选择的sku
	$scope.sku={};
	//加载默认sku的方法
	$scope.loadSku=function(){
		$scope.sku=skuList[0];
		$scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));		
	}
	//判断两个map集合是否相等的方法
	marchObject=function(map1,map2){
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}			
		}
		for(var k in map2){
			if(map2[k]!= map1[k]){
				return false;
			}
		}		
		return true;
	}	
	//查询当前用户选择的sku
	searchSku=function(){
		for(var i = 0; i<skuList.length;i++){
			if(marchObject(skuList[i].spec,$scope.specificationItems)){
				$scope.sku=skuList[i];
				return;
			}
		}
		$scope.sku={id:0,title:'--------',price:0};//执行到这里说明没有匹配的
	}
	
	//添加商品到购物车 需要获取skuid
	$scope.addToCart=function(){
		$http.get("http://localhost:9107/cart/addGoodsToCartList.do?itemId="+
			$scope.sku.id+"&num="+$scope.num,{'withCredentials':true}).success(
				function (response) {
					if (response.message){
						location.href="http://localhost:9107/cart.html";
					}else {
						alert(response.message)
					}
                }
		)
	}
})




