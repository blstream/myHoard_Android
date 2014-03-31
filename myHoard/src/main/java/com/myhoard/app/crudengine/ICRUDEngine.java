package com.myhoard.app.crudengine;

import java.util.List;

import com.myhoard.app.model.IModel;
import com.myhoard.app.model.Token;

/**
 * Description
 *
 * @author Tomasz Nosal & Marcin Łaszcz
 *         Date: 17.03.14
 */
 public interface ICrudEngine<T> {

    final public static int ERROR_CODE = -1;

    public List<T> getList(Token token);

    public T get(int id);

    /**
     * @param t obiekt wysyłany na serwer
     * @param token token uwierzytelniający
     * @return id dodanego obiektu lub ERROR_CODE w przypadku błędu
     */
    public int create(IModel t, Token token);

    public void update(IModel t, String id, Token token);

    public void remove(String id, Token token);
}
