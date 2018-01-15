package com.zyyopt.stocksearch;

import com.facebook.FacebookSdk;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.attr.button;
import static android.R.attr.checked;
import static android.R.attr.editable;
import static android.R.attr.resource;
import static android.R.id.list;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static android.media.CamcorderProfile.get;
import static android.os.Build.VERSION_CODES.M;
import static android.provider.ContactsContract.CommonDataKinds.Website.URL;
import static com.facebook.internal.CallbackManagerImpl.RequestCodeOffset.Share;
import static com.zyyopt.stocksearch.R.id.favoriteList;

public class MainActivity extends AppCompatActivity {
    // autocomplete test
    AutoCompleteTextView auto_stock;
    ArrayList<String> stocksymbols;
    ArrayAdapter<String> adapter;
    String url;
    // spinner test
    Spinner spinner1;
    Spinner spinner2;
    // favorite stock
    ArrayList<String> favorite_stocks;
    ArrayList<String> favorite_stocksafter;
    ListView favoriteList;
    //refresh
    ImageButton refreshButton;
    // long press
    String lpstock;
    // auto refresh
    Timer mytimer;
    TimerTask mytimertask;
    // progressBar
    ProgressBar progressBar1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Test textView Button
        TextView clearbutton = (TextView) findViewById(R.id.clear);
        clearbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auto_stock = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
                auto_stock.setText("");
            }
        });
        TextView getQuote = (TextView) findViewById(R.id.getQuote);
        getQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String symbol = "";
                symbol = auto_stock.getText().toString();
                if (symbol.equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(),"Please enter a symbol",Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    StringTokenizer st = new StringTokenizer(symbol, "-");
                    symbol = st.nextToken();
                    intent.putExtra("symbol", symbol);
                    startActivity(intent);
                }
            }
        });
        //test Spinner
        spinner1 = (Spinner) findViewById(R.id.sort_selection);
        spinner1.setPrompt("Title");
        ArrayAdapter<String> spinneradpter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.sortmethod)){
            @Override
            public boolean isEnabled(int position){
                if(position == 0){
                    return false;
                }else{
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View spinnerview = super.getDropDownView(position,convertView,parent);
                TextView spinnertext = (TextView) spinnerview;
                if(position == 0){
                    spinnertext.setTextColor(Color.parseColor("#bcbcbb"));
                }
                return spinnerview;
            }
        };
        spinneradpter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner1.setAdapter(spinneradpter);
        spinner2 = (Spinner) findViewById(R.id.sort_order);
        spinner2.setPrompt("Title");
        ArrayAdapter<String> spinneradpter2 = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.sortorder)){
            @Override
            public boolean isEnabled(int position){
                if(position == 0){
                    return false;
                }else{
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View spinnerview = super.getDropDownView(position,convertView,parent);
                TextView spinnertext = (TextView) spinnerview;
                if(position == 0){
                    spinnertext.setTextColor(Color.parseColor("#bcbcbb"));
                }
                return spinnerview;
            }
        };
        spinneradpter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner2.setAdapter(spinneradpter2);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String order = spinner2.getSelectedItem().toString();
                switch (position){
                    case 1:
                        SortList("Default",order);
                        break;
                    case 2:
                        SortList("Symbol",order);
                        break;
                    case 3:
                        SortList("Price",order);
                        break;
                    case 4:
                        SortList("Change",order);
                        break;
                    case 5:
                        SortList("Change(%)",order);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String method = spinner1.getSelectedItem().toString();
                switch (position){
                    case 1:
                        SortList(method,"Ascending");
                        break;
                    case 2:
                        SortList(method,"Descending");
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //test the favorite ListView
        favoriteList = (ListView) findViewById(R.id.favoriteList);
        SharedPreferences preferences = getSharedPreferences("FavoriteList", Context.MODE_PRIVATE);
        String orderlist = preferences.getString("order","");
        final String[] favorite_symbols = TextUtils.split(orderlist,"-");
        favorite_stocks = new ArrayList<String>();
        for(int i = 0; i < favorite_symbols.length-1; i++){
            String stocknames = favorite_symbols[i];
            favorite_stocks.add(preferences.getString(stocknames,""));
        }
        Log.d("FAVORITELENGTHCRAETE","LENGTH"+favorite_stocks.size());
        if(favorite_stocks.size() > 1) {
            CustomAdapter customAdapter = new CustomAdapter(this, favorite_stocks);
            favoriteList.setAdapter(customAdapter);
        }
        listviewHeight(favoriteList);
        // set the ListView clickable
        favoriteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                TextView name = view.findViewById(R.id.favoritelist_symbol);
                String symbol = name.getText().toString();
                Log.d("CLICK",symbol);
                intent.putExtra("symbol", symbol);
                intent.putExtra("ischecked", "checked");
                startActivity(intent);
            }
        });
        // set the long press function
        registerForContextMenu(favoriteList);
        // test refreshbutton
        refreshButton = (ImageButton) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar1.setVisibility(View.VISIBLE);
                reFresh();
            }
        });
        // test autofresh switch

        Switch autofresh = (Switch) findViewById(R.id.AutoRrefresh);
        autofresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    progressBar1.setVisibility(View.VISIBLE);
                    mytimer = new Timer();
                    mytimertask= new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    reFresh();
                                }
                            });

                        }
                    };
                    mytimer.scheduleAtFixedRate(mytimertask,0,5000);
                }else{
                    mytimertask.cancel();
                    progressBar1.setVisibility(View.INVISIBLE);
                }
            }
        });

        // test autoComplete
        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
        progressBar1.setVisibility(View.INVISIBLE);
        auto_stock = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        auto_stock.setThreshold(0);
        stocksymbols = new ArrayList<String>();
        auto_stock.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(charSequence.toString().length()<=3){
                    stocksymbols = new ArrayList<String>();
                    updateList(charSequence.toString());
                }
            }
        });
    }

    // refresh function
    public  void reFresh(){
        progressBar1.setVisibility(View.VISIBLE);
        favorite_stocksafter = new ArrayList<String>();

        for(int i = 0 ; i < favorite_stocks.size(); i++) {
            String url = "http://stocksearchmb.us-east-1.elasticbeanstalk.com/favorite?symbol="+favorite_stocks.get(i).split("@")[0];
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        String stockinfo = response.getString("stock");
                        boolean exist = false;
                        for(int i = 0; i < favorite_stocksafter.size(); i++){
                            if(favorite_stocksafter.get(i).split("@")[0].equals(stockinfo.split("@")[0])){
                                exist = true;
                            }
                        }
                        if(exist == false) {
                            favorite_stocksafter.add(stockinfo);
                        }
                        Log.d("REFRESH","STOCKINFO"+stockinfo);
                        Log.d("COMPARELENGTH",favorite_stocks.size() +" " + favorite_stocksafter.size());

                        if(favorite_stocksafter.size() == favorite_stocks.size()){
                            ArrayList<String> favorite_stocknew = new ArrayList<>();
                            for(int i = 0 ; i < favorite_stocks.size(); i++){
                                String name = favorite_stocks.get(i).split("@")[0];
                                for(int j = 0 ; j < favorite_stocks.size(); j++){
                                    if(favorite_stocksafter.get(j).split("@")[0].equals(name)){
                                        favorite_stocknew.add(favorite_stocksafter.get(j));
                                    }
                                }
                            }
                            progressBar1.setVisibility(View.INVISIBLE);
                            favorite_stocks = favorite_stocknew;
                            refreshfavorite();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("refresh","error");
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));
            MyApplication.getInstance().addToReqQueue(jsonObjectRequest);
        }
    }
    // refresh function
    public void refreshfavorite(){
        SharedPreferences sharedPreferences2  = getSharedPreferences("FavoriteList",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = sharedPreferences2.edit();
        for(int i = 0; i < favorite_stocks.size(); i++){
            String name = favorite_stocks.get(i).split("@")[0];
            editor2.putString(name, favorite_stocks.get(i));
            editor2.commit();
        }
        Log.d("FAVORITE","refreshfavorite");
        if(favorite_stocks.size() > 0) {
            CustomAdapter customAdapter = new CustomAdapter(this, favorite_stocks);
            favoriteList.setAdapter(customAdapter);
        }
        listviewHeight(favoriteList);
    }


    // long press function
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);

        if(view.getId() == R.id.favoriteList){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        }
        ListView list = (ListView) view;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        int position = info.position;
        lpstock = (String) list.getItemAtPosition(position);
        Log.d("LONG PRESS",lpstock);

    }
    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()){
            case R.id.remove_no:
                Toast.makeText(this,"No Selected",Toast.LENGTH_LONG).show();
                return true;
            case R.id.remove_yes:
                lpremove();
                Toast.makeText(this,"Yes Selected",Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    public void lpremove(){

        SharedPreferences sharedPreferences = getSharedPreferences("FavoriteList",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String orderlist = sharedPreferences.getString("order","");
        String symbol =lpstock.split("@")[0];
        String target = symbol+"-";
        String neworderlist = orderlist.replaceAll(target,"");
        editor.putString("order",neworderlist);
        editor.commit();
        String[] favorite_symbols = TextUtils.split(neworderlist,"-");
        favorite_stocks = new ArrayList<String>();
        for(int i = 0; i < favorite_symbols.length-1; i++){
            String stocknames = favorite_symbols[i];
            favorite_stocks.add(sharedPreferences.getString(stocknames,""));
        }

        CustomAdapter customAdapter = new CustomAdapter(this, favorite_stocks);
        favoriteList.setAdapter(customAdapter);
        listviewHeight(favoriteList);
    }

    // reResume function
    public void onResume(){
        super.onResume();
        auto_stock.setText("");
        spinner1.setSelection(0);
        spinner2.setSelection(0);
        favoriteList = (ListView) findViewById(R.id.favoriteList);
        SharedPreferences preferences = getSharedPreferences("FavoriteList", Context.MODE_PRIVATE);
        String orderlist = preferences.getString("order","");
        String[] favorite_symbols = TextUtils.split(orderlist,"-");
        favorite_stocks = new ArrayList<String>();
        for(int i = 0; i < favorite_symbols.length-1; i++){
            String stocknames = favorite_symbols[i];
            favorite_stocks.add(preferences.getString(stocknames,""));
        }
        Log.d("FAVORITE LENGTHRE","LENGTH "+favorite_stocks.size());
        if(favorite_stocks.size() >= 0) {
            CustomAdapter customAdapter = new CustomAdapter(this,favorite_stocks);
            favoriteList.setAdapter(customAdapter);
            listviewHeight(favoriteList);
        }
        reFresh();
    }
    // auto complete test
    public void updateList(final String stocksymbol){

        String input = "";
        try{
            input = "symbol="+URLEncoder.encode(stocksymbol,"utf-8");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        if(!stocksymbol.equals("")) {
            progressBar1.setVisibility(View.VISIBLE);
            url = "http://stocksearchmb.us-east-1.elasticbeanstalk.com/index?" + input;
            JsonObjectRequest jsObjReq = new JsonObjectRequest(url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                progressBar1.setVisibility(View.INVISIBLE);
                                stocksymbols = new ArrayList<>();
                                JSONArray jarry = response.getJSONArray("searchresult");
                                int len = Math.min(jarry.length(), 5);
                                for (int i = 0; i < len; i++) {
                                    String stock = jarry.getString(i);
                                    stocksymbols.add(stock);
                                }
                                adapter = new ArrayAdapter<String>(
                                        getApplicationContext(), android.R.layout.simple_list_item_1, stocksymbols
                                ) {
                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        View view = super.getView(position, convertView, parent);
                                        TextView text = (TextView) view.findViewById(android.R.id.text1);
                                        text.setTextColor(Color.BLACK);
                                        return view;
                                    }
                                };
                                auto_stock.setAdapter(adapter);
                                adapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.e("VOLLEY", "ERROR");
                }
            });
            MyApplication.getInstance().addToReqQueue(jsObjReq, "jreq");
        }

    }


    // mock data for favorite list
    class CustomAdapter extends ArrayAdapter<String>{
        public CustomAdapter(Context context, ArrayList<String> stocks) {
            super(context, 0, stocks);
        }


        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.favoritelist,null);
            TextView symbol = (TextView) view.findViewById(R.id.favoritelist_symbol);
            TextView price = (TextView) view.findViewById(R.id.favoritelist_price);
            TextView change = (TextView) view.findViewById(R.id.favoritelist_change);
            String favoritestock_info = favorite_stocks.get(i);
            StringTokenizer st = new StringTokenizer(favoritestock_info,"@");
            if(st.hasMoreTokens()) {
                symbol.setText(st.nextToken());
                price.setText(st.nextToken());
                String cg = st.nextToken();
                if(cg.charAt(0)!='-'){
                    change.setText(cg);
                    change.setTextColor(Color.GREEN);
                }else{
                    change.setText(cg);
                    change.setTextColor(Color.RED);
                }
            }
            return view;
        }
    }

    public static boolean listviewHeight(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;
        } else {
            return false;
        }
    }

    private void SortList(String sortmethod, String order){
        ListView favoriteList = (ListView) findViewById(R.id.favoriteList);

        SharedPreferences preferences = getSharedPreferences("FavoriteList", Context.MODE_PRIVATE);
        String orderlist = preferences.getString("order","");
        String[] favorite_symbols = TextUtils.split(orderlist,"-");
        favorite_stocks = new ArrayList<String>();
        for(int i = 0; i < favorite_symbols.length-1; i++){
            String stocknames = favorite_symbols[i];
            favorite_stocks.add(preferences.getString(stocknames,""));
        }
        if(order.equals("Ascending")){
            if(sortmethod.equals("Symbol")){
                Collections.sort(favorite_stocks,new SymbolComparator());
            }else if(sortmethod.equals("Price")){
                Collections.sort(favorite_stocks,new PriceComparator());
            }else if(sortmethod.equals("Change")){
                Collections.sort(favorite_stocks, new ChangeComparator());
            }else if(sortmethod.equals("Change(%)")){
                Collections.sort(favorite_stocks, new ChangePrecentComparator());
            }else{

            }
        }
        if(order.equals("Descending")){
            if(sortmethod.equals("Symbol")){
                Collections.sort(favorite_stocks,new SymbolComparator());
                Collections.reverse(favorite_stocks);
            }else if(sortmethod.equals("Price")){
                Collections.sort(favorite_stocks,new PriceComparator());
                Collections.reverse(favorite_stocks);
            }else if(sortmethod.equals("Change")){
                Collections.sort(favorite_stocks, new ChangeComparator());
                Collections.reverse(favorite_stocks);
            }else if(sortmethod.equals("Change(%)")){
                Collections.sort(favorite_stocks, new ChangePrecentComparator());
                Collections.reverse(favorite_stocks);
            }else{

            }

        }
        CustomAdapter customAdapter = new CustomAdapter(this,favorite_stocks);
        favoriteList.setAdapter(customAdapter);
        listviewHeight(favoriteList);
    }

    class SymbolComparator implements Comparator<String> {
        @Override
        public int compare(String a, String b){
           return  a.compareTo(b);
        }
    }
    class PriceComparator implements Comparator<String>{
        @Override
        public int compare(String a, String b){
            String aprice = TextUtils.split(a,"@")[1];
            String bprice = TextUtils.split(b, "@")[1];
            return (int)(Float.parseFloat(aprice) - Float.parseFloat(bprice));
        }
    }
    class ChangeComparator implements Comparator<String>{
        @Override
        public int compare(String a, String b){
            String achange = TextUtils.split(a, "@")[2];
            String bchange = TextUtils.split(b, "@")[2];
            String achangeval = TextUtils.split(achange, "\\(")[0];
            String bchangeval = TextUtils.split(bchange, "\\(")[0];
            return (int) (Float.parseFloat(achangeval)*100 - Float.parseFloat(bchangeval)*100);

        }
    }
    class ChangePrecentComparator implements Comparator<String>{
        @Override
        public int compare(String a, String b){
            String achange = TextUtils.split(a,"@")[2];
            String bchange = TextUtils.split(b,"@")[2];
            String achangeval = TextUtils.split(achange,"\\(")[1];
            String bchangeval = TextUtils.split(bchange, "\\(")[1];
            String achangeper = TextUtils.split(achangeval,"\\%")[0];
            String bchangeper = TextUtils.split(bchangeval,"\\%")[0];
            return (int)(Float.parseFloat(achangeper)*100 - Float.parseFloat(bchangeper)*100);
        }
    }
}
