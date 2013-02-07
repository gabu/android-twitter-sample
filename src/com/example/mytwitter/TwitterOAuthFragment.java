
package com.example.mytwitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mytwitter.util.TwitterUtils;

public class TwitterOAuthFragment extends Fragment {

    public interface TwitterOAuthListener {
        void onTwitterOAuthSuccess();
    }

    private TwitterOAuthListener mTwitterOAuthListener;

    private String mCallbackURL;
    private Twitter mTwitter;
    private RequestToken mRequestToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCallbackURL = getString(R.string.twitter_callback_url);
        mTwitter = TwitterUtils.getTwitterInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_twitter_oauth, container, false);
        view.findViewById(R.id.button_twitter_oauth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAuthorize();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof TwitterOAuthListener) {
            mTwitterOAuthListener = (TwitterOAuthListener) activity;
        } else {
            throw new ClassCastException(
                    "TwitterOAuthFragmentを扱うActivityではTwitterOAuthListenerインタフェースを実装してください。");
        }
    }

    /**
     * OAuth認証（厳密には認可）を開始します。
     * 
     * @param listener
     */
    private void startAuthorize() {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    mRequestToken = mTwitter.getOAuthRequestToken(mCallbackURL);
                    return mRequestToken.getAuthorizationURL();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String url) {
                if (url != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } else {
                    // 失敗。。。
                }
            }
        };
        task.execute();
    }

    /**
     * Fragmentには自動的に呼び出されるonNewIntent()はないのでActivityから明示的に呼び出してもらう必要があります。
     * 
     * @param intent
     */
    public void onNewIntent(Intent intent) {
        if (intent == null
                || intent.getData() == null
                || !intent.getData().toString().startsWith(mCallbackURL)) {
            return;
        }
        String verifier = intent.getData().getQueryParameter("oauth_verifier");

        AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(String... params) {
                try {
                    return mTwitter.getOAuthAccessToken(mRequestToken, params[0]);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AccessToken accessToken) {
                if (accessToken != null) {
                    // 認証成功！
                    TwitterUtils.storeAccessToken(getActivity(), accessToken);
                    Toast.makeText(getActivity(), "認証成功！", Toast.LENGTH_LONG).show();
                    mTwitterOAuthListener.onTwitterOAuthSuccess();
                } else {
                    // 認証失敗。。。
                    Toast.makeText(getActivity(), "認証失敗。。。", Toast.LENGTH_LONG).show();
                }
            }
        };
        task.execute(verifier);
    }
}
