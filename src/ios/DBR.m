/********* DBRPlugin.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>

#import <DynamsoftBarcodeReader/DynamsoftBarcodeSDK.h>


@interface DBR : CDVPlugin {}
  // Member variables go here.
@property (nonatomic, retain) DynamsoftBarcodeReader* barcodeReader;
@property Boolean initialized;
@property Boolean decoding;
- (void)init:(CDVInvokedUrlCommand*)command;
- (void)initWithOrganizationID:(CDVInvokedUrlCommand*)command;
- (void)decode:(CDVInvokedUrlCommand*)command;
- (void)initRuntimeSettingsWithString:(CDVInvokedUrlCommand*)command;
- (void)outputSettingsToString:(CDVInvokedUrlCommand*)command;
@end

@implementation DBR

- (void)init:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
        NSString* license = [command.arguments objectAtIndex:0];
        [self initDBR: license];
        CDVPluginResult* result = [CDVPluginResult
                                       resultWithStatus: CDVCommandStatus_OK
                                   messageAsString: self->_barcodeReader.getVersion
                                       ];
            
        [[self commandDelegate] sendPluginResult:result callbackId:command.callbackId];
    }];
    
}

- (void)initWithOrganizationID:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
        NSString* organizationID = [command.arguments objectAtIndex:0];
        [self initDBRWithOrganizationID: organizationID];
        CDVPluginResult* result = [CDVPluginResult
                                       resultWithStatus: CDVCommandStatus_OK
                                   messageAsString: self->_barcodeReader.getVersion
                                       ];
            
        [[self commandDelegate] sendPluginResult:result callbackId:command.callbackId];
    }];
    
}

- (void)decode:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
        NSString* base64 = [command.arguments objectAtIndex:0];
        NSArray<NSDictionary*> *array = [self decodeBase64: base64];
        CDVPluginResult* result = [CDVPluginResult
                                       resultWithStatus: CDVCommandStatus_OK
                                       messageAsArray: array
                                       ];
            
        [[self commandDelegate] sendPluginResult:result callbackId:command.callbackId];
    }];
    
}

- (void)initRuntimeSettingsWithString:(CDVInvokedUrlCommand*)command
{

    NSString* template = [command.arguments objectAtIndex:0];
    NSError __autoreleasing * _Nullable error;
    [_barcodeReader initRuntimeSettingsWithString:template conflictMode:EnumConflictModeOverwrite error:&error];
    CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus: CDVCommandStatus_OK
                                   messageAsBool: true
                                   ];
        
    [[self commandDelegate] sendPluginResult:result callbackId:command.callbackId];
}

- (void)outputSettingsToString:(CDVInvokedUrlCommand*)command
{

    NSError __autoreleasing * _Nullable error;
    NSString * settings = [_barcodeReader outputSettingsToString:@"currentRuntimeSettings" error:&error];
    CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus: CDVCommandStatus_OK
                                   messageAsString: settings
                                   ];
        
    [[self commandDelegate] sendPluginResult:result callbackId:command.callbackId];
}

- (void)initDBR: (NSString*) license{
    if (_initialized==false){
        NSLog(@"%s", "Initializing");
        _barcodeReader = [[DynamsoftBarcodeReader alloc] initWithLicense:license];
        _initialized = true;
    }else{
        NSLog(@"%s", "Already initialized.");
    }
}

- (void)initDBRWithOrganizationID: (NSString*) organizationID{
    if (_initialized==false){
        NSLog(@"%s", "Initializing");
        iDMDLSConnectionParameters* dls = [[iDMDLSConnectionParameters alloc] init];
        // Initialize license for Dynamsoft Barcode Reader.
        // The organization id 200001 here will grant you a public trial license good for 7 days. Note that network connection is required for this license to work.
        // If you want to use an offline license, please contact Dynamsoft Support: https://www.dynamsoft.com/company/contact/
        // You can also request a 30-day trial license in the customer portal: https://www.dynamsoft.com/customer/license/trialLicense?product=dbr&utm_source=installer&package=ios
        dls.organizationID = organizationID;
        _barcodeReader = [[DynamsoftBarcodeReader alloc] initLicenseFromDLS:dls verificationDelegate:self];
        _initialized = true;
    }else{
        NSLog(@"%s", "Already initialized.");
    }
}

- (NSArray<NSDictionary*>*)decodeBase64: (NSString*) base64 {
    NSMutableArray<NSDictionary*> * resultsArray = [[ NSMutableArray alloc] init];
    if (_initialized==true && _decoding==false){
        @try {
            NSLog(@"Decoding...");
            _decoding=true;
            NSError __autoreleasing * _Nullable error;
            NSArray<iTextResult*>* results = [_barcodeReader decodeBase64:base64 withTemplate:@"" error:&error];
            _decoding=false;
            //NSMutableArray<NSDictionary*> *resultsArray =  [[NSMutableArray alloc] init];;
            
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
                //NSLog(@"%@", @(p1.x));
                //NSLog(@"%@", result.barcodeText);
                [resultsArray addObject:(dictionary)];
                
            }
        }
        @catch (NSException *exception) {
            NSLog(@"Exception:%@",exception);
        }
        @finally{
            NSLog(@"Skip");
        }
    }
    NSArray<NSDictionary *> *array = [resultsArray copy];
    return array;
}

@end
