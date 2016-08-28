package me.helton.sharp.core.data;

import me.helton.sharp.core.annotations.Inject;

public class BeanWithInjectedFields {

	private SimpleBean privateObject;
	@Inject
	public SimpleBean publicObject;
	
	public SimpleBean getPrivateObject() {
		return privateObject;
	}

	@Inject
	public void setPrivateObject(SimpleBean simpleObject) {
		this.privateObject = simpleObject;
	}
	
}
