/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Segment.io, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.segment.android;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import com.segment.android.internal.util.Logger;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;
import static android.net.ConnectivityManager.TYPE_BLUETOOTH;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static com.segment.android.internal.util.Utils.getSystemService;
import static com.segment.android.internal.util.Utils.hasPermission;

/**
 * Context is a dictionary of extra, free-form information about a specific API call. You can add
 * any custom data to the context dictionary that you'd like to have access to in the raw logs.
 * <p/>
 * Some keys in the context dictionary have semantic meaning and will be collected for you
 * automatically, depending on the library you send data from.Some keys need to be manually
 * entered,
 * such as IP Address, speed, etc.
 * <p/>
 * This is not persisted to disk, and is recomputed each time the app starts. If you add your own
 * attributes, you'll have to update it as well for each app start if you want it to persist
 * between
 * sessions.
 */
public class AnalyticsContext {
  public static class App {
    public String name;
    public String version;
    public String build;
    // Android Specific
    public String packageName;
    public String versionCode;
    public String versionName;

    App(Context context) {
      try {
        PackageInfo packageInfo =
            context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        name = packageInfo.applicationInfo.name;
        version = packageInfo.versionName;
        build = packageInfo.packageName + '@' + packageInfo.versionCode;
        packageName = packageInfo.packageName;
        versionCode = String.valueOf(packageInfo.versionCode);
        versionName = packageInfo.versionName;
      } catch (PackageManager.NameNotFoundException e) {
        Logger.e(e, "Could not retrieve package for name %s", context.getPackageName());
      }
    }
  }

  public static class Campaign {
    public String name;
    public String source;
    public String medium;
    public String term;
    public String content;

    Campaign(String name, String source, String medium, String term, String content) {
      this.name = name;
      this.source = source;
      this.medium = medium;
      this.term = term;
      this.content = content;
    }
  }

  public static class Device {
    public String id;
    public String manufacturer;
    public String model;
    public String name;
    public String type;
    public String brand;

    // Ignored for Android
    // String idfv;
    // String idfa;
    // String adTrackingEnabled;

    Device() {
      id = Build.ID;
      manufacturer = Build.MANUFACTURER;
      model = Build.MODEL;
      name = Build.VERSION.CODENAME;
      type = Build.TYPE;
      brand = Build.BRAND;
    }
  }

  public static class Library {
    public String name;
    public int version;
    // Android Specific
    public String versionName;
    public boolean debug;
    public String buildType;
    public String flavor;
    public boolean logging;

    public Library(Context context) {
      // This is the library info, not the app's
      name = "analytics-android";
      version = BuildConfig.VERSION_CODE;
      versionName = BuildConfig.VERSION_NAME;
      debug = BuildConfig.DEBUG;
      buildType = BuildConfig.BUILD_TYPE;
      flavor = BuildConfig.FLAVOR;
      logging = Logger.isLogging();
    }
  }

  public static class Location {
    public double latitude;
    public double longitude;
    public double speed;

    Location(double latitude, double longitude, double speed) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.speed = speed;
    }
  }

  public static class Network {
    public boolean bluetooth;
    public String carrier;
    public boolean cellular;
    public boolean wifi;

    Network(Context context) {
      if (hasPermission(context, ACCESS_NETWORK_STATE)) {
        ConnectivityManager connectivityManager = getSystemService(context, CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
          NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(TYPE_WIFI);
          wifi = wifiInfo != null && wifiInfo.isConnected();
          NetworkInfo bluetoothInfo = connectivityManager.getNetworkInfo(TYPE_BLUETOOTH);
          bluetooth = bluetoothInfo != null && bluetoothInfo.isConnected();
          NetworkInfo cellularInfo = connectivityManager.getNetworkInfo(TYPE_MOBILE);
          cellular = cellularInfo != null && cellularInfo.isConnected();
        }
      }

      TelephonyManager telephonyManager = getSystemService(context, TELEPHONY_SERVICE);
      carrier = telephonyManager != null ? telephonyManager.getNetworkOperatorName() : "unknown";
    }
  }

  public static class OS {
    public String name;
    public String version;

    // Android Specific
    public int sdk;

    OS() {
      name = Build.VERSION.CODENAME;
      version = Build.VERSION.RELEASE;
      sdk = Build.VERSION.SDK_INT;
    }
  }

  public static class Referrer {
    public String id;
    public String link;
    public String name;
    public String type;
    public String url;

    Referrer(String id, String link, String name, String type, String url) {
      this.id = id;
      this.link = link;
      this.name = name;
      this.type = type;
      this.url = url;
    }
  }

  public static class Screen {
    public float density;
    public int height;
    public int width;
    // Android Specific
    public int densityDpi;
    public String densityBucket;
    public float scaledDensity;

    Screen(Context context) {
      WindowManager manager = getSystemService(context, Context.WINDOW_SERVICE);
      Display display = manager.getDefaultDisplay();

      DisplayMetrics displayMetrics = new DisplayMetrics();
      display.getMetrics(displayMetrics);

      density = displayMetrics.density;
      height = displayMetrics.heightPixels;
      width = displayMetrics.widthPixels;
      densityDpi = displayMetrics.densityDpi;
      densityBucket = getDensityString(displayMetrics);
      scaledDensity = displayMetrics.scaledDensity;
    }

    private static String getDensityString(DisplayMetrics displayMetrics) {
      switch (displayMetrics.densityDpi) {
        case DisplayMetrics.DENSITY_LOW:
          return "ldpi";
        case DisplayMetrics.DENSITY_MEDIUM:
          return "mdpi";
        case DisplayMetrics.DENSITY_HIGH:
          return "hdpi";
        case DisplayMetrics.DENSITY_XHIGH:
          return "xhdpi";
        case DisplayMetrics.DENSITY_XXHIGH:
          return "xxhdpi";
        case DisplayMetrics.DENSITY_XXXHIGH:
          return "xxxhdpi";
        case DisplayMetrics.DENSITY_TV:
          return "tvdpi";
        default:
          return "unknown";
      }
    }
  }

  private App app;
  private Campaign campaign;
  private Device device;
  private String ip;
  private Library library;
  private String locale;
  private Location location;
  private Network network;
  private OS os;
  private Referrer referrer;
  private Screen screen;
  private Traits traits;
  private String groupId;
  private String userAgent;
  private Map<String, Object> other;

  private AnalyticsContext(Context context) {
    app = new App(context);
    device = new Device();
    library = new Library(context);
    locale = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
    network = new Network(context);
    os = new OS();
    screen = new Screen(context);
    traits = Traits.with(context);
    // todo: groupId
    userAgent = System.getProperty("http.agent");
    other = new HashMap<String, Object>();
  }

  static AnalyticsContext singleton = null;

  public static AnalyticsContext with(Context context) {
    if (singleton == null) {
      synchronized (AnalyticsContext.class) {
        if (singleton == null) {
          singleton = new AnalyticsContext(context);
        }
      }
    }
    return singleton;
  }

  public AnalyticsContext put(String key, Object value) {
    other.put(key, value);
    return this;
  }

  public AnalyticsContext putCampaign(String name, String source, String medium, String term,
      String content) {
    campaign = new Campaign(name, source, medium, term, content);
    return this;
  }

  public AnalyticsContext putIp(String ip) {
    this.ip = ip;
    return this;
  }

  public AnalyticsContext putLocation(double latitude, double longitude, double speed) {
    location = new Location(latitude, longitude, speed);
    return this;
  }

  public AnalyticsContext putReferrer(String id, String link, String name, String type,
      String url) {
    referrer = new Referrer(id, link, name, type, url);
    return this;
  }

  public App getApp() {
    return app;
  }

  public Campaign getCampaign() {
    return campaign;
  }

  public Device getDevice() {
    return device;
  }

  public String getIp() {
    return ip;
  }

  public Library getLibrary() {
    return library;
  }

  public String getLocale() {
    return locale;
  }

  public Location getLocation() {
    return location;
  }

  public Network getNetwork() {
    return network;
  }

  public OS getOs() {
    return os;
  }

  public Referrer getReferrer() {
    return referrer;
  }

  public Screen getScreen() {
    return screen;
  }

  public Traits getTraits() {
    return traits;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public Map<String, Object> getOther() {
    return other;
  }
}