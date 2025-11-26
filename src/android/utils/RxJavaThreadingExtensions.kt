package com.aub.mobilebanking.phone.eg.utils

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * This makes the RX chain subscribe on IO thread and observe on Main thread.
 */
fun Completable.scheduleIoThenMain(): Completable {
    return compose {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

/**
 * This makes the RX chain subscribe on IO thread and observe on Main thread.
 */
fun <T : Any> Single<T>.scheduleIoThenMain(): Single<T> {
    return compose {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

/**
 * This makes the RX chain subscribe on IO thread and observe on Main thread.
 */
fun <T : Any> Observable<T>.scheduleIoThenMain(): Observable<T> {
    return compose {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

/**
 * This makes the RX chain subscribe on IO thread and observe on Main thread.
 */
fun <T : Any> Flowable<T>.scheduleIoThenMain(): Flowable<T> {
    return compose {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

/**
 * This makes the RX chain subscribe on IO thread and observe on Main thread.
 */
fun <T : Any> Maybe<T>.scheduleIoThenMain(): Maybe<T> {
    return compose {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}