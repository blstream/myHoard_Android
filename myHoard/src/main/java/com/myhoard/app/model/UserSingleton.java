package com.myhoard.app.model;

/**
 * Description
 *
 * @author gohilukk
 *         Date: 18.03.14
 */
public class UserSingleton {
    private static UserSingleton instance = null;
    private static Object mutex = new Object();
    public User user;
    public Token token;

    private UserSingleton() {
    }

    public static UserSingleton getInstance() {
        if (instance == null) {
            synchronized (mutex) {
                if (instance == null) instance = new UserSingleton();
            }
        }
        return instance;
    }
}
