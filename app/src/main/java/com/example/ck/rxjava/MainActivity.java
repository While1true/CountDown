package com.example.ck.rxjava;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import io.reactivex.annotations.NonNull;

public class MainActivity extends AppCompatActivity {

    private String TAG = "aaa";
    CounManager.MyObserver<Long> observer;
    private long last;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = (TextView) findViewById(R.id.sss);

        CountDownTimer timer=new CountDownTimer(100000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                Log.i(TAG, "onTick: "+Thread.currentThread()+"--"+millisUntilFinished+"--"+millisUntilFinished/1000);
            }

            @Override
            public void onFinish() {

            }
        };
        timer.start();

       //观察者
        observer = new CounManager.MyObserver<Long>() {
            @Override
            public void onNext(@NonNull Long aLong) {
                tv.setText(aLong + "时间间隔" + (System.currentTimeMillis() - last));
                Log.i(TAG, "onNext: "+(System.currentTimeMillis() - last));
                last = System.currentTimeMillis();

            }

            @Override
            public void onComplete() {
                Toast.makeText(MainActivity.this, "计数完成", 0).show();
                d.dispose();
            }
        };
       //启动计时
        CounManager.getInstance()
                .setmax(998)
                .setStep(10)
                .setStart(5)
                .setListener(observer)
                .startCount();


        RxUtils.StrictClick(tv)
                .subscribe(aVoid -> {
                    CounManager.getInstance().startCount();

                    Log.i(TAG, "onCreate: " + Thread.currentThread());
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        //暂停
        CounManager.getInstance().stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //继续
        CounManager.getInstance().startCount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //重置
        CounManager.getInstance().destory();

    }
}
