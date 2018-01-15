package com.zyyopt.stocksearch;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.StringTokenizer;

import static com.zyyopt.stocksearch.R.id.textView;


public class detail_news extends Fragment  {


    private ArrayList<String> stocknews;
    private String symbol;
    ListView newsList;
    ArrayList<String> URLS;
    TextView errormessage;
    ProgressBar progressbar_news;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //pass data
        if(getArguments()!=null){
            symbol = getArguments().getString("symbol");
        }
        Log.d("NEWSPASS",symbol);
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_detail_news, container, false);
        // loaderror message
        errormessage = (TextView) v.findViewById(R.id.errormessage);
        errormessage.setVisibility(View.GONE);
        // progreesbar
        progressbar_news = v.findViewById(R.id.progressBar_news);
        newsList = (ListView) v.findViewById(R.id.newsList);
        stocknews = new ArrayList<>();
        URLS = new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("http://stocksearchmb.us-east-1.elasticbeanstalk.com/detail?news=true&symbol="+symbol,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    progressbar_news.setVisibility(View.GONE);
                    JSONArray jsonstocknews = response.getJSONArray("news");
                    int len = Math.min(jsonstocknews.length(),5);
                    for(int i = 0 ; i < len; i++){
                        String stocknew = jsonstocknews.getString(i);
                        Log.d("TITLE",stocknew);
                        stocknews.add(stocknew);
                        CustomAdapter customAdapter = new CustomAdapter();
                        newsList.setAdapter(customAdapter);
                        customAdapter.notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY-NEWS","ERROR");
                showloaderrormessage();
            }
        });
        MyApplication.getInstance().addToReqQueue(jsonObjectRequest,"REQ_NEWS");
        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedUrl = URLS.get(position);
                Uri uri = Uri.parse(selectedUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
        });
        return v;
    }

    public void showloaderrormessage(){
        progressbar_news.setVisibility(View.INVISIBLE);
        errormessage.setText("Faild to load news data");
        errormessage.setVisibility(View.VISIBLE);

    };

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return stocknews.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getActivity().getLayoutInflater().inflate(R.layout.newslist,null);
            TextView Title = (TextView) view.findViewById(R.id.news_Title);
            TextView Author = (TextView) view.findViewById(R.id.news_Author);
            TextView PubDate = (TextView) view.findViewById(R.id.news_PubDate);
            String info = stocknews.get(i);
            StringTokenizer st = new StringTokenizer(info,"|");
            String title = st.nextToken();
            String link = st.nextToken();
            String author = st.nextToken();
            String pubDate = st.nextToken();
//            Title.setClickable(true);
//            Title.setMovementMethod(LinkMovementMethod.getInstance());
//            String text = "<a href='"+link + "'>"+title+"</a>";
//            Spannable s = (Spannable) Html.fromHtml(text);
//            for (URLSpan u: s.getSpans(0, s.length(), URLSpan.class)) {
//                s.setSpan(new UnderlineSpan() {
//                    public void updateDrawState(TextPaint tp) {
//                        tp.setUnderlineText(false);
//                    }
//                }, s.getSpanStart(u), s.getSpanEnd(u), 0);
//            }
//            Title.setText(s);
            URLS.add(link);
            Title.setText(title);
            Author.setText(author);
            PubDate.setText(pubDate);
            return view;
        }
    }
}
