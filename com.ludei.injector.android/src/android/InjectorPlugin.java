package com.ludei.injector.android;

import android.webkit.WebView;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.engine.SystemWebViewEngine;

import java.lang.reflect.Method;

public class InjectorPlugin extends CordovaPlugin {

    public static final String CANVASPLUS_CLASS = "com.ludei.canvasplus.CanvasPlusEngine";
    public static final String WEBVIEWPLUS_CLASS = "org.crosswalk.engine.XWalkWebViewEngine";
    public static final String SYSTEM_WEBVIEW_CLASS = "org.apache.cordova.engine.SystemWebViewEngine";

    @Override
    protected void pluginInitialize() {


        // Intercept URL loading to load the correct cordova.js files
        try {
            CordovaWebViewEngine engine = this.webView.getEngine();
            if (engine.getClass().getCanonicalName().equalsIgnoreCase(WEBVIEWPLUS_CLASS)) {
                Class clazz = Class.forName("com.ludei.webviewplus.android.CocoonXWalk");
                Method m = clazz.getMethod("setResourceClient", CordovaWebViewEngine.class);
                m.invoke(null, engine);

            } else if (engine.getClass().getCanonicalName().equalsIgnoreCase(SYSTEM_WEBVIEW_CLASS)) {
                WebView systemWebView = ((WebView) engine.getView());
                systemWebView.setWebViewClient(new CocoonSystemWebViewClient((SystemWebViewEngine)engine));
            }

            // Clean the webview cache before launching (Crosswalks doesn't do this internally and you never get the new content)
            engine.clearCache();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}