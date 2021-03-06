package com.maksing.moviedbdomain.usecase.session;

import com.maksing.moviedbdomain.entity.MovieDbConfig;
import com.maksing.moviedbdomain.entity.Session;
import com.maksing.moviedbdomain.manager.AuthenticationManager;
import com.maksing.moviedbdomain.query.Query;
import com.maksing.moviedbdomain.service.ServiceHolder;
import com.maksing.moviedbdomain.service.SessionService;
import com.maksing.moviedbdomain.usecase.UseCase;
import com.maksing.moviedbdomain.usecase.configuration.GetMovieDbConfigUseCase;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by maksing on 24/12/14.
 */
public abstract class SessionUseCase<S, Q extends Query> extends UseCase<S, Q> {
    private final GetMovieDbConfigUseCase mGetMovieDbConfigUseCase;
    private final SessionService mSessionService;

    public SessionUseCase(ServiceHolder serviceHolder) {
        mGetMovieDbConfigUseCase = new GetMovieDbConfigUseCase(serviceHolder);
        mSessionService = serviceHolder.getSessionService();
    }

    protected Observable<MovieDbConfig> getMovieDbConfig() {
        return mGetMovieDbConfigUseCase.getObservable();
    }

    protected Observable<Session> getCurrentSession() {
        Session session = AuthenticationManager.getInstance().getCurrentSession();
        if (session != null) {
            return Observable.just(AuthenticationManager.getInstance().getCurrentSession());
        } else {
            return mSessionService.getGuestSession().doOnNext(new Action1<Session>() {
                @Override
                public void call(Session session) {
                    AuthenticationManager.getInstance().setCurrentSession(session);
                }
            });
        }
    }

    @Override
    protected Observable<S> getObservable(Q query) {
        return null;
    }

    @Override
    protected Observable<S> getObservable() {
        return null;
    }
}
