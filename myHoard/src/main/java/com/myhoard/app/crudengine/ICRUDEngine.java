package com.myhoard.app.crudengine;

import java.util.List;

import com.myhoard.app.model.IModel;
import com.myhoard.app.model.Token;

/**
 * Description
 *
 * @author Tomasz Nosal
 *         Date: 17.03.14
 */
 public interface ICRUDEngine<T> {

    public List<T> getList(Token token);

    public T get(int id);

    public boolean create(IModel t, Token token);

    public void update(IModel t, String id, Token token);

    public void remove(String id, Token token);
}
