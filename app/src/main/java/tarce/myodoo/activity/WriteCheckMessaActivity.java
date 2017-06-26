package tarce.myodoo.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.POST;
import tarce.api.MyCallback;
import tarce.api.RetrofitClient;
import tarce.api.api.InventoryApi;
import tarce.model.inventory.TakeDeAreaBean;
import tarce.model.inventory.TakeDelListBean;
import tarce.myodoo.R;
import tarce.myodoo.activity.takedeliver.TakeDeliveListActivity;
import tarce.myodoo.uiutil.DialogIsSave;
import tarce.myodoo.uiutil.ImageUtil;
import tarce.myodoo.utils.StringUtils;
import tarce.support.AlertAialogUtils;
import tarce.support.BitmapUtils;
import tarce.support.ToastUtils;

/**
 * Created by zouzou on 2017/6/23.
 * 填写品检信息页面
 */

public class WriteCheckMessaActivity extends BaseActivity {
    @InjectView(R.id.tv_pizhu)
    TextView tvPizhu;
    @InjectView(R.id.edit_write_pizhu)
    EditText editWritePizhu;
    @InjectView(R.id.img_take_photo)
    ImageView imgTakePhoto;
    @InjectView(R.id.pass_tv)
    TextView passTv;
    @InjectView(R.id.not_pass_tv)
    TextView notPassTv;
    private String selectedImagePath = "";
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1;//拍照

    private String imgPath;//图片拍照照片的本地路径
    private String imgName;//后缀名
    private InventoryApi inventoryApi;
    private TakeDelListBean.ResultBean.ResDataBean resDataBean;
    private String type_code;
    private String state;
    private String confirm;
    private ArrayList<Integer> intArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writecheck_messa);
        ButterKnife.inject(this);

        setTitle("填写品检信息");
        Intent intent = getIntent();
        confirm = intent.getStringExtra("confirm");
        type_code = intent.getStringExtra("type_code");
        state = intent.getStringExtra("state");
        resDataBean = (TakeDelListBean.ResultBean.ResDataBean) intent.getSerializableExtra("bean");
        inventoryApi = RetrofitClient.getInstance(WriteCheckMessaActivity.this).create(InventoryApi.class);
        if (confirm.equals("confirm")){
            intArr = intent.getIntegerArrayListExtra("intArr");
            passTv.setText("同意入库");
            notPassTv.setText("退回");
            editWritePizhu.setFocusable(false);
            editWritePizhu.setHint("");
            editWritePizhu.setText(resDataBean.getQc_note().toString());
            imgTakePhoto.setClickable(false);
            if (StringUtils.isNullOrEmpty(resDataBean.getQc_img())){
                imgTakePhoto.setVisibility(View.GONE);
            }else {
                Glide.with(WriteCheckMessaActivity.this).load(resDataBean.getQc_img()).into(imgTakePhoto);
            }
        }
    }
    @OnClick(R.id.img_take_photo)
    void takePhoto(View view){
        imgName = "photo.jpg";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri());
        startActivityForResult(intent, REQUEST_CODE_IMAGE_CAPTURE);
    }

    @OnClick(R.id.pass_tv)
    void passTv(View view){
        switch (confirm){
            case "confirm":
                boolean showDia = false;
                for (int i = 0; i < intArr.size(); i++) {
                    if (intArr.get(i) != resDataBean.getPack_operation_product_ids().get(i).getProduct_id().getQty_available()){
                        showDia = true;
                        break;
                    }
                }
                if (showDia){
                    new DialogIsSave(WriteCheckMessaActivity.this).changeTitle().changeOne().changeTwo()
                            .setSave(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    rejectBotcreate("process");
                                }
                            }).setNotSave(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            rejectBotcreate("cancel_backorder");
                        }
                    }).setCancel().show();
                }else {
                    rejectBotcreate("cancel_backorder");
                }
                break;
            case "notConfirm":
                if (StringUtils.isNullOrEmpty(editWritePizhu.getText().toString())){
                    ToastUtils.showCommonToast(WriteCheckMessaActivity.this, "请填写批注");
                    return;
                }
                AlertAialogUtils.getCommonDialog(WriteCheckMessaActivity.this, "是否确定品检通过")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                commitState("qc_ok");
                            }
                        }).show();
                break;
        }
    }

    @OnClick(R.id.not_pass_tv)
    void passNottv(View view){
        switch (confirm){
            case "confirm"://退回
                AlertAialogUtils.getCommonDialog(WriteCheckMessaActivity.this, "是否确定退回")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                rejectBotcreate("reject");
                            }
                        }).show();
                break;
            case "notConfirm":
                if (StringUtils.isNullOrEmpty(editWritePizhu.getText().toString())){
                    ToastUtils.showCommonToast(WriteCheckMessaActivity.this, "请填写批注");
                    return;
                }
                commitState("qc_failed");
                break;
        }
    }

    /**
     * 退回  不创建欠单
     * */
    private void rejectBotcreate(final String state_for){
        showDefultProgressDialog();
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("picking_id",resDataBean.getPicking_id());
        hashMap.put("state",state_for);
        int size = resDataBean.getPack_operation_product_ids().size();
        Map[] maps = new Map[size];
        for (int i = 0; i < size; i++) {
            Map<Object, Object> map = new HashMap<>();
            map.put("pack_id",resDataBean.getPack_operation_product_ids().get(i).getPack_id());
            map.put("qty_done", StringUtils.doubleToInt(resDataBean.getPack_operation_product_ids().get(i).getQty_done()));
            maps[i] = map;
        }
        hashMap.put("pack_operation_product_ids",maps);
        final Call<TakeDeAreaBean> reject = inventoryApi.reject(hashMap);
        reject.enqueue(new MyCallback<TakeDeAreaBean>() {
            @Override
            public void onResponse(Call<TakeDeAreaBean> call, Response<TakeDeAreaBean> response) {
                dismissDefultProgressDialog();
                if (response.body() == null)return;
                if (response.body().getResult().getRes_data()!=null && response.body().getResult().getRes_code() == 1){
                    if (state_for.equals("reject")){
                        ToastUtils.showCommonToast(WriteCheckMessaActivity.this, "退回成功");
                        Intent intent = new Intent(WriteCheckMessaActivity.this, TakeDeliveListActivity.class);
                        intent.putExtra("from", "no");
                        intent.putExtra("type_code", type_code);
                        intent.putExtra("state",state);
                        startActivity(intent);
                        finish();
                    }else if (state_for.equals("cancel_backorder") || state_for.equals("process")){
                        AlertAialogUtils.getCommonDialog(WriteCheckMessaActivity.this, "入库调拨成功，等待入库")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(WriteCheckMessaActivity.this, TakeDeliveListActivity.class);
                                        intent.putExtra("from", "no");
                                        intent.putExtra("type_code", type_code);
                                        intent.putExtra("state",state);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(WriteCheckMessaActivity.this, TakeDeliveListActivity.class);
                                intent.putExtra("from", "no");
                                intent.putExtra("type_code", type_code);
                                intent.putExtra("state",state);
                                startActivity(intent);
                                finish();
                            }
                        }).show();
                    }
                }else {
                    ToastUtils.showCommonToast(WriteCheckMessaActivity.this, "出现错误，请联系开发人员调试");
                }
            }

            @Override
            public void onFailure(Call<TakeDeAreaBean> call, Throwable t) {
                dismissDefultProgressDialog();
                ToastUtils.showCommonToast(WriteCheckMessaActivity.this, t.toString());
            }
        });
    }

    /**
     * 品检通过不通过
     * */
    private void commitState(final String state_for){
        showDefultProgressDialog();
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("picking_id",resDataBean.getPicking_id());
        hashMap.put("qc_note",editWritePizhu.getText().toString());
        hashMap.put("state",state_for);
        hashMap.put("qc_img", BitmapUtils.bitmapToBase64(ImageUtil.decodeFile(selectedImagePath)));
        int size = resDataBean.getPack_operation_product_ids().size();
        Map[] maps = new Map[size];
        for (int i = 0; i < size; i++) {
            Map<Object, Object> map = new HashMap<>();
            map.put("pack_id",resDataBean.getPack_operation_product_ids().get(i).getPack_id());
            map.put("qty_done", StringUtils.doubleToInt(resDataBean.getPack_operation_product_ids().get(i).getQty_done()));
            maps[i] = map;
        }
        hashMap.put("pack_operation_product_ids",maps);
        Call<TakeDeAreaBean> takeDeAreaBeanCall = inventoryApi.commitRuku(hashMap);
        takeDeAreaBeanCall.enqueue(new MyCallback<TakeDeAreaBean>() {
            @Override
            public void onResponse(Call<TakeDeAreaBean> call, Response<TakeDeAreaBean> response) {
                dismissDefultProgressDialog();
                if (response.body() == null)return;
                if (response.body().getResult().getRes_data()!=null && response.body().getResult().getRes_code() == 1){
                    if ("qc_ok".equals(state_for)){
                        AlertAialogUtils.getCommonDialog(WriteCheckMessaActivity.this, "品检通过,等待采购检验")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(WriteCheckMessaActivity.this, TakeDeliveListActivity.class);
                                        intent.putExtra("from", "yes");
                                        intent.putExtra("type_code", type_code);
                                        intent.putExtra("state",state);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(WriteCheckMessaActivity.this, TakeDeliveListActivity.class);
                                intent.putExtra("from", "yes");
                                intent.putExtra("type_code", type_code);
                                intent.putExtra("state",state);
                                startActivity(intent);
                                finish();
                            }
                        }).show();
                    }else if ("qc_failed".equals(state_for)){
                        AlertAialogUtils.getCommonDialog(WriteCheckMessaActivity.this, "品检不通过，提交成功！")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(WriteCheckMessaActivity.this, TakeDeliveListActivity.class);
                                        intent.putExtra("from", "yes");
                                        intent.putExtra("type_code", type_code);
                                        intent.putExtra("state",state);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(WriteCheckMessaActivity.this, TakeDeliveListActivity.class);
                                intent.putExtra("from", "yes");
                                intent.putExtra("type_code", type_code);
                                intent.putExtra("state",state);
                                startActivity(intent);
                                finish();
                            }
                        }).show();
                    }
                }
            }
        });
    }
    public Uri setImageUri() {
        // Store image in dcim
        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/", "image" + new Date().getTime() + ".png");
        Uri imgUri = Uri.fromFile(file);
        this.imgPath = file.getAbsolutePath();
        return imgUri;
    }

    public String getImagePath() {
        return imgPath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == REQUEST_CODE_IMAGE_CAPTURE) {
                selectedImagePath = getImagePath();
                Glide.with(WriteCheckMessaActivity.this).load(new File(selectedImagePath)).into(imgTakePhoto);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }

    }
}
