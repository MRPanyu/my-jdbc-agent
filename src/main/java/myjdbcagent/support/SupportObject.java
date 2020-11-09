package myjdbcagent.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.WeakHashMap;

/**
 * A SupportObject is an data object attached to each
 * Connection/Statement/ResultSet object which stores information needed by
 * method delegations.
 * <p>
 * Currently the createTime for any object; The sql and parameters for
 * PreparedStatement/ResultSet and the current row number for ResultSet is
 * stored.
 * 
 * @author panyu
 *
 */
public class SupportObject implements Serializable {

	private static final long serialVersionUID = 1L;

	private static WeakHashMap<Object, SupportObject> supportObjectMap = new WeakHashMap<Object, SupportObject>(256);

	public static SupportObject getSupportObject(Object obj) {
		return supportObjectMap.get(obj);
	}

	public static boolean hasSupportObject(Object obj) {
		return supportObjectMap.containsKey(obj);
	}

	public static SupportObject createSupportObject(Object obj) {
		SupportObject s = supportObjectMap.get(obj);
		if (s == null) {
			synchronized (SupportObject.class) {
				s = supportObjectMap.get(obj);
				if (s == null) {
					s = new SupportObject();
					s.setCreateTime(System.currentTimeMillis());
					supportObjectMap.put(obj, s);
				}
			}
		}
		return s;
	}

	private long createTime;
	private String sql;
	private ArrayList<Object> params = new ArrayList<Object>();
	private int rowNum = 0;

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public ArrayList<Object> getParams() {
		return params;
	}

	public void setParams(ArrayList<Object> params) {
		this.params = params;
	}

	public Object[] getParamsArray() {
		return params.toArray();
	}

	public int getRowNum() {
		return rowNum;
	}

	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}

	public void copyProperties(SupportObject s) {
		this.createTime = s.createTime;
		this.sql = s.sql;
		this.params = s.params;
		this.rowNum = s.rowNum;
	}

}
