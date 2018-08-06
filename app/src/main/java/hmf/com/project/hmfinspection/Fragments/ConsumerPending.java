package hmf.com.project.hmfinspection.Fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import hmf.com.project.hmfinspection.Adpter.ConsumerFormAdpter;
import hmf.com.project.hmfinspection.Api.ConsumerFormApi;
import hmf.com.project.hmfinspection.AppController;
import hmf.com.project.hmfinspection.ESurvey;
import hmf.com.project.hmfinspection.R;
import hmf.com.project.hmfinspection.domains.ConsumerFormRes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by home on 5/11/2018.
 */

public class ConsumerPending extends Fragment {

    List<ConsumerFormRes> infos;
    RecyclerView recyclerView;
    String url;
    ConsumerFormAdpter availabilityAdpter;
    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.consumerpending, container, false);

        infos=new ArrayList<ConsumerFormRes>();
        recyclerView = (RecyclerView)view.findViewById(R.id.pendingview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(availabilityAdpter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendinglist();

        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(hmf.com.project.hmfinspection.R.id.hcontainer);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
        mSwipeRefreshLayout.setOnRefreshListener(new    SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(getContext(), "Refresh", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pendinglist();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 500);
            }
        });
        return view;
    }

    private void pendinglist() {

        url= AppController.BaseUrl+"b2cInspection/status/PENDING";

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("authorization","Bearer "+ESurvey.getAccessToken() )
                        .header("content-type", "application/json")
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            }
        });

        OkHttpClient client = httpClient.build();
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppController.BaseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        progressDialog = ProgressDialog.show(getContext(), "Please wait.",
                "Loading Data..!", true);
        ConsumerFormApi availabilityApi = retrofit.create(ConsumerFormApi.class);
        Call<List<ConsumerFormRes>> listCall= availabilityApi.getData(url);

        listCall.enqueue(new Callback<List<ConsumerFormRes>>() {
            @Override
            public void onResponse(Call<List<ConsumerFormRes>> call, retrofit2.Response<List<ConsumerFormRes>> response) {

                if(response.isSuccessful()){
                    progressDialog.dismiss();

                    infos=response.body();
                    if(infos==null){
                        Toast.makeText(getContext(),"No Pendings",Toast.LENGTH_LONG).show();

                    }else if(infos.size()==0){
                        Toast.makeText(getContext(),"No Pendings",Toast.LENGTH_LONG).show();

                    }else {
                        int s= response.body().size();
                        availabilityAdpter = new ConsumerFormAdpter(getContext(),infos);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setItemViewCacheSize(s);
                        recyclerView.setDrawingCacheEnabled(true);
                        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                        recyclerView.setAdapter(availabilityAdpter);

                    }
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(),response.message(),Toast.LENGTH_LONG).show();

                }

            }

            @Override
            public void onFailure(Call<List<ConsumerFormRes>> call, Throwable t) {

                progressDialog.dismiss();
                Toast.makeText(getContext(),t.getMessage(),Toast.LENGTH_LONG).show();


            }
        });

    }


}
