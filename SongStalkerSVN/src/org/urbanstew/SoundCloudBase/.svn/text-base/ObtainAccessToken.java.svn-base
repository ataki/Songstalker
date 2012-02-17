package org.urbanstew.SoundCloudBase;

import java.util.concurrent.Semaphore;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * ObtainAccessToken activity is used to execute the oauth authorization
 * protocol.
 * 
 * @author Stjepan Rajko
 */
public class ObtainAccessToken extends SoundCloudBaseActivity implements
		SoundCloudAuthorizationClient {
	/**
	 * Called when the Activity is created.
	 * <p>
	 * Initializes the SoundCloudRequest object and the user interface, and then
	 * tries to obtain the request token from SoundCloud.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.obtain_access_token);

		// JH: OAuth 2 won't need a web view
		/*
		 * mWebView = (WebView) findViewById(R.id.webview);
		 * mWebView.getSettings().setJavaScriptEnabled(true);
		 * mWebView.getSettings().setBlockNetworkImage(false);
		 * mWebView.getSettings().setLoadsImagesAutomatically(true);
		 * 
		 * if(Integer.parseInt(android.os.Build.VERSION.SDK) >= 8) new
		 * setSettingsForSDK8(mWebView); mWebView.setWebViewClient( new
		 * WebViewClient() { public boolean shouldOverrideUrlLoading(WebView
		 * view, String url) {
		 * if(url.startsWith("http://soundcloud.urbanstew.org/")) {
		 * mVerificationCode =
		 * Uri.parse(url).getQueryParameter("oauth_verifier");
		 * Log.d(ObtainAccessToken.class.getName(), "Verification code is: " +
		 * mVerificationCode); mVerificationCodeAvailable.release(); return
		 * true; } return false; } });
		 * 
		 * Toast.makeText(ObtainAccessToken.this,
		 * "Please select \"Allow access\" when the SoundCloud authorization page loads."
		 * , Toast.LENGTH_LONG).show();
		 */

		((Button) findViewById(R.id.login_button))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						String username = ((EditText) findViewById(R.id.user_box))
								.getText().toString();
						String password = ((EditText) findViewById(R.id.pass_box))
								.getText().toString();

						// JH: set callback to Stjepan's Oauth 2 login method
						getSCApplicationBase().authorize(
								ObtainAccessToken.this, username, password);
					}
				});

		// mVerificationCodeAvailable = new Semaphore(0);
		// getSCApplicationBase().authorizeWithoutCallback(this);
	}

	public void onDestroy() {
		super.onDestroy();
		// if the authorization is complete, canceling doesn't hurt
		getSCApplicationBase().cancel(this);
	}

	public void authorizationCompleted(final AuthorizationStatus status) {
		if (status == AuthorizationStatus.CANCELED)
			return;
		runOnUiThread(new Runnable() {
			public void run() {
				if (status == AuthorizationStatus.SUCCESSFUL) {
					Toast.makeText(ObtainAccessToken.this,
							"Authorization successful.", Toast.LENGTH_LONG)
							.show();
					finish();
				} else {
					String message = "";
					if (mAuthorizationException != null) {
						message += "Authorization failed with exception "
								+ mAuthorizationException.getClass().getName();
						// if(mAuthorizationException.getCause() != null)
						// message += " (" + mAuthorizationException..getCause()
						// + ")";
						message += ".";
						if (mAuthorizationException.getLocalizedMessage() != null)
							message += "\n\n"
									+ mAuthorizationException
											.getLocalizedMessage() + ".";
					}
					new AlertDialog.Builder(ObtainAccessToken.this)
							.setTitle("Authorization Failed")
							.setMessage(message)
							.setCancelable(false)
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											finish();
										}
									}).create().show();
				}
			}
		});
	}

	public void openAuthorizationURL(String url) {
		mWebView.loadUrl(url);
	}

	public String getVerificationCode() {
		try {
			mVerificationCodeAvailable.acquire();
		} catch (InterruptedException e) {
			Log.v(ObtainAccessToken.class.getSimpleName(),
					Log.getStackTraceString(e));
			return null;
		}
		return mVerificationCode;
	}

	public void exceptionOccurred(Exception e) {
		Log.v(ObtainAccessToken.class.getSimpleName(),
				Log.getStackTraceString(e));
		mAuthorizationException = e;
	}

	// WebView used to open the SoundCloud authorization page
	WebView mWebView;

	// Button used by the user to indicate she has authorized SoundDroid
	Button mAuthorizedButton;

	Semaphore mVerificationCodeAvailable;
	String mVerificationCode;
	Exception mAuthorizationException = null;
}

class setSettingsForSDK8 {
	setSettingsForSDK8(WebView mWebView) {
		// disabled because it requires Android 2.2, and building with 2.2
		// and an ImageButton seems to cause errors (see Issue 59),
		// and I'm not sure whether it has any benefit anyway
		// mWebView.getSettings().setBlockNetworkLoads(false);
	}
}
