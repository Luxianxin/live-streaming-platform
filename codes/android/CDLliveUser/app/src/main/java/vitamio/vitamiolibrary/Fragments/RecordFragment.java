package vitamio.vitamiolibrary.Fragments;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import vitamio.vitamiolibrary.Https;
import vitamio.vitamiolibrary.R;
import vitamio.vitamiolibrary.config.Config;
import vitamio.vitamiolibrary.videos.utils.PlayUtils;


public class RecordFragment extends Fragment implements AdapterView.OnItemClickListener {
    private List<Map<String,Object>> dataList = new ArrayList<>();
    private String URL[];
    private GridView mGv;
    private SimpleAdapter adapter;
    private SwipeRefreshLayout Freshlayout;
    private int[] icon={R.mipmap.ic_launcher,R.mipmap.ic_launcher,R.mipmap.ic_launcher
            ,R.mipmap.ic_launcher,R.mipmap.ic_launcher,R.mipmap.ic_launcher,R.mipmap.ic_launcher,R.mipmap.ic_launcher,R.mipmap.ic_launcher,R.mipmap.ic_launcher,R.mipmap.ic_launcher,R.mipmap.ic_launcher};
    private Retrofit retrofit;
    private Https https;





    public RecordFragment() { }


    /*public static RecordFragment newInstance(String userName) {
        RecordFragment fragment = new RecordFragment();
        Bundle bundle = new Bundle();
        bundle.putString("USER_NAME", userName);//利用此构造函数在登录页面向此Fragment传递用户名参数
        fragment.setArguments(bundle);
        return fragment;
    }*/

    public static RecordFragment newInstance() {
        RecordFragment fragment = new RecordFragment();
        /*Bundle bundle = new Bundle();
        bundle.putString("USER_NAME", userName);//利用此构造函数在登录页面向此Fragment传递用户名参数
        fragment.setArguments(bundle);*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //super.onCreate(savedInstanceState);

        //初始化Retrofit，通过 Retrofit.Builder 来创建一个retrofit客户端
        https = new Https(getActivity());
        retrofit = new Retrofit.Builder()
                .baseUrl(Config.DEFAULT_SERVER_URL_LOGIN)
                .addConverterFactory(GsonConverterFactory.create())
                .client(https.provideOkHttpClient())
                .build();

        mGv = (GridView)view.findViewById(R.id.gv_record_list);
        adapter = new SimpleAdapter(getActivity(),dataList,R.layout.item,new String[]{"image","UserName"},
                new int[]{R.id.image,R.id.text});

        Freshlayout = (SwipeRefreshLayout) view.findViewById(R.id.swipRefresh);

        //異步任務，进入页面时刷新
        new AsyncTask<Integer, Integer, Integer>() {

            @Override
            protected void onPreExecute() {

            }

            @Override
            protected Integer doInBackground(Integer... integers) {
                getData();
                return null;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                adapter=new SimpleAdapter(getActivity(),dataList,R.layout.item,new String[]{"image","UserName"},
                        new int[]{R.id.image,R.id.text});

                adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                    public boolean setViewValue(View view, Object data,
                                                String textRepresentation) {
                        if (view instanceof ImageView && data instanceof Bitmap) {
                            ImageView iv = (ImageView) view;
                            iv.setImageBitmap((Bitmap) data);
                            return true;
                        }
                        return false;
                    }
                });

                mGv.setAdapter(adapter);
            }
        }.execute();

        mGv.setOnItemClickListener(this);

        //下拉时刷新
        Freshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncTask<Integer, Integer, Integer>() {

                    @Override
                    protected void onPreExecute() {

                    }

                    @Override
                    protected Integer doInBackground(Integer... integers) {
                        dataList.clear();
                        getData();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Integer integer) {
                        Freshlayout.setRefreshing(false);
                        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                            public boolean setViewValue(View view, Object data,
                                                        String textRepresentation) {
                                if (view instanceof ImageView && data instanceof Bitmap) {
                                    ImageView iv = (ImageView) view;
                                    iv.setImageBitmap((Bitmap) data);
                                    return true;
                                }
                                return false;
                            }
                        });

                        mGv.setAdapter(adapter);
                    }
                }.execute();

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        //String UserName = getArguments().getString("USER_NAME");//获取从登录页面传来的用户名

        //String LiverName = dataList.get(position).get("UserName").toString();
        //String url= Config.DEFAULT_SERVER_URL_Live + LiverName;
        String url= "http://192.168.1.67:8000" + URL[position];
        PlayUtils.startIjkPlayVideo2(getActivity(), url, true);
    }

    //retrofit 定义
    public interface LiveService {
        @Headers({"Content-Type: application/json","Accept: application/json"})//json
        @POST("/streaming/replay")
        Call<ResponseBody> getList();
    }

    /* private List<Map<String,Object>> getData(){
         for(int i=0;i<icon.length;i++){
             Map<String,Object> map=new HashMap<>();
             map.put("image",icon[i]);
             map.put("text",iconName[i]);
             dataList.add(map);
         }
         return dataList;
     }
     */
    private void getData() {
        final RecordFragment.LiveService service = retrofit.create(RecordFragment.LiveService.class);
        retrofit2.Call<ResponseBody> call = service.getList();
        try {
            Response<ResponseBody> response = call.execute();
            String result = response.body().string();
            JSONObject arr = new JSONObject(result);
            JSONArray arr1 = arr.getJSONArray("ret");
            String username[] = new String[arr1.length()];
            Bitmap bm[] = new Bitmap[arr1.length()];
            URL = new String[arr1.length()];

            //Bitmap bm = Picasso.with(getActivity()).load("http://192.168.1.67:8080/live.jpg").get();
            //Bitmap bm = Picasso.with(getActivity()).load("https://www.baidu.com/img/bd_logo1.png?where=super").get();

            for(int i = 0; i < arr1.length(); i++) {
                JSONObject temp = new JSONObject(arr1.getString(i));
                username[i] = temp.getString("username");
                if(temp.getString("portrait").equals("")) {
                    bm[i] = Picasso.with(getActivity()).load(R.drawable.default_portrait).get();
                } else {
                    bm[i] = Picasso.with(getActivity()).load(Config.DEFAULT_SERVER_URL_LOGIN + temp.getString("portrait")).get();
                }
                URL[i] = temp.getString("url");
            }


            for(int i = 0; i < arr1.length(); i++) {
                Map<String,Object> map=new HashMap<>();
                map.put("image", bm[i]);
                map.put("UserName", username[i]);
                dataList.add(map);
            }

            System.out.print(dataList);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



}
