package com.maksing.moviedbdomain.usecase.session;

import com.maksing.moviedbdomain.entity.MovieDbConfig;
import com.maksing.moviedbdomain.entity.Session;
import com.maksing.moviedbdomain.exception.InvalidSessionException;
import com.maksing.moviedbdomain.manager.AuthenticationManager;
import com.maksing.moviedbdomain.query.Query;
import com.maksing.moviedbdomain.service.ServiceHolder;
import com.maksing.moviedbdomain.usecase.session.SessionUseCase;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by maksing on 26/12/14.
 */
public class InitializeSessionUseCase extends SessionUseCase<String, Query> {
    private Callbacks mCallbacks;

    public InitializeSessionUseCase(ServiceHolder serviceHolder) {
        super(serviceHolder);
    }

    public void setCallbacks(Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    /**
     *
     * @return an obserable which emit true or false,
     */
    public Observable<String> getObservable() {
        return getMovieDbConfig().cache().flatMap(new Func1<MovieDbConfig, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call(MovieDbConfig movieDbConfig) {
                if (AuthenticationManager.getInstance().getCurrentSession() == null) {
                    return mCallbacks.shouldStartGuestSession();
                } else {
                    return Observable.just(true);
                }
            }
        }).flatMap(new Func1<Boolean, Observable<Session>>() {
            @Override
            public Observable<Session> call(Boolean startSession) {
                if (startSession) {
                    return getCurrentSession();
                } else {
                    return Observable.error(new InvalidSessionException("cancelled initializing session.", InvalidSessionException.ERROR_CANCELLED));
                }
            }
        }).map(new Func1<Session, String>() {
            @Override
            public String call(Session session) {
                if (session != null && session.getSessionId().length() > 0) {
                    return session.getUserName();
                }

                return null;
            }
        }).flatMap(new Func1<String, Observable<String>>() {
            @Override
            public Observable<String> call(String userName) {
                if (userName == null) {
                    return Observable.error(new InvalidSessionException("invalid exception", InvalidSessionException.ERROR_INVALID));
                } else {
                    return Observable.just(userName);
                }
            }
        });
    }

    public interface Callbacks {
        public Observable<Boolean> shouldStartGuestSession(); //handle this in presentation layer. Presenter should return true if want to start session.
    }
}
