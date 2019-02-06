//
//  CanvasPlusEngine.mm
//
//  Created by Imanol Fernandez @Ludei
//

#import "CocoonInjector.h"

#pragma mark Custom URL Protocol

@implementation CocoonInjector

+ (BOOL)canInitWithRequest:(NSURLRequest *)request {
    
    //resolve cordova.js dependencies
    NSString * absolute = request.URL.absoluteString;
    if ([absolute.lowercaseString hasSuffix:@"cordova.js"] || [absolute.lowercaseString hasSuffix:@"cordova_plugins.js"]) {
        return YES;
    }
    
    //resolve plugin dependencies
    NSRange range = [absolute rangeOfString:@"/plugins/"];
    if (range.location == NSNotFound) {
        return NO;
    }
    
    NSString * path =[absolute substringFromIndex:range.location];
    NSString * fullPath = [NSString stringWithFormat:@"%@/www%@", [NSBundle mainBundle].resourcePath, path];
    
    BOOL exists = [[NSFileManager defaultManager] fileExistsAtPath:fullPath];
    return exists;
    
}

+ (NSURLRequest*)canonicalRequestForRequest:(NSURLRequest*)theRequest
{
    return theRequest;
}

- (void)startLoading
{
    NSData * data;
    NSString * absolute = self.request.URL.absoluteString;
    if ([absolute.lowercaseString hasSuffix:@"cordova.js"] || [absolute.lowercaseString hasSuffix:@"cordova_plugins.js"]) {
        //cordova dependencies
        NSString * resourceName = self.request.URL.lastPathComponent.stringByDeletingPathExtension;
        NSString * extension = self.request.URL.lastPathComponent.pathExtension;
        NSString * cordovaJSPath = [[NSBundle mainBundle] pathForResource:resourceName ofType:extension inDirectory:@"www"];
        data = [NSData dataWithContentsOfFile:cordovaJSPath];
    }
    else {
        //plugin dependencies
        NSRange range = [absolute rangeOfString:@"/plugins/"];
        NSString * path =[absolute substringFromIndex:range.location];
        NSString * fullPath = [NSString stringWithFormat:@"%@/www%@", [NSBundle mainBundle].resourcePath, path];
        data = [NSData dataWithContentsOfFile:fullPath];
    }
    
    NSDictionary * headers = @{
                               @"Content-Length": [NSString stringWithFormat:@"%lu",(unsigned long)[data length]],
                               @"Content-Type": @"text/javascript"
                               };
    

    
    NSHTTPURLResponse * response = [[NSHTTPURLResponse alloc] initWithURL:[self.request URL] statusCode:200 HTTPVersion:@"1.1" headerFields:headers];
    
    [[self client] URLProtocol:self didReceiveResponse:response cacheStoragePolicy:NSURLCacheStorageNotAllowed];
    [[self client] URLProtocol:self didLoadData:data];
    [[self client] URLProtocolDidFinishLoading:self];
}

- (void)stopLoading
{
    
}

@end
