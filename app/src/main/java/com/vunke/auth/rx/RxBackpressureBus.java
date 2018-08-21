package com.vunke.auth.rx;

/**
 * Created by zhuxi on 2017/10/13.
 */


import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class RxBackpressureBus {

    private static FlowableProcessor<Object> mBus;

    private RxBackpressureBus() {
        mBus = PublishProcessor.create().toSerialized();
    }

    //    private static class Holder {
//        private static RxBus instance = new RxBus();
//    }
    private static volatile RxBackpressureBus instance;

    public static RxBackpressureBus getInstance() {
        if (instance == null) {
            synchronized (RxBackpressureBus.class) {
                if (instance == null) {
                    instance = new RxBackpressureBus();
                }
            }
        }
        return instance;
    }

    public void post(@NonNull Object obj) {
        mBus.onNext(obj);
    }

    public <T> Flowable<T> register(Class<T> clz) {
        return mBus.ofType(clz);
    }

    public Flowable<Object> register() {
        return mBus;
    }

    public void unregisterAll() {
        //会将所有由mBus生成的Flowable都置completed状态后续的所有消息都收不到了
        mBus.onComplete();
    }

    public boolean hasSubscribers() {
        return mBus.hasSubscribers();
    }


    public void test() {
        RxBackpressureBus instance = RxBackpressureBus.getInstance();
        instance.register(String.class).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {

            }
        });
    }

//    public void Test(){
//         RxBackpressureBus.getInstance().register(String.class).filter(new Predicate<String>() {
//             @Override
//             public boolean test(@NonNull String s) throws Exception {
//                 return false;
//             }
//         }).subscribeOn(Schedulers.io())
//           .observeOn(AndroidSchedulers.mainThread())
//           .subscribe(new Subscriber<String>() {
//               @Override
//               public void onSubscribe(Subscription s) {
//
//               }
//
//               @Override
//               public void onNext(String s) {
//
//               }
//
//               @Override
//               public void onError(Throwable t) {
//
//               }
//
//               @Override
//               public void onComplete() {
//
//               }
//           })   ;
//    }
}
