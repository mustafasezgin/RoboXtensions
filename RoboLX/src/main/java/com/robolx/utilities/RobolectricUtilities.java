package com.robolx.utilities;

import android.content.Context;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import com.xtremelabs.robolectric.Robolectric;
import roboguice.RoboGuice;
import roboguice.inject.ContextScope;

public class RobolectricUtilities {

    public void enterContext(Context context){
        Injector activityInjector = RoboGuice.getInjector(context);
        activityInjector.getInstance(ContextScope.class).enter(context);
    }

    public void exitContext(Context context){
        Injector activityInjector = RoboGuice.getInjector(context);
        activityInjector.getInstance(ContextScope.class).exit(context);
    }

    public Injector getInjector(Context context){
        return RoboGuice.getInjector(context);
    }

    public void setGuiceModules(AbstractModule module){
        RoboGuice.setBaseApplicationInjector(Robolectric.application, Stage.DEVELOPMENT,
                Modules.override(module).with(RoboGuice.newDefaultRoboModule(Robolectric.application)));
    }
}
