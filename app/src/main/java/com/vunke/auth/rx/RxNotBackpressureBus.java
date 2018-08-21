package com.vunke.auth.rx;

/**
 * Created by zhuxi on 2017/10/13.
 */


import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;


public class RxNotBackpressureBus {

    private final Subject<Object> mBus;

    private RxNotBackpressureBus() {
        mBus = PublishSubject.create().toSerialized();
    }

    //    private static class Holder {
//        private static final RxBus BUS = new RxBus();
//    }
    private static volatile RxNotBackpressureBus instance;

    public static RxNotBackpressureBus getInstance() {
        if (instance == null) {
            synchronized (RxNotBackpressureBus.class) {
                if (instance == null) {
                    instance = new RxNotBackpressureBus();
                }
            }
        }
        return instance;
    }

    public void post(@NonNull Object obj) {
        mBus.onNext(obj);
    }

    public <T> Observable<T> register(Class<T> tClass) {
        return mBus.ofType(tClass);
    }

    public Observable<Object> register() {
        return mBus;
    }

    public boolean hasObservers() {
        return mBus.hasObservers();
    }

    public void unregisterAll() {
        //会将所有由mBus生成的Observable都置completed状态,后续的所有消息都收不到了
        mBus.onComplete();
    }


//public void Test(){
//    RxNotBackpressureBus.getInstance().register(String.class).filter(new Predicate<String>() {
//        @Override
//        public boolean test(@NonNull String s) throws Exception {
//            return false;
//        }
//    }).subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(new DisposableObserver<String>() {
//                @Override
//                public void onNext(@NonNull String s) {
//
//                }
//
//                @Override
//                public void onError(@NonNull Throwable e) {
//
//                }
//
//                @Override
//                public void onComplete() {
//
//                }
//            });
//}
}