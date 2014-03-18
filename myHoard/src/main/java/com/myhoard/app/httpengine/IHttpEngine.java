package com.myhoard.app.httpengine;

import java.util.List;

import com.myhoard.app.model.IModel;
import com.myhoard.app.model.Token;

/**
 * Description
 *
 * @author gohilukk
 *         Date: 17.03.14
 */
 public interface IHttpEngine<T> {

    public List<T> getList(Token token);

    public T get(int id);

    public boolean create(IModel t, Token token);

    public void update(IModel t, String id, Token token);

    public void remove(String id, Token token);
}
