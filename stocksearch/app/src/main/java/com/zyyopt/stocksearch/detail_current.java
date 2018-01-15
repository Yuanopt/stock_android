package com.zyyopt.stocksearch;

/**
 * Created by zyy on 11/20/17.
 */
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class detail_current extends Fragment {

    private String[] tabletag ={"Stock Symbol","Last Price", "Change", "Timestamp","Open","Close","Day's Range","Volume"," "};

    ArrayList<String> stockinfo;
    ListView tableinfo;
    // highchart
    WebView indicatorview;
    JSONArray timedatas;
    JSONArray pricedatas;
    JSONArray volumedatas;
    // highchart indicator
    JSONArray SMA;
    JSONArray EMA;
    JSONArray CCI;
    JSONArray RSI;
    JSONArray ADX;
    JSONArray BBANDS;
    JSONArray MACD;
    JSONArray STOCH;
    JSONArray selected;
    String selectedfunc;
    Spinner spinner1;
    // pass data
    private String symbol=" ";
    // progressbar
    private ProgressBar progressBar;
    private ProgressBar progressBar2;
    //favorite star
    private CheckBox favorite_star;
    private String isFavorite = " ";
    // facebook share
    ShareDialog mShareDialog;
    CallbackManager mCallbackManager;
    String shareUrl;
    //change button
    TextView changebutton;
    String selectedindi;
    //loaderror
    TextView loaderrormessage; // for table
    TextView loaderrormessage2; // for webview


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // pass data
        if(getArguments()!=null){
            symbol = getArguments().getString("symbol");
            isFavorite = getArguments().getString("ischecked");
        }
        Log.d("PRICESPASS",symbol);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail_current,container,false);
        // webview
        indicatorview = (WebView) rootView.findViewById(R.id.indicatorview);
        // load error message
        loaderrormessage = (TextView) rootView.findViewById(R.id.tableinfo_fail);
        loaderrormessage2 = (TextView) rootView.findViewById(R.id.indicatorview_fail);
        //show the progress bar
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        progressBar2 = (ProgressBar) rootView.findViewById(R.id.progressBar2);
        // facebook share
        ImageView fbshare = (ImageView) rootView.findViewById(R.id.facebookshare);
        fbshare.setClickable(true);
        fbshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FB","clicked");
                mCallbackManager = CallbackManager.Factory.create();
                mShareDialog = new ShareDialog(getActivity());
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(shareUrl))
                        .build();
                mShareDialog.show(content);
            }
        });
        // indicator spinner
        selectedindi = "Price";
        spinner1 = (Spinner) rootView.findViewById(R.id.indicators);
        ArrayAdapter<String> spinnerIndicator = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.indicator));
        spinnerIndicator.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner1.setAdapter(spinnerIndicator);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!spinner1.getSelectedItem().toString().equals(selectedindi)){
                    changebutton.setTextColor(Color.parseColor("#000000"));
                    changebutton.setClickable(true);
                } else {
                    changebutton.setTextColor(Color.parseColor("#c0c4c3"));
                    changebutton.setClickable(false);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        // indicator change button
        changebutton = (TextView) rootView.findViewById(R.id.changebutton);
        changebutton.setClickable(true);
        changebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changebutton.setTextColor(Color.parseColor("#c0c4c3"));
                String func = spinner1.getSelectedItem().toString();
                selectedindi = func;
                if(!func.equals("Price")) {

                    drawindicator(func);
                }else{
                    drawcurrent();
                }
                changebutton.setClickable(false);
            }
        });

        //Volley Request
        tableinfo = (ListView)rootView.findViewById(R.id.tableinfo);
        stockinfo = new ArrayList<String>();
        String url = "http://stocksearchmb.us-east-1.elasticbeanstalk.com/detail?symbol="+symbol;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                     progressBar.setVisibility(View.GONE);
                     if(spinner1.getSelectedItem().toString().equals("Price")) {
                         progressBar2.setVisibility(View.GONE);
                     }

                    // table infomation
                     if(response.has("open") == false){
                            showloaderror();
                     }else {
                         String open = response.getString("open");
                         String symbol1 = symbol;
                         String close = response.getString("close");
                         String pclose = response.getString("pclose");
                         String change = response.getString("change");
                         String timestamp = response.getString("timestamp");
                         String dayrange = response.getString("dayrange");
                         String volume = response.getString("volume");
                         stockinfo.add(symbol1);
                         stockinfo.add(pclose);
                         stockinfo.add(change);
                         stockinfo.add(timestamp);
                         stockinfo.add(open);
                         stockinfo.add(close);
                         stockinfo.add(dayrange);
                         stockinfo.add(volume);
                         CustomAdapter customAdapter = new CustomAdapter(getActivity(), stockinfo);
                         tableinfo.setAdapter(customAdapter);
                         customAdapter.notifyDataSetChanged();
                         Log.d("FAVORITELENGTH", "LENGTH=" + stockinfo.size());
                         // price chart
                         timedatas = response.getJSONArray("xdata");
                         pricedatas = response.getJSONArray("ydataprice");
                         volumedatas = response.getJSONArray("ydatavol");
                         favoritecheck();
                         if (selectedindi.equals("Price")) { // becase you may choose indicator before price down
                             drawcurrent();
                         }
                     }
                } catch (JSONException e){

                     progressBar.setVisibility(View.GONE);
                     progressBar2.setVisibility(View.GONE);
                     showloaderror();
                     e.printStackTrace();
                }
            }

            },new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.GONE);
                    progressBar2.setVisibility(View.GONE);
                    showloaderror();
                    Log.e("VOLLEY","ERROR"+error.toString());
                }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        MyApplication.getInstance().addToReqQueue(jsonObjectRequest,"REQ_CUR");

        // request indicator data
        indicatorRequest(symbol);
        // favorite star check
        favorite_star = (CheckBox) rootView.findViewById(R.id.favorite_star);
        iffavorite();

        return rootView;
    }
    // whether this stock is favorite
    public void iffavorite(){
        if(isFavorite.equals("checked")){
                favorite_star.setChecked(true);
        }else{
            SharedPreferences sharedPreferences3 = getActivity().getSharedPreferences("FavoriteList",Context.MODE_PRIVATE);
            String orderlist = sharedPreferences3.getString("order","");
            if(orderlist.indexOf(symbol)!=-1){
                favorite_star.setChecked(true);
            }
        }
    }
    // load error function
    public void showloaderror(){
        progressBar.setVisibility(View.INVISIBLE);
        loaderrormessage.setText("Failed to load data");
        loaderrormessage.setVisibility(View.VISIBLE);
        progressBar2.setVisibility(View.INVISIBLE);
        loaderrormessage2.setText("Failed to load data");
        loaderrormessage2.setVisibility(View.VISIBLE);
    }
    // load error function indicator
    public void showloderror2(String func){
//        progressBar2.setVisibility(View.INVISIBLE);

    }
    public void showloaderror3(String func){
        progressBar2.setVisibility(View.INVISIBLE);
        loaderrormessage2.setText("Faild to load data of " + func );
        loaderrormessage2.setVisibility(View.VISIBLE);
    }
    class CustomAdapter extends ArrayAdapter<String> {

        public CustomAdapter(@NonNull Context context, ArrayList<String> stockinfo) {
            super(context, 0, stockinfo);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getActivity().getLayoutInflater().inflate(R.layout.tablelist,null);
            TextView Tag = (TextView) view.findViewById(R.id.stocktable_tag);
            TextView Value = (TextView) view.findViewById(R.id.stocktable_value);
            ImageView UporDown = (ImageView) view.findViewById(R.id.upordown);
            String value = stockinfo.get(i);
            Tag.setText(tabletag[i]);
            Value.setText(value);
            if(tabletag[i].equals("Change")){
                if(value.charAt(0)=='-'){
                    UporDown.setImageResource(R.drawable.downarrow);
                }
                UporDown.setVisibility(View.VISIBLE);
            }else{
                UporDown.setVisibility(View.GONE);
            }
            return view;
        }
    }

    private void indicatorRequest(String symbol){
        String urlindicatorbase = "http://stocksearchmb.us-east-1.elasticbeanstalk.com/detail?symbol="+symbol+"&ind=true&indi=";
        String urlindicator = urlindicatorbase + "SMA";
        JsonObjectRequest jsonObjectRequestSMA = new JsonObjectRequest(urlindicator, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try{

                        SMA = response.getJSONArray("SMA");
                        if(SMA.length() > 10){
                            if(spinner1.getSelectedItem().toString().equals("SMA")){
                                progressBar2.setVisibility(View.GONE);
                                drawindicator("SMA");
                            }
                        } else {
                            showloderror2("SMA");
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                },new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY+SMA","ERROR"+error.toString());
                        showloderror2("SMA");
            }
            });
        jsonObjectRequestSMA.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        MyApplication.getInstance().addToReqQueue(jsonObjectRequestSMA,"SMA");

        urlindicator = urlindicatorbase + "EMA";
        JsonObjectRequest jsonObjectRequestEMA = new JsonObjectRequest(urlindicator, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{

                    EMA = response.getJSONArray("EMA");
                    if(EMA.length() > 10) {
                        if (spinner1.getSelectedItem().toString().equals("EMA")) {
                            progressBar2.setVisibility(View.GONE);
                            drawindicator("EMA");
                        }
                    }else{
                        showloderror2("EMA");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY+EMA","ERROR"+error.toString());
                showloderror2("EMA");
            }
        });
        jsonObjectRequestEMA.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        MyApplication.getInstance().addToReqQueue(jsonObjectRequestEMA,"EMA");

        urlindicator = urlindicatorbase + "CCI";
        JsonObjectRequest jsonObjectRequestCCI = new JsonObjectRequest(urlindicator, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    CCI = response.getJSONArray("CCI");
                    if(CCI.length() > 10) {
                        if (spinner1.getSelectedItem().toString().equals("CCI")) {
                            progressBar2.setVisibility(View.GONE);
                            drawindicator("CCI");
                        }
                    } else {
                        showloderror2("CCI");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY+CCI","ERROR"+error.toString());
                showloderror2("CCI");
            }
        });
        jsonObjectRequestCCI.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        MyApplication.getInstance().addToReqQueue(jsonObjectRequestCCI,"CCI");

        urlindicator = urlindicatorbase + "ADX";
        JsonObjectRequest jsonObjectRequestADX = new JsonObjectRequest(urlindicator, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    ADX = response.getJSONArray("ADX");
                    if(ADX.length() > 10) {
                        if (spinner1.getSelectedItem().toString().equals("ADX")) {
                            progressBar2.setVisibility(View.GONE);
                            drawindicator("ADX");
                        }
                    }else{
                        showloderror2("ADX");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY+ADX","ERROR"+error.toString());
                showloderror2("ADX");
            }
        });
        jsonObjectRequestADX.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        MyApplication.getInstance().addToReqQueue(jsonObjectRequestADX,"ADX");

        urlindicator = urlindicatorbase + "RSI";
        JsonObjectRequest jsonObjectRequestRSI = new JsonObjectRequest(urlindicator, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    RSI = response.getJSONArray("RSI");
                    if(RSI.length() > 10){
                        if(spinner1.getSelectedItem().toString().equals("RSI")){
                            progressBar2.setVisibility(View.GONE);
                            drawindicator("RSI");
                        }
                    } else {
                        showloderror2("RSI");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY+RSI","ERROR"+error.toString());
                showloderror2("RSI");
            }
        });
        jsonObjectRequestRSI.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        MyApplication.getInstance().addToReqQueue(jsonObjectRequestRSI,"RSI");

        urlindicator = urlindicatorbase + "BBANDS";
        JsonObjectRequest jsonObjectRequestBBANDS = new JsonObjectRequest(urlindicator, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    BBANDS = response.getJSONArray("BBANDS");
                    if(BBANDS.length() > 10) {
                        if (spinner1.getSelectedItem().toString().equals("BBANDS")) {
                            progressBar2.setVisibility(View.GONE);
                            drawindicator("BBANDS");
                        }
                    }else{
                        showloderror2("BBANDS");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY+BBANDS","ERROR"+error.toString());
                showloderror2("BBANDS");
                BBANDS = new JSONArray();
            }
        });
        jsonObjectRequestBBANDS.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        MyApplication.getInstance().addToReqQueue(jsonObjectRequestBBANDS,"BBANDS");

        urlindicator = urlindicatorbase + "MACD";
        JsonObjectRequest jsonObjectRequestMACD = new JsonObjectRequest(urlindicator, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    MACD = response.getJSONArray("MACD");
                    if(MACD.length() > 10) {
                        if (spinner1.getSelectedItem().toString().equals("MACD")) {
                            progressBar2.setVisibility(View.GONE);
                            drawindicator("MACD");
                        }
                    } else {
                        showloderror2("MACD");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY+MACD","ERROR"+error.toString());
                showloderror2("MACD");
                MACD = new JSONArray();
            }
        });
        jsonObjectRequestMACD.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        MyApplication.getInstance().addToReqQueue(jsonObjectRequestMACD,"MACD");

        urlindicator = urlindicatorbase + "STOCH";
        JsonObjectRequest jsonObjectRequestSTOCH = new JsonObjectRequest(urlindicator, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    STOCH = response.getJSONArray("STOCH");
                    if(STOCH.length() > 10 ) {
                        if (spinner1.getSelectedItem().toString().equals("STOCH")) {
                            progressBar2.setVisibility(View.GONE);
                            drawindicator("STOCH");
                        }
                    } else {
                        showloderror2("STOCH");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY+STOCH","ERROR"+error.toString());
                showloderror2("STOCH");
                STOCH = new JSONArray();
            }
        });
        jsonObjectRequestSTOCH.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        MyApplication.getInstance().addToReqQueue(jsonObjectRequestSTOCH,"STOCH");
        }

    // Java script interface
    public class FbJavascriptInterface{
        Context mContext;
        FbJavascriptInterface(Context c){
            mContext = c;
        }
        @JavascriptInterface
        public void Fbshare(String url){
            shareUrl = url;
            Log.d("URLSTRING",shareUrl);
        }
    }
    // current price chart
    private void drawcurrent(){
        indicatorview.setVisibility(View.INVISIBLE);
        if(timedatas==null || timedatas.length() == 0){
            showloaderror();
        } else {
            WebSettings webSettings = indicatorview.getSettings();
            FbJavascriptInterface jinterface = new FbJavascriptInterface(getActivity());
            indicatorview.addJavascriptInterface(jinterface, "Myfunction");
            webSettings.setJavaScriptEnabled(true);
            indicatorview.loadUrl("file:///android_asset/javascript.html");
            indicatorview.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){
                        Uri uri = request.getUrl();
                        view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        return  true;
                }
                @Override
                public void onPageFinished(WebView view, String url) {
                    indicatorview.post(new Runnable() {
                        @Override
                        public void run() {
                            indicatorview.loadUrl("javascript:drawPrice('" + timedatas.toString() + "','" + pricedatas.toString() + "','" + volumedatas.toString() + "','" + symbol + "');");
                            indicatorview.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
        }
    }
    // indicator price chart
    private void drawindicator(String func){
        indicatorview.setVisibility(View.INVISIBLE);
        loaderrormessage2.setVisibility(View.INVISIBLE);
        progressBar2.setVisibility(View.VISIBLE);
        switch (func){
            case "SMA":
                selected = SMA;
                break;
            case "EMA":
                selected = EMA;
                break;
            case "CCI":
                selected = CCI;
                break;
            case "ADX":
                selected = ADX;
                break;
            case "RSI":
                selected = RSI;
                break;
            case "BBANDS":
                selected = BBANDS;
                break;
            case "MACD":
                selected = MACD;
                break;
            case "STOCH":
                selected = STOCH;
                break;
        }
        if(selected == null) {
            progressBar2.setVisibility(View.VISIBLE);
        } else {
            if (selected.length() < 10) {
                showloaderror3(func);
            } else {

                progressBar2.setVisibility(View.INVISIBLE);
                loaderrormessage2.setVisibility(View.INVISIBLE);
                selectedfunc = func;
//                indicatorview = (WebView) getActivity().findViewById(R.id.indicatorview);

                WebSettings WebSettings = indicatorview.getSettings();
                WebSettings.setJavaScriptEnabled(true);
                FbJavascriptInterface jinterface = new FbJavascriptInterface(getActivity());
                indicatorview.addJavascriptInterface(jinterface, "Myfunction");
                indicatorview.loadUrl("file:///android_asset/javascript.html");
                indicatorview.setWebViewClient(new WebViewClient() {
                    public void onPageFinished(WebView view, String url) {
                        indicatorview.post(new Runnable() {
                            @Override
                            public void run() {
                                indicatorview.loadUrl("javascript:drawindicator('" + selected.toString() + "','" + selectedfunc + "','" + symbol + "');");
                                indicatorview.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }
        }
    }

    // favorite star
    private void favoritecheck(){

        favorite_star.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){

                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("FavoriteList",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String favoritestockinfo = symbol+"@"+stockinfo.get(5)+"@"+stockinfo.get(2);
                    editor.putString(symbol, favoritestockinfo);
                    String orderlist = sharedPreferences.getString("order","");
                    String neworderlist = orderlist + symbol + "-";
                    editor.putString("order",neworderlist);
                    editor.commit();
                    Log.d("FAVORITE","ADD "+symbol+" orderlist= "+sharedPreferences.getString("order","1"));
                }else {
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("FavoriteList",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String orderlist = sharedPreferences.getString("order","");
                    String target = symbol+"-";
                    String neworderlist = orderlist.replaceAll(target,"");
                    editor.putString("order",neworderlist);
                    editor.commit();
                    Log.d("FAVORITE","DELETE "+symbol+" orderlsit= "+sharedPreferences.getString("order","1"));

                }
            }
        });
    }

}
