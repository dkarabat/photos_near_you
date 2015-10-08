package com.javatechig.gridviewexample;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.javatechig.gridviewexample.retrogram.Instagram;
import com.javatechig.gridviewexample.retrogram.model.Media;
import com.javatechig.gridviewexample.retrogram.model.Popular;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import retrofit.RestAdapter;

public class GridViewActivity extends ActionBarActivity {
    private static final String TAG = GridViewActivity.class.getSimpleName();

    private GridView mGridView;
    private ProgressBar mProgressBar;

    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
//    private String FEED_URL = "http://javatechig.com/?json=get_recent_posts&count=45";

    protected static String AccessToken = "398315918.19f142f.1a6004bc7ce04dc1bc0a7914095a30cb";
    protected static String ClientId = "19f142f8b92641e7b528497c9d206379";

    protected static Double Latitude = 48.858844d;
    protected static Double Longitude = 2.294351d;

    protected Instagram instagram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);

        mGridView = (GridView) findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);

        //Grid view click event
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Get item at position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                Intent intent = new Intent(GridViewActivity.this, DetailsActivity.class);
                ImageView imageView = (ImageView) v.findViewById(R.id.grid_item_image);

                // Interesting data to pass across are the thumbnail size/location, the
                // resourceId of the source bitmap, the picture description, and the
                // orientation (to avoid returning back to an obsolete configuration if
                // the device rotates again in the meantime)

                int[] screenLocation = new int[2];
                imageView.getLocationOnScreen(screenLocation);

                //Pass the image title and url to DetailsActivity
                intent.putExtra("left", screenLocation[0]).
                        putExtra("top", screenLocation[1]).
                        putExtra("width", imageView.getWidth()).
                        putExtra("height", imageView.getHeight()).
                        putExtra("title", item.getTitle()).
                        putExtra("image", item.getImage());

                //Start details activity
                startActivity(intent);
            }
        });

        //Start download
        new AsyncHttpTask().execute();
        mProgressBar.setVisibility(View.VISIBLE);
    }


    //Downloading data asynchronously
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            try {
                instagram = new Instagram(AccessToken, RestAdapter.LogLevel.BASIC);
                GridItem item;
                final Popular popular = instagram.getMediaEndpoint().getPopular();
                parseResult(popular);
                result = 1; // Successful
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
                result = 0; //"Failed
            }

            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // Download complete. Lets update UI

            if (result == 1) {
                mGridAdapter.setGridData(mGridData);
            } else {
                Toast.makeText(GridViewActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }

            //Hide progressbar
            mProgressBar.setVisibility(View.GONE);
        }
    }


    String streamToString(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        // Close stream
        if (null != stream) {
            stream.close();
        }
        return result;
    }

    /**
     * Parsing the feed results and get the list
     *
     * @param popular
     */
    private void parseResult(Popular popular) {
        GridItem item;
        if (popular.getMediaList() != null) {
            for (Media media : popular.getMediaList()) {
                Log.i("link:", media.getImages().getLowResolution().getUrl());
                item = new GridItem();
                item.setImage(media.getImages().getLowResolution().getUrl());
                item.setTitle(media.getLink());
                mGridData.add(item);
            }
        }
    }
}