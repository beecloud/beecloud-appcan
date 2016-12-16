//
//  BCPay.m
//  BCPaySDK
//
//  Created by Ewenlong03 on 15/8/11.
//  Copyright (c) 2015年 BeeCloud. All rights reserved.
//

#import "BeeCloud.h"
#import "BCPay.h"
#import "BCPayUtil.h"
#import "EUtility.h"
#import "JSON.h"
#import "BaiduViewController.h"

@interface EUExBeeCloud ()<BeeCloudDelegate,BaiduViewControllerDelegate>

@end

@implementation EUExBeeCloud

- (void)initBeeCloud:(NSMutableArray *)inArguments {
    
    if ([inArguments isKindOfClass:[NSMutableArray class]] && [inArguments count] > 0) {
        SBJsonParser *jsonParser = [[SBJsonParser alloc] init];
        NSMutableDictionary *params = [jsonParser objectWithString:[inArguments lastObject]];
        
        if (params) {
            [BCPay setBeeCloudDelegate:self];
            
            NSString *appId = [params stringValueForKey:@"bcAppId" defaultValue:@""];
            NSString *wxAppID = [params stringValueForKey:@"wxAppId" defaultValue:@""];
            [BCPayCache sharedInstance].sandbox = [params boolValueForKey:@"sandbox" defaultValue:NO];
            
            [BCPay initWithAppID:appId];
            [BCPay initWeChatPay:wxAppID];
        }
    }
}

- (void)pay:(NSMutableArray *)inArguments {
    if ([inArguments isKindOfClass:[NSMutableArray class]] && inArguments.count > 0) {
        BCPayReq *payReq = [[BCPayReq alloc] init];
        
        SBJsonParser *jsonParser = [[SBJsonParser alloc] init];
        NSMutableDictionary *params = [jsonParser objectWithString:[inArguments lastObject]];
        
        payReq.channel = [params stringValueForKey:@"channel" defaultValue:@""];
        payReq.title = [params stringValueForKey:@"title" defaultValue:@""];
        payReq.totalfee = [NSString stringWithFormat:@"%@",@([params integerValueForKey:@"totalfee" defaultValue:0])];
        payReq.billno = [params stringValueForKey:@"billno" defaultValue:@""];
        //scheme
        NSDictionary *info = [[NSBundle mainBundle] infoDictionary];
        NSArray *schemes = [info arrayValueForKey:@"CFBundleURLTypes" defaultValue:nil];
        if (schemes && schemes.count > 0) {
            NSDictionary *schdic = [schemes firstObject];
            NSArray *schs = [schdic arrayValueForKey:@"CFBundleURLSchemes" defaultValue:nil];
            if (schs && schs.count > 0) {
                payReq.scheme = [schs firstObject];
            }
        }
        payReq.viewController = [EUtility brwCtrl:meBrwView];
        payReq.optional = [params dictValueForKey:@"optional" defaultValue:nil];
        [BCPay sendBCReq:payReq];
    }
}

- (void)getApiVersion:(NSMutableArray *)inArguments {
    NSDictionary *resp = @{@"apiVersion": kApiVersion};
    [self performSelectorOnMainThread:@selector(callbackJsonWithName:) withObject:@{@"cbGetApiVersion": resp} waitUntilDone:NO];
}

- (void)isSandboxMode:(NSMutableArray *)inArguments {
    NSDictionary *resp = @{@"sandbox": @([BCPayCache currentMode])};
    [self performSelectorOnMainThread:@selector(callbackJsonWithName:) withObject:@{@"cbIsSandboxMode": resp} waitUntilDone:NO];
}

- (void)isWxAppInstalled:(NSMutableArray *)inArguments {
    NSDictionary *resp = @{@"install": @([BCPay isWXAppInstalled])};
    [self performSelectorOnMainThread:@selector(callbackJsonWithName:) withObject:@{@"cbIsWxAppInstalled": resp} waitUntilDone:NO];
}

- (void)onBeeCloudResp:(id)resp {
    [self performSelectorOnMainThread:@selector(callbackJsonWithName:) withObject:@{@"cbPay": resp} waitUntilDone:NO];
}

- (void)onBeeCloudBaidu:(NSString *)url {
    BaiduViewController *bd = [[BaiduViewController alloc] init];
    bd.url = url;
    bd.delegate = self;
    bd.view.frame = [EUtility brwViewFrame:meBrwView];
    
    [[EUtility brwCtrl:meBrwView].navigationController pushViewController:bd animated:YES];
}

- (void)showBaiduPayResult:(NSDictionary *)result {
    [self performSelectorOnMainThread:@selector(callbackJsonWithName:) withObject:@{@"cbPay":result} waitUntilDone:NO];
}

- (void)callbackJsonWithName:(NSDictionary *)data {
    NSString *result = [[data allValues].lastObject JSONFragment];
    NSString *cbName = [NSString stringWithFormat:@"%@.%@",kKeyMoudleName, [data allKeys].lastObject];
    [self jsSuccessWithName:cbName opId:1 dataType:1 strData:result];
}

- (NSString *)genOutTradeNo {
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyyMMddHHmmssSSS"];
    return [formatter stringFromDate:[NSDate date]];
}

+ (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation {
    return [BCPay handleOpenUrl:url];
}

+ (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<NSString *,id> *)options {
    return [BCPay handleOpenUrl:url];
}

@end
