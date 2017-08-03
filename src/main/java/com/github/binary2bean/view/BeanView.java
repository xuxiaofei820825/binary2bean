package com.github.binary2bean.view;

public interface BeanView<T> {
	void display( T bean ) throws Exception;
}
