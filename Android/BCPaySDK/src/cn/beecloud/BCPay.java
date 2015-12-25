/**
 * BCPay.java
 *
 * Created by xuanzhui on 2015/7/27.
 * Copyright (c) 2015 BeeCloud. All rights reserved.
 */
package cn.beecloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alipay.sdk.app.PayTask;
import com.baidu.android.pay.PayCallBack;
import com.baidu.paysdk.PayCallBackManager;
import com.baidu.paysdk.api.BaiduPay;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.beecloud.BCHttpClientUtil.Response;
import cn.beecloud.entity.BCPayReqParams;
import cn.beecloud.entity.BCPayResult;
import cn.beecloud.entity.JSBridgePayReqParams;

/**
 * 支付类
 * 对于当前页面只初始化一次
 */
public class BCPay extends EUExBase {
	static final String TAG = "BCPay";
	
	static final String func_version_callback = "uexBeeCloud.cbGetApiVersion";
	
	static final String func_pay_callback = "uexBeeCloud.cbPay";
	
	private static Context context;
	public static BCPay instance;

	public BCPay(Context arg0, EBrowserView arg1) {
		super(arg0, arg1);
		context = arg0;
	}

	public static final String apiVersion = "1.3.0";

    // IWXAPI 是第三方app和微信通信的openapi接口
    public IWXAPI wxAPI = null;
    
    /**
     * 初始化beecloud appid
     * @param wxAppId
     */
    public void initBeeCloud(String[] appInfo) {
    	//Log.w(TAG, "initBeeCloud");
    	
    	if (appInfo == null || appInfo[0] == null || 
    			appInfo[0].length() == 0) {
    		Log.e(TAG, "appInfo NPE");
    		return;
    	}
    	
    	Gson res = new Gson();

        Map<String, Object> responseMap = res.fromJson(appInfo[0], 
        		new TypeToken<Map<String,Object>>() {}.getType());

        String appId = (String)responseMap.get("bcAppId");
    	
        if (appId != null &&
        		appId.length() != 0)
    		BeeCloud.setAppId(appId);
        
        Boolean sandbox = Boolean.FALSE;
        if (responseMap.get("sandbox") != null)
        	sandbox = (Boolean) responseMap.get("sandbox");
        
        if (sandbox.equals(Boolean.TRUE)) {
			BeeCloud.setSandbox(true);
		}
        
        String wxAppId = (String)responseMap.get("wxAppId");
        
        if (wxAppId != null && wxAppId.length() != 0)
        	initWeChat(wxAppId);
    }
 
    /**
     * 初始化微信支付，必须在需要调起微信支付的Activity的onCreate函数中调用
     * 微信支付只有经过初始化才能成功调起，其他支付渠道无此要求
     */
    public void initWeChat(String wxAppId) {

        String errMsg = null;

        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        wxAPI = WXAPIFactory.createWXAPI(context, null);

        BCCache.getInstance().wxAppId = wxAppId;

        try {
            if (isWXPaySupported()) {
                // 将该app注册到微信
                wxAPI.registerApp(wxAppId);
            } else {
                errMsg = "Error: 安装的微信版本不支持支付.";
                Log.d(TAG, errMsg);
            }
        } catch (Exception ignored) {
            errMsg = "Error: 无法注册微信 " + wxAppId + ". Exception: " + ignored.getMessage();
            Log.e(TAG, errMsg);
        }
    }

    /**
     * 判断微信是否支持支付
     * @return true表示支持
     */
    private boolean isWXPaySupported() {
        boolean isPaySupported = false;
        if (wxAPI != null) {
            isPaySupported = wxAPI.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
        }
        return isPaySupported;
    }

    /**
     * 校验bill参数
     * 设置公用参数
     *
     * @param billTitle       商品描述, 32个字节内, 汉字以2个字节计
     * @param billTotalFee    支付金额，以分为单位，必须是正整数
     * @param billNum         商户自定义订单号
     * @param parameters      用于存储公用信息
     * @param optional        为扩展参数，可以传入任意数量的key/value对来补充对业务逻辑的需求
     * @return                返回校验失败信息, 为null则表明校验通过
     */
    private String prepareParametersForPay(final String billTitle, final Integer billTotalFee,
                                           final String billNum, final Map<String, String> optional,
                                           BCPayReqParams parameters) {

        if (!BCValidationUtil.isValidBillTitleLength(billTitle)) {
            return "title 必须是长度不大于32个字节,最长16个汉字的字符串的合法字符串";
        }

        if (!BCValidationUtil.isValidBillNum(billNum))
            return "billno 必须是长度8~32位字母和/或数字组合成的字符串";

        if (billTotalFee < 0) {
            return "totalfee 以分为单位，必须是整数";
        }

        parameters.title = billTitle;
        parameters.totalFee = billTotalFee;
        parameters.billNum = billNum;
        parameters.optional = optional;

        return null;
    }
    
    public void getApiVersion(String[] parms) {
    	Log.w("BCPay", "getApiVersion");
    	
    	JSONObject ret = new JSONObject();
    	
    	try {
			ret.put("apiVersion", apiVersion);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
    	
    	jsCallback(func_version_callback, 0, EUExCallback.F_C_JSON, ret.toString());
    }
 
    void payCallBack(final int resultCode, final String resultMsg, final String errDetail){
    	    	
    	BCCache.executorService.execute(new Runnable(){

			@Override
			public void run() {
				Gson gson = new Gson(); 
		    	
		    	Map<String, Object> resultMap = new HashMap<String, Object>();
		    	resultMap.put("result_code", resultCode);
		    	resultMap.put("result_msg", resultMsg);
		    	resultMap.put("err_detail", errDetail);
		    	
		    	jsCallback(func_pay_callback, 0, EUExCallback.F_C_JSON, gson.toJson(resultMap));
			}
    		
    	});
    }
    
    public void pay(final String[] params) {
  	
    	if (params == null || params[0] == null){
    		payCallBack(BCPayResult.BC_ERR_CODE_COMMON, "参数检查出错", "params invalid:NPE");
    	}
    	
    	//保留当前的instance用于支付结束的回调
    	instance = this; 
    	
        BCCache.executorService.execute(new Runnable() {
            @Override
            public void run() {
            	Gson gson = new Gson();
            	JSBridgePayReqParams jsPayReqParams = gson.fromJson(params[0], 
            			new TypeToken<JSBridgePayReqParams>() {}.getType());
      
            	String channelType = jsPayReqParams.getChannel();
            	String billTitle = jsPayReqParams.getTitle();
            	Integer billTotalFee = jsPayReqParams.getTotalfee();
            	String billNum = jsPayReqParams.getBillno();
            	
            	Map<String, String> optional = jsPayReqParams.getOptional();

                //校验并准备公用参数
                BCPayReqParams parameters = null;
                try {
                    parameters = new BCPayReqParams(channelType);
                } catch (BCException e) {
                	
                	payCallBack(BCPayResult.BC_ERR_CODE_COMMON, "参数检查出错", e.getMessage());
                	
                    return;
                }

                String paramValidRes = prepareParametersForPay(billTitle, billTotalFee,
                        billNum, optional, parameters);

                if (paramValidRes != null) {
                	payCallBack(BCPayResult.BC_ERR_CODE_COMMON, "参数检查出错", paramValidRes);
                	
                    return;
                }

                String payURL = BCHttpClientUtil.getBillPayURL();

                Response response = BCHttpClientUtil.httpPost(payURL, parameters.transToBillReqMapParams());
                
                if (null == response) {
                	payCallBack(BCPayResult.BC_ERR_CODE_COMMON, "网络请求失败", "网络请求失败");
                    return;
                }
                
                if (response.code == 200) {
                    final String serverRet;
                	serverRet = response.content;

                	Gson res = new Gson();

                    Map<String, Object> responseMap = res.fromJson(serverRet, 
                    		new TypeToken<Map<String,Object>>() {}.getType());

                    //判断后台返回结果
                    Double resultCode = (Double) responseMap.get("result_code");
                    if (resultCode == 0) {
                    	BCCache.getInstance().billID = (String)responseMap.get("id");
						
						//如果是测试模式
                        if (BCCache.getInstance().isTestMode) {
                            reqTestModePayment(billTitle, billTotalFee);
                            return;
                        }
                        
                        //针对不同的支付渠道调用不同的API
						if (channelType.equals("WX_APP")){
							reqWXPaymentViaAPP(responseMap);
						} else if (channelType.equals("ALI_APP")) {
							reqAliPaymentViaAPP(responseMap);
						} else if (channelType.equals("UN_APP")) {
							reqUnionPaymentViaAPP(responseMap);
						} else if (channelType.equals("BD_APP")) {
							reqBaiduPaymentViaAPP(responseMap);
						}
						else {
							payCallBack(BCPayResult.BC_ERR_CODE_COMMON, "参数检查出错",
									"channel渠道不支持");
						}
                    } else {
                        //返回后端传回的错误信息
                    	payCallBack(resultCode.intValue(), String.valueOf(responseMap.get("result_msg")), 
                    			String.valueOf(responseMap.get("err_detail")));
			
                    }
                } else {
                	payCallBack(BCPayResult.BC_ERR_CODE_COMMON, "网络请求失败", 
                			"网络请求失败");
                }

            }
        });
    }
    
    private void reqTestModePayment(String billTitle, Integer billTotalFee) {
        Intent intent = new Intent(context, BCMockPayActivity.class);
        intent.putExtra("id", BCCache.getInstance().billID);
        intent.putExtra("billTitle", billTitle);
        intent.putExtra("billTotalFee", billTotalFee);
        startActivity(intent);
    }
    
    /**
     * 与服务器交互后下一步进入微信app支付
     *
     * @param responseMap     服务端返回参数
     */
    private void reqWXPaymentViaAPP(final Map<String, Object> responseMap) {
    	if (wxAPI == null || !isWXPaySupported()) {
    		payCallBack(BCPayResult.BC_ERR_CODE_COMMON, "参数检查错误", "未找到微信客户端，请先下载安装");
    		return;
    	}
    	
        //获取到服务器的订单参数后，以下主要代码即可调起微信支付。
        PayReq request = new PayReq();
        request.appId = String.valueOf(responseMap.get("app_id"));
        request.partnerId = String.valueOf(responseMap.get("partner_id"));
        request.prepayId = String.valueOf(responseMap.get("prepay_id"));
        request.packageValue = String.valueOf(responseMap.get("package"));
        request.nonceStr = String.valueOf(responseMap.get("nonce_str"));
        request.timeStamp = String.valueOf(responseMap.get("timestamp"));
        request.sign = String.valueOf(responseMap.get("pay_sign"));

        if (wxAPI != null) {
            wxAPI.sendReq(request);
        } else {
        	payCallBack(BCPayResult.BC_ERR_CODE_COMMON, "参数异常", "Error: 微信API为空, 需要初始化");
        }
    }

    /**
     * 与服务器交互后下一步进入支付宝app支付
     *
     * @param responseMap     服务端返回参数
     */
    private void reqAliPaymentViaAPP(final Map<String, Object> responseMap) {

        String orderString = (String) responseMap.get("order_string");

        PayTask aliPay = new PayTask((Activity)context);
        String aliResult = aliPay.pay(orderString);

        //解析ali返回结果
        Pattern pattern = Pattern.compile("resultStatus=\\{(\\d+?)\\}");
        Matcher matcher = pattern.matcher(aliResult);
        String resCode = "";
        if (matcher.find())
            resCode = matcher.group(1);

        final int result;
        final String errMsg;
        final String errDetail;

        //9000-订单支付成功, 8000-正在处理中, 4000-订单支付失败, 6001-用户中途取消, 6002-网络连接出错
        if (resCode.equals("9000")) {
            result = BCPayResult.BC_SUCC;
            errMsg = BCPayResult.RESULT_SUCCESS;
            errDetail = BCPayResult.RESULT_SUCCESS;
        } else if (resCode.equals("6001")) {
            result = BCPayResult.BC_CANCEL;
            errMsg = BCPayResult.RESULT_CANCEL;
            errDetail = BCPayResult.RESULT_CANCEL;
        } else if (resCode.equals("4000") || resCode.equals("6002")){
        	result = BCPayResult.BC_ERR_FAIL;
            errMsg = "正在处理中";
            errDetail = "正在处理中";
        } else {
            result = BCPayResult.BC_ERR_CODE_COMMON;
            errMsg = BCPayResult.FAIL_ERR_FROM_CHANNEL;
            errDetail = aliResult;
        }
    	
		payCallBack(result, errMsg, errDetail);

    }

    /**
     * 与服务器交互后下一步进入银联app支付
     *
     * @param responseMap     服务端返回参数
     */
    private void reqUnionPaymentViaAPP(final Map<String, Object> responseMap) {

        String TN = (String) responseMap.get("tn");

        Intent intent = new Intent();
        intent.setClass(context, BCUnionPaymentActivity.class);
        intent.putExtra("tn", TN);
        startActivity(intent);
        //startActivityForResult(intent, 1);
    }
    
    /**
     * 与服务器交互后下一步进入百度app支付
     *
     * @param responseMap     服务端返回参数
     */
    private void reqBaiduPaymentViaAPP(final Map<String, Object> responseMap) {
        String orderInfo = (String) responseMap.get("orderInfo");

        //Log.w(TAG, orderInfo);

        Map<String, String> map = new HashMap<String, String>();
        BaiduPay.getInstance().doPay(context, orderInfo, new PayCallBack() {
            public void onPayResult(int stateCode, String payDesc) {
                //Log.w(TAG, "rsult=" + stateCode + "#desc=" + payDesc);

                final int result;
                final String errMsg;
                final String errDetail;

                switch (stateCode) {
                    case PayCallBackManager.PayStateModle.PAY_STATUS_SUCCESS:// 需要到服务端验证支付结果

                        result = BCPayResult.BC_SUCC;
                        errMsg = BCPayResult.RESULT_SUCCESS;
                        errDetail = errMsg;
                        break;
                    case PayCallBackManager.PayStateModle.PAY_STATUS_PAYING:// 需要到服务端验证支付结果
                        result = BCPayResult.BC_ERR_FAIL;
                        errMsg = BCPayResult.FAIL_ERR_FROM_CHANNEL;
                        errDetail = "订单正在处理中，无法获取成功确认信息";
                        break;
                    case PayCallBackManager.PayStateModle.PAY_STATUS_CANCEL:
                        result = BCPayResult.BC_CANCEL;
                        errMsg = BCPayResult.RESULT_CANCEL;
                        errDetail = errMsg;
                        break;
                    case PayCallBackManager.PayStateModle.PAY_STATUS_NOSUPPORT:
                        result = BCPayResult.BC_ERR_CODE_COMMON;
                        errMsg = BCPayResult.FAIL_ERR_FROM_CHANNEL;
                        errDetail = "不支持该种支付方式";
                        break;
                    case PayCallBackManager.PayStateModle.PAY_STATUS_TOKEN_INVALID:
                        result = BCPayResult.BC_ERR_CODE_COMMON;
                        errMsg = BCPayResult.FAIL_ERR_FROM_CHANNEL;
                        errDetail = "无效的登陆状态";
                        break;
                    case PayCallBackManager.PayStateModle.PAY_STATUS_LOGIN_ERROR:
                        result = BCPayResult.BC_ERR_CODE_COMMON;
                        errMsg = BCPayResult.FAIL_ERR_FROM_CHANNEL;
                        errDetail = "登陆失败";
                        break;
                    case PayCallBackManager.PayStateModle.PAY_STATUS_ERROR:
                        result = BCPayResult.BC_ERR_FAIL;
                        errMsg = BCPayResult.FAIL_ERR_FROM_CHANNEL;
                        errDetail = "支付失败";
                        break;
                    case PayCallBackManager.PayStateModle.PAY_STATUS_LOGIN_OUT:
                        result = BCPayResult.BC_ERR_CODE_COMMON;
                        errMsg = BCPayResult.FAIL_ERR_FROM_CHANNEL;
                        errDetail = "退出登录";
                        break;
                    default:
                        result = BCPayResult.BC_ERR_FAIL;
                        errMsg = BCPayResult.FAIL_ERR_FROM_CHANNEL;
                        errDetail = "支付失败";
                        break;
                }
                
                payCallBack(result, errMsg, errDetail);	
        		
            }

            public boolean isHideLoadingDialog() {
                return true;
            }
        }, map);

    }

	@Override
	protected boolean clean() {
		return false;
	}
}
