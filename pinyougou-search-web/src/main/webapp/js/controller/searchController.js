app.controller("searchController",function ($scope,searchService,$location) {
   $scope.search=function () {
       $scope.searchMap.pageNum=parseInt($scope.searchMap.pageNum);

       searchService.search($scope.searchMap).success(
           function (response) {
            $scope.resultMap=response;
            //构建分页栏
               buildPageLabel();
           }
       );
   }
   //分页栏
    buildPageLabel=function () {
       $scope.pageLabel=[];

        var maxpageNum= $scope.resultMap.totalPages;//得到最后页码
        var firstPage=1;//开始页码
        var lastPage=maxpageNum;//截止页码
        $scope.firstDot=false;//前面没点
        $scope.lastDot=false;//后面没点
        if($scope.resultMap.totalPages> 5){ //如果总页数大于 5 页,显示部分页码
            if($scope.searchMap.pageNum<=3){//如果当前页小于等于 3
                lastPage=5; //前 5 页
                $scope.lastDot=true;
            }else if( $scope.searchMap.pageNum>=lastPage-2 ){//如果当前页大于等于最大
                firstPage= maxpageNum-4; //后 5 页
                $scope.firstDot=true;
            }else{ //显示当前页为中心的 5 页
                firstPage=$scope.searchMap.pageNum-2;
                lastPage=$scope.searchMap.pageNum+2;
                $scope.lastDot=true;
                $scope.firstDot=true;
            }
        }
        for (i=firstPage;i<=lastPage;i++){
           $scope.pageLabel.push(i);
       }

    };

   $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNum':1,'pageSize':20,'sortField':'','sort':''};//定义搜索对象
    //添加搜索项的方法
    $scope.addSearchItem=function (key, value) {
        if (key=='category' || key=='brand' || key=='price'){
            $scope.searchMap[key]=value;
        }else {
            $scope.searchMap.spec[key]=value;
        }
        $scope.search();
    }
        //撤销搜索项的方法
    $scope.removeSearchItem=function (key) {
        if (key=='category' || key=='brand' || key=='price'){
            $scope.searchMap[key]='';
        }else {
           delete $scope.searchMap.spec[key];
        }
        $scope.search();
    }
    //根据页码查询的方法
    $scope.queryByPage=function (page) {
        if (page<1){
            return;
        }if( page > $scope.resultMap.totalPages){
            page=$scope.resultMap.totalPages;
        }
        $scope.searchMap.pageNum=page;
        $scope.search();
    }
    //判断页码是否为第一页
    $scope.isTopPage=function () {
        if ($scope.searchMap.pageNum == 1){
            return true;
        }else {
            return false;
        }
    }
    //判断页码是否为最后一页
    $scope.isEndPage=function () {
        if ($scope.searchMap.pageNum == $scope.resultMap.totalPages){
            return true;
        }else {
            return false;
        }
    }
    //升序降序排序
    $scope.searchSort=function (sortField,sortValue) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sortValue;
        $scope.search();
    }
    //判断关键字是否为品牌
    $scope.keywordsIsBrand=function () {
       for (var i = 0;i< $scope.resultMap.brandList.length;i++){
           if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
               return true;
           }
       }
        return false;
    }
    //接收关键字的方法
    $scope.loadkeywords=function () {
       $scope.searchMap.keywords= $location.search()['keywords'];//获取关键字
       $scope.search();
    }
});