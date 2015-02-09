/*
 * Zion High School Application for Android
 * Copyright (C) 2013-2015 The Zion High School Application for Android Open Source Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package kr.hs.zion.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import kr.hs.zion.android.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by youngbin on 13. 11. 27.
 * TODO - Notices, Notices_Parents 하나로 통합
 */

public class Notices extends ActionBarActivity {
    private ProgressDialog progressDialog;
    private ArrayList<String> titlearray;
    private ArrayList<String> titleherfarray;
    private ArrayList<String> authorarray;
    private ArrayList<String> datearray;
    private PostListAdapter adapter;
    private SwipeRefreshLayout SRL;
    ListView listview;
    String url;
    String title;

    private final Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {

        }
    };
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notices);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listview = (ListView) findViewById(R.id.listView);

        url=getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(title);

        SRL = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        SRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                networkTask();
            }
        });
        networkTask();
    }
    private AdapterView.OnItemClickListener GoToWebPage = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView<?> adapterView, View clickedView, int pos, long id){
           String herfitem = titleherfarray.get(pos);
           Intent intent = new Intent(Notices.this, PostViewActivity.class);
           intent.putExtra("title", titlearray.get(pos));
           intent.putExtra("date", datearray.get(pos));
           intent.putExtra("author", authorarray.get(pos));
           intent.putExtra("URL", herfitem);
            startActivity(intent);
        }
    };

    //Method for get list of notices
private void networkTask(){
    NetworkChecker NetCheck = new NetworkChecker(Notices.this);
    if(NetCheck.isNetworkConnected()){
        final Handler mHandler = new Handler();
        new Thread()
        {

            public void run()
            {

                mHandler.post(new Runnable(){

                    public void run()
                    {
                        SRL.setRefreshing(true);
                    }
                });

                //Task

                //Notices URL
                try {
                    titlearray = new ArrayList<String>();
                    titleherfarray = new ArrayList<String>();
                    authorarray = new ArrayList<String>();
                    datearray = new ArrayList<String>();
                    //파싱할 페이지 URL
                    Document doc = Jsoup.connect(url).get();
                    Elements rawmaindata = doc.select(".listbody a"); //Get contents from tags,"a" which are in the class,"listbody"
                    Elements rawauthordata = doc.select("td:eq(3)"); //작성자 이름 얻기 - 3번째 td셀 에서 얻기
                    Elements rawdatedata = doc.select("td:eq(4)"); //작성 날자 얻기 - 4번째 td셀 에서 얻기
                    String titlestring = rawmaindata.toString();
                    Log.i("Notices","Parsed Strings" + titlestring);


                    //파싱할 데이터로 배열 생성
                    for (Element el : rawmaindata) {
                        String titlherfedata = el.attr("href");
                        String titledata = el.attr("title");
                        titleherfarray.add("http://www.zion.hs.kr/" + titlherfedata); // add value to ArrayList
                        titlearray.add(titledata); // add value to ArrayList
                    }
                    Log.i("Notices","Parsed Link Array Strings" + titleherfarray);
                    Log.i("Notices","Parsed Array Strings" + titlearray);

                    for (Element el : rawauthordata){
                        String authordata = el.text();
                        Log.d("Author",el.text());
                        authorarray.add(authordata);
                    }
                    for (Element el : rawdatedata){
                        String datedata = el.text();
                        Log.d("Date",el.text());
                        datearray.add(datedata);
                    }


                } catch (IOException e) {
                    e.printStackTrace();

                }


                mHandler.post(new Runnable(){
                    public void run(){
                        //UI Task
                        //배열로 어뎁터 설정
                        adapter = new PostListAdapter(Notices.this, titlearray,datearray,authorarray);
                        listview.setAdapter(adapter);
                        listview.setOnItemClickListener(GoToWebPage);
                        handler.sendEmptyMessage(0);
                        SRL.setRefreshing(false);

                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.recent),Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }.start();
        }else{
        Toast.makeText(getApplicationContext(),
                getString(R.string.network_connection_warning), Toast.LENGTH_LONG).show();
        }

    }
}
