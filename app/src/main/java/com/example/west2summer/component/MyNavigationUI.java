package com.example.west2summer.component;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;

import com.example.west2summer.R;
import com.example.west2summer.user.User;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;

import java.lang.ref.WeakReference;

public class MyNavigationUI {

    public static void setupWithNavController(@NonNull final NavigationView navigationView,
                                              @NonNull final NavController navController) {
        navigationView.setNavigationItemSelectedListener(
                item -> {
                    boolean handled = onNavDestinationSelected(item, navController);
                    if (handled) {
                        ViewParent parent = navigationView.getParent();
                        if (parent instanceof DrawerLayout) {
                            ((DrawerLayout) parent).closeDrawer(navigationView);
                        } else {
                            BottomSheetBehavior bottomSheetBehavior =
                                    findBottomSheetBehavior(navigationView);
                            if (bottomSheetBehavior != null) {
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            }
                        }
                    }
                    return handled;
                });
        final WeakReference<NavigationView> weakReference = new WeakReference<>(navigationView);
        navController.addOnDestinationChangedListener(
                new NavController.OnDestinationChangedListener() {
                    @Override
                    public void onDestinationChanged(@NonNull NavController controller,
                                                     @NonNull NavDestination destination, @Nullable Bundle arguments) {
                        NavigationView view = weakReference.get();
                        if (view == null) {
                            navController.removeOnDestinationChangedListener(this);
                            return;
                        }
                        Menu menu = view.getMenu();
                        for (int h = 0, size = menu.size(); h < size; h++) {
                            MenuItem item = menu.getItem(h);
                            item.setChecked(matchDestination(destination, item.getItemId()));
                        }
                    }
                });
    }

    private static boolean onNavDestinationSelected(@NonNull MenuItem item,
                                                    @NonNull NavController navController) {
        NavOptions.Builder builder = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setEnterAnim(R.anim.nav_default_enter_anim)
                .setExitAnim(R.anim.nav_default_exit_anim)
                .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                .setPopExitAnim(R.anim.nav_default_pop_exit_anim);
        if ((item.getOrder() & Menu.CATEGORY_SECONDARY) == 0) {
            builder.setPopUpTo(findStartDestination(navController.getGraph()).getId(), false);
        }
        NavOptions options = builder.build();
        try {
            //TODO provide proper API instead of using Exceptions as Control-Flow.
            if (User.Companion.isLoginned()) {
                navController.navigate(item.getItemId(), null, options);
            } else {
                if (item.getItemId() != R.id.map_fragment) {
                    navController.navigate(R.id.loginFragment, null, options);
                } else {
                    navController.navigateUp();
                }
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean matchDestination(@NonNull NavDestination destination,
                                            @IdRes int destId) {
        NavDestination currentDestination = destination;
        while (currentDestination.getId() != destId && currentDestination.getParent() != null) {
            currentDestination = currentDestination.getParent();
        }
        return currentDestination.getId() == destId;
    }

    private static NavDestination findStartDestination(@NonNull NavGraph graph) {
        NavDestination startDestination = graph;
        while (startDestination instanceof NavGraph) {
            NavGraph parent = (NavGraph) startDestination;
            startDestination = parent.findNode(parent.getStartDestination());
        }
        return startDestination;
    }

    private static BottomSheetBehavior findBottomSheetBehavior(@NonNull View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                return findBottomSheetBehavior((View) parent);
            }
            return null;
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof BottomSheetBehavior)) {
            // We hit a CoordinatorLayout, but the View doesn't have the BottomSheetBehavior
            return null;
        }
        return (BottomSheetBehavior) behavior;
    }
}