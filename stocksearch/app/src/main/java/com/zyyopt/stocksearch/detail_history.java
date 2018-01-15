package com.zyyopt.stocksearch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.zyyopt.stocksearch.R.id.indicatorview;
import static com.zyyopt.stocksearch.R.id.progressBar;

public class detail_history extends Fragment {

     WebView historyView;

     String symbol = "nopassdata";
     ProgressBar progressbar;
     TextView errormessage;
     boolean showProgress;
     boolean showerrormessage;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // pass data
        if(getArguments()!=null){
            symbol = getArguments().getString("symbol");
        }
        Log.d("HISTORYPASS",symbol);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail_history, container, false);
        progressbar = (ProgressBar) rootView.findViewById(R.id.progressBar_his);
        progressbar.setVisibility(View.VISIBLE);
        errormessage = (TextView) rootView.findViewById(R.id.errormessage_his);
        showProgress = true;
        showerrormessage = true;
        //
//        errormessage.setVisibility(View.INVISIBLE);
//        String url = "http://stocksearchmb.us-east-1.elasticbeanstalk.com/detail?symbol="+symbol;
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url,
//                    null, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try{
//                    timedatas = response.getJSONArray("xdata");
//                    pricedatas = response.getJSONArray("ydataprice");
//                    Log.d("TIMEDATA",timedatas.toString());
//                    Log.d("PRICEDATA",pricedatas.toString());
//                    drawHighchart();
//                }catch (JSONException e){
//                    e.printStackTrace();
//                }
//            }
//        },new Response.ErrorListener(){
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("VOLLEY","ERROR"+error.toString());
//            }
//        });
//        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
//                0,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        ));
//        MyApplication.getInstance().addToReqQueue(jsonObjectRequest,"REQ_HIS");
        historyView = (WebView)rootView.findViewById(R.id.historyview);
        drawHighchart();
        return rootView;
    }

    private void drawHighchart(){
        WebSettings webSettings = historyView.getSettings();
        WebViewInterface winterface = new WebViewInterface(getActivity());
        historyView.addJavascriptInterface(winterface, "HISFUNCTION");
        webSettings.setJavaScriptEnabled(true);
        historyView.loadUrl("file:///android_asset/javascript.html");
        historyView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){
                Uri uri = request.getUrl();
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return  true;
            }
            public void onPageFinished(WebView view, String url){
                historyView.post(new Runnable() {
                    @Override
                    public void run() {
                        historyView.loadUrl("javascript:drawHistory('"+ symbol + "');");
                    }
                });
            }
        });
    }

    public class WebViewInterface{
        Context mContext;
        WebViewInterface(Context c){mContext = c; }
        @JavascriptInterface
        public void ControlProgressBar(String stats){
            if(stats.equals("success")){
                Log.d("HISCONNECT","success");
//                progressbar.setVisibility(View.INVISIBLE);
                showProgress = false;
            } else {
                Log.d("HISCONNECT","fail");
                showProgress = false;
                showerrormessage = false;
//                progressbar.setVisibility(View.INVISIBLE);

//                errormessage.setText("Failed at load the data");
//                errormessage.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isV){
        super.setUserVisibleHint(isV);
            if(isV) {
                Log.d("SHOW", "AGAIN");
                if (showProgress == false) {
                    progressbar.setVisibility(View.INVISIBLE);
                }
                if (showerrormessage == false) {
                    progressbar.setVisibility(View.INVISIBLE);
                    errormessage.setVisibility(View.VISIBLE);
                    errormessage.setText("Failed to load historical data ");
                }
            }else{
                Log.d("SHOW","NONE");
            }
        }

}
