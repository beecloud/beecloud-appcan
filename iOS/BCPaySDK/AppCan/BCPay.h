//
//  BCPay.h
//  BCPaySDK
//
//  Created by Ewenlong03 on 15/8/11.
//  Copyright (c) 2015年 BeeCloud. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EUExBase.h"

@class BCBaseReq;
@class BCBaseResp;

@interface EUExBeeCloud : EUExBase
//
///**
// *  全局初始化
// *
// *  @param appId     BeeCloud平台APPID
// *  @param appSecret BeeCloud平台APPSECRET
// */
//- (void)initWithAppID:(NSMutableArray *)inArguments;
//
///**
// *  需要在每次启动第三方应用程序时调用。第一次调用后，会在微信的可用应用列表中出现。
// *  iOS7及以上系统需要调起一次微信才会出现在微信的可用应用列表中。
// *
// *  @param wxAppID 微信开放平台创建APP的APPID
// *
// *  @return 成功返回YES，失败返回NO。只有YES的情况下，才能正常执行微信支付。
// */
//- (void)initWeChatPay:(NSString *)wxAppID;
//
///**
// * 处理通过URL启动App时传递的数据。需要在application:openURL:sourceApplication:annotation:中调用。
// *
// * @param url 启动第三方应用时传递过来的URL
// *
// * @return 成功返回YES，失败返回NO。
// */
//- (BOOL)handleOpenUrl:(NSURL *)url;
//
///**
// *  获取API版本号
// *
// *  @return 版本号
// */
//- (NSString *)getBCApiVersion;
//
///**
// *  设置是否打印log
// *
// *  @param flag YES打印
// */
//- (void)setWillPrintLog:(BOOL)flag;
//
///**
// *  设置网络请求超时时间
// *
// *  @param time 超时时间, 5.0代表5秒。
// */
//- (void)setNetworkTimeout:(NSTimeInterval)time;
//
//#pragma mark - Send BeeCloud Request
//
///**
// *  发送BeeCloud Api请求
// *
// *  @param req 请求体
// *
// *  @return 发送请求是否成功
// */
//- (void)sendBCReq:(BCBaseReq *)req;


@end
