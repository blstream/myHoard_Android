package com.myhoard.app.crudengine;

import com.myhoard.app.model.IModel;
import com.myhoard.app.model.Token;

import java.util.List;

/**
 * Description
 *
 * @author Tomasz Nosal & Marcin Łaszcz
 *         Date: 17.03.14
 */
 public interface ICRUDEngine<T> {

    final public static int ERROR_CODE = -1;

    public List<T> getList(Token token);

    public T get(String id, Token token);

    /**
     * @param t obiekt wysyłany na serwer
     * @param token token uwierzytelniający
     * @return id dodanego obiektu lub ERROR_CODE w przypadku błędu
     */
    public IModel create(IModel t, Token token);

    public void update(IModel t, String id, Token token);

    public void remove(String id, Token token);
}
