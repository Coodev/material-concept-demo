package com.maksing.moviedbdata.service;

import com.maksing.moviedbdata.model.movie.Genre;
import com.maksing.moviedbdata.model.movie.MovieData;
import com.maksing.moviedbdata.model.movie.MovieListData;
import com.maksing.moviedbdata.model.movie.Result;
import com.maksing.moviedbdata.retrofit.MovieDbMovieService;
import com.maksing.moviedbdata.retrofit.RetrofitServiceStore;
import com.maksing.moviedbdomain.entity.Movie;
import com.maksing.moviedbdomain.entity.MovieList;
import com.maksing.moviedbdomain.service.MovieService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by maksing on 24/12/14.
 */
public class MovieDataService extends HttpService implements MovieService {
    private final String mApiKey;

    private static volatile MovieDataService sInstance;
    private final RetrofitServiceStore<MovieDbMovieService> mMovieDbMovieServiceStore;
    private final String mEndpoint;

    public static MovieDataService getInstance(String endPoint, String apiKey) {
        if (sInstance == null) {
            synchronized (MovieDataService.class) {
                if (sInstance == null) {
                    sInstance = new MovieDataService(endPoint, apiKey);
                }
            }
        }
        return sInstance;
    }

    //Make it private to disallow to call constructor directly
    private MovieDataService(String endPoint, String apiKey) {
        if (endPoint == null || apiKey == null) {
            throw new IllegalArgumentException("Arguments must not be null in constructing ConfigurationDataRepository");
        }
        mEndpoint = endPoint;
        mMovieDbMovieServiceStore = new RetrofitServiceStore<>(endPoint, MovieDbMovieService.class);
        mApiKey = apiKey;
    }

    @Override
    public Observable<MovieList> getDiscoverMovieList(String query, int page, final String posterBasePath, final String backdropBasePath) {
        return requestGet(mEndpoint + "discover/movie?" + query + "&page=" + page + "&api_key=" + mApiKey, MovieListData.class).map(new Func1<MovieListData, MovieList>() {
            @Override
            public MovieList call(MovieListData movieListData) {
                List<Movie> movies = new ArrayList<Movie>();

                if (movieListData.getResults() != null) {
                    for (Result result : movieListData.getResults()) {
                        String posterPath = "";
                        if (result.getPosterPath() != null) {
                            posterPath = posterBasePath + result.getPosterPath() + "?api_key=" + mApiKey;
                        }
                        String backdropPath = "";
                        if (result.getBackdropPath() != null) {
                            backdropPath = backdropBasePath + result.getBackdropPath() + "?api_key=" + mApiKey;
                        }
                        movies.add(new Movie(String.valueOf(result.getId()), result.getTitle(), posterPath, backdropPath));
                    }
                }

                return new MovieList(movieListData.getPage(), movieListData.getTotalPage(), movies);
            }
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Movie> getMovieById(String id, final String posterBasePath, final String backdropBasePath) {
        return mMovieDbMovieServiceStore.getService().getMovieById(mApiKey, id).map(new Func1<MovieData, Movie>() {
            @Override
            public Movie call(MovieData movieData) {
                String posterPath = posterBasePath + movieData.getPosterPath() + "?api_key=" + mApiKey;
                String backdropPath = backdropBasePath + movieData.getBackdropPath() + "?api_key=" + mApiKey;

                List<String> genres = new ArrayList<>();
                if (movieData.getGenres() != null) {
                    for (Genre genre : movieData.getGenres()) {
                        genres.add(genre.getName());
                    }
                }

                String lang = "";
                if (movieData.getSpokenLanguages() != null && !movieData.getSpokenLanguages().isEmpty()) {
                    lang = movieData.getSpokenLanguages().get(0).getName();
                }

                Date date = null;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    date = simpleDateFormat.parse(movieData.getReleaseDate());
                } catch (ParseException e) {

                }

                Movie movie = new Movie.Builder(String.valueOf(movieData.getId()), movieData.getTitle(), posterPath, backdropPath)
                        .setDescription(movieData.getOverview())
                        .setGenres(genres)
                        .setLanguage(lang)
                        .setRating((float)movieData.getVoteAverage())
                        .setRatingsCount(movieData.getVoteCount())
                        .setReleaseDate(date)
                        .setRunTime(movieData.getRuntime())
                        .setTagline(movieData.getTagline())
                        .build();

                return movie;
            }
        });
    }

    @Override
    public Observable<Movie> getReviewsByMovieId(int id) {
        return null;
    }
}
