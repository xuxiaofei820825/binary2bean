package com.github.binary2bean.decoder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.github.binary2bean.Utils;
import com.github.binary2bean.annotation.NumberRange;
import com.github.binary2bean.annotation.NBits;
import com.github.binary2bean.annotation.NBytes;

/**
 * 对类型为Number的成员变量进行解码。
 * 
 * @author xiaofei.xu
 *
 */
public class NumberFieldDecoder extends FieldDecoder<Number> {

	@Override
	public Number decode( final Field field, final FileChannel fileChannel, final ByteBuffer buffer,
			final Object instance ) throws Exception {

		NBytes readNBytes = null;
		NBits readNBits = null;
		NumberRange numberRange = null;

		// 获取成员变量的注解
		Annotation[] annotations = field.getAnnotations();

		for ( Annotation annotation : annotations ) {
			if ( annotation instanceof NBytes ) {
				readNBytes = (NBytes) annotation;
			}
			if ( annotation instanceof NBits ) {
				readNBits = (NBits) annotation;
			}
			if ( annotation instanceof NumberRange ) {
				numberRange = (NumberRange) annotation;
			}
		}

		Number rs = null;
		if ( readNBytes != null ) {
			rs = convertNBytesToNumber( fileChannel, buffer, readNBytes );
		}
		else if ( readNBits != null ) {
			rs = convertNBitsToNumber( fileChannel, buffer, readNBits );
		}
		else {
			// ReadNBytes 或者 ReadNBits 注解必须设置一个
			throw new RuntimeException( "ReadNBytes or ReadNBits annotation is required." );
		}

		if ( numberRange != null &&
				( rs.longValue() < numberRange.min() || rs.longValue() > numberRange.max() ) ) {
			throw new RuntimeException( "value is between " + numberRange.min() + " and " + numberRange.max() +
					", but " + rs.longValue() );
		}

		return rs;
	}

	// ========================================================================================
	// private functions

	/*
	 * 把比特转化为数值
	 */
	private Number convertNBitsToNumber( FileChannel fileChannel, ByteBuffer buffer, NBits readNBits )
			throws IOException {
		// 需要预读的字节数
		short byteSize = readNBits.bytes(),
				offset = readNBits.offset(),
				len = readNBits.len();

		// ready to read
		readyForRead( fileChannel, buffer, byteSize );

		// 做标记
		buffer.mark();

		// 读取字节
		byte[] bytes = new byte[byteSize];
		buffer.get( bytes );

		// 读不到预读字节数的末尾，就需要恢复position的位置
		if ( offset + len != byteSize * 8 ) {
			buffer.reset();
		}

		return Utils.getBitsAsInt( bytes, offset, len );
		// return Utils.getBitsAsIntByOrder( bytes, offset, len, FieldDecoder.BYTE_ORDER_TYPE );
	}

	/**
	 * 把字节转化为数值。只支持：2(short)，4(integer)，8(long)个字节
	 */
	private Number convertNBytesToNumber( FileChannel fileChannel, ByteBuffer buffer, NBytes readNBytes )
			throws IOException {

		final short len = readNBytes.len();

		// ready for read
		readyForRead( fileChannel, buffer, len );

		return Utils.getNumberFromByteBuffer( len, buffer );
	}
}
