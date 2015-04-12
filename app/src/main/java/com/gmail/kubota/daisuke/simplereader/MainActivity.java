package com.gmail.kubota.daisuke.simplereader;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gmail.kubota.daisuke.simplereader.model.RssObject;
import com.gmail.kubota.daisuke.simplereader.view.adapter.MainAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private static final String SAVED_INSTANCE_RSS_ARRAY = "RSS_ARRAY";

    RequestQueue mQueue;

    MainAdapter mAdapter;

    ArrayList<RssObject> mList = new ArrayList<>();

    SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_INSTANCE_RSS_ARRAY)) {
            mList = (ArrayList<RssObject>) savedInstanceState.getSerializable(SAVED_INSTANCE_RSS_ARRAY);
        }

        setContentView(R.layout.activity_main);
        mQueue = Volley.newRequestQueue(this);
        mAdapter = new MainAdapter(this, R.layout.adapter_reader_row, mList);

        Button button = (Button) findViewById(R.id.main_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRss();
            }
        });

        View emptyView = findViewById(R.id.main_empty_layout);
        ListView listView = (ListView) findViewById(R.id.main_list_view);
        listView.setAdapter(mAdapter);
        listView.setEmptyView(emptyView);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RssObject rss = mList.get(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra(DetailActivity.BUNDLE_RSS_OBJECT, rss);
                startActivity(intent);
            }
        });

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        // ローティング色
        mRefreshLayout.setColorSchemeResources(
                R.color.common_red,
                R.color.common_green,
                R.color.common_blue,
                R.color.common_purple);
        // 背景色
        mRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.common_gray);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestRss();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_INSTANCE_RSS_ARRAY, mList);
    }

    private void requestRss() {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject responseData = response.getJSONObject("responseData");
                    JSONObject feed = responseData.getJSONObject("feed");
                    JSONArray entries = feed.getJSONArray("entries");
                    ArrayList<RssObject> list = new ArrayList<>();
                    for (int i = 0; i < entries.length(); i++) {
                        JSONObject object = entries.getJSONObject(i);
                        RssObject rss = RssObject.getInstance(object);
                        if (rss != null) {
                            list.add(rss);
                        }
                    }
                    if (list.size() > 0) {
                        mList.clear();
                        mList.addAll(list);
                        mAdapter.notifyDataSetChanged();
                    }
                } catch (Exception ignore) {
                    Log.e("rss", "requestRss()", ignore);
                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.main_response_parse_error),
                            Toast.LENGTH_LONG).show();
                }
                // 更新が終了したらインジケータ非表示
                mRefreshLayout.setRefreshing(false);
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.main_response_error),
                        Toast.LENGTH_LONG);
                toast.show();
                // 更新が終了したらインジケータ非表示
                mRefreshLayout.setRefreshing(false);
            }
        };
        JsonObjectRequest request = new JsonObjectRequest(
                "https://ajax.googleapis.com/ajax/services/feed/load?v=1.0&q=http://nanapi.jp/feed&num=20",
                null,
                listener,
                errorListener);
        mQueue.add(request);
    }

}
