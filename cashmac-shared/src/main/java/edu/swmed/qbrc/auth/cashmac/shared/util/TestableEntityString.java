package edu.swmed.qbrc.auth.cashmac.shared.util;

public class TestableEntityString {

	public final String value;
	
	public TestableEntityString(Object in) {
		if (in != null)
			if (in instanceof String && in.toString().toUpperCase().equals("NULL"))
				this.value = null;
			else
				this.value = in.toString();
		else
			this.value = null;
				
	}
	
	
	public String getStringOrNull() {
		return this.value;
	}
	
	public String getString() {
		if (value != null)
			return value;
		else
			return "NULL";
	}
	
	public Integer getInt() {
		if (value != null)
			return Integer.parseInt(value);
		else
			return null;
	}
	
	public Float getFloat() {
		if (value != null)
			return Float.parseFloat(value);
		else
			return null;
	}
	
	public Long getLong() {
		if (value != null)
			return Long.parseLong(value);
		else
			return null;
	}
	
	public Double getDouble() {
		if (value != null)
			return Double.parseDouble(value);
		else
			return null;
	}
	
	public Short getShort() {
		if (value != null)
			return Short.parseShort(value);
		else
			return null;
	}
	
	public Boolean getBoolean() {
		if (value != null)
			return Boolean.parseBoolean(value);
		else
			return null;
	}
}
