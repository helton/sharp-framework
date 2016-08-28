package me.helton.sharp.core;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import me.helton.sharp.core.ServiceContainer;
import me.helton.sharp.core.data.BeanImplementsInterface;
import me.helton.sharp.core.data.BeanWithInjectedFields;
import me.helton.sharp.core.data.ClassWithInjectedSetterMethodWithMultiParams;
import me.helton.sharp.core.data.IMakerInterface;
import me.helton.sharp.core.data.ISimple;
import me.helton.sharp.core.data.ParentBean;
import me.helton.sharp.core.data.SimpleBean;

public class ServiceContainerTest {
	ServiceContainer container;

	@Before
	public void init() {
		this.container = new ServiceContainer();
	}
	
	@Test
	public void shouldResolveClasses() {
		SimpleBean obj = container.resolve(SimpleBean.class);
		assertThat(obj, notNullValue());
		assertThat(obj, instanceOf(SimpleBean.class));
	}
	
	@Test
	public void shouldResolveRegisteredInterfaces() {		
		container.register(ISimple.class, BeanImplementsInterface.class);
		ISimple obj = container.resolve(ISimple.class);
		assertThat(obj, notNullValue());
		assertThat(obj, instanceOf(BeanImplementsInterface.class));
		assertThat(obj.handle("test"), is("test"));
	}

	@Test
	public void shouldCanResolveInjectedPublicFieldsDirectly() {
		BeanWithInjectedFields obj = container.resolve(BeanWithInjectedFields.class);
		assertThat(obj, notNullValue());
		assertThat(obj, instanceOf(BeanWithInjectedFields.class));

		//testing injected fields
		assertThat(obj.publicObject, notNullValue());
		assertThat(obj.publicObject, instanceOf(SimpleBean.class));
	}

	@Test
	public void shouldResolveInjectedFieldsViaSetterMethod() {
		BeanWithInjectedFields obj = container.resolve(BeanWithInjectedFields.class);
		assertThat(obj, notNullValue());
		assertThat(obj, instanceOf(BeanWithInjectedFields.class));

		//testing injected fields
		assertThat(obj.getPrivateObject(), notNullValue());
		assertThat(obj.getPrivateObject(), instanceOf(SimpleBean.class));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowAnErrorRegisteringOnlyInterfaces() {
		container.register(ISimple.class, ISimple.class);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowAnExceptionRegisteringOnlyClasses() {
		container.register(BeanImplementsInterface.class, BeanImplementsInterface.class);
	}

	@Test
	public void shouldResolveByDelegateMethodToAnInterface() {
		final String message = "Called delegateMethod provided";
		final StringBuilder builder = new StringBuilder();
		Supplier<IMakerInterface> delegateMethod = () -> {
			builder.append(message);
			return new ParentBean();
		};
		container.register(IMakerInterface.class, delegateMethod);
		container.resolve(IMakerInterface.class);
		assertThat(builder.toString(), is(message));
	}
	
	@Test
	public void shouldResolveByDelegateMethodToAnClass() {
		final String message = "Called delegateMethod provided";
		final StringBuilder spy = new StringBuilder();
		Supplier<ParentBean> delegateMethod = () -> {
			spy.append(message);
			return new ParentBean();
		};
		container.register(ParentBean.class, delegateMethod);
		container.resolve(ParentBean.class);
		assertThat(spy.toString(), is(message));
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowAnExceptionWhenTriesToResolveInjectedSetterMethodWithMultiParams() {
		container.resolve(ClassWithInjectedSetterMethodWithMultiParams.class);
	}

}
