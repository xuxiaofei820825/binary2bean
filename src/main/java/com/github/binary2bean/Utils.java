package com.github.binary2bean;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.binary2bean.decoder.FieldDecoder;

public abstract class Utils {

	private final static Logger logger = LoggerFactory.getLogger( "TOOL" );

	/**
	 * 把指定字节数组中某段bit转化为整数。<br/>
	 * 注意：<br/>
	 * <ul>
	 * <li>当前只支持4个字节，且比特跨度len <= 8 ，即1个字节内。</li>
	 * <li>不考虑byte数组的大小端问题</li>
	 * </ul>
	 * 
	 * @param bytes
	 *          字节数组
	 * @param offset
	 *          bit偏移量。注意：从0开始
	 * @param len
	 *          bit的长度
	 * @return 转化后的整数
	 */
	public static int getBitsAsInt( final byte[] bytes, final int offset, final int len ) {

		// 只支持4个字节，且比特跨度8以内
		if ( bytes.length > 4 || len > 8 )
			throw new RuntimeException( "length of bytes <= 4 and len of bits <= 8" );

		if ( offset > bytes.length * 8 )
			throw new RuntimeException( "offset is out of range. offset <= " + bytes.length * 8 +
					", but offset is " + offset );

		if ( offset + len - 1 > bytes.length * 8 )
			throw new RuntimeException( "offset + len is out of range. offset: " + offset + ", len: " + len );

		// 获取字节数组的长度
		final int bytesLen = bytes.length;
		final byte[] intBytes = new byte[4];

		// 计算出需要前面补的字节数
		final int byteCntToPrepend = 4 - bytesLen;

		// 补0
		for ( int idx = 0; idx < byteCntToPrepend; idx++ ) {
			intBytes[idx] = 0x00;
		}
		System.arraycopy( bytes, 0, intBytes, byteCntToPrepend, bytes.length );

		int tmp = ByteBuffer.wrap( intBytes ).getInt();

		// 需要把前面补的0也要去掉
		final int leftLen = byteCntToPrepend * 8 + offset;
		// 左移去头
		tmp = tmp << leftLen;
		// 右移去尾(高位补0)
		tmp = tmp >>> leftLen + bytes.length * 8 - ( offset + len );

		return tmp;
	}

	public static int getBitsAsIntByOrder( final byte[] bytes, final int offset, final int len, final ByteOrder bo ) {

		// 只支持2个字节，且比特跨度8以内
		if ( bytes.length > 2 || len > 8 )
			throw new RuntimeException( "length of bytes <= 2 and len of bits <= 8" );

		if ( offset > bytes.length * 8 )
			throw new RuntimeException( "offset is out of range. offset <= " + bytes.length * 8 +
					", but offset is " + offset );

		if ( offset + len - 1 > bytes.length * 8 )
			throw new RuntimeException( "offset + len is out of range. offset: " + offset + ", len: " + len );

		int intTmp = 0;

		if ( bytes.length == 1 ) {
			byte tmp = ByteBuffer.wrap( bytes )
					.order( bo ).get();
			// 补0，转化为Integer
			intTmp = tmp | 0x00000000;
		}
		if ( bytes.length == 2 ) {
			short tmp = ByteBuffer.wrap( bytes )
					.order( bo ).getShort();
			// 补0，转化为Integer
			intTmp = tmp | 0x00000000;
		}

		// 需要把前面补的0也要去掉
		final int leftLen = ( 4 - bytes.length ) * 8 + ( bytes.length * 8 - offset - len );
		// 左移去头
		intTmp = intTmp << leftLen;
		// 右移去尾(高位补0)
		intTmp = intTmp >>> leftLen + offset;

		return intTmp;
	}

	public static Number getNumberFromByteBuffer( final int len, final ByteBuffer buffer ) {

		if ( len == 1 ) {
			return buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).get();
		}
		if ( len == 2 ) {
			return buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).getShort();
		}
		if ( len == 3 ) {
			byte[] bytes = new byte[] { 0x00, buffer.get(), buffer.get(), buffer.get() };
			return ByteBuffer.wrap( bytes ).getInt();
		}
		if ( len == 4 ) {
			return buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).getInt();
		}
		if ( len == 8 ) {
			return buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).getLong();
		}

		throw new RuntimeException( "Unsuported number type. len: " + len );
	}

	public static Logger getLogger() {
		return logger;
	}
}
