# CountDown
面试 倒计时上机题
### 昨天去福州XX公司面试，上机题。
题目：  实现 计数从0开始 每秒增加10 回调到Activity 中显示。要求用单例实现，绑定Activity生命周期。<br>
             还有一题是listview的相关，单选，考虑性能更新指定item，现在都用Recyclerview，还做listview干嘛。。。

### 考虑了如下几种实现方式：
1.Timer TimeTask 实现计时 这是java的方法 
     但更新UI还得用Handler 或者RunOnUIThread才行（不太好用的感觉，和方法2一样）

2.Handler 或者View 的postDelayRunnable （上机测试就用了这个吃力不讨好的方法）
     可以直接在主线程跟新UI

3.CountDownTimer 最简单的实现方法 也在主线程调用，但经过除了时间差一点外，使用起来方便简单
看日志Thread[main,5,main]--1955--1 10秒倒计时有接近45ms的误差
           Thread[main,5,main]--1842--1 100秒就有将近147秒的误差
 ---
        CountDownTimer timer=new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                Log.i(TAG, "onTick: "+Thread.currentThread()+"--"+millisUntilFinished+"--"+millisUntilFinished/1000);
            }

            @Override
            public void onFinish() {

            }
        };
        timer.start();
---

4.RxJava interval实现

1.使用

```
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
```

2.代码
```

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
        //已经启动了任务，没有结束
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
```

个人总结：CountDownTimer 实现最简单 ，按倒计时，每秒加10。
          其他方法都挺复杂的
          而且我还选了一个看起来简单却挺复杂的方法
          
          不知道还有没有更好的方法，请告知，继续等了通知，haha。
