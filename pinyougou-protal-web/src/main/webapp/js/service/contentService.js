app.service('contentService',function ($http) {
    //根据id查询分类广告
    this.findByCategoryId=function (id) {
        return $http.get('content/findByCategoryId.do?id='+id);
    }
});