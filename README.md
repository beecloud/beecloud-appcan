# uexBeeCloud


</br>
## **概述**
[uexBeeCloud](http://plugin.appcan.cn/details.html?id=481_index) 封装了支付宝（ALI\_APP），微信（WX\_APP），银联（UN\_APP）三个主流渠道的支付接口。  
使用此模块可轻松实现各个渠道的支付功能。
使用之前需要先到[BeeCloud](https://beecloud.cn) 注册认证，并[快速开始](https://beecloud.cn/apply)接入BeeCloud Pay.

</br>
</br>
## **AppCan插件**

**此插件需要用户自定义使用**  
iOS 插件`uexBeeCloud`需要自定义插件使用，即需要更改`uexBeeCloud`插件包里的 uexBeeCloud.plist 文件的`CFBundleURLSchemes`值。
>使用微信支付，请同时勾选插件uexWeixin。

配置示例:

```js
<dict>
	<key>CFBundleURLTypes</key>
	<array>
		<dict>
			<key>CFBundleURLName</key>
			<string>uexBeeCloud</string>
			<key>CFBundleURLSchemes</key>
			<array>
				<string>自定义的URL Scheme</string>
			</array>
		</dict>
	</array>
</dict>
```
配置描述:  
> 如果需要使用微信支付，必须配置URL Scheme为微信开放平台APPID;  
> 如果不需要使用微信支付，，可以自定义填写。

</br>
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
        wxAppId: "wxf1aa465362b4c8f1"
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
       scheme: '自定义的URL Scheme',
     optional: {'userID':'张三','mobile':'0512-86861620'} //用于商户扩展业务参数
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
 
scheme：  

 * 类型：String  
 * 默认值：无, `iOS支付宝支付必填` 
 * 描述：Url Scheme。支付宝需要，在插件包中的uexBeeCloud.plist中配置。如果需要使用微信支付，请在uexBeeCloud.plist中将URLScheme的值配置为微信开放平台APPID。
 
optional：  

 * 类型：Map(String, String) 
 * 默认值：无, 非必填  
 * 描述：商户业务扩展集。用于商户传递处理业务参数。例：{'userID':'张三','mobile':'0512-86861620'}
  
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










