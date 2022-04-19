/********* DBRPlugin.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>

#import <DynamsoftBarcodeReader/DynamsoftBarcodeReader.h>
#import <DynamsoftCameraEnhancer/DynamsoftCameraEnhancer.h>

CGFloat degreesToRadians(CGFloat degrees) {return degrees * M_PI / 180;};

@interface DBR: CDVPlugin<DBRLicenseVerificationListener, DCELicenseVerificationListener, DCEFrameListener>
  // Member variables go here.
@property (nonatomic, retain) DynamsoftBarcodeReader* barcodeReader;
@property (nonatomic, strong) DynamsoftCameraEnhancer *dce;
@property (nonatomic, strong) DCECameraView *dceView;
@property Boolean decoding;
@property NSString* scanCallbackId;
- (void)init:(CDVInvokedUrlCommand*)command;
- (void)decode:(CDVInvokedUrlCommand*)command;
- (void)destroy:(CDVInvokedUrlCommand*)command;
- (void)initRuntimeSettingsWithString:(CDVInvokedUrlCommand*)command;
- (void)outputSettingsToString:(CDVInvokedUrlCommand*)command;
- (void)startScanning:(CDVInvokedUrlCommand*)command;
- (void)stopScanning:(CDVInvokedUrlCommand*)command;
- (void)pauseScanning:(CDVInvokedUrlCommand*)command;
- (void)resumeScanning:(CDVInvokedUrlCommand*)command;
- (void)switchTorch:(CDVInvokedUrlCommand*)command;
- (void)getResolution:(CDVInvokedUrlCommand*)command;
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

- (void)destroy:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* result;
    if (_barcodeReader != nil) {
        [_barcodeReader dispose];
        result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    }else{
        result = [CDVPluginResult
                       resultWithStatus: CDVCommandStatus_ERROR
                       messageAsString: @"not initialized"
                       ];
    }
    [[self commandDelegate] sendPluginResult:result callbackId:command.callbackId];
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
    if (_barcodeReader == nil){
        NSLog(@"%s", "Initializing dbr");
        [DynamsoftBarcodeReader initLicense:license verificationDelegate:self];
        _barcodeReader = [[DynamsoftBarcodeReader alloc] init];
    }else{
        NSLog(@"%s", "Already initialized.");
    }
}


- (NSArray<NSDictionary*>*)decodeBase64: (NSString*) base64 {
    if (_barcodeReader != nil && _decoding==false){
        @try {
            NSLog(@"Decoding...");
            _decoding=true;
            NSError __autoreleasing * _Nullable error;
            NSArray<iTextResult*>* results = [_barcodeReader decodeBase64:base64 error:&error];
            _decoding=false;
            NSArray<NSDictionary*> * resultsArray = [self wrapResults:results];
            return resultsArray;
        }
        @catch (NSException *exception) {
            NSLog(@"Exception:%@",exception);
        }
        @finally{
            NSLog(@"Skip");
        }
    }
    NSArray<NSDictionary*> * resultsArray = [[ NSArray alloc] init];
    return resultsArray;
}

- (NSArray<NSDictionary*>*) wrapResults: (NSArray<iTextResult*>*) results {
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
        //NSLog(@"%@", @(p1.x));
        //NSLog(@"%@", result.barcodeText);
        [resultsArray addObject:(dictionary)];
    }
    NSArray<NSDictionary *> *array = [resultsArray copy];
    return array;
}

- (void)startScanning:(CDVInvokedUrlCommand*)command
{
    NSLog(@"%s", "start scanning");
    _scanCallbackId = command.callbackId;
    [self makeWebViewTransparent];
    NSString* license = [command.arguments objectAtIndex:0];
    if (self.dce == nil){
        [self initDCEAndStart:license];
    }else{
        [self.dce open];
    }
}

- (void)stopScanning:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* result;
    if (_dce != nil) {
        [_dce close];
        [self restoreWebViewBackground];
        result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    }else{
        result = [CDVPluginResult
                       resultWithStatus: CDVCommandStatus_ERROR
                       messageAsString: @"not started"
                       ];
    }
    [[self commandDelegate] sendPluginResult:result callbackId:command.callbackId];
}

- (void)pauseScanning:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* result;
    if (_dce != nil) {
        [_dce pause];
        result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    }else{
        result = [CDVPluginResult
                       resultWithStatus: CDVCommandStatus_ERROR
                       messageAsString: @"not started"
                       ];
    }
    [[self commandDelegate] sendPluginResult:result callbackId:command.callbackId];
}

- (void)resumeScanning:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* result;
    if (_dce != nil) {
        [_dce resume];
        result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    }else{
        result = [CDVPluginResult
                       resultWithStatus: CDVCommandStatus_ERROR
                       messageAsString: @"not started"
                       ];
    }
    [[self commandDelegate] sendPluginResult:result callbackId:command.callbackId];
}

- (void)switchTorch:(CDVInvokedUrlCommand *)command
{
    NSString* desiredStatus = [command.arguments objectAtIndex:0];
    CDVPluginResult* result;
    if (_dce != nil) {
        if ([desiredStatus isEqualToString:@"on"]){
            [_dce turnOnTorch];
        }else{
            [_dce turnOffTorch];
        }
        result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK];
    }else{
        result = [CDVPluginResult
                       resultWithStatus: CDVCommandStatus_ERROR
                       messageAsString: @"not started"
                       ];
    }
    [[self commandDelegate] sendPluginResult:result callbackId:command.callbackId];
}

- (void)getResolution:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult* result;
    if (_dce != nil) {
        NSString* resolution = [_dce getResolution];
        result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsString: resolution];
    }else{
        result = [CDVPluginResult
                       resultWithStatus: CDVCommandStatus_ERROR
                       messageAsString: @"not started"
                       ];
    }
    [[self commandDelegate] sendPluginResult:result callbackId:command.callbackId];
}


- (void)initDCEAndStart: (NSString*) license {
    NSLog(@"init dce");
    [DynamsoftCameraEnhancer initLicense:license verificationDelegate:self];
    _dceView = [DCECameraView cameraWithFrame:self.webView.superview.bounds];
    [self.webView.superview addSubview:_dceView];
    [self.webView.superview bringSubviewToFront:self.webView];
    _dce = [[DynamsoftCameraEnhancer alloc] initWithView:_dceView];
    [_dce setResolution:EnumRESOLUTION_720P];
    [_dce addListener:self];
    [_dce open];
}

- (void)frameOutPutCallback:(nonnull DCEFrame *)frame timeStamp:(NSTimeInterval)timeStamp {
    NSError __autoreleasing * _Nullable error;
    UIImage *image = [frame toUIImage];
    UIImage *rotatedImage = [self imageRotatedByDegrees:frame.orientation image:image];

    NSArray<iTextResult*>* results = [_barcodeReader decodeImage:rotatedImage error:&error];
    NSArray<NSDictionary*> * resultsArray = [self wrapResults:results];
    
    NSDictionary *dictionary = @{
           @"results" : resultsArray,
        @"frameWidth" : @(rotatedImage.size.width),
        @"frameHeight" : @(rotatedImage.size.height)
    };
    
    //NSData * JSONData = [NSJSONSerialization dataWithJSONObject:dictionary
    //                                                    options:kNilOptions
    //                                                      error:&error];
    //NSString *baseString = [[NSString alloc]initWithData:JSONData encoding:NSUTF8StringEncoding];

    //NSLog(@"base string: %@", baseString);
    
    CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus: CDVCommandStatus_OK
                                   messageAsDictionary: dictionary
                                   ];
    [result setKeepCallbackAsBool:YES];
        
    [[self commandDelegate] sendPluginResult:result callbackId:_scanCallbackId];
}


- (UIImage *)imageRotatedByDegrees:(CGFloat)degrees image: (UIImage*) image {
    __block UIImage *rotated;
    dispatch_sync(dispatch_get_main_queue(), ^{
        UIView *rotatedViewBox = [[UIView alloc] initWithFrame:CGRectMake(0,0,image.size.width, image.size.height)];
        CGAffineTransform t = CGAffineTransformMakeRotation(degreesToRadians(degrees));
        rotatedViewBox.transform = t;
        CGSize rotatedSize = rotatedViewBox.frame.size;
        // Create the bitmap context
        UIGraphicsBeginImageContext(rotatedSize);
        CGContextRef bitmap = UIGraphicsGetCurrentContext();
        // Move the origin to the middle of the image so we will rotate and scale around the center.
        CGContextTranslateCTM(bitmap, rotatedSize.width/2, rotatedSize.height/2);
        // Rotate the image context
        CGContextRotateCTM(bitmap, degreesToRadians(degrees));
        // Now, draw the rotated/scaled image into the context
        CGContextScaleCTM(bitmap, 1.0, -1.0);
        CGContextDrawImage(bitmap, CGRectMake(-image.size.width / 2, -image.size.height / 2, image.size.width, image.size.height), [image CGImage]);
        UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        rotated = newImage;
    });
    return rotated;
}

- (void)DCELicenseVerificationCallback:(bool)isSuccess error:(NSError *)error{

}

- (void)DBRLicenseVerificationCallback:(bool)isSuccess error:(NSError * _Nullable)error {

}


- (void) makeWebViewTransparent {
    [self.webView setOpaque:false];
    [self.webView setBackgroundColor:UIColor.clearColor];
    [self.webView.scrollView setBackgroundColor:UIColor.clearColor];
}

- (void)restoreWebViewBackground {
    [self.webView setOpaque:true];
    [self.webView setBackgroundColor:UIColor.whiteColor];
    [self.webView.scrollView setBackgroundColor:UIColor.whiteColor];
}

@end
