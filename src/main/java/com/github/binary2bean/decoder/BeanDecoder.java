package com.github.binary2bean.decoder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joor.Reflect;

import com.github.binary2bean.Utils;
import com.github.binary2bean.annotation.IgnoreIfNotEquals;
import com.github.binary2bean.annotation.Item;
import com.github.binary2bean.annotation.NBytes;
import com.github.binary2bean.annotation.Size;

public class BeanDecoder {

	/** Channel */
	private FileChannel fileChannel;

	/** ByteBuffer */
	private ByteBuffer buffer;

	public BeanDecoder( FileChannel fileChannel, ByteBuffer buffer ) {
		this.fileChannel = fileChannel;
		this.buffer = buffer;

		// ready to read
		this.buffer.flip();
	}

	/**
	 * 解码二进制数为Bean
	 * 
	 * @param clazz
	 *          Bean的类定义
	 * @return Bean的实例
	 * @throws Exception
	 */
	public <T> T decode( final Class<T> clazz ) throws Exception {

		// log
		Utils.getLogger().info( "====== Class: " + clazz.getName() + " ======" );

		// 创建指定类的实例
		T instance = Reflect.on( clazz ).create().get();

		// 获取该实例的所有属性(顺次的)
		Map<String, Reflect> fields = Reflect.on( instance ).fields();

		for ( String key : fields.keySet() ) {

			// 获取成员变量的类型
			Field field = clazz.getDeclaredField( key );
			Class<?> fieldClazz = field.getType();

			// 项目描述
			final String description = getItemDescription( field );

			boolean isIgnore = isIgnoreField( field, instance );
			if ( isIgnore )
				continue;

			// 基本类型：String, Number
			// 根据成员变量的类型，设置对应的字段解码器
			FieldDecoder<?> fieldDecoder = null;
			if ( ( "java.lang.String" ).equals( fieldClazz.getName() ) ) {
				// 1.String成员变量
				fieldDecoder = new StringFieldDecoder();
			}
			else if ( ( "java.lang.Number" ).equals( fieldClazz.getName() ) ) {
				// 2.Number成员变量
				fieldDecoder = new NumberFieldDecoder();
			}
			else if ( ( "java.util.List" ).equals( fieldClazz.getName() ) ) {
				// 3.List成员变量

				List<Object> list = decodeListProperty( field );

				// 设置属性的值
				Reflect.on( instance ).set( key, list );

				// 继续下一个属性
				continue;
			}
			else {
				// 4.自定义Bean成员变量

				Object beanValue = decodeCustomerBean( field );

				// 设置属性的值
				Reflect.on( instance ).set( key, beanValue );

				// 继续下一个属性
				continue;
			}

			// 解码
			Object value = fieldDecoder.decode( field, fileChannel, buffer, instance );

			// 设置属性的值
			Reflect.on( instance ).set( key, value );

			// log
			Utils.getLogger().info( "{} = {}, Item:{}", key, value, description );
		}

		return instance;
	}

	// ==============================================================================
	// private functions

	/*
	 * 解码自定义Bean
	 */
	private Object decodeCustomerBean( final Field field ) throws Exception {

		Class<?> fieldClazz = field.getType();

		// 递归调用，解析自定义Bean
		Object beanValue = this.decode( fieldClazz );

		return beanValue;
	}

	/**
	 * 解码List类型的成员变量
	 */
	private List<Object> decodeListProperty( final Field field ) throws Exception {

		// 获取List的Size
		int size = getSizeOfList( field );

		// log
		Utils.getLogger().debug( "Size of List: {}", size );

		// 创建List的实例
		List<Object> list = new ArrayList<Object>( size );

		// 获取List的泛型的类型
		Type fc = field.getGenericType();

		if ( !( fc instanceof ParameterizedType ) ) {
			throw new RuntimeException( "GenericType is not ParameterizedType." );
		}

		// 如果是泛型参数的类型
		ParameterizedType pt = (ParameterizedType) fc;

		// 得到泛型里的Class类型对象。
		Class<?> genericClazz = (Class<?>) pt.getActualTypeArguments()[0];

		// log
		Utils.getLogger().debug( "Class of List bean: {}", genericClazz.getName() );

		for ( int idx = 1; idx <= size; idx++ ) {

			// log
			Utils.getLogger().info( "====== Record #{}", idx );

			// 递归调用，解析自定义Bean
			Object beanValue = this.decode( genericClazz );

			list.add( beanValue );
		}

		return list;
	}

	private String getItemDescription( final Field field ) {

		// 获取数组的大小
		Item description = null;

		// 获取成员变量的注解
		Annotation[] annotations = field.getAnnotations();

		for ( Annotation annotation : annotations ) {
			if ( Item.class.isInstance( annotation ) ) {
				description = (Item) annotation;
			}
		}

		if ( description != null )
			return description.value();

		return "";
	}

	/**
	 * 获取List的大小
	 */
	private int getSizeOfList( final Field field ) throws IOException {

		// 获取数组的大小
		NBytes readNBytes = null;
		Size sizeAnno = null;

		// 获取成员变量的注解
		Annotation[] annotations = field.getAnnotations();

		for ( Annotation annotation : annotations ) {
			if ( NBytes.class.isInstance( annotation ) ) {
				readNBytes = (NBytes) annotation;
			}
			if ( Size.class.isInstance( annotation ) ) {
				sizeAnno = (Size) annotation;
			}
		}

		if ( readNBytes == null || sizeAnno == null ) {
			throw new RuntimeException( "ReadNBytes and Size annotation is required." );
		}

		int byteLen = readNBytes.len(); // 字节数

		// ready for read
		FieldDecoder.readyForRead( fileChannel, buffer, byteLen );

		// 获取文本的字节数
		int size = Utils.getNumberFromByteBuffer( byteLen, buffer ).intValue();

		if ( size < sizeAnno.min() || size > sizeAnno.max() ) {
			throw new RuntimeException( "Size of list is between " + sizeAnno.min() + " and " + sizeAnno.max() );
		}
		return size;
	}

	private boolean isIgnoreField( final Field field, final Object instance ) {

		IgnoreIfNotEquals ignoreIfNotEquals = null;

		// 获取成员变量的注解
		Annotation[] annotations = field.getAnnotations();

		for ( Annotation annotation : annotations ) {
			if ( IgnoreIfNotEquals.class.isInstance( annotation ) ) {
				ignoreIfNotEquals = (IgnoreIfNotEquals) annotation;
				break;
			}
		}

		if ( ignoreIfNotEquals == null )
			return false;

		final String property = ignoreIfNotEquals.property();
		final int value = ignoreIfNotEquals.value();

		// 获取属性值
		Number realValue = Reflect.on( instance ).get( property );

		return realValue.intValue() != value;
	}
}
