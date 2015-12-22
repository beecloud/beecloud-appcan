//
//  BCPayCache.h
//  BeeCloud SDK
//
//  Created by Junxian Huang on 2/27/14.
//  Copyright (c) 2014 BeeCloud Inc. All rights reserved.
//
#import <Foundation/Foundation.h>

/*!
 This header file is *NOT* included in the public release.
 */

/**
 *  BCCache stores system settings and content caches.
 */
@interface BCPayCache : NSObject

/**
 *  App key obtained when registering this app in BeeCloud website. Change this value via [BeeCloud setAppKey:];
 */
@property (nonatomic, strong) NSString *appId;

/**
 *  wechat open platform appID
 */
@property (nonatomic, strong) NSString *wxAppID;

/**
 *  YES表示沙箱测试环境
 */
@property (nonatomic, assign) BOOL sandbox;

/**
 *  Default network timeout in seconds for all network requests. Change this value via [BeeCloud setNetworkTimeout:];
 */
@property (nonatomic) NSTimeInterval networkTimeout;

/**
 *  Mark whether print log message.
 */
@property (nonatomic, assign) BOOL willPrintLogMsg;

/**
 *  Get the sharedInstance of BCCache.
 *
 *  @return BCCache shared instance.
 */
+ (instancetype)sharedInstance;

/**
 *  当前模式
 *
 *  @return YES表示沙箱测试模式；NO表示生产模式
 */
+ (BOOL)currentMode;

@end
