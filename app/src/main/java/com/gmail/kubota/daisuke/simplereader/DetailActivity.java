package com.gmail.kubota.daisuke.simplereader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.gmail.kubota.daisuke.simplereader.model.RssObject;
import com.gmail.kubota.daisuke.simplereader.network.BitmapCache;

/**
 *
 * Created by daisuke on 15/03/15.
 */
public class DetailActivity extends ActionBarActivity {

    public static final String BUNDLE_RSS_OBJECT = "RSS_OBJECT";

    public static final Object VOLLEY_TAG = new Object();

    private String mLink;

    private RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.detail_action_bar_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_detail);

        mQueue = Volley.newRequestQueue(this);

        RssObject rss = (RssObject) getIntent().getSerializableExtra(BUNDLE_RSS_OBJECT);

        TextView titleTextView = (TextView) findViewById(R.id.detail_title_text_view);
        titleTextView.setText(rss.getTitle());

        TextView descriptionTextView = (TextView) findViewById(R.id.detail_description_text_view);
        descriptionTextView.setText(rss.getDescription());

        mLink = rss.getLink();
        TextView linkTextView = (TextView) findViewById(R.id.detail_link_text_view);
        linkTextView.setText(mLink);

        Button button = (Button) findViewById(R.id.detail_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mLink));
                startActivity(intent);
            }
        });

        NetworkImageView imageView = (NetworkImageView) findViewById(R.id.image_view);
        imageView.setImageUrl(rss.getImage(), new ImageLoader(mQueue, new BitmapCache()));

    }

    @Override
    protected void onStop() {
        super.onStop();
        mQueue.cancelAll(VOLLEY_TAG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
