/********* DBRPlugin.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>

#import <DynamsoftBarcodeReader/DynamsoftBarcodeSDK.h>


@interface DBR : CDVPlugin {
  // Member variables go here.
  DynamsoftBarcodeReader *barcodeReader;
}
  - (void)decode:(CDVInvokedUrlCommand*)command;
@end

@implementation DBR

- (void)decode:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
        NSString* base64 = [command.arguments objectAtIndex:0];
        NSLog(@"%@", base64);
        NSArray<NSDictionary*> *array = [self decodeBase64: base64];
        CDVPluginResult* result = [CDVPluginResult
                                       resultWithStatus: CDVCommandStatus_OK
                                       messageAsMultipart: array
                                       ];
            
        [[self commandDelegate] sendPluginResult:result callbackId:command.callbackId];
    }];
    
}

- (void)configurationDBR{
    iDMDLSConnectionParameters* dls = [[iDMDLSConnectionParameters alloc] init];
    // Initialize license for Dynamsoft Barcode Reader.
    // The organization id 200001 here will grant you a public trial license good for 7 days. Note that network connection is required for this license to work.
    // If you want to use an offline license, please contact Dynamsoft Support: https://www.dynamsoft.com/company/contact/
    // You can also request a 30-day trial license in the customer portal: https://www.dynamsoft.com/customer/license/trialLicense?product=dbr&utm_source=installer&package=ios
    dls.organizationID = @"200001";
    barcodeReader = [[DynamsoftBarcodeReader alloc] initLicenseFromDLS:dls verificationDelegate:self];
    NSLog(@"%@", barcodeReader.getVersion);
    
}

- (NSArray<NSDictionary*>*)decodeBase64: (NSString*) base64 {
    if (barcodeReader ==NULL){
        [self configurationDBR];
    }
    NSError __autoreleasing * _Nullable error;
    NSArray<iTextResult*>* results = [barcodeReader decodeBase64:base64 withTemplate:@"" error:&error];
    NSLog(@"%lu", (unsigned long)results.count);
    //NSMutableArray<NSDictionary*> *resultsArray =  [[NSMutableArray alloc] init];;
    NSMutableArray<NSDictionary*> * resultsArray = [[ NSMutableArray alloc] init];
    for (iTextResult* result in results) {
        CGPoint p1 = [result.localizationResult.resultPoints[0] CGPointValue];
        CGPoint p2 = [result.localizationResult.resultPoints[1] CGPointValue];
        CGPoint p3 = [result.localizationResult.resultPoints[2] CGPointValue];
        CGPoint p4 = [result.localizationResult.resultPoints[3] CGPointValue];
        

        NSDictionary *dictionary = @{
               @"barcodeText" : result.barcodeText,
               @"barcodeFormat" : result.barcodeFormatString,
               @"x1" : @(p1.x),
               @"y1" : @(p1.y),
               @"x2" : @(p2.x),
               @"y2" : @(p2.y),
               @"x3" : @(p3.x),
               @"y3" : @(p3.y),
               @"x4" : @(p4.x),
               @"y4" : @(p4.y)
        };
        NSLog(@"%@", @(p1.x));
        NSLog(@"%@", result.barcodeText);
        [resultsArray addObject:(dictionary)];
    }
    NSArray<NSDictionary *> *array = [resultsArray copy];
    return array;
}

@end
