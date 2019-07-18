 //控制层 
app.controller('goodsController' ,function($scope,$controller,goodsService,uploadService,itemCatService,$location,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		var id = $location.search()['id'];
		if (id == null){
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				editor.html($scope.entity.goodsDesc.introduction)//商品介绍
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages)//商品图片
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems)//扩展属性
				$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems)//规格选项
				//循环把规格选项的字符串形式转换成对象
				for (var i = 0;i<$scope.entity.itemList.length;i++){
                    $scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);				
	}
	//保存 
	$scope.save=function(){
        $scope.entity.goodsDesc.introduction=editor.html();
        var object;
        if($scope.entity.goods.id == null){
        	object = goodsService.add($scope.entity)
		}else {
        	object = goodsService.update($scope.entity)
		}
        object.success(
			function(response){
				if(response.success){
					//提示信息
					alert("保存成功!");
					//跳转到商品页面
					location.href="goods.html";
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//查询用户名
	$scope.showLoginName=function () {
		goodsService.showLoginName().success(
			function (response) {
                alert(response.loginName);
                $scope.loginName=response.loginName;
            }
		)
    }

    //上传文件
	$scope.uploadFile=function () {
		uploadService.uploadFile().success(
			function (response) {
				if (response.success){
			$scope.image_entity.url=response.message;
				}else {
					alert(response.message)
				}
            }
		)
    }
    //保存增加Image列表属性
    $scope.add_image_entity=function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }
    //添加图片
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};
	$scope.deleImage=function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index,1);
    }
    //查询一级商品分类列表
	$scope.selectItemCat1List=function () {
		itemCatService.findByParentId(0).success(
			function (response) {
				$scope.itemCat1List=response;
            }
		)
    }
    //监控变量 执行函数
    $scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List=response;
            }
        )
    })
	//读取三级分类列表
    $scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List=response;
            }
        )
    })
	//根据监控三级列表的id变化,查询对应的模板id
    $scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
        itemCatService.findOne(newValue).success(
        	function (response) {
				$scope.entity.goods.typeTemplateId=response.typeId;
            }
		)
    })
	//读取模板id后,读取品牌列表
	//读取扩展属性
	$scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
        	function (response) {
				 $scope.typeTemplate=response;
				 $scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);
				 //扩展属性
                if ($location.search()['id']== null){//判断是增加商品
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse( $scope.typeTemplate.customAttributeItems);
                }
            }
		);
        typeTemplateService.findSpecList(newValue).success(
				function (response) {
					$scope.specList=response;
                }
			)
    })
	//更改规格属性的方法
	$scope.updateSpecAttribute=function ($event,name, value) {
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		if (object != null){
			//直接添加Push
			if ($event.target.checked){
                object.attributeValue.push(value);
           }else{
                object.attributeValue.splice(object.attributeValue.indexOf(value),1)
				if (object.attributeValue.length==0){
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1)
				}
			}
		}else {
			//创建集合
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]})
		}
    }
    //创建sku列表
	$scope.createItemList=function () {
		//初始化列表
		$scope.entity.itemList=[{spec:{},price:0,num:999,status:'0',isDefault:'0'}];
		var items=$scope.entity.goodsDesc.specificationItems;//勾选的规格和规格选项集合
		for (var i = 0;i<items.length;i++){//遍历的是勾选的集合
            $scope.entity.itemList=addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue)
		}
		
    }
    //抽取的增加行的方法
	addColumn=function (list, columnName, columnValues) {
		var newList = [];
		for (var i = 0;i<list.length;i++){
			var oldRow=list[i];
			for (var j = 0;j<columnValues.length;j++){
				var newRow = JSON.parse(JSON.stringify(oldRow));
				newRow.spec[columnName]=columnValues[j];
				newList.push(newRow);
			}
		}return newList;
    }
    $scope.status=['未审核','已审核','审核未通过','已关闭'];
	//定义一个数组存放商品分类
	$scope.itemCatList=[];
	//查询商品分类列表
	$scope.findItemCatList= function () {
		itemCatService.findAll().success(
			function (response) {
				for (var i = 0;i<response.length;i++){
					$scope.itemCatList[response[i].id]=response[i].name;
				}
            }
		)
    }
    //根据规格名称和规格选项名称返回是否勾选
	$scope.checkAttributeValue=function (specName,optionName) {
      var items =  $scope.entity.goodsDesc.specificationItems;
      var object = $scope.searchObjectByKey(items,'attributeName',specName);
      if (object != null){
		if(object.attributeValue.indexOf(optionName)>=0){
			return true;
		}else {
			return false;
		}
	  }else {
			return false;
	  }
    }

});
