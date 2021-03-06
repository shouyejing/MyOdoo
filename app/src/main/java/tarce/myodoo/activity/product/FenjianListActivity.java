package tarce.myodoo.activity.product;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tarce.api.RetrofitClient;
import tarce.api.api.InventoryApi;
import tarce.model.inventory.TakeDelListBean;
import tarce.myodoo.R;
import tarce.myodoo.activity.BaseActivity;
import tarce.myodoo.activity.takedeliver.TakeDeliveListActivity;
import tarce.myodoo.activity.takedeliver.TakeDeliverDetailActivity;
import tarce.myodoo.adapter.takedeliver.TakeDelListAdapter;
import tarce.myodoo.uiutil.RecyclerFooterView;
import tarce.myodoo.uiutil.RecyclerHeaderView;
import tarce.myodoo.uiutil.TipDialog;
import tarce.support.ToastUtils;

/**
 * Created by zouwansheng on 2017/10/26.
 */

public class FenjianListActivity extends BaseActivity {
    private static final int Refresh_Move = 1;//下拉动作
    private static final int Load_Move = 2;//上拉动作

    @InjectView(R.id.sale_number)
    TextView saleNumber;
    @InjectView(R.id.parnter_name)
    TextView parnterName;
    @InjectView(R.id.state_deliever)
    TextView stateDeliever;
    @InjectView(R.id.swipe_refresh_header)
    RecyclerHeaderView swipeRefreshHeader;
    @InjectView(R.id.swipe_target)
    RecyclerView swipeTarget;
    @InjectView(R.id.swipe_load_more_footer)
    RecyclerFooterView swipeLoadMoreFooter;
    @InjectView(R.id.swipeToLoad)
    SwipeToLoadLayout swipeToLoad;
    private InventoryApi inventoryApi;
    private int loadTime = 0;
    private List<TakeDelListBean.ResultBean.ResDataBean> res_data;
    private List<TakeDelListBean.ResultBean.ResDataBean> dataBeanList;
    private TakeDelListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takedel_list);
        ButterKnife.inject(this);

        inventoryApi = RetrofitClient.getInstance(FenjianListActivity.this).create(InventoryApi.class);
        setRecyclerview(swipeTarget);
        showDefultProgressDialog();
        initData(0,40,Refresh_Move);
        initRecycler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataBeanList == null && res_data == null){
            initData(0, 40, Refresh_Move);
        }
    }

    private void initRecycler() {
        swipeRefreshHeader.setGravity(Gravity.CENTER);
        swipeLoadMoreFooter.setGravity(Gravity.CENTER);
        swipeToLoad.setRefreshHeaderView(swipeRefreshHeader);
        swipeToLoad.setLoadMoreFooterView(swipeLoadMoreFooter);
        swipeToLoad.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData(0,40, Refresh_Move);
                if (listAdapter != null){
                    listAdapter.notifyDataSetChanged();
                }
                swipeToLoad.setRefreshing(false);
            }
        });
        swipeToLoad.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                swipeToLoad.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadTime++;
                        initData(40*loadTime, 40, Load_Move);
                        listAdapter.notifyDataSetChanged();
                        swipeToLoad.setLoadingMore(false);
                    }
                }, 1000);
            }
        });
    }

    //请求数据
    private void initData(final int offset, final int limit, final int move) {
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("state", "picking");
        hashMap.put("limit", limit);
        hashMap.put("offset", offset);
        hashMap.put("picking_type_code", "incoming");
        Call<TakeDelListBean> inComingOutgoingList = inventoryApi.getInComingOutgoingList(hashMap);
        inComingOutgoingList.enqueue(new Callback<TakeDelListBean>() {
            @Override
            public void onResponse(Call<TakeDelListBean> call, Response<TakeDelListBean> response) {
                dismissDefultProgressDialog();
                if (response.body()==null)return;
                if (response.body().getError()!=null){
                    new TipDialog(FenjianListActivity.this, R.style.MyDialogStyle, response.body().getError().getData().getMessage())
                            .show();
                    return;
                }
                if (response.body().getResult().getRes_data()!=null && response.body().getResult().getRes_code() ==1){
                    res_data = response.body().getResult().getRes_data();
                    if (move == Refresh_Move){
                        dataBeanList = res_data;
                        listAdapter = new TakeDelListAdapter(R.layout.adapter_takedel_list, res_data);
                        swipeTarget.setAdapter(listAdapter);
                    }else {
                        if (res_data == null){
                            ToastUtils.showCommonToast(FenjianListActivity.this, "没有更多数据...");
                            return;
                        }
                        dataBeanList = listAdapter.getData();
                        dataBeanList.addAll(res_data);
                        listAdapter.setData(dataBeanList);
                    }
                    initListener();
                }else if (response.body().getResult().getRes_data()!=null && response.body().getResult().getRes_code()==-1){
                    //ToastUtils.showCommonToast(FenjianListActivity.this, response.body().getResult().getRes_data());
                }else if (response.body().getResult().getRes_code() == 1 && response.body().getResult().getRes_data() == null
                        && move!=Load_Move){
                    swipeTarget.setVisibility(View.GONE);
                    ToastUtils.showCommonToast(FenjianListActivity.this, "没有更多数据...");
                }
            }

            @Override
            public void onFailure(Call<TakeDelListBean> call, Throwable t) {
                dismissDefultProgressDialog();
            }
        });
    }

    private void initListener() {
        listAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(FenjianListActivity.this, TakeDeliverDetailActivity.class);
                intent.putExtra("dataBean", listAdapter.getData().get(position));
                intent.putExtra("type_code", listAdapter.getData().get(position).getPicking_type_code());
                intent.putExtra("state",listAdapter.getData().get(position).getState());
                intent.putExtra("from", "yes");
                intent.putExtra("notneed", "");
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dataBeanList != null && res_data != null){
            dataBeanList = null;
            res_data = null;
        }
    }
}
