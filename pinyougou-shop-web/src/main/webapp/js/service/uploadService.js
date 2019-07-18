app.service("uploadService",function ($http) {
   this.uploadFile=function () {
       //创建文件上传的对象
       var formData = new FormData();
       formData.append('file',file.files[0]);//file是文件上传框的Name
       return $http({
           url:'../upload.do',
           method:'post',
           data:formData,
           headers:{'Content-Type':undefined},
           transformRequest: angular.identity
       })
   }
});