
package com.example.mytwitter;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.mytwitter.util.TwitterUtils;

public class TimelineFragment extends ListFragment {

    private TweetAdapter mAdapter;
    private Twitter mTwitter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new TweetAdapter(getActivity());
        setListAdapter(mAdapter);

        mTwitter = TwitterUtils.getTwitterInstance(getActivity());
        reloadTimeLine();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_timeline, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reload:
                reloadTimeLine();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class TweetAdapter extends ArrayAdapter<String> {

        public TweetAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);
        }
    }

    public void reloadTimeLine() {
        AsyncTask<Void, Void, List<Status>> task = new AsyncTask<Void, Void, List<Status>>() {
            @Override
            protected void onPreExecute() {
                setListShown(false);
            }

            @Override
            protected List<twitter4j.Status> doInBackground(Void... params) {
                try {
                    return mTwitter.getHomeTimeline();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<twitter4j.Status> result) {
                if (result != null) {
                    ArrayList<String> tweets = new ArrayList<String>();
                    for (twitter4j.Status status : result) {
                        tweets.add(status.getText());
                    }
                    mAdapter.clear();
                    mAdapter.addAll(tweets);
                } else {
                    Toast.makeText(getActivity(), "タイムラインの取得に失敗しました。。。", Toast.LENGTH_LONG).show();
                }
                setListShown(true);
            }
        };
        task.execute();
    }
}
