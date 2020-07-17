package com.licheedev.myapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AnotherActivity extends AppCompatActivity {

    @BindView(R.id.btn_exit)
    Button mBtnExit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_another);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_exit)
    public void onClick() {
        finish();
    }
}
