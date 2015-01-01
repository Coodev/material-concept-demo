package com.maksing.materialconceptdemo.presentation.presenter;

import com.maksing.materialconceptdemo.presentation.view.MultiListsView;
import com.maksing.materialconceptdemo.presentation.view.SingleListView;
import com.maksing.moviedbdomain.entity.Movie;
import com.maksing.moviedbdomain.entity.MovieList;
import com.maksing.moviedbdomain.entity.Page;
import com.maksing.moviedbdomain.usecase.GetDiscoverListUseCase;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by maksing on 23/12/14.
 */
public class MultiListsPresenter extends Presenter<MultiListsView> {
    private final Page mPage;

    private final GetDiscoverListUseCase mGetDiscoverListUseCase;
    private Observable<MovieList> mGetMovieListRequest;

    public MultiListsPresenter(Page page, GetDiscoverListUseCase getDiscoverListUseCase) {
        mGetDiscoverListUseCase = getDiscoverListUseCase;
        mPage = page;
    }

    @Override
    protected void restoreView() {
    }

    @Override
    protected boolean shouldRestore() {
        return false;
    }

    @Override
    protected void initializeView() {

        if (mGetMovieListRequest == null) {
            mGetMovieListRequest = mGetDiscoverListUseCase.getObservable(mPage.getDiscoverQueryAt(0), 0).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        getView().showProgressbar();
        addSubscription(mGetMovieListRequest.subscribe(new Subscriber<MovieList>() {
            @Override
            public void onCompleted() {
                getView().hideProgressbar();
            }

            @Override
            public void onError(Throwable e) {
                //TODO: error handling
            }

            @Override
            public void onNext(MovieList movieList) {

            }
        }));
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }
}
