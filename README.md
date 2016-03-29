# uexBeeCloud


</br>
## **概述**
[uexBeeCloud](http://plugin.appcan.cn/details.html?id=481_index) 封装了支付宝(ALI\_APP)、微信(WX\_APP)、银联(UN\_APP)、百度钱包(BD_APP)四个主流渠道的支付接口。  
使用此模块可轻松实现各个渠道的支付功能。使用之前需要先到[BeeCloud](https://beecloud.cn) 注册认证，并[快速开始](https://beecloud.cn/apply)接入BeeCloud Pay.

</br>
## **AppCan插件**

> **使用微信支付，请同时勾选AppCan官网公共插件里**uexWeixin**.**

`uexBeeCloud`iOS插件需要用户在`config.xml`配置使用，示例配置代码如下:

```
<config desc="uexBeeCloud" type="URLSCHEME">
      <urlScheme name="uexBeeCloud" schemes="['wxf1aa465362b4c8f1']"/>
</config>
```
配置描述:

* 如果需要使用微信支付，"wxf1aa465362b4c8f1"换成您自己申请的微信开放平台APPID  
* 如果不需要使用微信支付，可以自定义填写  

iOS 9 以后，为了预防APP通过非正常渠道获取用户的某些隐私信息，Apple启用了URLScheme白名单机制。
	
* **为了正常使用插件的所有功能还需要配置URLScheme白名单**([什么是URLScheme白名单](http://bbs.appcan.cn/forum.php?mod=viewthread&tid=29503&extra=))
* 配置白名单方法请参考[这里](http://newdocx.appcan.cn/newdocx/docx?type=1505_1291#设置urlScheme白名单)
* **uexBeeCloud**需要进白名单添加的URLScheme如下

```
<config desc="whiteList" type="AUTHORITY">
      <permission platform="iOS" info="urlSchemeWhiteList">
          <string>weixin</string>
          <string>wxpay</string>
          <string>alipay</string>
      </permission>
</config>
```

</br>
## 配置异步通知地址webhook url
支付成功后，BeeCloud将向用户在BeeCloud的"控制台->设置->Webhook"中指定的URL发送状态数据。用户可以根据该状态数据，结合自身系统内记录的订单信息做相应的处理。[查看webhook文档](https://beecloud.cn/doc/?index=11)

>如果在BeeCloud控制台配置了webhook url，用户支付成功后，BeeCloud会向webhook url推送订单支付成功消息。 未配置webhook url，BeeCloud不会发送异步通知。  
>服务器间的交互,不像页面跳转同步通知(REST Api中bill的参数return_url指定)可以在页面上显示出来，这种交互方式是通过后台通信来完成的，对用户是不可见的。  


</br>
## 沙箱测试
**在初始化时设置是否切换成沙箱测试模式**  
> 沙箱测试环境下**不产生**真实的交易

```js
//init
var bcData = {
   bcAppId: "c5d1cba1-5e3f-4ba0-941d-9b0a371fe719",
   wxAppId: "wxf1aa465362b4c8f1",
   sandbox: true //设置为true表示打开沙箱测试模式，默认为false(生产模式)
}
uexBeeCloud.initBeeCloud(JSON.stringify(bcData));
```

</br>
## **pay**

### 支付方法原型：  

```js  
	pay(jsonStr);
```	
### 调用示例：

```js
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no">
<title>支付列表</title>
<style type="text/css"></style>
<script type="text/javascript">
 window.uexOnload = function(type) {
    //init
    var bcData = {
        bcAppId: "c5d1cba1-5e3f-4ba0-941d-9b0a371fe719",
        wxAppId: "wxf1aa465362b4c8f1",
        sandbox: true //设置为true表示打开沙箱测试模式，默认为false(生产模式)
    }
    uexBeeCloud.initBeeCloud(JSON.stringify(bcData));
        
    uexBeeCloud.cbPay = function(opId, dataType, data) {
            var json = JSON.parse(data);
            alert(json.result_msg);
        }
    }

function pay() {
    //pay action
    var payData = {
      channel: "WX_APP",
        title: "appcan",
       billno: "2015082418050048",
     totalfee: 1,
     optional: {'userID':'张三','mobile':'0512-86861620'} //用于商户扩展业务参数,会在webhook回调中返回
    };
    uexBeeCloud.pay(JSON.stringify(payData));
}
</script>
</head>
<body>
<div class="pay_info">
  <h1><span>比可网络</span><br />¥100.00</h1>
  <p>商品<span>自制白开水</span></p>
  <p>交易单号<span>161165161df1d1fasd1616165</span></p>
</div>
<ul class="pay_list">
  <li onClick="pay();"><i></i><span><b>微信支付</b><br>WX_APP</span></li>
</ul>
</body>
</html>
```

### 字段说明 
channel：

 * 类型：String  
 * 默认值：无  
 * 描述：支付渠道。微信 WX_APP，支付宝 ALI_APP，银联在线 UN_APP
 
title：  

 * 类型：String  
 * 默认值：无  
 * 描述：订单描述。32个字节，最长支持16个汉字。
 
billno：

 * 类型：String  
 * 默认值：无, 必填  
 * 描述：订单号。8~32位字母和\或数字组合，必须保证在商户系统中唯一。建议根据当前时间生成订单号，格式为：yyyyMMddHHmmssSSS,"201508191436987"。
 
totalfee：  

 * 类型：Number  
 * 默认值：无, 必填  
 * 描述：订单金额。以分为单位，例如：100代表1元。
 
optional：  

 * 类型：Map(String, String) 
 * 默认值：无, 非必填  
 * 描述：商户业务扩展集。用于商户传递处理业务参数,会在[webhook回调](https://beecloud.cn/doc/?index=8)中返回。例：{'userID':'张三','mobile':'0512-86861620'}
  
</br>
</br>  
## **cbPay**

### 回调原型

```js
 cbPay(opId, dataType, data);
```

#### 字段说明
opId:  

 * 类型：Number类型
 * 描述：必选操作ID，此函数中不起作用，可忽略。  
 
dataType:

 * 类型：Number类型
 * 描述：必选数据类型详见[CONSTANT]中Callback方法数据类型。  
 
data:

 * 类型：String
 * 描述：必选json类型，格式如下：{"result_code": 0,"result_msg": "支付成功","err_detail": ""}


### 回调示例：

```js
//成功
{
	result_code: 0,
	result_msg: "支付成功",
	err_detail: ""
}
//失败
{
	result_code: -1,
	result_msg: "title 必须是长度不大于32个字节,最长16个汉字的字符串的合法字符串",
	err_detail: "title 必须是长度不大于32个字节,最长16个汉字的字符串的合法字符串"
}
``` 

</br>
</br>
## **getApiVersion**

### 获取API版本方法原型：

```js
getApiVersion();
```

### 示例代码

```js
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no">
<title>获取API版本号</title>
<style type="text/css"></style>
</head>
<body>
<ul class="pay_list">
  <li onClick="getApiVersion();"><i></i><span><b>获取API版本号</b><br>getApiVersion</span></li>
</ul>
</body>
<script type="text/javascript">
    window.uexOnload = function(type) {
        
        uexBeeCloud.cbGetApiVersion = function(opId, dataType, data) {
            var json = JSON.parse(data);
            alert(data);
        }
    }

function getApiVersion() {
    uexBeeCloud.getApiVersion();
}
</script>
</html>
```

</br>
</br>
## cbGetApiVersion

### 回调原型

```js
cbGetApiVersion(opId, dataType, data);
```

#### 字段说明
opId:  

 * 类型：Number类型
 * 描述：必选操作ID，此函数中不起作用，可忽略。  
 
dataType:

 * 类型：Number类型
 * 描述：必选数据类型详见[CONSTANT]中Callback方法数据类型。  
 
data:

 * 类型：String
 * 描述：必选json类型，格式如下：{"apiVersion": "1.0.0"} 
 
### 回调示例

```js
{
	apiVersion: "1.0.0" 
}
```

## 版本支持

#### 3.0.0+ 


</br>
</br>
</br>










