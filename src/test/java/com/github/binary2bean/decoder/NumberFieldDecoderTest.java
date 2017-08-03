package com.github.binary2bean.decoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.binary2bean.annotation.NBytes;

import lombok.Data;

public class NumberFieldDecoderTest {

	private static final String TEST_FILE = "TEST_FILE";
	private static final int BYTE_LEN = 1;
	private static final int SHORT_LEN = 2;
	private static final int INT_LEN = 4;
	private static final int LONG_LEN = 8;

	private static final byte BYTE_VALUE = 1;
	private static final short SHORT_VALUE = 35;
	private static final int INT_VALUE = -6081;
	private static final long LONG_VALUE = -300280000L;

	@Before
	public void setUp() throws Exception {

		File file = new File( TEST_FILE );

		OutputStream os = new FileOutputStream( file );

		{
			ByteBuffer buffer = ByteBuffer.allocate( BYTE_LEN );
			buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).put( BYTE_VALUE );
			os.write( buffer.array() );
		}

		{
			ByteBuffer buffer = ByteBuffer.allocate( SHORT_LEN );
			buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( SHORT_VALUE );
			os.write( buffer.array() );
		}

		{
			ByteBuffer buffer = ByteBuffer.allocate( INT_LEN );
			buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putInt( INT_VALUE );
			os.write( buffer.array() );
		}

		{
			ByteBuffer buffer = ByteBuffer.allocate( LONG_LEN );
			buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putLong( LONG_VALUE );
			os.write( buffer.array() );
		}

		os.flush();
		os.close();
	}

	@After
	public void tearDown() throws Exception {
		File file = new File( TEST_FILE );
		file.delete();
	}

	@Test
	public void test() throws Exception {
		File file = new File( TEST_FILE );

		RandomAccessFile aFile = new RandomAccessFile( file, "rw" );
		FileChannel inChannel = aFile.getChannel();

		ByteBuffer buf = ByteBuffer.allocate( 1024 );
		inChannel.read( buf );

		BeanDecoder decoder = new BeanDecoder( inChannel, buf );
		NumberFieldTestBean s = decoder.decode( NumberFieldTestBean.class );

		aFile.close();

		// 断言
		Assert.assertEquals( BYTE_VALUE, s.getNumberByte() );
		Assert.assertEquals( SHORT_VALUE, s.getNumberShort() );
		Assert.assertEquals( INT_VALUE, s.getNumberInt() );
		Assert.assertEquals( LONG_VALUE, s.getNumberLong() );
	}

	@Data
	static class NumberFieldTestBean {

		@NBytes(len = BYTE_LEN)
		private Number numberByte;

		@NBytes(len = SHORT_LEN)
		private Number numberShort;

		@NBytes(len = INT_LEN)
		private Number numberInt;

		@NBytes(len = LONG_LEN)
		private Number numberLong;
	}

}
