package com.example.ck.rxjava;

import android.view.View;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

/**
 * Created by ck on 2017/8/15.
 */

public class RxUtils {

    public static Observable<String> StrictClick(View view){
        checkNull(view);
        return Observable.create(new ViewClickOnSubscribe(view)).throttleFirst(600, TimeUnit.MILLISECONDS);
    }

    private static <T> void checkNull(T value){
        if(value==null){
            throw new NullPointerException("intend to click a null view");
        }
    }

    private static class ViewClickOnSubscribe implements ObservableOnSubscribe<String>{
        WeakReference<View>viewref;
        public ViewClickOnSubscribe(View view){
            viewref=new WeakReference<View>(view);
        }
        @Override
        public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {

            if(!e.isDisposed()&&viewref.get()!=null){
              View.OnClickListener clickListener=(v)->{
                    e.onNext("");
              };
                viewref.get().setOnClickListener(clickListener);
            }


        }
    }
}
