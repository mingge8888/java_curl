用法 直接复制在进控制器测试，
java类插件，高仿前端版的JQ的ajax参数方式，用Map类型进行控制参数
可读取http和https 支持get 和post ,上传文件等等操作
一般用于远程读取url,爬虫，阿里API接口等功能
必须导入httpclient4以上依赖
    Curl CurlFn = new Curl(obj);
     @param obj 通过Map类型批量传入参数
               obj.data   发送的数据 Map<String,String>
               obj.timeout  超时时间 String
               obj.header  支持头部 Map<String,String>
               obj.contentType  输出编码 String
               obj.method   get or post String
               obj.url  回话url地址  String
    return CurlFn.fetch(); //返回数据
    return CurlFn.error(); //返回错误