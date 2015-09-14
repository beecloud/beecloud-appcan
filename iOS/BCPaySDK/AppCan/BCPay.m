//
//  BCPay.m
//  BCPaySDK
//
//  Created by Ewenlong03 on 15/8/11.
//  Copyright (c) 2015年 BeeCloud. All rights reserved.
//

#import "BCPay.h"
#import "BCPaySDK.h"
#import "BCPayUtil.h"
#import "EUtility.h"
#import "JSON.h"

@interface EUExBeeCloud ()<BCApiDelegate>

@end

@implementation EUExBeeCloud

- (void)initBeeCloud:(NSMutableArray *)inArguments {
    
    if ([inArguments isKindOfClass:[NSMutableArray class]] && [inArguments count] > 0) {
        SBJsonParser *jsonParser = [[SBJsonParser alloc] init];
        NSMutableDictionary *params = [jsonParser objectWithString:[inArguments lastObject]];
        
        if (params) {
            [BCPaySDK setBCApiDelegate:self];
            
            NSString *appId = [params objectForKey:@"bcAppId"];
//            NSString *appSecret = [params objectForKey:@"bcAppSecret"];
            NSString *wxAppID = [params objectForKey:@"wxAppId"];
            
            [BCPaySDK initWithAppID:appId andAppSecret:@""];
            [BCPaySDK initWeChatPay:wxAppID];
        }
    }
}

- (void)pay:(NSMutableArray *)inArguments {
    if ([inArguments isKindOfClass:[NSMutableArray class]] && inArguments.count > 0) {
        BCPayReq *payReq = [[BCPayReq alloc] init];
        
        SBJsonParser *jsonParser = [[SBJsonParser alloc] init];
        NSMutableDictionary *params = [jsonParser objectWithString:[inArguments lastObject]];
        
        payReq.channel = [BCPaySDK getChannelType:[params objectForKey:@"channel"]];
        payReq.title = [params objectForKey:@"title"];
        payReq.totalfee =[NSString stringWithFormat:@"%@",[params objectForKey:@"totalfee"]];
        payReq.billno = [params objectForKey:@"billno"];
        payReq.scheme = [params objectForKey:@"scheme"];
        payReq.viewController = [EUtility brwCtrl:meBrwView];
        payReq.optional = [params objectForKey:@"optional"];
        [BCPaySDK sendBCReq:payReq];
    }
}

- (void)getApiVersion:(NSMutableArray *)inArguments {
    NSDictionary *resp = @{@"apiVersion": kApiVersion};
    [self performSelectorOnMainThread:@selector(callbackJsonWithName:) withObject:@{@"cbGetApiVersion": resp} waitUntilDone:NO];
}

- (void)onBCPayResp:(id)resp {
    [self performSelectorOnMainThread:@selector(callbackJsonWithName:) withObject:@{@"cbPay":resp} waitUntilDone:NO];
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
    return [BCPaySDK handleOpenUrl:url];
}

@end
