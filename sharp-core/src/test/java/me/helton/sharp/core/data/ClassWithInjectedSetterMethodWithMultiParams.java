package me.helton.sharp.core.data;

import me.helton.sharp.core.annotations.Inject;

public class ClassWithInjectedSetterMethodWithMultiParams {
	private SimpleBean simpleBean;
	
	@Inject
	public void setSimpleBean(SimpleBean simpleBean, String notUsed) {
		this.simpleBean = simpleBean;
	}
	
	public SimpleBean getSimpleBean() {
		return simpleBean;
	}
}
