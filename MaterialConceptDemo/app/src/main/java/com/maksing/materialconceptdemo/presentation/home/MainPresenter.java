package com.maksing.materialconceptdemo.presentation.home;

import com.maksing.materialconceptdemo.model.NavMenu;
import com.maksing.materialconceptdemo.model.NavMenuItem;
import com.maksing.materialconceptdemo.presentation.Presenter;
import com.maksing.moviedbdomain.entity.NavItem;
import com.maksing.moviedbdomain.entity.User;
import com.maksing.moviedbdomain.usecase.configuration.GetNavItemsUseCase;
import com.maksing.moviedbdomain.usecase.session.GetUserDataUseCase;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by maksing on 30/12/14.
 */
public class MainPresenter extends Presenter<MainView> {

    private List<NavMenuItem> mNavMenuItems;
    private NavMenuItem mCurrentNavMenuItem;

    private User mCurrentUser;

    private final GetNavItemsUseCase mGetNavItemsUseCase;
    private final GetUserDataUseCase mGetUserDataUseCase;

    private Observable<NavMenu> mGetNavigationRequest;

    public MainPresenter(GetNavItemsUseCase getNavItemsUseCase, GetUserDataUseCase getUserDataUseCase) {
        mGetNavItemsUseCase = getNavItemsUseCase;
        mGetUserDataUseCase = getUserDataUseCase;
    }

    @Override
    protected void restoreView() {
        updateData();
    }

    @Override
    protected boolean shouldRestore() {
        return mNavMenuItems != null && mCurrentUser != null;
    }

    @Override
    protected void initializeView() {
        if (mGetNavigationRequest == null) {
            mGetNavigationRequest = Observable.zip(mGetNavItemsUseCase.getObservable(), mGetUserDataUseCase.getObservable(), new Func2<List<NavItem>, User, NavMenu>() {
                @Override
                public NavMenu call(List<NavItem> navItems, User user) {
                    return new NavMenu(navItems, user);
                }
            }).cache().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }

        addSubscription(mGetNavigationRequest.subscribe(new Subscriber<NavMenu>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(NavMenu navMenu) {
                mNavMenuItems = new ArrayList<NavMenuItem>();

                for (NavItem item : navMenu.getNavItems()) {
                    mNavMenuItems.add(new NavMenuItem(item.getTitle(), item.getPage()));
                }
                mNavMenuItems.get(0).setSelected(true);

                mCurrentNavMenuItem = mNavMenuItems.get(0);
                mCurrentUser = navMenu.getUser();
                getView().gotoPage(mCurrentNavMenuItem.getPage());
                updateData();
            }
        }));
    }

    public NavMenuItem getCurrentNavMenuItem() {
        return mCurrentNavMenuItem;
    }

    public void setCurrentNavMenuItem(NavMenuItem currentNavMenuItem) {
        mCurrentNavMenuItem = currentNavMenuItem;
    }

    public void selectedNavItem(NavMenuItem navMenuItem) {
        mCurrentNavMenuItem.setSelected(false);
        mCurrentNavMenuItem = navMenuItem;
        navMenuItem.setSelected(true);
        getView().gotoPage(navMenuItem.getPage());
    }

    private void updateData() {
        getView().updateNavigationMenu(mNavMenuItems);
        getView().updateUser(mCurrentUser);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }
}
