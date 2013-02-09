
package com.example.mytwitter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.example.mytwitter.TwitterOAuthFragment.TwitterOAuthListener;
import com.example.mytwitter.util.TwitterUtils;

public class MainActivity extends FragmentActivity implements TwitterOAuthListener {

    private static final String TAG_TWITTER_OAUTH = "tag_twitter_oauth";
    private static final String TAG_TIMELINE = "tag_timeline";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!TwitterUtils.hasAccessToken(this)) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new TwitterOAuthFragment(), TAG_TWITTER_OAUTH)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new TimelineFragment(), TAG_TIMELINE)
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        TwitterOAuthFragment f = (TwitterOAuthFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_TWITTER_OAUTH);
        if (f != null) {
            f.onNewIntent(intent);
        }
    }

    @Override
    public void onTwitterOAuthSuccess() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new TimelineFragment(), TAG_TIMELINE)
                .commit();
    }
}
