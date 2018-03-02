package com.lxj.okhttpdownloaderdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    ArrayList<AppInfo> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String data = getDataFromServer();

        list = (ArrayList<AppInfo>) parseJsonToList(data,new TypeToken<List<AppInfo>>(){}.getType());

        recyclerView.setAdapter(new DownloadAdapter(list));

    }

    public String getDataFromServer() {
        try {
            InputStream is = getAssets().open("data");
            int len = -1;
            byte[] buffer = new byte[100];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((len=is.read(buffer))!=-1){
                baos.write(buffer,0,len);
            }
            is.close();
            return new String(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static List<?> parseJsonToList(String json, Type type) {
        Gson gson = new Gson();
        List<?> list = gson.fromJson(json, type);
        return list;
    }
}
