package app.cooka.cookapp.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.support.v4.content.ContextCompat.getSystemService;

public class SystemUtils {

    /**
     * Gets a Android Studio instance specific key hash that during development is required for social sign in services such as Facebook
     * @param context
     * @return the Android Studio instance specific key hash to be registered with the social sign in service for developmental access; returns null if an error occurred
     */
    public static String getSystemKeyHash(Activity context) {

        PackageInfo packageInfo;
        String key = null;
        try {
            String packageName = context.getApplicationContext().getPackageName();
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);

            for(Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0)).trim();
            }
        }
        catch(PackageManager.NameNotFoundException e) {
            Log.e("Name not found", e.toString());
        }
        catch(NoSuchAlgorithmException e) {
            Log.e("No such algorithm", e.toString());
        }
        catch(Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }

    /**
     * Gets a Android device specific unique identifier
     * @param contentResolver
     * @return the Android ID; null if an error occurred
     */
    public static String getAndroidId(ContentResolver contentResolver) {

        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
    }

}
