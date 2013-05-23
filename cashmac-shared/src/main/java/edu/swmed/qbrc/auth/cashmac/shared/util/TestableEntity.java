package edu.swmed.qbrc.auth.cashmac.shared.util;

import java.io.Serializable;
import java.util.List;

public interface TestableEntity<T extends Serializable> {

	public void testInput(List<TestableEntityString> input);
	public List<TestableEntityString> testOutput();
	
}
