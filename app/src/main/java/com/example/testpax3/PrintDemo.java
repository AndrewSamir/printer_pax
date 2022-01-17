package com.example.testpax3;

import android.app.Application;

import com.pax.dal.IDAL;
import com.pax.dal.IPrinter;
import com.pax.neptunelite.api.NeptuneLiteUser;

public class PrintDemo extends Application {
    private static IDAL dal;
    private static IPrinter printer;
    private static PrintDemo instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
    public static PrintDemo getInstance() {
        return instance;
    }

    public static IPrinter getPrinter() {
        if (printer == null) {
            printer = getDal().getPrinter();
        }
        return printer;
    }

    public static IDAL getDal() {
        if (dal == null) {
            try {
                dal = NeptuneLiteUser.getInstance().getDal(getInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dal;
    }
}
