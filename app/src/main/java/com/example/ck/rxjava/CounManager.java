package com.example.ck.rxjava;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ck on 2017/8/16.
 */

public class CounManager {
    public static final String TAG = "CounManager";
    MyObserver<Long> consumer;
    private int startcount = 0;
    private int tempstart = 0;
    private int step = 1;
    private int max = 100;


    public synchronized CounManager startCount() {
        //没有设置observer返回
        if (consumer == null)
            return this;
        //已经启动了任务，切没有结束
        if(consumer.d!=null&&!consumer.d.isDisposed())
            return this;

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .take(getCount())
                .map(a -> startcount + step * a)
                .doOnNext(aLong -> tempstart = aLong.intValue())
                .doOnComplete(() -> {
                    startcount = 0;
                    tempstart = 0;
                })
                .doOnDispose(() -> {
                    startcount = tempstart;
                    tempstart = 0;
                })
                .map(a -> a > max ? max : a)
                .subscribe(consumer);

        return this;
    }

    public CounManager stop() {
        if (consumer != null && !consumer.d.isDisposed())
            consumer.d.dispose();
        return this;
    }

    public CounManager destory() {
        if (!consumer.d.isDisposed())
            consumer.d.dispose();
        consumer.d = null;
        consumer = null;
        return this;
    }


    /**
     * 属性配置
     * @return
     */
    private int getCount() {
        return (max - startcount) / step + ((max - startcount) % step == 0 ? 0 : 1) + 1;
    }

    public CounManager setStart(int startcount) {

        this.startcount = startcount;
        return this;
    }

    public CounManager setmax(int max) {
        this.max = max;
        return this;
    }

    public CounManager setListener(MyObserver<Long> ab) {
        this.consumer = ab;
        return this;
    }

    public CounManager setStep(int step) {

        this.step = step;
        return this;
    }


    /**
     * 单例子
     *
     * @return
     */
    public static CounManager getInstance() {
        return SingleHolder.counManager;
    }

    private static class SingleHolder {
        private static CounManager counManager = new CounManager();
    }

    /**
     * observer
     * @param <T>
     */
    public static abstract class MyObserver<T> implements Observer<T> {
        public Disposable d;

        @Override
        public void onSubscribe(@NonNull Disposable d) {
            this.d = d;
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Log.i(TAG, "onError: " + e.getMessage());
        }

    }
}
