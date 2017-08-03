package com.github.binary2bean.decoder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public abstract class FieldDecoder<T> {

	public static final ByteOrder BYTE_ORDER_TYPE = ByteOrder.BIG_ENDIAN;

	public abstract T decode( Field field, FileChannel fileChannel, ByteBuffer buffer, Object instance ) throws Exception;

	/**
	 * 保证缓冲区够读byteSize指定的字节数
	 * 
	 * @param fileChannel
	 *          文件通道
	 * @param buffer
	 *          缓冲区
	 * @param byteSize
	 *          需要读取的字节数
	 * @throws IOException
	 */
	public static void readyForRead( final FileChannel fileChannel, final ByteBuffer buffer,
			final int byteSize ) throws IOException {

		if ( buffer.remaining() >= byteSize )
			return;

		// log
		System.out.println( "########## reading file for filling the bytebuffer ......  " );

		// 如果缓冲区可读取的字节数 < 需要读取的字节数
		// 则循环读取文件，直到读到文件结尾，或者获得所需的字节数
		int readBytes = 0;
		do {
			// 重置缓冲区
			if ( buffer.hasRemaining() )
				buffer.compact();
			else
				buffer.clear();

			// 读取一次
			readBytes = fileChannel.read( buffer );

			// ready to read
			buffer.flip();
		} while ( buffer.remaining() < byteSize && readBytes != -1 );

		// 可是还达不到，可能说文件本身有问题
		if ( buffer.remaining() < byteSize )
			throw new RuntimeException( "File format is error." );
	}
}
