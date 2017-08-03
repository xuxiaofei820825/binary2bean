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
import com.github.binary2bean.annotation.Text;

import lombok.Data;

public class StringFieldDecoderTest {

	private static final String TEST_FILE = "TEST_FILE";

	private static final String TEXT = "ABCDEFG";

	@Before
	public void setUp() throws Exception {
		String text = new String( TEXT );
		File file = new File( TEST_FILE );

		OutputStream os = new FileOutputStream( file );

		ByteBuffer buffer = ByteBuffer.allocate( 2 );
		buffer.order( FieldDecoder.BYTE_ORDER_TYPE ).putShort( (short) text.getBytes().length );

		os.write( buffer.array() );
		os.write( text.getBytes() );
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
		StringFieldTestBean s = decoder.decode( StringFieldTestBean.class );

		aFile.close();

		// 断言
		Assert.assertEquals( TEXT, s.getName() );
	}

	@Data
	static class StringFieldTestBean {

		@NBytes(len = 2)
		@Text(minLength = 0, maxLength = 255)
		private String name;
	}
}
