package com.github.binary2bean.decoder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import com.github.binary2bean.Utils;
import com.github.binary2bean.annotation.BCDCode;
import com.github.binary2bean.annotation.NBytes;
import com.github.binary2bean.annotation.Text;

public class StringFieldDecoder extends FieldDecoder<String> {

	@Override
	public String decode( final Field field, final FileChannel fileChannel, final ByteBuffer buffer,
			final Object instance ) throws Exception {

		NBytes readNBytes = null;
		Text textLength = null;
		BCDCode bcdCode = null;

		// 获取成员变量的注解
		Annotation[] annotations = field.getAnnotations();

		for ( Annotation annotation : annotations ) {
			if ( NBytes.class.isInstance( annotation ) ) {
				readNBytes = (NBytes) annotation;
			}
			if ( Text.class.isInstance( annotation ) ) {
				textLength = (Text) annotation;
			}
			if ( BCDCode.class.isInstance( annotation ) ) {
				bcdCode = (BCDCode) annotation;
			}
		}

		// 读取用于判断字符长度的字节数
		final int byteLen = readNBytes.len();

		// 如果字节数为0，则返回空白字符串
		if ( byteLen == 0 )
			return "";

		if ( bcdCode != null ) {
			// BCDCode String
			return decodeBCDCodeString( fileChannel, buffer );
		}
		else if ( textLength != null ) {
			// Text String
			return decodeTextString( fileChannel, buffer, textLength, byteLen );
		}
		else {
			throw new RuntimeException( "Text or BCDCode annotation is required." );
		}
	}

	private String decodeBCDCodeString( final FileChannel fileChannel, ByteBuffer buffer ) throws IOException {
		final int len = 6;

		// 准备读取文本
		readyForRead( fileChannel, buffer, len );

		StringBuilder sb = new StringBuilder();

		for ( int idx = 0; idx < len; idx++ ) {
			// 读取一个byte
			byte[] r = new byte[] { buffer.get() };

			// 截取4bit
			int first = Utils.getBitsAsInt( r, 0, 4 );
			if ( first == 15 )
				continue;
			sb.append( first );

			// 截取4bit
			int second = Utils.getBitsAsInt( r, 4, 4 );
			if ( second == 15 )
				continue;
			sb.append( second );
		}

		return sb.toString();
	}

	private String decodeTextString( final FileChannel fileChannel, ByteBuffer buffer, Text textLength,
			final int byteLen ) throws IOException {

		int min = textLength.minLength(); // 最小值
		int max = textLength.maxLength(); // 最大值

		// ready for read
		readyForRead( fileChannel, buffer, byteLen );

		final int len = Utils.getNumberFromByteBuffer( byteLen, buffer ).intValue();

		// log
		Utils.getLogger().debug( "Text length: {}", len );

		// 判定字节数的范围
		if ( len < min || len > max ) {
			throw new RuntimeException( "Text length is between " + min + " and " + max + ", but " + len );
		}

		byte[] text = new byte[len];

		// 准备读取文本
		readyForRead( fileChannel, buffer, len );

		// 读取文本
		buffer.get( text );

		return new String( text, Charset.forName( "UTF-8" ) );
	}
}
