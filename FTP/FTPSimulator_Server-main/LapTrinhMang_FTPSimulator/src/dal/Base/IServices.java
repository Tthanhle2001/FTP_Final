
package dal.Base;

import java.util.List;


public interface IServices<T> {
    
    public abstract List<T> GetAll();
    
    public abstract T Find(String value);
    
    public abstract boolean Create(T entity);
    
    public abstract boolean Update(T entity);
    
    public abstract boolean Remove(String value);
}
