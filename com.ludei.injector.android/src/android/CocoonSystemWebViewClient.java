package com.ludei.injector.android;

import android.net.Uri;
import android.webkit.URLUtil;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.LOG;
import org.apache.cordova.engine.SystemWebViewClient;
import org.apache.cordova.engine.SystemWebViewEngine;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by imanolmartin on 11/03/15.
 * This class overrides the default System Webview Client to intercept the cordova.js requests.
 * This should be kept up to date as cordova-android is updated.
 */
public class CocoonSystemWebViewClient extends SystemWebViewClient {

    private static final String TAG = CocoonSystemWebViewClient.class.getCanonicalName();

    public CocoonSystemWebViewClient(SystemWebViewEngine parentEngine) {
        super(parentEngine);
    }


    /**
     * Our code here is the cordova interception section. The rest of the code has been copied
     * from the cordova-android SystemWebViewClient class.
     */
    @Deprecated
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        try {
            CordovaResourceApi resourceApi = parentEngine.getCordovaWebView().getResourceApi();
            Uri origUri = Uri.parse(url);
            // Allow plugins to intercept WebView requests.
            Uri remappedUri = resourceApi.remapUri(origUri);

            if (!URLUtil.isNetworkUrl(remappedUri.toString()))
                return super.shouldInterceptRequest(view, url);

            // We always try to serve cordova.js from local so we always have the correct version
            if (remappedUri.toString().endsWith("cordova.js")) {
                CordovaResourceApi.OpenForReadResult result = resourceApi.openForRead(Uri.parse("file:///android_asset/www/cordova.js"), true);
                return new WebResourceResponse(result.mimeType, "UTF-8", result.inputStream);

            } else if (remappedUri.toString().endsWith("cordova_plugins.js")) {
                CordovaResourceApi.OpenForReadResult result = resourceApi.openForRead(Uri.parse("file:///android_asset/www/cordova_plugins.js"), true);
                return new WebResourceResponse(result.mimeType, "UTF-8", result.inputStream);

            } else if (remappedUri.toString().contains("plugins/")) {
                int index = remappedUri.toString().indexOf("plugins/");
                CordovaResourceApi.OpenForReadResult result = resourceApi.openForRead(Uri.parse("file:///android_asset/www/" + remappedUri.toString().substring(index)), true);
                return new WebResourceResponse(result.mimeType, "UTF-8", result.inputStream);
            }

        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) {
                LOG.e(TAG, "Error occurred while loading a file (returning a 404).", e);
            }

            return super.shouldInterceptRequest(view, url);
        }

        return super.shouldInterceptRequest(view, url);
    }
}
